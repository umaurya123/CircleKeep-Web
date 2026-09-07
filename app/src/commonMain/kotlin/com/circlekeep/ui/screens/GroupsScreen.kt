package com.circlekeep.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.circlekeep.data.Group
import com.circlekeep.ui.components.PlaceholderAvatar
import com.circlekeep.viewmodel.FriendViewModel
import com.circlekeep.ui.theme.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun GroupsScreen(
    viewModel: FriendViewModel,
    groups: List<Group>,
    onGroupClick: (String) -> Unit,
    onAddGroup: (String) -> Unit,
    onDeleteGroup: (Group) -> Unit,
    onRenameGroup: (String, String) -> Unit
) {
    val strings = LocalAppStrings.current
    val friends by viewModel.friendsState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var groupToEdit by remember { mutableStateOf<Group?>(null) }
    var newGroupName by remember { mutableStateOf("") }
    var editGroupName by remember { mutableStateOf("") }
    var groupToDelete by remember { mutableStateOf<Group?>(null) }
    var isReorderMode by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.groups) },
                actions = {
                    IconButton(onClick = { isReorderMode = !isReorderMode }) {
                        Icon(
                            imageVector = if (isReorderMode) Icons.Rounded.Check else Icons.Rounded.Reorder, 
                            contentDescription = "Toggle Reorder"
                        )
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add Group")
                    }
                }
            )
        }
    ) { padding ->
        if (groups.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(strings.noContacts) // Or a more specific string
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp) 
            ) {
                itemsIndexed(groups, key = { _, g -> g.name }) { index, group ->
                    val count = friends.count { it.friend.groups.contains(group.name) }
                    var verticalOffset by remember { mutableStateOf(0f) }
                    val currentGroups by rememberUpdatedState(groups)
                    val currentIndex by rememberUpdatedState(index)

                    ListItem(
                        headlineContent = { Text("${group.name} ($count)") },
                        leadingContent = { 
                            if (isReorderMode) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .pointerInput(group.name) {
                                            detectVerticalDragGestures(
                                                onDragStart = { verticalOffset = 0f },
                                                onVerticalDrag = { change, dragAmount ->
                                                    change.consume()
                                                    verticalOffset += dragAmount
                                                    val list = currentGroups
                                                    val idx = currentIndex
                                                    val threshold = 25f // More responsive
                                                    
                                                    if (verticalOffset > threshold && idx < list.size - 1) {
                                                        val newList = list.toMutableList()
                                                        val item = newList.removeAt(idx)
                                                        newList.add(idx + 1, item)
                                                        viewModel.updateGroupOrder(newList)
                                                        verticalOffset = 0f
                                                    } else if (verticalOffset < -threshold && idx > 0) {
                                                        val newList = list.toMutableList()
                                                        val item = newList.removeAt(idx)
                                                        newList.add(idx - 1, item)
                                                        viewModel.updateGroupOrder(newList)
                                                        verticalOffset = 0f
                                                    }
                                                }
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Reorder, 
                                        contentDescription = "Drag handle",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else {
                                Surface(
                                    modifier = Modifier.size(40.dp).clip(CircleShape),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    PlaceholderAvatar(name = group.name)
                                }
                            }
                        },
                        trailingContent = {
                            if (!isReorderMode) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { 
                                        groupToEdit = group
                                        editGroupName = group.name
                                        showEditDialog = true
                                    }) {
                                        Icon(Icons.Rounded.Edit, contentDescription = "Rename Group")
                                    }
                                    IconButton(onClick = { groupToDelete = group }) {
                                        Icon(Icons.Rounded.Delete, contentDescription = "Delete Group")
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .animateItem()
                            .combinedClickable(
                                onClick = { if (!isReorderMode) onGroupClick(group.name) },
                                onLongClick = { isReorderMode = true }
                            )
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(strings.groups) }, // "Add New Group"
            text = {
                OutlinedTextField(
                    value = newGroupName,
                    onValueChange = { newGroupName = it },
                    label = { Text(strings.groups) }, // "Group Name"
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newGroupName.isNotBlank()) {
                            onAddGroup(newGroupName)
                            newGroupName = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text(strings.ok)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    if (showEditDialog && groupToEdit != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(strings.editContact) }, // "Rename Group"
            text = {
                OutlinedTextField(
                    value = editGroupName,
                    onValueChange = { editGroupName = it },
                    label = { Text(strings.groups) }, // "Group Name"
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editGroupName.isNotBlank()) {
                            groupToEdit?.let { group ->
                                onRenameGroup(group.name, editGroupName)
                            }
                            showEditDialog = false
                            groupToEdit = null
                        }
                    }
                ) {
                    Text(strings.ok)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    if (groupToDelete != null) {
        AlertDialog(
            onDismissRequest = { groupToDelete = null },
            title = { Text(strings.delete) }, // "Delete Group"
            text = { Text("${strings.delete} \"${groupToDelete?.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        groupToDelete?.let { onDeleteGroup(it) }
                        groupToDelete = null
                    }
                ) {
                    Text(strings.delete) 
                }
            },
            dismissButton = {
                TextButton(onClick = { groupToDelete = null }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}
