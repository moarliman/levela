package com.levela.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max

private const val MAX_TILT_DEG = 10f

@Composable
fun BubbleLevel(rollDeg: Float, pitchDeg: Float, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(280.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val ringR = (size.minDimension / 2f) - 8f
        val bubbleR = ringR * 0.18f

        // Outer boundary ring
        drawCircle(
            color = Color(0xFF333333),
            radius = ringR,
            center = Offset(cx, cy),
            style = Stroke(width = 4f)
        )

        // 1-degree reference ring (level zone)
        val zoneR = ringR * (1f / MAX_TILT_DEG)
        drawCircle(
            color = Color(0xFF888888),
            radius = zoneR,
            center = Offset(cx, cy),
            style = Stroke(width = 2f)
        )

        // Crosshair
        drawLine(Color(0xFF555555), Offset(cx - ringR, cy), Offset(cx + ringR, cy), 1.5f)
        drawLine(Color(0xFF555555), Offset(cx, cy - ringR), Offset(cx, cy + ringR), 1.5f)

        // Bubble position: roll = left/right, pitch = front/back
        // Bubble moves opposite to the high side (toward the low side)
        val nx = (-rollDeg / MAX_TILT_DEG).coerceIn(-1f, 1f)
        val ny = (pitchDeg / MAX_TILT_DEG).coerceIn(-1f, 1f)
        var dx = nx * (ringR - bubbleR)
        var dy = ny * (ringR - bubbleR)
        val mag = hypot(dx, dy)
        val maxMag = ringR - bubbleR
        if (mag > maxMag) {
            dx = dx / mag * maxMag
            dy = dy / mag * maxMag
        }

        val tilt = max(abs(rollDeg), abs(pitchDeg))
        val bubbleColor = when {
            tilt < 1f -> Color(0xFF2ECC71)
            tilt < 3f -> Color(0xFFF1C40F)
            else      -> Color(0xFFE74C3C)
        }

        drawCircle(bubbleColor, radius = bubbleR, center = Offset(cx + dx, cy + dy))
        drawCircle(
            color = Color.Black,
            radius = bubbleR,
            center = Offset(cx + dx, cy + dy),
            style = Stroke(width = 2f)
        )
    }
}
