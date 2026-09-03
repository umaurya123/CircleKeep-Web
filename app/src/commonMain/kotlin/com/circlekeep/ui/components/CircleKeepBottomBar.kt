package com.circlekeep.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.circlekeep.navigation.Destination
import com.circlekeep.ui.theme.LocalAppStrings

@Composable
fun CircleKeepBottomBar(
    currentDestination: Destination,
    friendCount: Int,
    favoriteCount: Int,
    eventsCount: Int,
    onNavigate: (Destination) -> Unit
) {
    val strings = LocalAppStrings.current
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("${strings.home} ($friendCount)") },
            selected = currentDestination is Destination.Home || currentDestination is Destination.FriendDetail || currentDestination is Destination.AddFriend || currentDestination is Destination.EditFriend,
            onClick = { onNavigate(Destination.Home) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
            label = { Text("${strings.favorites} ($favoriteCount)") },
            selected = currentDestination is Destination.Favorites,
            onClick = { onNavigate(Destination.Favorites) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text(strings.groups) },
            selected = currentDestination is Destination.Groups,
            onClick = { onNavigate(Destination.Groups) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Rounded.CalendarToday, contentDescription = null) },
            label = { Text("${strings.events} ($eventsCount)") },
            selected = currentDestination is Destination.UpcomingEvents,
            onClick = { onNavigate(Destination.UpcomingEvents) }
        )
    }
}
