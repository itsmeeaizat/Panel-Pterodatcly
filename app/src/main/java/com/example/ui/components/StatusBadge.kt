package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ServerStatus
import com.example.ui.theme.Amber500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.OfflineBadgeBg
import com.example.ui.theme.OfflineBadgeText
import com.example.ui.theme.Rose500
import com.example.ui.theme.RunningBadgeBg
import com.example.ui.theme.RunningBadgeText
import com.example.ui.theme.StartingBadgeBg
import com.example.ui.theme.StartingBadgeText

@Composable
fun StatusBadge(
    status: ServerStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, dotColor, label) = when (status) {
        ServerStatus.RUNNING -> Quadruple(
            RunningBadgeBg,
            RunningBadgeText,
            Emerald500,
            "Running"
        )
        ServerStatus.STARTING -> Quadruple(
            StartingBadgeBg,
            StartingBadgeText,
            Amber500,
            "Starting..."
        )
        ServerStatus.STOPPING -> Quadruple(
            StartingBadgeBg,
            StartingBadgeText,
            Amber500,
            "Stopping..."
        )
        ServerStatus.OFFLINE -> Quadruple(
            OfflineBadgeBg,
            OfflineBadgeText,
            Rose500,
            "Offline"
        )
        ServerStatus.SUSPENDED -> Quadruple(
            Color(0xFFE2E8F0),
            Color(0xFF64748B),
            Color(0xFF64748B),
            "Suspended"
        )
        ServerStatus.UNKNOWN -> Quadruple(
            Color(0xFFE2E8F0),
            Color(0xFF64748B),
            Color(0xFF64748B),
            "Unknown"
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
