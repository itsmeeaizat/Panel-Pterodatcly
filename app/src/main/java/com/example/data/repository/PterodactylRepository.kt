package com.example.data.repository

import android.content.Context
import com.example.config.AppConfig
import com.example.data.config.ConfigSourceMode
import com.example.data.config.EffectiveConfig
import com.example.data.config.HybridConfigManager
import com.example.data.config.RawConfigFileData
import com.example.data.api.ApiClientFactory
import com.example.data.api.ConsoleCommandRequest
import com.example.data.api.PowerSignalRequest
import com.example.data.api.PterodactylApiService
import com.example.data.demo.DemoDataGenerator
import com.example.data.local.PterodactylDatabase
import com.example.data.model.AccountInfo
import com.example.data.model.ConnectionTestResult
import com.example.data.model.ConsoleLog
import com.example.data.model.PanelAccount
import com.example.data.model.PowerSignal
import com.example.data.model.PterodactylServer
import com.example.data.model.SavedServerEntity
import com.example.data.model.ServerLimits
import com.example.data.model.ServerStats
import com.example.data.model.ServerStatus
import com.example.data.model.SftpDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class PterodactylRepository(context: Context) {

    private val db = PterodactylDatabase.getInstance(context)
    private val accountDao = db.panelAccountDao()
    private val savedServerDao = db.savedServerDao()

    val allPanelAccounts: Flow<List<PanelAccount>> = accountDao.getAllAccounts()
    val activePanelAccount: Flow<PanelAccount?> = accountDao.getActiveAccountFlow()

    // In-memory server list & stats cache for responsive UI
    private val _serversFlow = MutableStateFlow<List<PterodactylServer>>(emptyList())
    val serversFlow: Flow<List<PterodactylServer>> = _serversFlow.asStateFlow()

    private var activeApiService: PterodactylApiService? = null
    private var isDemoMode: Boolean = false

    private val hybridConfigManager = HybridConfigManager(context)

    suspend fun initialize() {
        withContext(Dispatchers.IO) {
            syncRawConfigFileToAccounts()

            val effectiveConfig = hybridConfigManager.getEffectiveConfig()
            var activeAccount = accountDao.getActiveAccount()

            // Jika belum ada akun aktif dan konfigurasi efektif valid
            if (activeAccount == null && effectiveConfig.panelUrl.isNotBlank() && effectiveConfig.apiKey.isNotBlank()) {
                val newAccount = PanelAccount(
                    name = effectiveConfig.accountName,
                    panelUrl = effectiveConfig.panelUrl,
                    apiKey = effectiveConfig.apiKey,
                    isActive = true
                )
                val id = accountDao.insertAccount(newAccount)
                accountDao.setSingleActiveAccount(id)
                activeAccount = accountDao.getActiveAccount()
            }

            if (activeAccount != null && activeAccount.apiKey != DemoDataGenerator.demoAccount.apiKey) {
                isDemoMode = false
                activeApiService = ApiClientFactory.createService(activeAccount.panelUrl, activeAccount.apiKey)
                fetchServers()
            } else if (effectiveConfig.panelUrl.isNotBlank() && effectiveConfig.apiKey.isNotBlank()) {
                isDemoMode = false
                activeApiService = ApiClientFactory.createService(effectiveConfig.panelUrl, effectiveConfig.apiKey)
                fetchServers()
            } else {
                isDemoMode = false
                activeApiService = null
                _serversFlow.value = emptyList()
            }
        }
    }

    private suspend fun syncRawConfigFileToAccounts() {
        try {
            val rawData = hybridConfigManager.loadRawConfigFile()
            if (rawData.panelUrl.isNotBlank() && rawData.apiKey.isNotBlank()) {
                val existingAccounts = accountDao.getAllAccounts().firstOrNull() ?: emptyList()
                val matchedAccount = existingAccounts.find {
                    it.panelUrl.trimEnd('/') == rawData.panelUrl.trimEnd('/') && it.apiKey.trim() == rawData.apiKey.trim()
                }

                val cleanName = if (rawData.accountName.isNotBlank() && 
                    !rawData.accountName.contains("assets/", ignoreCase = true) && 
                    !rawData.accountName.contains("Raw File", ignoreCase = true) &&
                    !rawData.accountName.equals("Panel Bawaan", ignoreCase = true)
                ) {
                    rawData.accountName
                } else {
                    "Account"
                }

                if (matchedAccount == null) {
                    val rawAccount = PanelAccount(
                        name = cleanName,
                        panelUrl = rawData.panelUrl.trimEnd('/'),
                        apiKey = rawData.apiKey.trim(),
                        isActive = existingAccounts.isEmpty(),
                        isDefault = true
                    )
                    val insertedId = accountDao.insertAccount(rawAccount)
                    if (existingAccounts.isEmpty()) {
                        accountDao.setSingleActiveAccount(insertedId)
                    }
                } else if (!matchedAccount.isDefault) {
                    val updatedAccount = matchedAccount.copy(isDefault = true)
                    accountDao.insertAccount(updatedAccount)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getHybridConfigManager(): HybridConfigManager = hybridConfigManager

    suspend fun applyHybridConfig(
        sourceMode: ConfigSourceMode,
        customUrl: String? = null,
        customApiKey: String? = null,
        accountName: String = "Custom Panel"
    ) = withContext(Dispatchers.IO) {
        hybridConfigManager.setConfigSourceMode(sourceMode)
        if (customUrl != null || customApiKey != null) {
            val currentUrl = customUrl ?: hybridConfigManager.getCustomPanelUrl()
            val currentKey = customApiKey ?: hybridConfigManager.getCustomApiKey()
            hybridConfigManager.saveCustomConfig(currentUrl, currentKey, accountName)
        }

        val effective = hybridConfigManager.getEffectiveConfig()
        if (effective.panelUrl.isNotBlank() && effective.apiKey.isNotBlank()) {
            val newAccount = PanelAccount(
                name = effective.accountName,
                panelUrl = effective.panelUrl,
                apiKey = effective.apiKey,
                isActive = true
            )
            val id = accountDao.insertAccount(newAccount)
            accountDao.setSingleActiveAccount(id)

            isDemoMode = false
            activeApiService = ApiClientFactory.createService(effective.panelUrl, effective.apiKey)
            fetchServers()
        }
    }

    suspend fun saveAccount(account: PanelAccount): Long = withContext(Dispatchers.IO) {
        try {
            val accountId = accountDao.insertAccount(account)
            accountDao.setSingleActiveAccount(accountId)
            if (account.apiKey != DemoDataGenerator.demoAccount.apiKey) {
                isDemoMode = false
                activeApiService = ApiClientFactory.createService(account.panelUrl, account.apiKey)
            } else {
                isDemoMode = true
                _serversFlow.value = DemoDataGenerator.getDemoServers()
            }
            fetchServers()
            return@withContext accountId
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext -1L
        }
    }

    suspend fun switchActiveAccount(accountId: Long) = withContext(Dispatchers.IO) {
        try {
            accountDao.setSingleActiveAccount(accountId)
            val active = accountDao.getActiveAccount()
            if (active != null && active.apiKey != DemoDataGenerator.demoAccount.apiKey) {
                isDemoMode = false
                activeApiService = ApiClientFactory.createService(active.panelUrl, active.apiKey)
            } else {
                isDemoMode = true
                _serversFlow.value = DemoDataGenerator.getDemoServers()
            }
            fetchServers()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteAccount(account: PanelAccount) = withContext(Dispatchers.IO) {
        try {
            accountDao.deleteAccount(account)
            val remaining = accountDao.getAllAccounts().firstOrNull()
            if (!remaining.isNullOrEmpty()) {
                switchActiveAccount(remaining.first().id)
            } else {
                activeApiService = null
                _serversFlow.value = emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun restoreDefaultAccount() = withContext(Dispatchers.IO) {
        try {
            syncRawConfigFileToAccounts()
            val accounts = accountDao.getAllAccounts().firstOrNull()
            val defaultAccount = accounts?.firstOrNull { it.isDefault }
            if (defaultAccount != null) {
                switchActiveAccount(defaultAccount.id)
            } else if (!accounts.isNullOrEmpty()) {
                switchActiveAccount(accounts.first().id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun fetchServers(): Result<List<PterodactylServer>> = withContext(Dispatchers.IO) {
        if (isDemoMode) {
            val demoList = _serversFlow.value.ifEmpty { DemoDataGenerator.getDemoServers() }
            _serversFlow.value = demoList
            return@withContext Result.success(demoList)
        }

        if (activeApiService == null) {
            _serversFlow.value = emptyList()
            return@withContext Result.success(emptyList())
        }

        try {
            // First try Application API (/api/application/servers) for Admin Panel Mode
            val adminResponse = activeApiService!!.getAdminServers()
            if (adminResponse.isSuccessful && adminResponse.body() != null) {
                val list = adminResponse.body()!!.data.map { wrapper ->
                    val attr = wrapper.attributes
                    PterodactylServer(
                        identifier = attr.identifier.ifEmpty { "srv_${attr.id}" },
                        uuid = attr.uuid,
                        name = attr.name,
                        node = "Node #${attr.node}",
                        description = attr.description ?: "",
                        isServerOwner = true,
                        ownerUserId = attr.user,
                        ownerName = "User #${attr.user}",
                        eggName = "Egg #${attr.egg ?: 1}",
                        nodeId = attr.node,
                        currentStatus = if (attr.isSuspended) ServerStatus.SUSPENDED else ServerStatus.OFFLINE,
                        limits = ServerLimits(
                            memoryMb = attr.limits?.memory ?: 2048,
                            diskMb = attr.limits?.disk ?: 10240,
                            cpuPercentage = attr.limits?.cpu ?: 100f
                        ),
                        primaryAllocation = "Alloc #${attr.allocation ?: 1}",
                        isSuspended = attr.isSuspended
                    )
                }

                _serversFlow.value = list
                return@withContext Result.success(list)
            }

            // Fallback to Client API (/api/client) if Admin Application API is unavailable
            val response = activeApiService!!.getAccountServers()
            if (response.isSuccessful && response.body() != null) {
                val list = response.body()!!.data.map { wrapper ->
                    val attr = wrapper.attributes
                    val alloc = attr.relationships?.allocations?.data?.firstOrNull()?.attributes
                    val allocString = if (alloc != null) "${alloc.ip}:${alloc.port}" else "Default Allocation"

                    PterodactylServer(
                        identifier = attr.identifier,
                        uuid = attr.uuid,
                        name = attr.name,
                        node = attr.node,
                        description = attr.description ?: "",
                        isServerOwner = attr.isServerOwner ?: true,
                        currentStatus = if (attr.isSuspended == true) ServerStatus.SUSPENDED else ServerStatus.UNKNOWN,
                        limits = ServerLimits(
                            memoryMb = attr.limits?.memory ?: 2048,
                            diskMb = attr.limits?.disk ?: 10240,
                            cpuPercentage = attr.limits?.cpu ?: 100f
                        ),
                        primaryAllocation = allocString,
                        sftpDetails = SftpDetails(alloc?.ip ?: "127.0.0.1", 2022)
                    )
                }

                _serversFlow.value = list

                // Cache in Room DB
                val activeAcc = accountDao.getActiveAccount()
                if (activeAcc != null) {
                    val entities = list.map { s ->
                        SavedServerEntity(
                            identifier = s.identifier,
                            panelAccountId = activeAcc.id,
                            name = s.name,
                            node = s.node,
                            description = s.description,
                            primaryAllocation = s.primaryAllocation,
                            memoryMb = s.limits.memoryMb,
                            diskMb = s.limits.diskMb,
                            cpuPercentage = s.limits.cpuPercentage
                        )
                    }
                    savedServerDao.insertServers(entities)
                }

                Result.success(list)
            } else {
                Result.failure(Exception("HTTP Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun testConnection(panelUrl: String, apiKey: String): ConnectionTestResult = withContext(Dispatchers.IO) {
        if (panelUrl.isBlank()) {
            return@withContext ConnectionTestResult(false, "URL Panel tidak boleh kosong.")
        }
        if (apiKey.isBlank()) {
            return@withContext ConnectionTestResult(false, "API Key tidak boleh kosong.")
        }

        if (apiKey == DemoDataGenerator.demoAccount.apiKey) {
            return@withContext ConnectionTestResult(
                isSuccess = true,
                message = "Koneksi Berhasil! (Mode Demo / Simulasi Panel)",
                details = "Menggunakan data demo simulasi lokal."
            )
        }

        try {
            val testService = ApiClientFactory.createService(panelUrl, apiKey)
            if (testService == null) {
                return@withContext ConnectionTestResult(
                    isSuccess = false,
                    message = "Gagal: Format URL Panel atau API Key tidak valid."
                )
            }

            if (apiKey.startsWith("ptla_")) {
                val adminResp = testService.getAdminNodes()
                if (adminResp.isSuccessful && adminResp.body() != null) {
                    val count = adminResp.body()!!.data.size
                    return@withContext ConnectionTestResult(
                        isSuccess = true,
                        message = "Koneksi Admin Panel Berhasil Terhubung! (200 OK)",
                        details = "Token Admin Application (ptla_) valid. Terhubung dengan $count Nodes."
                    )
                } else {
                    val code = adminResp.code()
                    val msg = when (code) {
                        401 -> "Gagal (HTTP 401 Unauthorized): Admin API Token (ptla_) tidak valid atau salah."
                        403 -> "Gagal (HTTP 403 Forbidden): Token dilarang mengakes /api/application."
                        404 -> "Gagal (HTTP 404 Not Found): Domain URL Panel tidak valid / endpoint tidak ditemukan."
                        else -> "Gagal (HTTP $code): ${adminResp.message().ifEmpty { "Koneksi ditolak" }}"
                    }
                    return@withContext ConnectionTestResult(false, msg)
                }
            } else {
                val clientResp = testService.getAccountServers()
                if (clientResp.isSuccessful && clientResp.body() != null) {
                    val count = clientResp.body()!!.data.size
                    return@withContext ConnectionTestResult(
                        isSuccess = true,
                        message = "Koneksi Client Panel Berhasil Terhubung! (200 OK)",
                        details = "Token Client (ptlc_) valid. Terhubung dengan $count Server."
                    )
                } else {
                    val code = clientResp.code()
                    val msg = when (code) {
                        401 -> "Gagal (HTTP 401 Unauthorized): API Key Client (ptlc_) salah."
                        403 -> "Gagal (HTTP 403 Forbidden): API Key dilarang mengakses panel ini."
                        404 -> "Gagal (HTTP 404 Not Found): Domain URL Panel tidak valid."
                        else -> "Gagal (HTTP $code): ${clientResp.message().ifEmpty { "Koneksi ditolak" }}"
                    }
                    return@withContext ConnectionTestResult(false, msg)
                }
            }
        } catch (e: java.net.UnknownHostException) {
            return@withContext ConnectionTestResult(
                false,
                "Gagal Koneksi: Domain/URL Panel tidak dapat ditemukan (Unknown Host). Periksa kembali alamat domain Anda."
            )
        } catch (e: java.net.SocketTimeoutException) {
            return@withContext ConnectionTestResult(
                false,
                "Gagal Koneksi: Timeout. Server Panel tidak merespons dalam 15 detik."
            )
        } catch (e: javax.net.ssl.SSLException) {
            return@withContext ConnectionTestResult(
                false,
                "Gagal Koneksi: Masalah Sertifikat SSL / HTTPS pada domain Panel Pterodactyl."
            )
        } catch (e: Exception) {
            return@withContext ConnectionTestResult(
                false,
                "Gagal Koneksi: ${e.localizedMessage ?: e.message ?: "Terjadi kesalahan jaringan"}"
            )
        }
    }

    suspend fun getAdminUsers(): Result<List<com.example.data.model.PterodactylAdminUser>> = withContext(Dispatchers.IO) {
        if (isDemoMode || activeApiService == null) {
            return@withContext Result.success(DemoDataGenerator.getDemoUsers())
        }
        try {
            val response = activeApiService!!.getAdminUsers()
            if (response.isSuccessful && response.body() != null) {
                val users = response.body()!!.data.map { u ->
                    val a = u.attributes
                    com.example.data.model.PterodactylAdminUser(
                        id = a.id,
                        username = a.username,
                        email = a.email,
                        fullName = "${a.firstName ?: ""} ${a.lastName ?: ""}".trim().ifEmpty { a.username },
                        isRootAdmin = a.rootAdmin
                    )
                }
                Result.success(users)
            } else {
                Result.success(DemoDataGenerator.getDemoUsers())
            }
        } catch (e: Exception) {
            Result.success(DemoDataGenerator.getDemoUsers())
        }
    }

    suspend fun getAdminNodes(): Result<List<com.example.data.model.PterodactylNode>> = withContext(Dispatchers.IO) {
        if (isDemoMode || activeApiService == null) {
            return@withContext Result.success(DemoDataGenerator.getDemoNodes())
        }
        try {
            val response = activeApiService!!.getAdminNodes()
            if (response.isSuccessful && response.body() != null) {
                val nodes = response.body()!!.data.map { n ->
                    val a = n.attributes
                    com.example.data.model.PterodactylNode(
                        id = a.id,
                        name = a.name,
                        fqdn = a.fqdn ?: "",
                        memoryMb = a.memory,
                        diskMb = a.disk
                    )
                }
                Result.success(nodes)
            } else {
                Result.success(DemoDataGenerator.getDemoNodes())
            }
        } catch (e: Exception) {
            Result.success(DemoDataGenerator.getDemoNodes())
        }
    }

    suspend fun getAdminEggs(): Result<List<com.example.data.model.PterodactylEgg>> = withContext(Dispatchers.IO) {
        if (isDemoMode || activeApiService == null) {
            return@withContext Result.success(DemoDataGenerator.getDemoEggs())
        }
        try {
            val response = activeApiService!!.getAdminEggs(nestId = 1)
            if (response.isSuccessful && response.body() != null) {
                val eggs = response.body()!!.data.map { egg ->
                    val a = egg.attributes
                    com.example.data.model.PterodactylEgg(
                        id = a.id,
                        nestId = a.nest,
                        name = a.name,
                        dockerImage = a.dockerImage,
                        startupCommand = a.startup
                    )
                }
                Result.success(eggs)
            } else {
                Result.success(DemoDataGenerator.getDemoEggs())
            }
        } catch (e: Exception) {
            Result.success(DemoDataGenerator.getDemoEggs())
        }
    }

    suspend fun createServer(params: com.example.data.model.CreateServerParams): Result<PterodactylServer> = withContext(Dispatchers.IO) {
        if (isDemoMode || activeApiService == null) {
            val newId = kotlin.random.Random.nextInt(100, 999)
            val newIdent = "srv_${newId}"
            val newServer = PterodactylServer(
                identifier = newIdent,
                uuid = java.util.UUID.randomUUID().toString(),
                name = params.name,
                node = "Node #${params.nodeId}",
                description = "Created by Admin via Pterodactyl Mobile Manager",
                isServerOwner = true,
                ownerUserId = params.ownerUserId,
                ownerName = "User #${params.ownerUserId}",
                eggName = "Egg #${params.eggId}",
                nodeId = params.nodeId,
                currentStatus = ServerStatus.RUNNING,
                limits = ServerLimits(
                    memoryMb = params.memoryMb,
                    diskMb = params.diskMb,
                    cpuPercentage = params.cpuPercentage
                ),
                primaryAllocation = "192.168.1.${kotlin.random.Random.nextInt(10, 200)}:25565"
            )
            _serversFlow.value = _serversFlow.value + newServer
            return@withContext Result.success(newServer)
        }

        try {
            val req = com.example.data.api.CreateServerRequestDto(
                name = params.name,
                user = params.ownerUserId,
                egg = params.eggId,
                dockerImage = params.dockerImage,
                startup = params.startupCommand,
                limits = com.example.data.api.ServerLimitsDto(
                    memory = params.memoryMb,
                    swap = 0,
                    disk = params.diskMb,
                    io = 500,
                    cpu = params.cpuPercentage
                ),
                featureLimits = com.example.data.api.FeatureLimitsDto(
                    databases = params.databasesLimit,
                    allocations = params.allocationsLimit,
                    backups = params.backupsLimit
                )
            )

            val response = activeApiService!!.createServer(req)
            if (response.isSuccessful && response.body() != null) {
                val attr = response.body()!!.attributes
                val createdServer = PterodactylServer(
                    identifier = attr.identifier.ifEmpty { "srv_${attr.id}" },
                    uuid = attr.uuid,
                    name = attr.name,
                    node = "Node #${attr.node}",
                    description = attr.description ?: "",
                    isServerOwner = true,
                    ownerUserId = attr.user,
                    ownerName = "User #${attr.user}",
                    eggName = "Egg #${attr.egg ?: 1}",
                    nodeId = attr.node,
                    currentStatus = ServerStatus.OFFLINE,
                    limits = ServerLimits(
                        memoryMb = attr.limits?.memory ?: params.memoryMb,
                        diskMb = attr.limits?.disk ?: params.diskMb,
                        cpuPercentage = attr.limits?.cpu ?: params.cpuPercentage
                    )
                )

                fetchServers() // Refresh global server list
                Result.success(createdServer)
            } else {
                Result.failure(Exception("Gagal membuat server: Code ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun fetchServerStats(server: PterodactylServer): Result<ServerStats> = withContext(Dispatchers.IO) {
        if (isDemoMode || activeApiService == null) {
            return@withContext Result.success(DemoDataGenerator.generateLiveServerStats(server))
        }

        try {
            val response = activeApiService!!.getServerUtilization(server.identifier)
            if (response.isSuccessful && response.body() != null) {
                val statsAttr = response.body()!!.attributes
                val res = statsAttr.resources

                val serverStats = ServerStats(
                    currentState = statsAttr.currentState,
                    memoryBytes = res.memoryBytes,
                    memoryLimitBytes = server.limits.memoryMb * 1024L * 1024L,
                    cpuAbsolutePercentage = res.cpuAbsolute,
                    diskBytes = res.diskBytes,
                    diskLimitBytes = server.limits.diskMb * 1024L * 1024L,
                    networkRxBytes = res.networkRxBytes,
                    networkTxBytes = res.networkTxBytes,
                    uptimeMs = res.uptimeMs
                )
                Result.success(serverStats)
            } else {
                Result.failure(Exception("Failed to fetch stats: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPowerSignal(serverIdentifier: String, signal: PowerSignal): Result<Unit> = withContext(Dispatchers.IO) {
        if (isDemoMode || activeApiService == null) {
            // Simulate status change in demo list
            val updated = _serversFlow.value.map { s ->
                if (s.identifier == serverIdentifier) {
                    val newStatus = when (signal) {
                        PowerSignal.START -> ServerStatus.RUNNING
                        PowerSignal.STOP -> ServerStatus.OFFLINE
                        PowerSignal.RESTART -> ServerStatus.STARTING
                        PowerSignal.KILL -> ServerStatus.OFFLINE
                    }
                    s.copy(currentStatus = newStatus)
                } else s
            }
            _serversFlow.value = updated
            return@withContext Result.success(Unit)
        }

        try {
            val response = activeApiService!!.sendPowerSignal(serverIdentifier, PowerSignalRequest(signal.signal))
            if (response.isSuccessful) {
                fetchServers() // Refresh servers list after power signal
                Result.success(Unit)
            } else {
                Result.failure(Exception("Power action failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendConsoleCommand(serverIdentifier: String, command: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (isDemoMode || activeApiService == null) {
            return@withContext Result.success(Unit)
        }

        try {
            val response = activeApiService!!.sendConsoleCommand(serverIdentifier, ConsoleCommandRequest(command))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Command failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAccountInfo(): Result<AccountInfo> = withContext(Dispatchers.IO) {
        if (isDemoMode || activeApiService == null) {
            return@withContext Result.success(DemoDataGenerator.demoAccountInfo)
        }

        try {
            val response = activeApiService!!.getAccountDetails()
            if (response.isSuccessful && response.body() != null) {
                val attr = response.body()!!.attributes
                Result.success(
                    AccountInfo(
                        id = attr.id,
                        username = attr.username,
                        email = attr.email,
                        firstName = attr.firstName,
                        lastName = attr.lastName,
                        isAdmin = attr.isAdmin
                    )
                )
            } else {
                Result.failure(Exception("Account fetch failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isDemo(): Boolean = isDemoMode

    suspend fun setDemoMode(enabled: Boolean) = withContext(Dispatchers.IO) {
        isDemoMode = enabled
        if (enabled) {
            _serversFlow.value = DemoDataGenerator.getDemoServers()
        } else {
            val active = accountDao.getActiveAccount()
            if (active != null && active.apiKey != DemoDataGenerator.demoAccount.apiKey) {
                activeApiService = ApiClientFactory.createService(active.panelUrl, active.apiKey)
            } else {
                activeApiService = null
            }
        }
        fetchServers()
    }
}
