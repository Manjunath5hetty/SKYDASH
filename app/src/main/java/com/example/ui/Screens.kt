package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --------------------------------------------------------------------
// 1. LOGIN SCREEN
// --------------------------------------------------------------------
@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var activeTab by remember { mutableStateOf("CUSTOMER") } // "CUSTOMER" or "ADMIN"
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Sync state placeholders
    LaunchedEffect(activeTab) {
        email = if (activeTab == "CUSTOMER") "customer@skydash.com" else "admin@skydash.com"
        password = "••••••••"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1A))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .widthIn(max = 450.dp)
                .fillMaxWidth()
        ) {
            // Logo Icon & Header
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFF151D30), CircleShape)
                    .border(2.dp, Color(0xFF00E5FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("✦", color = Color(0xFF00E5FF), fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "SKYDASH",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                "Autonomous Drone Delivery System",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Role Tab selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color(0xFF151D30), RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (activeTab == "CUSTOMER") Color(0xFF00E5FF) else Color.Transparent)
                        .clickable { activeTab = "CUSTOMER" }
                        .testTag("customer_tab_login"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "CUSTOMER",
                        color = if (activeTab == "CUSTOMER") Color(0xFF0A0F1A) else Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (activeTab == "ADMIN") Color(0xFFAA88FF) else Color.Transparent)
                        .clickable { activeTab = "ADMIN" }
                        .testTag("admin_tab_login"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "ADMIN PORTAL",
                        color = if (activeTab == "ADMIN") Color(0xFF0A0F1A) else Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Input Fields Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151D30)),
                border = BoxBorder(Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Sign In Details",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF0A0F1A),
                            unfocusedContainerColor = Color(0xFF0A0F1A),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("login_email")
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Access Key / Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF0A0F1A),
                            unfocusedContainerColor = Color(0xFF0A0F1A),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("login_password")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button with 1.2s authenticating spinner animation
            Button(
                onClick = {
                    if (!isLoading) {
                        isLoading = true
                        coroutineScope.launch {
                            delay(1200) // 1.2 second loading animation simulation
                            isLoading = false
                            viewModel.login(activeTab)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == "CUSTOMER") Color(0xFF00E5FF) else Color(0xFFAA88FF),
                    contentColor = Color(0xFF0A0F1A)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("login_button"),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color(0xFF0A0F1A),
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        "ENTER CONTROL SYSTEM",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Stats Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatPill("12,000+", "Active Drones")
                StatPill("98.7%", "Success Rate")
                StatPill("4.2 Min", "Avg Delivery")
            }
        }
    }
}

@Composable
private fun StatPill(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color(0xFF00E5FF), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color(0xFF94A3B8), fontSize = 10.sp)
    }
}


