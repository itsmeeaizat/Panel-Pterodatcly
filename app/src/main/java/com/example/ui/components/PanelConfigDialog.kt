package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Surface
import com.example.data.config.RawConfigFileData
import com.example.data.model.ConnectionTestResult
import com.example.data.model.PanelAccount
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Rose500

@Composable
fun PanelConfigDialog(
    activeAccount: PanelAccount?,
    allAccounts: List<PanelAccount>,
    rawConfigFileData: RawConfigFileData? = null,
    isTestingConnection: Boolean = false,
    connectionTestResult: ConnectionTestResult? = null,
    onTestConnection: (url: String, apiKey: String) -> Unit = { _, _ -> },
    onClearTestResult: () -> Unit = {},
    onDismiss: () -> Unit,
    onSaveAccount: (name: String, url: String, apiKey: String, id: Long, isDefault: Boolean) -> Unit,
    onSwitchAccount: (Long) -> Unit,
    onDeleteAccount: (PanelAccount) -> Unit,
    onRestoreDefaultAccount: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var panelName by remember { mutableStateOf("") }
    var panelUrl by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var editingAccount by remember { mutableStateOf<PanelAccount?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
        title = {
            Column {
                Text(
                    text = "Konfigurasi Panel Pterodactyl",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Kelola koneksi server panel dan API Key Anda",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
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
                        onClick = { 
                            selectedTab = 0
                            editingAccount = null
                            panelName = ""
                            panelUrl = ""
                            apiKey = ""
                            onClearTestResult()
                        },
                        text = { Text("Koneksi Saved", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { 
                            selectedTab = 1
                            onClearTestResult()
                        },
                        text = { Text(if (editingAccount != null) "Edit Akun" else "+ Tambah Baru", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Connection Test Feedback Status Banner (Shared)
                if (isTestingConnection) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Sedang menguji koneksi ke Panel...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                } else if (connectionTestResult != null) {
                    val isSuccess = connectionTestResult.isSuccess
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSuccess) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .testTag("connection_test_result_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (isSuccess) Emerald500 else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = connectionTestResult.message,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSuccess) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                )
                                if (connectionTestResult.details.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = connectionTestResult.details,
                                        fontSize = 11.sp,
                                        color = if (isSuccess) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }

                if (selectedTab == 0) {
                    // Saved Accounts List
                    if (allAccounts.isEmpty()) {
                        Text(
                            text = "Belum ada koneksi panel tersimpan.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 220.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(allAccounts) { account ->
                                val isActive = account.id == activeAccount?.id
                                val isRawDefault = account.isDefault || (rawConfigFileData != null && 
                                    rawConfigFileData.panelUrl.isNotBlank() && 
                                    account.panelUrl.trimEnd('/') == rawConfigFileData.panelUrl.trimEnd('/') && 
                                    account.apiKey.trim() == rawConfigFileData.apiKey.trim()) ||
                                    account.name.contains("Bawaan", ignoreCase = true)

                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("account_item_${account.id}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = account.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.weight(1f, fill = false)
                                                )
                                                if (isRawDefault) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Surface(
                                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                                        shape = RoundedCornerShape(6.dp)
                                                    ) {
                                                        Text(
                                                            text = "Bawaan",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                                if (isActive) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "Active",
                                                        tint = Emerald500,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = account.panelUrl,
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = {
                                                    editingAccount = account
                                                    panelName = account.name
                                                    panelUrl = account.panelUrl
                                                    apiKey = account.apiKey
                                                    selectedTab = 1
                                                },
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .testTag("button_edit_saved_${account.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit Akun",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = { onTestConnection(account.panelUrl, account.apiKey) },
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .testTag("button_ping_saved_${account.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.NetworkCheck,
                                                    contentDescription = "Uji Koneksi Ping",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            if (!isActive) {
                                                TextButton(
                                                    onClick = { onSwitchAccount(account.id) },
                                                    contentPadding = PaddingValues(horizontal = 6.dp)
                                                ) {
                                                    Text("Pilih", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            IconButton(
                                                onClick = { onDeleteAccount(account) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Hapus",
                                                    tint = Rose500,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { onRestoreDefaultAccount() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("button_restore_default_account")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restore,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pulihkan Akun Bawaan (assets/default_pterodactyl.json)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Add New Form
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = panelName,
                            onValueChange = { 
                                panelName = it 
                                if (connectionTestResult != null) onClearTestResult()
                            },
                            label = { Text("Nama Panel (opsional)") },
                            placeholder = { Text("Contoh: VPS Game Main") },
                            leadingIcon = {
                                Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_panel_name")
                        )

                        OutlinedTextField(
                            value = panelUrl,
                            onValueChange = { 
                                panelUrl = it 
                                if (connectionTestResult != null) onClearTestResult()
                            },
                            label = { Text("URL Domain Panel") },
                            placeholder = { Text("https://panel.domainanda.com") },
                            leadingIcon = {
                                Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_panel_url")
                        )

                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = { 
                                apiKey = it 
                                if (connectionTestResult != null) onClearTestResult()
                            },
                            label = { Text("API Key (ptla_ Admin / ptlc_ Client)") },
                            placeholder = { Text("ptla_xxxxxxxxxxxxxx") },
                            leadingIcon = {
                                Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_api_key")
                        )

                        Text(
                            text = "Petunjuk Mode Admin: Gunakan Admin Application Token (ptla_...) dari Admin -> Application API Credentials di Panel Pterodactyl Anda.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Uji Koneksi Button
                        OutlinedButton(
                            onClick = { onTestConnection(panelUrl, apiKey) },
                            enabled = !isTestingConnection && panelUrl.isNotBlank() && apiKey.isNotBlank(),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("button_test_connection")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NetworkCheck,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isTestingConnection) "Memeriksa Koneksi..." else "Uji Koneksi (Test Ping)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedTab == 1) {
                Button(
                    onClick = {
                        if (panelUrl.isNotBlank() && apiKey.isNotBlank()) {
                            onSaveAccount(
                                panelName, 
                                panelUrl, 
                                apiKey, 
                                editingAccount?.id ?: 0L, 
                                editingAccount?.isDefault ?: false
                            )
                            editingAccount = null
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("button_save_panel_config")
                ) {
                    Icon(
                        imageVector = if (editingAccount != null) Icons.Default.Edit else Icons.Default.Add, 
                        contentDescription = null, 
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (editingAccount != null) "Simpan Perubahan" else "Hubungkan Panel", 
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Tutup", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        dismissButton = {
            if (selectedTab == 1) {
                TextButton(onClick = { selectedTab = 0 }) {
                    Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    )
}

