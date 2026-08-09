package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PowerSignal
import com.example.data.model.ServerStatus
import com.example.ui.theme.Amber500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate800

@Composable
fun PowerControlButtons(
    status: ServerStatus,
    onSendSignal: (PowerSignal) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val isRunning = status == ServerStatus.RUNNING

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (!isRunning) {
            Button(
                onClick = { onSendSignal(PowerSignal.START) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Emerald500,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(if (compact) 38.dp else 44.dp)
                    .testTag("power_start_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Start",
                    fontSize = if (compact) 12.sp else 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Button(
                onClick = { onSendSignal(PowerSignal.RESTART) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Amber500,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(if (compact) 38.dp else 44.dp)
                    .testTag("power_restart_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Restart",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Restart",
                    fontSize = if (compact) 12.sp else 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = { onSendSignal(PowerSignal.STOP) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Rose500,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(if (compact) 38.dp else 44.dp)
                    .testTag("power_stop_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Stop",
                    fontSize = if (compact) 12.sp else 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (!compact && isRunning) {
            OutlinedButton(
                onClick = { onSendSignal(PowerSignal.KILL) },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Rose500
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(44.dp)
                    .testTag("power_kill_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Kill",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Kill", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
