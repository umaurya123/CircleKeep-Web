package com.circlekeep

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import com.circlekeep.ui.CircleKeepApp

private var pendingImportData by mutableStateOf<String?>(null)

fun MainViewController(
    importData: String? = null,
    onImportConsumed: () -> Unit = {}
) = ComposeUIViewController {
    CircleKeepApp(
        importData = importData ?: pendingImportData,
        onImportConsumed = {
            pendingImportData = null
            onImportConsumed()
        }
    )
}

fun handleExternalImport(data: String) {
    pendingImportData = data
}
