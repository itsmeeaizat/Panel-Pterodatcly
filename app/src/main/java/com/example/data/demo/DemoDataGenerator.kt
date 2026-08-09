package com.example.data.demo

import com.example.data.model.AccountInfo
import com.example.data.model.ConsoleLog
import com.example.data.model.PanelAccount
import com.example.data.model.PterodactylServer
import com.example.data.model.ServerLimits
import com.example.data.model.ServerStats
import com.example.data.model.ServerStatus
import com.example.data.model.SftpDetails
import kotlin.random.Random

object DemoDataGenerator {

    val demoAccount = PanelAccount(
        id = 999L,
        name = "Demo Panel",
        panelUrl = "https://panel.demo-pterodactyl.io",
        apiKey = "demo_ptlc_key_12345",
        isActive = true
    )

    val demoAccountInfo = AccountInfo(
        id = 1,
        username = "PterodactylAdmin",
        email = "admin@pterodactyl-control.io",
        firstName = "Senior",
        lastName = "Architect",
        isAdmin = true,
        twoFactorEnabled = true
    )

    fun getDemoUsers(): List<com.example.data.model.PterodactylAdminUser> {
        return listOf(
            com.example.data.model.PterodactylAdminUser(1, "admin", "admin@panel.io", "System Admin", isRootAdmin = true),
            com.example.data.model.PterodactylAdminUser(2, "alex_dev", "alex@client.org", "Alex Rivers", isRootAdmin = false),
            com.example.data.model.PterodactylAdminUser(3, "guild_master", "sarah@guild.net", "Sarah Connor", isRootAdmin = false)
        )
    }

    fun getDemoNodes(): List<com.example.data.model.PterodactylNode> {
        return listOf(
            com.example.data.model.PterodactylNode(1, "US-East-Node-01", "node1.us-east.panel.io", 32768, 512000),
            com.example.data.model.PterodactylNode(2, "EU-Central-Node-02", "node2.eu-central.panel.io", 65536, 1024000),
            com.example.data.model.PterodactylNode(3, "ASIA-SG-Node-01", "node3.asia-sg.panel.io", 32768, 256000)
        )
    }

    fun getDemoEggs(): List<com.example.data.model.PterodactylEgg> {
        return listOf(
            com.example.data.model.PterodactylEgg(1, 1, "Minecraft Paper 1.20.4", "ghcr.io/pterodactyl/yolks:java_17", "java -Xms128M -XX:+UseG1GC -jar {{SERVER_JARFILE}}"),
            com.example.data.model.PterodactylEgg(2, 2, "Node.js Web / Discord Host", "ghcr.io/pterodactyl/yolks:nodejs_18", "npm start"),
            com.example.data.model.PterodactylEgg(3, 3, "Python 3.10 App Engine", "ghcr.io/pterodactyl/yolks:python_3.10", "python main.py"),
            com.example.data.model.PterodactylEgg(4, 4, "Rust Dedicated Server", "ghcr.io/pterodactyl/games:rust", "./RustDedicated -batchmode"),
            com.example.data.model.PterodactylEgg(5, 5, "Valheim Dedicated Server", "ghcr.io/pterodactyl/games:valheim", "./valheim_server.x86_64")
        )
    }

    fun getDemoServers(): List<PterodactylServer> {
        return listOf(
            PterodactylServer(
                identifier = "a1b2c3d4",
                uuid = "a1b2c3d4-1111-2222-3333-444455556666",
                name = "Minecraft Survival [1.20.4]",
                node = "US-East-Node-01",
                description = "PaperMC High Performance Survival Server with Dynmap & Essentials",
                isServerOwner = true,
                ownerUserId = 1,
                ownerName = "admin",
                eggName = "Minecraft Paper 1.20.4",
                nodeId = 1,
                currentStatus = ServerStatus.RUNNING,
                limits = ServerLimits(memoryMb = 8192, diskMb = 50000, cpuPercentage = 200f),
                primaryAllocation = "192.168.1.50:25565",
                sftpDetails = SftpDetails("192.168.1.50", 2022)
            ),
            PterodactylServer(
                identifier = "e5f6g7h8",
                uuid = "e5f6g7h8-2222-3333-4444-555566667777",
                name = "VALHEIM Dedicated World",
                node = "EU-Central-Node-02",
                description = "Co-op Viking Realm for Guild Members",
                isServerOwner = false,
                ownerUserId = 3,
                ownerName = "guild_master",
                eggName = "Valheim Dedicated Server",
                nodeId = 2,
                currentStatus = ServerStatus.RUNNING,
                limits = ServerLimits(memoryMb = 4096, diskMb = 20000, cpuPercentage = 150f),
                primaryAllocation = "51.15.100.22:2456",
                sftpDetails = SftpDetails("51.15.100.22", 2022)
            ),
            PterodactylServer(
                identifier = "i9j0k1l2",
                uuid = "i9j0k1l2-3333-4444-5555-666677778888",
                name = "Rust Staging Server",
                node = "EU-Central-Node-02",
                description = "High Pop Modded Server 2x Gather Rate",
                isServerOwner = false,
                ownerUserId = 2,
                ownerName = "alex_dev",
                eggName = "Rust Dedicated Server",
                nodeId = 2,
                currentStatus = ServerStatus.OFFLINE,
                limits = ServerLimits(memoryMb = 16384, diskMb = 80000, cpuPercentage = 400f),
                primaryAllocation = "162.243.10.12:28015",
                sftpDetails = SftpDetails("162.243.10.12", 2022)
            ),
            PterodactylServer(
                identifier = "m3n4o5p6",
                uuid = "m3n4o5p6-4444-5555-6666-777788889999",
                name = "Node.js Web Service",
                node = "ASIA-SG-Node-01",
                description = "Express API Backend & Redis Queue Worker",
                isServerOwner = false,
                ownerUserId = 2,
                ownerName = "alex_dev",
                eggName = "Node.js Web / Discord Host",
                nodeId = 3,
                currentStatus = ServerStatus.STARTING,
                limits = ServerLimits(memoryMb = 2048, diskMb = 10000, cpuPercentage = 100f),
                primaryAllocation = "128.199.20.88:3000",
                sftpDetails = SftpDetails("128.199.20.88", 2022)
            )
        )
    }


