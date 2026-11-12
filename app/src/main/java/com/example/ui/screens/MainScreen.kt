package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.NotificationItem
import com.example.ui.MainTab
import com.example.ui.NotifyUiState
import com.example.ui.components.GreyButton
import com.example.ui.components.UndoDeletePopup
import com.example.ui.theme.AccentRed
import com.example.ui.theme.ButtonGrey
import com.example.ui.theme.ButtonGreyBorder
import com.example.ui.theme.ButtonText
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
  uiState: NotifyUiState,
  onTabSelected: (MainTab) -> Unit,
  onOpenCreate: () -> Unit,
  onOpenEdit: (NotificationItem) -> Unit,
  onDismissEditor: () -> Unit,
  onSaveNotification: (NotificationItem) -> Unit,
  onToggleEnabled: (Long, Boolean) -> Unit,
  onDeleteNotification: (NotificationItem) -> Unit,
  onUndoDelete: () -> Unit,
  onTestSend: (NotificationItem) -> Unit,
  onOpenSettingsDialog: () -> Unit,
  onDismissSettingsDialog: () -> Unit,
  onStopApp: (String, String, Long) -> Unit,
  onRestartApp: () -> Unit
) {
  val context = LocalContext.current

  // Permission handling
  var hasNotificationPermission by remember {
    mutableStateOf(
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
      } else {
        true
      }
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasNotificationPermission = isGranted
  }

  // Filter out pending deleted item so it's visually hidden immediately
  val visibleNotifications = remember(uiState.notifications, uiState.pendingDeletedItem) {
    uiState.notifications.filter { it.id != uiState.pendingDeletedItem?.id }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = PureBlack,
    contentColor = TextPrimary,
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    topBar = {
      TopAppBar(
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(ButtonGrey),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                tint = PureWhite,
                modifier = Modifier.size(16.dp)
              )
            }

            Text(
              text = "Notify",
              color = PureWhite,
              fontSize = 17.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        },
        actions = {
          // Settings button (renamed from Resume)
          Surface(
            modifier = Modifier
              .clickable { onOpenSettingsDialog() }
              .padding(end = 12.dp)
              .testTag("settings_top_button"),
            shape = RoundedCornerShape(6.dp),
            color = if (uiState.appControl.isStopped) Color(0xFF3E1F1F) else ButtonGrey,
            border = BorderStroke(1.dp, if (uiState.appControl.isStopped) AccentRed.copy(alpha = 0.5f) else ButtonGreyBorder)
          ) {
            Text(
              text = if (uiState.appControl.isStopped) "Stopped" else "Settings",
              color = if (uiState.appControl.isStopped) AccentRed else ButtonText,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = PureBlack,
          scrolledContainerColor = DarkSurface
        )
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = onOpenCreate,
        containerColor = ButtonGrey,
        contentColor = PureWhite,
        shape = CircleShape,
        modifier = Modifier.testTag("add_notification_button")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(24.dp))
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // Notification permission request if not granted
        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(DarkSurfaceVariant)
              .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Enable notifications",
              color = TextSecondary,
              fontSize = 12.sp
            )

            GreyButton(
              text = "Allow",
              onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
              testTag = "allow_permission_button"
            )
          }
        }

        // Two tabs only: Scheduled and Day Alerts
        val tabs = listOf(
          MainTab.SCHEDULED to "Scheduled",
          MainTab.DAY_ALERTS to "Day Alerts"
        )

        TabRow(
          selectedTabIndex = uiState.selectedTab.ordinal,
          containerColor = PureBlack,
          contentColor = PureWhite,
          divider = {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DarkBorder)
            )
          },
          indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
              modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab.ordinal]),
              color = PureWhite,
              height = 2.dp
            )
          }
        ) {
          tabs.forEach { (tab, title) ->
            val isSelected = uiState.selectedTab == tab
            Tab(
              selected = isSelected,
              onClick = { onTabSelected(tab) },
              text = {
                Text(
                  text = title,
                  color = if (isSelected) PureWhite else TextSecondary,
                  fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                  fontSize = 13.sp
                )
              },
              modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Content
        when (uiState.selectedTab) {
          MainTab.SCHEDULED -> {
            SchedulesScreen(
              notifications = visibleNotifications,
              onToggleEnabled = onToggleEnabled,
              onEdit = onOpenEdit,
              onDelete = onDeleteNotification,
              onCreateNew = onOpenCreate
            )
          }

          MainTab.DAY_ALERTS -> {
            DayAlertsScreen(
              notifications = visibleNotifications,
              onToggleEnabled = onToggleEnabled,
              onEdit = onOpenEdit,
              onDelete = onDeleteNotification,
              onCreateNew = onOpenCreate
            )
          }
        }
      }

      // 3-second "Deleted" pop-up with "Undo" button
      UndoDeletePopup(
        visible = uiState.pendingDeletedItem != null,
        onUndo = onUndoDelete,
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = 76.dp)
      )
    }
  }

  // Notification Editor
  if (uiState.isEditorOpen) {
    NotificationEditorSheet(
      itemToEdit = uiState.editingItem,
      initialType = uiState.editorInitialType,
      existingNotifications = uiState.notifications,
      onDismiss = onDismissEditor,
      onSave = onSaveNotification,
      onTestSend = onTestSend
    )
  }

  // Settings Dialog (fast lightweight modal)
  if (uiState.isSettingsDialogOpen) {
    SettingsDialog(
      appControl = uiState.appControl,
      onStop = onStopApp,
      onRestart = onRestartApp,
      onDismiss = onDismissSettingsDialog
    )
  }
}
