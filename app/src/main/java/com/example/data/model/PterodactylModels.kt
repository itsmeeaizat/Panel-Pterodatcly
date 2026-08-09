package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class ConnectionTestResult(
    val isSuccess: Boolean,
    val message: String,
    val details: String = ""
)

/**
 * Entity representing a configured Pterodactyl Panel connection.
 */
@Entity(tableName = "panel_accounts")
data class PanelAccount(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val panelUrl: String,
    val apiKey: String,
    val isActive: Boolean = false,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Represents a game/vps server returned by Pterodactyl API (Application or Client API).
 */
data class PterodactylServer(
    val identifier: String,
    val uuid: String,
    val name: String,
    val node: String,
    val description: String = "",
    val isServerOwner: Boolean = true,
    val ownerUserId: Int = 1,
    val ownerName: String = "Admin / User",
    val eggName: String = "Minecraft Paper",
    val nodeId: Int = 1,
    val currentStatus: ServerStatus = ServerStatus.OFFLINE,
    val limits: ServerLimits = ServerLimits(),
    val primaryAllocation: String = "127.0.0.1:25565",
    val sftpDetails: SftpDetails = SftpDetails(),
    val isSuspended: Boolean = false
)

/**
 * Admin domain models
 */
data class PterodactylAdminUser(
    val id: Int,
    val username: String,
    val email: String,
    val fullName: String,
    val isRootAdmin: Boolean = false
)

data class PterodactylNode(
    val id: Int,
    val name: String,
    val fqdn: String = "",
    val memoryMb: Long = 16384,
    val diskMb: Long = 102400
)

data class PterodactylEgg(
    val id: Int,
    val nestId: Int = 1,
    val name: String,
    val dockerImage: String = "ghcr.io/pterodactyl/yolks:java_17",
    val startupCommand: String = "java -Xms128M -XX:+UseG1GC -jar {{SERVER_JARFILE}}"
)

data class CreateServerParams(
    val name: String,
    val ownerUserId: Int,
    val nodeId: Int = 1,
    val nestId: Int = 1,
    val eggId: Int = 1,
    val dockerImage: String = "ghcr.io/pterodactyl/yolks:java_17",
    val startupCommand: String = "java -Xms128M -XX:+UseG1GC -jar {{SERVER_JARFILE}}",
    val memoryMb: Long = 2048,
    val cpuPercentage: Float = 100f,
    val diskMb: Long = 10240,
    val databasesLimit: Int = 1,
    val allocationsLimit: Int = 1,
    val backupsLimit: Int = 1
)


enum class ServerStatus(val label: String) {
    RUNNING("Running"),
    STARTING("Starting"),
    STOPPING("Stopping"),
    OFFLINE("Offline"),
    SUSPENDED("Suspended"),
    UNKNOWN("Unknown")
}

data class ServerLimits(
    val memoryMb: Long = 2048,
    val swapMb: Long = 0,
    val diskMb: Long = 10240,
    val ioWeight: Int = 500,
    val cpuPercentage: Float = 100f
)

data class SftpDetails(
    val ip: String = "127.0.0.1",
    val port: Int = 2022
)

/**
 * Real-time utilization data for a server.
 */
data class ServerStats(
    val currentState: String = "offline",
    val memoryBytes: Long = 0,
    val memoryLimitBytes: Long = 2048L * 1024L * 1024L,
    val cpuAbsolutePercentage: Float = 0f,
    val diskBytes: Long = 0,
    val diskLimitBytes: Long = 10240L * 1024L * 1024L,
    val networkRxBytes: Long = 0,
    val networkTxBytes: Long = 0,
    val uptimeMs: Long = 0
) {
    val memoryMb: Long get() = memoryBytes / (1024 * 1024)
    val memoryLimitMb: Long get() = memoryLimitBytes / (1024 * 1024)
    val diskMb: Long get() = diskBytes / (1024 * 1024)
    val diskLimitMb: Long get() = diskLimitBytes / (1024 * 1024)
}

/**
 * User account profile details on the panel.
 */
data class AccountInfo(
    val id: Int = 1,
    val username: String = "Admin",
    val email: String = "admin@pterodactyl.panel",
    val firstName: String = "Panel",
    val lastName: String = "Admin",
    val isAdmin: Boolean = true,
    val twoFactorEnabled: Boolean = false
)

/**
 * Power action commands.
 */
enum class PowerSignal(val signal: String) {
    START("start"),
    STOP("stop"),
    RESTART("restart"),
    KILL("kill")
}

/**
 * Console log item.
 */
data class ConsoleLog(
    val timestamp: Long = System.currentTimeMillis(),
    val message: String,
    val isSystem: Boolean = false,
    val isError: Boolean = false
)

/**
 * Entity for caching offline or saved favorite servers.
 */
@Entity(tableName = "saved_servers")
data class SavedServerEntity(
    @PrimaryKey val identifier: String,
    val panelAccountId: Long,
    val name: String,
    val node: String,
    val description: String,
    val primaryAllocation: String,
    val memoryMb: Long,
    val diskMb: Long,
    val cpuPercentage: Float,
    val isFavorite: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)
