package com.levela.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TrailerTopView(modifier: Modifier = Modifier) {
    val tm = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 10.sp, color = Color(0xFF333333))
    val smallStyle = TextStyle(fontSize = 9.sp, color = Color(0xFFAAAAAA))

    Canvas(modifier = modifier.fillMaxWidth().height(200.dp)) {
        val pad = 36f
        val w = size.width - pad * 2
        val h = size.height - pad * 2
        val left = pad
        val top = pad

        // Trailer body fill
        drawRect(Color(0xFFF5EFE0), topLeft = Offset(left, top), size = Size(w, h))
        // Trailer body border
        drawRect(
            Color(0xFF333333),
            topLeft = Offset(left, top),
            size = Size(w, h),
            style = Stroke(width = 3f)
        )

        // Hitch triangle at front (left side)
        val hitchPath = Path().apply {
            moveTo(left, top + h * 0.28f)
            lineTo(left - 28f, top + h * 0.5f)
            lineTo(left, top + h * 0.72f)
            close()
        }
        drawPath(hitchPath, Color(0xFF555555))

        // Wheels near rear (right side)
        val wheelW = w * 0.08f
        val wheelH = h * 0.20f
        val wheelX = left + w * 0.68f
        drawRect(Color(0xFF222222), topLeft = Offset(wheelX, top - wheelH * 0.3f),  size = Size(wheelW, wheelH))
        drawRect(Color(0xFF222222), topLeft = Offset(wheelX, top + h - wheelH * 0.7f), size = Size(wheelW, wheelH))

        // Dashed center lines
        val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
        drawLine(
            Color(0xFFBBBBBB),
            Offset(left, top + h / 2f), Offset(left + w, top + h / 2f),
            1.5f, pathEffect = dash
        )
        drawLine(
            Color(0xFFBBBBBB),
            Offset(left + w / 2f, top), Offset(left + w / 2f, top + h),
            1.5f, pathEffect = dash
        )

        // ESP32 footprint (small dark rectangle near center)
        val ex = left + w * 0.42f
        val ey = top + h * 0.38f
        val ew = w * 0.16f
        val eh = h * 0.24f
        drawRect(Color(0xFF2C3E50), topLeft = Offset(ex, ey), size = Size(ew, eh))
        drawText(tm, "ESP32", Offset(ex + 2f, ey + eh / 2f - 6f),
            style = TextStyle(fontSize = 7.sp, color = Color.White))

        // X axis arrow pointing toward hitch (left)
        val midY = ey + eh / 2f
        val midX = ex + ew / 2f
        drawLine(Color(0xFFE67E22), Offset(midX, midY), Offset(ex - 16f, midY), 3f)
        // arrowhead
        drawLine(Color(0xFFE67E22), Offset(ex - 16f, midY), Offset(ex - 8f, midY - 5f), 2f)
        drawLine(Color(0xFFE67E22), Offset(ex - 16f, midY), Offset(ex - 8f, midY + 5f), 2f)
        drawText(tm, "X", Offset(ex - 28f, midY - 8f),
            style = TextStyle(fontSize = 9.sp, color = Color(0xFFE67E22)))

        // Y axis arrow pointing toward trailer left (top of canvas)
        drawLine(Color(0xFF1ABC9C), Offset(midX, midY), Offset(midX, ey - 16f), 3f)
        drawLine(Color(0xFF1ABC9C), Offset(midX, ey - 16f), Offset(midX - 5f, ey - 8f), 2f)
        drawLine(Color(0xFF1ABC9C), Offset(midX, ey - 16f), Offset(midX + 5f, ey - 8f), 2f)
        drawText(tm, "Y", Offset(midX + 6f, ey - 26f),
            style = TextStyle(fontSize = 9.sp, color = Color(0xFF1ABC9C)))

        // Edge labels
        drawText(tm, "FRONT / HITCH", Offset(left, top - 22f), style = labelStyle)
        drawText(tm, "REAR",          Offset(left + w - 32f, top + h + 6f), style = labelStyle)
        drawText(tm, "LEFT",          Offset(left + w / 2f - 14f, top - 22f), style = labelStyle)
        drawText(tm, "RIGHT",         Offset(left + w / 2f - 16f, top + h + 6f), style = labelStyle)

        // Mount note
        drawText(tm, "Mount ESP32 with X axis pointing toward hitch",
            Offset(left, top + h + 20f), style = smallStyle)
    }
}
