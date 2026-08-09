package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.config.ConfigSourceMode
import com.example.data.config.EffectiveConfig
import com.example.data.config.RawConfigFileData
import com.example.data.demo.DemoDataGenerator
import com.example.data.model.AccountInfo
import com.example.data.model.ConsoleLog
import com.example.data.model.PanelAccount
import com.example.data.model.PowerSignal
import com.example.data.model.PterodactylServer
import com.example.data.model.ServerStats
import com.example.data.repository.PterodactylRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val message: String? = null) : UiState()
    data class Error(val message: String) : UiState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PterodactylRepository(application)

    val allPanelAccounts: StateFlow<List<PanelAccount>> = repository.allPanelAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeAccount: StateFlow<PanelAccount?> = repository.activePanelAccount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val servers: StateFlow<List<PterodactylServer>> = repository.serversFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _selectedServer = MutableStateFlow<PterodactylServer?>(null)
    val selectedServer: StateFlow<PterodactylServer?> = _selectedServer.asStateFlow()

    private val _serverStats = MutableStateFlow<Map<String, ServerStats>>(emptyMap())
    val serverStats: StateFlow<Map<String, ServerStats>> = _serverStats.asStateFlow()

    private val _accountInfo = MutableStateFlow<AccountInfo?>(null)
    val accountInfo: StateFlow<AccountInfo?> = _accountInfo.asStateFlow()

    private val _consoleLogs = MutableStateFlow<Map<String, List<ConsoleLog>>>(emptyMap())
    val consoleLogs: StateFlow<Map<String, List<ConsoleLog>>> = _consoleLogs.asStateFlow()

    private val _adminUsers = MutableStateFlow<List<com.example.data.model.PterodactylAdminUser>>(emptyList())
    val adminUsers: StateFlow<List<com.example.data.model.PterodactylAdminUser>> = _adminUsers.asStateFlow()

    private val _adminNodes = MutableStateFlow<List<com.example.data.model.PterodactylNode>>(emptyList())
    val adminNodes: StateFlow<List<com.example.data.model.PterodactylNode>> = _adminNodes.asStateFlow()

    private val _adminEggs = MutableStateFlow<List<com.example.data.model.PterodactylEgg>>(emptyList())
    val adminEggs: StateFlow<List<com.example.data.model.PterodactylEgg>> = _adminEggs.asStateFlow()

    private val _connectionTestResult = MutableStateFlow<com.example.data.model.ConnectionTestResult?>(null)
    val connectionTestResult: StateFlow<com.example.data.model.ConnectionTestResult?> = _connectionTestResult.asStateFlow()

    private val _isTestingConnection = MutableStateFlow(false)
    val isTestingConnection: StateFlow<Boolean> = _isTestingConnection.asStateFlow()

    private val _effectiveConfig = MutableStateFlow(repository.getHybridConfigManager().getEffectiveConfig())
    val effectiveConfig: StateFlow<EffectiveConfig> = _effectiveConfig.asStateFlow()

    private val _rawConfigFileData = MutableStateFlow(repository.getHybridConfigManager().loadRawConfigFile())
    val rawConfigFileData: StateFlow<RawConfigFileData> = _rawConfigFileData.asStateFlow()

    private val _isDemoMode = MutableStateFlow(repository.isDemo())
    val isDemoModeState: StateFlow<Boolean> = _isDemoMode.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    private var statsPollingJob: Job? = null

    init {
        viewModelScope.launch {
            repository.initialize()
            refreshHybridConfigState()
            loadData()
            fetchAdminFormData()
            startStatsPolling()
        }
    }

    fun refreshHybridConfigState() {
        val manager = repository.getHybridConfigManager()
        _effectiveConfig.value = manager.getEffectiveConfig()
        _rawConfigFileData.value = manager.loadRawConfigFile()
    }

    fun setHybridConfigSourceMode(mode: ConfigSourceMode) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.applyHybridConfig(sourceMode = mode)
            refreshHybridConfigState()
            _toastEvent.emit("Sumber Konfigurasi diubah ke: ${if (mode == ConfigSourceMode.RAW_FILE) "File Mentah (assets/app_config.json)" else "Konfigurasi Kustom Aplikasi"}")
            loadData()
            fetchAdminFormData()
        }
    }

    fun saveCustomApiKeyAndUrl(panelUrl: String, apiKey: String, accountName: String = "Custom Panel") {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.applyHybridConfig(
                sourceMode = ConfigSourceMode.CUSTOM_MANUAL,
                customUrl = panelUrl,
                customApiKey = apiKey,
                accountName = accountName
            )
            refreshHybridConfigState()
            _toastEvent.emit("API Key & URL Kustom berhasil diperbarui!")
            loadData()
            fetchAdminFormData()
        }
    }

    fun testConnection(panelUrl: String, apiKey: String) {
        viewModelScope.launch {
            _isTestingConnection.value = true
            _connectionTestResult.value = null
            val res = repository.testConnection(panelUrl, apiKey)
            _connectionTestResult.value = res
            _isTestingConnection.value = false
        }
    }

    fun clearConnectionTest() {
        _connectionTestResult.value = null
        _isTestingConnection.value = false
    }

    fun fetchAdminFormData() {
        viewModelScope.launch {
            val usersRes = repository.getAdminUsers()
            if (usersRes.isSuccess) _adminUsers.value = usersRes.getOrDefault(emptyList())

            val nodesRes = repository.getAdminNodes()
            if (nodesRes.isSuccess) _adminNodes.value = nodesRes.getOrDefault(emptyList())

            val eggsRes = repository.getAdminEggs()
            if (eggsRes.isSuccess) _adminEggs.value = eggsRes.getOrDefault(emptyList())
        }
    }

    fun createServer(params: com.example.data.model.CreateServerParams, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.createServer(params)
            if (result.isSuccess) {
                val created = result.getOrThrow()
                _toastEvent.emit("Server '${created.name}' berhasil dibuat oleh Admin!")
                _uiState.value = UiState.Success("Server berhasil dibuat")
                loadData()
                onSuccess()
            } else {
                val err = result.exceptionOrNull()?.message ?: "Gagal membuat server"
                _uiState.value = UiState.Error(err)
                _toastEvent.emit(err)
            }
        }
    }


    fun loadData() {
        viewModelScope.launch {
            _isDemoMode.value = repository.isDemo()
            _uiState.value = UiState.Loading
            val serverResult = repository.fetchServers()
            val accountResult = repository.fetchAccountInfo()

            if (serverResult.isSuccess) {
                val serverList = serverResult.getOrDefault(emptyList())
                if (_selectedServer.value == null && serverList.isNotEmpty()) {
                    selectServer(serverList.first())
                }
                _uiState.value = UiState.Success()
            } else {
                _uiState.value = UiState.Error(
                    serverResult.exceptionOrNull()?.message ?: "Gagal terhubung ke Panel Pterodactyl"
                )
            }

            if (accountResult.isSuccess) {
                _accountInfo.value = accountResult.getOrNull()
            }
        }
    }

    fun selectServer(server: PterodactylServer) {
        _selectedServer.value = server
        if (!_consoleLogs.value.containsKey(server.identifier)) {
            val initialLogs = DemoDataGenerator.getInitialConsoleLogs(server.name)
            _consoleLogs.value = _consoleLogs.value + (server.identifier to initialLogs)
        }
        refreshSelectedServerStats()
    }

    fun refreshSelectedServerStats() {
        val server = _selectedServer.value ?: return
        viewModelScope.launch {
            val result = repository.fetchServerStats(server)
            if (result.isSuccess) {
                val stats = result.getOrThrow()
                _serverStats.value = _serverStats.value + (server.identifier to stats)
            }
        }
    }

    private fun startStatsPolling() {
        statsPollingJob?.cancel()
        statsPollingJob = viewModelScope.launch {
            while (true) {
                val currentServers = servers.value
                val selected = _selectedServer.value
                val newMap = _serverStats.value.toMutableMap()

                for (server in currentServers) {
                    val result = repository.fetchServerStats(server)
                    if (result.isSuccess) {
                        newMap[server.identifier] = result.getOrThrow()
                    }
                }
                _serverStats.value = newMap
                delay(4000) // Poll stats every 4 seconds
            }
        }
    }

    fun sendPowerSignal(server: PterodactylServer, signal: PowerSignal) {
        viewModelScope.launch {
            _toastEvent.emit("Mengirim perintah ${signal.name} ke ${server.name}...")
            val result = repository.sendPowerSignal(server.identifier, signal)
            if (result.isSuccess) {
                _toastEvent.emit("Sinyal ${signal.name} berhasil dikirim!")
                // Append system log
                val currentList = _consoleLogs.value[server.identifier] ?: emptyList()
                val logMessage = ConsoleLog(
                    message = "[Pterodactyl Mobile]: Power signal sent -> ${signal.name.uppercase()}",
                    isSystem = true
                )
                _consoleLogs.value = _consoleLogs.value + (server.identifier to (currentList + logMessage))
                repository.fetchServers()
                refreshSelectedServerStats()
            } else {
                _toastEvent.emit("Gagal mengirim sinyal: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun sendConsoleCommand(serverIdentifier: String, command: String) {
        if (command.isBlank()) return
        viewModelScope.launch {
            val currentList = _consoleLogs.value[serverIdentifier] ?: emptyList()
            val userLog = ConsoleLog(
                message = "> $command",
                isSystem = false
            )
            val updatedLogs = currentList + userLog
            _consoleLogs.value = _consoleLogs.value + (serverIdentifier to updatedLogs)

            val result = repository.sendConsoleCommand(serverIdentifier, command)
            if (result.isSuccess) {
                // Simulate console output response
                delay(300)
                val responseLog = ConsoleLog(
                    message = "[Server Response]: Command executed successfully.",
                    isSystem = true
                )
                _consoleLogs.value = _consoleLogs.value + (serverIdentifier to (updatedLogs + responseLog))
            } else {
                val errorLog = ConsoleLog(
                    message = "[Error]: ${result.exceptionOrNull()?.message}",
                    isError = true
                )
                _consoleLogs.value = _consoleLogs.value + (serverIdentifier to (updatedLogs + errorLog))
            }
        }
    }

    fun savePanelAccount(name: String, url: String, apiKey: String, id: Long = 0L, isDefault: Boolean = false) {
        val cleanUrl = url.trim()
        val cleanKey = apiKey.trim()
        if (cleanUrl.isBlank() || cleanKey.isBlank()) return

        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val cleanName = name.trim().ifBlank { "Account" }
                val account = PanelAccount(
                    id = id,
                    name = cleanName,
                    panelUrl = cleanUrl,
                    apiKey = cleanKey,
                    isActive = true,
                    isDefault = isDefault
                )
                val accountId = repository.saveAccount(account)
                if (accountId != -1L) {
                    _toastEvent.emit("Koneksi Panel $cleanName berhasil disimpan!")
                } else {
                    _toastEvent.emit("Gagal menyimpan akun, periksa kembali format URL / API Key.")
                }
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = UiState.Error(e.message ?: "Gagal menyimpan akun")
                _toastEvent.emit("Gagal menyimpan akun: ${e.message}")
            }
        }
    }

    fun switchPanelAccount(accountId: Long) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.switchActiveAccount(accountId)
            _toastEvent.emit("Beralih koneksi panel...")
            loadData()
        }
    }

    fun deletePanelAccount(account: PanelAccount) {
        viewModelScope.launch {
            repository.deleteAccount(account)
            _toastEvent.emit("Panel ${account.name} dihapus dari database")
            loadData()
        }
    }

    fun restoreDefaultAccount() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.restoreDefaultAccount()
            _toastEvent.emit("Akun bawaan sistem berhasil dipulihkan")
            loadData()
        }
    }

    fun isDemoMode(): Boolean = _isDemoMode.value

    fun toggleDemoMode(enabled: Boolean) {
        viewModelScope.launch {
            _isDemoMode.value = enabled
            _uiState.value = UiState.Loading
            repository.setDemoMode(enabled)
            _toastEvent.emit(if (enabled) "Mode Demo (Simulasi Server) diaktifkan" else "Mode Demo dimatikan")
            loadData()
        }
    }
}
