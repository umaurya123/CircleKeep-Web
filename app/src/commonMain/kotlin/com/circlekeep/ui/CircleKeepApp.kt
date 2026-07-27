package com.circlekeep.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.circlekeep.*
import com.circlekeep.navigation.Destination
import com.circlekeep.ui.theme.CircleKeepTheme
import com.circlekeep.ui.components.CircleKeepBottomBar
import com.circlekeep.ui.screens.*
import com.circlekeep.viewmodel.FriendViewModel
import com.circlekeep.viewmodel.getFriendViewModelFactory

@Composable
fun CircleKeepApp() {
    val viewModel: FriendViewModel = viewModel(factory = getFriendViewModelFactory())
    val themePreference by viewModel.themeState.collectAsState()
    val platformUI = rememberPlatformUI()
    val platform = getPlatform()
    val navController = rememberNavController()

    // Handle back button on top-level screens to prevent accidental exit
    var lastBackPressTime by remember { mutableStateOf(0L) }
    
    PlatformBackHandler(enabled = true) {
        if (navController.previousBackStackEntry != null) {
            navController.popBackStack()
        } else {
            val currentTime = platform.currentTimeMillis()
            if (currentTime - lastBackPressTime < 2000) {
                platformUI.exitApp()
            } else {
                lastBackPressTime = currentTime
                platformUI.showToast("Press back again to exit")
            }
        }
    }

    CircleKeepTheme(themePreference = themePreference) {
        CompositionLocalProvider(LocalPlatformUI provides platformUI) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: ""
            
            val barDestination = when {
                currentRoute.contains("Favorites") -> Destination.Favorites
                currentRoute.contains("Groups") -> Destination.Groups
                currentRoute.contains("Settings") -> Destination.Settings
                else -> Destination.Home
            }

        Column(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    val friends by viewModel.friendsState.collectAsState()
                    val isPaid by viewModel.isPaidState.collectAsState()
                    
                    Column {
                        if (!isPaid) {
                            BannerAdView()
                        }
                        CircleKeepBottomBar(
                            currentDestination = barDestination,
                            friendCount = friends.size,
                            favoriteCount = friends.count { it.friend.isFavorite },
                            onNavigate = { destination ->
                                if (destination == Destination.Home && barDestination == Destination.Home) {
                                    viewModel.clearFilters()
                                }
                                navController.navigate(destination) {
                                    popUpTo(Destination.Home) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                },
                contentWindowInsets = WindowInsets(0, 0, 0, 0)
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = Destination.Home,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable<Destination.Home> {
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
                                    navController.navigate(Destination.FriendDetail(id))
                                },
                                onEditFriendClick = { id, childId ->
                                    navController.navigate(Destination.EditFriend(id, childId))
                                },
                                onAddFriendClick = {
                                    navController.navigate(Destination.AddFriend)
                                },
                                onToggleFavorite = viewModel::toggleFavorite,
                                onTogglePin = viewModel::togglePin
                            )
                        }
                        composable<Destination.FriendDetail> { backStackEntry ->
                            val detail: Destination.FriendDetail = backStackEntry.toRoute()
                            val friendWithChildren by viewModel.getFriend(detail.friendId).collectAsState(initial = null)
                            
                            FriendDetailScreen(
                                friendWithChildren = friendWithChildren,
                                onEditClick = { id, childId ->
                                    navController.navigate(Destination.EditFriend(id, childId))
                                },
                                onDeleteClick = { friend ->
                                    viewModel.deleteFriend(friend)
                                    navController.popBackStack()
                                },
                                onTogglePin = { friend -> viewModel.togglePin(friend) },
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable<Destination.AddFriend> {
                            val groups by viewModel.groupsState.collectAsState()
                            val friends by viewModel.friendsState.collectAsState()
                            val isPaid by viewModel.isPaidState.collectAsState()

                            AddEditFriendScreen(
                                availableGroups = groups.map { it.name },
                                friendCount = friends.size,
                                isPaid = isPaid,
                                onSave = { friend, children ->
                                    viewModel.saveFriend(friend, children)
                                    navController.popBackStack()
                                },
                                onCancel = { navController.popBackStack() }
                            )
                        }
                        composable<Destination.EditFriend> { backStackEntry ->
                            val edit: Destination.EditFriend = backStackEntry.toRoute()
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
                                        navController.popBackStack()
                                    },
                                    onCancel = { navController.popBackStack() }
                                )
                            }
                        }
                        composable<Destination.Favorites> {
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
                                onFriendClick = { id -> navController.navigate(Destination.FriendDetail(id)) },
                                onEditFriendClick = { id, childId -> navController.navigate(Destination.EditFriend(id, childId)) },
                                onAddFriendClick = { navController.navigate(Destination.AddFriend) },
                                onToggleFavorite = viewModel::toggleFavorite,
                                onTogglePin = viewModel::togglePin
                            )
                        }
                        composable<Destination.Groups> {
                            val groups by viewModel.groupsState.collectAsState()
                            
                            GroupsScreen(
                                viewModel = viewModel,
                                groups = groups,
                                onGroupClick = { groupName ->
                                    viewModel.onSelectedGroupChange(groupName)
                                    navController.navigate(Destination.Home) {
                                        popUpTo(Destination.Home) { inclusive = true }
                                    }
                                },
                                onAddGroup = viewModel::addGroup,
                                onDeleteGroup = viewModel::deleteGroup,
                                onRenameGroup = viewModel::renameGroup
                            )
                        }
                        composable<Destination.Settings> {
                            SettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
