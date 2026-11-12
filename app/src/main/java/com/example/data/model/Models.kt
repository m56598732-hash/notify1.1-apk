package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

enum class NotificationType {
  TEMPORARY,
  DAY_ALERT
}

enum class DayOfWeekItem(val code: String, val shortName: String, val calendarDay: Int) {
  MONDAY("Mon", "M", Calendar.MONDAY),
  TUESDAY("Tue", "T", Calendar.TUESDAY),
  WEDNESDAY("Wed", "W", Calendar.WEDNESDAY),
  THURSDAY("Thu", "T", Calendar.THURSDAY),
  FRIDAY("Fri", "F", Calendar.FRIDAY),
  SATURDAY("Sat", "S", Calendar.SATURDAY),
  SUNDAY("Sun", "S", Calendar.SUNDAY);

  companion object {
    fun allCodes(): String = values().joinToString(",") { it.code }

    fun parseCodes(codesString: String): Set<String> {
      if (codesString.isBlank()) return emptySet()
      return codesString.split(",").map { it.trim() }.toSet()
    }
  }
}

@Entity(tableName = "notifications")
data class NotificationItem(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val title: String,
  val body: String,
  val type: String = NotificationType.TEMPORARY.name,
  val isRecurring: Boolean = true,
  val selectedDays: String = DayOfWeekItem.allCodes(),
  val hour: Int = 9,
  val minute: Int = 0,
  val isEnabled: Boolean = true,
  val expirationMinutes: Int = 30, // Default 30 min per user requirement
  val hasActionButton: Boolean = false,
  val actionButtonLabel: String = "Skip Today",
  val displayTitle: String = "", // Full customization display overrides
  val displayBody: String = "",
  val displaySubtext: String = "",
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_control")
data class AppControlState(
  @PrimaryKey
  val id: Int = 1,
  val isStopped: Boolean = false,
  val stoppedAt: Long = 0L,
  val resumeAtMillis: Long = 0L,
  val resumeDateText: String = "",
  val resumeTimeText: String = ""
)

@Entity(tableName = "daily_skips")
data class DailySkipRecord(
  @PrimaryKey
  val dateString: String,
  val skippedAt: Long = System.currentTimeMillis()
)
