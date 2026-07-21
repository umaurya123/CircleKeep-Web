package com.circlekeep.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.provider.ContactsContract
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import coil.compose.AsyncImage
import com.circlekeep.R
import com.circlekeep.data.Child
import com.circlekeep.data.Friend
import com.circlekeep.data.FriendWithChildren
import com.circlekeep.data.Group
import com.circlekeep.navigation.Destination
import com.circlekeep.viewmodel.FriendViewModel
import com.circlekeep.viewmodel.SortOrder
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.launch
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun parseDate(dateString: String?): Date? {
    if (dateString.isNullOrBlank()) return null
    val formats = listOf(
        "dd/MM/yyyy", "MM/dd/yyyy", "yyyy-MM-dd", "dd-MM-yyyy",
        "MM/dd/yy", "dd/MM/yy", "M/d/yy", "d/M/yy",
        "MMM dd, yyyy", "MMM d, yyyy", "MMMM dd, yyyy", "MMMM d, yyyy",
        "MMM dd, yy", "MMM d, yy", "MMMM dd, yy", "MMMM d, yy"
    )
    
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    
    for (format in formats) {
        try {
            val sdf = SimpleDateFormat(format, Locale.getDefault())
            sdf.isLenient = false
            var date = sdf.parse(dateString)
            if (date != null) {
                // Handle 2-digit years or very small years
                val cal = Calendar.getInstance().apply { time = date }
                var year = cal.get(Calendar.YEAR)
                
                if (year < 100) {
                    // If year is 66, it should likely be 1966. 
                    // Pivot at currentYear % 100 + 10 (e.g., 2035)
                    val pivot = (currentYear % 100) + 10
                    year += if (year > pivot) 1900 else 2000
                    cal.set(Calendar.YEAR, year)
                    date = cal.time
                } else if (year > currentYear && year < currentYear + 100) {
                    // If year is in the future (like 2066 for Aug 15, 66), move it to past century
                    cal.set(Calendar.YEAR, year - 100)
                    date = cal.time
                }
                return date
            }
        } catch (e: Exception) { }
    }
    val styles = listOf(java.text.DateFormat.SHORT, java.text.DateFormat.MEDIUM, java.text.DateFormat.LONG)
    for (style in styles) {
        try {
            val df = java.text.DateFormat.getDateInstance(style, Locale.getDefault())
            df.isLenient = false
            var date = df.parse(dateString)
            if (date != null) {
                val cal = Calendar.getInstance().apply { time = date }
                var year = cal.get(Calendar.YEAR)
                if (year < 100) {
                    val pivot = (currentYear % 100) + 10
                    year += if (year > pivot) 1900 else 2000
                    cal.set(Calendar.YEAR, year)
                    date = cal.time
                } else if (year > currentYear && year < currentYear + 100) {
                    cal.set(Calendar.YEAR, year - 100)
                    date = cal.time
                }
                return date
            }
        } catch (e: Exception) { }
    }
    return null
}

fun parseDateToDayMonth(dateString: String): Pair<String, String>? {
    val date = parseDate(dateString) ?: return null
    val cal = Calendar.getInstance()
    cal.time = date
    val day = cal.get(Calendar.DAY_OF_MONTH).toString()
    val month = (cal.get(Calendar.MONTH) + 1).toString()
    return day to month
}

fun formatDisplayDate(dateString: String?): String {
    val date = parseDate(dateString) ?: return dateString ?: ""
    return java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM).format(date)
}

fun formatPartialDate(day: String, month: String): String {
    if (day.isBlank() || month.isBlank()) return ""
    val months = listOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val monthName = month.toIntOrNull()?.let { if (it in 1..12) months[it] else month } ?: month
    return "$monthName $day"
}

fun isDayValidForMonth(day: String, month: String): Boolean {
    if (day.isBlank()) return true
    val m = month.toIntOrNull() ?: return false
    val d = day.toIntOrNull() ?: return false
    
    if (m !in 1..12) return false
    
    val maxDays = when (m) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> 29
        else -> 0
    }
    return d in 1..maxDays
}

fun calculateAge(dobString: String?): Int? {
    val date = parseDate(dobString) ?: return null
    val today = Calendar.getInstance()
    val birth = Calendar.getInstance().apply { time = date }
    var age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
    if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) age--
    return age
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CircleKeepApp() {
    val context = LocalContext.current
    val backStack = rememberNavBackStack(Destination.Home)
    val viewModel: FriendViewModel = viewModel(factory = FriendViewModel.Factory)

    // Handle back button on top-level screens to prevent accidental exit
    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    val currentDestination = backStack.last() as Destination
    val isTopLevelDestination = backStack.size == 1 && (
            currentDestination is Destination.Home ||
            currentDestination is Destination.Favorites ||
            currentDestination is Destination.Groups ||
            currentDestination is Destination.Settings
    )
    
    androidx.activity.compose.BackHandler(enabled = isTopLevelDestination) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressTime < 2000) {
            (context as? Activity)?.finish()
        } else {
            lastBackPressTime = currentTime
            android.widget.Toast.makeText(context, "Press back again to exit", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    // Custom back logic to ensure we always go back through the stack or to Home
    val onBack: () -> Unit = {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        } else if (backStack.last() !is Destination.Home) {
            backStack.clear()
            backStack.add(Destination.Home)
        }
    }
    
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val directive = remember(windowAdaptiveInfo) {
        calculatePaneScaffoldDirective(windowAdaptiveInfo)
            .copy(horizontalPartitionSpacerSize = 0.dp)
    }
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = directive)

    Column(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.weight(1f),
            bottomBar = {
                val current = backStack.last() as Destination
                val friends by viewModel.friendsState.collectAsState()
                CircleKeepBottomBar(
                    currentDestination = current,
                    friendCount = friends.size,
                    favoriteCount = friends.count { it.friend.isFavorite },
                    onNavigate = { destination ->
                        if (destination is Destination.Home && backStack.last() is Destination.Home) {
                            viewModel.onSearchQueryChange("")
                            viewModel.onSelectedGroupChange(null)
                        }
                        if (backStack.last() != destination) {
                            if (destination is Destination.Home) {
                                viewModel.onSelectedGroupChange(null)
                            }
                            backStack.clear()
                            backStack.add(destination)
                        }
                    }
                )
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            NavDisplay(
                backStack = backStack,
                modifier = Modifier.padding(innerPadding),
                onBack = onBack,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                sceneStrategy = listDetailStrategy,
                entryProvider = entryProvider {
                    entry<Destination.Home>(
                        metadata = ListDetailSceneStrategy.listPane(
                            detailPlaceholder = {
                                PlaceholderScreen(stringResource(R.string.no_friends))
                            }
                        )
                    ) {
                        val friends by viewModel.friendsState.collectAsState()
                        val searchQuery by viewModel.searchQuery.collectAsState()
                        val selectedGroup by viewModel.selectedGroup.collectAsState()
                        val sortOrder by viewModel.sortOrder.collectAsState()
                        val showInlineData by viewModel.showInlineData.collectAsState()

                        FriendListScreen(
                            friends = friends,
                            searchQuery = searchQuery,
                            selectedGroup = selectedGroup,
                            currentSortOrder = sortOrder,
                            showInlineData = showInlineData,
                            onSearchQueryChange = viewModel::onSearchQueryChange,
                            onSelectedGroupChange = viewModel::onSelectedGroupChange,
                            onSortChange = viewModel::onSortOrderChange,
                            onToggleInline = viewModel::toggleInlineData,
                            onFriendClick = { id ->
                                backStack.add(Destination.FriendDetail(id))
                            },
                            onEditFriendClick = { id, childId ->
                                backStack.add(Destination.EditFriend(id, childId))
                            },
                            onAddFriendClick = {
                                backStack.add(Destination.AddFriend)
                            },
                            onToggleFavorite = viewModel::toggleFavorite,
                            onTogglePin = viewModel::togglePin
                        )
                    }
                    entry<Destination.FriendDetail>(
                        metadata = ListDetailSceneStrategy.detailPane()
                    ) { detail ->
                        val friendWithChildren by viewModel.getFriend(detail.friendId).collectAsState(initial = null)
                        
                        FriendDetailScreen(
                            friendWithChildren = friendWithChildren,
                            onEditClick = { id, childId ->
                                backStack.add(Destination.EditFriend(id, childId))
                            },
                            onDeleteClick = { friend ->
                                viewModel.deleteFriend(friend)
                                backStack.removeLastOrNull()
                            },
                            onTogglePin = { friend -> viewModel.togglePin(friend) },
                            onBackClick = onBack
                        )
                    }
                    entry<Destination.AddFriend>(
                        metadata = ListDetailSceneStrategy.detailPane()
                    ) {
                        val groups by viewModel.groupsState.collectAsState()
                        val friends by viewModel.friendsState.collectAsState()
                        val isPaid by viewModel.isPaidState.collectAsState()

                        AddEditFriendScreen(
                            availableGroups = groups.map { it.name },
                            friendCount = friends.size,
                            isPaid = isPaid,
                            onSave = { friend, children ->
                                viewModel.saveFriend(friend, children)
                                onBack()
                            },
                            onCancel = onBack
                        )
                    }
                    entry<Destination.EditFriend>(
                        metadata = ListDetailSceneStrategy.detailPane()
                    ) { edit ->
                        val groups by viewModel.groupsState.collectAsState()
                        val friends by viewModel.friendsState.collectAsState()
                        val isPaid by viewModel.isPaidState.collectAsState()
                        val friendWithChildren by viewModel.getFriend(edit.friendId).collectAsState(initial = null)
                        
                        friendWithChildren?.let { data ->
                            AddEditFriendScreen(
                                initialFriend = data.friend,
                                initialChildren = data.children,
                                scrollToChildId = edit.childId,
                                availableGroups = groups.map { it.name },
                                friendCount = friends.size,
                                isPaid = isPaid,
                                onSave = { friend, children ->
                                    viewModel.saveFriend(friend, children)
                                    onBack()
                                },
                                onCancel = onBack
                            )
                        }
                    }
                    entry<Destination.Favorites>(
                        metadata = ListDetailSceneStrategy.listPane()
                    ) {
                        val friends by viewModel.friendsState.collectAsState()
                        val favorites = friends.filter { it.friend.isFavorite }
                        val searchQuery by viewModel.searchQuery.collectAsState()
                        val sortOrder by viewModel.sortOrder.collectAsState()
                        val showInlineData by viewModel.showInlineData.collectAsState()
                        
                        FriendListScreen(
                            friends = favorites,
                            searchQuery = searchQuery,
                            selectedGroup = null,
                            currentSortOrder = sortOrder,
                            showInlineData = showInlineData,
                            onSearchQueryChange = viewModel::onSearchQueryChange,
                            onSelectedGroupChange = { },
                            onSortChange = viewModel::onSortOrderChange,
                            onToggleInline = viewModel::toggleInlineData,
                            onFriendClick = { id -> backStack.add(Destination.FriendDetail(id)) },
                            onEditFriendClick = { id, childId -> backStack.add(Destination.EditFriend(id, childId)) },
                            onAddFriendClick = { backStack.add(Destination.AddFriend) },
                            onToggleFavorite = viewModel::toggleFavorite,
                            onTogglePin = viewModel::togglePin
                        )
                    }
                    entry<Destination.Groups>(
                        metadata = ListDetailSceneStrategy.listPane()
                    ) {
                        val groups by viewModel.groupsState.collectAsState()
                        
                        GroupsScreen(
                            groups = groups,
                            onGroupClick = { groupName ->
                                viewModel.onSelectedGroupChange(groupName)
                                backStack.clear()
                                backStack.add(Destination.Home)
                            },
                            onAddGroup = viewModel::addGroup,
                            onDeleteGroup = viewModel::deleteGroup,
                            onRenameGroup = viewModel::renameGroup
                        )
                    }
                    entry<Destination.Settings>(
                        metadata = ListDetailSceneStrategy.listPane()
                    ) {
                        SettingsScreen(viewModel = viewModel)
                    }
                }
            )
        }
        val isPaid by viewModel.isPaidState.collectAsState()
        if (!isPaid) {
            BannerAd()
        }
    }
}

