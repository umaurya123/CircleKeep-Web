@file:JvmName("PlatformUIAndroid")
package com.circlekeep

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.circlekeep.ui.BannerAd
import com.yalantis.ucrop.UCrop
import java.io.File

class AndroidPlatformUI(
    private val context: android.content.Context,
    private val billingManager: BillingManager? = null
) : PlatformUI {
    override fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    override fun openMap(address: String) {
        val encodedAddress = Uri.encode(address)
        openUrl("geo:0,0?q=$encodedAddress")
    }

    override fun dialPhone(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
        context.startActivity(intent)
    }

    override fun sendEmail(email: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
        }
        context.startActivity(Intent.createChooser(intent, "Send Email"))
    }

    override fun sendDataByEmail(jsonData: String) {
        try {
            val cacheDir = File(context.cacheDir, "exports")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            
            val fileName = "CircleKeep_Backup_${System.currentTimeMillis()}.json"
            val file = File(cacheDir, fileName)
            file.writeText(jsonData)

            val contentUri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_SUBJECT, "CircleKeep Data Export")
                putExtra(Intent.EXTRA_TEXT, "Attached is your CircleKeep data export.")
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            val chooser = Intent.createChooser(intent, "Send Email")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            showToast("Failed to share data: ${e.message}")
        }
    }

    override fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun showInterstitialAd(onAdDismissed: () -> Unit) {
        val activity = context as? Activity ?: run {
            onAdDismissed()
            return
        }
        com.circlekeep.ui.loadInterstitialAd(context) { ad ->
            if (ad != null) {
                com.circlekeep.ui.showInterstitialAd(activity, ad, onAdDismissed)
            } else {
                onAdDismissed()
            }
        }
    }

    override fun exitApp() {
        (context as? Activity)?.finish()
    }

    override fun launchPurchaseFlow(productId: String) {
        val activity = context as? Activity ?: return
        billingManager?.launchPurchaseFlow(activity, productId)
    }

    override fun queryPurchases() {
        billingManager?.queryPurchases()
    }

    override fun encodeUrl(text: String): String {
        return java.net.URLEncoder.encode(text, "UTF-8")
    }

    override fun requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val activity = context as? Activity ?: return
            androidx.core.app.ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }
    }
}

