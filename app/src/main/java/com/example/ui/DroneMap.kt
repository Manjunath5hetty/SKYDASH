package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AStarRoute
import com.example.data.Drone
import com.example.data.NoFlyZone
import com.example.data.Restaurant
import com.example.data.SimulationEngine
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DroneMap(
    drones: List<Drone>,
    restaurants: List<Restaurant> = emptyList(),
    noFlyZones: List<NoFlyZone> = emptyList(),
    activeOrder: com.example.data.Order? = null,
    route: AStarRoute? = null,
    userLat: Double = SimulationEngine.USER_LAT,
    userLng: Double = SimulationEngine.USER_LNG,
    heightDp: Int = 320,
    showAllDrones: Boolean = true,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    // Infinite transition for pulsing, propellers, and route courier dots
    val infiniteTransition = rememberInfiniteTransition(label = "mapAnimations")
    
    // Pulsing factor (0f to 1f) for rings
    val pulseProg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAnimation"
    )

    // Propeller rotation (0f to 360f)
    val propRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "propellerRotation"
    )

    // Courier dot progress along the route (0f to 1f)
    val courierProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "courierDotProgress"
    )

    // Procedural city blocks (fixed list to prevent re-creation flicker)
    val cityBlocks = remember {
        listOf(
            // xFraction, yFraction, widthFraction, heightFraction
            Block(0.1f, 0.12f, 0.15f, 0.15f),
            Block(0.35f, 0.08f, 0.18f, 0.20f),
            Block(0.65f, 0.15f, 0.22f, 0.12f),
            Block(0.12f, 0.40f, 0.14f, 0.25f),
            Block(0.40f, 0.45f, 0.25f, 0.15f),
            Block(0.75f, 0.40f, 0.18f, 0.30f),
            Block(0.15f, 0.75f, 0.20f, 0.15f),
            Block(0.42f, 0.72f, 0.15f, 0.20f),
            Block(0.68f, 0.78f, 0.12f, 0.14f),
            Block(0.05f, 0.58f, 0.08f, 0.12f),
            Block(0.55f, 0.32f, 0.12f, 0.10f),
            Block(0.85f, 0.05f, 0.10f, 0.18f)
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .background(Color(0xFF0A0F1A))
            .border(1.dp, Color(0xFF1E293B))
            .testTag("drone_map")
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        // Coordinate projection functions
        fun latToY(lat: Double): Float {
            val ratio = (SimulationEngine.MAX_LAT - lat) / (SimulationEngine.MAX_LAT - SimulationEngine.MIN_LAT)
            return (ratio * height).toFloat()
        }

        fun lngToX(lng: Double): Float {
            val ratio = (lng - SimulationEngine.MIN_LNG) / (SimulationEngine.MAX_LNG - SimulationEngine.MIN_LNG)
            return (ratio * width).toFloat()
        }

        // Draw basic canvas layers
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Layer 1: Background Grid (40px cyan lines at 6% opacity)
            val gridSizePx = 40f
            val gridColor = Color(0xFF00E5FF).copy(alpha = 0.06f)
            
            // Vertical lines
            var x = 0f
            while (x < size.width) {
                drawLine(gridColor, start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = 1f)
                x += gridSizePx
            }
            // Horizontal lines
            var y = 0f
            while (y < size.height) {
                drawLine(gridColor, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1f)
                y += gridSizePx
            }

            // Layer 2: Procedural City Blocks
            cityBlocks.forEach { block ->
                drawRect(
                    color = Color(0xFF1E293B).copy(alpha = 0.6f),
                    topLeft = Offset(block.xFrac * size.width, block.yFrac * size.height),
                    size = Size(block.wFrac * size.width, block.hFrac * size.height)
                )
            }

            // Layer 3: No-Fly Zones
            noFlyZones.forEach { nfz ->
                val centerX = lngToX(nfz.lng)
                val centerY = latToY(nfz.lat)
                
                // radius is in degrees; we project the longitude delta to pixels
                val radiusPx = (nfz.radius / (SimulationEngine.MAX_LNG - SimulationEngine.MIN_LNG)).toFloat() * size.width
                val zoneColor = if (nfz.type == "PROHIBITED") Color(0xFFFF3B30) else Color(0xFFFF9500)
                
                // Pulsing outer edge
                val outerRadius = radiusPx * (1f + pulseProg * 0.05f)
                val opacity = 0.03f + (1f - pulseProg) * 0.05f
                
                // Draw radial gradient simulation
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(zoneColor.copy(alpha = 0.15f), zoneColor.copy(alpha = 0.02f)),
                        center = Offset(centerX, centerY),
                        radius = radiusPx
                    ),
                    center = Offset(centerX, centerY),
                    radius = radiusPx
                )

                // Dashed boundary stroke
                drawCircle(
                    color = zoneColor.copy(alpha = 0.4f),
                    center = Offset(centerX, centerY),
                    radius = radiusPx,
                    style = Stroke(
                        width = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                )
            }

            // Layer 4: Route Path
            if (route != null && route.waypoints.isNotEmpty()) {
                val pathPoints = route.waypoints.map { Offset(lngToX(it.lng), latToY(it.lat)) }
                
                // 1. Neon glow layer (6px with 25% opacity)
                for (i in 0 until pathPoints.size - 1) {
                    drawLine(
                        color = Color(0xFF00E5FF).copy(alpha = 0.25f),
                        start = pathPoints[i],
                        end = pathPoints[i + 1],
                        strokeWidth = 6f
                    )
                }

                // 2. Base route layer (2px, dashed)
                for (i in 0 until pathPoints.size - 1) {
                    drawLine(
                        color = Color(0xFF00E5FF),
                        start = pathPoints[i],
                        end = pathPoints[i + 1],
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )
                }

                // 3. Animated courier dot traveling continuously
                if (route.waypoints.size > 1) {
                    val totalSegments = route.waypoints.size - 1
                    val scaledProg = courierProgress * totalSegments
                    val idx = scaledProg.toInt().coerceIn(0, totalSegments - 1)
                    val segProg = scaledProg - idx
                    
                    val p1 = pathPoints[idx]
                    val p2 = pathPoints[idx + 1]
                    
                    val dotX = p1.x + (p2.x - p1.x) * segProg
                    val dotY = p1.y + (p2.y - p1.y) * segProg
                    
                    // Glow background
                    drawCircle(
                        color = Color(0xFF00E5FF).copy(alpha = 0.4f),
                        center = Offset(dotX, dotY),
                        radius = 8f
                    )
                    // Core dot
                    drawCircle(
                        color = Color(0xFF00E5FF),
                        center = Offset(dotX, dotY),
                        radius = 4f
                    )
                }
            }
        }

        // Layer 5: Compass Overlay (Top-Right)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .size(36.dp)
                .background(Color(0xFF151D30).copy(alpha = 0.8f), CircleShape)
                .border(1.dp, Color(0xFF1E293B), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(16.dp)) {
                // North Arrow
                val arrowWidth = 5f
                val arrowHeight = 14f
                val centerX = size.width / 2f
                val centerY = size.height / 2f

                // Top (North) - Red
                val northPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(centerX, centerY - arrowHeight / 2)
                    lineTo(centerX - arrowWidth, centerY)
                    lineTo(centerX + arrowWidth, centerY)
                    close()
                }
                drawPath(northPath, Color(0xFFFF3B30))

                // Bottom (South) - White
                val southPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(centerX, centerY + arrowHeight / 2)
                    lineTo(centerX - arrowWidth, centerY)
                    lineTo(centerX + arrowWidth, centerY)
                    close()
                }
                drawPath(southPath, Color(0xFFE2E8F0))
            }
            Text(
                "N",
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.offset(y = (-11).dp)
            )
        }

        // Layer 6: Scale Bar Overlay (Bottom-Left)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .background(Color(0xFF151D30).copy(alpha = 0.7f), MaterialTheme.shapes.extraSmall)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(2.dp)
                    .background(Color(0xFF00E5FF))
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "500 m",
                color = Color(0xFF94A3B8),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Layer 7: User Location (Pulsing ring and dot with "YOU" label)
        val userX = lngToX(userLng)
        val userY = latToY(userLat)
        
        // Render user ring pulse
        val pulseRadiusPx = 18f * (1f + pulseProg * 0.5f)
        val pulseAlpha = 0.6f * (1f - pulseProg)

        Box(
            modifier = Modifier
                .offset(
                    x = with(density) { (userX).toDp() } - 24.dp,
                    y = with(density) { (userY).toDp() } - 24.dp
                )
                .size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            // Pulse circle
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color(0xFF007AFF).copy(alpha = pulseAlpha),
                    radius = pulseRadiusPx,
                    center = center
                )
                drawCircle(
                    color = Color(0xFF007AFF),
                    radius = 5f,
                    center = center
                )
            }
            // Label above user dot
            Box(
                modifier = Modifier
                    .offset(y = (-14).dp)
                    .background(Color(0xFF007AFF), MaterialTheme.shapes.extraSmall)
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    "YOU",
                    color = Color.White,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Layer 8: Restaurants
        restaurants.forEach { rest ->
            val rx = lngToX(rest.latLngLng())
            val ry = latToY(rest.latLngLat())

            Box(
                modifier = Modifier
                    .offset(
                        x = with(density) { (rx).toDp() } - 20.dp,
                        y = with(density) { (ry).toDp() } - 20.dp
                    )
                    .size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Small floating restaurant name
                    Text(
                        rest.name,
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .background(Color(0xFF0A0F1A).copy(alpha = 0.8f), MaterialTheme.shapes.extraSmall)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    // Icon circle
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color(0xFF151D30), CircleShape)
                            .border(1.dp, Color(0xFF10B981), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(rest.img, fontSize = 12.sp)
                    }
                }
            }
        }

        // Layer 9: Drones (Diamond shapes, status-coded, propellers)
        drones.forEach { drone ->
            // Filter drone representation
            val matchesActiveOrder = activeOrder != null && drone.id == activeOrder.droneId
            val shouldRender = showAllDrones || matchesActiveOrder

            if (shouldRender) {
                val dx = lngToX(drone.lng)
                val dy = latToY(drone.lat)

                // Color configuration:
                // IN_FLIGHT (active) -> Gold #FFDC00
                // IN_FLIGHT (other) -> Cyan #00E5FF
                // ASSIGNED -> Purple #AA88FF
                // CHARGING -> Orange #FF9500
                // IDLE -> Gray #AAAAAA
                val droneColor = when {
                    drone.status == "IN_FLIGHT" && matchesActiveOrder -> Color(0xFFFFDC00)
                    drone.status == "IN_FLIGHT" -> Color(0xFF00E5FF)
                    drone.status == "ASSIGNED" -> Color(0xFFAA88FF)
                    drone.status == "CHARGING" -> Color(0xFFFF9500)
                    else -> Color(0xFF94A3B8)
                }

                Box(
                    modifier = Modifier
                        .offset(
                            x = with(density) { (dx).toDp() } - 25.dp,
                            y = with(density) { (dy).toDp() } - 25.dp
                        )
                        .size(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Dynamic background glows for active in-flight order
                    if (drone.status == "IN_FLIGHT") {
                        val glowOpacity = if (matchesActiveOrder) 0.15f * (1f - pulseProg) else 0.08f * (1f - pulseProg)
                        val glowRadius = if (matchesActiveOrder) 18f * (1f + pulseProg * 0.4f) else 14f * (1f + pulseProg * 0.3f)
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = droneColor.copy(alpha = glowOpacity),
                                radius = glowRadius,
                                center = center
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Floating ID Tag
                        Text(
                            text = drone.id,
                            color = droneColor,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(Color(0xFF0A0F1A).copy(alpha = 0.9f), MaterialTheme.shapes.extraSmall)
                                .padding(horizontal = 3.dp, vertical = 1.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(1.dp))

                        // Diamond representation + Propellers
                        Box(
                            modifier = Modifier.size(18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Draw Diamond shape
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val size = size.width
                                val path = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(size / 2f, 2f) // top
                                    lineTo(size - 2f, size / 2f) // right
                                    lineTo(size / 2f, size - 2f) // bottom
                                    lineTo(2f, size / 2f) // left
                                    close()
                                }
                                drawPath(path = path, color = droneColor)
                                drawPath(path = path, color = Color.White.copy(alpha = 0.3f), style = Stroke(width = 1f))
                            }

                            // Propellers (only for IN_FLIGHT drones)
                            if (drone.status == "IN_FLIGHT") {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val size = size.width
                                    val radius = size / 2f
                                    
                                    // Let's draw 2 tiny spinning blades
                                    val angleRad = Math.toRadians(propRotation.toDouble())
                                    val bladeLength = 7f
                                    
                                    val endX1 = radius + bladeLength * cos(angleRad).toFloat()
                                    val endY1 = radius + bladeLength * sin(angleRad).toFloat()
                                    val endX2 = radius - bladeLength * cos(angleRad).toFloat()
                                    val endY2 = radius - bladeLength * sin(angleRad).toFloat()

                                    // Blade 1
                                    drawLine(
                                        color = Color.White.copy(alpha = 0.7f),
                                        start = Offset(radius, radius),
                                        end = Offset(endX1, endY1),
                                        strokeWidth = 1.5f
                                    )
                                    // Blade 2
                                    drawLine(
                                        color = Color.White.copy(alpha = 0.7f),
                                        start = Offset(radius, radius),
                                        end = Offset(endX2, endY2),
                                        strokeWidth = 1.5f
                                    )
                                }
                            }
                            
                            // Small drone icon or battery indicator inside
                            Text(
                                "⚡",
                                fontSize = 8.sp,
                                color = if (drone.battery < 30) Color(0xFFFF3B30) else Color.White,
                                modifier = Modifier.offset(y = (-1).dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Extension to avoid lat/lng duplicate parameter names matching system schema
private fun Restaurant.latLngLat() = this.lat
private fun Restaurant.latLngLng() = this.lng

private data class Block(
    val xFrac: Float,
    val yFrac: Float,
    val wFrac: Float,
    val hFrac: Float
)
