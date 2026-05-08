package com.levela.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.levela.app.R
import kotlin.math.abs

private const val EDGE_THRESHOLD = 0.3f
private const val IMAGE_ASPECT   = 614f / 1130f

// Trailer body position within the image (fractions of image size)
// Left/right inset ≈ 16%, top inset ≈ 19% (below hitch), bottom inset ≈ 8%
private const val INSET_H = 0.16f
private const val INSET_T = 0.19f
private const val INSET_B = 0.08f

@Composable
fun TrailerLevelView(rollDeg: Float, pitchDeg: Float, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {

        // Soft ambient shadow — radius extends well beyond canvas so the edge never shows
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r  = maxOf(size.width, size.height) * 1.2f
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.00f to Color(0x28000000),
                        0.40f to Color(0x18000000),
                        0.70f to Color(0x08000000),
                        1.00f to Color(0x00000000)
                    ),
                    center = Offset(cx, cy + size.height * 0.05f),
                    radius = r
                ),
                radius = r,
                center = Offset(cx, cy + size.height * 0.05f)
            )
        }

        // Inner box matches image aspect ratio — no letterboxing around indicators
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(IMAGE_ASPECT)
        ) {
            val w = maxWidth
            val h = maxHeight

            Image(
                painter = painterResource(R.drawable.trailer),
                contentDescription = "Trailer top view",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            BubbleLevel(
                rollDeg  = rollDeg,
                pitchDeg = pitchDeg,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize(0.65f)
                    .alpha(0.78f)
            )

            // Indicators pinned to the actual trailer body corners
            EdgeIndicator(
                liftDeg  = -(pitchDeg + rollDeg),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = w * INSET_H, top = h * INSET_T)
            )
            EdgeIndicator(
                liftDeg  = -pitchDeg + rollDeg,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = w * INSET_H, top = h * INSET_T)
            )
            EdgeIndicator(
                liftDeg  = pitchDeg - rollDeg,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = w * INSET_H, bottom = h * INSET_B)
            )
            EdgeIndicator(
                liftDeg  = pitchDeg + rollDeg,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = w * INSET_H, bottom = h * INSET_B)
            )
        }
    }
}

@Composable
private fun EdgeIndicator(liftDeg: Float, modifier: Modifier = Modifier) {
    val magnitude = abs(liftDeg)
    val (label, bgColor) = when {
        liftDeg >  EDGE_THRESHOLD -> "↑ LIFT"  to urgencyColor(magnitude)
        liftDeg < -EDGE_THRESHOLD -> "↓ LOWER" to urgencyColor(magnitude)
        else                      -> "✓"        to Color(0xFF27AE60)
    }

    Surface(
        color           = bgColor.copy(alpha = 0.90f),
        shape           = RoundedCornerShape(8.dp),
        shadowElevation = 4.dp,
        modifier        = modifier
    ) {
        Text(
            text       = label,
            color      = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize   = 12.sp,
            modifier   = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

private fun urgencyColor(deg: Float) = when {
    deg < 1f -> Color(0xFFF39C12)
    deg < 3f -> Color(0xFFE67E22)
    else     -> Color(0xFFE74C3C)
}