@Composable
actual fun rememberPlatformUI(): PlatformUI {
    val context = LocalContext.current
    val application = context.applicationContext as? CircleKeepApplication
    return remember(context, application) { 
        AndroidPlatformUI(context, application?.billingManager) 
    }
}

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
    val context = LocalContext.current
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact(),
        onResult = { uri ->
            try {
                uri?.let {
                    val cursor = context.contentResolver.query(it, null, null, null, null)
                    cursor?.use { c ->
                        if (c.moveToFirst()) {
                            val idIndex = c.getColumnIndex(ContactsContract.Contacts._ID)
                            val contactId = if (idIndex >= 0) c.getString(idIndex) else null
                            
                            val nameIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                            val name = if (nameIndex >= 0) c.getString(nameIndex) else ""
                            
                            var importedFirstName = ""
                            var importedMiddleName = ""
                            var importedLastName = ""
                            var importedCellPhone = ""
                            var importedOfficePhone = ""
                            var importedEmail = ""
                            var importedWorkEmail = ""
                            var importedAddress = ""
                            var importedCompanyName = ""
                            var importedNotes = ""
                            var importedDateOfBirth = ""
                            var importedAnniversaryDate = ""

                            if (name.isNotBlank()) {
                                val parts = name.split(" ")
                                importedFirstName = parts.firstOrNull() ?: ""
                                importedLastName = if (parts.size > 1) parts.last() else ""
                                importedMiddleName = if (parts.size > 2) parts.subList(1, parts.size - 1).joinToString(" ") else ""
                            }
                            
                            contactId?.let { cid ->
                                // Structured Name
                                context.contentResolver.query(
                                    ContactsContract.Data.CONTENT_URI,
                                    null,
                                    "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                                    arrayOf(cid, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE),
                                    null
                                )?.use { sn ->
                                    if (sn.moveToFirst()) {
                                        val fnIdx = sn.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME)
                                        val mnIdx = sn.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME)
                                        val lnIdx = sn.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME)
                                        if (fnIdx >= 0) sn.getString(fnIdx)?.let { importedFirstName = it }
                                        if (mnIdx >= 0) sn.getString(mnIdx)?.let { importedMiddleName = it }
                                        if (lnIdx >= 0) sn.getString(lnIdx)?.let { importedLastName = it }
                                    }
                                }

                                // Phone Numbers
                                context.contentResolver.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                                    arrayOf(cid),
                                    "${ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY} DESC"
                                )?.use { pc ->
                                    val numIdx = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                    val typeIdx = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
                                    val primaryIdx = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY)
                                    
                                    while (pc.moveToNext()) {
                                        val number = if (numIdx >= 0) pc.getString(numIdx) ?: "" else ""
                                        val type = if (typeIdx >= 0) pc.getInt(typeIdx) else -1
                                        val isPrimary = if (primaryIdx >= 0) pc.getInt(primaryIdx) > 0 else false
                                        
                                        if (number.isNotBlank()) {
                                            if (isPrimary) {
                                                importedCellPhone = number
                                                // If we found the absolute default, we can stop looking for others
                                                // but let's see if there's an office phone too
                                            }

                                            when (type) {
                                                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> {
                                                    if (importedCellPhone.isBlank()) importedCellPhone = number
                                                }
                                                ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> {
                                                    if (importedOfficePhone.isBlank()) importedOfficePhone = number
                                                }
                                                else -> {
                                                    if (importedCellPhone.isBlank()) importedCellPhone = number
                                                }
                                            }
                                        }
                                    }
                                }

                                // Emails
                                context.contentResolver.query(
                                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                    null,
                                    "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
                                    arrayOf(cid),
                                    null
                                )?.use { ec ->
                                    val addressIdx = ec.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
                                    val typeIdx = ec.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE)
                                    while (ec.moveToNext()) {
                                        val emailAddr = if (addressIdx >= 0) ec.getString(addressIdx) ?: "" else ""
                                        val type = if (typeIdx >= 0) ec.getInt(typeIdx) else -1
                                        if (emailAddr.isNotBlank()) {
                                            when (type) {
                                                ContactsContract.CommonDataKinds.Email.TYPE_HOME -> importedEmail = emailAddr
                                                ContactsContract.CommonDataKinds.Email.TYPE_WORK -> importedWorkEmail = emailAddr
                                                else -> if (importedEmail.isBlank()) importedEmail = emailAddr
                                            }
                                        }
                                    }
                                }

                                // Address
                                context.contentResolver.query(
                                    ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
                                    null,
                                    "${ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID} = ?",
                                    arrayOf(cid),
                                    null
                                )?.use { ac ->
                                    val addrIdx = ac.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS)
                                    if (ac.moveToFirst() && addrIdx >= 0) {
                                        importedAddress = ac.getString(addrIdx) ?: ""
                                    }
                                }

                                // Organization
                                context.contentResolver.query(
                                    ContactsContract.Data.CONTENT_URI,
                                    null,
                                    "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                                    arrayOf(cid, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE),
                                    null
                                )?.use { oc ->
                                    val companyIdx = oc.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY)
                                    if (oc.moveToFirst() && companyIdx >= 0) {
                                        importedCompanyName = oc.getString(companyIdx) ?: ""
                                    }
                                }

                                // Notes
                                context.contentResolver.query(
                                    ContactsContract.Data.CONTENT_URI,
                                    null,
                                    "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                                    arrayOf(cid, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE),
                                    null
                                )?.use { nc ->
                                    val noteIdx = nc.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE)
                                    if (nc.moveToFirst() && noteIdx >= 0) {
                                        importedNotes = nc.getString(noteIdx) ?: ""
                                    }
                                }

                                // Events
                                context.contentResolver.query(
                                    ContactsContract.Data.CONTENT_URI,
                                    null,
                                    "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                                    arrayOf(cid, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE),
                                    null
                                )?.use { ec ->
                                    val typeIdx = ec.getColumnIndex(ContactsContract.CommonDataKinds.Event.TYPE)
                                    val dateIdx = ec.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE)
                                    while (ec.moveToNext()) {
                                        val type = if (typeIdx >= 0) ec.getInt(typeIdx) else -1
                                        val date = if (dateIdx >= 0) ec.getString(dateIdx) ?: "" else ""
                                        if (date.isNotBlank()) {
                                            when (type) {
                                                ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY -> {
                                                    importedDateOfBirth = date
                                                }
                                                ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY -> {
                                                    importedAnniversaryDate = date
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            
                            onContactPicked(
                                importedFirstName, importedMiddleName, importedLastName,
                                importedCellPhone, importedOfficePhone, importedEmail, importedWorkEmail,
                                importedAddress, importedCompanyName, importedNotes,
                                importedDateOfBirth, importedAnniversaryDate
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("CircleKeep", "Error picking contact", e)
            } finally {
                onTriggerReset()
            }
        }
    )

    val contactPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                contactPickerLauncher.launch(null)
            } else {
                Toast.makeText(context, "Contact permission is required to import contacts", Toast.LENGTH_SHORT).show()
                onTriggerReset()
            }
        }
    )

    if (trigger) {
        SideEffect {
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                contactPickerLauncher.launch(null)
            } else {
                contactPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
            }
        }
    }
}

