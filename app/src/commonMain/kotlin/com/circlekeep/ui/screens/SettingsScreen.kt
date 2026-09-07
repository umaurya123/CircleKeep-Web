package com.circlekeep.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.circlekeep.FilePicker
import com.circlekeep.FilePickerMode
import com.circlekeep.LocalPlatformUI
import com.circlekeep.getPlatform
import com.circlekeep.ui.theme.LocalAppStrings
import com.circlekeep.viewmodel.FriendViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: FriendViewModel,
    onBackClick: () -> Unit
) {
    val strings = LocalAppStrings.current
    val themePreference by viewModel.themeState.collectAsState()
    val languagePreference by viewModel.languageState.collectAsState()
    val isPaid by viewModel.isPaidState.collectAsState()
    val remindersEnabled by viewModel.remindersEnabledState.collectAsState()
    val platformUI = LocalPlatformUI.current
    val platform = getPlatform()
    
    var importTrigger by remember { mutableStateOf(false) }
    var importData by remember { mutableStateOf<String?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

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
    val exportData = remember(exportTrigger) { 
        if (exportTrigger) viewModel.getExportData() else null 
    }
    
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
            TopAppBar(
                title = {
                    Column {
                        Text(strings.appName, style = MaterialTheme.typography.titleLarge)
                        Text(strings.settings, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(strings.theme, style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                FilterChip(
                    selected = themePreference == "Light",
                    onClick = { viewModel.onThemeChange("Light") },
                    label = { Text(strings.light) }
                )
                FilterChip(
                    selected = themePreference == "Dark",
                    onClick = { viewModel.onThemeChange("Dark") },
                    label = { Text(strings.dark) }
                )
                FilterChip(
                    selected = themePreference == "System",
                    onClick = { viewModel.onThemeChange("System") },
                    label = { Text(strings.system) }
                )
            }
            
            Spacer(Modifier.height(16.dp))
            Text(strings.language, style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                FilterChip(
                    selected = languagePreference == "English",
                    onClick = { viewModel.onLanguageChange("English") },
                    label = { Text("English") }
                )
                FilterChip(
                    selected = languagePreference == "Hindi",
                    onClick = { viewModel.onLanguageChange("Hindi") },
                    label = { Text("हिन्दी") }
                )
                FilterChip(
                    selected = languagePreference == "Spanish",
                    onClick = { viewModel.onLanguageChange("Spanish") },
                    label = { Text("Español") }
                )
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            ListItem(
                headlineContent = { Text(strings.eventNotifications) },
                supportingContent = { Text(strings.eventNotificationsDesc) },
                trailingContent = {
                    Switch(
                        checked = remindersEnabled,
                        onCheckedChange = { 
                            viewModel.onRemindersToggle(it) 
                            if (it) {
                                platformUI.requestNotificationPermission()
                            }
                        }
                    )
                }
            )

            val hideQr by viewModel.hideQrState.collectAsState()
            ListItem(
                headlineContent = { Text(strings.hideQr) },
                supportingContent = { Text(strings.hideQrDesc) },
                trailingContent = {
                    Switch(
                        checked = hideQr,
                        onCheckedChange = viewModel::onHideQrChange
                    )
                }
            )
            
            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            
            if (!isPaid) {
                Button(
                    onClick = { platformUI.launchPurchaseFlow("pro_upgrade") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(Icons.Default.Build, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(strings.purchaseApp)
                }
                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { platformUI.queryPurchases() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Build, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(strings.restorePurchases)
                }
                Spacer(Modifier.height(12.dp))
            }

            Button(
                onClick = { exportTrigger = true }, 
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(strings.exportData)
            }
            
            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = { platformUI.sendDataByEmail(viewModel.getExportData()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Email, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(strings.exportAndEmailData)
            }

            Spacer(Modifier.height(12.dp))
            
            OutlinedButton(onClick = { importTrigger = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.ArrowDownward, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(strings.importData)
            }

            Spacer(Modifier.height(12.dp))
            
            TextButton(
                onClick = { platformUI.sendEmail("CircleKeepApp@gmail.com") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Email, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(strings.sendFeedback)
            }

            TextButton(
                onClick = { platformUI.openUrl("https://circlekeepapp.com") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Language, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(strings.visitWebsite)
            }

            val isDebugOrBeta = platform.buildVariant.lowercase().let { it == "debug" || it == "beta" } || 
                               platform.buildVariant.isBlank() || 
                               platform.buildVariant == "Unknown"

            if (isDebugOrBeta) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { showDeleteAllDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(strings.deleteAllRecords)
                }
            }

            Spacer(Modifier.weight(1f))
            
            ListItem(
                headlineContent = { Text(strings.appVersion) },
                supportingContent = { Text("${platform.appVersion} (${platform.buildVariant})") }
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text(strings.developer) },
                supportingContent = { Text("CircleKeep Team") }
            )
            if (isPaid) {
                ListItem(
                    headlineContent = { Text(strings.status) },
                    supportingContent = { Text(strings.proVersionActive, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
                )
            }
        }
    }

    if (showImportDialog && importData != null) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text(strings.importData) },
            text = { Text("Are you sure you want to import this data? Existing matches will be skipped.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        importData?.let { viewModel.importData(it) }
                        showImportDialog = false
                        importData = null
                    }
                ) {
                    Text(strings.importData)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text(strings.deleteAllRecords) },
            text = { Text("Are you sure you want to delete all friends, children, and groups? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showDeleteAllDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(strings.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}
