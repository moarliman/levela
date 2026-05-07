package com.levela.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.levela.app.ble.BleManager
import com.levela.app.ble.BleState
import com.levela.app.ble.LevelData
import kotlinx.coroutines.flow.StateFlow

class LevelViewModel(app: Application) : AndroidViewModel(app) {
    private val ble = BleManager(app)

    val state: StateFlow<BleState> = ble.state
    val level: StateFlow<LevelData> = ble.level

    fun startScan()   = ble.startScan()
    fun disconnect()  = ble.disconnect()
    fun calibrate()   = ble.calibrate()
    fun clearCal()    = ble.clearCalibration()

    override fun onCleared() { ble.disconnect() }
}
