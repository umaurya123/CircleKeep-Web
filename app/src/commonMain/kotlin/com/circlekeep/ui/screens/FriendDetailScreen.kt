package com.circlekeep.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import com.circlekeep.LocalPlatformUI
import com.circlekeep.data.Friend
import com.circlekeep.data.FriendWithChildren
import com.circlekeep.getPlatform
import com.circlekeep.ui.components.DetailRow
import com.circlekeep.ui.theme.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendDetailScreen(
    friendWithChildren: FriendWithChildren?,
    onEditClick: (Long, Long?) -> Unit,
    onDeleteClick: (Friend) -> Unit,
    onTogglePin: (Friend) -> Unit,
    onConvertPartnerClick: (FriendWithChildren) -> Unit,
    onBackClick: () -> Unit
) {
    val strings = LocalAppStrings.current
    val platformUI = LocalPlatformUI.current
    val platform = getPlatform()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.appName) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.cancel)
                    }
                },
                actions = {
                    if (friendWithChildren != null) {
                        IconButton(onClick = { 
                            onTogglePin(friendWithChildren.friend)
                        }) {
                            Icon(Icons.Rounded.PushPin, contentDescription = "Pin", tint = if (friendWithChildren.friend.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        var showQrDialog by remember { mutableStateOf(false) }
                        IconButton(onClick = { showQrDialog = true }) {
                            Icon(Icons.Rounded.QrCode, contentDescription = strings.shareContact)
                        }
                        if (showQrDialog) {
                            val qrContent = buildString {
                                append("CIRCLEKEEP:1.0\n")
                                append("FN:${friendWithChildren.friend.firstName}\n")
                                append("MN:${friendWithChildren.friend.middleName}\n")
                                append("LN:${friendWithChildren.friend.lastName}\n")
                                append("NN:${friendWithChildren.friend.nickname}\n")
                                append("TEL:${friendWithChildren.friend.cellPhone}\n")
                                append("EML:${friendWithChildren.friend.email}\n")
                                append("ADR:${friendWithChildren.friend.address}\n")
                                append("GRP:${friendWithChildren.friend.groups.joinToString(",")}\n")
                                append("DOB:${friendWithChildren.friend.dateOfBirth}\n")
                                append("ANN:${friendWithChildren.friend.anniversaryDate}\n")
                                append("NTS:${friendWithChildren.friend.notes}\n")
                            }
                            val qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${platformUI.encodeUrl(qrContent)}"
                            
                            AlertDialog(
                                onDismissRequest = { showQrDialog = false },
                                title = { Text(strings.shareContact) },
                                text = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        AsyncImage(
                                            model = qrUrl,
                                            contentDescription = "QR Code",
                                            modifier = Modifier.size(200.dp)
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(strings.scanToAdd, style = MaterialTheme.typography.labelMedium)
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { showQrDialog = false }) {
                                        Text(strings.close)
                                    }
                                }
                            )
                        }
                        IconButton(onClick = { 
                            onEditClick(friendWithChildren.friend.id, null)
                        }) {
                            Icon(Icons.Rounded.Edit, contentDescription = strings.editContact)
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Rounded.Delete, contentDescription = strings.delete)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (friendWithChildren == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(80.dp).clip(CircleShape),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            val name = "${friendWithChildren.friend.firstName} ${friendWithChildren.friend.middleName} ${friendWithChildren.friend.lastName}"
                            if (!friendWithChildren.friend.imageUri.isNullOrBlank() && friendWithChildren.friend.imageUri != "null") {
                                SubcomposeAsyncImage(
                                    model = friendWithChildren.friend.imageUri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    error = { com.circlekeep.ui.components.PlaceholderAvatar(name = name) }
                                )
                            } else {
                                com.circlekeep.ui.components.PlaceholderAvatar(name = name)
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = buildString {
                                    append(friendWithChildren.friend.firstName)
                                    if (friendWithChildren.friend.nickname.isNotBlank()) append(" '${friendWithChildren.friend.nickname}'")
                                    if (friendWithChildren.friend.middleName.isNotBlank()) append(" ${friendWithChildren.friend.middleName}")
                                    if (friendWithChildren.friend.lastName.isNotBlank()) append(" ${friendWithChildren.friend.lastName}")
                                },
                                style = MaterialTheme.typography.headlineMedium
                            )
                            if (friendWithChildren.friend.companyName.isNotBlank()) {
                                Text(
                                    text = friendWithChildren.friend.companyName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (friendWithChildren.friend.collegeSchoolName.isNotBlank()) {
                                Text(
                                    text = friendWithChildren.friend.collegeSchoolName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            if (friendWithChildren.friend.cellPhone.isNotBlank()) {
                                DetailRow(Icons.Rounded.Phone, friendWithChildren.friend.cellPhone, onClick = {
                                    platformUI.dialPhone(friendWithChildren.friend.cellPhone)
                                })
                            }
                        }
                    }
                }

                item {
                    if (friendWithChildren.friend.secondaryImageUri != null) {
                        var showFullScreen by remember { mutableStateOf(false) }
                        
                        Card(modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(strings.memoryPhoto, style = MaterialTheme.typography.labelMedium)
                                Spacer(Modifier.height(4.dp))
                                AsyncImage(
                                    model = friendWithChildren.friend.secondaryImageUri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(MaterialTheme.shapes.small)
                                        .clickable { showFullScreen = true },
                                    contentScale = ContentScale.Crop
                                )
                                TextButton(onClick = { showFullScreen = true }) {
                                    Text(strings.viewFull)
                                }
                            }
                        }

                        if (showFullScreen) {
                            AlertDialog(
                                onDismissRequest = { showFullScreen = false },
                                text = {
                                    AsyncImage(
                                        model = friendWithChildren.friend.secondaryImageUri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                                        contentScale = ContentScale.Fit
                                    )
                                },
                                confirmButton = {
                                    TextButton(onClick = { showFullScreen = false }) {
                                        Text(strings.close)
                                    }
                                }
                            )
                        }
                    }

                    DetailRow(Icons.Rounded.LocationOn, friendWithChildren.friend.address, onClick = {
                        platformUI.openMap(friendWithChildren.friend.address)
                    })
                    DetailRow(Icons.Rounded.Work, friendWithChildren.friend.officePhone, onClick = {
                        platformUI.dialPhone(friendWithChildren.friend.officePhone)
                    })
                    DetailRow(Icons.Rounded.Email, friendWithChildren.friend.email, onClick = {
                        platformUI.sendEmail(friendWithChildren.friend.email)
                    })
                    if (friendWithChildren.friend.workEmail.isNotBlank()) {
                        DetailRow(Icons.Rounded.Email, friendWithChildren.friend.workEmail, label = strings.workEmail, onClick = {
                            platformUI.sendEmail(friendWithChildren.friend.workEmail)
                        })
                    }
                    DetailRow(Icons.Rounded.Cake, platform.formatDisplayDate(friendWithChildren.friend.dateOfBirth), label = buildString {
                        append(strings.dob)
                        platform.calculateAge(friendWithChildren.friend.dateOfBirth)?.let { append(" ($it yrs)") }
                    })
                    if (friendWithChildren.friend.dateOfBirth.isBlank() && friendWithChildren.friend.birthDay.isNotBlank() && friendWithChildren.friend.birthMonth.isNotBlank()) {
                        DetailRow(
                            icon = Icons.Rounded.Cake,
                            text = platform.formatPartialDate(friendWithChildren.friend.birthDay, friendWithChildren.friend.birthMonth),
                            label = strings.birthday
                        )
                    }
                    DetailRow(Icons.Rounded.Favorite, platform.formatDisplayDate(friendWithChildren.friend.anniversaryDate), label = buildString {
                        append(strings.marriageAnniversary)
                        platform.calculateAge(friendWithChildren.friend.anniversaryDate)?.let { append(" ($it yrs)") }
                    })
                    if (friendWithChildren.friend.anniversaryDate.isBlank() && friendWithChildren.friend.anniversaryDay.isNotBlank() && friendWithChildren.friend.anniversaryMonth.isNotBlank()) {
                        DetailRow(
                            icon = Icons.Rounded.Favorite,
                            text = platform.formatPartialDate(friendWithChildren.friend.anniversaryDay, friendWithChildren.friend.anniversaryMonth),
                            label = strings.marriageAnniversary
                        )
                    }
                    DetailRow(Icons.Rounded.People, friendWithChildren.friend.siblings, label = strings.siblings)
                    DetailRow(Icons.Rounded.Group, friendWithChildren.friend.groups.joinToString(", "))
                    
                    if (friendWithChildren.friend.petName.isNotBlank()) {
                        DetailRow(Icons.Rounded.Pets, friendWithChildren.friend.petName, label = strings.petName)
                        if (friendWithChildren.friend.petImageUri != null) {
                            Card(modifier = Modifier.padding(start = 44.dp, top = 4.dp, bottom = 8.dp).size(100.dp)) {
                                AsyncImage(
                                    model = friendWithChildren.friend.petImageUri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    DetailRow(Icons.AutoMirrored.Rounded.Notes, friendWithChildren.friend.notes)

                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "${strings.created}: ${platform.formatTimestamp(friendWithChildren.friend.createdAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${strings.lastModified}: ${platform.formatTimestamp(friendWithChildren.friend.lastModifiedAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                if (friendWithChildren.friend.partnerFirstName.isNotBlank()) {
                    item {
                        Spacer(Modifier.height(16.dp))
                        val partnerTypeKey = friendWithChildren.friend.partnerType
                        val partnerHeader = if (partnerTypeKey.isBlank()) strings.partner else (strings.partnerTypes[partnerTypeKey] ?: partnerTypeKey)
                        
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(partnerHeader, style = MaterialTheme.typography.titleLarge)
                            Row {
                                IconButton(onClick = { onConvertPartnerClick(friendWithChildren) }) {
                                    Icon(Icons.Rounded.PersonAdd, contentDescription = strings.addContact, tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { onEditClick(friendWithChildren.friend.id, -1L) }) {
                                    Icon(Icons.Rounded.Edit, contentDescription = strings.editContact)
                                }
                            }
                        }
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        modifier = Modifier.size(60.dp).clip(CircleShape),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        val name = "${friendWithChildren.friend.partnerFirstName} ${friendWithChildren.friend.partnerMiddleName} ${friendWithChildren.friend.partnerLastName}"
                                        if (!friendWithChildren.friend.partnerImageUri.isNullOrBlank() && friendWithChildren.friend.partnerImageUri != "null") {
                                            SubcomposeAsyncImage(
                                                model = friendWithChildren.friend.partnerImageUri,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop,
                                                error = { com.circlekeep.ui.components.PlaceholderAvatar(name = name) }
                                            )
                                        } else {
                                            com.circlekeep.ui.components.PlaceholderAvatar(name = name)
                                        }
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column {
                                        val partnerDisplayName = buildString {
                                            append(friendWithChildren.friend.partnerFirstName)
                                            if (friendWithChildren.friend.partnerNickname.isNotBlank()) append(" '${friendWithChildren.friend.partnerNickname}'")
                                            if (friendWithChildren.friend.partnerMiddleName.isNotBlank()) append(" ${friendWithChildren.friend.partnerMiddleName}")
                                            if (friendWithChildren.friend.partnerLastName.isNotBlank()) append(" ${friendWithChildren.friend.partnerLastName}")
                                        }
                                        Text(
                                            text = partnerDisplayName,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                        if (friendWithChildren.friend.partnerPhone.isNotBlank()) {
                                            DetailRow(Icons.Rounded.Phone, friendWithChildren.friend.partnerPhone, onClick = {
                                                platformUI.dialPhone(friendWithChildren.friend.partnerPhone)
                                            })
                                        }
                                    }
                                }
                                if (friendWithChildren.friend.partnerCompanyName.isNotBlank()) {
                                    DetailRow(Icons.Rounded.Business, friendWithChildren.friend.partnerCompanyName, label = strings.companyName)
                                }
                                if (friendWithChildren.friend.partnerCollegeSchoolName.isNotBlank()) {
                                    DetailRow(Icons.Rounded.School, friendWithChildren.friend.partnerCollegeSchoolName, label = strings.collegeName)
                                }
                                if (friendWithChildren.friend.partnerEmail.isNotBlank()) {
                                    DetailRow(Icons.Rounded.Email, friendWithChildren.friend.partnerEmail, onClick = {
                                        platformUI.sendEmail(friendWithChildren.friend.partnerEmail)
                                    })
                                }
                                if (friendWithChildren.friend.partnerWorkEmail.isNotBlank()) {
                                    DetailRow(Icons.Rounded.Email, friendWithChildren.friend.partnerWorkEmail, label = strings.workEmail, onClick = {
                                        platformUI.sendEmail(friendWithChildren.friend.partnerWorkEmail)
                                    })
                                }
                                if (friendWithChildren.friend.partnerDateOfBirth.isNotBlank()) {
                                    DetailRow(
                                        icon = Icons.Rounded.Cake,
                                        text = platform.formatDisplayDate(friendWithChildren.friend.partnerDateOfBirth),
                                        label = buildString {
                                            append(strings.dob)
                                            platform.calculateAge(friendWithChildren.friend.partnerDateOfBirth)?.let { append(" ($it yrs)") }
                                        }
                                    )
                                } else if (friendWithChildren.friend.partnerBirthDay.isNotBlank() && friendWithChildren.friend.partnerBirthMonth.isNotBlank()) {
                                    DetailRow(
                                        icon = Icons.Rounded.Cake,
                                        text = platform.formatPartialDate(friendWithChildren.friend.partnerBirthDay, friendWithChildren.friend.partnerBirthMonth),
                                        label = strings.birthday
                                    )
                                }
                                if (friendWithChildren.friend.partnerSiblings.isNotBlank()) {
                                    DetailRow(Icons.Rounded.People, friendWithChildren.friend.partnerSiblings, label = strings.siblings)
                                }
                            }
                        }
                    }
                }
                
                if (friendWithChildren.children.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(16.dp))
                        Text(strings.children, style = MaterialTheme.typography.titleLarge)
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    }
                    items(friendWithChildren.children) { child ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        modifier = Modifier.size(40.dp).clip(CircleShape),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        val name = "${child.firstName} ${child.middleName} ${child.lastName}"
                                        if (!child.imageUri.isNullOrBlank() && child.imageUri != "null") {
                                            SubcomposeAsyncImage(
                                                model = child.imageUri,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop,
                                                error = { com.circlekeep.ui.components.PlaceholderAvatar(name = name) }
                                            )
                                        } else {
                                            com.circlekeep.ui.components.PlaceholderAvatar(name = name)
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        val childDisplayName = buildString {
                                            append(child.firstName)
                                            if (child.nickname.isNotBlank()) append(" '${child.nickname}'")
                                            if (child.middleName.isNotBlank()) append(" ${child.middleName}")
                                            if (child.lastName.isNotBlank()) append(" ${child.lastName}")
                                        }
                                        Text(
                                            text = childDisplayName,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        if (child.phoneNumber.isNotBlank()) {
                                            DetailRow(Icons.Rounded.Phone, child.phoneNumber, onClick = {
                                                platformUI.dialPhone(child.phoneNumber)
                                            })
                                        }
                                    }
                                    IconButton(onClick = { onEditClick(friendWithChildren.friend.id, child.id) }) {
                                        Icon(Icons.Rounded.Edit, contentDescription = strings.editContact)
                                    }
                                }
                                if (child.collegeSchoolName.isNotBlank()) {
                                    DetailRow(Icons.Rounded.School, child.collegeSchoolName)
                                }
                                if (child.email.isNotBlank()) {
                                    DetailRow(Icons.Rounded.Email, child.email, onClick = {
                                        platformUI.sendEmail(child.email)
                                    })
                                }
                                if (child.workEmail.isNotBlank()) {
                                    DetailRow(Icons.Rounded.Email, child.workEmail, label = strings.workEmail, onClick = {
                                        platformUI.sendEmail(child.workEmail)
                                    })
                                }
                                if (child.dateOfBirth.isNotBlank()) {
                                    DetailRow(Icons.Rounded.Cake, platform.formatDisplayDate(child.dateOfBirth), label = buildString {
                                        append(strings.dob)
                                        platform.calculateAge(child.dateOfBirth)?.let { append(" ($it yrs)") }
                                    })
                                } else {
                                    if (child.birthDay.isNotBlank() && child.birthMonth.isNotBlank()) {
                                        DetailRow(
                                            icon = Icons.Rounded.Cake,
                                            text = platform.formatPartialDate(child.birthDay, child.birthMonth),
                                            label = strings.birthday
                                        )
                                    }
                                    if (child.age != null) {
                                        DetailRow(
                                            icon = Icons.Rounded.Cake,
                                            text = "${child.age} ${if (child.ageUnit.contains("Month")) strings.monthUnit else strings.yearUnit}",
                                            label = strings.age
                                        )
                                    }
                                }
                                
                                if (child.siblings.isNotBlank()) {
                                    DetailRow(Icons.Rounded.People, child.siblings, label = strings.siblings)
                                }
                                
                                if (child.petName.isNotBlank()) {
                                    DetailRow(Icons.Rounded.Pets, child.petName, label = strings.petName)
                                    if (child.petImageUri != null) {
                                        Card(modifier = Modifier.padding(start = 44.dp, top = 4.dp, bottom = 8.dp).size(80.dp)) {
                                            AsyncImage(model = child.petImageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        }
                                    }
                                }
                                
                                if (child.notes.isNotBlank()) {
                                    DetailRow(Icons.AutoMirrored.Rounded.Notes, child.notes)
                                }
                                
                                if (child.partnerFirstName.isNotBlank()) {
                                    Spacer(Modifier.height(8.dp))
                                    if (child.anniversaryDate.isNotBlank()) {
                                        DetailRow(Icons.Rounded.Favorite, platform.formatDisplayDate(child.anniversaryDate), label = strings.marriageAnniversary)
                                    } else if (child.anniversaryDay.isNotBlank() && child.anniversaryMonth.isNotBlank()) {
                                        DetailRow(
                                            icon = Icons.Rounded.Favorite,
                                            text = platform.formatPartialDate(child.anniversaryDay, child.anniversaryMonth),
                                            label = strings.marriageAnniversary
                                        )
                                    }
                                    
                                    val partnerTypeKey = child.partnerType
                                    val partnerLabel = if (partnerTypeKey.isBlank()) strings.partner else (strings.partnerTypes[partnerTypeKey] ?: partnerTypeKey)
                                    
                                    val childPartnerDisplayName = buildString {
                                        append(child.partnerFirstName)
                                        if (child.partnerNickname.isNotBlank()) append(" '${child.partnerNickname}'")
                                        if (child.partnerMiddleName.isNotBlank()) append(" ${child.partnerMiddleName}")
                                        if (child.partnerLastName.isNotBlank()) append(" ${child.partnerLastName}")
                                    }
                                    Text(
                                        text = partnerLabel,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = childPartnerDisplayName,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    if (child.partnerPhone.isNotBlank()) {
                                        DetailRow(Icons.Rounded.Phone, child.partnerPhone, onClick = {
                                            platformUI.dialPhone(child.partnerPhone)
                                        })
                                    }
                                    if (child.partnerCompanyName.isNotBlank()) {
                                        DetailRow(Icons.Rounded.Business, child.partnerCompanyName, label = strings.companyName)
                                    }
                                    if (child.partnerCollegeSchoolName.isNotBlank()) {
                                        DetailRow(Icons.Rounded.School, child.partnerCollegeSchoolName, label = strings.collegeName)
                                    }
                                    if (child.partnerEmail.isNotBlank()) {
                                        DetailRow(Icons.Rounded.Email, child.partnerEmail, onClick = {
                                            platformUI.sendEmail(child.partnerEmail)
                                        })
                                    }
                                    if (child.partnerWorkEmail.isNotBlank()) {
                                        DetailRow(Icons.Rounded.Email, child.partnerWorkEmail, label = strings.workEmail, onClick = {
                                            platformUI.sendEmail(child.partnerWorkEmail)
                                        })
                                    }
                                    if (child.partnerDateOfBirth.isNotBlank()) {
                                        DetailRow(
                                            icon = Icons.Rounded.Cake,
                                            text = platform.formatDisplayDate(child.partnerDateOfBirth),
                                            label = buildString {
                                                append(strings.dob)
                                                platform.calculateAge(child.partnerDateOfBirth)?.let { append(" ($it yrs)") }
                                            }
                                        )
                                    } else if (child.partnerBirthDay.isNotBlank() && child.partnerBirthMonth.isNotBlank()) {
                                        DetailRow(
                                            icon = Icons.Rounded.Cake,
                                            text = platform.formatPartialDate(child.partnerBirthDay, child.partnerBirthMonth),
                                            label = strings.birthday
                                        )
                                    }
                                    if (child.partnerSiblings.isNotBlank()) {
                                        DetailRow(Icons.Rounded.People, child.partnerSiblings, label = strings.siblings)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog && friendWithChildren != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(strings.delete) },
            text = { Text("Are you sure you want to delete this friend?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick(friendWithChildren.friend)
                        showDeleteDialog = false
                    }
                ) {
                    Text(strings.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}
