package com.circlekeep

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import platform.UIKit.*
import platform.Foundation.*
import platform.Contacts.*
import platform.ContactsUI.*
import platform.darwin.NSObject
import platform.darwin.dispatch_after
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_time
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.NSEC_PER_SEC
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import platform.UniformTypeIdentifiers.UTType
import platform.UniformTypeIdentifiers.UTTypeJSON

interface NativePlatformProvider {
    fun getBannerView(): UIView
    fun showInterstitialAd(onAdDismissed: () -> Unit)
    fun launchPurchaseFlow(productId: String)
    fun queryPurchases()
}

private var platformProvider: NativePlatformProvider? = null

fun setPlatformProvider(provider: NativePlatformProvider) {
    platformProvider = provider
}

fun notifyPurchaseSuccess() {
    MainScope().launch(kotlinx.coroutines.Dispatchers.Main) {
        com.circlekeep.viewmodel.userPreferencesRepository.updateIsPaid(true)
    }
}

class IOSPlatformUI : PlatformUI {
    override fun openUrl(url: String) {
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl != null) {
            dispatch_async(dispatch_get_main_queue()) {
                UIApplication.sharedApplication.openURL(nsUrl, options = emptyMap<Any?, Any?>(), completionHandler = null)
            }
        }
    }

    override fun openMap(address: String) {
        val nsString = address as platform.Foundation.NSString
        val encodedAddress = nsString.stringByAddingPercentEncodingWithAllowedCharacters(
            platform.Foundation.NSCharacterSet.URLQueryAllowedCharacterSet()
        ) ?: address.replace(" ", "%20")
        openUrl("maps://?q=$encodedAddress")
    }

    override fun dialPhone(phoneNumber: String) {
        val cleanPhone = phoneNumber.filter { it.isDigit() || it == '+' }
        openUrl("tel:$cleanPhone")
    }

    override fun sendEmail(email: String) {
        openUrl("mailto:$email")
    }

    override fun showToast(message: String) {
        dispatch_async(dispatch_get_main_queue()) {
            val topVC = getTopViewController()
            if (topVC != null) {
                val alert = UIAlertController.alertControllerWithTitle(
                    title = null,
                    message = message,
                    preferredStyle = UIAlertControllerStyleAlert
                )
                topVC.presentViewController(alert, true, null)
                
                val delay = 2L * NSEC_PER_SEC.toLong()
                dispatch_after(dispatch_time(DISPATCH_TIME_NOW, delay), dispatch_get_main_queue()) {
                    alert.dismissViewControllerAnimated(true, null)
                }
            }
        }
    }

    override fun showInterstitialAd(onAdDismissed: () -> Unit) {
        val provider = platformProvider
        if (provider != null) {
            provider.showInterstitialAd(onAdDismissed)
        } else {
            onAdDismissed()
        }
    }

    override fun exitApp() {}

    override fun launchPurchaseFlow(productId: String) {
        platformProvider?.launchPurchaseFlow(productId)
    }

    override fun queryPurchases() {
        platformProvider?.queryPurchases()
    }

    companion object {
        fun getTopViewController(): UIViewController? {
            val window = UIApplication.sharedApplication.windows.filterIsInstance<UIWindow>().firstOrNull { it.isKeyWindow() }
                ?: UIApplication.sharedApplication.keyWindow
            
            var topController = window?.rootViewController
            while (topController?.presentedViewController != null && !topController.presentedViewController!!.isBeingDismissed()) {
                topController = topController.presentedViewController
            }
            return topController
        }

        fun presentSafe(viewController: UIViewController, animated: Boolean = true, completion: (() -> Unit)? = null) {
            dispatch_async(dispatch_get_main_queue()) {
                val topVC = getTopViewController()
                if (topVC != null) {
                    if (topVC.presentedViewController != null) {
                        println("DEBUG: topVC is already presenting, waiting...")
                        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (0.1 * NSEC_PER_SEC.toDouble()).toLong()), dispatch_get_main_queue()) {
                            presentSafe(viewController, animated, completion)
                        }
                    } else {
                        topVC.presentViewController(viewController, animated, completion)
                    }
                }
            }
        }
    }
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
        val delegate = remember {
            object : NSObject(), CNContactPickerDelegateProtocol {
                override fun contactPicker(picker: CNContactPickerViewController, didSelectContact: CNContact) {
                    val firstName = didSelectContact.givenName
                    val middleName = didSelectContact.middleName
                    val lastName = didSelectContact.familyName
                    
                    var cellPhone = ""
                    var officePhone = ""
                    didSelectContact.phoneNumbers.forEach { 
                        val labeledValue = it as? CNLabeledValue
                        val value = labeledValue?.value as? CNPhoneNumber
                        val stringValue = value?.stringValue ?: ""
                        val label = labeledValue?.label
                        if (label == CNLabelPhoneNumberMobile || label == CNLabelPhoneNumberiPhone) {
                            if (cellPhone.isBlank()) cellPhone = stringValue
                        } else if (label == CNLabelWork) {
                            if (officePhone.isBlank()) officePhone = stringValue
                        } else {
                            if (cellPhone.isBlank()) cellPhone = stringValue
                        }
                    }

                    var email = ""
                    var workEmail = ""
                    didSelectContact.emailAddresses.forEach {
                        val labeledValue = it as? CNLabeledValue
                        val stringValue = labeledValue?.value as? String ?: ""
                        val label = labeledValue?.label
                        if (label == CNLabelHome) {
                            if (email.isBlank()) email = stringValue
                        } else if (label == CNLabelWork) {
                            if (workEmail.isBlank()) workEmail = stringValue
                        } else {
                            if (email.isBlank()) email = stringValue
                        }
                    }

                    var addressValue = ""
                    didSelectContact.postalAddresses.firstOrNull()?.let {
                        val labeledValue = it as? CNLabeledValue
                        val value = labeledValue?.value as? CNPostalAddress
                        if (value != null) {
                            addressValue = listOf(value.street, value.city, value.state, value.postalCode, value.country)
                                .filter { it.isNotBlank() }.joinToString(", ")
                        }
                    }

                    val companyName = didSelectContact.organizationName
                    val notes = didSelectContact.note
                    
                    var dob = ""
                    didSelectContact.birthday?.let {
                        val day = it.day.toString().padStart(2, '0')
                        val month = it.month.toString().padStart(2, '0')
                        val year = if (it.year > 0) it.year.toString() else "1900"
                        dob = "$day/$month/$year"
                    }

                    onContactPicked(
                        firstName, middleName, lastName, 
                        cellPhone, officePhone, email, workEmail,
                        addressValue, companyName, notes, dob, ""
                    )
                    
                    onTriggerReset()
                    dispatch_async(dispatch_get_main_queue()) {
                        picker.dismissViewControllerAnimated(true, null)
                    }
                }

                override fun contactPickerDidCancel(picker: CNContactPickerViewController) {
                    onCancel()
                    onTriggerReset()
                    dispatch_async(dispatch_get_main_queue()) {
                        picker.dismissViewControllerAnimated(true, null)
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            // Small delay to ensure no re-entrancy issues with PPT or other transitions
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (0.2 * NSEC_PER_SEC.toDouble()).toLong()), dispatch_get_main_queue()) {
                val picker = CNContactPickerViewController()
                picker.delegate = delegate
                val topVC = IOSPlatformUI.getTopViewController()
                if (topVC != null) {
                    println("DEBUG: Presenting Contact Picker from ${topVC::class.simpleName}")
                    topVC.presentViewController(picker, true, null)
                } else {
                    println("ERROR: Could not find top view controller to present Contact Picker")
                    onTriggerReset()
                }
            }
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
        val delegate = remember {
            object : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
                override fun imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo: Map<Any?, *>) {
                    val image = (didFinishPickingMediaWithInfo[UIImagePickerControllerEditedImage] ?: didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage]) as? UIImage
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
                    
                    dispatch_async(dispatch_get_main_queue()) {
                        picker.dismissViewControllerAnimated(true, null)
                        onTriggerReset()
                    }
                }

                override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                    dispatch_async(dispatch_get_main_queue()) {
                        picker.dismissViewControllerAnimated(true, null)
                        onTriggerReset()
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (0.2 * NSEC_PER_SEC.toDouble()).toLong()), dispatch_get_main_queue()) {
                val imagePicker = UIImagePickerController()
                imagePicker.delegate = delegate
                imagePicker.allowsEditing = true
                imagePicker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
                val topVC = IOSPlatformUI.getTopViewController()
                if (topVC != null) {
                    topVC.presentViewController(imagePicker, true, null)
                } else {
                    onTriggerReset()
                }
            }
        }
    }
}

