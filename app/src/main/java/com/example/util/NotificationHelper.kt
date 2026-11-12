package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.db.AppDatabase
import com.example.data.model.DayOfWeekItem
import com.example.data.model.NotificationItem
import com.example.data.model.NotificationType
import com.example.receiver.NotificationAlarmReceiver
import com.example.receiver.SkipTodayReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NotificationHelper {

  const val CHANNEL_NOTIFY = "notify_channel_silent"
  const val ACTION_SKIP_TODAY = "com.example.notify.ACTION_SKIP_TODAY"
  const val ACTION_TRIGGER_NOTIFICATION = "com.example.notify.ACTION_TRIGGER_NOTIFICATION"
  const val ACTION_TRIGGER_DAY_ALERT = "com.example.notify.ACTION_TRIGGER_DAY_ALERT"
  const val ACTION_EXPIRE_NOTIFICATION = "com.example.notify.ACTION_EXPIRE_NOTIFICATION"
  const val ACTION_AUTO_RESUME = "com.example.notify.ACTION_AUTO_RESUME"

  const val EXTRA_ITEM_ID = "extra_item_id"
  const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

  fun createNotificationChannels(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

      // Silent channel by default per user request
      val channel = NotificationChannel(
        CHANNEL_NOTIFY,
        "Notify",
        NotificationManager.IMPORTANCE_LOW
      ).apply {
        description = "Notify alerts"
        enableLights(false)
        enableVibration(false)
        setSound(null, null)
      }

      notificationManager.createNotificationChannel(channel)
    }
  }

  /**
   * Sends a temporary notification.
   * Silent by default with auto-expiration (default 30 min).
   */
  fun sendTemporaryNotification(
    context: Context,
    item: NotificationItem,
    overrideNotificationId: Int? = null
  ): Int {
    val notificationManager = NotificationManagerCompat.from(context)
    val notifId = overrideNotificationId ?: (item.id.toInt().takeIf { it != 0 } ?: (System.currentTimeMillis() % 100000).toInt())

    val contentIntent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val contentPendingIntent = PendingIntent.getActivity(
      context,
      notifId,
      contentIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val effectiveTitle = item.displayTitle.ifBlank { item.title.ifBlank { "Notification" } }
    val effectiveBody = item.displayBody.ifBlank { item.body.ifBlank { "Notify alert" } }

    val builder = NotificationCompat.Builder(context, CHANNEL_NOTIFY)
      .setSmallIcon(android.R.drawable.ic_popup_reminder)
      .setContentTitle(effectiveTitle)
      .setContentText(effectiveBody)
      .setStyle(NotificationCompat.BigTextStyle().bigText(effectiveBody))
      .setContentIntent(contentPendingIntent)
      .setAutoCancel(true)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .setSilent(true)

    if (item.displaySubtext.isNotBlank()) {
      builder.setSubText(item.displaySubtext)
    }

    // Expiration timeout (default 30 minutes, or custom)
    if (item.expirationMinutes > 0) {
      val timeoutMillis = item.expirationMinutes * 60 * 1000L
      builder.setTimeoutAfter(timeoutMillis)
    }

    try {
      notificationManager.notify(notifId, builder.build())
    } catch (e: SecurityException) {
      // Permission not granted
    }

    return notifId
  }

  /**
   * Day Alert: Aggregates everything from that day.
   * Has a "Skip Today" action button by default which deletes all notifications
   * from Notify and stops any further notifications today.
   */
  fun sendDayAlertNotification(
    context: Context,
    item: NotificationItem,
    dayItems: List<NotificationItem>,
    overrideNotificationId: Int? = null
  ): Int {
    val notificationManager = NotificationManagerCompat.from(context)
    val notifId = overrideNotificationId ?: (item.id.toInt().takeIf { it != 0 } ?: 99999)

    val contentIntent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val contentPendingIntent = PendingIntent.getActivity(
      context,
      notifId,
      contentIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Summary of all alerts scheduled for that day
    val defaultBodyText = if (dayItems.isEmpty()) {
      "No other alerts scheduled for today."
    } else {
      dayItems.joinToString("\n") { alert ->
        val timeStr = DateUtil.formatTime(alert.hour, alert.minute)
        "• $timeStr — ${alert.title}"
      }
    }
    val effectiveBody = item.displayBody.ifBlank { defaultBodyText }

    // "Skip Today" Action Button targeting SkipTodayReceiver
    val skipIntent = Intent(context, SkipTodayReceiver::class.java).apply {
      action = ACTION_SKIP_TODAY
    }
    val skipPendingIntent = PendingIntent.getBroadcast(
      context,
      1001,
      skipIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val effectiveTitle = item.displayTitle.ifBlank { item.title.ifBlank { "Day Alert" } }
    val effectiveActionLabel = item.actionButtonLabel.ifBlank { "Skip Today" }

    val builder = NotificationCompat.Builder(context, CHANNEL_NOTIFY)
      .setSmallIcon(android.R.drawable.ic_dialog_info)
      .setContentTitle(effectiveTitle)
      .setContentText(if (dayItems.isEmpty()) "Day summary" else "${dayItems.size} alerts today")
      .setStyle(NotificationCompat.BigTextStyle().bigText(effectiveBody))
      .setContentIntent(contentPendingIntent)
      .setAutoCancel(true)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .setSilent(true)
      .addAction(0, effectiveActionLabel, skipPendingIntent)

    if (item.displaySubtext.isNotBlank()) {
      builder.setSubText(item.displaySubtext)
    }

    if (item.expirationMinutes > 0) {
      builder.setTimeoutAfter(item.expirationMinutes * 60 * 1000L)
    }

    try {
      notificationManager.notify(notifId, builder.build())
    } catch (e: SecurityException) {
      // Permission not granted
    }

    return notifId
  }

  /**
   * Deletes every single notification Notify has sent to the device.
   */
  fun deleteAllSentNotifications(context: Context) {
    val notificationManager = NotificationManagerCompat.from(context)
    notificationManager.cancelAll()
  }

  /**
   * Schedules an exact or standard alarm for a NotificationItem.
   */
  fun scheduleAlarm(context: Context, item: NotificationItem) {
    if (!item.isEnabled) {
      cancelAlarm(context, item.id)
      return
    }

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

    val intent = Intent(context, NotificationAlarmReceiver::class.java).apply {
      action = if (item.type == NotificationType.DAY_ALERT.name) {
        ACTION_TRIGGER_DAY_ALERT
      } else {
        ACTION_TRIGGER_NOTIFICATION
      }
      putExtra(EXTRA_ITEM_ID, item.id)
    }

    val requestCode = item.id.toInt()
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      requestCode,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val triggerAtMillis = DateUtil.calculateNextTriggerMillis(
      hour = item.hour,
      minute = item.minute,
      selectedDaysCodes = DayOfWeekItem.parseCodes(item.selectedDays),
      isRecurring = item.isRecurring
    )

    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager.setExactAndAllowWhileIdle(
          AlarmManager.RTC_WAKEUP,
          triggerAtMillis,
          pendingIntent
        )
      } else {
        alarmManager.setExact(
          AlarmManager.RTC_WAKEUP,
          triggerAtMillis,
          pendingIntent
        )
      }
    } catch (e: SecurityException) {
      alarmManager.set(
        AlarmManager.RTC_WAKEUP,
        triggerAtMillis,
        pendingIntent
      )
    }
  }

  fun cancelAlarm(context: Context, itemId: Long) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val intent = Intent(context, NotificationAlarmReceiver::class.java).apply {
      action = ACTION_TRIGGER_NOTIFICATION
      putExtra(EXTRA_ITEM_ID, itemId)
    }
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      itemId.toInt(),
      intent,
      PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )
    if (pendingIntent != null) {
      alarmManager.cancel(pendingIntent)
      pendingIntent.cancel()
    }
  }

  fun scheduleAutoResume(context: Context, resumeAtMillis: Long) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val intent = Intent(context, NotificationAlarmReceiver::class.java).apply {
      action = ACTION_AUTO_RESUME
    }
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      8888,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, resumeAtMillis, pendingIntent)
      } else {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, resumeAtMillis, pendingIntent)
      }
    } catch (e: SecurityException) {
      alarmManager.set(AlarmManager.RTC_WAKEUP, resumeAtMillis, pendingIntent)
    }
  }

  fun cancelAutoResume(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val intent = Intent(context, NotificationAlarmReceiver::class.java).apply {
      action = ACTION_AUTO_RESUME
    }
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      8888,
      intent,
      PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )
    if (pendingIntent != null) {
      alarmManager.cancel(pendingIntent)
      pendingIntent.cancel()
    }
  }
}
