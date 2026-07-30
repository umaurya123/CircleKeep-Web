package com.circlekeep

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.circlekeep.ui.screens.AddEditFriendScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class AddEditFriendScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAddFriend_basicInputAndSave() {
        var saved = false
        composeTestRule.setContent {
            CompositionLocalProvider(LocalPlatformUI provides rememberPlatformUI()) {
                AddEditFriendScreen(
                    onSave = { _, _ -> saved = true },
                    onCancel = {}
                )
            }
        }

        // Verify "Add Friend" title exists
        composeTestRule.onNodeWithText("Add Friend").assertIsDisplayed()

        // Input First Name (Required)
        composeTestRule.onNodeWithText("First Name").performTextInput("John")

        // Click Save icon
        composeTestRule.onNodeWithContentDescription("Save").performClick()

        assert(saved)
    }

    @Test
    fun testValidation_InvalidEmailDisablesSave() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalPlatformUI provides rememberPlatformUI()) {
                AddEditFriendScreen(
                    onSave = { _, _ -> },
                    onCancel = {}
                )
            }
        }

        // Enter a valid name
        composeTestRule.onNodeWithText("First Name").performTextInput("John")
        
        // Scroll to "Show More" and click it
        composeTestRule.onNodeWithTag("MainList").performScrollToNode(hasText("Show More"))
        composeTestRule.onAllNodesWithText("Show More")[0].performClick()
        
        // Scroll to Email field
        composeTestRule.onNodeWithTag("MainList").performScrollToNode(hasText("Email"))
        
        // Enter an invalid email (no @)
        composeTestRule.onNodeWithText("Email").performTextInput("invalid-email")

        // Check if Save button is disabled
        composeTestRule.onNodeWithContentDescription("Save").assertIsNotEnabled()

        // Fix the email
        composeTestRule.onNodeWithText("Email").performTextReplacement("john@example.com")

        // Check if Save button is enabled
        composeTestRule.onNodeWithContentDescription("Save").assertIsEnabled()
    }

    @Test
    fun testValidation_InvalidDayShowsError() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalPlatformUI provides rememberPlatformUI()) {
                AddEditFriendScreen(
                    onSave = { _, _ -> },
                    onCancel = {}
                )
            }
        }

        // Open Month picker for Birthday.
        composeTestRule.onAllNodesWithText("Month")[0].performClick()
        
        // Wait for dialog and select February
        composeTestRule.onNodeWithText("February").performClick()

        // Enter 30 in the Day field
        composeTestRule.onAllNodesWithText("Day")[0].performTextInput("30")

        // Verify error message appears
        composeTestRule.onNodeWithText("Invalid day").assertIsDisplayed()
        
        // Save button should be disabled
        composeTestRule.onNodeWithContentDescription("Save").assertIsNotEnabled()
    }

    @Test
    fun testSections_ShowMoreToggle() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalPlatformUI provides rememberPlatformUI()) {
                AddEditFriendScreen(
                    onSave = { _, _ -> },
                    onCancel = {}
                )
            }
        }

        // Scroll to "Partner" to ensure the partner section's Company Name is available in the tree
        composeTestRule.onNodeWithTag("MainList").performScrollToNode(hasText("Partner"))

        // Initially only 1 "Company Name" should exist (in Partner section)
        composeTestRule.onAllNodesWithText("Company Name").assertCountEquals(1)

        // Scroll back to "Show More" and click it
        composeTestRule.onNodeWithTag("MainList").performScrollToNode(hasText("Show More"))
        composeTestRule.onAllNodesWithText("Show More")[0].performClick()

        // Now 2 "Company Name" nodes should exist (one in main, one in partner)
        composeTestRule.onAllNodesWithText("Company Name").assertCountEquals(2)

        // Click "Show Less"
        composeTestRule.onNodeWithText("Show Less").performClick()

        // Should be back to 1
        composeTestRule.onAllNodesWithText("Company Name").assertCountEquals(1)
    }

    @Test
    fun testGroups_Selection() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalPlatformUI provides rememberPlatformUI()) {
                AddEditFriendScreen(
                    onSave = { _, _ -> },
                    onCancel = {},
                    availableGroups = listOf("Gym", "Hiking")
                )
            }
        }

        // Target the Groups field. 
        // We use a broader search to find the clickable area around "Groups".
        composeTestRule.onNodeWithText("Groups").performClick()

        // Select groups in the dialog
        composeTestRule.onNodeWithText("Gym").performClick()
        composeTestRule.onNodeWithText("Family").performClick()

        // Close dialog
        composeTestRule.onNodeWithText("OK").performClick()

        // Verify groups are displayed in the field
        composeTestRule.onNodeWithText("Gym, Family").assertIsDisplayed()
    }

    @Test
    fun testAddChild_increasesList() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalPlatformUI provides rememberPlatformUI()) {
                AddEditFriendScreen(
                    onSave = { _, _ -> },
                    onCancel = {}
                )
            }
        }

        // Scroll to find the "Add Child" button
        composeTestRule.onNodeWithTag("MainList").performScrollToNode(hasText("Add Child"))
        
        // Click the button
        composeTestRule.onNodeWithText("Add Child").performClick()

        // Verify a new child section appears by checking for the Delete icon
        composeTestRule.onNodeWithContentDescription("Delete Child").assertExists()
    }

    @Test
    fun testImagePicker_TriggersOnAvatarClick() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalPlatformUI provides rememberPlatformUI()) {
                AddEditFriendScreen(
                    onSave = { _, _ -> },
                    onCancel = {}
                )
            }
        }

        // Clicking the main avatar should be possible
        composeTestRule.onNodeWithText("Select Image").performClick()
    }

    @Test
    fun testTabNavigation() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalPlatformUI provides rememberPlatformUI()) {
                AddEditFriendScreen(
                    onSave = { _, _ -> },
                    onCancel = {}
                )
            }
        }

        // Focus First Name
        composeTestRule.onNodeWithText("First Name").performClick()
        composeTestRule.onNodeWithText("First Name").assertIsFocused()

        // Simulate Tab key press
        composeTestRule.onRoot().performKeyPress(
            androidx.compose.ui.input.key.KeyEvent(
                android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_TAB)
            )
        )
        
        // Middle Name should be focused
        composeTestRule.onNodeWithText("Middle Name").assertIsFocused()
    }
}
