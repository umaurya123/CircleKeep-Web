package com.circlekeep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.circlekeep.ui.CircleKeepApp
import com.circlekeep.ui.theme.CircleKeepTheme
import com.circlekeep.viewmodel.FriendViewModel
import com.circlekeep.viewmodel.getFriendViewModelFactory
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: FriendViewModel = viewModel(factory = getFriendViewModelFactory())
            val themePreference by viewModel.themeState.collectAsState()
            val platformUI = rememberPlatformUI()
            
            CompositionLocalProvider(LocalPlatformUI provides platformUI) {
                CircleKeepTheme(themePreference = themePreference) {
                    CircleKeepApp()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    CircleKeepTheme {
        CircleKeepApp()
    }
}
