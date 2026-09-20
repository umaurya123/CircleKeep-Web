package com.circlekeep

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.circlekeep.ui.CircleKeepApp

class MainActivity : ComponentActivity() {
    private var initialImportData by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle incoming intent if app was closed
        intent?.let { handleIntent(it) }

        enableEdgeToEdge()
        setContent {
            CircleKeepApp(importData = initialImportData, onImportConsumed = { initialImportData = null })
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle incoming intent if app was already running
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_VIEW) {
            val uri = intent.data
            if (uri != null) {
                try {
                    contentResolver.openInputStream(uri)?.use { input ->
                        initialImportData = input.bufferedReader().use { it.readText() }
                    }
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    CircleKeepApp()
}
