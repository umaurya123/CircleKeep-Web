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
import androidx.compose.ui.input.key.*
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import com.circlekeep.getPlatform
import com.circlekeep.DatePickerField
import com.circlekeep.ui.theme.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerSectionEdit(
    modifier: Modifier = Modifier,
    partnerType: String,
    onPartnerTypeChange: (String) -> Unit,
    partnerFirstName: String,
    onPartnerFirstNameChange: (String) -> Unit,
    partnerMiddleName: String,
    onPartnerMiddleNameChange: (String) -> Unit,
    partnerLastName: String,
    onPartnerLastNameChange: (String) -> Unit,
    partnerNickname: String,
    onPartnerNicknameChange: (String) -> Unit,
    partnerPhone: String,
    onPartnerPhoneChange: (String) -> Unit,
    partnerEmail: String,
    onPartnerEmailChange: (String) -> Unit,
    partnerWorkEmail: String,
    onPartnerWorkEmailChange: (String) -> Unit,
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
    val strings = LocalAppStrings.current
    var showMore by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
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
                            Text(strings.selectImage)
                        }
                        if (partnerImageUri != null) {
                            TextButton(onClick = { onPartnerImageUriChange(null) }) {
                                Text(strings.removeImage, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            PartnerTypeDropdown(value = partnerType, onValueChange = onPartnerTypeChange)
            OutlinedTextField(
                value = partnerFirstName,
                onValueChange = onPartnerFirstNameChange,
                label = { Text(strings.firstName) },
                modifier = modifier,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )
            OutlinedTextField(
                value = partnerMiddleName,
                onValueChange = onPartnerMiddleNameChange,
                label = { Text(strings.middleName) },
                modifier = modifier,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )
            OutlinedTextField(
                value = partnerLastName,
                onValueChange = onPartnerLastNameChange,
                label = { Text(strings.lastName) },
                modifier = modifier,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )
            OutlinedTextField(
                value = partnerNickname,
                onValueChange = onPartnerNicknameChange,
                label = { Text(strings.nickname) },
                modifier = modifier,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )
            OutlinedTextField(
                value = partnerPhone,
                onValueChange = onPartnerPhoneChange,
                label = { Text(strings.cellPhone) },
                modifier = modifier,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                )
            )
            OutlinedTextField(
                value = partnerEmail,
                onValueChange = onPartnerEmailChange,
                label = { Text(strings.email) },
                modifier = modifier,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )
            OutlinedTextField(
                value = partnerWorkEmail,
                onValueChange = onPartnerWorkEmailChange,
                label = { Text(strings.workEmail) },
                modifier = modifier,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            DatePickerField(
                value = partnerDateOfBirth,
                onValueChange = {
                    onPartnerDateOfBirthChange(it)
                    if (it.isBlank()) {
                        onPartnerBirthDayChange("")
                        onPartnerBirthMonthChange("")
                    } else {
                        try {
                            platform.parseDateToDayMonth(it)?.let { (d, m) ->
                                onPartnerBirthDayChange(d)
                                onPartnerBirthMonthChange(m)
                            }
                        } catch (_: Exception) {}
                    }
                },
                label = strings.partnerDob,
                modifier = modifier
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = partnerBirthDay,
                    onValueChange = onPartnerBirthDayChange,
                    label = { Text(strings.day) },
                    modifier = modifier.weight(0.4f).onPreviewKeyEvent { 
                        if (it.key == Key.Tab && it.type == KeyEventType.KeyDown) {
                            focusManager.moveFocus(if (it.isShiftPressed) FocusDirection.Previous else FocusDirection.Next)
                            true
                        } else false
                    }, 
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    isError = !isPartnerDobValid,
                    supportingText = { if (!isPartnerDobValid) Text("Invalid day") }
                )
                MonthDropdown(value = partnerBirthMonth, onValueChange = onPartnerBirthMonthChange, modifier = Modifier.weight(0.6f))
            }

            if (showMore) {
                OutlinedTextField(
                    value = partnerSiblings,
                    onValueChange = onPartnerSiblingsChange,
                    label = { Text(strings.siblings) },
                    modifier = modifier,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )
                OutlinedTextField(
                    value = partnerCompanyName,
                    onValueChange = onPartnerCompanyNameChange,
                    label = { Text(strings.companyName) },
                    modifier = modifier,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )
                OutlinedTextField(
                    value = partnerCollegeSchoolName,
                    onValueChange = onPartnerCollegeSchoolNameChange,
                    label = { Text(strings.collegeName) },
                    modifier = modifier,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    )
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
                Text(if (showMore) strings.showLess else strings.showMore)
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
    val strings = LocalAppStrings.current
    var expanded by remember { mutableStateOf(false) }
    val months = strings.months
    
    val monthName = if (value.toIntOrNull() in 1..12) months[value.toInt() - 1] else value

    Box(modifier = modifier) {
        OutlinedTextField(
            value = monthName,
            onValueChange = { },
            label = { Text(strings.month) },
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
    val strings = LocalAppStrings.current
    var expanded by remember { mutableStateOf(false) }
    val types = strings.partnerTypes

    Box(modifier = modifier) {
        val displayText = types[value] ?: value
        OutlinedTextField(
            value = displayText,
            onValueChange = { },
            label = { Text(strings.partner) },
            placeholder = { Text(types[""] ?: "Select Type") },
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
            types.forEach { (key, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onValueChange(key)
                        expanded = false
                    }
                )
            }
        }
    }
}