@Composable
actual fun ImagePicker(
    onImagePicked: (String?) -> Unit,
    trigger: Boolean,
    onTriggerReset: () -> Unit
) {
    val context = LocalContext.current
    
    val cropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data != null) {
                    onImagePicked(UCrop.getOutput(data)?.toString())
                } else {
                    onImagePicked(null)
                }
            }
            onTriggerReset()
        }
    )

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> 
            uri?.let { selectedUri ->
                val destinationUri = File(context.filesDir, "crop_${System.currentTimeMillis()}.jpg").toUri()
                val uCrop = UCrop.of(selectedUri, destinationUri)
                    .withAspectRatio(1f, 1f)
                    .withMaxResultSize(500, 500)
                
                cropLauncher.launch(uCrop.getIntent(context))
            } ?: onTriggerReset()
        }
    )

    if (trigger) {
        SideEffect {
            launcher.launch("image/*")
        }
    }
}

@Composable
actual fun QRScanner(
    onCodeScanned: (String) -> Unit,
    onCancel: () -> Unit,
    trigger: Boolean,
    onTriggerReset: () -> Unit
) {
    val context = LocalContext.current
    var showScanner by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                showScanner = true
            } else {
                Toast.makeText(context, "Camera permission is required to scan QR codes", Toast.LENGTH_SHORT).show()
                onTriggerReset()
            }
        }
    )

    LaunchedEffect(trigger) {
        if (trigger) {
            isProcessing = false
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                showScanner = true
            } else {
                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    if (showScanner) {
        Dialog(
            onDismissRequest = { 
                showScanner = false
                onCancel()
                onTriggerReset()
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                val lifecycleOwner = LocalLifecycleOwner.current
                val cameraProviderFuture = remember { androidx.camera.lifecycle.ProcessCameraProvider.getInstance(context) }
                
                AndroidView(
                    factory = { ctx ->
                        val previewView = androidx.camera.view.PreviewView(ctx)
                        val executor = ContextCompat.getMainExecutor(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = androidx.camera.core.Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val scanner = com.google.mlkit.vision.barcode.BarcodeScanning.getClient()
                            val imageAnalysis = androidx.camera.core.ImageAnalysis.Builder()
                                .setBackpressureStrategy(androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            imageAnalysis.setAnalyzer(executor) { imageProxy ->
                                val mediaImage = imageProxy.image
                                if (mediaImage != null && !isProcessing) {
                                    val image = com.google.mlkit.vision.common.InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                    scanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            if (isProcessing) return@addOnSuccessListener
                                            for (barcode in barcodes) {
                                                barcode.rawValue?.let { code ->
                                                    isProcessing = true
                                                    onCodeScanned(code)
                                                    showScanner = false
                                                    onTriggerReset()
                                                    return@addOnSuccessListener
                                                }
                                            }
                                        }
                                        .addOnCompleteListener { imageProxy.close() }
                                } else {
                                    imageProxy.close()
                                }
                            }

                            val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
                            } catch (e: Exception) {
                                android.util.Log.e("CircleKeep", "Use case binding failed", e)
                            }
                        }, executor)
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                IconButton(
                    onClick = { 
                        showScanner = false
                        onCancel()
                        onTriggerReset()
                    },
                    modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
                }
                
                Text(
                    "Scan QR Code",
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

@Composable
actual fun BannerAdView() {
    BannerAd()
}

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(enabled = enabled, onBack = onBack)
}

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
            val parts = value.split("/")
            if (parts.size == 3) {
                val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                cal.set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt())
                cal.timeInMillis
            } else System.currentTimeMillis()
        } catch (_: Exception) {
            System.currentTimeMillis()
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
                        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                        cal.timeInMillis = it
                        
                        val year = cal.get(java.util.Calendar.YEAR)
                        val month = cal.get(java.util.Calendar.MONTH)
                        val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
                        
                        val resultCal = java.util.Calendar.getInstance()
                        resultCal.set(year, month, day)
                        
                        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                        onValueChange(sdf.format(resultCal.time))
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
    val context = LocalContext.current
    
    val createLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri ->
            uri?.let {
                if (dataToSave != null) {
                    try {
                        context.contentResolver.openOutputStream(it)?.use { output ->
                            output.write(dataToSave.toByteArray())
                        }
                        onFilePicked("Success")
                    } catch (e: Exception) {
                        onFilePicked("Error: ${e.message}")
                    }
                } else {
                    onFilePicked(it.toString())
                }
            }
            onTriggerReset()
        }
    )
    
    val openLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.openInputStream(it)?.use { input ->
                    onFilePicked(input.bufferedReader().use { it.readText() } )
                }
            }
            onTriggerReset()
        }
    )

    if (trigger) {
        SideEffect {
            if (mode == FilePickerMode.Create) {
                val sdf = java.text.SimpleDateFormat("yyyyMMdd_HHmm", java.util.Locale.getDefault())
                val timestamp = sdf.format(java.util.Date())
                createLauncher.launch("CircleKeep_Backup_$timestamp.json")
            } else {
                openLauncher.launch(arrayOf("*/*"))
            }
        }
    }
}
