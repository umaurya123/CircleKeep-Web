package com.circlekeep

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

interface PlatformUI {
    fun openUrl(url: String)
    fun openMap(address: String)
    fun dialPhone(phoneNumber: String)
    fun sendEmail(email: String)
    fun showToast(message: String)
    fun showInterstitialAd(onAdDismissed: () -> Unit)
    fun exitApp()
    fun launchPurchaseFlow(productId: String)
    fun queryPurchases()
    fun encodeUrl(text: String): String
    fun requestNotificationPermission()
}

@Composable
expect fun rememberPlatformUI(): PlatformUI

val LocalPlatformUI = staticCompositionLocalOf<PlatformUI> {
    error("PlatformUI not provided")
}

@Composable
expect fun ContactPicker(
    onContactPicked: (
        firstName: String, 
        middleName: String, 
        lastName: String, 
        cellPhone: String, 
        officePhone: String, 
        email: String, 
        workEmail: String, 
        address: String, 
        companyName: String, 
        notes: String, 
        dateOfBirth: String, 
        anniversaryDate: String
    ) -> Unit,
    onCancel: () -> Unit,
    trigger: Boolean,
    onTriggerReset: () -> Unit
)

@Composable
expect fun ImagePicker(
    onImagePicked: (String?) -> Unit,
    trigger: Boolean,
    onTriggerReset: () -> Unit
)

@Composable
expect fun BannerAdView()

@Composable
expect fun PlatformBackHandler(enabled: Boolean = true, onBack: () -> Unit)

@Composable
expect fun FilePicker(
    onFilePicked: (String) -> Unit,
    trigger: Boolean,
    onTriggerReset: () -> Unit,
    mode: FilePickerMode,
    dataToSave: String? = null
)

enum class FilePickerMode { Read, Create }

@Composable
expect fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
)
