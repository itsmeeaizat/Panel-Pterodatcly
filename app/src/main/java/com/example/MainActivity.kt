package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.announcement.AnnouncementData
import com.example.data.announcement.AnnouncementManager
import com.example.data.model.PterodactylServer
import com.example.ui.MainViewModel
import com.example.ui.components.AnnouncementDialog
import com.example.ui.components.PanelConfigDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ServerDetailScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.PterodactylTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PterodactylTheme {
                PterodactylApp(viewModel = viewModel, onShowToast = { msg ->
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                })
            }
        }
    }
}

@Composable
fun PterodactylApp(
    viewModel: MainViewModel,
    onShowToast: (String) -> Unit
) {
    val servers by viewModel.servers.collectAsStateWithLifecycle()
    val serverStats by viewModel.serverStats.collectAsStateWithLifecycle()
    val activeAccount by viewModel.activeAccount.collectAsStateWithLifecycle()
    val allAccounts by viewModel.allPanelAccounts.collectAsStateWithLifecycle()
    val accountInfo by viewModel.accountInfo.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedServer by viewModel.selectedServer.collectAsStateWithLifecycle()
    val consoleLogsMap by viewModel.consoleLogs.collectAsStateWithLifecycle()
    val adminUsers by viewModel.adminUsers.collectAsStateWithLifecycle()
    val adminNodes by viewModel.adminNodes.collectAsStateWithLifecycle()
    val adminEggs by viewModel.adminEggs.collectAsStateWithLifecycle()
    val isTestingConnection by viewModel.isTestingConnection.collectAsStateWithLifecycle()
    val connectionTestResult by viewModel.connectionTestResult.collectAsStateWithLifecycle()
    val effectiveConfig by viewModel.effectiveConfig.collectAsStateWithLifecycle()
    val rawConfigFileData by viewModel.rawConfigFileData.collectAsStateWithLifecycle()
    val isDemoModeActive by viewModel.isDemoModeState.collectAsStateWithLifecycle()

    var showPanelConfigDialog by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

    val context = LocalContext.current
    var activeAnnouncement by remember {
        mutableStateOf<AnnouncementData?>(null)
    }

    LaunchedEffect(Unit) {
        activeAnnouncement = AnnouncementManager.shouldShowAnnouncement(context)
        viewModel.toastEvent.collect { message ->
            onShowToast(message)
        }
    }

    // Display Popup Pengumuman if active and not dismissed
    activeAnnouncement?.let { announcement ->
        AnnouncementDialog(
            announcement = announcement,
            onDismiss = {
                AnnouncementManager.markAsDismissed(context, announcement.version)
                activeAnnouncement = null
            }
        )
    }

    Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
        when (screen) {
            Screen.Dashboard -> {
                DashboardScreen(
                    servers = servers,
                    serverStats = serverStats,
                    activeAccount = activeAccount,
                    accountInfo = accountInfo,
                    uiState = uiState,
                    isDemoMode = isDemoModeActive,
                    onRefresh = { viewModel.loadData() },
                    onServerSelected = { server ->
                        viewModel.selectServer(server)
                        currentScreen = Screen.ServerDetail(server)
                    },
                    onPowerSignal = { server, signal ->
                        viewModel.sendPowerSignal(server, signal)
                    },
                    onOpenPanelSettings = { currentScreen = Screen.Settings },
                    onOpenAccountSelection = { showPanelConfigDialog = true },
                    onCreateServerClick = {
                        viewModel.fetchAdminFormData()
                        currentScreen = Screen.CreateServer
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            Screen.CreateServer -> {
                com.example.ui.screens.CreateServerScreen(
                    users = adminUsers,
                    nodes = adminNodes,
                    eggs = adminEggs,
                    onCreateServer = { params ->
                        viewModel.createServer(params) {
                            currentScreen = Screen.Dashboard
                        }
                    },
                    onBackClick = { currentScreen = Screen.Dashboard },
                    modifier = Modifier.fillMaxSize()
                )
            }
            is Screen.Settings -> {
                SettingsScreen(
                    isDemoModeActive = isDemoModeActive,
                    effectiveConfig = effectiveConfig,
                    rawConfigFileData = rawConfigFileData,
                    onSelectSourceMode = { mode -> viewModel.setHybridConfigSourceMode(mode) },
                    onSaveCustomConfig = { url, key -> viewModel.saveCustomApiKeyAndUrl(url, key) },
                    onToggleDemoMode = { enabled -> viewModel.toggleDemoMode(enabled) },
                    onBackClick = { currentScreen = Screen.Dashboard },
                    onOpenPanelConfig = { showPanelConfigDialog = true },
                    onRestoreDefaultAccount = { viewModel.restoreDefaultAccount() },
                    modifier = Modifier.fillMaxSize()
                )
            }
            is Screen.ServerDetail -> {
                val server = selectedServer ?: screen.server
                val logs = consoleLogsMap[server.identifier] ?: emptyList()
                val currentStats = serverStats[server.identifier]

                ServerDetailScreen(
                    server = server,
                    stats = currentStats,
                    consoleLogs = logs,
                    onBack = { currentScreen = Screen.Dashboard },
                    onSendPowerSignal = { signal ->
                        viewModel.sendPowerSignal(server, signal)
                    },
                    onSendConsoleCommand = { cmd ->
                        viewModel.sendConsoleCommand(server.identifier, cmd)
                    },
                    onRefreshStats = { viewModel.refreshSelectedServerStats() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }


    if (showPanelConfigDialog) {
        PanelConfigDialog(
            activeAccount = activeAccount,
            allAccounts = allAccounts,
            rawConfigFileData = rawConfigFileData,
            isTestingConnection = isTestingConnection,
            connectionTestResult = connectionTestResult,
            onTestConnection = { url, apiKey ->
                viewModel.testConnection(url, apiKey)
            },
            onClearTestResult = {
                viewModel.clearConnectionTest()
            },
            onDismiss = {
                viewModel.clearConnectionTest()
                showPanelConfigDialog = false
            },
            onSaveAccount = { name, url, apiKey, id, isDefault ->
                viewModel.savePanelAccount(name, url, apiKey, id, isDefault)
            },
            onSwitchAccount = { accountId ->
                viewModel.switchPanelAccount(accountId)
            },
            onDeleteAccount = { account ->
                viewModel.deletePanelAccount(account)
            },
            onRestoreDefaultAccount = {
                viewModel.restoreDefaultAccount()
            }
        )
    }
}

sealed class Screen {
    object Dashboard : Screen()
    object CreateServer : Screen()
    object Settings : Screen()
    data class ServerDetail(val server: PterodactylServer) : Screen()
}
