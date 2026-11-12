package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayOfWeekItem
import com.example.ui.theme.AccentRed
import com.example.ui.theme.ButtonGrey
import com.example.ui.theme.ButtonGreyBorder
import com.example.ui.theme.ButtonText
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun MonochromeCard(
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
  border: BorderStroke? = BorderStroke(1.dp, DarkBorder),
  content: @Composable () -> Unit
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    border = border
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      content()
    }
  }
}

@Composable
fun GreyButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String? = null,
  isAlert: Boolean = false
) {
  Button(
    onClick = onClick,
    shape = RoundedCornerShape(8.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = if (isAlert) Color(0xFF3E1F1F) else ButtonGrey,
      contentColor = if (isAlert) AccentRed else ButtonText
    ),
    border = BorderStroke(1.dp, if (isAlert) AccentRed.copy(alpha = 0.5f) else ButtonGreyBorder),
    modifier = modifier.then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
  ) {
    Text(
      text = text,
      fontWeight = FontWeight.Medium,
      fontSize = 13.sp
    )
  }
}

@Composable
fun DayChip(
  day: DayOfWeekItem,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .size(36.dp)
      .clip(CircleShape)
      .background(if (isSelected) ButtonGrey else DarkSurfaceVariant)
      .clickable { onClick() },
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = day.shortName,
      color = if (isSelected) PureWhite else TextSecondary,
      fontWeight = FontWeight.SemiBold,
      fontSize = 13.sp
    )
  }
}

@Composable
fun MonochromeSwitchRow(
  title: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  testTag: String? = null
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = title,
      color = TextPrimary,
      fontSize = 14.sp,
      fontWeight = FontWeight.Normal
    )

    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      modifier = Modifier.then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
      colors = SwitchDefaults.colors(
        checkedThumbColor = PureWhite,
        checkedTrackColor = ButtonGrey,
        uncheckedThumbColor = TextSecondary,
        uncheckedTrackColor = DarkElevated,
        uncheckedBorderColor = DarkBorder
      )
    )
  }
}

/**
 * 3-second "Deleted" pop-up with "Undo" button
 */
@Composable
fun UndoDeletePopup(
  visible: Boolean,
  onUndo: () -> Unit,
  modifier: Modifier = Modifier
) {
  AnimatedVisibility(
    visible = visible,
    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    modifier = modifier
  ) {
    Surface(
      shape = RoundedCornerShape(10.dp),
      color = Color(0xFF1E1E1E),
      shadowElevation = 8.dp,
      border = BorderStroke(1.dp, Color(0xFF4A4A4A)),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .testTag("undo_delete_popup")
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Deleted",
          color = PureWhite,
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium
        )

        Surface(
          shape = RoundedCornerShape(6.dp),
          color = Color(0xFF333333),
          border = BorderStroke(1.dp, Color(0xFF666666)),
          modifier = Modifier
            .clickable { onUndo() }
            .testTag("undo_button")
        ) {
          Text(
            text = "Undo",
            color = PureWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
          )
        }
      }
    }
  }
}
