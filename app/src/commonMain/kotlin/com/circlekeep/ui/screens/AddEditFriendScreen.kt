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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.key.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import coil3.compose.SubcomposeAsyncImage
import com.circlekeep.*
import androidx.compose.ui.platform.testTag
import com.circlekeep.data.Child
import com.circlekeep.data.Friend
import com.circlekeep.getPlatform
import com.circlekeep.ui.components.ChildItemEdit
import com.circlekeep.ui.components.PartnerSectionEdit
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

data class PickedContact(
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val cellPhone: String,
    val officePhone: String,
    val email: String,
    val workEmail: String,
    val address: String,
    val companyName: String,
    val notes: String,
    val dateOfBirth: String,
    val anniversaryDate: String
)

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
    val platformUI = LocalPlatformUI.current
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
    var pendingContact by remember { mutableStateOf<PickedContact?>(null) }
    var showImportTargetDialog by remember { mutableStateOf(false) }
    var showChildPickerForImport by remember { mutableStateOf(false) }

    val children = remember { mutableStateListOf<Child>().apply { addAll(initialChildren) } }

    var contactPickerTrigger by remember { mutableStateOf(false) }

    ContactPicker(
        trigger = contactPickerTrigger,
        onTriggerReset = { contactPickerTrigger = false },
        onContactPicked = { f, m, l, cp, op, e, we, a, cn, n, dob, anniv ->
            pendingContact = PickedContact(f, m, l, cp, op, e, we, a, cn, n, dob, anniv)
            showImportTargetDialog = true
        },
        onCancel = { contactPickerTrigger = false }
    )



    val isEmailValid = email.isBlank() || email.contains("@") // Simplified for KMP
    
    val isDobValid = platform.isDayValidForMonth(birthDay, birthMonth)
    val isAnniversaryValid = platform.isDayValidForMonth(anniversaryDay, anniversaryMonth)
    val isPartnerDobValid = platform.isDayValidForMonth(partnerBirthDay, partnerBirthMonth)
    
    val isFormValid = isEmailValid && isDobValid && isAnniversaryValid && isPartnerDobValid

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
    val focusManager = LocalFocusManager.current
    
    val modifierWithTabHandler = Modifier.fillMaxWidth().onPreviewKeyEvent { 
        if (it.key == Key.Tab && it.type == KeyEventType.KeyDown) {
            focusManager.moveFocus(if (it.isShiftPressed) FocusDirection.Previous else FocusDirection.Next)
            true
        } else {
            false
        }
    }
    
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
                                    workEmail = workEmail,
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
                                
                                if (friendCount >= 2 && !isPaid) {
                                    platformUI.showInterstitialAd {
                                        isSaving = true
                                        scope.launch {
                                            for (i in 5 downTo 1) {
                                                saveDelaySeconds = i
                                                delay(1.seconds)
                                            }
                                            onSave(friend, children.toList())
                                        }
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
                    .fillMaxSize()
                    .testTag("MainList"),
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
                        modifier = modifierWithTabHandler,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        )
                    )
                    OutlinedTextField(
                        value = middleName, 
                        onValueChange = { middleName = it }, 
                        label = { Text("Middle Name") }, 
                        modifier = modifierWithTabHandler,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        )
                    )
                    OutlinedTextField(
                        value = lastName, 
                        onValueChange = { lastName = it }, 
                        label = { Text("Last Name") }, 
                        modifier = modifierWithTabHandler,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        )
                    )
                    OutlinedTextField(
                        value = cellPhone, 
                        onValueChange = { cellPhone = it }, 
                        label = { Text("Cell Phone") }, 
                        modifier = modifierWithTabHandler,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        )
                    )
                    OutlinedTextField(
                        value = officePhone, 
                        onValueChange = { officePhone = it }, 
                        label = { Text("Office Phone") }, 
                        modifier = modifierWithTabHandler,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        )
                    )
                    OutlinedTextField(
                        value = address, 
                        onValueChange = { address = it }, 
                        label = { Text("Address") }, 
                        modifier = modifierWithTabHandler,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        )
                    )
                    
                    DatePickerField(
                        value = dateOfBirth,
                        onValueChange = { 
                            dateOfBirth = it
                            if (it.isBlank()) {
                                birthDay = ""
                                birthMonth = ""
                            } else {
                                try {
                                    platform.parseDateToDayMonth(it)?.let { (d, m) ->
                                        birthDay = d
                                        birthMonth = m
                                    }
                                } catch (_: Exception) {}
                            }
                        },
                        label = buildString {
                            append("DOB")
                            try {
                                platform.calculateAge(dateOfBirth)?.let { append(" ($it yrs)") }
                            } catch (_: Exception) {}
                        },
                        modifier = modifierWithTabHandler
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = birthDay, 
                            onValueChange = { birthDay = it }, 
                            label = { Text("Day") }, 
                            modifier = Modifier.weight(0.4f).onPreviewKeyEvent { 
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
                                try {
                                    platform.parseDateToDayMonth(it)?.let { (d, m) ->
                                        anniversaryDay = d
                                        anniversaryMonth = m
                                    }
                                } catch (_: Exception) {}
                            }
                        },
                        label = "Marriage Date",
                        modifier = modifierWithTabHandler
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = anniversaryDay, 
                            onValueChange = { anniversaryDay = it }, 
                            label = { Text("Day") }, 
                            modifier = Modifier.weight(0.4f).onPreviewKeyEvent { 
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
                            supportingText = { if (!isAnniversaryValid) Text("Invalid day") }
                        )
                        com.circlekeep.ui.components.MonthDropdown(value = anniversaryMonth, onValueChange = { anniversaryMonth = it }, modifier = Modifier.weight(0.6f))
                    }

                    Box(modifier = Modifier.fillMaxWidth().clickable { showGroupDialog = true }) {
                        OutlinedTextField(
                            value = selectedGroups.joinToString(", "),
                            onValueChange = { },
                            label = { Text("Groups") },
                            modifier = modifierWithTabHandler,
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
                            modifier = modifierWithTabHandler,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            )
                        )
                        OutlinedTextField(
                            value = collegeSchoolName, 
                            onValueChange = { collegeSchoolName = it }, 
                            label = { Text("College Name") },
                            modifier = modifierWithTabHandler,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            )
                        )
                        OutlinedTextField(
                            value = siblings, 
                            onValueChange = { siblings = it }, 
                            label = { Text("Siblings") }, 
                            modifier = modifierWithTabHandler,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            )
                        )
                        OutlinedTextField(
                            value = email, 
                            onValueChange = { email = it }, 
                            label = { Text("Email") }, 
                            modifier = modifierWithTabHandler, 
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            isError = !isEmailValid,
                            supportingText = { if (!isEmailValid) Text("Invalid email format") }
                        )
                        OutlinedTextField(
                            value = workEmail, 
                            onValueChange = { workEmail = it }, 
                            label = { Text("Work Email") }, 
                            modifier = modifierWithTabHandler, 
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            )
                        )
                        OutlinedTextField(
                            value = notes, 
                            onValueChange = { notes = it }, 
                            label = { Text("Notes") }, 
                            modifier = modifierWithTabHandler.heightIn(max = 200.dp).verticalScroll(rememberScrollState()),
                            minLines = 3,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Next
                            )
                        )
                        OutlinedTextField(
                            value = petName,
                            onValueChange = { petName = it },
                            label = { Text("Pet Name") },
                            modifier = modifierWithTabHandler,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            )
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
                        modifier = modifierWithTabHandler,
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
                        partnerEmail = partnerEmail,
                        onPartnerEmailChange = { partnerEmail = it },
                        partnerWorkEmail = partnerWorkEmail,
                        onPartnerWorkEmailChange = { partnerWorkEmail = it },
                        partnerDateOfBirth = partnerDateOfBirth,
                        onPartnerDateOfBirthChange = {
                            partnerDateOfBirth = it
                            try {
                                platform.parseDateToDayMonth(it)?.let { (d, m) ->
                                    partnerBirthDay = d
                                    partnerBirthMonth = m
                                }
                            } catch (_: Exception) {}
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
                        modifier = modifierWithTabHandler,
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
                            progress = { (5f - saveDelaySeconds.toFloat()) / 5f },
                            modifier = Modifier.size(80.dp),
                            strokeWidth = 8.dp
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("Saving in $saveDelaySeconds seconds...", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Purchase Pro version to skip ads and saving delay!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
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

    if (showImportTargetDialog && pendingContact != null) {
        AlertDialog(
            onDismissRequest = { showImportTargetDialog = false; pendingContact = null },
            title = { Text("Import Contact To...") },
            text = {
                Column {
                    Text("Choose where to import this contact's details.")
                }
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            pendingContact?.let { p ->
                                firstName = p.firstName; middleName = p.middleName; lastName = p.lastName
                                cellPhone = p.cellPhone; officePhone = p.officePhone; email = p.email; workEmail = p.workEmail
                                address = p.address; companyName = p.companyName; notes = p.notes
                                dateOfBirth = p.dateOfBirth; anniversaryDate = p.anniversaryDate
                                try {
                                    platform.parseDateToDayMonth(p.dateOfBirth)?.let { (d, m) -> birthDay = d; birthMonth = m }
                                    platform.parseDateToDayMonth(p.anniversaryDate)?.let { (d, m) -> anniversaryDay = d; anniversaryMonth = m }
                                } catch (_: Exception) {}
                            }
                            showImportTargetDialog = false; pendingContact = null
                        }
                    ) {
                        Icon(Icons.Rounded.Person, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Main Friend Section")
                    }
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            pendingContact?.let { p ->
                                partnerFirstName = p.firstName; partnerMiddleName = p.middleName; partnerLastName = p.lastName
                                partnerPhone = p.cellPhone; partnerEmail = p.email; partnerWorkEmail = p.workEmail
                                partnerCompanyName = p.companyName; partnerDateOfBirth = p.dateOfBirth
                                try {
                                    platform.parseDateToDayMonth(p.dateOfBirth)?.let { (d, m) -> partnerBirthDay = d; partnerBirthMonth = m }
                                } catch (_: Exception) {}
                            }
                            showImportTargetDialog = false; pendingContact = null
                        }
                    ) {
                        Icon(Icons.Rounded.Favorite, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Partner Section")
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            pendingContact?.let { p ->
                                children.add(Child(
                                    friendId = initialFriend?.id ?: 0L,
                                    firstName = p.firstName,
                                    middleName = p.middleName,
                                    lastName = p.lastName,
                                    phoneNumber = p.cellPhone,
                                    email = p.email,
                                    workEmail = p.workEmail,
                                    dateOfBirth = p.dateOfBirth,
                                    notes = p.notes,
                                    collegeSchoolName = p.companyName
                                ))
                                focusNewChildTrigger = true
                            }
                            showImportTargetDialog = false; pendingContact = null
                        }
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("New Child Record")
                    }
                    if (children.isNotEmpty()) {
                        TextButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                showImportTargetDialog = false
                                showChildPickerForImport = true
                            }
                        ) {
                            Icon(Icons.Rounded.Edit, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Update Existing Child...")
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportTargetDialog = false; pendingContact = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showChildPickerForImport && pendingContact != null) {
        AlertDialog(
            onDismissRequest = { showChildPickerForImport = false; pendingContact = null },
            title = { Text("Select Child to Update") },
            text = {
                LazyColumn {
                    items(children.size) { index ->
                        val child = children[index]
                        val name = "${child.firstName} ${child.lastName}".trim().ifBlank { "Child ${index + 1}" }
                        ListItem(
                            headlineContent = { Text(name) },
                            modifier = Modifier.clickable {
                                pendingContact?.let { p ->
                                    children[index] = child.copy(
                                        firstName = if (child.firstName.isBlank()) p.firstName else child.firstName,
                                        middleName = if (child.middleName.isBlank()) p.middleName else child.middleName,
                                        lastName = if (child.lastName.isBlank()) p.lastName else child.lastName,
                                        phoneNumber = if (child.phoneNumber.isBlank()) p.cellPhone else child.phoneNumber,
                                        email = if (child.email.isBlank()) p.email else child.email,
                                        workEmail = if (child.workEmail.isBlank()) p.workEmail else child.workEmail,
                                        dateOfBirth = if (child.dateOfBirth.isBlank()) p.dateOfBirth else child.dateOfBirth,
                                        notes = if (child.notes.isBlank()) p.notes else child.notes,
                                        collegeSchoolName = if (child.collegeSchoolName.isBlank()) p.companyName else child.collegeSchoolName
                                    )
                                }
                                showChildPickerForImport = false
                                pendingContact = null
                            }
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showChildPickerForImport = false; pendingContact = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
