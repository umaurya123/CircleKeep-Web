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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.circlekeep.DatePickerField
import com.circlekeep.data.Child
import com.circlekeep.getPlatform

@Composable
fun ChildItemEdit(
    child: Child,
    initiallyExpanded: Boolean = false,
    shouldAutoFocus: Boolean = false,
    onChildChange: (Child) -> Unit,
    onDelete: () -> Unit,
    onSelectImage: () -> Unit,
    onSelectPartnerImage: () -> Unit,
    onSelectPetImage: () -> Unit
) {
    var showMore by remember { mutableStateOf(initiallyExpanded) }
    val focusRequester = remember { FocusRequester() }
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
                        if (!child.imageUri.isNullOrBlank()) {
                            AsyncImage(
                                model = child.imageUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            PlaceholderAvatar(name = "${child.firstName} ${child.middleName} ${child.lastName}")
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Child", fontWeight = FontWeight.Bold)
                        Row {
                            TextButton(onClick = { onSelectImage() }) {
                                Text("Select Image", style = MaterialTheme.typography.labelSmall)
                            }
                            if (child.imageUri != null) {
                                TextButton(onClick = { onChildChange(child.copy(imageUri = null)) }) {
                                    Text("Remove Image", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Rounded.Delete, contentDescription = null)
                }
            }
            OutlinedTextField(
                value = child.firstName, 
                onValueChange = { onChildChange(child.copy(firstName = it)) }, 
                label = { Text("First Name") }, 
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { if (!it.isFocused) onChildChange(child.copy(firstName = child.firstName.trim())) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            OutlinedTextField(
                value = child.middleName, 
                onValueChange = { onChildChange(child.copy(middleName = it)) }, 
                label = { Text("Middle Name") }, 
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (!it.isFocused) onChildChange(child.copy(middleName = child.middleName.trim())) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            OutlinedTextField(
                value = child.lastName, 
                onValueChange = { onChildChange(child.copy(lastName = it)) }, 
                label = { Text("Last Name") }, 
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (!it.isFocused) onChildChange(child.copy(lastName = child.lastName.trim())) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            OutlinedTextField(
                value = child.phoneNumber, 
                onValueChange = { onChildChange(child.copy(phoneNumber = it)) }, 
                label = { Text("Cell Phone") }, 
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (!it.isFocused) onChildChange(child.copy(phoneNumber = child.phoneNumber.trim())) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            
            if (showMore) {
                OutlinedTextField(
                    value = child.collegeSchoolName, 
                    onValueChange = { onChildChange(child.copy(collegeSchoolName = it)) },
                    label = { Text("College Name") }, 
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (!it.isFocused) onChildChange(child.copy(collegeSchoolName = child.collegeSchoolName.trim())) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                DatePickerField(
                    value = child.dateOfBirth,
                    onValueChange = { 
                        var newChild = child.copy(dateOfBirth = it)
                        if (it.isBlank()) {
                            newChild = newChild.copy(birthDay = "", birthMonth = "")
                        } else {
                            platform.parseDateToDayMonth(it)?.let { (d, m) ->
                                newChild = newChild.copy(birthDay = d, birthMonth = m)
                            }
                        }
                        onChildChange(newChild)
                    },
                    label = "DOB"
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = child.birthDay, 
                        onValueChange = { onChildChange(child.copy(birthDay = it)) }, 
                        label = { Text("Day") }, 
                        modifier = Modifier.weight(0.4f), 
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = !isDobValid,
                        supportingText = { if (!isDobValid) Text("Invalid day") }
                    )
                    MonthDropdown(value = child.birthMonth, onValueChange = { onChildChange(child.copy(birthMonth = it)) }, modifier = Modifier.weight(0.6f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = child.age?.toString() ?: "",
                        onValueChange = { onChildChange(child.copy(age = it.toIntOrNull())) },
                        label = { Text("Age") },
                        modifier = Modifier.weight(0.4f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    
                    var showAgeUnitMenu by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(0.6f)) {
                        OutlinedTextField(
                            value = child.ageUnit,
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
                                text = { Text("Year(s)") },
                                onClick = {
                                    onChildChange(child.copy(ageUnit = "Year(s)"))
                                    showAgeUnitMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Month(s)") },
                                onClick = {
                                    onChildChange(child.copy(ageUnit = "Month(s)"))
                                    showAgeUnitMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text("Child's Partner", style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        modifier = Modifier.size(40.dp).clip(CircleShape).clickable { onSelectPartnerImage() },
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        if (!child.partnerImageUri.isNullOrBlank()) {
                            AsyncImage(model = child.partnerImageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            PlaceholderAvatar(name = "${child.partnerFirstName} ${child.partnerMiddleName} ${child.partnerLastName}")
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row {
                            TextButton(onClick = { onSelectPartnerImage() }) {
                                Text("Select Image", style = MaterialTheme.typography.labelSmall)
                            }
                            if (child.partnerImageUri != null) {
                                TextButton(onClick = { onChildChange(child.copy(partnerImageUri = null)) }) {
                                    Text("Remove Image", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        PartnerTypeDropdown(value = child.partnerType, onValueChange = { onChildChange(child.copy(partnerType = it)) })
                        OutlinedTextField(
                            value = child.partnerFirstName, 
                            onValueChange = { onChildChange(child.copy(partnerFirstName = it)) }, 
                            label = { Text("First Name") }, 
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                        )
                        OutlinedTextField(
                            value = child.partnerMiddleName, 
                            onValueChange = { onChildChange(child.copy(partnerMiddleName = it)) }, 
                            label = { Text("Middle Name") }, 
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                        )
                        OutlinedTextField(
                            value = child.partnerLastName, 
                            onValueChange = { onChildChange(child.copy(partnerLastName = it)) }, 
                            label = { Text("Last Name") }, 
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                        )
                        OutlinedTextField(
                            value = child.partnerPhone, 
                            onValueChange = { onChildChange(child.copy(partnerPhone = it)) }, 
                            label = { Text("Partner Phone") }, 
                            modifier = Modifier.fillMaxWidth(), 
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        OutlinedTextField(
                            value = child.partnerSiblings, 
                            onValueChange = { onChildChange(child.copy(partnerSiblings = it)) }, 
                            label = { Text("Siblings") }, 
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
                            platform.parseDateToDayMonth(it)?.let { (d, m) ->
                                newChild = newChild.copy(partnerBirthDay = d, partnerBirthMonth = m)
                            }
                        }
                        onChildChange(newChild)
                    },
                    label = "Partner DOB"
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = child.partnerBirthDay, 
                        onValueChange = { onChildChange(child.copy(partnerBirthDay = it)) }, 
                        label = { Text("Day") }, 
                        modifier = Modifier.weight(0.4f), 
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = !isPartnerDobValid,
                        supportingText = { if (!isPartnerDobValid) Text("Invalid day") }
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
                            platform.parseDateToDayMonth(it)?.let { (d, m) ->
                                newChild = newChild.copy(anniversaryDay = d, anniversaryMonth = m)
                            }
                        }
                        onChildChange(newChild)
                    },
                    label = "Marriage Date"
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = child.anniversaryDay, 
                        onValueChange = { onChildChange(child.copy(anniversaryDay = it)) }, 
                        label = { Text("Day") }, 
                        modifier = Modifier.weight(0.4f), 
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = !isAnniversaryValid,
                        supportingText = { if (!isAnniversaryValid) Text("Invalid day") }
                    )
                    MonthDropdown(value = child.anniversaryMonth, onValueChange = { onChildChange(child.copy(anniversaryMonth = it)) }, modifier = Modifier.weight(0.6f))
                }

                OutlinedTextField(
                    value = child.notes, 
                    onValueChange = { onChildChange(child.copy(notes = it)) }, 
                    label = { Text("Notes") }, 
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = child.petName,
                    onValueChange = { onChildChange(child.copy(petName = it)) },
                    label = { Text("Pet Name") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
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
                            Text("Select Pet Image", style = MaterialTheme.typography.labelSmall)
                        }
                        if (child.petImageUri != null) {
                            TextButton(onClick = { onChildChange(child.copy(petImageUri = null)) }) {
                                Text("Remove Image", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
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
                Text(if (showMore) "Show Less" else "Show More")
            }
        }
    }
}
