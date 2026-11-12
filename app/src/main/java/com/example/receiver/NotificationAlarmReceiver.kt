package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.db.AppDatabase
import com.example.data.model.AppControlState
import com.example.data.model.DayOfWeekItem
import com.example.data.model.NotificationType
import com.example.util.DateUtil
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationAlarmReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent?) {
    if (intent == null) return
    val pendingResult = goAsync()

    CoroutineScope(Dispatchers.IO).launch {
      try {
        val action = intent.action
        val db = AppDatabase.getDatabase(context)

        // Check if app is stopped
        val appControl = db.notificationDao().getAppControl()
        val isAppStopped = appControl?.isStopped == true

        when (action) {
          NotificationHelper.ACTION_AUTO_RESUME -> {
            // Automatically resume app after pause schedule expires
            db.notificationDao().setAppControl(
              AppControlState(id = 1, isStopped = false, stoppedAt = 0L, resumeAtMillis = 0L)
            )
            val enabled = db.notificationDao().getEnabledNotifications()
            for (item in enabled) {
              NotificationHelper.scheduleAlarm(context, item)
            }
          }

          NotificationHelper.ACTION_TRIGGER_NOTIFICATION -> {
            if (!isAppStopped) {
              val todayStr = DateUtil.getTodayDateString()
              val isTodaySkipped = db.notificationDao().getDailySkip(todayStr) != null

              val itemId = intent.getLongExtra(NotificationHelper.EXTRA_ITEM_ID, -1L)
              if (itemId > 0) {
                val item = db.notificationDao().getNotificationById(itemId)
                if (item != null && item.isEnabled) {
                  if (!isTodaySkipped) {
                    val selectedDays = DayOfWeekItem.parseCodes(item.selectedDays)
                    if (DateUtil.isDayMatchingToday(selectedDays)) {
                      NotificationHelper.sendTemporaryNotification(context, item)
                    }
                  }

                  if (item.isRecurring) {
                    NotificationHelper.scheduleAlarm(context, item)
                  } else {
                    db.notificationDao().setEnabled(item.id, false)
                  }
                }
              }
            }
          }

          NotificationHelper.ACTION_TRIGGER_DAY_ALERT -> {
            if (!isAppStopped) {
              val todayStr = DateUtil.getTodayDateString()
              val isTodaySkipped = db.notificationDao().getDailySkip(todayStr) != null

              val itemId = intent.getLongExtra(NotificationHelper.EXTRA_ITEM_ID, -1L)
              if (itemId > 0) {
                val item = db.notificationDao().getNotificationById(itemId)
                if (item != null && item.isEnabled) {
                  if (!isTodaySkipped) {
                    val allNotifications = db.notificationDao().getEnabledNotifications()
                    val todayItems = allNotifications.filter {
                      it.id != item.id &&
                          DateUtil.isDayMatchingToday(DayOfWeekItem.parseCodes(it.selectedDays))
                    }
                    NotificationHelper.sendDayAlertNotification(context, item, todayItems)
                  }

                  if (item.isRecurring) {
                    NotificationHelper.scheduleAlarm(context, item)
                  } else {
                    db.notificationDao().setEnabled(item.id, false)
                  }
                }
              }
            }
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
      } finally {
        pendingResult.finish()
      }
    }
  }
}
