package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.data.db.AppDatabase
import com.example.data.model.DailySkipRecord
import com.example.util.DateUtil
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Listens for the "Skip Today" action.
 * Automatically skips today, stops further notifications, and deletes every single
 * notification Notify has sent to the device including that Day Alert.
 */
class SkipTodayReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent?) {
    val pendingResult = goAsync()

    CoroutineScope(Dispatchers.IO).launch {
      try {
        val todayStr = DateUtil.getTodayDateString()
        val db = AppDatabase.getDatabase(context)

        // 1. Mark today as skipped
        db.notificationDao().insertDailySkip(
          DailySkipRecord(dateString = todayStr, skippedAt = System.currentTimeMillis())
        )

        // 2. Delete every single notification already sent from the app
        NotificationHelper.deleteAllSentNotifications(context)
      } catch (e: Exception) {
        e.printStackTrace()
      } finally {
        pendingResult.finish()
      }
    }
  }
}
