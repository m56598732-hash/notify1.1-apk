package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppControlState
import com.example.data.model.NotificationItem
import com.example.data.model.NotificationType
import com.example.data.repository.NotificationRepository
import com.example.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
  SCHEDULED,
  DAY_ALERTS
}

data class NotifyUiState(
  val notifications: List<NotificationItem> = emptyList(),
  val appControl: AppControlState = AppControlState(),
  val selectedTab: MainTab = MainTab.SCHEDULED,
  val isEditorOpen: Boolean = false,
  val editingItem: NotificationItem? = null,
  val editorInitialType: NotificationType = NotificationType.TEMPORARY,
  val isSettingsDialogOpen: Boolean = false,
  val pendingDeletedItem: NotificationItem? = null
)

private data class UiControlsState(
  val tab: MainTab,
  val isEditorOpen: Boolean,
  val editingItem: NotificationItem?,
  val initialType: NotificationType,
  val isSettingsOpen: Boolean,
  val pendingDeletedItem: NotificationItem?
)

class NotificationViewModel(application: Application) : AndroidViewModel(application) {

  private val repository by lazy { NotificationRepository(application) }

  private val _selectedTab = MutableStateFlow(MainTab.SCHEDULED)
  private val _isEditorOpen = MutableStateFlow(false)
  private val _editingItem = MutableStateFlow<NotificationItem?>(null)
  private val _editorInitialType = MutableStateFlow(NotificationType.TEMPORARY)
  private val _isSettingsDialogOpen = MutableStateFlow(false)
  private val _pendingDeletedItem = MutableStateFlow<NotificationItem?>(null)

  private var pendingDeleteJob: Job? = null

  // Combine all local UI state into a single reactive flow so any change triggers state emission
  private val uiControlsFlow = combine(
    _selectedTab,
    _isEditorOpen,
    _editingItem,
    _editorInitialType
  ) { tab, editorOpen, editing, initialType ->
    Triple(tab, editorOpen to editing, initialType)
  }.combine(
    combine(_isSettingsDialogOpen, _pendingDeletedItem) { settingsOpen, pendingDeleted ->
      settingsOpen to pendingDeleted
    }
  ) { (tab, editorPair, initialType), (settingsOpen, pendingDeleted) ->
    UiControlsState(
      tab = tab,
      isEditorOpen = editorPair.first,
      editingItem = editorPair.second,
      initialType = initialType,
      isSettingsOpen = settingsOpen,
      pendingDeletedItem = pendingDeleted
    )
  }

  val uiState: StateFlow<NotifyUiState> = combine(
    repository.observeNotifications(),
    repository.observeAppControl(),
    uiControlsFlow
  ) { notifications, appControl, controls ->
    NotifyUiState(
      notifications = notifications,
      appControl = appControl ?: AppControlState(),
      selectedTab = controls.tab,
      isEditorOpen = controls.isEditorOpen,
      editingItem = controls.editingItem,
      editorInitialType = controls.initialType,
      isSettingsDialogOpen = controls.isSettingsOpen,
      pendingDeletedItem = controls.pendingDeletedItem
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = NotifyUiState()
  )

  init {
    viewModelScope.launch(Dispatchers.IO) {
      NotificationHelper.createNotificationChannels(getApplication())
    }
  }

  fun setTab(tab: MainTab) {
    _selectedTab.value = tab
  }

  fun openCreate() {
    _editingItem.value = null
    _editorInitialType.value = if (_selectedTab.value == MainTab.DAY_ALERTS) {
      NotificationType.DAY_ALERT
    } else {
      NotificationType.TEMPORARY
    }
    _isEditorOpen.value = true
  }

  fun openEdit(item: NotificationItem) {
    _editingItem.value = item
    _editorInitialType.value = if (item.type == NotificationType.DAY_ALERT.name) {
      NotificationType.DAY_ALERT
    } else {
      NotificationType.TEMPORARY
    }
    _isEditorOpen.value = true
  }

  fun dismissEditor() {
    _isEditorOpen.value = false
    _editingItem.value = null
  }

  fun openSettingsDialog() {
    _isSettingsDialogOpen.value = true
  }

  fun dismissSettingsDialog() {
    _isSettingsDialogOpen.value = false
  }

  fun saveNotification(item: NotificationItem) {
    viewModelScope.launch(Dispatchers.IO) {
      repository.saveNotification(item)
    }
    dismissEditor()
  }

  fun toggleNotification(id: Long, isEnabled: Boolean) {
    viewModelScope.launch(Dispatchers.IO) {
      repository.toggleNotificationEnabled(id, isEnabled)
    }
  }

  /**
   * Delete with 3-second Undo pop-up:
   * Visually hides item immediately, displays Undo popup for 3 seconds.
   * If undone, cancels job and restores item.
   */
  fun requestDeleteNotification(item: NotificationItem) {
    commitPendingDelete()

    _pendingDeletedItem.value = item
    pendingDeleteJob = viewModelScope.launch {
      delay(3000)
      if (_pendingDeletedItem.value?.id == item.id) {
        repository.deleteNotification(item)
        _pendingDeletedItem.value = null
      }
    }
  }

  fun undoDelete() {
    pendingDeleteJob?.cancel()
    pendingDeleteJob = null
    _pendingDeletedItem.value = null
  }

  private fun commitPendingDelete() {
    val pending = _pendingDeletedItem.value ?: return
    pendingDeleteJob?.cancel()
    pendingDeleteJob = null
    _pendingDeletedItem.value = null
    viewModelScope.launch(Dispatchers.IO) {
      repository.deleteNotification(pending)
    }
  }

  fun testSendNow(item: NotificationItem) {
    viewModelScope.launch(Dispatchers.IO) {
      repository.testSendNotificationNow(item)
    }
  }

  fun stopApp(resumeDateText: String = "", resumeTimeText: String = "", resumeAtMillis: Long = 0L) {
    viewModelScope.launch(Dispatchers.IO) {
      repository.stopApp(resumeDateText, resumeTimeText, resumeAtMillis)
    }
    dismissSettingsDialog()
  }

  fun restartApp() {
    viewModelScope.launch(Dispatchers.IO) {
      repository.restartApp()
    }
    dismissSettingsDialog()
  }
}
