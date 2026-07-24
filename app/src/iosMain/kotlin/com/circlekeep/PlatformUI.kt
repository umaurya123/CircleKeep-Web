package com.circlekeep

import androidx.compose.runtime.Composable

class IOSPlatformUI : PlatformUI {
    override fun openUrl(url: String) {}
    override fun dialPhone(phoneNumber: String) {}
    override fun sendEmail(email: String) {}
    override fun showToast(message: String) {}
    override fun exitApp() {}
}

@Composable
actual fun rememberPlatformUI(): PlatformUI = IOSPlatformUI()

@Composable
actual fun ContactPicker(
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
) {
    if (trigger) onTriggerReset()
}

@Composable
actual fun ImagePicker(
    onImagePicked: (String?) -> Unit,
    trigger: Boolean,
    onTriggerReset: () -> Unit
) {
    if (trigger) onTriggerReset()
}

@Composable
actual fun BannerAdView() {}

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {}

@Composable
actual fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: androidx.compose.ui.Modifier
) {
}

@Composable
actual fun FilePicker(
    onFilePicked: (String) -> Unit,
    trigger: Boolean,
    onTriggerReset: () -> Unit,
    mode: FilePickerMode
) {
    if (trigger) onTriggerReset()
}
