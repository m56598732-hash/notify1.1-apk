package com.example.data.repository

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.model.AppControlState
import com.example.data.model.NotificationItem
import com.example.data.model.NotificationType
import com.example.util.DateUtil
import com.example.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class NotificationRepository(private val context: Context) {

  private val database by lazy { AppDatabase.getDatabase(context) }
  private val dao by lazy { database.notificationDao() }

  fun observeNotifications(): Flow<List<NotificationItem>> = dao.observeAllNotifications()

  fun observeAppControl(): Flow<AppControlState?> = dao.observeAppControl()

  suspend fun saveNotification(item: NotificationItem): Long = withContext(Dispatchers.IO) {
    val id = if (item.id == 0L) {
      dao.insertNotification(item)
    } else {
      dao.updateNotification(item)
      item.id
    }

    val updatedItem = dao.getNotificationById(id)
    val appControl = dao.getAppControl()
    val isStopped = appControl?.isStopped == true

    if (updatedItem != null && updatedItem.isEnabled && !isStopped) {
      NotificationHelper.scheduleAlarm(context, updatedItem)
    } else {
      NotificationHelper.cancelAlarm(context, id)
    }
    id
  }

  suspend fun deleteNotification(item: NotificationItem) = withContext(Dispatchers.IO) {
    NotificationHelper.cancelAlarm(context, item.id)
    dao.deleteNotification(item)
  }

  suspend fun toggleNotificationEnabled(id: Long, isEnabled: Boolean) = withContext(Dispatchers.IO) {
    dao.setEnabled(id, isEnabled)
    val item = dao.getNotificationById(id)
    val appControl = dao.getAppControl()
    val isStopped = appControl?.isStopped == true

    if (item != null) {
      if (isEnabled && !isStopped) {
        NotificationHelper.scheduleAlarm(context, item)
      } else {
        NotificationHelper.cancelAlarm(context, id)
      }
    }
  }

  suspend fun testSendNotificationNow(item: NotificationItem) = withContext(Dispatchers.IO) {
    if (item.type == NotificationType.DAY_ALERT.name) {
      val allEnabled = dao.getEnabledNotifications()
      val dayItems = allEnabled.filter { it.id != item.id }
      NotificationHelper.sendDayAlertNotification(context, item, dayItems)
    } else {
      NotificationHelper.sendTemporaryNotification(context, item)
    }
  }

  suspend fun stopApp(
    resumeDateText: String = "",
    resumeTimeText: String = "",
    resumeAtMillis: Long = 0L
  ) = withContext(Dispatchers.IO) {
    dao.setAppControl(
      AppControlState(
        id = 1,
        isStopped = true,
        stoppedAt = System.currentTimeMillis(),
        resumeAtMillis = resumeAtMillis,
        resumeDateText = resumeDateText,
        resumeTimeText = resumeTimeText
      )
    )

    // Delete all notifications currently on device
    NotificationHelper.deleteAllSentNotifications(context)

    if (resumeAtMillis > System.currentTimeMillis()) {
      NotificationHelper.scheduleAutoResume(context, resumeAtMillis)
    }
  }

  suspend fun restartApp() = withContext(Dispatchers.IO) {
    dao.setAppControl(
      AppControlState(
        id = 1,
        isStopped = false,
        stoppedAt = 0L,
        resumeAtMillis = 0L,
        resumeDateText = "",
        resumeTimeText = ""
      )
    )
    NotificationHelper.cancelAutoResume(context)

    // Re-arm alarms for enabled notifications
    val enabled = dao.getEnabledNotifications()
    for (item in enabled) {
      NotificationHelper.scheduleAlarm(context, item)
    }
  }
}
