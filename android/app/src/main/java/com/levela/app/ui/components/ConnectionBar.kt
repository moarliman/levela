package com.levela.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.levela.app.ble.BleState

@Composable
fun ConnectionBar(
    state: BleState,
    onScan: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = stateColor(state).copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stateLabel(state),
                style = MaterialTheme.typography.bodyMedium,
                color = stateColor(state)
            )
            when (state) {
                is BleState.Idle, is BleState.Error ->
                    Button(onClick = onScan) { Text("Scan") }
                is BleState.Connected ->
                    OutlinedButton(onClick = onDisconnect) { Text("Disconnect") }
                else -> {}
            }
        }
    }
}

private fun stateLabel(s: BleState) = when (s) {
    is BleState.Idle         -> "Not connected"
    is BleState.Scanning     -> "Scanning…"
    is BleState.Found        -> "Found ${s.name}"
    is BleState.Connecting   -> "Connecting…"
    is BleState.Connected    -> "Connected"
    is BleState.Reconnecting -> "Reconnecting…"
    is BleState.Error        -> "Error: ${s.msg}"
}

private fun stateColor(s: BleState) = when (s) {
    is BleState.Connected    -> Color(0xFF2ECC71)
    is BleState.Error        -> Color(0xFFE74C3C)
    is BleState.Reconnecting -> Color(0xFFF1C40F)
    else                     -> Color(0xFF888888)
}
