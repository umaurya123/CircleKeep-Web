package com.circlekeep.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.circlekeep.FilePicker
import com.circlekeep.FilePickerMode
import com.circlekeep.LocalPlatformUI
import com.circlekeep.viewmodel.FriendViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: FriendViewModel) {
    val themePreference by viewModel.themeState.collectAsState()
    val isPaid by viewModel.isPaidState.collectAsState()
    val platformUI = LocalPlatformUI.current
    val platform = com.circlekeep.getPlatform()
    
    var importTrigger by remember { mutableStateOf(false) }
    var importData by remember { mutableStateOf<String?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }

    FilePicker(
        trigger = importTrigger,
        onTriggerReset = { importTrigger = false },
        onFilePicked = { 
            importData = it
            showImportDialog = true
        },
        mode = FilePickerMode.Read
    )

    var exportTrigger by remember { mutableStateOf(false) }
    val exportData = if (exportTrigger) viewModel.getExportData() else null
    
    FilePicker(
        trigger = exportTrigger,
        onTriggerReset = { exportTrigger = false },
        onFilePicked = { result ->
            if (result == "Success") {
                platformUI.showToast("Data exported successfully")
            } else if (result.startsWith("Error")) {
                platformUI.showToast(result)
            }
        },
        mode = FilePickerMode.Create,
        dataToSave = exportData
    )

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Column {
                    Text("CircleKeep", style = MaterialTheme.typography.titleLarge)
                    Text("Settings", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Theme", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                FilterChip(
                    selected = themePreference == "Light",
                    onClick = { viewModel.onThemeChange("Light") },
                    label = { Text("Light") }
                )
                FilterChip(
                    selected = themePreference == "Dark",
                    onClick = { viewModel.onThemeChange("Dark") },
                    label = { Text("Dark") }
                )
                FilterChip(
                    selected = themePreference == "System",
                    onClick = { viewModel.onThemeChange("System") },
                    label = { Text("System") }
                )
            }
            
            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            
            if (!isPaid) {
                Button(
                    onClick = { viewModel.purchaseApp() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(Icons.Rounded.ShoppingCart, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Purchase App (Remove Ads)")
                }
                Spacer(Modifier.height(12.dp))
            }

            Button(
                onClick = { exportTrigger = true }, 
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.FileUpload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Export Data")
            }
            
            Spacer(Modifier.height(12.dp))
            
            OutlinedButton(onClick = { importTrigger = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Rounded.FileDownload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Import Data")
            }

            Spacer(Modifier.height(12.dp))
            
            TextButton(
                onClick = { platformUI.sendEmail("CircleKeepApp@gmail.com") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Feedback, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Send Feedback")
            }

            Spacer(Modifier.weight(1f))
            
            ListItem(
                headlineContent = { Text("App Version") },
                supportingContent = { Text("1.2.0 (${platform.buildVariant})") }
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("Developer") },
                supportingContent = { Text("CircleKeep Team") }
            )
            if (isPaid) {
                ListItem(
                    headlineContent = { Text("Status") },
                    supportingContent = { Text("Pro Version Active", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
                )
            }
        }
    }

    if (showImportDialog && importData != null) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Data") },
            text = { Text("Are you sure you want to import this data? Existing matches will be skipped.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        importData?.let { viewModel.importData(it) }
                        showImportDialog = false
                        importData = null
                    }
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
