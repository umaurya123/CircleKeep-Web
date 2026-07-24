package com.circlekeep.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import com.circlekeep.getPlatform
import com.circlekeep.DatePickerField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerSectionEdit(
    partnerType: String,
    onPartnerTypeChange: (String) -> Unit,
    partnerFirstName: String,
    onPartnerFirstNameChange: (String) -> Unit,
    partnerMiddleName: String,
    onPartnerMiddleNameChange: (String) -> Unit,
    partnerLastName: String,
    onPartnerLastNameChange: (String) -> Unit,
    partnerPhone: String,
    onPartnerPhoneChange: (String) -> Unit,
    partnerDateOfBirth: String,
    onPartnerDateOfBirthChange: (String) -> Unit,
    partnerBirthDay: String,
    onPartnerBirthDayChange: (String) -> Unit,
    partnerBirthMonth: String,
    onPartnerBirthMonthChange: (String) -> Unit,
    isPartnerDobValid: Boolean,
    partnerSiblings: String,
    onPartnerSiblingsChange: (String) -> Unit,
    partnerCompanyName: String,
    onPartnerCompanyNameChange: (String) -> Unit,
    partnerCollegeSchoolName: String,
    onPartnerCollegeSchoolNameChange: (String) -> Unit,
    partnerImageUri: String?,
    onPartnerImageUriChange: (String?) -> Unit,
    onSelectImage: () -> Unit
) {
    var showMore by remember { mutableStateOf(false) }
    val platform = getPlatform()

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .clickable { onSelectImage() },
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    val name = "$partnerFirstName $partnerMiddleName $partnerLastName"
                    if (!partnerImageUri.isNullOrBlank() && partnerImageUri != "null") {
                        SubcomposeAsyncImage(
                            model = partnerImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            error = { PlaceholderAvatar(name = name) }
                        )
                    } else {
                        PlaceholderAvatar(name = name)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row {
                        TextButton(onClick = { onSelectImage() }) {
                            Text("Select Image")
                        }
                        if (partnerImageUri != null) {
                            TextButton(onClick = { onPartnerImageUriChange(null) }) {
                                Text("Remove Image", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            PartnerTypeDropdown(value = partnerType, onValueChange = onPartnerTypeChange)
            OutlinedTextField(
                value = partnerFirstName,
                onValueChange = onPartnerFirstNameChange,
                label = { Text("First Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (!it.isFocused) onPartnerFirstNameChange(partnerFirstName.trim()) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            OutlinedTextField(
                value = partnerMiddleName,
                onValueChange = onPartnerMiddleNameChange,
                label = { Text("Middle Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (!it.isFocused) onPartnerMiddleNameChange(partnerMiddleName.trim()) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            OutlinedTextField(
                value = partnerLastName,
                onValueChange = onPartnerLastNameChange,
                label = { Text("Last Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (!it.isFocused) onPartnerLastNameChange(partnerLastName.trim()) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            OutlinedTextField(
                value = partnerPhone,
                onValueChange = onPartnerPhoneChange,
                label = { Text("Partner Phone") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (!it.isFocused) onPartnerPhoneChange(partnerPhone.trim()) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            DatePickerField(
                value = partnerDateOfBirth,
                onValueChange = {
                    onPartnerDateOfBirthChange(it)
                    if (it.isBlank()) {
                        onPartnerBirthDayChange("")
                        onPartnerBirthMonthChange("")
                    } else {
                        platform.parseDateToDayMonth(it)?.let { (d, m) ->
                            onPartnerBirthDayChange(d)
                            onPartnerBirthMonthChange(m)
                        }
                    }
                },
                label = "Partner DOB"
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = partnerBirthDay,
                    onValueChange = onPartnerBirthDayChange,
                    label = { Text("Day") },
                    modifier = Modifier.weight(0.4f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = !isPartnerDobValid,
                    supportingText = { if (!isPartnerDobValid) Text("Invalid day") }
                )
                MonthDropdown(value = partnerBirthMonth, onValueChange = onPartnerBirthMonthChange, modifier = Modifier.weight(0.6f))
            }

            if (showMore) {
                OutlinedTextField(
                    value = partnerSiblings,
                    onValueChange = onPartnerSiblingsChange,
                    label = { Text("Siblings") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (!it.isFocused) onPartnerSiblingsChange(partnerSiblings.trim()) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                OutlinedTextField(
                    value = partnerCompanyName,
                    onValueChange = onPartnerCompanyNameChange,
                    label = { Text("Company Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (!it.isFocused) onPartnerCompanyNameChange(partnerCompanyName.trim()) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                OutlinedTextField(
                    value = partnerCollegeSchoolName,
                    onValueChange = onPartnerCollegeSchoolNameChange,
                    label = { Text("College Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (!it.isFocused) onPartnerCollegeSchoolNameChange(partnerCollegeSchoolName.trim()) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
            }

            TextButton(
                onClick = { showMore = !showMore },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (showMore) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(if (showMore) "Show Less" else "Show More")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    
    val monthName = if (value.toIntOrNull() in 1..12) months[value.toInt() - 1] else value

    Box(modifier = modifier) {
        OutlinedTextField(
            value = monthName,
            onValueChange = { },
            label = { Text("Month") },
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            enabled = false,
            readOnly = true,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            trailingIcon = {
                Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            months.forEachIndexed { index, month ->
                DropdownMenuItem(
                    text = { Text(month) },
                    onClick = {
                        onValueChange((index + 1).toString())
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerTypeDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val types = listOf("", "Spouse", "Fiance", "Boyfriend", "Girlfriend", "Other")

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { },
            label = { Text("Partner Type") },
            placeholder = { Text("Select Type") },
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            enabled = false,
            readOnly = true,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            trailingIcon = {
                Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            types.forEach { type ->
                val displayType = when (type) {
                    "" -> "None"
                    else -> type
                }
                DropdownMenuItem(
                    text = { Text(displayType) },
                    onClick = {
                        onValueChange(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

