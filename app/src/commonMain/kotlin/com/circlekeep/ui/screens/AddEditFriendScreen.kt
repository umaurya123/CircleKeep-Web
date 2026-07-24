package com.circlekeep.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.circlekeep.*
import com.circlekeep.data.Child
import com.circlekeep.data.Friend
import com.circlekeep.getPlatform
import com.circlekeep.ui.components.ChildItemEdit
import com.circlekeep.ui.components.PartnerSectionEdit
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFriendScreen(
    initialFriend: Friend? = null,
    initialChildren: List<Child> = emptyList(),
    scrollToChildId: Long? = null,
    availableGroups: List<String> = emptyList(),
    friendCount: Int = 0,
    isPaid: Boolean = false,
    onSave: (Friend, List<Child>) -> Unit,
    onCancel: () -> Unit
) {
    val platform = getPlatform()
    val scope = rememberCoroutineScope()
    var saveDelaySeconds by remember { mutableIntStateOf(0) }
    var isSaving by remember { mutableStateOf(false) }

    var firstName by remember { mutableStateOf(initialFriend?.firstName ?: "") }
    var middleName by remember { mutableStateOf(initialFriend?.middleName ?: "") }
    var lastName by remember { mutableStateOf(initialFriend?.lastName ?: "") }
    var address by remember { mutableStateOf(initialFriend?.address ?: "") }
    var cellPhone by remember { mutableStateOf(initialFriend?.cellPhone ?: "") }
    var officePhone by remember { mutableStateOf(initialFriend?.officePhone ?: "") }
    var email by remember { mutableStateOf(initialFriend?.email ?: "") }
    var workEmail by remember { mutableStateOf(initialFriend?.workEmail ?: "") }
    
    var partnerFirstName by remember { mutableStateOf(initialFriend?.partnerFirstName ?: "") }
    var partnerMiddleName by remember { mutableStateOf(initialFriend?.partnerMiddleName ?: "") }
    var partnerLastName by remember { mutableStateOf(initialFriend?.partnerLastName ?: "") }
    var partnerPhone by remember { mutableStateOf(initialFriend?.partnerPhone ?: "") }
    var partnerEmail by remember { mutableStateOf(initialFriend?.partnerEmail ?: "") }
    var partnerWorkEmail by remember { mutableStateOf(initialFriend?.partnerWorkEmail ?: "") }
    var partnerType by remember { mutableStateOf(initialFriend?.partnerType ?: "") }
    var partnerSiblings by remember { mutableStateOf(initialFriend?.partnerSiblings ?: "") }
    var partnerCompanyName by remember { mutableStateOf(initialFriend?.partnerCompanyName ?: "") }
    var partnerCollegeSchoolName by remember { mutableStateOf(initialFriend?.partnerCollegeSchoolName ?: "") }
    
    var companyName by remember { mutableStateOf(initialFriend?.companyName ?: "") }
    var collegeSchoolName by remember { mutableStateOf(initialFriend?.collegeSchoolName ?: "") }
    var siblings by remember { mutableStateOf(initialFriend?.siblings ?: "") }
    
    var dateOfBirth by remember { mutableStateOf(initialFriend?.dateOfBirth ?: "") }
    var birthDay by remember { mutableStateOf(initialFriend?.birthDay ?: "") }
    var birthMonth by remember { mutableStateOf(initialFriend?.birthMonth ?: "") }
    
    var partnerDateOfBirth by remember { mutableStateOf(initialFriend?.partnerDateOfBirth ?: "") }
    var partnerBirthDay by remember { mutableStateOf(initialFriend?.partnerBirthDay ?: "") }
    var partnerBirthMonth by remember { mutableStateOf(initialFriend?.partnerBirthMonth ?: "") }
    
    var anniversaryDate by remember { mutableStateOf(initialFriend?.anniversaryDate ?: "") }
    var anniversaryDay by remember { mutableStateOf(initialFriend?.anniversaryDay ?: "") }
    var anniversaryMonth by remember { mutableStateOf(initialFriend?.anniversaryMonth ?: "") }

    var notes by remember { mutableStateOf(initialFriend?.notes ?: "") }
    var imageUri by remember { mutableStateOf(initialFriend?.imageUri) }
    var partnerImageUri by remember { mutableStateOf(initialFriend?.partnerImageUri) }
    var petName by remember { mutableStateOf(initialFriend?.petName ?: "") }
    var petImageUri by remember { mutableStateOf(initialFriend?.petImageUri) }
    
    var focusNewChildTrigger by remember { mutableStateOf(false) }
    var showMore by remember { mutableStateOf(false) }

    val isEmailValid = email.isBlank() || email.contains("@") // Simplified for KMP
    
    val isDobValid = platform.isDayValidForMonth(birthDay, birthMonth)
    val isAnniversaryValid = platform.isDayValidForMonth(anniversaryDay, anniversaryMonth)
    val isPartnerDobValid = platform.isDayValidForMonth(partnerBirthDay, partnerBirthMonth)
    
    val isFormValid = isEmailValid && isDobValid && isAnniversaryValid && isPartnerDobValid
    
    val selectedGroups = remember { mutableStateListOf<String>().apply { 
        if (initialFriend != null) addAll(initialFriend.groups) else add("Friend")
    } }
    
    LaunchedEffect(initialFriend?.groups) {
        if (initialFriend != null) {
            selectedGroups.clear()
            selectedGroups.addAll(initialFriend.groups)
        }
    }
    
    var showGroupDialog by remember { mutableStateOf(false) }
    val children = remember { mutableStateListOf<Child>().apply { addAll(initialChildren) } }

    var contactPickerTrigger by remember { mutableStateOf(false) }
    ContactPicker(
        trigger = contactPickerTrigger,
        onTriggerReset = { contactPickerTrigger = false },
        onContactPicked = { f, m, l, cp, op, e, we, a, cn, n, dob, anniv ->
            // Use simplified picker logic
            firstName = f; middleName = m; lastName = l
            cellPhone = cp; officePhone = op; email = e; workEmail = we
            address = a; companyName = cn; notes = n
            dateOfBirth = dob
            anniversaryDate = anniv
            platform.parseDateToDayMonth(dob)?.let { (d, mon) -> birthDay = d; birthMonth = mon }
            platform.parseDateToDayMonth(anniv)?.let { (d, mon) -> anniversaryDay = d; anniversaryMonth = mon }
        },
        onCancel = { contactPickerTrigger = false }
    )

    var mainImagePickerTrigger by remember { mutableStateOf(false) }
    ImagePicker(
        trigger = mainImagePickerTrigger,
        onTriggerReset = { mainImagePickerTrigger = false },
        onImagePicked = { imageUri = it }
    )

    var partnerImagePickerTrigger by remember { mutableStateOf(false) }
    ImagePicker(
        trigger = partnerImagePickerTrigger,
        onTriggerReset = { partnerImagePickerTrigger = false },
        onImagePicked = { partnerImageUri = it }
    )

    var petImagePickerTrigger by remember { mutableStateOf(false) }
    ImagePicker(
        trigger = petImagePickerTrigger,
        onTriggerReset = { petImagePickerTrigger = false },
        onImagePicked = { petImageUri = it }
    )

    val listState = rememberLazyListState()
    
    LaunchedEffect(scrollToChildId) {
        if (scrollToChildId != null) {
            if (scrollToChildId == -1L) {
                listState.animateScrollToItem(2)
            } else {
                val index = children.indexOfFirst { it.id == scrollToChildId }
                if (index != -1) {
                    listState.animateScrollToItem(index + 4)
                }
            }
        }
    }

    LaunchedEffect(focusNewChildTrigger) {
        if (focusNewChildTrigger && children.isNotEmpty()) {
            listState.animateScrollToItem(children.size + 4)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initialFriend == null) "Add Friend" else "Edit Friend") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Rounded.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    IconButton(onClick = { contactPickerTrigger = true }) {
                        Icon(Icons.Rounded.PersonAdd, contentDescription = "Import")
                    }
                    IconButton(
                        onClick = {
                            if (isFormValid && !isSaving) {
                                val friend = Friend(
                                    id = initialFriend?.id ?: 0L,
                                    firstName = firstName,
                                    middleName = middleName,
                                    lastName = lastName,
                                    address = address,
                                    cellPhone = cellPhone,
                                    officePhone = officePhone,
                                    email = email,
                                    partnerFirstName = partnerFirstName,
                                    partnerMiddleName = partnerMiddleName,
                                    partnerLastName = partnerLastName,
                                    partnerPhone = partnerPhone,
                                    partnerType = partnerType,
                                    partnerImageUri = partnerImageUri,
                                    partnerSiblings = partnerSiblings,
                                    partnerCompanyName = partnerCompanyName,
                                    partnerCollegeSchoolName = partnerCollegeSchoolName,
                                    dateOfBirth = dateOfBirth,
                                    birthDay = birthDay,
                                    birthMonth = birthMonth,
                                    partnerDateOfBirth = partnerDateOfBirth,
                                    partnerBirthDay = partnerBirthDay,
                                    partnerBirthMonth = partnerBirthMonth,
                                    anniversaryDate = anniversaryDate,
                                    anniversaryDay = anniversaryDay,
                                    anniversaryMonth = anniversaryMonth,
                                    companyName = companyName,
                                    collegeSchoolName = collegeSchoolName,
                                    siblings = siblings,
                                    groups = selectedGroups.toList(),
                                    isFavorite = initialFriend?.isFavorite ?: false,
                                    isPinned = initialFriend?.isPinned ?: false,
                                    imageUri = imageUri,
                                    petName = petName,
                                    petImageUri = petImageUri,
                                    notes = notes
                                )
                                
                                if (friendCount > 15 && !isPaid) {
                                    isSaving = true
                                    scope.launch {
                                        for (i in 10 downTo 1) {
                                            saveDelaySeconds = i
                                            delay(1.seconds)
                                        }
                                        onSave(friend, children.toList())
                                    }
                                } else {
                                    onSave(friend, children.toList())
                                }
                            }
                        },
                        enabled = isFormValid && !isSaving
                    ) {
                        if (isSaving) {
                            Text("$saveDelaySeconds", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Rounded.Check, contentDescription = "Save")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .clickable { mainImagePickerTrigger = true },
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                if (!imageUri.isNullOrBlank() && imageUri != "null") {
                                    SubcomposeAsyncImage(
                                        model = imageUri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        error = { com.circlekeep.ui.components.PlaceholderAvatar(name = "$firstName $middleName $lastName") }
                                    )
                                } else {
                                    com.circlekeep.ui.components.PlaceholderAvatar(name = "$firstName $middleName $lastName")
                                }
                            }
                            Row {
                                TextButton(onClick = { mainImagePickerTrigger = true }) {
                                    Text("Select Image")
                                }
                                if (imageUri != null) {
                                    TextButton(onClick = { imageUri = null }) {
                                        Text("Remove Image", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = firstName, 
                        onValueChange = { firstName = it }, 
                        label = { Text("First Name") }, 
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )
                    OutlinedTextField(
                        value = middleName, 
                        onValueChange = { middleName = it }, 
                        label = { Text("Middle Name") }, 
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )
                    OutlinedTextField(
                        value = lastName, 
                        onValueChange = { lastName = it }, 
                        label = { Text("Last Name") }, 
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )
                    OutlinedTextField(
                        value = cellPhone, 
                        onValueChange = { cellPhone = it }, 
                        label = { Text("Cell Phone") }, 
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    OutlinedTextField(
                        value = officePhone, 
                        onValueChange = { officePhone = it }, 
                        label = { Text("Office Phone") }, 
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    OutlinedTextField(
                        value = address, 
                        onValueChange = { address = it }, 
                        label = { Text("Address") }, 
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )
                    
                    DatePickerField(
                        value = dateOfBirth,
                        onValueChange = { 
                            dateOfBirth = it
                            if (it.isBlank()) {
                                birthDay = ""
                                birthMonth = ""
                            } else {
                                platform.parseDateToDayMonth(it)?.let { (d, m) ->
                                    birthDay = d
                                    birthMonth = m
                                }
                            }
                        },
                        label = buildString {
                            append("DOB")
                            platform.calculateAge(dateOfBirth)?.let { append(" ($it yrs)") }
                        }
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = birthDay, 
                            onValueChange = { birthDay = it }, 
                            label = { Text("Day") }, 
                            modifier = Modifier.weight(0.4f), 
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = !isDobValid,
                            supportingText = { if (!isDobValid) Text("Invalid day") }
                        )
                        com.circlekeep.ui.components.MonthDropdown(value = birthMonth, onValueChange = { birthMonth = it }, modifier = Modifier.weight(0.6f))
                    }

                    DatePickerField(
                        value = anniversaryDate,
                        onValueChange = { 
                            anniversaryDate = it
                            if (it.isBlank()) {
                                anniversaryDay = ""
                                anniversaryMonth = ""
                            } else {
                                platform.parseDateToDayMonth(it)?.let { (d, m) ->
                                    anniversaryDay = d
                                    anniversaryMonth = m
                                }
                            }
                        },
                        label = "Marriage Date"
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = anniversaryDay, 
                            onValueChange = { anniversaryDay = it }, 
                            label = { Text("Day") }, 
                            modifier = Modifier.weight(0.4f), 
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = !isAnniversaryValid,
                            supportingText = { if (!isAnniversaryValid) Text("Invalid day") }
                        )
                        com.circlekeep.ui.components.MonthDropdown(value = anniversaryMonth, onValueChange = { anniversaryMonth = it }, modifier = Modifier.weight(0.6f))
                    }

                    Box(modifier = Modifier.fillMaxWidth().clickable { showGroupDialog = true }) {
                        OutlinedTextField(
                            value = selectedGroups.joinToString(", "),
                            onValueChange = { },
                            label = { Text("Groups") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            enabled = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.outline,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            ),
                            trailingIcon = { Icon(Icons.Rounded.ArrowDropDown, contentDescription = null) }
                        )
                        Box(modifier = Modifier.matchParentSize().background(Color.Transparent).clickable { showGroupDialog = true })
                    }
                }
                
                item {
                    if (showMore) {
                        OutlinedTextField(
                            value = companyName, 
                            onValueChange = { companyName = it }, 
                            label = { Text("Company Name") }, 
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                        )
                        OutlinedTextField(
                            value = collegeSchoolName, 
                            onValueChange = { collegeSchoolName = it }, 
                            label = { Text("College Name") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                        )
                        OutlinedTextField(
                            value = siblings, 
                            onValueChange = { siblings = it }, 
                            label = { Text("Siblings") }, 
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                        )
                        OutlinedTextField(
                            value = email, 
                            onValueChange = { email = it }, 
                            label = { Text("Email") }, 
                            modifier = Modifier.fillMaxWidth(), 
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            isError = !isEmailValid,
                            supportingText = { if (!isEmailValid) Text("Invalid email format") }
                        )
                        OutlinedTextField(
                            value = workEmail, 
                            onValueChange = { workEmail = it }, 
                            label = { Text("Work Email") }, 
                            modifier = Modifier.fillMaxWidth(), 
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        OutlinedTextField(
                            value = notes, 
                            onValueChange = { notes = it }, 
                            label = { Text("Notes") }, 
                            modifier = Modifier.fillMaxWidth(), 
                            minLines = 2,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                        )
                        OutlinedTextField(
                            value = petName,
                            onValueChange = { petName = it },
                            label = { Text("Pet Name") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .clickable { petImagePickerTrigger = true },
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                if (!petImageUri.isNullOrBlank() && petImageUri != "null") {
                                    SubcomposeAsyncImage(
                                        model = petImageUri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        error = { Icon(Icons.Rounded.Pets, contentDescription = null, modifier = Modifier.padding(12.dp)) }
                                    )
                                } else {
                                    Icon(Icons.Rounded.Pets, contentDescription = null, modifier = Modifier.padding(12.dp))
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Row {
                                TextButton(onClick = { petImagePickerTrigger = true }) {
                                    Text("Select Pet Image")
                                }
                                if (petImageUri != null) {
                                    TextButton(onClick = { petImageUri = null }) {
                                        Text("Remove Image", color = MaterialTheme.colorScheme.error)
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

                item {
                    Text("Partner", style = MaterialTheme.typography.titleLarge)
                    PartnerSectionEdit(
                        partnerType = partnerType,
                        onPartnerTypeChange = { partnerType = it },
                        partnerFirstName = partnerFirstName,
                        onPartnerFirstNameChange = { partnerFirstName = it },
                        partnerMiddleName = partnerMiddleName,
                        onPartnerMiddleNameChange = { partnerMiddleName = it },
                        partnerLastName = partnerLastName,
                        onPartnerLastNameChange = { partnerLastName = it },
                        partnerPhone = partnerPhone,
                        onPartnerPhoneChange = { partnerPhone = it },
                        partnerDateOfBirth = partnerDateOfBirth,
                        onPartnerDateOfBirthChange = {
                            partnerDateOfBirth = it
                            platform.parseDateToDayMonth(it)?.let { (d, m) ->
                                partnerBirthDay = d
                                partnerBirthMonth = m
                            }
                        },
                        partnerBirthDay = partnerBirthDay,
                        onPartnerBirthDayChange = { partnerBirthDay = it },
                        partnerBirthMonth = partnerBirthMonth,
                        onPartnerBirthMonthChange = { partnerBirthMonth = it },
                        isPartnerDobValid = isPartnerDobValid,
                        partnerSiblings = partnerSiblings,
                        onPartnerSiblingsChange = { partnerSiblings = it },
                        partnerCompanyName = partnerCompanyName,
                        onPartnerCompanyNameChange = { partnerCompanyName = it },
                        partnerCollegeSchoolName = partnerCollegeSchoolName,
                        onPartnerCollegeSchoolNameChange = { partnerCollegeSchoolName = it },
                        partnerImageUri = partnerImageUri,
                        onPartnerImageUriChange = { partnerImageUri = it },
                        onSelectImage = { partnerImagePickerTrigger = true }
                    )
                }

                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Children", style = MaterialTheme.typography.titleLarge)
                        TextButton(onClick = {
                            val lastChildEmpty = children.lastOrNull()?.let { 
                                it.firstName.isBlank() && it.lastName.isBlank() 
                            } ?: false
                            
                            if (children.isEmpty() || !lastChildEmpty) {
                                children.add(Child(friendId = initialFriend?.id ?: 0L, firstName = "", lastName = "", collegeSchoolName = ""))
                                focusNewChildTrigger = true
                            }
                        }) {
                            Icon(Icons.Rounded.Add, contentDescription = null)
                            Text("Add Child")
                        }
                    }
                }
                
                items(children.size) { index ->
                    var childImagePickerTrigger by remember { mutableStateOf(false) }
                    var childPartnerImagePickerTrigger by remember { mutableStateOf(false) }
                    var childPetImagePickerTrigger by remember { mutableStateOf(false) }

                    ImagePicker(trigger = childImagePickerTrigger, onTriggerReset = { childImagePickerTrigger = false }, onImagePicked = { children[index] = children[index].copy(imageUri = it) })
                    ImagePicker(trigger = childPartnerImagePickerTrigger, onTriggerReset = { childPartnerImagePickerTrigger = false }, onImagePicked = { children[index] = children[index].copy(partnerImageUri = it) })
                    ImagePicker(trigger = childPetImagePickerTrigger, onTriggerReset = { childPetImagePickerTrigger = false }, onImagePicked = { children[index] = children[index].copy(petImageUri = it) })

                    ChildItemEdit(
                        child = children[index],
                        initiallyExpanded = children[index].id == scrollToChildId,
                        shouldAutoFocus = index == children.size - 1 && focusNewChildTrigger,
                        onChildChange = { children[index] = it },
                        onDelete = { children.removeAt(index) },
                        onSelectImage = { childImagePickerTrigger = true },
                        onSelectPartnerImage = { childPartnerImagePickerTrigger = true },
                        onSelectPetImage = { childPetImagePickerTrigger = true }
                    )
                    if (index == children.size - 1 && focusNewChildTrigger) {
                        SideEffect { focusNewChildTrigger = false }
                    }
                }
            }
            
            if (isSaving && !isPaid) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { (10f - saveDelaySeconds.toFloat()) / 10f },
                            modifier = Modifier.size(80.dp),
                            strokeWidth = 8.dp
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("Saving in $saveDelaySeconds seconds...", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }

    if (showGroupDialog) {
        val groups = (availableGroups + "General" + "Family" + "Work" + "School" + "Sports").distinct().sorted()
        AlertDialog(
            onDismissRequest = { showGroupDialog = false },
            title = { Text("Select Groups") },
            text = {
                LazyColumn {
                    items(groups) { group ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (selectedGroups.contains(group)) {
                                        selectedGroups.remove(group)
                                    } else {
                                        selectedGroups.add(group)
                                    }
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedGroups.contains(group),
                                onCheckedChange = null
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(group)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGroupDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}
