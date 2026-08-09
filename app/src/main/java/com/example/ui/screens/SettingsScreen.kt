package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.data.config.ConfigSourceMode
import com.example.data.config.EffectiveConfig
import com.example.data.config.RawConfigFileData
import com.example.ui.components.HybridConfigCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDemoModeActive: Boolean,
    effectiveConfig: EffectiveConfig? = null,
    rawConfigFileData: RawConfigFileData? = null,
    onSelectSourceMode: (ConfigSourceMode) -> Unit = {},
    onSaveCustomConfig: (panelUrl: String, apiKey: String) -> Unit = { _, _ -> },
    onToggleDemoMode: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    onOpenPanelConfig: (() -> Unit)? = null,
    onRestoreDefaultAccount: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var localDemoChecked by remember(isDemoModeActive) { mutableStateOf(isDemoModeActive) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan Aplikasi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("button_back_settings")
                    ) {
                        Text("← Kembali", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
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
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Kartu Hybrid Configuration System
            if (effectiveConfig != null && rawConfigFileData != null) {
                HybridConfigCard(
                    effectiveConfig = effectiveConfig,
                    rawConfigFileData = rawConfigFileData,
                    onSelectSourceMode = onSelectSourceMode,
                    onSaveCustomConfig = onSaveCustomConfig
                )
            }

            // Kartu Pengaturan Mode Demo
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_demo_mode")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mode Demo (Simulasi Server)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Gunakan data tiruan untuk mencoba aplikasi tanpa harus menghubungkan panel Pterodactyl asli.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))

                    // Tombol Switch / Saklar Mode Demo
                    Switch(
                        checked = localDemoChecked,
                        onCheckedChange = { newValue ->
                            localDemoChecked = newValue
                            onToggleDemoMode(newValue)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("switch_demo_mode")
                    )
                }
            }

            // Kartu Kelola API Key / Panel
            if (onOpenPanelConfig != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Koneksi Panel Pterodactyl Terdaftar",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Daftar & atur multiple akun/koneksi panel Pterodactyl yang tersimpan.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = onOpenPanelConfig,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Kelola Akun", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            if (onRestoreDefaultAccount != null) {
                                androidx.compose.material3.OutlinedButton(
                                    onClick = onRestoreDefaultAccount,
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Pulihkan Bawaan", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Informasi Tambahan di Menu Pengaturan
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tentang Aplikasi",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pterodactyl Mobile Manager v1.0\nFitur Hybrid Configuration & Modern Material 3.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
