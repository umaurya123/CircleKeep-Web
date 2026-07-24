package com.circlekeep.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.circlekeep.data.Friend
import com.circlekeep.data.FriendWithChildren
import com.circlekeep.viewmodel.SortOrder
import com.circlekeep.ui.components.FriendItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendListScreen(
    friends: List<FriendWithChildren>,
    searchQuery: String,
    selectedGroup: String?,
    currentSortOrder: SortOrder,
    showInlineData: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSelectedGroupChange: (String?) -> Unit,
    onSortChange: (SortOrder) -> Unit,
    onToggleInline: () -> Unit,
    onFriendClick: (Long) -> Unit,
    onEditFriendClick: (Long, Long?) -> Unit,
    onAddFriendClick: () -> Unit,
    onToggleFavorite: (Friend) -> Unit,
    onTogglePin: (Friend) -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (selectedGroup != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("$selectedGroup (${friends.size})", style = MaterialTheme.typography.titleLarge)
                            IconButton(onClick = { onSelectedGroupChange(null) }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear Filter")
                            }
                        }
                    } else {
                        TextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text("Search (${friends.size})") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) }
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onToggleInline) {
                        Icon(
                            imageVector = if (showInlineData) Icons.Rounded.ViewStream else Icons.AutoMirrored.Rounded.ViewList,
                            contentDescription = "Toggle Inline"
                        )
                    }
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.AutoMirrored.Rounded.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("First, Last Name") },
                                leadingIcon = { if (currentSortOrder == SortOrder.FIRST_LAST_NAME) Icon(Icons.Rounded.Check, contentDescription = null) },
                                onClick = {
                                    onSortChange(SortOrder.FIRST_LAST_NAME)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Last, First Name") },
                                leadingIcon = { if (currentSortOrder == SortOrder.LAST_FIRST_NAME) Icon(Icons.Rounded.Check, contentDescription = null) },
                                onClick = {
                                    onSortChange(SortOrder.LAST_FIRST_NAME)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Group") },
                                leadingIcon = { if (currentSortOrder == SortOrder.GROUP) Icon(Icons.Rounded.Check, contentDescription = null) },
                                onClick = {
                                    onSortChange(SortOrder.GROUP)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Birthday") },
                                leadingIcon = { if (currentSortOrder == SortOrder.BIRTHDAY) Icon(Icons.Rounded.Check, contentDescription = null) },
                                onClick = {
                                    onSortChange(SortOrder.BIRTHDAY)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Marriage Anniversary") },
                                leadingIcon = { if (currentSortOrder == SortOrder.MARRIAGE_ANNIVERSARY) Icon(Icons.Rounded.Check, contentDescription = null) },
                                onClick = {
                                    onSortChange(SortOrder.MARRIAGE_ANNIVERSARY)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddFriendClick) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Friend")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(friends) { friendWithChildren ->
                FriendItem(
                    friendWithChildren = friendWithChildren,
                    showInline = showInlineData,
                    currentSortOrder = currentSortOrder,
                    onClick = { onFriendClick(friendWithChildren.friend.id) },
                    onEditClick = { childId -> onEditFriendClick(friendWithChildren.friend.id, childId) },
                    onToggleFavorite = { onToggleFavorite(friendWithChildren.friend) },
                    onTogglePin = { onTogglePin(friendWithChildren.friend) }
                )
            }
        }
    }
}
