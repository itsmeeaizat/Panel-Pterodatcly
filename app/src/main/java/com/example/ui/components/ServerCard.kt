package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PowerSignal
import com.example.data.model.PterodactylServer
import com.example.data.model.ServerStats
import com.example.data.model.ServerStatus
import com.example.ui.theme.PurplePteroPrimary

@Composable
fun ServerCard(
    server: PterodactylServer,
    stats: ServerStats?,
    onServerClick: () -> Unit,
    onSendPowerSignal: (PowerSignal) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentStatus = when (stats?.currentState?.lowercase()) {
        "running" -> ServerStatus.RUNNING
        "starting" -> ServerStatus.STARTING
        "stopping" -> ServerStatus.STOPPING
        "offline" -> ServerStatus.OFFLINE
        else -> server.currentStatus
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onServerClick() }
            .testTag("server_card_${server.identifier}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: Server Name, Node, Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = server.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = server.primaryAllocation,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = server.node,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Admin Mode Badges: Owner User & Egg Template
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Pemilik: ${server.ownerName}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = server.eggName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        if (server.isSuspended) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "SUSPENDED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                }

                StatusBadge(status = currentStatus)
            }

            if (server.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = server.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Resource Gauges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val cpuPercent = stats?.cpuAbsolutePercentage ?: 0f
                val memUsedMb = stats?.memoryMb ?: 0L
                val memLimitMb = server.limits.memoryMb
                val diskUsedMb = stats?.diskMb ?: 0L
                val diskLimitMb = server.limits.diskMb

                ResourceGauge(
                    title = "CPU",
                    currentValueText = "%.1f%%".format(cpuPercent),
                    limitValueText = "${server.limits.cpuPercentage.toInt()}% max",
                    progressFraction = (cpuPercent / server.limits.cpuPercentage).coerceIn(0f, 1f),
                    icon = Icons.Default.Speed,
                    modifier = Modifier.weight(1f)
                )

                ResourceGauge(
                    title = "RAM",
                    currentValueText = "$memUsedMb MB",
                    limitValueText = "$memLimitMb MB",
                    progressFraction = if (memLimitMb > 0) (memUsedMb.toFloat() / memLimitMb.toFloat()) else 0f,
                    icon = Icons.Default.Memory,
                    modifier = Modifier.weight(1f)
                )

                ResourceGauge(
                    title = "Disk",
                    currentValueText = if (diskUsedMb >= 1024) "%.1f GB".format(diskUsedMb / 1024f) else "$diskUsedMb MB",
                    limitValueText = if (diskLimitMb >= 1024) "${diskLimitMb / 1024} GB" else "$diskLimitMb MB",
                    progressFraction = if (diskLimitMb > 0) (diskUsedMb.toFloat() / diskLimitMb.toFloat()) else 0f,
                    icon = Icons.Default.Storage,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Power Actions
            PowerControlButtons(
                status = currentStatus,
                onSendSignal = onSendPowerSignal,
                compact = true
            )
        }
    }
}
