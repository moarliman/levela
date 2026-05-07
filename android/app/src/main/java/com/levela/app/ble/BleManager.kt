package com.levela.app.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder

@SuppressLint("MissingPermission")
class BleManager(private val ctx: Context) {

    private val btMgr = ctx.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? get() = btMgr.adapter
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _state = MutableStateFlow<BleState>(BleState.Idle)
    val state: StateFlow<BleState> = _state.asStateFlow()

    private val _level = MutableStateFlow(LevelData())
    val level: StateFlow<LevelData> = _level.asStateFlow()

    private var gatt: BluetoothGatt? = null
    private var lastDeviceAddr: String? = null
    private var reconnectJob: Job? = null

    fun startScan() {
        val a = adapter ?: run { _state.value = BleState.Error("Bluetooth not available"); return }
        if (!a.isEnabled) { _state.value = BleState.Error("Bluetooth disabled"); return }
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(BleUuids.SERVICE))
            .build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        _state.value = BleState.Scanning
        a.bluetoothLeScanner.startScan(listOf(filter), settings, scanCb)
    }

    fun stopScan() {
        adapter?.bluetoothLeScanner?.stopScan(scanCb)
    }

    private val scanCb = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            stopScan()
            val dev = result.device
            lastDeviceAddr = dev.address
            _state.value = BleState.Found(dev.name ?: "Levela", dev.address)
            connectTo(dev)
        }

        override fun onScanFailed(errorCode: Int) {
            _state.value = BleState.Error("Scan failed: $errorCode")
        }
    }

    private fun connectTo(device: BluetoothDevice) {
        _state.value = BleState.Connecting
        gatt = device.connectGatt(ctx, false, gattCb, BluetoothDevice.TRANSPORT_LE)
    }

    private val gattCb = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    _state.value = BleState.Connected
                    g.requestMtu(64)
                    g.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    g.close()
                    gatt = null
                    _state.value = BleState.Reconnecting
                    scheduleReconnect()
                }
            }
        }

        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            val dataChar = g.getService(BleUuids.SERVICE)
                ?.getCharacteristic(BleUuids.CHAR_DATA) ?: return
            g.setCharacteristicNotification(dataChar, true)
            val cccd = dataChar.getDescriptor(BleUuids.CCCD)
            @Suppress("DEPRECATION")
            cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            @Suppress("DEPRECATION")
            g.writeDescriptor(cccd)
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(
            g: BluetoothGatt,
            c: BluetoothGattCharacteristic
        ) {
            if (c.uuid == BleUuids.CHAR_DATA) parseAndEmit(c.value)
        }

        override fun onCharacteristicChanged(
            g: BluetoothGatt,
            c: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            if (c.uuid == BleUuids.CHAR_DATA) parseAndEmit(value)
        }
    }

    private fun parseAndEmit(b: ByteArray) {
        if (b.size < 8) return
        val bb = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN)
        val roll  = bb.float
        val pitch = bb.float
        _level.value = LevelData(roll, pitch)
    }

    fun calibrate() = writeCmd(BleUuids.CMD_CALIBRATE)

    fun clearCalibration() = writeCmd(BleUuids.CMD_CLEAR_CAL)

    @Suppress("DEPRECATION")
    private fun writeCmd(b: Byte) {
        val g = gatt ?: return
        val ch = g.getService(BleUuids.SERVICE)
            ?.getCharacteristic(BleUuids.CHAR_CMD) ?: return
        ch.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        ch.value = byteArrayOf(b)
        g.writeCharacteristic(ch)
    }

    private fun scheduleReconnect() {
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            var delayMs = 1_000L
            while (isActive && _state.value !is BleState.Connected) {
                delay(delayMs)
                val addr = lastDeviceAddr
                if (addr != null) {
                    val dev = adapter?.getRemoteDevice(addr) ?: continue
                    connectTo(dev)
                } else {
                    startScan()
                }
                delayMs = (delayMs * 2).coerceAtMost(15_000L)
            }
        }
    }

    fun disconnect() {
        reconnectJob?.cancel()
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        _state.value = BleState.Idle
    }
}