@Composable
fun CircleKeepBottomBar(
    currentDestination: Destination,
    friendCount: Int,
    favoriteCount: Int,
    onNavigate: (Destination) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Rounded.Home, contentDescription = null) },
            label = { Text("${stringResource(R.string.home)} ($friendCount)") },
            selected = currentDestination is Destination.Home || currentDestination is Destination.FriendDetail || currentDestination is Destination.AddFriend || currentDestination is Destination.EditFriend,
            onClick = { onNavigate(Destination.Home) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Rounded.Favorite, contentDescription = null) },
            label = { Text("${stringResource(R.string.favorites)} ($favoriteCount)") },
            selected = currentDestination is Destination.Favorites,
            onClick = { onNavigate(Destination.Favorites) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Rounded.Groups, contentDescription = null) },
            label = { Text(stringResource(R.string.groups)) },
            selected = currentDestination is Destination.Groups,
            onClick = { onNavigate(Destination.Groups) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Rounded.Settings, contentDescription = null) },
            label = { Text(stringResource(R.string.settings)) },
            selected = currentDestination is Destination.Settings,
            onClick = { onNavigate(Destination.Settings) }
        )
    }
}

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
                            Text("${selectedGroup} (${friends.size})", style = MaterialTheme.typography.titleLarge)
                            IconButton(onClick = { onSelectedGroupChange(null) }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear Filter")
                            }
                        }
                    } else {
                        TextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text("${stringResource(R.string.search)} (${friends.size})") },
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
                            imageVector = if (showInlineData) Icons.Rounded.ViewStream else Icons.Rounded.ViewList,
                            contentDescription = stringResource(R.string.toggle_inline)
                        )
                    }
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.AutoMirrored.Rounded.Sort, contentDescription = stringResource(R.string.sort))
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
                Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.add_friend))
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(friends) { friendWithChildren ->
                FriendItem(
                    friend = friendWithChildren.friend,
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

@Composable
fun FriendItem(
    friend: Friend,
    showInline: Boolean,
    currentSortOrder: SortOrder,
    onClick: () -> Unit,
    onEditClick: (Long?) -> Unit,
    onToggleFavorite: () -> Unit,
    onTogglePin: () -> Unit
) {
    val viewModel: FriendViewModel = viewModel(factory = FriendViewModel.Factory)
    val friendWithChildren by viewModel.getFriend(friend.id).collectAsState(initial = null)

    val displayName = if (currentSortOrder == SortOrder.LAST_FIRST_NAME) {
        buildString {
            append(friend.lastName)
            if (friend.firstName.isNotBlank()) {
                append(", ")
                append(friend.firstName)
                if (friend.middleName.isNotBlank()) append(" ${friend.middleName}")
            }
        }
    } else {
        buildString {
            append(friend.firstName)
            if (friend.middleName.isNotBlank()) append(" ${friend.middleName}")
            if (friend.lastName.isNotBlank()) append(" ${friend.lastName}")
        }
    }

    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(displayName)
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
                if (friend.imageUri != null) {
                    AsyncImage(
                        model = friend.imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.padding(8.dp))
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
                    if (friend.birthDay.isNotBlank() && friend.birthMonth.isNotBlank()) {
                        val months = listOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                        val monthName = friend.birthMonth.toIntOrNull()?.let { if (it in 1..12) months[it] else friend.birthMonth } ?: friend.birthMonth
                        append("🎂 $monthName ${friend.birthDay}")
                    }
                    if (friend.anniversaryDay.isNotBlank() && friend.anniversaryMonth.isNotBlank()) {
                        if (isNotEmpty()) append(" • ")
                        val months = listOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
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
                        val partnerTypeStr = if (friend.partnerType == "Fiance") stringResource(R.string.fiance) else friend.partnerType
                        val label = if (partnerTypeStr.isNotBlank()) partnerTypeStr else stringResource(R.string.partner_name)
                        val partnerDisplayName = buildString {
                            append(friend.partnerFirstName)
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
                            text = stringResource(R.string.children_name_label),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    friendWithChildren?.children?.forEach { child ->
                        val childText = buildString {
                            append(child.firstName)
                            if (child.middleName.isNotBlank()) append(" ${child.middleName}")
                            if (child.lastName.isNotBlank()) append(" ${child.lastName}")
                            if (child.age != null) append(" (${child.age} ${child.ageUnit})")
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
                        imageVector = if (friend.isPinned) Icons.Rounded.PushPin else Icons.Rounded.PushPin,
                        contentDescription = "Pin",
                        tint = if (friend.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (friend.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = stringResource(R.string.favorite),
                        tint = if (friend.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendDetailScreen(
    friendWithChildren: FriendWithChildren?,
    onEditClick: (Long, Long?) -> Unit,
    onDeleteClick: (Friend) -> Unit,
    onTogglePin: (Friend) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (friendWithChildren != null) {
                        IconButton(onClick = { 
                            friendWithChildren?.friend?.let { onTogglePin(it) }
                        }) {
                            Icon(Icons.Rounded.PushPin, contentDescription = "Pin", tint = if (friendWithChildren?.friend?.isPinned == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { 
                            friendWithChildren?.friend?.let { onEditClick(it.id, null) }
                        }) {
                            Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.edit_friend))
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.delete_friend))
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
                            if (friendWithChildren!!.friend.imageUri != null) {
                                AsyncImage(
                                    model = friendWithChildren!!.friend.imageUri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.padding(16.dp))
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = buildString {
                                    append(friendWithChildren!!.friend.firstName)
                                    if (friendWithChildren!!.friend.middleName.isNotBlank()) append(" ${friendWithChildren!!.friend.middleName}")
                                    if (friendWithChildren!!.friend.lastName.isNotBlank()) append(" ${friendWithChildren!!.friend.lastName}")
                                },
                                style = MaterialTheme.typography.headlineMedium
                            )
                            if (friendWithChildren!!.friend.companyName.isNotBlank()) {
                                Text(
                                    text = friendWithChildren!!.friend.companyName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (friendWithChildren!!.friend.collegeSchoolName.isNotBlank()) {
                                Text(
                                    text = friendWithChildren!!.friend.collegeSchoolName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }

                item {
                    DetailRow(Icons.Rounded.LocationOn, friendWithChildren!!.friend.address, onClick = {
                        val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(friendWithChildren!!.friend.address)}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        context.startActivity(mapIntent)
                    })
                    DetailRow(Icons.Rounded.Phone, friendWithChildren!!.friend.cellPhone, onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${friendWithChildren!!.friend.cellPhone}"))
                        context.startActivity(intent)
                    })
                    DetailRow(Icons.Rounded.Work, friendWithChildren!!.friend.officePhone, onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${friendWithChildren!!.friend.officePhone}"))
                        context.startActivity(intent)
                    })
                    DetailRow(Icons.Rounded.Email, friendWithChildren!!.friend.email, onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:${friendWithChildren!!.friend.email}")
                        }
                        context.startActivity(Intent.createChooser(intent, "Send Email"))
                    })
                    DetailRow(Icons.Rounded.Cake, formatDisplayDate(friendWithChildren!!.friend.dateOfBirth), label = buildString {
                        append(stringResource(R.string.dob))
                        calculateAge(friendWithChildren!!.friend.dateOfBirth)?.let { append(" ($it yrs)") }
                    })
                    if (friendWithChildren!!.friend.dateOfBirth.isBlank() && friendWithChildren!!.friend.birthDay.isNotBlank() && friendWithChildren!!.friend.birthMonth.isNotBlank()) {
                        DetailRow(
                            icon = Icons.Rounded.Cake,
                            text = formatPartialDate(friendWithChildren!!.friend.birthDay, friendWithChildren!!.friend.birthMonth),
                            label = stringResource(R.string.birthday)
                        )
                    }
                    DetailRow(Icons.Rounded.Favorite, formatDisplayDate(friendWithChildren!!.friend.anniversaryDate), label = buildString {
                        append(stringResource(R.string.marriage_date))
                        calculateAge(friendWithChildren!!.friend.anniversaryDate)?.let { append(" ($it yrs)") }
                    })
                    if (friendWithChildren!!.friend.anniversaryDate.isBlank() && friendWithChildren!!.friend.anniversaryDay.isNotBlank() && friendWithChildren!!.friend.anniversaryMonth.isNotBlank()) {
                        DetailRow(
                            icon = Icons.Rounded.Favorite,
                            text = formatPartialDate(friendWithChildren!!.friend.anniversaryDay, friendWithChildren!!.friend.anniversaryMonth),
                            label = stringResource(R.string.marriage_day)
                        )
                    }
                    DetailRow(Icons.Rounded.People, friendWithChildren!!.friend.siblings, label = stringResource(R.string.siblings))
                    DetailRow(Icons.Rounded.Group, friendWithChildren!!.friend.groups.joinToString(", "))
                    
                    if (friendWithChildren!!.friend.petName.isNotBlank()) {
                        DetailRow(Icons.Rounded.Pets, friendWithChildren!!.friend.petName, label = stringResource(R.string.pet_name))
                        if (friendWithChildren!!.friend.petImageUri != null) {
                            Card(modifier = Modifier.padding(start = 44.dp, top = 4.dp, bottom = 8.dp).size(100.dp)) {
                                AsyncImage(
                                    model = friendWithChildren!!.friend.petImageUri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    DetailRow(Icons.Rounded.Notes, friendWithChildren!!.friend.notes)
                }

                if (friendWithChildren!!.friend.partnerFirstName.isNotBlank()) {
                    item {
                        Spacer(Modifier.height(16.dp))
                        val partnerTypeStr = if (friendWithChildren!!.friend.partnerType == "Fiance") stringResource(R.string.fiance) else friendWithChildren!!.friend.partnerType
                        val partnerHeader = if (partnerTypeStr.isNotBlank()) partnerTypeStr else stringResource(R.string.partner)
                        
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(partnerHeader, style = MaterialTheme.typography.titleLarge)
                            IconButton(onClick = { onEditClick(friendWithChildren!!.friend.id, -1L) }) {
                                Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.edit_friend))
                            }
                        }
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        modifier = Modifier.size(60.dp).clip(CircleShape),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        if (friendWithChildren!!.friend.partnerImageUri != null) {
                                            AsyncImage(
                                                model = friendWithChildren!!.friend.partnerImageUri,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.padding(12.dp))
                                        }
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column {
                                        val partnerDisplayName = buildString {
                                            append(friendWithChildren!!.friend.partnerFirstName)
                                            if (friendWithChildren!!.friend.partnerMiddleName.isNotBlank()) append(" ${friendWithChildren!!.friend.partnerMiddleName}")
                                            if (friendWithChildren!!.friend.partnerLastName.isNotBlank()) append(" ${friendWithChildren!!.friend.partnerLastName}")
                                        }
                                        Text(
                                            text = partnerDisplayName,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                        if (friendWithChildren!!.friend.partnerPhone.isNotBlank()) {
                                            DetailRow(Icons.Rounded.Phone, friendWithChildren!!.friend.partnerPhone, onClick = {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${friendWithChildren!!.friend.partnerPhone}"))
                                                context.startActivity(intent)
                                            })
                                        }
                                    }
                                }
                                if (friendWithChildren!!.friend.partnerDateOfBirth.isNotBlank()) {
                                    DetailRow(
                                        icon = Icons.Rounded.Cake,
                                        text = formatDisplayDate(friendWithChildren!!.friend.partnerDateOfBirth),
                                        label = buildString {
                                            append(stringResource(R.string.dob))
                                            calculateAge(friendWithChildren!!.friend.partnerDateOfBirth)?.let { append(" ($it yrs)") }
                                        }
                                    )
                                } else if (friendWithChildren!!.friend.partnerBirthDay.isNotBlank() && friendWithChildren!!.friend.partnerBirthMonth.isNotBlank()) {
                                    DetailRow(
                                        icon = Icons.Rounded.Cake,
                                        text = formatPartialDate(friendWithChildren!!.friend.partnerBirthDay, friendWithChildren!!.friend.partnerBirthMonth),
                                        label = stringResource(R.string.birthday)
                                    )
                                }
                                if (friendWithChildren!!.friend.partnerSiblings.isNotBlank()) {
                                    DetailRow(Icons.Rounded.People, friendWithChildren!!.friend.partnerSiblings, label = stringResource(R.string.siblings))
                                }
                            }
                        }
                    }
                }
                
                if (friendWithChildren!!.children.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(16.dp))
                        Text(stringResource(R.string.children), style = MaterialTheme.typography.titleLarge)
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    }
                    items(friendWithChildren!!.children) { child ->
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
                                            Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.padding(8.dp))
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
                                    IconButton(onClick = { onEditClick(friendWithChildren!!.friend.id, child.id) }) {
                                        Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.edit_friend))
                                    }
                                }
                                if (child.collegeSchoolName.isNotBlank()) {
                                    DetailRow(Icons.Rounded.School, child.collegeSchoolName)
                                }
                                if (child.phoneNumber.isNotBlank()) {
                                    DetailRow(Icons.Rounded.Phone, child.phoneNumber, onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${child.phoneNumber}"))
                                        context.startActivity(intent)
                                    })
                                }
                                if (child.dateOfBirth.isNotBlank()) {
                                    DetailRow(Icons.Rounded.Cake, formatDisplayDate(child.dateOfBirth), label = buildString {
                                        append(stringResource(R.string.dob))
                                        calculateAge(child.dateOfBirth)?.let { append(" ($it yrs)") }
                                    })
                                } else {
                                    if (child.birthDay.isNotBlank() && child.birthMonth.isNotBlank()) {
                                        DetailRow(
                                            icon = Icons.Rounded.Cake,
                                            text = formatPartialDate(child.birthDay, child.birthMonth),
                                            label = stringResource(R.string.birthday)
                                        )
                                    }
                                    if (child.age != null) {
                                        DetailRow(
                                            icon = Icons.Rounded.Cake,
                                            text = "${child.age} ${child.ageUnit}",
                                            label = stringResource(R.string.age)
                                        )
                                    }
                                }
                                
                                if (child.partnerFirstName.isNotBlank()) {
                                    Spacer(Modifier.height(8.dp))
                                    if (child.anniversaryDate.isNotBlank()) {
                                        DetailRow(Icons.Rounded.Favorite, formatDisplayDate(child.anniversaryDate), label = stringResource(R.string.marriage_date))
                                    } else if (child.anniversaryDay.isNotBlank() && child.anniversaryMonth.isNotBlank()) {
                                        DetailRow(
                                            icon = Icons.Rounded.Favorite,
                                            text = formatPartialDate(child.anniversaryDay, child.anniversaryMonth),
                                            label = stringResource(R.string.marriage_day)
                                        )
                                    }
                                    
                                    val partnerTypeStr = if (child.partnerType == "Fiance") stringResource(R.string.fiance) else child.partnerType
                                    val partnerLabel = if (partnerTypeStr.isNotBlank()) partnerTypeStr else "Partner"
                                    
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
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${child.partnerPhone}"))
                                            context.startActivity(intent)
                                        })
                                    }
                                    if (child.partnerDateOfBirth.isNotBlank()) {
                                        DetailRow(
                                            icon = Icons.Rounded.Cake,
                                            text = formatDisplayDate(child.partnerDateOfBirth),
                                            label = buildString {
                                                append(stringResource(R.string.dob))
                                                calculateAge(child.partnerDateOfBirth)?.let { append(" ($it yrs)") }
                                            }
                                        )
                                    } else if (child.partnerBirthDay.isNotBlank() && child.partnerBirthMonth.isNotBlank()) {
                                        DetailRow(
                                            icon = Icons.Rounded.Cake,
                                            text = formatPartialDate(child.partnerBirthDay, child.partnerBirthMonth),
                                            label = stringResource(R.string.birthday)
                                        )
                                    }
                                    if (child.partnerSiblings.isNotBlank()) {
                                        DetailRow(Icons.Rounded.People, child.partnerSiblings, label = stringResource(R.string.siblings))
                                    }
                                }
                                
                                if (child.petName.isNotBlank()) {
                                    DetailRow(Icons.Rounded.Pets, child.petName, label = stringResource(R.string.pet_name))
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
            title = { Text(stringResource(R.string.delete_friend)) },
            text = { Text(stringResource(R.string.confirm_delete)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        friendWithChildren?.friend?.let { onDeleteClick(it) }
                        showDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete_friend))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, label: String? = null, onClick: (() -> Unit)? = null) {
    if (text.isNotBlank()) {
        Row(
            verticalAlignment = Alignment.CenterVertically, 
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(vertical = 4.dp, horizontal = 4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                if (label != null) {
                    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                }
                Text(text, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFriendScreen(
    initialFriend: Friend? = null,
    initialChildren: List<Child> = emptyList(),
    scrollToChildId: Long? = null,
    availableGroups: List<String> = emptyList(),
    friendCount: Int = 0,
    isPaid: Boolean = false,
    onSave: (Friend, List<Child>) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var saveDelaySeconds by remember { mutableIntStateOf(0) }
    var isSaving by remember { mutableStateOf(false) }
    var showFullPageAd by remember { mutableStateOf(false) }
    var interstitialAd by remember { mutableStateOf<com.google.android.gms.ads.interstitial.InterstitialAd?>(null) }

    LaunchedEffect(Unit) {
        if (friendCount > 15 && !isPaid) {
            loadInterstitialAd(context) { ad ->
                interstitialAd = ad
            }
        }
    }

    var firstName by remember { mutableStateOf(initialFriend?.firstName ?: "") }
    var middleName by remember { mutableStateOf(initialFriend?.middleName ?: "") }
    var lastName by remember { mutableStateOf(initialFriend?.lastName ?: "") }
    var address by remember { mutableStateOf(initialFriend?.address ?: "") }
    var cellPhone by remember { mutableStateOf(initialFriend?.cellPhone ?: "") }
    var officePhone by remember { mutableStateOf(initialFriend?.officePhone ?: "") }
    var email by remember { mutableStateOf(initialFriend?.email ?: "") }
    var workEmail by remember { mutableStateOf(initialFriend?.workEmail ?: "") }
    
    var partnerFirstName by remember { mutableStateOf(initialFriend?.partnerFirstName ?: "") }
    var partnerMiddleName by remember { mutableStateOf(initialFriend?.partnerMiddleName ?: "") }
    var partnerLastName by remember { mutableStateOf(initialFriend?.partnerLastName ?: "") }
    var partnerPhone by remember { mutableStateOf(initialFriend?.partnerPhone ?: "") }
    var partnerEmail by remember { mutableStateOf(initialFriend?.partnerEmail ?: "") }
    var partnerWorkEmail by remember { mutableStateOf(initialFriend?.partnerWorkEmail ?: "") }
    var partnerType by remember { mutableStateOf(initialFriend?.partnerType ?: "") }
    var partnerSiblings by remember { mutableStateOf(initialFriend?.partnerSiblings ?: "") }
    var partnerCompanyName by remember { mutableStateOf(initialFriend?.partnerCompanyName ?: "") }
    var partnerCollegeSchoolName by remember { mutableStateOf(initialFriend?.partnerCollegeSchoolName ?: "") }
    
    var companyName by remember { mutableStateOf(initialFriend?.companyName ?: "") }
    var collegeSchoolName by remember { mutableStateOf(initialFriend?.collegeSchoolName ?: "") }
    var siblings by remember { mutableStateOf(initialFriend?.siblings ?: "") }
    
    var dateOfBirth by remember { mutableStateOf(initialFriend?.dateOfBirth ?: "") }
    var birthDay by remember { mutableStateOf(initialFriend?.birthDay ?: "") }
    var birthMonth by remember { mutableStateOf(initialFriend?.birthMonth ?: "") }
    
    var partnerDateOfBirth by remember { mutableStateOf(initialFriend?.partnerDateOfBirth ?: "") }
    var partnerBirthDay by remember { mutableStateOf(initialFriend?.partnerBirthDay ?: "") }
    var partnerBirthMonth by remember { mutableStateOf(initialFriend?.partnerBirthMonth ?: "") }
    
    var anniversaryDate by remember { mutableStateOf(initialFriend?.anniversaryDate ?: "") }
    var anniversaryDay by remember { mutableStateOf(initialFriend?.anniversaryDay ?: "") }
    var anniversaryMonth by remember { mutableStateOf(initialFriend?.anniversaryMonth ?: "") }

    var notes by remember { mutableStateOf(initialFriend?.notes ?: "") }
    var imageUri by remember { mutableStateOf(initialFriend?.imageUri) }
    var partnerImageUri by remember { mutableStateOf(initialFriend?.partnerImageUri) }
    var petName by remember { mutableStateOf(initialFriend?.petName ?: "") }
    var petImageUri by remember { mutableStateOf(initialFriend?.petImageUri) }
    
    var focusNewChildTrigger by remember { mutableStateOf(false) }
    var showMore by remember { mutableStateOf(false) }

    val isEmailValid = email.isBlank() || android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    
    val isDobValid = isDayValidForMonth(birthDay, birthMonth)
    val isAnniversaryValid = isDayValidForMonth(anniversaryDay, anniversaryMonth)
    val isPartnerDobValid = isDayValidForMonth(partnerBirthDay, partnerBirthMonth)
    
    val isFormValid = isEmailValid && isDobValid && isAnniversaryValid && isPartnerDobValid
    
    val selectedGroups = remember { mutableStateListOf<String>().apply { 
        if (initialFriend != null) addAll(initialFriend.groups) else add("Friend")
    } }
    
    // Synchronize selectedGroups with initialFriend updates
    LaunchedEffect(initialFriend?.groups) {
        if (initialFriend != null) {
            selectedGroups.clear()
            selectedGroups.addAll(initialFriend.groups)
        }
    }
    
    var showGroupDialog by remember { mutableStateOf(false) }

    val children = remember { mutableStateListOf<Child>().apply { addAll(initialChildren) } }

    val cropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                imageUri = UCrop.getOutput(result.data!!)?.toString()
            }
        }
    )

    val friendImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                val destinationUri = Uri.fromFile(File(context.cacheDir, "crop_${System.currentTimeMillis()}.jpg"))
                val uCrop = UCrop.of(it, destinationUri)
                    .withAspectRatio(1f, 1f)
                    .withMaxResultSize(500, 500)
                
                cropLauncher.launch(uCrop.getIntent(context))
            }
        }
    )

    val partnerCropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                partnerImageUri = UCrop.getOutput(result.data!!)?.toString()
            }
        }
    )

    val friendPetCropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                petImageUri = UCrop.getOutput(result.data!!)?.toString()
            }
        }
    )

    val partnerImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                val destinationUri = Uri.fromFile(File(context.cacheDir, "crop_partner_${System.currentTimeMillis()}.jpg"))
                val uCrop = UCrop.of(it, destinationUri)
                    .withAspectRatio(1f, 1f)
                    .withMaxResultSize(500, 500)
                
                partnerCropLauncher.launch(uCrop.getIntent(context))
            }
        }
    )

    val friendPetImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                val destinationUri = Uri.fromFile(File(context.cacheDir, "crop_pet_${System.currentTimeMillis()}.jpg"))
                val uCrop = UCrop.of(it, destinationUri)
                    .withAspectRatio(1f, 1f)
                    .withMaxResultSize(500, 500)
                
                friendPetCropLauncher.launch(uCrop.getIntent(context))
            }
        }
    )

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact(),
        onResult = { uri ->
            try {
                uri?.let {
                    val cursor = context.contentResolver.query(it, null, null, null, null)
                    cursor?.use { c ->
                        if (c.moveToFirst()) {
                            val idIndex = c.getColumnIndex(ContactsContract.Contacts._ID)
                            val contactId = if (idIndex >= 0) c.getString(idIndex) else null
                            
                            val nameIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                            val name = if (nameIndex >= 0) c.getString(nameIndex) else ""
                            
                            var importedFirstName = ""
                            var importedMiddleName = ""
                            var importedLastName = ""
                            var importedCellPhone = ""
                            var importedOfficePhone = ""
                            var importedEmail = ""
                            var importedWorkEmail = ""
                            var importedAddress = ""
                            var importedCompanyName = ""
                            var importedNotes = ""
                            var importedDateOfBirth = ""
                            var importedBirthDay = ""
                            var importedBirthMonth = ""
                            var importedAnniversaryDate = ""
                            var importedAnniversaryDay = ""
                            var importedAnniversaryMonth = ""

                            if (name.isNotBlank()) {
                                val parts = name.split(" ")
                                importedFirstName = parts.firstOrNull() ?: ""
                                importedLastName = if (parts.size > 1) parts.last() else ""
                                importedMiddleName = if (parts.size > 2) parts.subList(1, parts.size - 1).joinToString(" ") else ""
                            }
                            
                            contactId?.let { cid ->
                                // Structured Name
                                context.contentResolver.query(
                                    ContactsContract.Data.CONTENT_URI,
                                    null,
                                    "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                                    arrayOf(cid, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE),
                                    null
                                )?.use { sn ->
                                    if (sn.moveToFirst()) {
                                        val fnIdx = sn.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME)
                                        val mnIdx = sn.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME)
                                        val lnIdx = sn.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME)
                                        if (fnIdx >= 0) sn.getString(fnIdx)?.let { importedFirstName = it }
                                        if (mnIdx >= 0) sn.getString(mnIdx)?.let { importedMiddleName = it }
                                        if (lnIdx >= 0) sn.getString(lnIdx)?.let { importedLastName = it }
                                    }
                                }

                                // Phone Numbers
                                context.contentResolver.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                                    arrayOf(cid),
                                    null
                                )?.use { pc ->
                                    val numIdx = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                    val typeIdx = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
                                    while (pc.moveToNext()) {
                                        val number = if (numIdx >= 0) pc.getString(numIdx) ?: "" else ""
                                        val type = if (typeIdx >= 0) pc.getInt(typeIdx) else -1
                                        if (number.isNotBlank()) {
                                            when (type) {
                                                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> importedCellPhone = number
                                                ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> importedOfficePhone = number
                                                else -> if (importedCellPhone.isBlank()) importedCellPhone = number
                                            }
                                        }
                                    }
                                }

                                // Emails
                                context.contentResolver.query(
                                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                    null,
                                    "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
                                    arrayOf(cid),
                                    null
                                )?.use { ec ->
                                    val addrIdx = ec.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
                                    val typeIdx = ec.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE)
                                    while (ec.moveToNext()) {
                                        val addr = if (addrIdx >= 0) ec.getString(addrIdx) ?: "" else ""
                                        val type = if (typeIdx >= 0) ec.getInt(typeIdx) else -1
                                        if (addr.isNotBlank()) {
                                            when (type) {
                                                ContactsContract.CommonDataKinds.Email.TYPE_HOME -> importedEmail = addr
                                                ContactsContract.CommonDataKinds.Email.TYPE_WORK -> importedWorkEmail = addr
                                                else -> if (importedEmail.isBlank()) importedEmail = addr
                                            }
                                        }
                                    }
                                }

                                // Address
                                context.contentResolver.query(
                                    ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
                                    null,
                                    "${ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID} = ?",
                                    arrayOf(cid),
                                    null
                                )?.use { ac ->
                                    val addrIdx = ac.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS)
                                    if (ac.moveToFirst() && addrIdx >= 0) {
                                        importedAddress = ac.getString(addrIdx) ?: ""
                                    }
                                }

                                // Organization
                                context.contentResolver.query(
                                    ContactsContract.Data.CONTENT_URI,
                                    null,
                                    "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                                    arrayOf(cid, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE),
                                    null
                                )?.use { oc ->
                                    val compIdx = oc.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY)
                                    if (oc.moveToFirst() && compIdx >= 0) {
                                        importedCompanyName = oc.getString(compIdx) ?: ""
                                    }
                                }

                                // Notes
                                context.contentResolver.query(
                                    ContactsContract.Data.CONTENT_URI,
                                    null,
                                    "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                                    arrayOf(cid, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE),
                                    null
                                )?.use { nc ->
                                    val noteIdx = nc.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE)
                                    if (nc.moveToFirst() && noteIdx >= 0) {
                                        importedNotes = nc.getString(noteIdx) ?: ""
                                    }
                                }

                                // Events
                                context.contentResolver.query(
                                    ContactsContract.Data.CONTENT_URI,
                                    null,
                                    "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                                    arrayOf(cid, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE),
                                    null
                                )?.use { ec ->
                                    val typeIdx = ec.getColumnIndex(ContactsContract.CommonDataKinds.Event.TYPE)
                                    val dateIdx = ec.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE)
                                    while (ec.moveToNext()) {
                                        val type = if (typeIdx >= 0) ec.getInt(typeIdx) else -1
                                        val date = if (dateIdx >= 0) ec.getString(dateIdx) ?: "" else ""
                                        if (date.isNotBlank()) {
                                            when (type) {
                                                ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY -> {
                                                    importedDateOfBirth = date
                                                    parseDateToDayMonth(date)?.let { (d, m) ->
                                                        importedBirthDay = d
                                                        importedBirthMonth = m
                                                    }
                                                }
                                                ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY -> {
                                                    importedAnniversaryDate = date
                                                    parseDateToDayMonth(date)?.let { (d, m) ->
                                                        importedAnniversaryDay = d
                                                        importedAnniversaryMonth = m
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Show selection dialog
                            val options = mutableListOf<String>().apply {
                                add("Main Profile")
                                add("Partner Section")
                                children.forEachIndexed { i, child ->
                                    add("Child ${i + 1}: ${if (child.firstName.isNotBlank()) child.firstName else "Empty"}")
                                    add("Child ${i + 1}'s Partner")
                                }
                            }

                            (context as? androidx.activity.ComponentActivity)?.let { activity ->
                                val builder = android.app.AlertDialog.Builder(activity)
                                builder.setTitle("Where to import data?")
                                builder.setItems(options.toTypedArray()) { _, which ->
                                    when (which) {
                                        0 -> { // Main Profile
                                            firstName = importedFirstName
                                            middleName = importedMiddleName
                                            lastName = importedLastName
                                            cellPhone = importedCellPhone
                                            officePhone = importedOfficePhone
                                            email = importedEmail
                                            workEmail = importedWorkEmail
                                            address = importedAddress
                                            companyName = importedCompanyName
                                            notes = importedNotes
                                            dateOfBirth = importedDateOfBirth
                                            birthDay = importedBirthDay
                                            birthMonth = importedBirthMonth
                                            anniversaryDate = importedAnniversaryDate
                                            anniversaryDay = importedAnniversaryDay
                                            anniversaryMonth = importedAnniversaryMonth
                                        }
                                        1 -> { // Partner Section
                                            partnerFirstName = importedFirstName
                                            partnerMiddleName = importedMiddleName
                                            partnerLastName = importedLastName
                                            partnerPhone = importedCellPhone
                                            partnerEmail = importedEmail
                                            partnerWorkEmail = importedWorkEmail
                                            partnerCompanyName = importedCompanyName
                                            partnerDateOfBirth = importedDateOfBirth
                                            partnerBirthDay = importedBirthDay
                                            partnerBirthMonth = importedBirthMonth
                                        }
                                        else -> {
                                            val childIndex = (which - 2) / 2
                                            val isPartner = (which - 2) % 2 != 0
                                            if (childIndex < children.size) {
                                                val child = children[childIndex]
                                                if (isPartner) {
                                                    children[childIndex] = child.copy(
                                                        partnerFirstName = importedFirstName,
                                                        partnerMiddleName = importedMiddleName,
                                                        partnerLastName = importedLastName,
                                                        partnerPhone = importedCellPhone,
                                                        partnerEmail = importedEmail,
                                                        partnerWorkEmail = importedWorkEmail,
                                                        partnerDateOfBirth = importedDateOfBirth,
                                                        partnerBirthDay = importedBirthDay,
                                                        partnerBirthMonth = importedBirthMonth,
                                                        anniversaryDate = importedAnniversaryDate,
                                                        anniversaryDay = importedAnniversaryDay,
                                                        anniversaryMonth = importedAnniversaryMonth
                                                    )
                                                } else {
                                                    children[childIndex] = child.copy(
                                                        firstName = importedFirstName,
                                                        middleName = importedMiddleName,
                                                        lastName = importedLastName,
                                                        phoneNumber = importedCellPhone,
                                                        email = importedEmail,
                                                        workEmail = importedWorkEmail,
                                                        dateOfBirth = importedDateOfBirth,
                                                        birthDay = importedBirthDay,
                                                        birthMonth = importedBirthMonth
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                builder.show()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("CircleKeep", "Error picking contact", e)
            }
        }
    )

    val listState = rememberLazyListState()
    
    LaunchedEffect(scrollToChildId) {
        if (scrollToChildId != null) {
            if (scrollToChildId == -1L) {
                // Scroll to partner section (item index 2)
                listState.animateScrollToItem(2)
            } else {
                val index = children.indexOfFirst { it.id == scrollToChildId }
                if (index != -1) {
                    // Profile pic/core (0), Show more (1), Partner (2), Children header (3), Children (4...)
                    listState.animateScrollToItem(index + 4)
                }
            }
        }
    }

    val contactPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                contactPickerLauncher.launch(null)
            } else {
                android.widget.Toast.makeText(context, "Contact permission is required to import contacts", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(focusNewChildTrigger) {
        if (focusNewChildTrigger && children.isNotEmpty()) {
            // item 0: Profile/Core fields
            // item 1: Show More toggle
            // item 2: Partner section
            // item 3: Children Header
            // items: Children records (starting at index 4)
            listState.animateScrollToItem(children.size + 4)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initialFriend == null) stringResource(R.string.add_friend) else stringResource(R.string.edit_friend)) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.cancel))
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            contactPickerLauncher.launch(null)
                        } else {
                            contactPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
                        }
                    }) {
                        Icon(Icons.Rounded.PersonAdd, contentDescription = "Import from Contacts")
                    }
                    IconButton(
                        onClick = {
                            if (isFormValid && !isSaving) {
                                val friend = Friend(
                                    id = initialFriend?.id ?: 0L,
                                    firstName = firstName,
                                    middleName = middleName,
                                    lastName = lastName,
                                    address = address,
                                    cellPhone = cellPhone,
                                    officePhone = officePhone,
                                    email = email,
                                    partnerFirstName = partnerFirstName,
                                    partnerMiddleName = partnerMiddleName,
                                    partnerLastName = partnerLastName,
                                    partnerPhone = partnerPhone,
                                    partnerType = partnerType,
                                    partnerImageUri = partnerImageUri,
                                    partnerSiblings = partnerSiblings,
                                    partnerCompanyName = partnerCompanyName,
                                    partnerCollegeSchoolName = partnerCollegeSchoolName,
                                    dateOfBirth = dateOfBirth,
                                    birthDay = birthDay,
                                    birthMonth = birthMonth,
                                    partnerDateOfBirth = partnerDateOfBirth,
                                    partnerBirthDay = partnerBirthDay,
                                    partnerBirthMonth = partnerBirthMonth,
                                    anniversaryDate = anniversaryDate,
                                    anniversaryDay = anniversaryDay,
                                    anniversaryMonth = anniversaryMonth,
                                    companyName = companyName,
                                    collegeSchoolName = collegeSchoolName,
                                    siblings = siblings,
                                    groups = selectedGroups.toList(),
                                    isFavorite = initialFriend?.isFavorite ?: false,
                                    isPinned = initialFriend?.isPinned ?: false,
                                    imageUri = imageUri,
                                    petName = petName,
                                    petImageUri = petImageUri,
                                    notes = notes
                                )
                                
                                if (friendCount > 15 && !isPaid) {
                                    isSaving = true
                                    showFullPageAd = true
                                    
                                    val activity = context as? Activity
                                    if (activity != null && interstitialAd != null) {
                                        showInterstitialAd(activity, interstitialAd!!) {
                                            // Ad dismissed
                                        }
                                    }

                                    scope.launch {
                                        for (i in 10 downTo 1) {
                                            saveDelaySeconds = i
                                            kotlinx.coroutines.delay(1000)
                                        }
                                        onSave(friend, children.toList())
                                    }
                                } else {
                                    onSave(friend, children.toList())
                                }
                            }
                        },
                        enabled = isFormValid && !isSaving
                    ) {
                        if (isSaving) {
                            Text("$saveDelaySeconds", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Rounded.Check, contentDescription = stringResource(R.string.save))
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (isSaving && !isPaid) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Please purchase the app to remove ads and save instantly.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .clickable { friendImageLauncher.launch("image/*") },
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                if (imageUri != null) {
                                    AsyncImage(
                                        model = imageUri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.padding(16.dp))
                                }
                            }
                            Row {
                                TextButton(onClick = { friendImageLauncher.launch("image/*") }) {
                                    Text(stringResource(R.string.select_image))
                                }
                                if (imageUri != null) {
                                    TextButton(onClick = { imageUri = null }) {
                                        Text(stringResource(R.string.remove_image), color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = firstName, 
                        onValueChange = { firstName = it }, 
                        label = { Text(stringResource(R.string.first_name)) }, 
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { if (!it.isFocused) firstName = firstName.trim() },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )
                    OutlinedTextField(
                        value = middleName, 
                        onValueChange = { middleName = it }, 
                        label = { Text(stringResource(R.string.middle_name)) }, 
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { if (!it.isFocused) middleName = middleName.trim() },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )
                    OutlinedTextField(
                        value = lastName, 
                        onValueChange = { lastName = it }, 
                        label = { Text(stringResource(R.string.last_name)) }, 
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { if (!it.isFocused) lastName = lastName.trim() },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )
                    OutlinedTextField(
                        value = cellPhone, 
                        onValueChange = { cellPhone = it }, 
                        label = { Text(stringResource(R.string.cell_phone)) }, 
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { if (!it.isFocused) cellPhone = cellPhone.trim() },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    OutlinedTextField(
                        value = officePhone, 
                        onValueChange = { officePhone = it }, 
                        label = { Text(stringResource(R.string.office_phone)) }, 
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { if (!it.isFocused) officePhone = officePhone.trim() },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    OutlinedTextField(
                        value = address, 
                        onValueChange = { address = it }, 
                        label = { Text(stringResource(R.string.address)) }, 
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { if (!it.isFocused) address = address.trim() },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )
                    
                    DatePickerField(
                        value = dateOfBirth,
                        onValueChange = { 
                            dateOfBirth = it
                            if (it.isBlank()) {
                                birthDay = ""
                                birthMonth = ""
                            } else {
                                parseDateToDayMonth(it)?.let { (d, m) ->
                                    birthDay = d
                                    birthMonth = m
                                }
                            }
                        },
                        label = buildString {
                            append(stringResource(R.string.dob))
                            calculateAge(dateOfBirth)?.let { append(" ($it yrs)") }
                        }
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = birthDay, 
                            onValueChange = { birthDay = it }, 
                            label = { Text("Day") }, 
                            modifier = Modifier.weight(0.4f), 
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = !isDobValid,
                            supportingText = { if (!isDobValid) Text("Invalid day") }
                        )
                        MonthDropdown(value = birthMonth, onValueChange = { birthMonth = it }, modifier = Modifier.weight(0.6f))
                    }

                    DatePickerField(
                        value = anniversaryDate,
                        onValueChange = { 
                            anniversaryDate = it
                            if (it.isBlank()) {
                                anniversaryDay = ""
                                anniversaryMonth = ""
                            } else {
                                parseDateToDayMonth(it)?.let { (d, m) ->
                                    anniversaryDay = d
                                    anniversaryMonth = m
                                }
                            }
                        },
                        label = stringResource(R.string.marriage_date)
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = anniversaryDay, 
                            onValueChange = { anniversaryDay = it }, 
                            label = { Text("Day") }, 
                            modifier = Modifier.weight(0.4f), 
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = !isAnniversaryValid,
                            supportingText = { if (!isAnniversaryValid) Text("Invalid day") }
                        )
                        MonthDropdown(value = anniversaryMonth, onValueChange = { anniversaryMonth = it }, modifier = Modifier.weight(0.6f))
                    }

                    Box(modifier = Modifier.fillMaxWidth().clickable { showGroupDialog = true }) {
                        OutlinedTextField(
                            value = selectedGroups.joinToString(", "),
                            onValueChange = { },
                            label = { Text(stringResource(R.string.groups)) },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            enabled = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.outline,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            ),
                            trailingIcon = { Icon(Icons.Rounded.ArrowDropDown, contentDescription = null) }
                        )
                        Box(modifier = Modifier.matchParentSize().background(Color.Transparent).clickable { showGroupDialog = true })
                    }
                }
                
                item {
                    if (showMore) {
                        OutlinedTextField(
                            value = companyName, 
                            onValueChange = { companyName = it }, 
                            label = { Text(stringResource(R.string.company_name)) }, 
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { if (!it.isFocused) companyName = companyName.trim() },
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                        )
                        OutlinedTextField(
                            value = collegeSchoolName, 
                            onValueChange = { collegeSchoolName = it }, 
                            label = { Text(stringResource(R.string.college_name)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { if (!it.isFocused) collegeSchoolName = collegeSchoolName.trim() },
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                        )
                        OutlinedTextField(
                            value = siblings, 
                            onValueChange = { siblings = it }, 
                            label = { Text(stringResource(R.string.siblings)) }, 
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { if (!it.isFocused) siblings = siblings.trim() },
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                        )
                        OutlinedTextField(
                            value = email, 
                            onValueChange = { email = it }, 
                            label = { Text(stringResource(R.string.email)) }, 
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { if (!it.isFocused) email = email.trim() }, 
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            isError = !isEmailValid,
                            supportingText = { if (!isEmailValid) Text("Invalid email format") }
                        )
                        OutlinedTextField(
                            value = workEmail, 
                            onValueChange = { workEmail = it }, 
                            label = { Text("Work Email") }, 
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { if (!it.isFocused) workEmail = workEmail.trim() }, 
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        OutlinedTextField(
                            value = notes, 
                            onValueChange = { notes = it }, 
                            label = { Text(stringResource(R.string.notes)) }, 
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { if (!it.isFocused) notes = notes.trim() }, 
                            minLines = 2,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                        )
                        OutlinedTextField(
                            value = petName,
                            onValueChange = { petName = it },
                            label = { Text(stringResource(R.string.pet_name)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { if (!it.isFocused) petName = petName.trim() },
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .clickable { friendPetImageLauncher.launch("image/*") },
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                if (petImageUri != null) {
                                    AsyncImage(
                                        model = petImageUri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(Icons.Rounded.Pets, contentDescription = null, modifier = Modifier.padding(12.dp))
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Row {
                                TextButton(onClick = { friendPetImageLauncher.launch("image/*") }) {
                                    Text(stringResource(R.string.select_pet_image))
                                }
                                if (petImageUri != null) {
                                    TextButton(onClick = { petImageUri = null }) {
                                        Text(stringResource(R.string.remove_image), color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                    
                    TextButton(
                        onClick = { showMore = !showMore },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (showMore) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (showMore) stringResource(R.string.show_less) else stringResource(R.string.show_more))
                    }
                }

                item {
                    Text(stringResource(R.string.partner), style = MaterialTheme.typography.titleLarge)
                    PartnerSectionEdit(
                        partnerType = partnerType,
                        onPartnerTypeChange = { partnerType = it },
                        partnerFirstName = partnerFirstName,
                        onPartnerFirstNameChange = { partnerFirstName = it },
                        partnerMiddleName = partnerMiddleName,
                        onPartnerMiddleNameChange = { partnerMiddleName = it },
                        partnerLastName = partnerLastName,
                        onPartnerLastNameChange = { partnerLastName = it },
                        partnerPhone = partnerPhone,
                        onPartnerPhoneChange = { partnerPhone = it },
                        partnerDateOfBirth = partnerDateOfBirth,
                        onPartnerDateOfBirthChange = {
                            partnerDateOfBirth = it
                            parseDateToDayMonth(it)?.let { (d, m) ->
                                partnerBirthDay = d
                                partnerBirthMonth = m
                            }
                        },
                        partnerBirthDay = partnerBirthDay,
                        onPartnerBirthDayChange = { partnerBirthDay = it },
                        partnerBirthMonth = partnerBirthMonth,
                        onPartnerBirthMonthChange = { partnerBirthMonth = it },
                        isPartnerDobValid = isPartnerDobValid,
                        partnerSiblings = partnerSiblings,
                        onPartnerSiblingsChange = { partnerSiblings = it },
                        partnerCompanyName = partnerCompanyName,
                        onPartnerCompanyNameChange = { partnerCompanyName = it },
                        partnerCollegeSchoolName = partnerCollegeSchoolName,
                        onPartnerCollegeSchoolNameChange = { partnerCollegeSchoolName = it },
                        partnerImageUri = partnerImageUri,
                        onPartnerImageUriChange = { partnerImageUri = it },
                        partnerImageLauncher = partnerImageLauncher
                    )
                }

                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.children), style = MaterialTheme.typography.titleLarge)
                        TextButton(onClick = {
                            val lastChildEmpty = children.lastOrNull()?.let { 
                                it.firstName.isBlank() && it.lastName.isBlank() 
                            } ?: false
                            
                            if (children.isEmpty() || !lastChildEmpty) {
                                children.add(Child(friendId = initialFriend?.id ?: 0L, firstName = "", lastName = "", collegeSchoolName = ""))
                                focusNewChildTrigger = true
                            }
                        }) {
                            Icon(Icons.Rounded.Add, contentDescription = null)
                            Text("Add Child")
                        }
                    }
                }
                
                items(children.size) { index ->
                    ChildItemEdit(
                        child = children[index],
                        initiallyExpanded = children[index].id == scrollToChildId,
                        shouldAutoFocus = index == children.size - 1 && focusNewChildTrigger,
                        onChildChange = { children[index] = it },
                        onDelete = { children.removeAt(index) }
                    )
                    if (index == children.size - 1 && focusNewChildTrigger) {
                        SideEffect { focusNewChildTrigger = false }
                    }
                }
            }
            
            if (showFullPageAd) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Rounded.AdUnits, contentDescription = null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(24.dp))
                            Text("CircleKeep Ad", style = MaterialTheme.typography.headlineLarge)
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Thank you for using CircleKeep. Large contact lists require a one-time purchase to remove ads and enable instant saving.",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(48.dp))
                            CircularProgressIndicator(
                                progress = { (10f - saveDelaySeconds.toFloat()) / 10f },
                                modifier = Modifier.size(80.dp),
                                strokeWidth = 8.dp
                            )
                            Spacer(Modifier.height(16.dp))
                            Text("Saving in $saveDelaySeconds seconds...", style = MaterialTheme.typography.titleMedium)
                        }
                        Surface(
                            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                "Purchase the app to remove ads and save instantly.",
                                modifier = Modifier.padding(24.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

    if (showGroupDialog) {
        val groups = (availableGroups + "General" + "Family" + "Work" + "School" + "Sports").distinct().sorted()
        AlertDialog(
            onDismissRequest = { showGroupDialog = false },
            title = { Text(stringResource(R.string.select_groups)) },
            text = {
                LazyColumn {
                    items(groups) { group ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedGroups.contains(group),
                                    onClick = {
                                        if (selectedGroups.contains(group)) {
                                            selectedGroups.remove(group)
                                        } else {
                                            selectedGroups.add(group)
                                        }
                                    }
                                )
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedGroups.contains(group),
                                onCheckedChange = null
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(group)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGroupDialog = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
}

@Composable
fun ChildItemEdit(
    child: Child,
    initiallyExpanded: Boolean = false,
    shouldAutoFocus: Boolean = false,
    onChildChange: (Child) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var showMore by remember { mutableStateOf(initiallyExpanded) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(shouldAutoFocus) {
        if (shouldAutoFocus) {
            focusRequester.requestFocus()
        }
    }
    
    val cropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                onChildChange(child.copy(imageUri = UCrop.getOutput(result.data!!)?.toString()))
            }
        }
    )

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> 
            uri?.let {
                val destinationUri = Uri.fromFile(File(context.cacheDir, "crop_child_${System.currentTimeMillis()}.jpg"))
                val uCrop = UCrop.of(it, destinationUri)
                    .withAspectRatio(1f, 1f)
                    .withMaxResultSize(500, 500)
                
                cropLauncher.launch(uCrop.getIntent(context))
            }
        }
    )

    val partnerCropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                onChildChange(child.copy(partnerImageUri = UCrop.getOutput(result.data!!)?.toString()))
            }
        }
    )

    val partnerImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                val destinationUri = Uri.fromFile(File(context.cacheDir, "crop_child_partner_${System.currentTimeMillis()}.jpg"))
                val uCrop = UCrop.of(it, destinationUri)
                    .withAspectRatio(1f, 1f)
                    .withMaxResultSize(500, 500)
                
                partnerCropLauncher.launch(uCrop.getIntent(context))
            }
        }
    )
    
    var showAgeUnitMenu by remember { mutableStateOf(false) }
    
    val isDobValid = isDayValidForMonth(child.birthDay, child.birthMonth)
    val isAnniversaryValid = isDayValidForMonth(child.anniversaryDay, child.anniversaryMonth)
    val isPartnerDobValid = isDayValidForMonth(child.partnerBirthDay, child.partnerBirthMonth)

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable { launcher.launch("image/*") },
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
                            Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.padding(8.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Child", fontWeight = FontWeight.Bold)
                        Row {
                            TextButton(onClick = { launcher.launch("image/*") }) {
                                Text(stringResource(R.string.select_image), style = MaterialTheme.typography.labelSmall)
                            }
                            if (child.imageUri != null) {
                                TextButton(onClick = { onChildChange(child.copy(imageUri = null)) }) {
                                    Text(stringResource(R.string.remove_image), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Rounded.Delete, contentDescription = null)
                }
            }
            OutlinedTextField(
                value = child.firstName, 
                onValueChange = { onChildChange(child.copy(firstName = it)) }, 
                label = { Text(stringResource(R.string.first_name)) }, 
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { if (!it.isFocused) onChildChange(child.copy(firstName = child.firstName.trim())) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            OutlinedTextField(
                value = child.middleName, 
                onValueChange = { onChildChange(child.copy(middleName = it)) }, 
                label = { Text(stringResource(R.string.middle_name)) }, 
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (!it.isFocused) onChildChange(child.copy(middleName = child.middleName.trim())) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            OutlinedTextField(
                value = child.lastName, 
                onValueChange = { onChildChange(child.copy(lastName = it)) }, 
                label = { Text(stringResource(R.string.last_name)) }, 
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (!it.isFocused) onChildChange(child.copy(lastName = child.lastName.trim())) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            OutlinedTextField(
                value = child.phoneNumber, 
                onValueChange = { onChildChange(child.copy(phoneNumber = it)) }, 
                label = { Text(stringResource(R.string.cell_phone)) }, 
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (!it.isFocused) onChildChange(child.copy(phoneNumber = child.phoneNumber.trim())) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            
            if (showMore) {
                OutlinedTextField(
                    value = child.collegeSchoolName, 
                    onValueChange = { onChildChange(child.copy(collegeSchoolName = it)) },
                    label = { Text(stringResource(R.string.college_name)) }, 
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (!it.isFocused) onChildChange(child.copy(collegeSchoolName = child.collegeSchoolName.trim())) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                DatePickerField(
                    value = child.dateOfBirth,
                    onValueChange = { 
                        var newChild = child.copy(dateOfBirth = it)
                        if (it.isBlank()) {
                            newChild = newChild.copy(birthDay = "", birthMonth = "")
                        } else {
                            parseDateToDayMonth(it)?.let { (d, m) ->
                                newChild = newChild.copy(birthDay = d, birthMonth = m)
                            }
                        }
                        onChildChange(newChild)
                    },
                    label = stringResource(R.string.dob)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = child.birthDay, 
                        onValueChange = { onChildChange(child.copy(birthDay = it)) }, 
                        label = { Text("Day") }, 
                        modifier = Modifier.weight(0.4f), 
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = !isDobValid,
                        supportingText = { if (!isDobValid) Text("Invalid day") }
                    )
                    MonthDropdown(value = child.birthMonth, onValueChange = { onChildChange(child.copy(birthMonth = it)) }, modifier = Modifier.weight(0.6f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = child.age?.toString() ?: "",
                        onValueChange = { onChildChange(child.copy(age = it.toIntOrNull())) },
                        label = { Text(stringResource(R.string.age)) },
                        modifier = Modifier.weight(0.4f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    
                    Box(modifier = Modifier.weight(0.6f)) {
                        OutlinedTextField(
                            value = child.ageUnit,
                            onValueChange = { },
                            label = { },
                            modifier = Modifier.fillMaxWidth().clickable { showAgeUnitMenu = true },
                            enabled = false,
                            readOnly = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                            trailingIcon = { Icon(Icons.Rounded.ArrowDropDown, contentDescription = null) }
                        )
                        DropdownMenu(
                            expanded = showAgeUnitMenu,
                            onDismissRequest = { showAgeUnitMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Year(s)") },
                                onClick = {
                                    onChildChange(child.copy(ageUnit = "Year(s)"))
                                    showAgeUnitMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Month(s)") },
                                onClick = {
                                    onChildChange(child.copy(ageUnit = "Month(s)"))
                                    showAgeUnitMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text("Child's Partner", style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        modifier = Modifier.size(40.dp).clip(CircleShape).clickable { partnerImageLauncher.launch("image/*") },
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        if (child.partnerImageUri != null) {
                            AsyncImage(model = child.partnerImageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.padding(8.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row {
                            TextButton(onClick = { partnerImageLauncher.launch("image/*") }) {
                                Text(stringResource(R.string.select_image), style = MaterialTheme.typography.labelSmall)
                            }
                            if (child.partnerImageUri != null) {
                                TextButton(onClick = { onChildChange(child.copy(partnerImageUri = null)) }) {
                                    Text(stringResource(R.string.remove_image), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        PartnerTypeDropdown(value = child.partnerType, onValueChange = { onChildChange(child.copy(partnerType = it)) })
                        OutlinedTextField(
                            value = child.partnerFirstName, 
                            onValueChange = { onChildChange(child.copy(partnerFirstName = it)) }, 
                            label = { Text(stringResource(R.string.first_name)) }, 
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                        )
                        OutlinedTextField(
                            value = child.partnerMiddleName, 
                            onValueChange = { onChildChange(child.copy(partnerMiddleName = it)) }, 
                            label = { Text(stringResource(R.string.middle_name)) }, 
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                        )
                        OutlinedTextField(
                            value = child.partnerLastName, 
                            onValueChange = { onChildChange(child.copy(partnerLastName = it)) }, 
                            label = { Text(stringResource(R.string.last_name)) }, 
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                        )
                        OutlinedTextField(
                            value = child.partnerPhone, 
                            onValueChange = { onChildChange(child.copy(partnerPhone = it)) }, 
                            label = { Text(stringResource(R.string.partner_phone)) }, 
                            modifier = Modifier.fillMaxWidth(), 
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        OutlinedTextField(
                            value = child.partnerSiblings, 
                            onValueChange = { onChildChange(child.copy(partnerSiblings = it)) }, 
                            label = { Text(stringResource(R.string.siblings)) }, 
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                        )
                    }
                }
                DatePickerField(
                    value = child.partnerDateOfBirth,
                    onValueChange = { 
                        var newChild = child.copy(partnerDateOfBirth = it)
                        if (it.isBlank()) {
                            newChild = newChild.copy(partnerBirthDay = "", partnerBirthMonth = "")
                        } else {
                            parseDateToDayMonth(it)?.let { (d, m) ->
                                newChild = newChild.copy(partnerBirthDay = d, partnerBirthMonth = m)
                            }
                        }
                        onChildChange(newChild)
                    },
                    label = "Partner ${stringResource(R.string.dob)}"
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = child.partnerBirthDay, 
                        onValueChange = { onChildChange(child.copy(partnerBirthDay = it)) }, 
                        label = { Text("Day") }, 
                        modifier = Modifier.weight(0.4f), 
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = !isPartnerDobValid,
                        supportingText = { if (!isPartnerDobValid) Text("Invalid day") }
                    )
                    MonthDropdown(value = child.partnerBirthMonth, onValueChange = { onChildChange(child.copy(partnerBirthMonth = it)) }, modifier = Modifier.weight(0.6f))
                }
                DatePickerField(
                    value = child.anniversaryDate,
                    onValueChange = { 
                        var newChild = child.copy(anniversaryDate = it)
                        if (it.isBlank()) {
                            newChild = newChild.copy(anniversaryDay = "", anniversaryMonth = "")
                        } else {
                            parseDateToDayMonth(it)?.let { (d, m) ->
                                newChild = newChild.copy(anniversaryDay = d, anniversaryMonth = m)
                            }
                        }
                        onChildChange(newChild)
                    },
                    label = stringResource(R.string.marriage_date)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = child.anniversaryDay, 
                        onValueChange = { onChildChange(child.copy(anniversaryDay = it)) }, 
                        label = { Text("Day") }, 
                        modifier = Modifier.weight(0.4f), 
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = !isAnniversaryValid,
                        supportingText = { if (!isAnniversaryValid) Text("Invalid day") }
                    )
                    MonthDropdown(value = child.anniversaryMonth, onValueChange = { onChildChange(child.copy(anniversaryMonth = it)) }, modifier = Modifier.weight(0.6f))
                }

                OutlinedTextField(
                    value = child.notes, 
                    onValueChange = { onChildChange(child.copy(notes = it)) }, 
                    label = { Text(stringResource(R.string.notes)) }, 
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = child.petName,
                    onValueChange = { onChildChange(child.copy(petName = it)) },
                    label = { Text(stringResource(R.string.pet_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val cropPetLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult(),
                        onResult = { result ->
                            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                                onChildChange(child.copy(petImageUri = UCrop.getOutput(result.data!!)?.toString()))
                            }
                        }
                    )
                    val petLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent(),
                        onResult = { uri ->
                            uri?.let {
                                val destinationUri = Uri.fromFile(File(context.cacheDir, "crop_child_pet_${System.currentTimeMillis()}.jpg"))
                                val uCrop = UCrop.of(it, destinationUri)
                                    .withAspectRatio(1f, 1f)
                                    .withMaxResultSize(500, 500)
                                cropPetLauncher.launch(uCrop.getIntent(context))
                            }
                        }
                    )
                    Surface(
                        modifier = Modifier.size(48.dp).clip(CircleShape).clickable { petLauncher.launch("image/*") },
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        if (child.petImageUri != null) {
                            AsyncImage(model = child.petImageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Icon(Icons.Rounded.Pets, contentDescription = null, modifier = Modifier.padding(8.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Row {
                        TextButton(onClick = { petLauncher.launch("image/*") }) {
                            Text(stringResource(R.string.select_pet_image), style = MaterialTheme.typography.labelSmall)
                        }
                        if (child.petImageUri != null) {
                            TextButton(onClick = { onChildChange(child.copy(petImageUri = null)) }) {
                                Text(stringResource(R.string.remove_image), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
            
            TextButton(
                onClick = { showMore = !showMore },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (showMore) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(if (showMore) stringResource(R.string.show_less) else stringResource(R.string.show_more))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    val initialDateMillis = remember(value) {
        parseDate(value)?.time ?: System.currentTimeMillis()
    }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis
    )
    
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        cal.timeInMillis = it
                        
                        val year = cal.get(Calendar.YEAR)
                        val month = cal.get(Calendar.MONTH)
                        val day = cal.get(Calendar.DAY_OF_MONTH)
                        
                        val resultCal = Calendar.getInstance()
                        resultCal.set(year, month, day)
                        
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        onValueChange(sdf.format(resultCal.time))
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        onValueChange("")
                        showDatePicker = false
                    }) {
                        Text("Clear")
                    }
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it) },
        label = { Text(label) },
        modifier = modifier.fillMaxWidth().clickable { showDatePicker = true },
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Rounded.CalendarToday, contentDescription = null)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    
    val monthName = if (value.toIntOrNull() in 1..12) months[value.toInt() - 1] else value

    Box(modifier = modifier) {
        OutlinedTextField(
            value = monthName,
            onValueChange = { },
            label = { Text("Month") },
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            enabled = false,
            readOnly = true,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            trailingIcon = {
                Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            months.forEachIndexed { index, month ->
                DropdownMenuItem(
                    text = { Text(month) },
                    onClick = {
                        onValueChange((index + 1).toString())
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerTypeDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val types = listOf("", "Spouse", "Fiance", "Boyfriend", "Girlfriend", "Other")

    Box(modifier = modifier) {
        OutlinedTextField(
            value = if (value == "Fiance") stringResource(R.string.fiance) else value,
            onValueChange = { },
            label = { Text("Partner Type") },
            placeholder = { Text("Select Type") },
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            enabled = false,
            readOnly = true,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            trailingIcon = {
                Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            types.forEach { type ->
                val displayType = when (type) {
                    "Fiance" -> stringResource(R.string.fiance)
                    "" -> "None"
                    else -> type
                }
                DropdownMenuItem(
                    text = { Text(displayType) },
                    onClick = {
                        onValueChange(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun PartnerSectionEdit(
    partnerType: String,
    onPartnerTypeChange: (String) -> Unit,
    partnerFirstName: String,
    onPartnerFirstNameChange: (String) -> Unit,
    partnerMiddleName: String,
    onPartnerMiddleNameChange: (String) -> Unit,
    partnerLastName: String,
    onPartnerLastNameChange: (String) -> Unit,
    partnerPhone: String,
    onPartnerPhoneChange: (String) -> Unit,
    partnerDateOfBirth: String,
    onPartnerDateOfBirthChange: (String) -> Unit,
    partnerBirthDay: String,
    onPartnerBirthDayChange: (String) -> Unit,
    partnerBirthMonth: String,
    onPartnerBirthMonthChange: (String) -> Unit,
    isPartnerDobValid: Boolean,
    partnerSiblings: String,
    onPartnerSiblingsChange: (String) -> Unit,
    partnerCompanyName: String,
    onPartnerCompanyNameChange: (String) -> Unit,
    partnerCollegeSchoolName: String,
    onPartnerCollegeSchoolNameChange: (String) -> Unit,
    partnerImageUri: String?,
    onPartnerImageUriChange: (String?) -> Unit,
    partnerImageLauncher: androidx.activity.result.ActivityResultLauncher<String>
) {
    var showMore by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .clickable { partnerImageLauncher.launch("image/*") },
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    if (partnerImageUri != null) {
                        AsyncImage(
                            model = partnerImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.padding(12.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row {
                        TextButton(onClick = { partnerImageLauncher.launch("image/*") }) {
                            Text(stringResource(R.string.select_image))
                        }
                        if (partnerImageUri != null) {
                            TextButton(onClick = { onPartnerImageUriChange(null) }) {
                                Text(stringResource(R.string.remove_image), color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            PartnerTypeDropdown(value = partnerType, onValueChange = onPartnerTypeChange)
            OutlinedTextField(
                value = partnerFirstName,
                onValueChange = onPartnerFirstNameChange,
                label = { Text(stringResource(R.string.first_name)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (!it.isFocused) onPartnerFirstNameChange(partnerFirstName.trim()) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            OutlinedTextField(
                value = partnerMiddleName,
                onValueChange = onPartnerMiddleNameChange,
                label = { Text(stringResource(R.string.middle_name)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (!it.isFocused) onPartnerMiddleNameChange(partnerMiddleName.trim()) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            OutlinedTextField(
                value = partnerLastName,
                onValueChange = onPartnerLastNameChange,
                label = { Text(stringResource(R.string.last_name)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (!it.isFocused) onPartnerLastNameChange(partnerLastName.trim()) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            OutlinedTextField(
                value = partnerPhone,
                onValueChange = onPartnerPhoneChange,
                label = { Text(stringResource(R.string.partner_phone)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (!it.isFocused) onPartnerPhoneChange(partnerPhone.trim()) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            DatePickerField(
                value = partnerDateOfBirth,
                onValueChange = {
                    onPartnerDateOfBirthChange(it)
                    if (it.isBlank()) {
                        onPartnerBirthDayChange("")
                        onPartnerBirthMonthChange("")
                    } else {
                        parseDateToDayMonth(it)?.let { (d, m) ->
                            onPartnerBirthDayChange(d)
                            onPartnerBirthMonthChange(m)
                        }
                    }
                },
                label = "Partner ${stringResource(R.string.dob)}"
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = partnerBirthDay,
                    onValueChange = onPartnerBirthDayChange,
                    label = { Text("Day") },
                    modifier = Modifier.weight(0.4f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = !isPartnerDobValid,
                    supportingText = { if (!isPartnerDobValid) Text("Invalid day") }
                )
                MonthDropdown(value = partnerBirthMonth, onValueChange = onPartnerBirthMonthChange, modifier = Modifier.weight(0.6f))
            }

            if (showMore) {
                OutlinedTextField(
                    value = partnerSiblings,
                    onValueChange = onPartnerSiblingsChange,
                    label = { Text(stringResource(R.string.siblings)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (!it.isFocused) onPartnerSiblingsChange(partnerSiblings.trim()) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                OutlinedTextField(
                    value = partnerCompanyName,
                    onValueChange = onPartnerCompanyNameChange,
                    label = { Text(stringResource(R.string.company_name)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (!it.isFocused) onPartnerCompanyNameChange(partnerCompanyName.trim()) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                OutlinedTextField(
                    value = partnerCollegeSchoolName,
                    onValueChange = onPartnerCollegeSchoolNameChange,
                    label = { Text(stringResource(R.string.college_name)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (!it.isFocused) onPartnerCollegeSchoolNameChange(partnerCollegeSchoolName.trim()) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
            }

            TextButton(
                onClick = { showMore = !showMore },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (showMore) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(if (showMore) stringResource(R.string.show_less) else stringResource(R.string.show_more))
            }
        }
    }
}

@Composable
fun PlaceholderScreen(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    groups: List<Group>,
    onGroupClick: (String) -> Unit,
    onAddGroup: (String) -> Unit,
    onDeleteGroup: (Group) -> Unit,
    onRenameGroup: (String, String) -> Unit
) {
    val viewModel: FriendViewModel = viewModel(factory = FriendViewModel.Factory)
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
                title = { Text(stringResource(R.string.groups)) },
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
                Text(stringResource(R.string.no_friends))
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(groups) { group ->
                    val count = friends.count { it.friend.groups.contains(group.name) }
                    ListItem(
                        modifier = Modifier.clickable { onGroupClick(group.name) },
                        headlineContent = { Text("${group.name} ($count)") },
                        leadingContent = { Icon(Icons.Rounded.Group, contentDescription = null) },
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
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(stringResource(R.string.cancel))
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
                            onRenameGroup(groupToEdit!!.name, editGroupName)
                            showEditDialog = false
                            groupToEdit = null
                        }
                    }
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text(stringResource(R.string.cancel))
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
                    Text(stringResource(R.string.delete_friend)) // Reusing delete_friend for "Delete" text
                }
            },
            dismissButton = {
                TextButton(onClick = { groupToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: FriendViewModel) {
    val themePreference by viewModel.themeState.collectAsState()
    val isPaid by viewModel.isPaidState.collectAsState()
    val context = LocalContext.current
    var showImportDialog by remember { mutableStateOf(false) }
    var importData by remember { mutableStateOf<String?>(null) }
    
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri ->
            uri?.let {
                val data = viewModel.getExportData()
                context.contentResolver.openOutputStream(it)?.use { output ->
                    output.write(data.toByteArray())
                }
            }
        }
    )
    
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.openInputStream(it)?.use { input ->
                    val data = input.bufferedReader().use { reader -> reader.readText() }
                    importData = data
                    showImportDialog = true
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.settings)) })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text(stringResource(R.string.theme), style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                FilterChip(
                    selected = themePreference == "Light",
                    onClick = { viewModel.onThemeChange("Light") },
                    label = { Text(stringResource(R.string.light)) }
                )
                FilterChip(
                    selected = themePreference == "Dark",
                    onClick = { viewModel.onThemeChange("Dark") },
                    label = { Text(stringResource(R.string.dark)) }
                )
                FilterChip(
                    selected = themePreference == "System",
                    onClick = { viewModel.onThemeChange("System") },
                    label = { Text(stringResource(R.string.system)) }
                )
            }
            
            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            
            if (!isPaid) {
                Button(
                    onClick = { viewModel.purchaseApp() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Icon(Icons.Rounded.ShoppingCart, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Purchase App (Remove Ads)")
                }
                Spacer(Modifier.height(12.dp))
            }

            Button(
                onClick = { 
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    exportLauncher.launch("CircleKeep_Backup_$timeStamp.json") 
                }, 
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.FileUpload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.export_data))
            }
            
            Spacer(Modifier.height(12.dp))
            
            OutlinedButton(onClick = { importLauncher.launch(arrayOf("*/*")) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Rounded.FileDownload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.import_data))
            }

            Spacer(Modifier.height(12.dp))
            
            TextButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:CircleKeepApp@gmail.com")
                        putExtra(Intent.EXTRA_SUBJECT, "CircleKeep Feedback")
                    }
                    context.startActivity(Intent.createChooser(intent, "Send Feedback"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Feedback, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Send Feedback")
            }

            Spacer(Modifier.weight(1f))
            
            ListItem(
                headlineContent = { Text("App Version") },
                supportingContent = { Text("1.2.0") }
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("Developer") },
                supportingContent = { Text("CircleKeep Team") }
            )
            if (isPaid) {
                ListItem(
                    headlineContent = { Text("Status") },
                    supportingContent = { Text("Pro Version Active", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
                )
            }
        }
    }

    if (showImportDialog && importData != null) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text(stringResource(R.string.import_data)) },
            text = { Text(stringResource(R.string.confirm_import)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        importData?.let { viewModel.importData(it) }
                        showImportDialog = false
                        importData = null
                    }
                ) {
                    Text(stringResource(R.string.import_data))
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}


