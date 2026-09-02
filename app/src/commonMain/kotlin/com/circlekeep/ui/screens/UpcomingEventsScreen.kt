package com.circlekeep.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.circlekeep.viewmodel.FriendViewModel
import com.circlekeep.viewmodel.UpcomingEvent

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UpcomingEventsScreen(
    viewModel: FriendViewModel,
    onEventClick: (Long) -> Unit
) {
    val events by viewModel.upcomingEventsState.collectAsState()

    // Group events by timeframe
    val groupedEvents = remember(events) {
        events.groupBy {
            when {
                it.daysRemaining == 0 -> "Today"
                it.daysRemaining == 1 -> "Tomorrow"
                it.daysRemaining <= 7 -> "This Week"
                else -> "Later this Month"
            }
        }
    }

    val categories = listOf("Today", "Tomorrow", "This Week", "Later this Month")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upcoming Events") },
                actions = {
                    IconButton(onClick = { /* Implement global reminder settings */ }) {
                        Icon(Icons.Rounded.Notifications, contentDescription = "Reminders")
                    }
                }
            )
        }
    ) { padding ->
        if (events.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.Notifications, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.height(16.dp))
                    Text("No events in the next 30 days", color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                categories.forEach { category ->
                    val categoryEvents = groupedEvents[category]
                    if (!categoryEvents.isNullOrEmpty()) {
                        stickyHeader {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Text(
                                    text = category,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (category == "Today") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                        items(categoryEvents) { event ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                EventCard(event = event, onClick = { onEventClick(event.friendId) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(event: UpcomingEvent, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (event.daysRemaining == 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            val icon = if (event.type.contains("Birthday")) Icons.Rounded.Cake else Icons.Rounded.Favorite
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(20.dp)
            )

            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp).clip(androidx.compose.foundation.shape.CircleShape),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    if (!event.imageUri.isNullOrBlank() && event.imageUri != "null") {
                        AsyncImage(
                            model = event.imageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        com.circlekeep.ui.components.PlaceholderAvatar(name = event.name)
                    }
                }

                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    val displayType = event.type.replace("Wedding Anniversary", "Marriage Anniversary")
                    Text(event.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(displayType, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(event.date, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    val dayText = when (event.daysRemaining) {
                        0 -> "Today!"
                        1 -> "Tomorrow"
                        else -> "In ${event.daysRemaining} days"
                    }
                    Text(
                        text = dayText,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (event.daysRemaining <= 7) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
