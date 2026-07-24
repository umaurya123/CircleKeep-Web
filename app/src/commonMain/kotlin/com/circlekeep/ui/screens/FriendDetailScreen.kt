package com.circlekeep.ui.screens

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
import com.circlekeep.LocalPlatformUI
import com.circlekeep.data.Friend
import com.circlekeep.data.FriendWithChildren
import com.circlekeep.getPlatform
import com.circlekeep.ui.components.DetailRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendDetailScreen(
    friendWithChildren: FriendWithChildren?,
    onEditClick: (Long, Long?) -> Unit,
    onDeleteClick: (Friend) -> Unit,
    onTogglePin: (Friend) -> Unit,
    onBackClick: () -> Unit
) {
    val platformUI = LocalPlatformUI.current
    val platform = getPlatform()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CircleKeep") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (friendWithChildren != null) {
                        IconButton(onClick = { 
                            onTogglePin(friendWithChildren.friend)
                        }) {
                            Icon(Icons.Rounded.PushPin, contentDescription = "Pin", tint = if (friendWithChildren.friend.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { 
                            onEditClick(friendWithChildren.friend.id, null)
                        }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete")
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
                            if (friendWithChildren.friend.imageUri != null) {
                                AsyncImage(
                                    model = friendWithChildren.friend.imageUri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                com.circlekeep.ui.components.PlaceholderAvatar(
                                    name = friendWithChildren.friend.firstName + " " + friendWithChildren.friend.lastName
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = buildString {
                                    append(friendWithChildren.friend.firstName)
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
                        }
                    }
                }

                item {
                    DetailRow(Icons.Rounded.LocationOn, friendWithChildren.friend.address, onClick = {
                        platformUI.openUrl("geo:0,0?q=${friendWithChildren.friend.address}")
                    })
                    DetailRow(Icons.Rounded.Phone, friendWithChildren.friend.cellPhone, onClick = {
                        platformUI.dialPhone(friendWithChildren.friend.cellPhone)
                    })
                    DetailRow(Icons.Rounded.Work, friendWithChildren.friend.officePhone, onClick = {
                        platformUI.dialPhone(friendWithChildren.friend.officePhone)
                    })
                    DetailRow(Icons.Rounded.Email, friendWithChildren.friend.email, onClick = {
                        platformUI.sendEmail(friendWithChildren.friend.email)
                    })
                    DetailRow(Icons.Rounded.Cake, platform.formatDisplayDate(friendWithChildren.friend.dateOfBirth), label = buildString {
                        append("DOB")
                        platform.calculateAge(friendWithChildren.friend.dateOfBirth)?.let { append(" ($it yrs)") }
                    })
                    if (friendWithChildren.friend.dateOfBirth.isBlank() && friendWithChildren.friend.birthDay.isNotBlank() && friendWithChildren.friend.birthMonth.isNotBlank()) {
                        DetailRow(
                            icon = Icons.Rounded.Cake,
                            text = platform.formatPartialDate(friendWithChildren.friend.birthDay, friendWithChildren.friend.birthMonth),
                            label = "Birthday"
                        )
                    }
                    DetailRow(Icons.Rounded.Favorite, platform.formatDisplayDate(friendWithChildren.friend.anniversaryDate), label = buildString {
                        append("Marriage Date")
                        platform.calculateAge(friendWithChildren.friend.anniversaryDate)?.let { append(" ($it yrs)") }
                    })
                    if (friendWithChildren.friend.anniversaryDate.isBlank() && friendWithChildren.friend.anniversaryDay.isNotBlank() && friendWithChildren.friend.anniversaryMonth.isNotBlank()) {
                        DetailRow(
                            icon = Icons.Rounded.Favorite,
                            text = platform.formatPartialDate(friendWithChildren.friend.anniversaryDay, friendWithChildren.friend.anniversaryMonth),
                            label = "Marriage Day"
                        )
                    }
                    DetailRow(Icons.Rounded.People, friendWithChildren.friend.siblings, label = "Siblings")
                    DetailRow(Icons.Rounded.Group, friendWithChildren.friend.groups.joinToString(", "))
                    
                    if (friendWithChildren.friend.petName.isNotBlank()) {
                        DetailRow(Icons.Rounded.Pets, friendWithChildren.friend.petName, label = "Pet Name")
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
                }

                if (friendWithChildren.friend.partnerFirstName.isNotBlank()) {
                    item {
                        Spacer(Modifier.height(16.dp))
                        val partnerTypeStr = friendWithChildren.friend.partnerType
                        val partnerHeader = partnerTypeStr.ifBlank { "Partner" }
                        
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(partnerHeader, style = MaterialTheme.typography.titleLarge)
                            IconButton(onClick = { onEditClick(friendWithChildren.friend.id, -1L) }) {
                                Icon(Icons.Rounded.Edit, contentDescription = "Edit")
                            }
                        }
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        modifier = Modifier.size(60.dp).clip(CircleShape),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        if (friendWithChildren.friend.partnerImageUri != null) {
                                            AsyncImage(
                                                model = friendWithChildren.friend.partnerImageUri,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            com.circlekeep.ui.components.PlaceholderAvatar(
                                                name = friendWithChildren.friend.partnerFirstName + " " + friendWithChildren.friend.partnerLastName
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column {
                                        val partnerDisplayName = buildString {
                                            append(friendWithChildren.friend.partnerFirstName)
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
                                if (friendWithChildren.friend.partnerDateOfBirth.isNotBlank()) {
                                    DetailRow(
                                        icon = Icons.Rounded.Cake,
                                        text = platform.formatDisplayDate(friendWithChildren.friend.partnerDateOfBirth),
                                        label = buildString {
                                            append("DOB")
                                            platform.calculateAge(friendWithChildren.friend.partnerDateOfBirth)?.let { append(" ($it yrs)") }
                                        }
                                    )
                                } else if (friendWithChildren.friend.partnerBirthDay.isNotBlank() && friendWithChildren.friend.partnerBirthMonth.isNotBlank()) {
                                    DetailRow(
                                        icon = Icons.Rounded.Cake,
                                        text = platform.formatPartialDate(friendWithChildren.friend.partnerBirthDay, friendWithChildren.friend.partnerBirthMonth),
                                        label = "Birthday"
                                    )
                                }
                                if (friendWithChildren.friend.partnerSiblings.isNotBlank()) {
                                    DetailRow(Icons.Rounded.People, friendWithChildren.friend.partnerSiblings, label = "Siblings")
                                }
                            }
                        }
                    }
                }
                
                if (friendWithChildren.children.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(16.dp))
                        Text("Children", style = MaterialTheme.typography.titleLarge)
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
                                        if (child.imageUri != null) {
                                            AsyncImage(
                                                model = child.imageUri,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            com.circlekeep.ui.components.PlaceholderAvatar(
                                                name = child.firstName + " " + child.lastName
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    val childDisplayName = buildString {
                                        append(child.firstName)
                                        if (child.middleName.isNotBlank()) append(" ${child.middleName}")
                                        if (child.lastName.isNotBlank()) append(" ${child.lastName}")
                                    }
                                    Text(
                                        text = childDisplayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = { onEditClick(friendWithChildren.friend.id, child.id) }) {
                                        Icon(Icons.Rounded.Edit, contentDescription = "Edit")
                                    }
                                }
                                if (child.collegeSchoolName.isNotBlank()) {
                                    DetailRow(Icons.Rounded.School, child.collegeSchoolName)
                                }
                                if (child.phoneNumber.isNotBlank()) {
                                    DetailRow(Icons.Rounded.Phone, child.phoneNumber, onClick = {
                                        platformUI.dialPhone(child.phoneNumber)
                                    })
                                }
                                if (child.dateOfBirth.isNotBlank()) {
                                    DetailRow(Icons.Rounded.Cake, platform.formatDisplayDate(child.dateOfBirth), label = buildString {
                                        append("DOB")
                                        platform.calculateAge(child.dateOfBirth)?.let { append(" ($it yrs)") }
                                    })
                                } else {
                                    if (child.birthDay.isNotBlank() && child.birthMonth.isNotBlank()) {
                                        DetailRow(
                                            icon = Icons.Rounded.Cake,
                                            text = platform.formatPartialDate(child.birthDay, child.birthMonth),
                                            label = "Birthday"
                                        )
                                    }
                                    if (child.age != null) {
                                        DetailRow(
                                            icon = Icons.Rounded.Cake,
                                            text = "${child.age} ${child.ageUnit}",
                                            label = "Age"
                                        )
                                    }
                                }
                                
                                if (child.partnerFirstName.isNotBlank()) {
                                    Spacer(Modifier.height(8.dp))
                                    if (child.anniversaryDate.isNotBlank()) {
                                        DetailRow(Icons.Rounded.Favorite, platform.formatDisplayDate(child.anniversaryDate), label = "Marriage Date")
                                    } else if (child.anniversaryDay.isNotBlank() && child.anniversaryMonth.isNotBlank()) {
                                        DetailRow(
                                            icon = Icons.Rounded.Favorite,
                                            text = platform.formatPartialDate(child.anniversaryDay, child.anniversaryMonth),
                                            label = "Marriage Day"
                                        )
                                    }
                                    
                                    val partnerTypeStr = child.partnerType
                                    val partnerLabel = partnerTypeStr.ifBlank { "Partner" }
                                    
                                    val childPartnerDisplayName = buildString {
                                        append(child.partnerFirstName)
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
                                    if (child.partnerDateOfBirth.isNotBlank()) {
                                        DetailRow(
                                            icon = Icons.Rounded.Cake,
                                            text = platform.formatDisplayDate(child.partnerDateOfBirth),
                                            label = buildString {
                                                append("DOB")
                                                platform.calculateAge(child.partnerDateOfBirth)?.let { append(" ($it yrs)") }
                                            }
                                        )
                                    } else if (child.partnerBirthDay.isNotBlank() && child.partnerBirthMonth.isNotBlank()) {
                                        DetailRow(
                                            icon = Icons.Rounded.Cake,
                                            text = platform.formatPartialDate(child.partnerBirthDay, child.partnerBirthMonth),
                                            label = "Birthday"
                                        )
                                    }
                                    if (child.partnerSiblings.isNotBlank()) {
                                        DetailRow(Icons.Rounded.People, child.partnerSiblings, label = "Siblings")
                                    }
                                }
                                
                                if (child.petName.isNotBlank()) {
                                    DetailRow(Icons.Rounded.Pets, child.petName, label = "Pet Name")
                                    if (child.petImageUri != null) {
                                        Card(modifier = Modifier.padding(start = 44.dp, top = 4.dp, bottom = 8.dp).size(80.dp)) {
                                            AsyncImage(model = child.petImageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        }
                                    }
                                }
                                
                                if (child.notes.isNotBlank()) {
                                    Text(child.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
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
            title = { Text("Delete Friend") },
            text = { Text("Are you sure you want to delete this friend?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick(friendWithChildren.friend)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
