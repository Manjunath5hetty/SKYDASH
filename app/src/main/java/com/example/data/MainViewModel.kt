package com.example.data

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel : ViewModel() {

    // Central state machine
    private val _screen = mutableStateOf("LOGIN")
    val screen: State<String> = _screen

    private val _userRole = mutableStateOf("CUSTOMER") // "CUSTOMER" or "ADMIN"
    val userRole: State<String> = _userRole

    private val _selectedRestaurant = mutableStateOf<Restaurant?>(null)
    val selectedRestaurant: State<Restaurant?> = _selectedRestaurant

    // Cart items state
    private val _cart = mutableStateListOf<CartItem>()
    val cart: List<CartItem> = _cart

    // List of active and past orders
    private val _orders = mutableStateListOf<Order>()
    val orders: List<Order> = _orders

    // Drones state
    private val _drones = mutableStateListOf<Drone>().apply {
        addAll(SimulationEngine.getInitialDrones())
    }
    val drones: List<Drone> = _drones

    // Active tracked order
    private val _activeOrder = mutableStateOf<Order?>(null)
    val activeOrder: State<Order?> = _activeOrder

    // Active tab in Admin Dashboard
    private val _adminTab = mutableStateOf("OVERVIEW")
    val adminTab: State<String> = _adminTab

    // Chronological logs
    private val _telemetryLogs = mutableStateListOf<TelemetryLog>().apply {
        add(TelemetryLog(getCurrentTimeWithSeconds(), "SYSTEM", "Drone flight control system online.", null, null))
        add(TelemetryLog(getCurrentTimeWithSeconds(), "DR-002", "En-route to secondary airspace waypoint.", 12.9620, 77.5880))
        add(TelemetryLog(getCurrentTimeWithSeconds(), "DR-003", "Docked at station. Initiating power cycle.", 12.9780, 77.6050))
    }
    val telemetryLogs: List<TelemetryLog> = _telemetryLogs

    // Floating notification / toast state
    private val _notification = mutableStateOf<Pair<String, String>?>(null) // Pair(Message, Type "INFO" / "SUCCESS" / "WARNING")
    val notification: State<Pair<String, String>?> = _notification

    // Progress of active in-flight drone (0.0 to 1.0)
    private val _activeFlightProgress = mutableStateOf(0f)
    val activeFlightProgress: State<Float> = _activeFlightProgress

    // Simulation loop job references
    private var telemetryJob: Job? = null
    private var orderAdvancementJob: Job? = null

    init {
        startTelemetryLoop()
    }

    fun navigateTo(destination: String) {
        _screen.value = destination
    }

    fun login(role: String) {
        _userRole.value = role
        if (role == "CUSTOMER") {
            _screen.value = "CUSTOMER_HOME"
        } else {
            _screen.value = "ADMIN"
        }
        showToast("Welcome to SkyDash! Logged in as $role", "SUCCESS")
    }

    fun logout() {
        _screen.value = "LOGIN"
        _cart.clear()
        _activeOrder.value = null
        _activeFlightProgress.value = 0f
        orderAdvancementJob?.cancel()
        showToast("Logged out successfully.", "INFO")
    }

    fun selectRestaurant(restaurant: Restaurant) {
        _selectedRestaurant.value = restaurant
        _screen.value = "MENU"
    }

    // Cart Management
    fun addToCart(menuItem: MenuItem) {
        val existingIndex = _cart.indexOfFirst { it.menuItem.id == menuItem.id }
        if (existingIndex >= 0) {
            val item = _cart[existingIndex]
            _cart[existingIndex] = item.copy(qty = item.qty + 1)
        } else {
            _cart.add(CartItem(menuItem, 1))
        }
    }

    fun removeFromCart(menuItem: MenuItem) {
        val existingIndex = _cart.indexOfFirst { it.menuItem.id == menuItem.id }
        if (existingIndex >= 0) {
            val item = _cart[existingIndex]
            if (item.qty > 1) {
                _cart[existingIndex] = item.copy(qty = item.qty - 1)
            } else {
                _cart.removeAt(existingIndex)
            }
        }
    }

    fun clearCart() {
        _cart.clear()
    }

    // Toast/Notification Helper
    fun showToast(message: String, type: String = "INFO") {
        _notification.value = Pair(message, type)
        viewModelScope.launch {
            delay(3500)
            if (_notification.value?.first == message) {
                _notification.value = null
            }
        }
    }

    // Search and filter restaurants
    fun getFilteredRestaurants(searchQuery: String): List<Restaurant> {
        val all = SimulationEngine.getInitialRestaurants()
        if (searchQuery.isBlank()) return all
        return all.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.cuisine.contains(searchQuery, ignoreCase = true)
        }
    }

    // Drone commands (Admin overrides)
    fun issueReturnCommand(droneId: String) {
        val index = _drones.indexOfFirst { it.id == droneId }
        if (index >= 0) {
            val d = _drones[index]
            _drones[index] = d.copy(status = "IDLE", speed = 0, altitude = 0)
            addLog(droneId, "Command Issued: RETURN TO BASE. Status set to IDLE.", d.lat, d.lng)
            showToast("Command sent: $droneId returning to base.", "INFO")
        }
    }

    fun issueChargeCommand(droneId: String) {
        val index = _drones.indexOfFirst { it.id == droneId }
        if (index >= 0) {
            val d = _drones[index]
            _drones[index] = d.copy(status = "CHARGING", speed = 0, altitude = 0)
            addLog(droneId, "Command Issued: INITIALIZE CHARGE. Docked at power station.", d.lat, d.lng)
            showToast("Command sent: $droneId docked for charging.", "INFO")
        }
    }

    // Order Placement & Lifecycle
    fun getBestAvailableDrone(): Drone? {
        return _drones
            .filter { it.status == "IDLE" && it.battery > SimulationEngine.DRONE_MIN_BATT }
            .maxByOrNull { it.battery }
    }

    fun placeOrder() {
        val restaurant = _selectedRestaurant.value ?: return
        if (_cart.isEmpty()) return

        val assignedDrone = getBestAvailableDrone()
        val droneId = assignedDrone?.id ?: "PENDING"
        
        // Generate route
        val route = SimulationEngine.generateAStarRoute(
            restaurant.lat, restaurant.lng,
            SimulationEngine.USER_LAT, SimulationEngine.USER_LNG
        )

        val subtotal = _cart.sumOf { it.menuItem.price * it.qty }
        val total = subtotal + SimulationEngine.DELIVERY_FEE

        val orderId = "ORD-${8822 + _orders.size}"
        val order = Order(
            id = orderId,
            restaurantId = restaurant.id,
            restaurantName = restaurant.name,
            items = _cart.toList(),
            total = total,
            status = "CREATED",
            droneId = droneId,
            createdAt = getCurrentTime(),
            eta = getEstimatedTime(route.eta),
            route = route
        )

        // Add to orders
        _orders.add(order)
        _activeOrder.value = order
        _cart.clear()

        // Update assigned drone state if available
        if (assignedDrone != null) {
            updateDroneStatus(assignedDrone.id, "ASSIGNED", 0.8) // payload load set to 0.8kg avg
            addLog(assignedDrone.id, "Assigned to Order $orderId. Payload secured.", assignedDrone.lat, assignedDrone.lng)
        }

        addLog("SYSTEM", "New order created: $orderId at ${restaurant.name} for ₹$total.", null, null)

        // Navigate to tracking
        _screen.value = "TRACKING"

        // Start order advancement simulation
        startOrderAdvancement(orderId)
    }

    private fun updateDroneStatus(droneId: String, status: String, load: Double? = null) {
        val index = _drones.indexOfFirst { it.id == droneId }
        if (index >= 0) {
            val d = _drones[index]
            _drones[index] = d.copy(
                status = status,
                load = load ?: d.load,
                speed = if (status == "IN_FLIGHT") 48 else 0,
                altitude = if (status == "IN_FLIGHT") 100 else 0
            )
        }
    }

    private fun startOrderAdvancement(orderId: String) {
        orderAdvancementJob?.cancel()
        _activeFlightProgress.value = 0f
        
        orderAdvancementJob = viewModelScope.launch {
            // Stage 1: CREATED -> CONFIRMED (3s)
            delay(3000)
            updateActiveOrderStatus(orderId, "CONFIRMED")
            showToast("Order Confirmed by Restaurant!", "INFO")

            // Stage 2: CONFIRMED -> PREPARING (4s)
            delay(4000)
            updateActiveOrderStatus(orderId, "PREPARING")

            // Stage 3: PREPARING -> READY_FOR_DISPATCH (5s)
            delay(5000)
            updateActiveOrderStatus(orderId, "READY_FOR_DISPATCH")
            showToast("Your meal is ready and loading onto drone!", "INFO")

            // Stage 4: READY_FOR_DISPATCH -> IN_FLIGHT (6s)
            delay(6000)
            val currentOrder = _activeOrder.value
            if (currentOrder != null && currentOrder.id == orderId) {
                updateActiveOrderStatus(orderId, "IN_FLIGHT")
                if (currentOrder.droneId != "PENDING") {
                    updateDroneStatus(currentOrder.droneId, "IN_FLIGHT")
                    addLog(currentOrder.droneId, "Takeoff success. Initiating A* flight path navigation.", null, null)
                }
                showToast("🚀 Drone Dispatched! Altitude 100m. ETA: ${currentOrder.route?.eta?.toInt() ?: 4} min", "SUCCESS")
            }

            // Stage 5: IN_FLIGHT -> DELIVERED (Takes 8 seconds of simulated travel)
            val flightTicks = 8
            for (i in 1..flightTicks) {
                delay(1000)
                _activeFlightProgress.value = i.toFloat() / flightTicks.toFloat()
            }

            // Transition to DELIVERED
            val finalOrder = _activeOrder.value
            if (finalOrder != null && finalOrder.id == orderId) {
                updateActiveOrderStatus(orderId, "DELIVERED")
                if (finalOrder.droneId != "PENDING") {
                    val index = _drones.indexOfFirst { it.id == finalOrder.droneId }
                    if (index >= 0) {
                        val d = _drones[index]
                        _drones[index] = d.copy(
                            status = "IDLE",
                            lat = SimulationEngine.USER_LAT,
                            lng = SimulationEngine.USER_LNG,
                            speed = 0,
                            altitude = 0,
                            load = 0.0,
                            totalDeliveries = d.totalDeliveries + 1
                        )
                        addLog(finalOrder.droneId, "Package successfully dropped at delivery point. Status set to IDLE.", SimulationEngine.USER_LAT, SimulationEngine.USER_LNG)
                    }
                }
                addLog("SYSTEM", "Order $orderId delivered successfully.", null, null)
                showToast("🎉 Order Delivered! Enjoy your meal!", "SUCCESS")
            }
        }
    }

    private fun updateActiveOrderStatus(orderId: String, status: String) {
        // Update in active state
        val current = _activeOrder.value
        if (current != null && current.id == orderId) {
            val updated = current.copy(status = status)
            _activeOrder.value = updated
            
            // Update in full order list
            val listIndex = _orders.indexOfFirst { it.id == orderId }
            if (listIndex >= 0) {
                _orders[listIndex] = updated
            }
        }
    }

    // Real-Time Telemetry Simulation (2-second ticks)
    private fun startTelemetryLoop() {
        telemetryJob?.cancel()
        telemetryJob = viewModelScope.launch {
            while (true) {
                delay(2000)
                
                // Update each drone
                for (i in _drones.indices) {
                    val drone = _drones[i]
                    when (drone.status) {
                        "IN_FLIGHT" -> {
                            // Check if this drone belongs to the active tracked order
                            val isActiveOrderDrone = _activeOrder.value?.droneId == drone.id && _activeOrder.value?.status == "IN_FLIGHT"
                            
                            if (isActiveOrderDrone) {
                                val order = _activeOrder.value
                                val route = order?.route
                                if (route != null && route.waypoints.isNotEmpty()) {
                                    val progress = _activeFlightProgress.value
                                    val wps = route.waypoints
                                    
                                    val totalSegments = wps.size - 1
                                    val scaledProgress = progress * totalSegments
                                    val segmentIndex = scaledProgress.toInt().coerceIn(0, totalSegments - 1)
                                    val segmentProgress = scaledProgress - segmentIndex
                                    
                                    val p1 = wps[segmentIndex]
                                    val p2 = wps[segmentIndex + 1]
                                    
                                    val currentLat = p1.lat + (p2.lat - p1.lat) * segmentProgress
                                    val currentLng = p1.lng + (p2.lng - p1.lng) * segmentProgress
                                    val currentAlt = (p1.altitude + (p2.altitude - p1.altitude) * segmentProgress).toInt()

                                    val speed = (45..55).random()
                                    val newBattery = (drone.battery - 0.15).coerceAtLeast(0.0) // slightly more visible drain for active order

                                    _drones[i] = drone.copy(
                                        lat = currentLat,
                                        lng = currentLng,
                                        altitude = currentAlt,
                                        speed = speed,
                                        battery = newBattery
                                    )
                                    
                                    if (Math.random() < 0.25) {
                                        addLog(drone.id, "Navigating waypoint segment ${segmentIndex + 1}. Wind correction active.", currentLat, currentLng)
                                    }
                                }
                            } else {
                                // Other in flight drones drift randomly
                                val driftLat = (Math.random() - 0.5) * 0.0008
                                val driftLng = (Math.random() - 0.5) * 0.0008
                                val currentLat = drone.lat + driftLat
                                val currentLng = drone.lng + driftLng
                                val speed = (45..55).random()
                                val newBattery = (drone.battery - 0.05).coerceAtLeast(0.0)

                                _drones[i] = drone.copy(
                                    lat = currentLat,
                                    lng = currentLng,
                                    speed = speed,
                                    battery = newBattery
                                )
                                
                                if (drone.battery < 20 && drone.battery > 0) {
                                    showToast("⚠️ Warning: ${drone.id} battery is critical!", "WARNING")
                                    addLog(drone.id, "Telemetry Warning: Battery level critical (${drone.battery.toInt()}%). Returning to charger recommended.", currentLat, currentLng)
                                }
                            }
                        }
                        "CHARGING" -> {
                            // battery increases by 1.5% per tick for visible feedback
                            val newBattery = (drone.battery + 1.5).coerceAtMost(100.0)
                            
                            _drones[i] = drone.copy(
                                battery = newBattery
                            )
                            
                            if (newBattery >= 100.0) {
                                _drones[i] = _drones[i].copy(status = "IDLE")
                                addLog(drone.id, "Battery charge cycle completed (100%). System ready.", drone.lat, drone.lng)
                                showToast("${drone.id} charge complete! Ready for dispatch.", "INFO")
                            }
                        }
                    }
                }
            }
        }
    }

    fun setAdminTab(tab: String) {
        _adminTab.value = tab
    }

    // Log tracking helpers
    private fun addLog(droneId: String, description: String, lat: Double?, lng: Double?) {
        val log = TelemetryLog(
            timestamp = getCurrentTimeWithSeconds(),
            droneId = droneId,
            description = description,
            lat = lat,
            lng = lng
        )
        _telemetryLogs.add(0, log) // prepend for latest-first list
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    private fun getCurrentTimeWithSeconds(): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    }

    private fun getEstimatedTime(minutesToAdd: Double): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.MINUTE, minutesToAdd.toInt())
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
    }

    override fun onCleared() {
        super.onCleared()
        telemetryJob?.cancel()
        orderAdvancementJob?.cancel()
    }
}