@Composable
actual fun BannerAdView() {
    val provider = platformProvider
    if (provider != null) {
        UIKitView(
            factory = { provider.getBannerView() },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "CircleKeep - Keeping you connected",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

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

@OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
@Composable
actual fun FilePicker(
    onFilePicked: (String) -> Unit,
    trigger: Boolean,
    onTriggerReset: () -> Unit,
    mode: FilePickerMode,
    dataToSave: String?
) {
    if (trigger) {
        val delegate = remember {
            object : NSObject(), UIDocumentPickerDelegateProtocol {
                override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
                    val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
                    if (url != null) {
                        if (mode == FilePickerMode.Read) {
                            val data = NSData.dataWithContentsOfURL(url)
                            if (data != null) {
                                val string = NSString.create(data = data, encoding = NSUTF8StringEncoding)
                                onFilePicked(string?.toString() ?: "")
                            }
                        } else {
                            onFilePicked("Success")
                        }
                    }
                    
                    dispatch_async(dispatch_get_main_queue()) {
                        controller.dismissViewControllerAnimated(true, null)
                        onTriggerReset()
                    }
                }

                override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                    dispatch_async(dispatch_get_main_queue()) {
                        controller.dismissViewControllerAnimated(true, null)
                        onTriggerReset()
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (0.2 * NSEC_PER_SEC.toDouble()).toLong()), dispatch_get_main_queue()) {
                val picker = if (mode == FilePickerMode.Create && dataToSave != null) {
                    val tempDir = NSTemporaryDirectory()
                    val formatter = NSDateFormatter()
                    formatter.dateFormat = "yyyyMMdd_HHmm"
                    val timestamp = formatter.stringFromDate(NSDate())
                    val fileName = "CircleKeep_Backup_$timestamp.json"
                    val filePath = if (tempDir.endsWith("/")) tempDir + fileName else "$tempDir/$fileName"
                    
                    val nsString = NSString.create(string = dataToSave)
                    nsString.writeToFile(filePath, true, NSUTF8StringEncoding, null)
                    
                    val url = NSURL.fileURLWithPath(filePath)
                    UIDocumentPickerViewController(forExportingURLs = listOf(url))
                } else {
                    UIDocumentPickerViewController(forOpeningContentTypes = listOf<UTType>(UTTypeJSON), asCopy = true)
                }
                picker.delegate = delegate
                val topVC = IOSPlatformUI.getTopViewController()
                if (topVC != null) {
                    topVC.presentViewController(picker, true, null)
                } else {
                    println("ERROR: Could not find top view controller to present File Picker")
                    onTriggerReset()
                }
            }
        }
    }
}
