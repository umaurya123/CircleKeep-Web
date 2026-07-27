package com.circlekeep

import androidx.compose.foundation.clickable
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import platform.UIKit.*
import platform.Foundation.*
import platform.darwin.NSObject
import platform.darwin.dispatch_after
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_time
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.NSEC_PER_SEC
import kotlinx.cinterop.ExperimentalForeignApi

class IOSPlatformUI : PlatformUI {
    override fun openUrl(url: String) {
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl != null) {
            UIApplication.sharedApplication.openURL(nsUrl)
        }
    }

    override fun dialPhone(phoneNumber: String) {
        val cleanPhone = phoneNumber.filter { it.isDigit() || it == '+' }
        openUrl("tel:$cleanPhone")
    }

    override fun sendEmail(email: String) {
        openUrl("mailto:$email")
    }

    override fun showToast(message: String) {
        val alert = UIAlertController.alertControllerWithTitle(
            title = null,
            message = message,
            preferredStyle = UIAlertControllerStyleAlert
        )
        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(alert, true, null)
        
        val delay = 2L * NSEC_PER_SEC.toLong()
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, delay), dispatch_get_main_queue()) {
            alert.dismissViewControllerAnimated(true, null)
        }
    }

    override fun exitApp() {}
}

@Composable
actual fun rememberPlatformUI(): PlatformUI = remember { IOSPlatformUI() }

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
    if (trigger) {
        LaunchedEffect(Unit) {
            onTriggerReset()
        }
    }
}

@OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
@Composable
actual fun ImagePicker(
    onImagePicked: (String?) -> Unit,
    trigger: Boolean,
    onTriggerReset: () -> Unit
) {
    if (trigger) {
        val imagePicker = remember { UIImagePickerController() }
        val delegate = remember {
            object : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
                override fun imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo: Map<Any?, *>) {
                    val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
                    if (image != null) {
                        val data = UIImageJPEGRepresentation(image, 0.8)
                        if (data != null) {
                            val paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
                            val documentDirectory = paths.firstOrNull() as? String
                            val fileName = "img_${NSDate().timeIntervalSince1970}.jpg"
                            val filePath = "$documentDirectory/$fileName"
                            data.writeToFile(filePath, true)
                            onImagePicked(filePath)
                        } else {
                            onImagePicked(null)
                        }
                    } else {
                        onImagePicked(null)
                    }
                    picker.dismissViewControllerAnimated(true, null)
                    onTriggerReset()
                }

                override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                    picker.dismissViewControllerAnimated(true, null)
                    onTriggerReset()
                }
            }
        }

        LaunchedEffect(Unit) {
            imagePicker.delegate = delegate
            imagePicker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
            UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(imagePicker, true, null)
        }
    }
}

@Composable
actual fun BannerAdView() {}

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    val initialDateMillis = remember(value) {
        try {
            val formatter = NSDateFormatter()
            formatter.dateFormat = "dd/MM/yyyy"
            val date = formatter.dateFromString(value)
            if (date != null) {
                (date.timeIntervalSince1970 * 1000).toLong()
            } else {
                (NSDate().timeIntervalSince1970 * 1000).toLong()
            }
        } catch (_: Exception) {
            (NSDate().timeIntervalSince1970 * 1000).toLong()
        }
    }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis
    )
    
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val date = NSDate.dateWithTimeIntervalSince1970(it / 1000.0)
                        val formatter = NSDateFormatter()
                        formatter.dateFormat = "dd/MM/yyyy"
                        onValueChange(formatter.stringFromDate(date))
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        onValueChange("")
                        showDatePicker = false
                    }) {
                        Text("Clear")
                    }
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it) },
        label = { Text(label) },
        modifier = modifier.fillMaxWidth().clickable { showDatePicker = true },
        enabled = true,
        readOnly = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
        ),
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Rounded.CalendarToday, contentDescription = null)
            }
        }
    )
}

@Composable
actual fun FilePicker(
    onFilePicked: (String) -> Unit,
    trigger: Boolean,
    onTriggerReset: () -> Unit,
    mode: FilePickerMode,
    dataToSave: String?
) {
    if (trigger) onTriggerReset()
}