// --------------------------------------------------------------------
// 2. CUSTOMER HOME SCREEN
// --------------------------------------------------------------------
@Composable
fun CustomerHomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isMapExpanded by remember { mutableStateOf(false) }

    val filteredRestaurants = viewModel.getFilteredRestaurants(searchQuery)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1A))
            .padding(horizontal = 16.dp)
    ) {
        // Space for system status/padding
        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Header: personalized greeting, weather pill, logout
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Welcome Back, Captain",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF10B981), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Airspace: SAFE (28°C)",
                            color = Color(0xFF10B981),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Logout
                IconButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier
                        .background(Color(0xFF151D30), CircleShape)
                        .border(1.dp, Color(0xFF1E293B), CircleShape)
                        .testTag("logout_button")
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Active Order Banner: Pulsing cyan dot + order details
        val activeOrder = viewModel.activeOrder.value
        if (activeOrder != null && activeOrder.status != "DELIVERED") {
            item {
                Card(
                    onClick = { viewModel.navigateTo("TRACKING") },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .border(1.dp, Color(0xFF4F46E5), RoundedCornerShape(12.dp))
                        .testTag("active_order_banner"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PulsingDot(color = Color(0xFF00E5FF))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Active Shipment: ${activeOrder.id}",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Status: ${activeOrder.status.replace("_", " ")}",
                                    color = Color(0xFF00E5FF),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Text(
                            "TRACK LIVE ➔",
                            color = Color(0xFF00E5FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Area map expandable card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151D30)),
                border = BoxBorder(Color(0xFF1E293B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Airspace", tint = Color(0xFF00E5FF))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Tactical Drone Airspace Map",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = { isMapExpanded = !isMapExpanded },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E293B),
                                contentColor = Color(0xFF00E5FF)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp).testTag("toggle_map_button")
                        ) {
                            Text(
                                if (isMapExpanded) "HIDE MAP" else "SHOW MAP",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = isMapExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        DroneMap(
                            drones = viewModel.drones,
                            restaurants = SimulationEngine.getInitialRestaurants(),
                            noFlyZones = SimulationEngine.getNoFlyZones(),
                            activeOrder = activeOrder,
                            heightDp = 240,
                            showAllDrones = true
                        )
                    }
                }
            }
        }

        // Search Bar (Filters restaurant grid)
        item {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by cuisine or restaurant...", color = Color(0xFF64748B)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF00E5FF)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF151D30),
                    unfocusedContainerColor = Color(0xFF151D30),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color(0xFF00E5FF)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .testTag("restaurant_search_bar")
            )
        }

        // Restaurant grid heading
        item {
            Text(
                "Drone Dispatch Hubs",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }

        // Restaurant cards - responsive grid simulation (using FlowRow style or structured chunks)
        val chunks = filteredRestaurants.chunked(2)
        if (filteredRestaurants.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, contentDescription = "No Hubs", tint = Color(0xFF475569), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No active launchpads match your search.", color = Color(0xFF64748B), fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(chunks) { rowItems ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    rowItems.forEach { restaurant ->
                        Box(modifier = Modifier.weight(1f).padding(4.dp)) {
                            RestaurantCard(
                                restaurant = restaurant,
                                onClick = { viewModel.selectRestaurant(restaurant) }
                            )
                        }
                    }
                    if (rowItems.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Past orders section
        val completedOrders = viewModel.orders.filter { it.status == "DELIVERED" }
        if (completedOrders.isNotEmpty()) {
            item {
                Text(
                    "Flight Delivery Records",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            items(completedOrders) { ord ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF151D30)),
                    border = BoxBorder(Color(0xFF1E293B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Launchpad: ${ord.restaurantName}",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Total: ₹${ord.total} • Completed at ${ord.createdAt}",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF134E5E), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "DELIVERED",
                                color = Color(0xFF00E5FF),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151D30)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
            .testTag("restaurant_card_${restaurant.id}"),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji Icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF0A0F1A), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(restaurant.img, fontSize = 18.sp)
                }
                
                // Rating Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFD700), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        restaurant.rating.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                restaurant.name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                restaurant.cuisine,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Divider(color = Color(0xFF1E293B), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "⏱ ${restaurant.deliveryTime}m",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .background(Color(0xFF0284C7).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFF0284C7), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        "DRONE",
                        color = Color(0xFF38BDF8),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun PulsingDot(color: Color, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .size(10.dp)
            .background(color.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size((8f * scale).dp)
                .background(color, CircleShape)
        )
    }
}


// --------------------------------------------------------------------
// 3. MENU SCREEN
// --------------------------------------------------------------------
@Composable
fun MenuScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val restaurant = viewModel.selectedRestaurant.value ?: return
    val menuItems = SimulationEngine.getMenuItems(restaurant.id)
    val cart = viewModel.cart

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1A))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with back button & details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo("CUSTOMER_HOME") },
                    modifier = Modifier
                        .background(Color(0xFF151D30), CircleShape)
                        .testTag("menu_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        restaurant.name,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${restaurant.cuisine} • Delivery Fee: ₹49",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            }

            // Scrollable Menu List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                items(menuItems) { item ->
                    val cartItem = cart.find { it.menuItem.id == item.id }
                    val qty = cartItem?.qty ?: 0

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF151D30)),
                        border = BoxBorder(Color(0xFF1E293B)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("menu_item_${item.id}"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Food Emoji circle
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Color(0xFF0A0F1A), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(item.img, fontSize = 22.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        item.name,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "🔥 ${item.cal} kcal",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        "₹${item.price}",
                                        color = Color(0xFF00E5FF),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }

                            // Add/Remove Quantities
                            if (qty > 0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(Color(0xFF0A0F1A), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.removeFromCart(item) },
                                        modifier = Modifier.size(32.dp).testTag("decrease_qty_${item.id}")
                                    ) {
                                        Text("-", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        qty.toString(),
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                    IconButton(
                                        onClick = { viewModel.addToCart(item) },
                                        modifier = Modifier.size(32.dp).testTag("increase_qty_${item.id}")
                                    ) {
                                        Text("+", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.addToCart(item) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1E293B),
                                        contentColor = Color(0xFF00E5FF)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(36.dp).testTag("add_item_${item.id}")
                                ) {
                                    Text("ADD +", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }
                
                // Extra breathing room at bottom for sticky bar
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        // Sticky cart bar
        if (cart.isNotEmpty()) {
            val totalCount = cart.sumOf { it.qty }
            val totalCost = cart.sumOf { it.menuItem.price * it.qty }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xFF0A0F1A).copy(alpha = 0.95f))
                        )
                    )
                    .padding(16.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF00E5FF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable { viewModel.navigateTo("CHECKOUT") }
                        .testTag("sticky_cart_bar"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF0A0F1A).copy(alpha = 0.2f), CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "$totalCount items",
                                    color = Color(0xFF0A0F1A),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "₹$totalCost",
                                color = Color(0xFF0A0F1A),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "NEXT: CHECKOUT",
                                color = Color(0xFF0A0F1A),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = Color(0xFF0A0F1A))
                        }
                    }
                }
            }
        }
    }
}


