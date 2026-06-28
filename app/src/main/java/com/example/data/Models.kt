package com.example.data

data class Restaurant(
    val id: Int,
    val name: String,
    val cuisine: String,
    val lat: Double,
    val lng: Double,
    val rating: Double,
    val deliveryTime: String,
    val img: String
)

data class MenuItem(
    val id: Int,
    val name: String,
    val price: Int,
    val cal: Int,
    val img: String
)

data class Drone(
    val id: String,
    val name: String,
    val status: String, // "IDLE", "ASSIGNED", "IN_FLIGHT", "CHARGING"
    val battery: Double, // 0 to 100
    val lat: Double,
    val lng: Double,
    val speed: Int,
    val altitude: Int,
    val load: Double,
    val totalDeliveries: Int
)

data class NoFlyZone(
    val id: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val radius: Double, // in degrees, e.g., 0.003
    val type: String // "RESTRICTED" or "PROHIBITED"
)

data class CartItem(
    val menuItem: MenuItem,
    val qty: Int
)

data class RouteWaypoint(
    val lat: Double,
    val lng: Double,
    val altitude: Int
)

data class AStarRoute(
    val waypoints: List<RouteWaypoint>,
    val distance: Double, // km
    val eta: Double, // minutes
    val score: Double // 0.92 to 0.99
)

data class Order(
    val id: String,
    val restaurantId: Int,
    val restaurantName: String,
    val items: List<CartItem>,
    val total: Int,
    val status: String, // "CREATED", "CONFIRMED", "PREPARING", "READY_FOR_DISPATCH", "IN_FLIGHT", "DELIVERED"
    val droneId: String, // "DR-001" to "DR-005" or "PENDING"
    val createdAt: String,
    val eta: String,
    val route: AStarRoute?
)

data class TelemetryLog(
    val timestamp: String,
    val droneId: String,
    val description: String,
    val lat: Double?,
    val lng: Double?
)
