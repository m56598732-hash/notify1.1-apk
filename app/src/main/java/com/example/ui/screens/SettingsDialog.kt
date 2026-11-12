package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppControlState
import com.example.ui.components.GreyButton
import com.example.ui.components.MonochromeCard
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.DateUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun SettingsDialog(
  appControl: AppControlState,
  onStop: (resumeDate: String, resumeTime: String, resumeMillis: Long) -> Unit,
  onRestart: () -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val cal = remember {
    Calendar.getInstance().apply {
      if (appControl.resumeAtMillis > System.currentTimeMillis()) {
        timeInMillis = appControl.resumeAtMillis
      } else {
        add(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, 9)
        set(Calendar.MINUTE, 0)
      }
    }
  }

  var selectedYear by remember { mutableIntStateOf(cal.get(Calendar.YEAR)) }
  var selectedMonth by remember { mutableIntStateOf(cal.get(Calendar.MONTH)) }
  var selectedDay by remember { mutableIntStateOf(cal.get(Calendar.DAY_OF_MONTH)) }

  var selectedHour by remember { mutableIntStateOf(cal.get(Calendar.HOUR_OF_DAY)) }
  var selectedMinute by remember { mutableIntStateOf(cal.get(Calendar.MINUTE)) }

  val openDatePicker = {
    android.app.DatePickerDialog(
      context,
      { _, y, m, d ->
        selectedYear = y
        selectedMonth = m
        selectedDay = d
      },
      selectedYear,
      selectedMonth,
      selectedDay
    ).show()
  }

  val openTimePicker = {
    android.app.TimePickerDialog(
      context,
      { _, h, m ->
        selectedHour = h
        selectedMinute = m
      },
      selectedHour,
      selectedMinute,
      false
    ).show()
  }

  val displayCal = Calendar.getInstance().apply {
    set(Calendar.YEAR, selectedYear)
    set(Calendar.MONTH, selectedMonth)
    set(Calendar.DAY_OF_MONTH, selectedDay)
    set(Calendar.HOUR_OF_DAY, selectedHour)
    set(Calendar.MINUTE, selectedMinute)
    set(Calendar.SECOND, 0)
  }
  val dateFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(displayCal.time)
  val dateDisplayFormatted = SimpleDateFormat("EEE, MMM d, yyyy", Locale.US).format(displayCal.time)

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = DarkSurface,
      border = BorderStroke(1.dp, DarkBorder),
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .widthIn(max = 480.dp)
        .padding(vertical = 24.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Settings",
            color = PureWhite,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
          )

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("settings_close_button")
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Stop / Restart action
        if (appControl.isStopped) {
          Column {
            if (appControl.resumeAtMillis > System.currentTimeMillis()) {
              Text(
                text = "Stopped until ${appControl.resumeDateText} ${appControl.resumeTimeText}",
                color = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 10.dp)
              )
            } else {
              Text(
                text = "App is currently stopped.",
                color = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 10.dp)
              )
            }

            GreyButton(
              text = "Restart",
              onClick = onRestart,
              modifier = Modifier.fillMaxWidth(),
              testTag = "restart_app_button"
            )
          }
        } else {
          GreyButton(
            text = "Stop",
            onClick = { onStop("", "", 0L) },
            modifier = Modifier.fillMaxWidth(),
            isAlert = true,
            testTag = "stop_app_button"
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Resume settings with Date & Time Pickers (no manual typing)
        MonochromeCard {
          Text(
            text = "Resume settings",
            color = PureWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Date Picker Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.CalendarToday, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = dateDisplayFormatted,
                color = PureWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
              )
            }

            GreyButton(
              text = "Pick date",
              onClick = openDatePicker,
              testTag = "pick_resume_date_button"
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Time Picker Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Schedule, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = DateUtil.formatTime(selectedHour, selectedMinute),
                color = PureWhite,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
            }

            GreyButton(
              text = "Pick time",
              onClick = openTimePicker,
              testTag = "pick_resume_time_button"
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          GreyButton(
            text = "Stop and set resume date",
            onClick = {
              val timeFormatted = String.format("%02d:%02d", selectedHour, selectedMinute)
              onStop(dateFormatted, timeFormatted, displayCal.timeInMillis)
            },
            modifier = Modifier.fillMaxWidth(),
            testTag = "schedule_resume_button"
          )
        }

        Spacer(modifier = Modifier.height(10.dp))
      }
    }
  }
}
