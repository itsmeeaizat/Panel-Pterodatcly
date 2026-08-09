package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.ui.theme.Emerald500
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.data.model.ConsoleLog
import com.example.data.model.PowerSignal
import com.example.data.model.PterodactylServer
import com.example.data.model.ServerStats
import com.example.data.model.ServerStatus
import com.example.ui.components.PowerControlButtons
import com.example.ui.components.ResourceGauge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerDetailScreen(
    server: PterodactylServer,
    stats: ServerStats?,
    consoleLogs: List<ConsoleLog>,
    onBack: () -> Unit,
    onSendPowerSignal: (PowerSignal) -> Unit,
    onSendConsoleCommand: (String) -> Unit,
    onRefreshStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var commandInput by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(server.identifier) {
        while (true) {
            onRefreshStats()
            delay(5000)
        }
    }

    val currentStatus = if (server.isSuspended) {
        ServerStatus.SUSPENDED
    } else {
        when (stats?.currentState?.lowercase()) {
            "running" -> ServerStatus.RUNNING
            "starting" -> ServerStatus.STARTING
            "stopping" -> ServerStatus.STOPPING
            "offline", "stopped" -> ServerStatus.OFFLINE
            "suspended" -> ServerStatus.SUSPENDED
            else -> server.currentStatus
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("button_back_detail")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                title = {
                    Column {
                        Text(
                            text = server.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "${server.node} • ID: ${server.identifier}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    StatusBadge(status = currentStatus, modifier = Modifier.padding(end = 12.dp))
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Power Action Bar
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Kontrol Daya Server",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PowerControlButtons(
                        status = currentStatus,
                        onSendSignal = onSendPowerSignal,
                        compact = false
                    )
                }
            }

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ringkasan Stats", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    },
                    modifier = Modifier.testTag("tab_stats")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Konsol Log", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    },
                    modifier = Modifier.testTag("tab_console")
                )
            }

            if (selectedTab == 0) {
                // Overview Tab Content
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Card Informasi Umum & Egg (Clean Light Style)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Informasi VPS",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Egg: ${server.eggName.ifEmpty { server.description.ifEmpty { "Minecraft / Custom Node" } }}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StatusBadge(status = currentStatus)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Node Lokasi: ${server.node} • Alokasi: ${server.primaryAllocation}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    item {
                        // Real-time Resource Utilization Gauges
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Penggunaan Sumber Daya Real-time",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(Emerald500, CircleShape)
                                            )
                                            Text(
                                                text = "Sinkronisasi API Panel (5s)",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    IconButton(onClick = onRefreshStats) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Refresh Stats",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                val cpuPercent = stats?.cpuAbsolutePercentage ?: 0f
                                val memUsedMb = stats?.memoryMb ?: 0L
                                val memLimitMb = server.limits.memoryMb
                                val diskUsedMb = stats?.diskMb ?: 0L
                                val diskLimitMb = server.limits.diskMb

                                ResourceGauge(
                                    title = "Penggunaan CPU",
                                    currentValueText = "%.1f%%".format(cpuPercent),
                                    limitValueText = "Batas ${server.limits.cpuPercentage.toInt()}%",
                                    progressFraction = (cpuPercent / server.limits.cpuPercentage).coerceIn(0f, 1f),
                                    icon = Icons.Default.Speed,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                ResourceGauge(
                                    title = "Penggunaan Memori (RAM)",
                                    currentValueText = "$memUsedMb MB",
                                    limitValueText = "Batas $memLimitMb MB",
                                    progressFraction = if (memLimitMb > 0) (memUsedMb.toFloat() / memLimitMb.toFloat()) else 0f,
                                    icon = Icons.Default.Memory,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                val formattedDiskUsed = if (diskUsedMb >= 1024) "%.1f GB".format(diskUsedMb / 1024f) else "$diskUsedMb MB"
                                val formattedDiskLimit = if (diskLimitMb >= 1024) "${diskLimitMb / 1024} GB" else "$diskLimitMb MB"

                                ResourceGauge(
                                    title = "Penyimpanan Disk",
                                    currentValueText = "$formattedDiskUsed / $formattedDiskLimit",
                                    limitValueText = "Kapasitas Total $formattedDiskLimit",
                                    progressFraction = if (diskLimitMb > 0) (diskUsedMb.toFloat() / diskLimitMb.toFloat()) else 0f,
                                    icon = Icons.Default.Storage,
                                    isAnimated = false,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    item {
                        // Connection & SFTP Details Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Detail Alokasi Network & SFTP",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                InfoRow(
                                    label = "Alokasi IP Utama",
                                    value = server.primaryAllocation,
                                    onCopy = {
                                        clipboardManager.setText(AnnotatedString(server.primaryAllocation))
                                    }
                                )

                                InfoRow(
                                    label = "Host SFTP",
                                    value = "${server.sftpDetails.ip}:${server.sftpDetails.port}",
                                    onCopy = {
                                        clipboardManager.setText(AnnotatedString("${server.sftpDetails.ip}:${server.sftpDetails.port}"))
                                    }
                                )

                                InfoRow(
                                    label = "Username SFTP",
                                    value = "${server.sftpDetails.ip.replace(".", "")}.${server.identifier}",
                                    onCopy = {
                                        clipboardManager.setText(AnnotatedString("${server.sftpDetails.ip.replace(".", "")}.${server.identifier}"))
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                // Console Tab Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    val listState = rememberLazyListState()

                    LaunchedEffect(consoleLogs.size) {
                        if (consoleLogs.isNotEmpty()) {
                            listState.animateScrollToItem(consoleLogs.size - 1)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(TerminalBg)
                            .padding(10.dp)
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(consoleLogs) { log ->
                                val timeString = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(
                                    Date(log.timestamp)
                                )
                                Text(
                                    text = "[$timeString] ${log.message}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = when {
                                        log.isError -> Rose500
                                        log.isSystem -> Cyan400
                                        else -> TerminalGreen
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Command Prompt Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commandInput,
                            onValueChange = { commandInput = it },
                            placeholder = { Text("Kirim perintah RCON...", fontSize = 12.sp, fontFamily = FontFamily.Monospace) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_console_command")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (commandInput.isNotBlank()) {
                                    onSendConsoleCommand(commandInput)
                                    commandInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.height(52.dp).testTag("button_send_command")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Kirim",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        IconButton(onClick = onCopy) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Salin",
                tint = Cyan400,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
