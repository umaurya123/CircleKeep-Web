package com.circlekeep.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.circlekeep.data.Group
import com.circlekeep.ui.components.PlaceholderAvatar
import com.circlekeep.viewmodel.FriendViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    viewModel: FriendViewModel,
    groups: List<Group>,
    onGroupClick: (String) -> Unit,
    onAddGroup: (String) -> Unit,
    onDeleteGroup: (Group) -> Unit,
    onRenameGroup: (String, String) -> Unit
) {
    val friends by viewModel.friendsState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var groupToEdit by remember { mutableStateOf<Group?>(null) }
    var newGroupName by remember { mutableStateOf("") }
    var editGroupName by remember { mutableStateOf("") }
    var groupToDelete by remember { mutableStateOf<Group?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Groups") },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add Group")
                    }
                }
            )
        }
    ) { padding ->
        if (groups.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No friends yet")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(groups) { group ->
                    val count = friends.count { it.friend.groups.contains(group.name) }
                    ListItem(
                        modifier = Modifier.clickable { onGroupClick(group.name) },
                        headlineContent = { Text("${group.name} ($count)") },
                        leadingContent = { 
                            Surface(
                                modifier = Modifier.size(40.dp).clip(CircleShape),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                PlaceholderAvatar(name = group.name)
                            }
                        },
                        trailingContent = {
                            Row {
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
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Group") },
            text = {
                OutlinedTextField(
                    value = newGroupName,
                    onValueChange = { newGroupName = it },
                    label = { Text("Group Name") },
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
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditDialog && groupToEdit != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Rename Group") },
            text = {
                OutlinedTextField(
                    value = editGroupName,
                    onValueChange = { editGroupName = it },
                    label = { Text("Group Name") },
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
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (groupToDelete != null) {
        AlertDialog(
            onDismissRequest = { groupToDelete = null },
            title = { Text("Delete Group") },
            text = { Text("Are you sure you want to delete group \"${groupToDelete?.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        groupToDelete?.let { onDeleteGroup(it) }
                        groupToDelete = null
                    }
                ) {
                    Text("Delete") 
                }
            },
            dismissButton = {
                TextButton(onClick = { groupToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
