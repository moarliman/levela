package com.levela.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.levela.app.ble.BleState
import com.levela.app.ui.components.AngleReadout
import com.levela.app.ui.components.BubbleLevel
import com.levela.app.ui.components.ConnectionBar
import com.levela.app.ui.components.TrailerTopView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelScreen(vm: LevelViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val level by vm.level.collectAsStateWithLifecycle()
    val connected = state is BleState.Connected

    Scaffold(
        topBar = { TopAppBar(title = { Text("Levela") }) }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ConnectionBar(state = state, onScan = vm::startScan, onDisconnect = vm::disconnect)

            Spacer(Modifier.height(16.dp))
            TrailerTopView()

            Spacer(Modifier.height(16.dp))
            BubbleLevel(rollDeg = level.rollDeg, pitchDeg = level.pitchDeg)

            Spacer(Modifier.height(12.dp))
            AngleReadout(rollDeg = level.rollDeg, pitchDeg = level.pitchDeg)

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = vm::calibrate,
                    enabled = connected,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Calibrate")
                }
                OutlinedButton(
                    onClick = vm::clearCal,
                    enabled = connected,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset Cal")
                }
            }
        }
    }
}
