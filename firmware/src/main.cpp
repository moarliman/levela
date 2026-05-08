#include <Arduino.h>
#include <Wire.h>
#include <Preferences.h>
#include <NimBLEDevice.h>
#include <I2Cdev.h>
#include <MPU6050.h>
#include <math.h>

static const char* SVC_UUID  = "6c65762d-0000-4000-8000-6c6576656c61";
static const char* DATA_UUID = "6c65762d-0001-4000-8000-6c6576656c61";
static const char* CMD_UUID  = "6c65762d-0002-4000-8000-6c6576656c61";

static const int I2C_SDA = 21;
static const int I2C_SCL = 22;

MPU6050 mpu;

Preferences prefs;
float ax_off = 0, ay_off = 0, az_off = 0;

volatile bool calibrateRequested = false;
volatile bool clearCalRequested  = false;

NimBLECharacteristic* dataChar = nullptr;
bool clientConnected = false;

static void loadCalibration() {
    prefs.begin("levela", true);
    ax_off = prefs.getFloat("ax", 0.0f);
    ay_off = prefs.getFloat("ay", 0.0f);
    az_off = prefs.getFloat("az", 0.0f);
    prefs.end();
    Serial.printf("Loaded cal: ax=%.4f ay=%.4f az=%.4f\n", ax_off, ay_off, az_off);
}

static void saveCalibration() {
    prefs.begin("levela", false);
    prefs.putFloat("ax", ax_off);
    prefs.putFloat("ay", ay_off);
    prefs.putFloat("az", az_off);
    prefs.end();
    Serial.printf("Saved cal: ax=%.4f ay=%.4f az=%.4f\n", ax_off, ay_off, az_off);
}

class ServerCb : public NimBLEServerCallbacks {
    void onConnect(NimBLEServer* s) override {
        clientConnected = true;
        Serial.println("BLE connect");
    }
    void onDisconnect(NimBLEServer* s) override {
        clientConnected = false;
        Serial.println("BLE disconnect");
        NimBLEDevice::startAdvertising();
    }
};

class CmdCb : public NimBLECharacteristicCallbacks {
    void onWrite(NimBLECharacteristic* c) override {
        std::string v = c->getValue();
        if (v.empty()) return;
        uint8_t op = (uint8_t)v[0];
        if (op == 0x01) {
            calibrateRequested = true;
            Serial.println("CMD: calibrate");
        } else if (op == 0x02) {
            clearCalRequested = true;
            Serial.println("CMD: clear calibration");
        }
    }
};

struct Tilt { float roll_deg; float pitch_deg; };

// EMA smoothing: lower alpha = smoother but more lag (0.1 = heavy, 0.3 = moderate)
static const float ALPHA = 0.05f;
static float filtered_roll  = 0.0f;
static float filtered_pitch = 0.0f;
static bool  filter_init    = false;

static Tilt readTilt() {
    int16_t rx, ry, rz;
    mpu.getAcceleration(&rx, &ry, &rz);
    float ax = (float)rx / 16384.0f - ax_off;
    float ay = (float)ry / 16384.0f - ay_off;
    float az = (float)rz / 16384.0f - az_off;

    float roll  = atan2f(ay, az) * 180.0f / (float)M_PI;
    float pitch = atan2f(-ax, sqrtf(ay * ay + az * az)) * 180.0f / (float)M_PI;

    if (!filter_init) {
        filtered_roll  = roll;
        filtered_pitch = pitch;
        filter_init    = true;
    } else {
        filtered_roll  = ALPHA * roll  + (1.0f - ALPHA) * filtered_roll;
        filtered_pitch = ALPHA * pitch + (1.0f - ALPHA) * filtered_pitch;
    }
    return { filtered_roll, filtered_pitch };
}

static void doCalibrate() {
    const int N = 50;
    float sx = 0, sy = 0, sz = 0;
    for (int i = 0; i < N; ++i) {
        int16_t rx, ry, rz;
        mpu.getAcceleration(&rx, &ry, &rz);
        sx += (float)rx / 16384.0f;
        sy += (float)ry / 16384.0f;
        sz += (float)rz / 16384.0f;
        delay(5);
    }
    ax_off = sx / N;
    ay_off = sy / N;
    az_off = sz / N - 1.0f;
    saveCalibration();
}

static void doClearCalibration() {
    ax_off = ay_off = az_off = 0.0f;
    saveCalibration();
}

void setup() {
    Serial.begin(115200);
    delay(200);

    Wire.begin(I2C_SDA, I2C_SCL);
    Wire.setClock(400000);
    mpu.initialize();
    if (!mpu.testConnection()) {
        Serial.println("MPU6050 not found — check wiring!");
    } else {
        Serial.println("MPU6050 OK");
    }
    mpu.setFullScaleAccelRange(MPU6050_ACCEL_FS_2);

    loadCalibration();

    uint64_t mac = ESP.getEfuseMac();
    char name[20];
    snprintf(name, sizeof(name), "Levela-%02X%02X",
             (uint8_t)(mac >> 8), (uint8_t)mac);

    NimBLEDevice::init(name);
    NimBLEDevice::setPower(ESP_PWR_LVL_P7);

    NimBLEServer* server = NimBLEDevice::createServer();
    server->setCallbacks(new ServerCb());

    NimBLEService* svc = server->createService(SVC_UUID);

    dataChar = svc->createCharacteristic(
        DATA_UUID,
        NIMBLE_PROPERTY::READ | NIMBLE_PROPERTY::NOTIFY);

    NimBLECharacteristic* cmdChar = svc->createCharacteristic(
        CMD_UUID,
        NIMBLE_PROPERTY::WRITE);
    cmdChar->setCallbacks(new CmdCb());

    svc->start();

    NimBLEAdvertising* adv = NimBLEDevice::getAdvertising();
    adv->addServiceUUID(SVC_UUID);
    adv->setName(name);
    adv->setScanResponse(true);
    NimBLEDevice::startAdvertising();
    Serial.printf("Advertising as %s\n", name);
}

void loop() {
    static uint32_t last = 0;
    uint32_t now = millis();
    if (now - last < 50) { delay(1); return; }
    last = now;

    if (calibrateRequested) { calibrateRequested = false; doCalibrate(); }
    if (clearCalRequested)  { clearCalRequested  = false; doClearCalibration(); }

    Tilt t = readTilt();

    if (clientConnected && dataChar) {
        uint8_t buf[8];
        memcpy(buf,     &t.roll_deg,  4);
        memcpy(buf + 4, &t.pitch_deg, 4);
        dataChar->setValue(buf, 8);
        dataChar->notify();
    }
}
