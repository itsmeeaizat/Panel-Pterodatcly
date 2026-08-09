package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountInfo
import com.example.data.model.PanelAccount
import com.example.data.model.PowerSignal
import com.example.data.model.PterodactylServer
import com.example.data.model.ServerStats
import com.example.data.model.ServerStatus
import com.example.ui.UiState
import com.example.ui.components.ServerCard
import com.example.ui.theme.Amber500
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    servers: List<PterodactylServer>,
    serverStats: Map<String, ServerStats>,
    activeAccount: PanelAccount?,
    accountInfo: AccountInfo?,
    uiState: UiState,
    isDemoMode: Boolean,
    onRefresh: () -> Unit,
    onServerSelected: (PterodactylServer) -> Unit,
    onPowerSignal: (PterodactylServer, PowerSignal) -> Unit,
    onOpenPanelSettings: () -> Unit,
    onOpenAccountSelection: () -> Unit = {},
    onCreateServerClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredServers = servers.filter { server ->
        val matchesSearch = server.name.contains(searchQuery, ignoreCase = true) ||
                server.node.contains(searchQuery, ignoreCase = true) ||
                server.ownerName.contains(searchQuery, ignoreCase = true) ||
                server.eggName.contains(searchQuery, ignoreCase = true) ||
                server.identifier.contains(searchQuery, ignoreCase = true)

        val stats = serverStats[server.identifier]
        val status = when (stats?.currentState?.lowercase()) {
            "running" -> ServerStatus.RUNNING
            "starting" -> ServerStatus.STARTING
            "stopping" -> ServerStatus.STOPPING
            "offline" -> ServerStatus.OFFLINE
            else -> server.currentStatus
        }

        val matchesFilter = when (selectedFilter) {
            "Online" -> status == ServerStatus.RUNNING
            "Starting" -> status == ServerStatus.STARTING
            "Offline" -> status == ServerStatus.OFFLINE
            else -> true
        }

        matchesSearch && matchesFilter
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = "Logo",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.widthIn(max = 140.dp)) {
                            Text(
                                text = "Pterodactyl Admin",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            LiveConnectionIndicator(
                                uiState = uiState,
                                isDemoMode = isDemoMode,
                                activeAccount = activeAccount
                            )
                        }
                    }
                },
                actions = {
                    // Tombol Akun di Atas Kanan Dashboard Beranda
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .padding(end = 2.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onOpenAccountSelection() }
                            .testTag("button_account_selection_header")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Pilih Akun",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val displayName = when {
                                activeAccount == null -> "Account"
                                activeAccount.name.contains("Bawaan", ignoreCase = true) || activeAccount.isDefault -> "Account"
                                else -> activeAccount.name
                            }
                            Text(
                                text = displayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 80.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.testTag("button_refresh_dashboard")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onOpenPanelSettings,
                        modifier = Modifier.testTag("button_panel_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateServerClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Buat Server", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_create_server")
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Demo Mode Notice Banner
            if (isDemoMode) {
                Surface(
                    color = Amber500.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenPanelSettings() }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Amber500,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Mode Demo Aktif. Sentuh di sini untuk memasukkan Client API Key Panel Anda.",
                                fontSize = 11.sp,
                                color = Amber500,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Global Status Section (Matching Professional Polish Theme)
            val totalServers = servers.size
            val onlineCount = servers.count { server ->
                val stats = serverStats[server.identifier]
                stats?.currentState?.lowercase() == "running" || server.currentStatus == ServerStatus.RUNNING
            }
            val totalRamMb = servers.sumOf { it.limits.memoryMb }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "STATUS GLOBAL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$onlineCount/$totalServers Server Aktif",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Total Alokasi RAM: ${totalRamMb / 1024} GB",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    val hasConfiguredAccount = activeAccount != null &&
                            activeAccount.panelUrl.isNotBlank() &&
                            activeAccount.apiKey.isNotBlank() &&
                            activeAccount.apiKey != com.example.data.demo.DemoDataGenerator.demoAccount.apiKey
                    val isLiveConnected = uiState !is UiState.Error && (hasConfiguredAccount || isDemoMode)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            !isLiveConnected -> MaterialTheme.colorScheme.error
                                            isDemoMode -> Amber500
                                            else -> Emerald500
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when {
                                    !isLiveConnected -> "OFFLINE"
                                    onlineCount == totalServers && totalServers > 0 -> "ALL HEALTHY"
                                    else -> "OPERATIONAL"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLiveConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari server, node, IP...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Cari", tint = MaterialTheme.colorScheme.primary)
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("input_search_server")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("All", "Online", "Starting", "Offline")
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = MaterialTheme.colorScheme.outline,
                            selectedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("filter_chip_$filter")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Loading / Error Banner
            AnimatedVisibility(visible = uiState is UiState.Loading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Cyan400,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Memuat data server...",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (uiState is UiState.Error) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Rose500.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = uiState.message,
                            color = Rose500,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onRefresh) {
                            Text("Coba Lagi", color = Cyan400, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Server List
            if (filteredServers.isEmpty() && uiState !is UiState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (servers.isEmpty()) "Belum Ada Server Terhubung" else "Tidak Ada Server Ditemukan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (servers.isEmpty())
                                "Hubungkan URL Panel Pterodactyl & API Key Anda di Pengaturan, atau aktifkan Mode Demo untuk melihat simulasi server."
                            else
                                "Coba ubah kata kunci pencarian atau ganti filter status.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (servers.isEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onOpenPanelSettings,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Buka Pengaturan", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredServers, key = { it.identifier }) { server ->
                        ServerCard(
                            server = server,
                            stats = serverStats[server.identifier],
                            onServerClick = { onServerSelected(server) },
                            onSendPowerSignal = { signal -> onPowerSignal(server, signal) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Slate900)
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}

@Composable
fun LiveConnectionIndicator(
    uiState: UiState,
    isDemoMode: Boolean,
    activeAccount: PanelAccount? = null,
    modifier: Modifier = Modifier
) {
    val isError = uiState is UiState.Error
    val isLoading = uiState is UiState.Loading

    // Valid real account exists if panelUrl & apiKey are not blank, and not dummy demo key
    val hasConfiguredAccount = activeAccount != null &&
            activeAccount.panelUrl.isNotBlank() &&
            activeAccount.apiKey.isNotBlank() &&
            activeAccount.apiKey != com.example.data.demo.DemoDataGenerator.demoAccount.apiKey

    // Real live connection or active demo connection
    val isLiveConnected = !isError && (hasConfiguredAccount || isDemoMode)

    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_anim"
    )

    val dotColor = when {
        isError || (!hasConfiguredAccount && !isDemoMode) -> MaterialTheme.colorScheme.error
        isLoading -> Amber500
        isDemoMode -> Amber500
        else -> Emerald500
    }

    val statusText = when {
        isError -> {
            val err = (uiState as UiState.Error).message
            when {
                err.contains("401") || err.contains("403") -> "Terputus (API Key Invalid)"
                err.contains("UnknownHost") || err.contains("404") -> "Terputus (URL Panel Invalid)"
                else -> "Panel Terputus"
            }
        }
        isLoading -> "Menghubungkan ke Panel..."
        !hasConfiguredAccount && !isDemoMode -> "Terputus (Belum Dikonfigurasi)"
        isDemoMode -> "Panel Live (Simulasi Demo)"
        hasConfiguredAccount -> {
            val key = activeAccount!!.apiKey
            when {
                key.startsWith("ptla_") -> "Terhubung (Admin API ptla_)"
                key.startsWith("ptlc_") -> "Terhubung (Client API ptlc_)"
                else -> "Terhubung ke Panel"
            }
        }
        else -> "Terputus"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.testTag("live_connection_indicator")
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (isLiveConnected) dotColor.copy(alpha = alpha) else dotColor)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = statusText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isLiveConnected) dotColor else MaterialTheme.colorScheme.error
        )
    }
}