    fun generateLiveServerStats(server: PterodactylServer): ServerStats {
        return when (server.currentStatus) {
            ServerStatus.RUNNING -> {
                val memPercent = Random.nextDouble(0.35, 0.78)
                val cpuPercent = Random.nextDouble(12.0, 88.0).toFloat()
                val diskPercent = 0.29
                val totalMem = server.limits.memoryMb * 1024L * 1024L
                val totalDisk = server.limits.diskMb * 1024L * 1024L

                ServerStats(
                    currentState = "running",
                    memoryBytes = (totalMem * memPercent).toLong(),
                    memoryLimitBytes = totalMem,
                    cpuAbsolutePercentage = cpuPercent,
                    diskBytes = (totalDisk * diskPercent).toLong(),
                    diskLimitBytes = totalDisk,
                    networkRxBytes = Random.nextLong(100_000, 5_000_000),
                    networkTxBytes = Random.nextLong(500_000, 12_000_000),
                    uptimeMs = System.currentTimeMillis() - 172800000L
                )
            }
            ServerStatus.STARTING -> {
                val totalMem = server.limits.memoryMb * 1024L * 1024L
                val totalDisk = server.limits.diskMb * 1024L * 1024L

                ServerStats(
                    currentState = "starting",
                    memoryBytes = (totalMem * 0.25).toLong(),
                    memoryLimitBytes = totalMem,
                    cpuAbsolutePercentage = Random.nextDouble(80.0, 120.0).toFloat(),
                    diskBytes = (totalDisk * 0.22).toLong(),
                    diskLimitBytes = totalDisk,
                    networkRxBytes = 15000,
                    networkTxBytes = 8000,
                    uptimeMs = 12000
                )
            }
            else -> {
                val totalMem = server.limits.memoryMb * 1024L * 1024L
                val totalDisk = server.limits.diskMb * 1024L * 1024L
                ServerStats(
                    currentState = "offline",
                    memoryBytes = 0,
                    memoryLimitBytes = totalMem,
                    cpuAbsolutePercentage = 0f,
                    diskBytes = (totalDisk * 0.20).toLong(),
                    diskLimitBytes = totalDisk,
                    networkRxBytes = 0,
                    networkTxBytes = 0,
                    uptimeMs = 0
                )
            }
        }
    }

    fun getInitialConsoleLogs(serverName: String): List<ConsoleLog> {
        val now = System.currentTimeMillis()
        return listOf(
            ConsoleLog(now - 120000, "Container initialized on Pterodactyl Wings node", isSystem = true),
            ConsoleLog(now - 110000, "[Pterodactyl Daemon]: Fetching server configuration files...", isSystem = true),
            ConsoleLog(now - 90000, "[Server]: Starting process: java -Xms2G -Xmx8G -jar server.jar nogui"),
            ConsoleLog(now - 80000, "[Server Thread/INFO]: Loading properties from server.properties"),
            ConsoleLog(now - 70000, "[Server Thread/INFO]: Default game type: SURVIVAL"),
            ConsoleLog(now - 60000, "[Server Thread/INFO]: Preparing level 'world'"),
            ConsoleLog(now - 45000, "[Server Thread/INFO]: Preparing start region for dimension minecraft:overworld"),
            ConsoleLog(now - 30000, "[Server Thread/INFO]: Time elapsed: 14210 ms"),
            ConsoleLog(now - 15000, "[Server Thread/INFO]: Done (15.220s)! For help, type \"help\""),
            ConsoleLog(now - 5000, "[Pterodactyl Daemon]: Server marked as RUNNING", isSystem = true)
        )
    }
}