// --------------------------------------------------------------------
// 4. CHECKOUT SCREEN
// --------------------------------------------------------------------
@Composable
fun CheckoutScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val cart = viewModel.cart
    val restaurant = viewModel.selectedRestaurant.value ?: return

    val subtotal = cart.sumOf { it.menuItem.price * it.qty }
    val total = subtotal + SimulationEngine.DELIVERY_FEE

    // Matches best available drone
    val matchedDrone = viewModel.getBestAvailableDrone()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1A))
            .padding(horizontal = 16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo("MENU") },
                    modifier = Modifier
                        .background(Color(0xFF151D30), CircleShape)
                        .testTag("checkout_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Shipment Blueprint",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Panel 1: Order Summary
        item {
            Text(
                "Cargo Manifest",
                color = Color(0xFF64748B),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151D30)),
                border = BoxBorder(Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    cart.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${item.qty}x ${item.menuItem.name}",
                                color = Color.White,
                                fontSize = 13.sp
                            )
                            Text(
                                "₹${item.menuItem.price * item.qty}",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Divider(color = Color(0xFF1E293B), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        Text("₹$subtotal", color = Color.White, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Drone Logistics Fee", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        Text("₹${SimulationEngine.DELIVERY_FEE}", color = Color.White, fontSize = 13.sp)
                    }

                    Divider(color = Color(0xFF1E293B), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Grand Total", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("₹$total", color = Color(0xFF00E5FF), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Panel 2: Drone Assignment Panel
        item {
            Text(
                "Drone Assignment",
                color = Color(0xFF64748B),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151D30)),
                border = BoxBorder(Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (matchedDrone != null) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color(0xFF10B981), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    matchedDrone.id,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    " (${matchedDrone.name})",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 13.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .border(1.dp, Color(0xFF10B981), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "BEST FIT",
                                    color = Color(0xFF10B981),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress battery bar
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Battery Status", color = Color(0xFF94A3B8), fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Text("${matchedDrone.battery.toInt()}%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (matchedDrone.battery / 100f).toFloat() },
                            color = if (matchedDrone.battery > 50) Color(0xFF10B981) else Color(0xFFFF9500),
                            trackColor = Color(0xFF0A0F1A),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Load Class", color = Color(0xFF64748B), fontSize = 10.sp)
                                Text("Light (0.8 kg)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Total Deliveries", color = Color(0xFF64748B), fontSize = 10.sp)
                                Text("${matchedDrone.totalDeliveries} trips", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "None available", tint = Color(0xFFFF3B30), modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No Drones Available",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "All dispatch units are either depleted or active. The order will wait in PENDING cue.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Panel 3: Weather Check
        item {
            Text(
                "Telemetry Weather Validation",
                color = Color(0xFF64748B),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151D30)),
                border = BoxBorder(Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Temp: 28°C • Wind: 12 km/h", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Humidity: 65% • Density altitude nominal", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF134E5E), RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "STATUS: SAFE",
                            color = Color(0xFF00E5FF),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Action Button
        item {
            Button(
                onClick = { viewModel.placeOrder() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E5FF),
                    contentColor = Color(0xFF0A0F1A)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("place_order_button"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "LAUNCH AUTONOMOUS SHIPMENT",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}


// --------------------------------------------------------------------
// 5. ORDER TRACKING SCREEN
// --------------------------------------------------------------------
@Composable
fun OrderTrackingScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val order = viewModel.activeOrder.value ?: return
    val flightProgress = viewModel.activeFlightProgress.value

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1A))
    ) {
        // Sticky/Float top banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151D30)),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.navigateTo("CUSTOMER_HOME") },
                        modifier = Modifier.background(Color(0xFF0A0F1A), CircleShape).testTag("tracking_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Track Shipment ${order.id}",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Launchpad: ${order.restaurantName}",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                    if (order.status != "DELIVERED") {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("EST ARRIVAL", color = Color(0xFF64748B), fontSize = 9.sp)
                            Text(order.eta, color = Color(0xFFFFDC00), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Live map section
        item {
            Box(modifier = Modifier.fillMaxWidth()) {
                DroneMap(
                    drones = viewModel.drones,
                    restaurants = SimulationEngine.getInitialRestaurants(),
                    activeOrder = order,
                    route = order.route,
                    heightDp = 280,
                    showAllDrones = false // only show active drone
                )
            }
        }

        // Status Stepper Progress
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Flight Navigation Checkpoints",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Render 6 stages
                val stages = listOf(
                    "CREATED" to "Order broadcasted & assigned.",
                    "CONFIRMED" to "Kitchen cooking sequence initiated.",
                    "PREPARING" to "Assembling ingredients and packing payload.",
                    "READY_FOR_DISPATCH" to "Secured on landing bay. Diagnostics passed.",
                    "IN_FLIGHT" to "Takeoff success. Navigating A* route.",
                    "DELIVERED" to "Tether drop completed safely. Shipment finalized."
                )

                stages.forEachIndexed { index, (stageName, stageDesc) ->
                    val isCompleted = isStageCompleted(order.status, stageName)
                    val isActive = order.status == stageName

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        // Stepper Left Dot
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        color = when {
                                            isCompleted -> Color(0xFF10B981)
                                            isActive -> Color(0xFF00E5FF)
                                            else -> Color(0xFF1E293B)
                                        },
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCompleted) {
                                    Icon(Icons.Default.Check, contentDescription = "Done", tint = Color(0xFF0A0F1A), modifier = Modifier.size(14.dp))
                                } else if (isActive) {
                                    PulsingDot(color = Color.White, modifier = Modifier.size(10.dp))
                                } else {
                                    Text((index + 1).toString(), color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (index < stages.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(32.dp)
                                        .background(
                                            if (isCompleted) Color(0xFF10B981) else Color(0xFF1E293B)
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Stepper Text details
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stageName.replace("_", " "),
                                color = if (isActive) Color(0xFF00E5FF) else if (isCompleted) Color.White else Color(0xFF64748B),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                stageDesc,
                                color = if (isActive || isCompleted) Color(0xFF94A3B8) else Color(0xFF475569),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Telemetry Panel - shown ONLY during IN_FLIGHT
        if (order.status == "IN_FLIGHT") {
            item {
                val activeDrone = viewModel.drones.find { it.id == order.droneId }
                if (activeDrone != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF151D30)),
                        border = BoxBorder(Color(0xFF1E293B)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = "Log", tint = Color(0xFFFFDC00))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Live Telemetry HUD (${activeDrone.id})",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFFF9500).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "ALTITUDE FLIGHT",
                                        color = Color(0xFFFF9500),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                TelemetryMetricItem("Battery", "${activeDrone.battery.toInt()}%", Modifier.weight(1f))
                                TelemetryMetricItem("Air Speed", "${activeDrone.speed} km/h", Modifier.weight(1f))
                                TelemetryMetricItem("Altitude", "${activeDrone.altitude} m", Modifier.weight(1f))
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                TelemetryMetricItem("Est Distance", String.format(Locale.getDefault(), "%.2f km", order.route?.distance ?: 0.0), Modifier.weight(1f))
                                TelemetryMetricItem("A* Optimality", String.format(Locale.getDefault(), "%.3f", order.route?.score ?: 0.95), Modifier.weight(1f))
                                TelemetryMetricItem("Progress", "${(flightProgress * 100).toInt()}%", Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Delivery Celebration Banner upon DELIVERED status
        if (order.status == "DELIVERED") {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF134E5E)),
                    border = BoxBorder(Color(0xFF00E5FF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("delivery_success_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎉 COMPLETE SUCCESS", color = Color(0xFF00E5FF), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Autonomous drone landed safely at target coordinates. Order package dispatched to your doorstep.",
                            color = Color.White,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Total Amount Paid: ₹${order.total}",
                            color = Color(0xFFFFDC00),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.navigateTo("CUSTOMER_HOME") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00E5FF),
                                contentColor = Color(0xFF0A0F1A)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("dismiss_delivery_button")
                        ) {
                            Text("ACKNOWLEDGE & DISMISS", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(48.dp)) }
    }
}

private fun isStageCompleted(currentStatus: String, stageToCheck: String): Boolean {
    val order = listOf("CREATED", "CONFIRMED", "PREPARING", "READY_FOR_DISPATCH", "IN_FLIGHT", "DELIVERED")
    val currentIndex = order.indexOf(currentStatus)
    val checkIndex = order.indexOf(stageToCheck)
    return checkIndex < currentIndex && currentIndex != -1
}

@Composable
private fun TelemetryMetricItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, color = Color(0xFF64748B), fontSize = 10.sp)
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}


// --------------------------------------------------------------------
// 6. ADMIN DASHBOARD SCREEN
// --------------------------------------------------------------------
@Composable
fun AdminDashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val activeTab = viewModel.adminTab.value

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1A))
    ) {
        // Top Toolbar
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF151D30)),
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFFFF3B30), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "SkyDash Command Control",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                IconButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier.background(Color(0xFF0A0F1A), CircleShape)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                }
            }
        }

        // Horizontal Navigation Tabs Row (Responsive Slider)
        ScrollableTabRow(
            selectedTabIndex = getTabIndex(activeTab),
            containerColor = Color(0xFF151D30),
            contentColor = Color(0xFF00E5FF),
            edgePadding = 8.dp
        ) {
            val tabNames = listOf("OVERVIEW", "MAP", "DRONES", "ORDERS", "TELEMETRY")
            tabNames.forEach { tab ->
                Tab(
                    selected = activeTab == tab,
                    onClick = { viewModel.setAdminTab(tab) },
                    text = { Text(tab, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("admin_tab_$tab")
                )
            }
        }

        // Render Tab content dynamically
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (activeTab) {
                "OVERVIEW" -> AdminOverviewTab(viewModel)
                "MAP" -> AdminMapTab(viewModel)
                "DRONES" -> AdminDronesTab(viewModel)
                "ORDERS" -> AdminOrdersTab(viewModel)
                "TELEMETRY" -> AdminTelemetryTab(viewModel)
            }
        }
    }
}

private fun getTabIndex(tab: String): Int {
    return when (tab) {
        "OVERVIEW" -> 0
        "MAP" -> 1
        "DRONES" -> 2
        "ORDERS" -> 3
        "TELEMETRY" -> 4
        else -> 0
    }
}

// ADMIN SUBTAB: OVERVIEW
@Composable
private fun AdminOverviewTab(viewModel: MainViewModel) {
    val activeDrones = viewModel.drones.count { it.status == "IN_FLIGHT" }
    val liveOrders = viewModel.orders.count { it.status != "DELIVERED" }
    val charging = viewModel.drones.count { it.status == "CHARGING" }
    val deliveredToday = viewModel.orders.count { it.status == "DELIVERED" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // KPI grid row
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                    KpiCard("ACTIVE FLEET", activeDrones.toString(), Color(0xFF00E5FF))
                }
                Box(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                    KpiCard("LIVE SHIPMENTS", liveOrders.toString(), Color(0xFFFFDC00))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                    KpiCard("CHARGING CLIPS", charging.toString(), Color(0xFFFF9500))
                }
                Box(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                    KpiCard("DELIVERED TODAY", deliveredToday.toString(), Color(0xFF10B981))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Mini Airspace Map
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151D30)),
                border = BoxBorder(Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        "Airspace Scan (Compact View)",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                    DroneMap(
                        drones = viewModel.drones,
                        restaurants = SimulationEngine.getInitialRestaurants(),
                        heightDp = 180,
                        showAllDrones = true
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Fleet List table
        item {
            Text(
                "Drone Fleet Roster",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(viewModel.drones) { d ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151D30)),
                border = BoxBorder(Color(0xFF1E293B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    when (d.status) {
                                        "IN_FLIGHT" -> Color(0xFF00E5FF)
                                        "ASSIGNED" -> Color(0xFFAA88FF)
                                        "CHARGING" -> Color(0xFFFF9500)
                                        else -> Color(0xFF94A3B8)
                                    },
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(d.id, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(d.name, color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(d.status, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                        Text("🔋 ${d.battery.toInt()}%", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiCard(title: String, value: String, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151D30)),
        border = BoxBorder(Color(0xFF1E293B)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(title, color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
    }
}


// ADMIN SUBTAB: MAP
@Composable
private fun AdminMapTab(viewModel: MainViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        DroneMap(
            drones = viewModel.drones,
            restaurants = SimulationEngine.getInitialRestaurants(),
            noFlyZones = SimulationEngine.getNoFlyZones(),
            heightDp = 280,
            showAllDrones = true,
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            item {
                Text(
                    "No-Fly Zones (Airspace Boundaries)",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            items(SimulationEngine.getNoFlyZones()) { nfz ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF151D30)),
                    border = BoxBorder(Color(0xFF1E293B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(nfz.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Center: ${nfz.lat}, ${nfz.lng}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    if (nfz.type == "PROHIBITED") Color(0xFFFF3B30).copy(alpha = 0.15f) else Color(0xFFFF9500).copy(alpha = 0.15f),
                                    RoundedCornerShape(4.dp)
                                )
                                .border(
                                    1.dp,
                                    if (nfz.type == "PROHIBITED") Color(0xFFFF3B30) else Color(0xFFFF9500),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                nfz.type,
                                color = if (nfz.type == "PROHIBITED") Color(0xFFFF3B30) else Color(0xFFFF9500),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}


// ADMIN SUBTAB: DRONES (manual control override RETURN/CHARGE)
@Composable
private fun AdminDronesTab(viewModel: MainViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(viewModel.drones) { drone ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151D30)),
                border = BoxBorder(Color(0xFF1E293B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("admin_drone_card_${drone.id}"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(drone.id, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(drone.name, color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }

                        Box(
                            modifier = Modifier
                                .background(Color(0xFF1E293B), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                drone.status,
                                color = when (drone.status) {
                                    "IN_FLIGHT" -> Color(0xFF00E5FF)
                                    "ASSIGNED" -> Color(0xFFAA88FF)
                                    "CHARGING" -> Color(0xFFFF9500)
                                    else -> Color(0xFF94A3B8)
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Battery bar
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Battery Status", color = Color(0xFF94A3B8), fontSize = 11.sp, modifier = Modifier.weight(1f))
                        Text("${drone.battery.toInt()}%", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (drone.battery / 100f).toFloat() },
                        color = if (drone.battery > 50) Color(0xFF10B981) else if (drone.battery > 20) Color(0xFFFF9500) else Color(0xFFFF3B30),
                        trackColor = Color(0xFF0A0F1A),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Speed", color = Color(0xFF64748B), fontSize = 9.sp)
                            Text("${drone.speed} km/h", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Altitude", color = Color(0xFF64748B), fontSize = 9.sp)
                            Text("${drone.altitude} m", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Deliveries", color = Color(0xFF64748B), fontSize = 9.sp)
                            Text("${drone.totalDeliveries} trips", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Divider(color = Color(0xFF1E293B), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

                    // Command override buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { viewModel.issueReturnCommand(drone.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E293B),
                                contentColor = Color.White
                            ),
                            enabled = drone.status == "IN_FLIGHT",
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .padding(end = 4.dp)
                                .testTag("btn_return_${drone.id}")
                        ) {
                            Text("RETURN BASE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.issueChargeCommand(drone.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF9500).copy(alpha = 0.15f),
                                contentColor = Color(0xFFFF9500)
                            ),
                            enabled = drone.status == "IDLE",
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .padding(start = 4.dp)
                                .testTag("btn_charge_${drone.id}")
                        ) {
                            Text("FORCE CHARGE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}


// ADMIN SUBTAB: ORDERS
@Composable
private fun AdminOrdersTab(viewModel: MainViewModel) {
    if (viewModel.orders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Info, contentDescription = "No Orders", tint = Color(0xFF475569), modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("No active orders captured in the log stream yet.", color = Color(0xFF64748B), fontSize = 13.sp)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(viewModel.orders) { ord ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF151D30)),
                    border = BoxBorder(Color(0xFF1E293B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(ord.id, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Launch: ${ord.restaurantName}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }

                            Box(
                                modifier = Modifier
                                    .background(
                                        color = when (ord.status) {
                                            "DELIVERED" -> Color(0xFF10B981).copy(alpha = 0.15f)
                                            "IN_FLIGHT" -> Color(0xFFFFDC00).copy(alpha = 0.15f)
                                            else -> Color(0xFF00E5FF).copy(alpha = 0.15f)
                                        },
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        1.dp,
                                        color = when (ord.status) {
                                            "DELIVERED" -> Color(0xFF10B981)
                                            "IN_FLIGHT" -> Color(0xFFFFDC00)
                                            else -> Color(0xFF00E5FF)
                                        },
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    ord.status,
                                    color = when (ord.status) {
                                        "DELIVERED" -> Color(0xFF10B981)
                                        "IN_FLIGHT" -> Color(0xFFFFDC00)
                                        else -> Color(0xFF00E5FF)
                                    },
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Divider(color = Color(0xFF1E293B), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Assigned Unit", color = Color(0xFF64748B), fontSize = 9.sp)
                                Text(ord.droneId, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Cargo Value", color = Color(0xFF64748B), fontSize = 9.sp)
                                Text("₹${ord.total}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Dispatch Time", color = Color(0xFF64748B), fontSize = 9.sp)
                                Text(ord.createdAt, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}


// ADMIN SUBTAB: TELEMETRY (Chronological log feed with GPS link simulations)
@Composable
private fun AdminTelemetryTab(viewModel: MainViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(viewModel.telemetryLogs) { log ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151D30)),
                border = BoxBorder(Color(0xFF1E293B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "[${log.timestamp}]",
                                color = Color(0xFF64748B),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                log.droneId,
                                color = if (log.droneId == "SYSTEM") Color(0xFF00E5FF) else Color(0xFFAA88FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        if (log.lat != null && log.lng != null) {
                            Text(
                                "GPS: " + String.format(Locale.getDefault(), "%.4f", log.lat) + ", " + String.format(Locale.getDefault(), "%.4f", log.lng),
                                color = Color(0xFF10B981),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .background(Color(0xFF134E5E).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        log.description,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// Border Helper
@Composable
private fun BoxBorder(color: Color): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(1.dp, color)
}
