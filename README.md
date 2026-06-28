# SkyDash 🛸

SkyDash is a cutting-edge, autonomous drone delivery platform simulator built natively for Android using Jetpack Compose and Material Design 3. It simulates real-time flight telemetry, adaptive airspace routing, dynamic fleet coordination, and offers an end-to-end customer-to-admin logistics experience.

## ✨ Core Features

### 👤 Customer Experience
*   **Secure Authentication**: A futuristic login portal styling high-contrast input fields and secure credential validation.
*   **Curated Menu & Checkout**: Seamless interactive food/cargo menu with responsive cart management and animated, tactile checkout flows.
*   **Real-time Drone Telemetry Tracking**: Watch live flight metrics (altitude, speed, wind resistance, and remaining battery life) with an interactive flight path visualizer.

### 👑 Admin & Airspace Dispatch Dashboard
*   **Interactive Airspace Map**: Monitor active deliveries and track coordinates in a styled slate-colored control panel.
*   **Fleet Status & Operations**: Appoint drone status (Idle, Dispatching, En Route, Returning), manage battery alerts, and inspect sensor reports.
*   **Order Action Hub**: Approve new orders, assign drone models, and deploy fleet tasks instantly with custom visual feedback.

### ⚙️ Telemetry Simulation Engine
*   **Physics-based Flight Pathing**: Generates real-time latitude, longitude, and elevation increments.
*   **Ambient Atmospheric Constraints**: Dynamically simulates crosswinds, high-altitude air pressure, and emergency rerouting behaviors.

---

## 🎨 Visual Identity

SkyDash features a gorgeous **Cosmic Slate Theme** (deep interstellar background paired with high-contrast neon teal and amber accent highlights) optimized for dynamic dark mode:
*   **Edge-to-Edge Fluidity**: Uses Material 3 edge-to-edge window insets (`enableEdgeToEdge`) for distraction-free monitoring.
*   **Futuristic Design Language**: Incorporates custom glassmorphic cards, clear indicators with Material Symbols, high-density telemetry grids, and tactile feedback patterns.

---

## 🛠️ Technology Stack

*   **UI Framework**: [Jetpack Compose](https://developer.android.com/compose) (100% Kotlin declarative UI)
*   **Design System**: Material Design 3 (M3)
*   **Architecture**: MVVM (Model-View-ViewModel) with structured unidirectional state flow using `MutableStateFlow` and Compose state triggers.
*   **Asynchronous Engine**: Kotlin Coroutines & Flows for smooth real-time background simulation loops.
*   **Testing Infrastructure**:
    *   **Robolectric**: Local JVM context testing for reliable verification of simulation algorithms and view state assertions.
    *   **Roborazzi**: Automated visual regression and screenshot verification suite.

---

## 🚀 Getting Started

### 📦 Prerequisites
*   Android Studio Ladybug (or newer)
*   JDK 17+
*   Gradle 8+

### 🔨 Running & Building

To compile and assemble the debug application package, run:
```bash
gradle assembleDebug
```

### 🧪 Executing Tests

To run local JVM unit and integration tests:
```bash
gradle :app:testDebugUnitTest
```

To run Roborazzi screenshot verification:
```bash
gradle :app:verifyRoborazziDebug
```

To update visual golden screenshots after modifying design layouts:
```bash
gradle :app:recordRoborazziDebug
```
