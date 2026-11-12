package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
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
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.util.DateUtil

@Composable
fun SchedulesScreen(
  notifications: List<NotificationItem>,
  onToggleEnabled: (Long, Boolean) -> Unit,
  onEdit: (NotificationItem) -> Unit,
  onDelete: (NotificationItem) -> Unit,
  onCreateNew: () -> Unit,
  modifier: Modifier = Modifier
) {
  if (notifications.isEmpty()) {
    EmptySchedulesView(onCreateNew = onCreateNew, modifier = modifier)
  } else {
    LazyColumn(
      modifier = modifier
        .fillMaxSize()
        .testTag("schedules_list"),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(
        items = notifications,
        key = { it.id }
      ) { item ->
        ScheduleItemCard(
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
fun ScheduleItemCard(
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
        // Time & Type
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = DateUtil.formatTime(item.hour, item.minute),
            color = if (item.isEnabled) PureWhite else TextSecondary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )

          if (item.type == NotificationType.DAY_ALERT.name) {
            Text(
              text = "Day Alert",
              color = TextSecondary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium
            )
          } else if (item.expirationMinutes > 0) {
            Text(
              text = "${item.expirationMinutes} min",
              color = TextTertiary,
              fontSize = 11.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Title
        Text(
          text = item.title,
          color = if (item.isEnabled) PureWhite else TextSecondary,
          fontSize = 15.sp,
          fontWeight = FontWeight.Medium
        )

        // Body
        if (item.body.isNotBlank()) {
          Text(
            text = item.body,
            color = TextSecondary,
            fontSize = 13.sp,
            maxLines = 2,
            modifier = Modifier.padding(top = 2.dp)
          )
        }

        // Days of week
        if (item.isRecurring) {
          val selectedDays = DayOfWeekItem.parseCodes(item.selectedDays)
          Row(
            modifier = Modifier.padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            DayOfWeekItem.values().forEach { day ->
              val isSelected = day.code in selectedDays
              Box(
                modifier = Modifier
                  .size(20.dp)
                  .clip(RoundedCornerShape(3.dp))
                  .background(if (isSelected && item.isEnabled) ButtonGrey else DarkElevated),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = day.shortName,
                  color = if (isSelected && item.isEnabled) PureWhite else TextTertiary,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
          }
        }
      }

      // Switch toggle
      Switch(
        checked = item.isEnabled,
        onCheckedChange = onToggle,
        modifier = Modifier.testTag("toggle_schedule_${item.id}"),
        colors = SwitchDefaults.colors(
          checkedThumbColor = PureWhite,
          checkedTrackColor = ButtonGrey,
          uncheckedThumbColor = TextSecondary,
          uncheckedTrackColor = DarkElevated,
          uncheckedBorderColor = DarkBorder
        )
      )
    }

    // Actions footer: Edit, Delete (Test button is hidden here and shown inside editor!)
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
          .testTag("edit_schedule_${item.id}")
      ) {
        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(15.dp))
      }

      IconButton(
        onClick = onDelete,
        modifier = Modifier
          .size(32.dp)
          .testTag("delete_schedule_${item.id}")
      ) {
        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextSecondary, modifier = Modifier.size(15.dp))
      }
    }
  }
}

@Composable
fun EmptySchedulesView(
  onCreateNew: () -> Unit,
  modifier: Modifier = Modifier
) {
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
        text = "No scheduled notifications",
        color = TextSecondary,
        fontSize = 14.sp
      )
      Spacer(modifier = Modifier.height(14.dp))
      GreyButton(
        text = "Create",
        onClick = onCreateNew,
        testTag = "empty_create_button"
      )
    }
  }
}
