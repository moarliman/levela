package com.levela.app.ble

import java.util.UUID

object BleUuids {
    val SERVICE: UUID   = UUID.fromString("6c65762d-0000-4000-8000-6c6576656c61")
    val CHAR_DATA: UUID = UUID.fromString("6c65762d-0001-4000-8000-6c6576656c61")
    val CHAR_CMD: UUID  = UUID.fromString("6c65762d-0002-4000-8000-6c6576656c61")
    val CCCD: UUID      = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    const val CMD_CALIBRATE: Byte = 0x01
    const val CMD_CLEAR_CAL: Byte = 0x02
}
