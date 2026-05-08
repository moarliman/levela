package com.levela.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

private const val READOUT_DEAD_ZONE = 0.15f

@Composable
fun AngleReadout(rollDeg: Float, pitchDeg: Float, modifier: Modifier = Modifier) {
    val roll  = if (abs(rollDeg)  < READOUT_DEAD_ZONE) 0f else rollDeg
    val pitch = if (abs(pitchDeg) < READOUT_DEAD_ZONE) 0f else pitchDeg

    Surface(
        modifier        = modifier.fillMaxWidth(),
        shape           = RoundedCornerShape(16.dp),
        color           = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shadowElevation = 0.dp,
        tonalElevation  = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AngleValue(label = "ROLL",  deg = roll)
            // Divider
            Surface(
                color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    .then(Modifier.padding(top = 4.dp))
            ) {}
            AngleValue(label = "PITCH", deg = pitch)
        }
    }
}

@Composable
private fun AngleValue(label: String, deg: Float) {
    val isLevel = deg == 0f
    val valueColor = when {
        isLevel        -> Color(0xFF27AE60)
        abs(deg) < 1f  -> MaterialTheme.colorScheme.onSurface
        abs(deg) < 3f  -> Color(0xFFF39C12)
        else           -> Color(0xFFE74C3C)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text       = if (isLevel) "0.0°" else "%+.1f°".format(deg),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize   = 26.sp,
            color      = valueColor
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
            letterSpacing = 1.5.sp
        )
    }
}
