package com.circlekeep.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.circlekeep.data.Friend
import com.circlekeep.data.FriendWithChildren
import com.circlekeep.data.Group
import com.circlekeep.viewmodel.SortOrder
import com.circlekeep.ui.components.FriendItem
import com.circlekeep.ui.components.FriendGridItem
import com.circlekeep.LocalPlatformUI
import com.circlekeep.ui.theme.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun FriendListScreen(
    friends: List<FriendWithChildren>,
    groups: List<Group>,
    activeGroups: Set<String>,
    searchQuery: String,
    selectedGroups: Set<String>,
    groupFilterMode: String,
    onGroupFilterModeChange: (String) -> Unit,
    onGroupSelected: (String) -> Unit,
    onSetGroups: (Set<String>) -> Unit,
    onGroupClear: () -> Unit,
    onSetDefaultGroups: (Set<String>) -> Unit,
    currentSortOrder: SortOrder,
    showInlineData: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSortChange: (SortOrder) -> Unit,
    onToggleInline: () -> Unit,
    onFriendClick: (Long) -> Unit,
    onEditFriendClick: (Long, Long?) -> Unit,
    onAddFriendClick: () -> Unit,
    onToggleFavorite: (Friend) -> Unit,
    onTogglePin: (Friend) -> Unit,
    onQRScanned: (String) -> Unit,
    onDeleteFriends: (Set<Long>) -> Unit,
    onNavigateToSettings: () -> Unit,
    isGridView: Boolean = false,
    gridColumns: Int = 2,
    onToggleGridView: (Boolean) -> Unit = {},
    onGridColumnsChange: (Int) -> Unit = {}
) {
    val strings = LocalAppStrings.current
    var showSortMenu by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var selectedFriendIds by remember { mutableStateOf(setOf<Long>()) }
    val selectionMode = selectedFriendIds.isNotEmpty()
    val platformUI = LocalPlatformUI.current
    
    var isGroupMultiSelectMode by remember { mutableStateOf(selectedGroups.size > 1) }
    var qrScannerTrigger by remember { mutableStateOf(false) }

    com.circlekeep.QRScanner(
        trigger = qrScannerTrigger,
        onTriggerReset = { qrScannerTrigger = false },
        onCodeScanned = { raw ->
            onQRScanned(raw)
            qrScannerTrigger = false
        },
        onCancel = { qrScannerTrigger = false }
    )
    
    // Sync mode when groups are loaded or changed
    LaunchedEffect(selectedGroups) {
        if (selectedGroups.size > 1) {
            isGroupMultiSelectMode = true
        } else if (selectedGroups.isEmpty()) {
            isGroupMultiSelectMode = false
        }
    }

    // Ensure all active groups are shown, and preserve order from groups list
    val filteredGroups = remember(groups, activeGroups) {
        groups.filter { group ->
            activeGroups.contains(group.name.trim())
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface).windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))) {
                if (selectionMode) {
                    TopAppBar(
                        title = { Text("${selectedFriendIds.size} ${strings.selected}") },
                        navigationIcon = {
                            IconButton(onClick = { selectedFriendIds = emptySet() }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear selection")
                            }
                        },
                        actions = {
                            IconButton(onClick = { 
                                onDeleteFriends(selectedFriendIds)
                                selectedFriendIds = emptySet()
                            }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Delete selected")
                            }
                        }
                    )
                } else {
                    // Row 1: App Name and Icons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strings.appName,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { onToggleGridView(!isGridView) }) {
                                Icon(
                                    imageVector = if (isGridView) Icons.AutoMirrored.Rounded.ViewList else Icons.Rounded.GridView,
                                    contentDescription = "Toggle Grid View"
                                )
                            }
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
                                        text = { Text(strings.sortFirstLast) },
                                        leadingIcon = { if (currentSortOrder == SortOrder.FIRST_LAST_NAME) Icon(Icons.Rounded.Check, contentDescription = null) },
                                        onClick = {
                                            onSortChange(SortOrder.FIRST_LAST_NAME)
                                            showSortMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(strings.sortLastFirst) },
                                        leadingIcon = { if (currentSortOrder == SortOrder.LAST_FIRST_NAME) Icon(Icons.Rounded.Check, contentDescription = null) },
                                        onClick = {
                                            onSortChange(SortOrder.LAST_FIRST_NAME)
                                            showSortMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(strings.sortGroup) },
                                        leadingIcon = { if (currentSortOrder == SortOrder.GROUP) Icon(Icons.Rounded.Check, contentDescription = null) },
                                        onClick = {
                                            onSortChange(SortOrder.GROUP)
                                            showSortMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(strings.sortBirthday) },
                                        leadingIcon = { if (currentSortOrder == SortOrder.BIRTHDAY) Icon(Icons.Rounded.Check, contentDescription = null) },
                                        onClick = {
                                            onSortChange(SortOrder.BIRTHDAY)
                                            showSortMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(strings.sortAnniversary) },
                                        leadingIcon = { if (currentSortOrder == SortOrder.MARRIAGE_ANNIVERSARY) Icon(Icons.Rounded.Check, contentDescription = null) },
                                        onClick = {
                                            onSortChange(SortOrder.MARRIAGE_ANNIVERSARY)
                                            showSortMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(strings.sortCreationDate) },
                                        leadingIcon = { if (currentSortOrder == SortOrder.CREATION_DATE) Icon(Icons.Rounded.Check, contentDescription = null) },
                                        onClick = {
                                            onSortChange(SortOrder.CREATION_DATE)
                                            showSortMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(strings.sortLastModified) },
                                        leadingIcon = { if (currentSortOrder == SortOrder.LAST_MODIFIED) Icon(Icons.Rounded.Check, contentDescription = null) },
                                        onClick = {
                                            onSortChange(SortOrder.LAST_MODIFIED)
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                            Box {
                                IconButton(onClick = { showMoreMenu = true }) {
                                    Icon(Icons.Rounded.MoreVert, contentDescription = "More options")
                                }
                                DropdownMenu(
                                    expanded = showMoreMenu,
                                    onDismissRequest = { showMoreMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(strings.settings) },
                                        leadingIcon = { Icon(Icons.Rounded.Settings, contentDescription = null) },
                                        onClick = {
                                            showMoreMenu = false
                                            onNavigateToSettings()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(strings.visitWebsite) },
                                        leadingIcon = { Icon(Icons.Rounded.Language, contentDescription = null) },
                                        onClick = {
                                            showMoreMenu = false
                                            platformUI.openUrl("https://circlekeepapp.com")
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Row 2: Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("${strings.search} (${friends.size})") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        ),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Rounded.Clear, contentDescription = "Clear search")
                                }
                            }
                        }
                    )
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        FilterChip(
                            selected = selectedGroups.isEmpty(),
                            onClick = {
                                isGroupMultiSelectMode = false
                                onGroupClear()
                            },
                            label = { Text(strings.all) }
                        )
                    }
                    
                    item {
                        IconButton(onClick = { 
                            onSetDefaultGroups(selectedGroups)
                            platformUI.showToast("Default view saved")
                        }) {
                            Icon(
                                Icons.Rounded.PushPin, 
                                contentDescription = "Set Default", 
                                tint = if (selectedGroups.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    if (isGroupMultiSelectMode || selectedGroups.size > 1) {
                        item {
                            FilterChip(
                                selected = false,
                                onClick = { 
                                    onGroupFilterModeChange(if (groupFilterMode == "OR") "AND" else "OR") 
                                },
                                label = { Text("Mode: $groupFilterMode") },
                                colors = FilterChipDefaults.filterChipColors(
                                    labelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                    items(filteredGroups) { group ->
                        val groupName = group.name
                        val isSelected = selectedGroups.contains(groupName)
                        
                        // Use a Box to capture both tap and long-press without FilterChip interference
                        Box(modifier = Modifier.padding(vertical = 4.dp)) {
                            FilterChip(
                                selected = isSelected,
                                onClick = { }, // Handled by Box
                                label = { Text(groupName) }
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .combinedClickable(
                                        onClick = { 
                                            if (isGroupMultiSelectMode) {
                                                onGroupSelected(groupName)
                                            } else {
                                                onSetGroups(setOf(groupName))
                                            }
                                        },
                                        onLongClick = { 
                                            isGroupMultiSelectMode = true
                                            onGroupSelected(groupName)
                                        }
                                    )
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (!selectionMode) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FloatingActionButton(onClick = onAddFriendClick) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add Friend")
                    }
                }
            }
        }
    ) { padding ->
        if (friends.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    text = if (searchQuery.isNotBlank() || selectedGroups.isNotEmpty()) strings.noResults else strings.noContacts,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns),
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(friends, key = { it.friend.id }) { friendWithChildren ->
                    val friendId = friendWithChildren.friend.id
                    val isSelected = selectedFriendIds.contains(friendId)
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { 
                                    if (selectedFriendIds.isNotEmpty()) {
                                        selectedFriendIds = if (isSelected) {
                                            selectedFriendIds - friendId
                                        } else {
                                            selectedFriendIds + friendId
                                        }
                                    } else {
                                        onFriendClick(friendId) 
                                    }
                                },
                                onLongClick = {
                                    selectedFriendIds = if (isSelected) {
                                        selectedFriendIds - friendId
                                    } else {
                                        selectedFriendIds + friendId
                                    }
                                }
                            )
                    ) {
                        FriendGridItem(
                            friendWithChildren = friendWithChildren,
                            onToggleFavorite = { onToggleFavorite(friendWithChildren.friend) },
                            onTogglePin = { onTogglePin(friendWithChildren.friend) }
                        )
                        
                        if (isSelected) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                modifier = Modifier.matchParentSize(),
                                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                                shape = MaterialTheme.shapes.medium
                            ) {}
                            Icon(
                                Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(friends, key = { it.friend.id }) { friendWithChildren ->
                    val friendId = friendWithChildren.friend.id
                    val isSelected = selectedFriendIds.contains(friendId)
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { 
                                    if (selectedFriendIds.isNotEmpty()) {
                                        selectedFriendIds = if (isSelected) {
                                            selectedFriendIds - friendId
                                        } else {
                                            selectedFriendIds + friendId
                                        }
                                    } else {
                                        onFriendClick(friendId) 
                                    }
                                },
                                onLongClick = {
                                    selectedFriendIds = if (isSelected) {
                                        selectedFriendIds - friendId
                                    } else {
                                        selectedFriendIds + friendId
                                    }
                                }
                            )
                    ) {
                        FriendItem(
                            friendWithChildren = friendWithChildren,
                            showInline = showInlineData,
                            currentSortOrder = currentSortOrder,
                            onEditClick = { childId -> onEditFriendClick(friendId, childId) },
                            onToggleFavorite = { onToggleFavorite(friendWithChildren.friend) },
                            onTogglePin = { onTogglePin(friendWithChildren.friend) }
                        )
                        
                        if (isSelected) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                modifier = Modifier.matchParentSize(),
                                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                                shape = MaterialTheme.shapes.medium
                            ) {}
                            Icon(
                                Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
