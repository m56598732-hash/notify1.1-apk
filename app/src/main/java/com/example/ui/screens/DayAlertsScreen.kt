package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayOfWeekItem
import com.example.data.model.NotificationItem
import com.example.data.model.NotificationType
import com.example.ui.components.GreyButton
import com.example.ui.components.MonochromeCard
import com.example.ui.theme.ButtonGrey
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkElevated
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.util.DateUtil

@Composable
fun DayAlertsScreen(
  notifications: List<NotificationItem>,
  onToggleEnabled: (Long, Boolean) -> Unit,
  onEdit: (NotificationItem) -> Unit,
  onDelete: (NotificationItem) -> Unit,
  onCreateNew: () -> Unit,
  modifier: Modifier = Modifier
) {
  val dayAlerts = notifications.filter { it.type == NotificationType.DAY_ALERT.name }

  if (dayAlerts.isEmpty()) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .padding(24.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          text = "No scheduled day alerts",
          color = TextSecondary,
          fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(14.dp))
        GreyButton(
          text = "Create",
          onClick = onCreateNew,
          testTag = "empty_create_day_alert_button"
        )
      }
    }
  } else {
    LazyColumn(
      modifier = modifier
        .fillMaxSize()
        .testTag("day_alerts_list"),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      item(key = "day_alerts_header") {
        Text(
          text = "Scheduled Day Alerts",
          color = TextSecondary,
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium,
          modifier = Modifier.padding(vertical = 4.dp)
        )
      }

      items(
        items = dayAlerts,
        key = { it.id }
      ) { item ->
        DayAlertCard(
          item = item,
          onToggle = { onToggleEnabled(item.id, it) },
          onEdit = { onEdit(item) },
          onDelete = { onDelete(item) }
        )
      }
    }
  }
}

@Composable
fun DayAlertCard(
  item: NotificationItem,
  onToggle: (Boolean) -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  MonochromeCard(
    onClick = onEdit,
    border = BorderStroke(1.dp, if (item.isEnabled) DarkBorder else DarkElevated)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Top
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = DateUtil.formatTime(item.hour, item.minute),
          color = if (item.isEnabled) PureWhite else TextSecondary,
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = item.title,
          color = if (item.isEnabled) PureWhite else TextSecondary,
          fontSize = 15.sp,
          fontWeight = FontWeight.Medium
        )

        val selectedDays = DayOfWeekItem.parseCodes(item.selectedDays)
        Text(
          text = if (selectedDays.size == 7) "Every day" else selectedDays.joinToString(" "),
          color = TextTertiary,
          fontSize = 11.sp,
          modifier = Modifier.padding(top = 4.dp)
        )

        Text(
          text = "Action: Skip Today",
          color = TextTertiary,
          fontSize = 11.sp,
          modifier = Modifier.padding(top = 2.dp)
        )
      }

      Switch(
        checked = item.isEnabled,
        onCheckedChange = onToggle,
        modifier = Modifier.testTag("toggle_day_alert_${item.id}"),
        colors = SwitchDefaults.colors(
          checkedThumbColor = PureWhite,
          checkedTrackColor = ButtonGrey,
          uncheckedThumbColor = TextSecondary,
          uncheckedTrackColor = DarkElevated,
          uncheckedBorderColor = DarkBorder
        )
      )
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp),
      horizontalArrangement = Arrangement.End,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onEdit,
        modifier = Modifier
          .size(32.dp)
          .testTag("edit_day_alert_${item.id}")
      ) {
        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(15.dp))
      }

      IconButton(
        onClick = onDelete,
        modifier = Modifier
          .size(32.dp)
          .testTag("delete_day_alert_${item.id}")
      ) {
        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextSecondary, modifier = Modifier.size(15.dp))
      }
    }
  }
}
