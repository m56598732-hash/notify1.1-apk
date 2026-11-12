package com.example.util

import com.example.data.model.DayOfWeekItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtil {

  private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
  private val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
  private val displayDateFormat = SimpleDateFormat("EEE, MMM d", Locale.US)

  fun getTodayDateString(): String {
    return dateFormat.format(Date())
  }

  fun formatTime(hour: Int, minute: Int): String {
    val cal = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, hour)
      set(Calendar.MINUTE, minute)
      set(Calendar.SECOND, 0)
    }
    return timeFormat.format(cal.time)
  }

  fun formatTimestamp(millis: Long): String {
    if (millis <= 0) return "Never"
    val date = Date(millis)
    val todayStr = getTodayDateString()
    val itemDateStr = dateFormat.format(date)
    return if (todayStr == itemDateStr) {
      "Today, ${timeFormat.format(date)}"
    } else {
      "${displayDateFormat.format(date)}, ${timeFormat.format(date)}"
    }
  }

  /**
   * Calculates the next trigger timestamp in milliseconds for a given hour, minute,
   * and selected days of week.
   */
  fun calculateNextTriggerMillis(
    hour: Int,
    minute: Int,
    selectedDaysCodes: Set<String>,
    isRecurring: Boolean
  ): Long {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, hour)
      set(Calendar.MINUTE, minute)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }

    if (!isRecurring) {
      // One-off notification: if time today already passed, schedule for tomorrow
      if (target.before(now)) {
        target.add(Calendar.DAY_OF_YEAR, 1)
      }
      return target.timeInMillis
    }

    // Recurring: check from today (day 0) through day 6
    val daysMap = DayOfWeekItem.values().associateBy { it.code }
    val selectedCalendarDays = selectedDaysCodes.mapNotNull { daysMap[it]?.calendarDay }.toSet()

    // If no days selected, default to all days
    val activeCalendarDays = if (selectedCalendarDays.isEmpty()) {
      DayOfWeekItem.values().map { it.calendarDay }.toSet()
    } else {
      selectedCalendarDays
    }

    for (offset in 0..7) {
      val candidate = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, offset)
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }

      val candidateDayOfWeek = candidate.get(Calendar.DAY_OF_WEEK)
      if (candidateDayOfWeek in activeCalendarDays) {
        if (offset == 0) {
          if (candidate.after(now)) {
            return candidate.timeInMillis
          }
        } else {
          return candidate.timeInMillis
        }
      }
    }

    // Fallback: 1 week from now
    target.add(Calendar.WEEK_OF_YEAR, 1)
    return target.timeInMillis
  }

  fun isDayMatchingToday(selectedDaysCodes: Set<String>): Boolean {
    val todayCalendarDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    val daysMap = DayOfWeekItem.values().associateBy { it.code }
    val selectedCalendarDays = selectedDaysCodes.mapNotNull { daysMap[it]?.calendarDay }.toSet()
    return selectedCalendarDays.isEmpty() || todayCalendarDay in selectedCalendarDays
  }
}
