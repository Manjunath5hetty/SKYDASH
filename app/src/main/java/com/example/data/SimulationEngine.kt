package com.example.data

import kotlin.math.*

object SimulationEngine {
    const val MIN_LAT = 12.958
    const val MAX_LAT = 12.982
    const val MIN_LNG = 77.583
    const val MAX_LNG = 77.612

    const val USER_LAT = 12.9655
    const val USER_LNG = 77.6100
    const val DELIVERY_FEE = 49
    const val DRONE_MIN_BATT = 30.0
    const val DRONE_SPEED_KMMIN = 0.8 // 48 km/h

    fun getInitialRestaurants(): List<Restaurant> = listOf(
        Restaurant(1, "Golden Dragon", "Chinese", 12.9715, 77.5855, 4.7, "15-20", "🥢"),
        Restaurant(2, "Pizza Hub", "Italian", 12.9605, 77.5920, 4.5, "20-25", "🍕"),
        Restaurant(3, "Burger Bistro", "Fast Food", 12.9750, 77.6010, 4.6, "12-18", "🍔"),
        Restaurant(4, "Curry House", "Indian", 12.9630, 77.5985, 4.8, "18-24", "🍛"),
        Restaurant(5, "Sweet Delights", "Dessert", 12.9680, 77.6070, 4.9, "10-15", "🍰")
    )

    fun getMenuItems(restaurantId: Int): List<MenuItem> = when (restaurantId) {
        1 -> listOf(
            MenuItem(101, "Spring Rolls", 150, 220, "🥖"),
            MenuItem(102, "Hakka Noodles", 240, 480, "🍜"),
            MenuItem(103, "Dim Sums", 190, 150, "🥟"),
            MenuItem(104, "Sweet & Sour Chicken", 290, 550, "🍗")
        )
        2 -> listOf(
            MenuItem(201, "Margherita Pizza", 320, 800, "🍕"),
            MenuItem(202, "Garlic Bread", 120, 350, "🥖"),
            MenuItem(203, "Pasta Arrabiata", 260, 450, "🍝"),
            MenuItem(204, "Tiramisu", 180, 380, "🍰")
        )
        3 -> listOf(
            MenuItem(301, "Classic Cheese Burger", 150, 520, "🍔"),
            MenuItem(302, "French Fries", 90, 310, "🍟"),
            MenuItem(303, "Onion Rings", 110, 240, "🍩"),
            MenuItem(304, "Chocolate Shake", 130, 420, "🥤")
        )
        4 -> listOf(
            MenuItem(401, "Butter Chicken with Naan", 350, 950, "🍛"),
            MenuItem(402, "Paneer Butter Masala", 290, 800, "🍛"),
            MenuItem(403, "Garlic Naan", 50, 200, "🫓"),
            MenuItem(404, "Biryani", 300, 750, "🍚")
        )
        5 -> listOf(
            MenuItem(501, "Chocolate Lava Cake", 150, 450, "🧁"),
            MenuItem(502, "Blueberry Cheesecake", 180, 400, "🍰"),
            MenuItem(503, "Strawberry Waffle", 160, 350, "🧇"),
            MenuItem(504, "Vanilla Ice Cream", 80, 180, "🍨")
        )
        else -> emptyList()
    }

    fun getInitialDrones(): List<Drone> = listOf(
        // ID, Name, Status, Battery, Lat, Lng, Speed, Altitude, Load, Deliveries
        Drone("DR-001", "Falcon-1", "IDLE", 94.0, 12.9700, 77.5950, 0, 0, 0.0, 42),
        Drone("DR-002", "Hawk-2", "IN_FLIGHT", 71.0, 12.9620, 77.5880, 48, 120, 1.2, 87),
        Drone("DR-003", "Eagle-3", "CHARGING", 23.0, 12.9780, 77.6050, 0, 0, 0.0, 61),
        Drone("DR-004", "Raven-4", "IDLE", 88.0, 12.9720, 77.5900, 0, 0, 0.0, 29),
        Drone("DR-005", "Storm-5", "ASSIGNED", 56.0, 12.9655, 77.6100, 0, 0, 0.8, 55)
    )

    fun getNoFlyZones(): List<NoFlyZone> = listOf(
        NoFlyZone("NFZ-1", "Air Force Base", 12.9680, 77.5930, 0.0035, "RESTRICTED"),
        NoFlyZone("NFZ-2", "Metro Construction Zone", 12.9610, 77.6030, 0.0025, "PROHIBITED")
    )

    // Haversine formula to compute distance in kilometers
    fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Radius of earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    // Generate A* Route from restaurant (start) to user (end) with 12 segments
    fun generateAStarRoute(startLat: Double, startLng: Double, endLat: Double, endLng: Double): AStarRoute {
        val steps = 12
        val waypoints = mutableListOf<RouteWaypoint>()
        
        // Origin
        waypoints.add(RouteWaypoint(startLat, startLng, 0))
        
        // Intermediate points
        for (i in 1 until steps) {
            val fraction = i.toDouble() / steps.toDouble()
            val interpLat = startLat + (endLat - startLat) * fraction
            val interpLng = startLng + (endLng - startLng) * fraction
            
            // Jitter ±0.001 degrees
            val jitterLat = interpLat + (Math.random() - 0.5) * 0.002
            val jitterLng = interpLng + (Math.random() - 0.5) * 0.002
            
            // Altitude 80 - 140 meters for intermediates
            val altitude = (80..140).random()
            waypoints.add(RouteWaypoint(jitterLat, jitterLng, altitude))
        }
        
        // Destination
        waypoints.add(RouteWaypoint(endLat, endLng, 0))
        
        // Compute total distance cumulative
        var totalDistance = 0.0
        for (i in 0 until waypoints.size - 1) {
            val w1 = waypoints[i]
            val w2 = waypoints[i + 1]
            totalDistance += calculateHaversineDistance(w1.lat, w1.lng, w2.lat, w2.lng)
        }
        
        // ETA = distance / speed
        val etaMinutes = totalDistance / DRONE_SPEED_KMMIN
        
        // Heuristic route quality score (0.920 to 0.990)
        val scoreValue = 0.920 + Math.random() * 0.070
        
        return AStarRoute(
            waypoints = waypoints,
            distance = totalDistance,
            eta = etaMinutes,
            score = scoreValue
        )
    }
}
