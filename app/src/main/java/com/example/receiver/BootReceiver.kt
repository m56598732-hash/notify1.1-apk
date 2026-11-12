package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.db.AppDatabase
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Reschedules all active enabled notifications after device reboot.
 */
class BootReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent?) {
    if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
    val pendingResult = goAsync()

    CoroutineScope(Dispatchers.IO).launch {
      try {
        NotificationHelper.createNotificationChannels(context)
        val db = AppDatabase.getDatabase(context)
        val enabledNotifications = db.notificationDao().getEnabledNotifications()
        for (item in enabledNotifications) {
          NotificationHelper.scheduleAlarm(context, item)
        }
      } catch (e: Exception) {
        e.printStackTrace()
      } finally {
        pendingResult.finish()
      }
    }
  }
}
