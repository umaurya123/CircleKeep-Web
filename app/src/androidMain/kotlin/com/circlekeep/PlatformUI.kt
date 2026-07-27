@file:JvmName("PlatformUIAndroid")
package com.circlekeep

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.circlekeep.ui.BannerAd
import com.yalantis.ucrop.UCrop
import java.io.File

class AndroidPlatformUI(private val context: android.content.Context) : PlatformUI {
    override fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
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

    override fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun exitApp() {
        (context as? Activity)?.finish()
    }
}

@Composable
actual fun rememberPlatformUI(): PlatformUI {
    val context = LocalContext.current
    return remember(context) { AndroidPlatformUI(context) }
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
                                    null
                                )?.use { pc ->
                                    val numIdx = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                    val typeIdx = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
                                    while (pc.moveToNext()) {
                                        val number = if (numIdx >= 0) pc.getString(numIdx) ?: "" else ""
                                        val type = if (typeIdx >= 0) pc.getInt(typeIdx) else -1
                                        if (number.isNotBlank()) {
                                            when (type) {
                                                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> importedCellPhone = number
                                                ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> importedOfficePhone = number
                                                else -> if (importedCellPhone.isBlank()) importedCellPhone = number
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
                val destinationUri = File(context.cacheDir, "crop_${System.currentTimeMillis()}.jpg").toUri()
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
