package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CreateServerParams
import com.example.data.model.PterodactylAdminUser
import com.example.data.model.PterodactylEgg
import com.example.data.model.PterodactylNode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateServerScreen(
    users: List<PterodactylAdminUser>,
    nodes: List<PterodactylNode>,
    eggs: List<PterodactylEgg>,
    onCreateServer: (CreateServerParams) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var serverName by remember { mutableStateOf("") }
    var selectedUserId by remember { mutableIntStateOf(users.firstOrNull()?.id ?: 1) }
    var selectedNodeId by remember { mutableIntStateOf(nodes.firstOrNull()?.id ?: 1) }
    var selectedEggId by remember { mutableIntStateOf(eggs.firstOrNull()?.id ?: 1) }

    val initialEgg = eggs.find { it.id == selectedEggId } ?: eggs.firstOrNull()
    var dockerImage by remember(selectedEggId) { mutableStateOf(initialEgg?.dockerImage ?: "ghcr.io/pterodactyl/yolks:nodejs_18") }
    var startupCommand by remember(selectedEggId) { mutableStateOf(initialEgg?.startupCommand ?: "npm start") }

    var memoryMbText by remember { mutableStateOf("2048") }
    var cpuPercentageText by remember { mutableStateOf("100") }
    var diskMbText by remember { mutableStateOf("10240") }

    var userDropdownExpanded by remember { mutableStateOf(false) }

    val selectedUser = users.find { it.id == selectedUserId } ?: users.firstOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Buat Server Baru", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Admin Application API (/api/application/servers)", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("button_back_create_server")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Banner Notice Demo / Simulasi
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Mode Demo / Simulasi Admin Panel",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Perubahan atau pembuatan server baru di halaman ini disimulasikan sesuai struktur REST API Pterodactyl Admin asli.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Section 1: Detail Identitas Server & User
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Dns, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("1. Informasi Server & Pemilik", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    OutlinedTextField(
                        value = serverName,
                        onValueChange = { serverName = it },
                        label = { Text("Nama Server") },
                        placeholder = { Text("Contoh: Node.js Express API / Minecraft SMP") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_server_name")
                    )

                    // Owner User Selection
                    Text("Pemilik Server (Owner Client User)", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedUser?.let { "${it.username} (#${it.id} - ${it.fullName})" } ?: "User #${selectedUserId}",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pilih User Pemilik") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            trailingIcon = { Text("▼", modifier = Modifier.padding(end = 12.dp), fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { userDropdownExpanded = true }
                                .testTag("select_user_owner")
                        )

                        DropdownMenu(
                            expanded = userDropdownExpanded,
                            onDismissRequest = { userDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            users.forEach { user ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("${user.username} (#${user.id})", fontWeight = FontWeight.Bold)
                                            Text(user.fullName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    onClick = {
                                        selectedUserId = user.id
                                        userDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Section 2: Node & Egg Selection + Flexible Startup Command
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("2. Pilihan Node, Egg & Startup Perintah", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    // Node choice
                    Text("Pilih Node Lokasi Server:", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        nodes.forEach { node ->
                            val isSelected = node.id == selectedNodeId
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedNodeId = node.id },
                                label = { Text(node.name, fontSize = 12.sp) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    // Egg choice
                    Text("Pilih Application Egg / Lingkungan:", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        eggs.forEach { egg ->
                            val isSelected = egg.id == selectedEggId
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedEggId = egg.id
                                    dockerImage = egg.dockerImage
                                    startupCommand = egg.startupCommand
                                },
                                label = { Text(egg.name, fontSize = 12.sp) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = dockerImage,
                        onValueChange = { dockerImage = it },
                        label = { Text("Docker Image") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_docker_image")
                    )

                    // Flexible Free Text Startup Command TextField
                    OutlinedTextField(
                        value = startupCommand,
                        onValueChange = { startupCommand = it },
                        label = { Text("Startup Command (Perintah Eksekusi Bebas)") },
                        placeholder = { Text("Contoh: npm start, node index.js, python main.py") },
                        leadingIcon = { Icon(Icons.Default.Terminal, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_startup_command")
                    )

                    // Quick Command Snippet Shortcuts
                    Text("Presisi Cepat Command:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            "npm start",
                            "node index.js",
                            "python main.py",
                            "uvicorn main:app --host 0.0.0.0",
                            "java -Xms128M -XX:+UseG1GC -jar {{SERVER_JARFILE}}"
                        ).forEach { cmd ->
                            FilterChip(
                                selected = startupCommand == cmd,
                                onClick = { startupCommand = cmd },
                                label = { Text(cmd, fontSize = 10.sp) }
                            )
                        }
                    }
                }
            }

            // Section 3: Limits Resource (Custom TextField RAM, CPU, SSD)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Memory, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("3. Batasan Sumber Daya (Resource Limits)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    // 1. Memory RAM Input TextField
                    Text("Kapasitas RAM (MB):", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    OutlinedTextField(
                        value = memoryMbText,
                        onValueChange = { memoryMbText = it },
                        label = { Text("Kapasitas RAM (MB)") },
                        placeholder = { Text("Ketik nilai RAM dalam MB (misal: 2048)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_memory_mb")
                    )

                    // Quick RAM preset shortcuts
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("1024" to "1 GB", "2048" to "2 GB", "4096" to "4 GB", "8192" to "8 GB", "16384" to "16 GB").forEach { (valMb, label) ->
                            FilterChip(
                                selected = memoryMbText == valMb,
                                onClick = { memoryMbText = valMb },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }

                    // 2. CPU Limit Input TextField
                    Text("Batas CPU (%):", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    OutlinedTextField(
                        value = cpuPercentageText,
                        onValueChange = { cpuPercentageText = it },
                        label = { Text("Persentase CPU (%)") },
                        placeholder = { Text("Ketik persentase CPU (misal: 50, 100, 200)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_cpu_percent")
                    )

                    // Quick CPU preset shortcuts
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("50" to "50%", "100" to "1 Core (100%)", "200" to "2 Cores (200%)", "400" to "4 Cores (400%)").forEach { (valCpu, label) ->
                            FilterChip(
                                selected = cpuPercentageText == valCpu,
                                onClick = { cpuPercentageText = valCpu },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }

                    // 3. Disk SSD Storage Input TextField
                    Text("Batas Disk Storage SSD (MB):", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    OutlinedTextField(
                        value = diskMbText,
                        onValueChange = { diskMbText = it },
                        label = { Text("Penyimpanan SSD (MB)") },
                        placeholder = { Text("Ketik kapasitas SSD dalam MB (misal: 10240)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_disk_mb")
                    )

                    // Quick SSD preset shortcuts
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("5120" to "5 GB", "10240" to "10 GB", "25600" to "25 GB", "51200" to "50 GB").forEach { (valDisk, label) ->
                            FilterChip(
                                selected = diskMbText == valDisk,
                                onClick = { diskMbText = valDisk },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action submit button
            Button(
                onClick = {
                    if (serverName.isNotBlank()) {
                        val params = CreateServerParams(
                            name = serverName.trim(),
                            ownerUserId = selectedUserId,
                            nodeId = selectedNodeId,
                            nestId = 1,
                            eggId = selectedEggId,
                            dockerImage = dockerImage,
                            startupCommand = startupCommand,
                            memoryMb = memoryMbText.toLongOrNull() ?: 2048,
                            cpuPercentage = cpuPercentageText.toFloatOrNull() ?: 100f,
                            diskMb = diskMbText.toLongOrNull() ?: 10240
                        )
                        onCreateServer(params)
                    }
                },
                enabled = serverName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("button_submit_create_server")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buat Server Sekarang (Admin Mode)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
