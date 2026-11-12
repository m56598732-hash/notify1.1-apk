package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayOfWeekItem
import com.example.data.model.NotificationItem
import com.example.data.model.NotificationType
import com.example.ui.components.DayChip
import com.example.ui.components.GreyButton
import com.example.ui.components.MonochromeCard
import com.example.ui.components.MonochromeSwitchRow
import com.example.ui.theme.ButtonGrey
import com.example.ui.theme.ButtonGreyBorder
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.util.DateUtil
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationEditorSheet(
  itemToEdit: NotificationItem?,
  initialType: NotificationType = NotificationType.TEMPORARY,
  existingNotifications: List<NotificationItem>,
  onDismiss: () -> Unit,
  onSave: (NotificationItem) -> Unit,
  onTestSend: (NotificationItem) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  var title by remember { mutableStateOf(itemToEdit?.title ?: "") }
  var body by remember { mutableStateOf(itemToEdit?.body ?: "") }

  // Use initialType when creating new (e.g. Day Alert if user clicked + from Day Alerts tab)
  var notificationType by remember {
    mutableStateOf(
      if (itemToEdit != null) {
        if (itemToEdit.type == NotificationType.DAY_ALERT.name) NotificationType.DAY_ALERT else NotificationType.TEMPORARY
      } else {
        initialType
      }
    )
  }

  // Recurring is enabled by default
  var isRecurring by remember { mutableStateOf(itemToEdit?.isRecurring ?: true) }

  var selectedDays by remember {
    mutableStateOf(
      itemToEdit?.let { DayOfWeekItem.parseCodes(it.selectedDays) }
        ?: DayOfWeekItem.values().map { it.code }.toSet()
    )
  }

  val context = androidx.compose.ui.platform.LocalContext.current
  val currentCal = Calendar.getInstance()
  var selectedHour by remember {
    mutableIntStateOf(itemToEdit?.hour ?: ((currentCal.get(Calendar.HOUR_OF_DAY) + 1) % 24))
  }
  var selectedMinute by remember {
    mutableIntStateOf(itemToEdit?.minute ?: 0)
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

  // Expiration: by default set on 30 min
  var expirationMinutes by remember { mutableIntStateOf(itemToEdit?.expirationMinutes ?: 30) }

  // Customization display overrides
  var displayTitle by remember { mutableStateOf(itemToEdit?.displayTitle ?: "") }
  var displayBody by remember { mutableStateOf(itemToEdit?.displayBody ?: "") }
  var displaySubtext by remember { mutableStateOf(itemToEdit?.displaySubtext ?: "") }
  var actionButtonLabel by remember { mutableStateOf(itemToEdit?.actionButtonLabel ?: "Skip Today") }

  // Toggle for full display editing
  var isEditingDisplay by remember { mutableStateOf(false) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = DarkSurface,
    contentColor = TextPrimary,
    dragHandle = null
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 18.dp, vertical = 20.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (itemToEdit == null) "New notification" else "Edit notification",
          color = PureWhite,
          fontSize = 17.sp,
          fontWeight = FontWeight.SemiBold
        )

        IconButton(
          onClick = onDismiss,
          modifier = Modifier.testTag("editor_close_button")
        ) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Type Selector: Temporary Alert vs Day Alert
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
          .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        val temporarySelected = notificationType == NotificationType.TEMPORARY
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(6.dp))
            .background(if (temporarySelected) ButtonGrey else Color.Transparent)
            .clickable { notificationType = NotificationType.TEMPORARY }
            .padding(vertical = 9.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Temporary Alert",
            color = if (temporarySelected) PureWhite else TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
          )
        }

        val dayAlertSelected = notificationType == NotificationType.DAY_ALERT
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(6.dp))
            .background(if (dayAlertSelected) ButtonGrey else Color.Transparent)
            .clickable { notificationType = NotificationType.DAY_ALERT }
            .padding(vertical = 9.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Day Alert",
            color = if (dayAlertSelected) PureWhite else TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Title
      OutlinedTextField(
        value = title,
        onValueChange = { title = it },
        label = { Text("Title", color = TextSecondary) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = ButtonGreyBorder,
          unfocusedBorderColor = DarkBorder,
          focusedTextColor = PureWhite,
          unfocusedTextColor = PureWhite,
          focusedContainerColor = DarkElevated,
          unfocusedContainerColor = DarkElevated
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("notification_title_input")
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Body (only "Body")
      OutlinedTextField(
        value = body,
        onValueChange = { body = it },
        label = { Text("Body", color = TextSecondary) },
        minLines = 2,
        maxLines = 3,
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = ButtonGreyBorder,
          unfocusedBorderColor = DarkBorder,
          focusedTextColor = PureWhite,
          unfocusedTextColor = PureWhite,
          focusedContainerColor = DarkElevated,
          unfocusedContainerColor = DarkElevated
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("notification_body_input")
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Schedule Time: interactive time picker
      MonochromeCard(onClick = openTimePicker) {
        Text(
          text = "Schedule time",
          color = TextSecondary,
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              Icons.Default.Notifications,
              contentDescription = null,
              tint = PureWhite,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = DateUtil.formatTime(selectedHour, selectedMinute),
              color = PureWhite,
              fontSize = 22.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }

          GreyButton(
            text = "Pick time",
            onClick = openTimePicker,
            testTag = "pick_time_button"
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Recurring
      MonochromeCard {
        MonochromeSwitchRow(
          title = "Recurring notification",
          checked = isRecurring,
          onCheckedChange = { isRecurring = it },
          testTag = "recurring_switch"
        )

        AnimatedVisibility(visible = isRecurring) {
          Column(modifier = Modifier.padding(top = 10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              DayOfWeekItem.values().forEach { day ->
                val isSelected = day.code in selectedDays
                DayChip(
                  day = day,
                  isSelected = isSelected,
                  onClick = {
                    selectedDays = if (isSelected) {
                      if (selectedDays.size > 1) selectedDays - day.code else selectedDays
                    } else {
                      selectedDays + day.code
                    }
                  }
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Customization (open by default, displays exact preview + Edit display with full control)
      MonochromeCard {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Customization",
            color = PureWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
          )

          Text(
            text = if (isEditingDisplay) "Hide edit" else "Edit display",
            color = PureWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
              .clickable { isEditingDisplay = !isEditingDisplay }
              .padding(4.dp)
              .testTag("toggle_edit_display_button")
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (notificationType == NotificationType.TEMPORARY) {
          // Notification Expiration (default 30 min)
          Text(
            text = "Notification Expiration",
            color = TextSecondary,
            fontSize = 12.sp
          )

          Spacer(modifier = Modifier.height(8.dp))

          val timeoutOptions = listOf(5 to "5 min", 15 to "15 min", 30 to "30 min", 60 to "1 hr", 120 to "2 hr", 0 to "Never")
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            timeoutOptions.forEach { (mins, label) ->
              val selected = expirationMinutes == mins
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (selected) ButtonGrey else DarkElevated)
                  .border(1.dp, if (selected) ButtonGreyBorder else DarkBorder, RoundedCornerShape(6.dp))
                  .clickable { expirationMinutes = mins }
                  .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = label,
                  color = if (selected) PureWhite else TextSecondary,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Temporary Alert exact notification preview
          Text(
            text = "Notification display preview",
            color = TextSecondary,
            fontSize = 12.sp
          )

          Spacer(modifier = Modifier.height(8.dp))

          val previewTitle = displayTitle.ifBlank { title.ifBlank { "Notification" } }
          val previewBody = displayBody.ifBlank { body.ifBlank { "Notify alert" } }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = DarkElevated,
            border = BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(ButtonGrey),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.Notifications, contentDescription = null, tint = PureWhite, modifier = Modifier.size(10.dp))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("Notify", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                if (displaySubtext.isNotBlank()) {
                  Text(" • $displaySubtext", color = TextTertiary, fontSize = 11.sp)
                }
                Text(" • now", color = TextTertiary, fontSize = 11.sp)
              }

              Spacer(modifier = Modifier.height(6.dp))

              Text(
                text = previewTitle,
                color = PureWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
              )

              Spacer(modifier = Modifier.height(2.dp))

              Text(
                text = previewBody,
                color = TextSecondary,
                fontSize = 12.sp
              )
            }
          }

          // Full Display Editing Controls for Temporary Alert
          AnimatedVisibility(visible = isEditingDisplay) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
              OutlinedTextField(
                value = displayTitle,
                onValueChange = { displayTitle = it },
                label = { Text("Display title override", color = TextSecondary, fontSize = 11.sp) },
                placeholder = { Text(title.ifBlank { "Notification" }, color = TextTertiary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = ButtonGreyBorder,
                  unfocusedBorderColor = DarkBorder,
                  focusedTextColor = PureWhite,
                  unfocusedTextColor = PureWhite,
                  focusedContainerColor = DarkElevated,
                  unfocusedContainerColor = DarkElevated
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("display_title_input")
              )

              Spacer(modifier = Modifier.height(8.dp))

              OutlinedTextField(
                value = displayBody,
                onValueChange = { displayBody = it },
                label = { Text("Display body override", color = TextSecondary, fontSize = 11.sp) },
                placeholder = { Text(body.ifBlank { "Notify alert" }, color = TextTertiary) },
                minLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = ButtonGreyBorder,
                  unfocusedBorderColor = DarkBorder,
                  focusedTextColor = PureWhite,
                  unfocusedTextColor = PureWhite,
                  focusedContainerColor = DarkElevated,
                  unfocusedContainerColor = DarkElevated
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("display_body_input")
              )

              Spacer(modifier = Modifier.height(8.dp))

              OutlinedTextField(
                value = displaySubtext,
                onValueChange = { displaySubtext = it },
                label = { Text("Display subtext / tag (optional)", color = TextSecondary, fontSize = 11.sp) },
                placeholder = { Text("e.g. Work, Urgent, Reminder", color = TextTertiary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = ButtonGreyBorder,
                  unfocusedBorderColor = DarkBorder,
                  focusedTextColor = PureWhite,
                  unfocusedTextColor = PureWhite,
                  focusedContainerColor = DarkElevated,
                  unfocusedContainerColor = DarkElevated
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("display_subtext_input")
              )
            }
          }
        } else {
          // Day Alert Customization:
          // Shows exact preview + Edit display with full control
          Text(
            text = "Notification display preview",
            color = TextSecondary,
            fontSize = 12.sp
          )

          Spacer(modifier = Modifier.height(8.dp))

          val dayItems = existingNotifications.filter { alert ->
            val alertDays = DayOfWeekItem.parseCodes(alert.selectedDays)
            selectedDays.any { it in alertDays }
          }

          val defaultDayBody = if (dayItems.isEmpty()) {
            "No other alerts scheduled for selected days."
          } else {
            dayItems.joinToString("\n") { alert ->
              "• ${DateUtil.formatTime(alert.hour, alert.minute)} — ${alert.title}"
            }
          }

          val previewDayTitle = displayTitle.ifBlank { title.ifBlank { "Day Alert" } }
          val previewDayBody = displayBody.ifBlank { defaultDayBody }
          val previewActionLabel = actionButtonLabel.ifBlank { "Skip Today" }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = DarkElevated,
            border = BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(ButtonGrey),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.Notifications, contentDescription = null, tint = PureWhite, modifier = Modifier.size(10.dp))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("Notify", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                if (displaySubtext.isNotBlank()) {
                  Text(" • $displaySubtext", color = TextTertiary, fontSize = 11.sp)
                }
                Text(" • now", color = TextTertiary, fontSize = 11.sp)
              }

              Spacer(modifier = Modifier.height(6.dp))

              Text(
                text = previewDayTitle,
                color = PureWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
              )

              Spacer(modifier = Modifier.height(4.dp))

              Text(
                text = previewDayBody,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
              )

              Spacer(modifier = Modifier.height(10.dp))

              // Action button preview
              Box(
                modifier = Modifier
                  .background(ButtonGrey, RoundedCornerShape(6.dp))
                  .border(1.dp, ButtonGreyBorder, RoundedCornerShape(6.dp))
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text(
                  text = previewActionLabel,
                  color = PureWhite,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }

          // Full Display Editing Controls for Day Alert
          AnimatedVisibility(visible = isEditingDisplay) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
              OutlinedTextField(
                value = displayTitle,
                onValueChange = { displayTitle = it },
                label = { Text("Display title override", color = TextSecondary, fontSize = 11.sp) },
                placeholder = { Text(title.ifBlank { "Day Alert" }, color = TextTertiary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = ButtonGreyBorder,
                  unfocusedBorderColor = DarkBorder,
                  focusedTextColor = PureWhite,
                  unfocusedTextColor = PureWhite,
                  focusedContainerColor = DarkElevated,
                  unfocusedContainerColor = DarkElevated
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("day_display_title_input")
              )

              Spacer(modifier = Modifier.height(8.dp))

              OutlinedTextField(
                value = displayBody,
                onValueChange = { displayBody = it },
                label = { Text("Display body / text override", color = TextSecondary, fontSize = 11.sp) },
                placeholder = { Text("Custom summary text or notes", color = TextTertiary) },
                minLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = ButtonGreyBorder,
                  unfocusedBorderColor = DarkBorder,
                  focusedTextColor = PureWhite,
                  unfocusedTextColor = PureWhite,
                  focusedContainerColor = DarkElevated,
                  unfocusedContainerColor = DarkElevated
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("day_display_body_input")
              )

              Spacer(modifier = Modifier.height(8.dp))

              OutlinedTextField(
                value = actionButtonLabel,
                onValueChange = { actionButtonLabel = it },
                label = { Text("Action button label", color = TextSecondary, fontSize = 11.sp) },
                placeholder = { Text("Skip Today", color = TextTertiary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = ButtonGreyBorder,
                  unfocusedBorderColor = DarkBorder,
                  focusedTextColor = PureWhite,
                  unfocusedTextColor = PureWhite,
                  focusedContainerColor = DarkElevated,
                  unfocusedContainerColor = DarkElevated
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("action_button_label_input")
              )

              Spacer(modifier = Modifier.height(8.dp))

              OutlinedTextField(
                value = displaySubtext,
                onValueChange = { displaySubtext = it },
                label = { Text("Display subtext (optional)", color = TextSecondary, fontSize = 11.sp) },
                placeholder = { Text("e.g. Day summary", color = TextTertiary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = ButtonGreyBorder,
                  unfocusedBorderColor = DarkBorder,
                  focusedTextColor = PureWhite,
                  unfocusedTextColor = PureWhite,
                  focusedContainerColor = DarkElevated,
                  unfocusedContainerColor = DarkElevated
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("day_display_subtext_input")
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      val currentItem = NotificationItem(
        id = itemToEdit?.id ?: 0L,
        title = title.ifBlank { if (notificationType == NotificationType.DAY_ALERT) "Day Alert" else "Alert" },
        body = body,
        type = notificationType.name,
        isRecurring = isRecurring,
        selectedDays = selectedDays.joinToString(","),
        hour = selectedHour,
        minute = selectedMinute,
        isEnabled = true,
        expirationMinutes = expirationMinutes,
        hasActionButton = notificationType == NotificationType.DAY_ALERT,
        actionButtonLabel = actionButtonLabel.ifBlank { "Skip Today" },
        displayTitle = displayTitle,
        displayBody = displayBody,
        displaySubtext = displaySubtext
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        GreyButton(
          text = "Test",
          onClick = { onTestSend(currentItem) },
          modifier = Modifier.weight(1f),
          testTag = "editor_test_button"
        )

        GreyButton(
          text = "Save",
          onClick = { onSave(currentItem) },
          modifier = Modifier.weight(1f),
          testTag = "editor_save_button"
        )
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
