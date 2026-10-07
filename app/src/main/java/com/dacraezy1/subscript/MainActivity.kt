package com.dacraezy1.subscript

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.dacraezy1.subscript.ui.screens.DashboardScreen
import com.dacraezy1.subscript.ui.theme.SubScriptTheme
import com.dacraezy1.subscript.ui.viewmodel.SubscriptionViewModel

/**
 * Main Activity hosting the Jetpack Compose UI.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: SubscriptionViewModel by viewModels {
        val app = application as SubScriptApp
        SubscriptionViewModel.Factory(app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SubScriptTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DashboardScreen(viewModel = viewModel)
                }
            }
        }
    }
}
