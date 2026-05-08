package com.levela.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.levela.app.ble.BleState

@Composable
fun ConnectionBar(
    state: BleState,
    onScan: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dotColor  = stateColor(state)
    val textColor = MaterialTheme.colorScheme.onSurface

    Surface(
        modifier      = modifier.fillMaxWidth(),
        shape         = RoundedCornerShape(50),
        color         = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shadowElevation = 0.dp,
        tonalElevation  = 0.dp
    ) {
        Row(
            modifier = Modifier
                .border(1.dp, dotColor.copy(alpha = 0.25f), RoundedCornerShape(50))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Status dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(dotColor, CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text  = stateLabel(state),
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }

            when (state) {
                is BleState.Idle, is BleState.Error ->
                    Button(
                        onClick = onScan,
                        shape   = RoundedCornerShape(50),
                        colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text("Scan", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                is BleState.Connected ->
                    TextButton(onClick = onDisconnect) {
                        Text("Disconnect", fontSize = 13.sp, color = textColor.copy(alpha = 0.5f))
                    }
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
    else                     -> Color(0xFFAAAAAA)
}
