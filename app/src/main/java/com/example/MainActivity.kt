package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.MainViewModel
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val viewModel: MainViewModel = viewModel()
        val currentScreen by viewModel.screen
        val toastNotification by viewModel.notification

        Scaffold(
          modifier = Modifier.fillMaxSize(),
          contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color(0xFF0A0F1A))
              .padding(innerPadding)
          ) {
            // Main Router Screen with smooth conditional routing
            Box(modifier = Modifier.fillMaxSize()) {
              when (currentScreen) {
                "LOGIN" -> LoginScreen(viewModel = viewModel)
                "CUSTOMER_HOME" -> CustomerHomeScreen(viewModel = viewModel)
                "MENU" -> MenuScreen(viewModel = viewModel)
                "CHECKOUT" -> CheckoutScreen(viewModel = viewModel)
                "TRACKING" -> OrderTrackingScreen(viewModel = viewModel)
                "ADMIN" -> AdminDashboardScreen(viewModel = viewModel)
                else -> LoginScreen(viewModel = viewModel)
              }
            }

            // Floating Toast notification banner at the top
            AnimatedVisibility(
                visible = toastNotification != null,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
              toastNotification?.let { (msg, type) ->
                val cardColor = when (type) {
                  "SUCCESS" -> Color(0xFF134E5E)
                  "WARNING" -> Color(0xFF78350F)
                  else -> Color(0xFF1E293B)
                }
                val borderColor = when (type) {
                  "SUCCESS" -> Color(0xFF00E5FF)
                  "WARNING" -> Color(0xFFFF9500)
                  else -> Color(0xFF475569)
                }

                Card(
                  colors = CardDefaults.cardColors(containerColor = cardColor),
                  modifier = Modifier
                      .fillMaxWidth()
                      .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                      .testTag("toast_notification_card"),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      Icons.Default.Info,
                      contentDescription = "Notification Status",
                      tint = if (type == "SUCCESS") Color(0xFF00E5FF) else if (type == "WARNING") Color(0xFFFF9500) else Color.White,
                      modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                      text = msg,
                      color = Color.White,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.weight(1f)
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

