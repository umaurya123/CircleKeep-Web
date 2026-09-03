package com.circlekeep.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.circlekeep.data.FriendWithChildren
import com.circlekeep.viewmodel.SortOrder
import com.circlekeep.ui.theme.LocalAppStrings

@Composable
fun FriendItem(
    modifier: Modifier = Modifier,
    friendWithChildren: FriendWithChildren,
    showInline: Boolean,
    currentSortOrder: SortOrder,
    onEditClick: (Long?) -> Unit,
    onToggleFavorite: () -> Unit,
    onTogglePin: () -> Unit
) {
    val strings = LocalAppStrings.current
    val friend = friendWithChildren.friend
    val displayName = if (currentSortOrder == SortOrder.LAST_FIRST_NAME) {
        buildString {
            append(friend.lastName)
            if (friend.firstName.isNotBlank()) {
                append(", ")
                append(friend.firstName)
                if (friend.nickname.isNotBlank()) append(" '${friend.nickname}'")
                if (friend.middleName.isNotBlank()) append(" ${friend.middleName}")
            }
        }
    } else {
        buildString {
            append(friend.firstName)
            if (friend.nickname.isNotBlank()) append(" '${friend.nickname}'")
            if (friend.middleName.isNotBlank()) append(" ${friend.middleName}")
            if (friend.lastName.isNotBlank()) append(" ${friend.lastName}")
        }
    }

    ListItem(
        modifier = modifier,
        headlineContent = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = displayName,
                    fontWeight = FontWeight.Bold
                )
                if (friend.isPinned) {
                    Icon(Icons.Rounded.PushPin, contentDescription = "Pinned", modifier = Modifier.size(16.dp).padding(start = 4.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
        },
        leadingContent = {
            Surface(
                modifier = Modifier.size(40.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                val name = "${friend.firstName} ${friend.middleName} ${friend.lastName}"
                if (!friend.imageUri.isNullOrBlank() && friend.imageUri != "null") {
                    SubcomposeAsyncImage(
                        model = friend.imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = { PlaceholderAvatar(name = name) }
                    )
                } else {
                    PlaceholderAvatar(name = name)
                }
            }
        },
        supportingContent = {
            Column {
                if (friend.cellPhone.isNotBlank()) {
                    Text(friend.cellPhone, style = MaterialTheme.typography.bodySmall)
                }
                if (friend.groups.isNotEmpty()) {
                    Text(friend.groups.joinToString(", "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
                
                val importantDates = buildString {
                    val months = listOf("") + strings.months
                    if (friend.birthDay.isNotBlank() && friend.birthMonth.isNotBlank()) {
                        val monthName = friend.birthMonth.toIntOrNull()?.let { if (it in 1..12) months[it] else friend.birthMonth } ?: friend.birthMonth
                        append("🎂 $monthName ${friend.birthDay}")
                    }
                    if (friend.anniversaryDay.isNotBlank() && friend.anniversaryMonth.isNotBlank()) {
                        if (isNotEmpty()) append(" • ")
                        val monthName = friend.anniversaryMonth.toIntOrNull()?.let { if (it in 1..12) months[it] else friend.anniversaryMonth } ?: friend.anniversaryMonth
                        append("💍 $monthName ${friend.anniversaryDay}")
                    }
                }
                if (importantDates.isNotBlank()) {
                    Text(importantDates, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                }

                if (showInline) {
                    if (friend.partnerFirstName.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        val partnerTypeKey = friend.partnerType
                        val label = if (partnerTypeKey.isBlank()) strings.partner else (strings.partnerTypes[partnerTypeKey] ?: partnerTypeKey)
                        val partnerDisplayName = buildString {
                            append(friend.partnerFirstName)
                            if (friend.partnerNickname.isNotBlank()) append(" '${friend.partnerNickname}'")
                            if (friend.partnerMiddleName.isNotBlank()) append(" ${friend.partnerMiddleName}")
                            if (friend.partnerLastName.isNotBlank()) append(" ${friend.partnerLastName}")
                        }
                        Text("$label: $partnerDisplayName", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        if (friend.partnerPhone.isNotBlank()) {
                            Text(friend.partnerPhone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    if (friend.email.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(friend.email, style = MaterialTheme.typography.bodySmall)
                    }
                    
                    if (friendWithChildren?.children?.isNotEmpty() == true) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = strings.children,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    friendWithChildren?.children?.forEach { child ->
                        val childText = buildString {
                            append(child.firstName)
                            if (child.nickname.isNotBlank()) append(" '${child.nickname}'")
                            if (child.middleName.isNotBlank()) append(" ${child.middleName}")
                            if (child.lastName.isNotBlank()) append(" ${child.lastName}")
                            if (child.age != null) {
                                val unit = if (child.ageUnit.contains("Month")) strings.monthUnit else strings.yearUnit
                                append(" (${child.age} $unit)")
                            }
                            if (child.phoneNumber.isNotBlank()) append(" - ${child.phoneNumber}")
                        }
                        Text(
                            text = "• $childText",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.clickable { onEditClick(child.id) }
                        )
                    }
                }
            }
        },
        trailingContent = {
            Row {
                IconButton(onClick = onTogglePin) {
                    Icon(
                        imageVector = Icons.Rounded.PushPin,
                        contentDescription = "Pin",
                        tint = if (friend.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (friend.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (friend.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    )
}
