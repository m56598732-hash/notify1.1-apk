package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.MainTab
import com.example.ui.NotificationViewModel
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  private val viewModel: NotificationViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    if (intent?.getStringExtra("OPEN_TAB") == "DAILY") {
      viewModel.setTab(MainTab.DAY_ALERTS)
    }

    setContent {
      MyApplicationTheme {
        val uiState by viewModel.uiState.collectAsState()

        BackHandler(enabled = uiState.isEditorOpen || uiState.isSettingsDialogOpen) {
          if (uiState.isEditorOpen) {
            viewModel.dismissEditor()
          } else if (uiState.isSettingsDialogOpen) {
            viewModel.dismissSettingsDialog()
          }
        }

        MainScreen(
          uiState = uiState,
          onTabSelected = viewModel::setTab,
          onOpenCreate = viewModel::openCreate,
          onOpenEdit = viewModel::openEdit,
          onDismissEditor = viewModel::dismissEditor,
          onSaveNotification = viewModel::saveNotification,
          onToggleEnabled = viewModel::toggleNotification,
          onDeleteNotification = viewModel::requestDeleteNotification,
          onUndoDelete = viewModel::undoDelete,
          onTestSend = viewModel::testSendNow,
          onOpenSettingsDialog = viewModel::openSettingsDialog,
          onDismissSettingsDialog = viewModel::dismissSettingsDialog,
          onStopApp = viewModel::stopApp,
          onRestartApp = viewModel::restartApp
        )
      }
    }
  }
}
