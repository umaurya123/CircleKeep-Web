package com.circlekeep.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.key.*
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import com.circlekeep.DatePickerField
import com.circlekeep.data.Child
import com.circlekeep.getPlatform
import com.circlekeep.ui.theme.LocalAppStrings

@Composable
fun ChildItemEdit(
    modifier: Modifier = Modifier,
    child: Child,
    initiallyExpanded: Boolean = false,
    shouldAutoFocus: Boolean = false,
    onChildChange: (Child) -> Unit,
    onDelete: () -> Unit,
    onSelectImage: () -> Unit,
    onSelectPartnerImage: () -> Unit,
    onSelectPetImage: () -> Unit
) {
    val strings = LocalAppStrings.current
    var showMore by remember { mutableStateOf(initiallyExpanded) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val platform = getPlatform()

    LaunchedEffect(shouldAutoFocus) {
        if (shouldAutoFocus) {
            focusRequester.requestFocus()
        }
    }
    
    val isDobValid = platform.isDayValidForMonth(child.birthDay, child.birthMonth)
    val isAnniversaryValid = platform.isDayValidForMonth(child.anniversaryDay, child.anniversaryMonth)
    val isPartnerDobValid = platform.isDayValidForMonth(child.partnerBirthDay, child.partnerBirthMonth)

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable { onSelectImage() },
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        val name = "${child.firstName} ${child.middleName} ${child.lastName}"
                        if (!child.imageUri.isNullOrBlank() && child.imageUri != "null") {
                            SubcomposeAsyncImage(
                                model = child.imageUri,
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
                    Column {
                        Text(strings.child, fontWeight = FontWeight.Bold)
                        Row {
                            TextButton(onClick = { onSelectImage() }) {
                                Text(strings.selectImage, style = MaterialTheme.typography.labelSmall)
                            }
                            if (child.imageUri != null) {
                                TextButton(onClick = { onChildChange(child.copy(imageUri = null)) }) {
                                    Text(strings.removeImage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Rounded.Delete, contentDescription = strings.delete)
                }
            }
            OutlinedTextField(
                value = child.firstName, 
                onValueChange = { onChildChange(child.copy(firstName = it)) }, 
                label = { Text(strings.firstName) }, 
                modifier = modifier
                    .focusRequester(focusRequester),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )
            OutlinedTextField(
                value = child.middleName, 
                onValueChange = { onChildChange(child.copy(middleName = it)) }, 
                label = { Text(strings.middleName) }, 
                modifier = modifier,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )
            OutlinedTextField(
                value = child.lastName, 
                onValueChange = { onChildChange(child.copy(lastName = it)) }, 
                label = { Text(strings.lastName) }, 
                modifier = modifier,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )
            OutlinedTextField(
                value = child.nickname, 
                onValueChange = { onChildChange(child.copy(nickname = it)) }, 
                label = { Text(strings.nickname) },
                modifier = modifier,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )
            OutlinedTextField(
                value = child.phoneNumber, 
                onValueChange = { onChildChange(child.copy(phoneNumber = it)) }, 
                label = { Text(strings.cellPhone) }, 
                modifier = modifier,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                )
            )
            
            if (showMore) {
                OutlinedTextField(
                    value = child.collegeSchoolName, 
                    onValueChange = { onChildChange(child.copy(collegeSchoolName = it)) },
                    label = { Text(strings.collegeName) }, 
                    modifier = modifier,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )
                DatePickerField(
                    value = child.dateOfBirth,
                    onValueChange = { 
                        var newChild = child.copy(dateOfBirth = it)
                        if (it.isBlank()) {
                            newChild = newChild.copy(birthDay = "", birthMonth = "")
                        } else {
                            try {
                                platform.parseDateToDayMonth(it)?.let { (d, m) ->
                                    newChild = newChild.copy(birthDay = d, birthMonth = m)
                                }
                            } catch (_: Exception) {}
                        }
                        onChildChange(newChild)
                    },
                    label = strings.dob,
                    modifier = modifier
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = child.birthDay, 
                        onValueChange = { onChildChange(child.copy(birthDay = it)) }, 
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
                        isError = !isDobValid,
                        supportingText = { if (!isDobValid) Text(strings.invalidDay) }
                    )
                    MonthDropdown(value = child.birthMonth, onValueChange = { onChildChange(child.copy(birthMonth = it)) }, modifier = Modifier.weight(0.6f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = child.age?.toString() ?: "",
                        onValueChange = { onChildChange(child.copy(age = it.toIntOrNull())) },
                        label = { Text(strings.age) },
                        modifier = Modifier.weight(0.4f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    
                    var showAgeUnitMenu by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(0.6f)) {
                        val unitLabel = if (child.ageUnit.contains("Month")) strings.monthUnit else strings.yearUnit
                        OutlinedTextField(
                            value = unitLabel,
                            onValueChange = { },
                            label = { },
                            modifier = Modifier.fillMaxWidth().clickable { showAgeUnitMenu = true },
                            enabled = false,
                            readOnly = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                            trailingIcon = { Icon(Icons.Rounded.ArrowDropDown, contentDescription = null) }
                        )
                        DropdownMenu(
                            expanded = showAgeUnitMenu,
                            onDismissRequest = { showAgeUnitMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(strings.yearUnit) },
                                onClick = {
                                    onChildChange(child.copy(ageUnit = "Year(s)"))
                                    showAgeUnitMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(strings.monthUnit) },
                                onClick = {
                                    onChildChange(child.copy(ageUnit = "Month(s)"))
                                    showAgeUnitMenu = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = child.workEmail, 
                    onValueChange = { onChildChange(child.copy(workEmail = it)) }, 
                    label = { Text(strings.workEmail) }, 
                    modifier = modifier, 
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )
                OutlinedTextField(
                    value = child.siblings, 
                    onValueChange = { onChildChange(child.copy(siblings = it)) }, 
                    label = { Text(strings.siblings) }, 
                    modifier = modifier,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )
                OutlinedTextField(
                    value = child.notes, 
                    onValueChange = { onChildChange(child.copy(notes = it)) }, 
                    label = { Text(strings.notes) }, 
                    modifier = modifier
                        .heightIn(max = 200.dp)
                        .verticalScroll(rememberScrollState()),
                    minLines = 3,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = child.petName,
                    onValueChange = { onChildChange(child.copy(petName = it)) },
                    label = { Text(strings.petName) },
                    modifier = modifier,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(48.dp).clip(CircleShape).clickable { onSelectPetImage() },
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        if (child.petImageUri != null) {
                            AsyncImage(model = child.petImageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Icon(Icons.Rounded.Pets, contentDescription = null, modifier = Modifier.padding(8.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Row {
                        TextButton(onClick = { onSelectPetImage() }) {
                            Text(strings.selectImage, style = MaterialTheme.typography.labelSmall)
                        }
                        if (child.petImageUri != null) {
                            TextButton(onClick = { onChildChange(child.copy(petImageUri = null)) }) {
                                Text(strings.removeImage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(strings.childsPartner, style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        modifier = Modifier.size(40.dp).clip(CircleShape).clickable { onSelectPartnerImage() },
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        val name = "${child.partnerFirstName} ${child.partnerMiddleName} ${child.partnerLastName}"
                        if (!child.partnerImageUri.isNullOrBlank() && child.partnerImageUri != "null") {
                            SubcomposeAsyncImage(
                                model = child.partnerImageUri,
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
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row {
                            TextButton(onClick = { onSelectPartnerImage() }) {
                                Text(strings.selectImage, style = MaterialTheme.typography.labelSmall)
                            }
                            if (child.partnerImageUri != null) {
                                TextButton(onClick = { onChildChange(child.copy(partnerImageUri = null)) }) {
                                    Text(strings.removeImage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        PartnerTypeDropdown(value = child.partnerType, onValueChange = { onChildChange(child.copy(partnerType = it)) })
                        OutlinedTextField(
                            value = child.partnerFirstName, 
                            onValueChange = { onChildChange(child.copy(partnerFirstName = it)) }, 
                            label = { Text(strings.firstName) }, 
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                        )
                        OutlinedTextField(
                            value = child.partnerMiddleName, 
                            onValueChange = { onChildChange(child.copy(partnerMiddleName = it)) }, 
                            label = { Text(strings.middleName) }, 
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                        )
                        OutlinedTextField(
                            value = child.partnerLastName, 
                            onValueChange = { onChildChange(child.copy(partnerLastName = it)) }, 
                            label = { Text(strings.lastName) }, 
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                        )
                        OutlinedTextField(
                            value = child.partnerNickname, 
                            onValueChange = { onChildChange(child.copy(partnerNickname = it)) }, 
                            label = { Text(strings.nickname) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                        )
                        OutlinedTextField(
                            value = child.partnerPhone, 
                            onValueChange = { onChildChange(child.copy(partnerPhone = it)) }, 
                            label = { Text(strings.cellPhone) }, 
                            modifier = Modifier.fillMaxWidth(), 
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        OutlinedTextField(
                            value = child.partnerEmail, 
                            onValueChange = { onChildChange(child.copy(partnerEmail = it)) }, 
                            label = { Text(strings.email) }, 
                            modifier = Modifier.fillMaxWidth(), 
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        OutlinedTextField(
                            value = child.partnerWorkEmail, 
                            onValueChange = { onChildChange(child.copy(partnerWorkEmail = it)) }, 
                            label = { Text(strings.workEmail) }, 
                            modifier = Modifier.fillMaxWidth(), 
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        OutlinedTextField(
                            value = child.partnerSiblings, 
                            onValueChange = { onChildChange(child.copy(partnerSiblings = it)) }, 
                            label = { Text(strings.siblings) }, 
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                        )
                        OutlinedTextField(
                            value = child.partnerCompanyName,
                            onValueChange = { onChildChange(child.copy(partnerCompanyName = it)) },
                            label = { Text(strings.companyName) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                        )
                        OutlinedTextField(
                            value = child.partnerCollegeSchoolName,
                            onValueChange = { onChildChange(child.copy(partnerCollegeSchoolName = it)) },
                            label = { Text(strings.collegeName) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                        )
                    }
                }
                DatePickerField(
                    value = child.partnerDateOfBirth,
                    onValueChange = { 
                        var newChild = child.copy(partnerDateOfBirth = it)
                        if (it.isBlank()) {
                            newChild = newChild.copy(partnerBirthDay = "", partnerBirthMonth = "")
                        } else {
                            try {
                                platform.parseDateToDayMonth(it)?.let { (d, m) ->
                                    newChild = newChild.copy(partnerBirthDay = d, partnerBirthMonth = m)
                                }
                            } catch (_: Exception) {}
                        }
                        onChildChange(newChild)
                    },
                    label = strings.partnerDob,
                    modifier = modifier
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = child.partnerBirthDay, 
                        onValueChange = { onChildChange(child.copy(partnerBirthDay = it)) }, 
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
                        supportingText = { if (!isPartnerDobValid) Text(strings.invalidDay) }
                    )
                    MonthDropdown(value = child.partnerBirthMonth, onValueChange = { onChildChange(child.copy(partnerBirthMonth = it)) }, modifier = Modifier.weight(0.6f))
                }
                DatePickerField(
                    value = child.anniversaryDate,
                    onValueChange = { 
                        var newChild = child.copy(anniversaryDate = it)
                        if (it.isBlank()) {
                            newChild = newChild.copy(anniversaryDay = "", anniversaryMonth = "")
                        } else {
                            try {
                                platform.parseDateToDayMonth(it)?.let { (d, m) ->
                                    newChild = newChild.copy(anniversaryDay = d, anniversaryMonth = m)
                                }
                            } catch (_: Exception) {}
                        }
                        onChildChange(newChild)
                    },
                    label = strings.marriageDate,
                    modifier = modifier
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = child.anniversaryDay, 
                        onValueChange = { onChildChange(child.copy(anniversaryDay = it)) }, 
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
                        isError = !isAnniversaryValid,
                        supportingText = { if (!isAnniversaryValid) Text(strings.invalidDay) }
                    )
                    MonthDropdown(value = child.anniversaryMonth, onValueChange = { onChildChange(child.copy(anniversaryMonth = it)) }, modifier = Modifier.weight(0.6f))
                }
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
