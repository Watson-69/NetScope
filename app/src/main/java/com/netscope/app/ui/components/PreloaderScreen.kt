package com.netscope.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netscope.app.ui.theme.CrimsonRed
import com.netscope.app.ui.theme.MilemarkInk
import com.netscope.app.ui.theme.MilemarkOrange
import com.netscope.app.ui.theme.MilemarkPaper
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * 3D Isometric Freight Container Preloader inspired by Milemark Logistics Template.
 * Features a real-time rotating 3D wireframe shipping container with corner locks,
 * dynamic velocity bars, and freight dispatch telemetry ticker.
 */
@Composable
fun PreloaderScreen(
    onPreloadComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var progress by remember { mutableFloatStateOf(0f) }
    var statusIndex by remember { mutableIntStateOf(0) }

    val statusMessages = listOf(
        "ESTABLISHING 3D FREIGHT TELEMETRY...",
        "SYNCHRONIZING INBOUND/OUTBOUND CONDUITS...",
        "RESEQUENCING EVENT TIMELINES...",
        "DISPATCH ENGINE SYNCHRONIZED"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "3DContainerTransition")
    
    // Continuous 3D rotation angle
    val rotationY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ContainerRotationY"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
        label = "ProgressAnimation"
    )

    LaunchedEffect(Unit) {
        progress = 0.28f
        statusIndex = 0
        delay(400)

        progress = 0.60f
        statusIndex = 1
        delay(450)

        progress = 0.88f
        statusIndex = 2
        delay(450)

        progress = 1.0f
        statusIndex = 3
        delay(350)

        onPreloadComplete()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090A08))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onPreloadComplete
            ),
        contentAlignment = Alignment.Center
    ) {
        // Subtle Logistics Coordinates Grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 45.dp.toPx()
            val gridColor = Color(0xFF161814)
            var x = 0f
            while (x < size.width) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), 1f)
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 1f)
                y += step
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(28.dp)
        ) {
            // Milemark Eyebrow Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MilemarkOrange)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MILEMARK 3D TELEMETRY",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = MilemarkOrange
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 3D Isometric Shipping Container Cube
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    val scale = size.width * 0.32f

                    // 8 Vertices of a 3D rectangular shipping container
                    // Width = 1.0, Height = 0.8, Depth = 1.6 (long freight container)
                    val vertices = listOf(
                        floatArrayOf(-0.9f, -0.65f, -1.2f),
                        floatArrayOf(0.9f, -0.65f, -1.2f),
                        floatArrayOf(0.9f, 0.65f, -1.2f),
                        floatArrayOf(-0.9f, 0.65f, -1.2f),
                        floatArrayOf(-0.9f, -0.65f, 1.2f),
                        floatArrayOf(0.9f, -0.65f, 1.2f),
                        floatArrayOf(0.9f, 0.65f, 1.2f),
                        floatArrayOf(-0.9f, 0.65f, 1.2f)
                    )

                    val rotRadY = Math.toRadians(rotationY.toDouble())
                    val pitchRadX = Math.toRadians(26.0) // Isometric top-down tilt

                    // Project 3D -> 2D
                    val projected = vertices.map { v ->
                        val x = v[0]
                        val y = v[1]
                        val z = v[2]

                        // Rotate Y
                        val cosY = cos(rotRadY).toFloat()
                        val sinY = sin(rotRadY).toFloat()
                        val x1 = x * cosY + z * sinY
                        val z1 = -x * sinY + z * cosY

                        // Rotate X (pitch)
                        val cosX = cos(pitchRadX).toFloat()
                        val sinX = sin(pitchRadX).toFloat()
                        val y2 = y * cosX - z1 * sinX
                        val z2 = y * sinX + z1 * cosX

                        val projX = centerX + x1 * scale
                        val projY = centerY + y2 * scale
                        Offset(projX, projY) to z2
                    }

                    // 12 Edges of shipping container
                    val edges = listOf(
                        0 to 1, 1 to 2, 2 to 3, 3 to 0, // Back face
                        4 to 5, 5 to 6, 6 to 7, 7 to 4, // Front face
                        0 to 4, 1 to 5, 2 to 6, 3 to 7  // Connecting longitudinal rails
                    )

                    // Draw Container Edges
                    edges.forEach { (startIdx, endIdx) ->
                        val p1 = projected[startIdx].first
                        val p2 = projected[endIdx].first
                        val avgZ = (projected[startIdx].second + projected[endIdx].second) / 2f
                        val edgeColor = if (avgZ > 0) MilemarkOrange else Color(0xFF383B33)
                        val strokeW = if (avgZ > 0) 2.2.dp.toPx() else 1.2.dp.toPx()

                        drawLine(
                            color = edgeColor,
                            start = p1,
                            end = p2,
                            strokeWidth = strokeW
                        )
                    }

                    // Draw Corner Casting Locks (cargo locks)
                    projected.forEach { (pt, z) ->
                        val lockColor = if (z > 0) MilemarkOrange else Color(0xFF7A7D73)
                        drawCircle(
                            color = lockColor,
                            radius = if (z > 0) 3.5.dp.toPx() else 2.dp.toPx(),
                            center = pt
                        )
                    }

                    // Center Coordinate Pulse
                    drawCircle(
                        color = CrimsonRed,
                        radius = 3.dp.toPx(),
                        center = Offset(centerX, centerY)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Milemark 3-Bar Brand Velocity Mark + Title
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 3 Skewed Bars
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(MilemarkOrange)
                    )
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(Color.White)
                    )
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(CrimsonRed)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "netscope",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.8).sp,
                    color = Color.White
                )
                Text(
                    text = ".",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = MilemarkOrange
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "PRECISION FREIGHT TELEMETRY",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.6.sp,
                color = Color(0xFFA1A39B)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Milemark Cargo Progress Rail
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(Color(0xFF242621))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(3.dp)
                        .background(MilemarkOrange)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Telemetry Status Ticker
            Text(
                text = statusMessages.getOrElse(statusIndex) { "DISPATCHING..." },
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFC7C9BF),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${(animatedProgress * 100).toInt()}% • LAT 37.77° N  LON 122.41° W",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MilemarkOrange
            )
        }

        // Bottom Dispatch Rail
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        ) {
            Text(
                text = "PRECISION. DELIVERED. • TAP TO SKIP",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF55574F),
                letterSpacing = 1.2.sp
            )
        }
    }
}
