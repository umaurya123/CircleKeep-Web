package com.circlekeep.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.circlekeep.data.FriendWithChildren

@Composable
fun FriendGridItem(
    modifier: Modifier = Modifier,
    friendWithChildren: FriendWithChildren,
    onToggleFavorite: () -> Unit,
    onTogglePin: () -> Unit
) {
    val friend = friendWithChildren.friend
    val displayName = "${friend.firstName} ${friend.lastName}"

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                Surface(
                    modifier = Modifier.size(80.dp).clip(CircleShape),
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
                
                if (friend.isPinned) {
                    Icon(
                        Icons.Rounded.PushPin, 
                        contentDescription = "Pinned", 
                        modifier = Modifier.size(16.dp), 
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = displayName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (friend.nickname.isNotBlank()) {
                Text(
                    text = "'${friend.nickname}'",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (friend.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (friend.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                IconButton(onClick = onTogglePin, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.PushPin,
                        contentDescription = "Pin",
                        tint = if (friend.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
