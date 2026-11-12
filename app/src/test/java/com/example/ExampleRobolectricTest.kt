package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DayOfWeekItem
import com.example.data.model.NotificationItem
import com.example.data.model.NotificationType
import com.example.util.DateUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read app_name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Notify", appName)
  }

  @Test
  fun `notification item recurring is enabled by default`() {
    val item = NotificationItem(
      title = "Standup",
      body = "Daily sprint sync"
    )
    assertTrue("Recurring must be enabled by default", item.isRecurring)
    assertEquals(NotificationType.TEMPORARY.name, item.type)
    assertEquals(30, item.expirationMinutes)
  }

  @Test
  fun `date utility formats time correctly`() {
    val formatted = DateUtil.formatTime(14, 30)
    assertTrue(formatted.contains("02:30") || formatted.contains("2:30"))
    assertTrue(formatted.contains("PM"))
  }

  @Test
  fun `date utility calculates next trigger timestamp`() {
    val nextTrigger = DateUtil.calculateNextTriggerMillis(
      hour = 9,
      minute = 0,
      selectedDaysCodes = DayOfWeekItem.values().map { it.code }.toSet(),
      isRecurring = true
    )
    assertTrue(nextTrigger > System.currentTimeMillis() - 60000)
  }
}
