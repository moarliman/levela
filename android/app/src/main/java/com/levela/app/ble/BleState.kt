package com.levela.app.ble

sealed interface BleState {
    data object Idle         : BleState
    data object Scanning     : BleState
    data class  Found(val name: String, val address: String) : BleState
    data object Connecting   : BleState
    data object Connected    : BleState
    data object Reconnecting : BleState
    data class  Error(val msg: String) : BleState
}

data class LevelData(val rollDeg: Float = 0f, val pitchDeg: Float = 0f)
