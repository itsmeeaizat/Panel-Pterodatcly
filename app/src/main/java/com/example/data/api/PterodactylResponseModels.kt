package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PterodactylListResponse<T>(
    @Json(name = "object") val objectType: String = "list",
    @Json(name = "data") val data: List<PterodactylDataWrapper<T>> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PterodactylObjectResponse<T>(
    @Json(name = "object") val objectType: String = "object",
    @Json(name = "attributes") val attributes: T
)

@JsonClass(generateAdapter = true)
data class PterodactylDataWrapper<T>(
    @Json(name = "object") val objectType: String = "",
    @Json(name = "attributes") val attributes: T
)

@JsonClass(generateAdapter = true)
data class ClientServerAttributes(
    @Json(name = "identifier") val identifier: String,
    @Json(name = "uuid") val uuid: String,
    @Json(name = "name") val name: String,
    @Json(name = "node") val node: String,
    @Json(name = "description") val description: String? = "",
    @Json(name = "is_server_owner") val isServerOwner: Boolean? = true,
    @Json(name = "is_suspended") val isSuspended: Boolean? = false,
    @Json(name = "limits") val limits: ServerLimitsDto? = null,
    @Json(name = "relationships") val relationships: ServerRelationshipsDto? = null
)

@JsonClass(generateAdapter = true)
data class ServerLimitsDto(
    @Json(name = "memory") val memory: Long = 2048,
    @Json(name = "swap") val swap: Long = 0,
    @Json(name = "disk") val disk: Long = 10240,
    @Json(name = "io") val io: Int = 500,
    @Json(name = "cpu") val cpu: Float = 100f
)

@JsonClass(generateAdapter = true)
data class ServerRelationshipsDto(
    @Json(name = "allocations") val allocations: PterodactylListResponse<AllocationAttributes>? = null
)

@JsonClass(generateAdapter = true)
data class AllocationAttributes(
    @Json(name = "id") val id: Int,
    @Json(name = "ip") val ip: String,
    @Json(name = "port") val port: Int,
    @Json(name = "is_default") val isDefault: Boolean = false
)

@JsonClass(generateAdapter = true)
data class ResourceUtilizationResponse(
    @Json(name = "object") val objectType: String = "stats",
    @Json(name = "attributes") val attributes: ResourceUtilizationAttributes
)

@JsonClass(generateAdapter = true)
data class ResourceUtilizationAttributes(
    @Json(name = "current_state") val currentState: String = "offline",
    @Json(name = "is_suspended") val isSuspended: Boolean = false,
    @Json(name = "resources") val resources: ResourceStatsDto
)

@JsonClass(generateAdapter = true)
data class ResourceStatsDto(
    @Json(name = "memory_bytes") val memoryBytes: Long = 0,
    @Json(name = "cpu_absolute") val cpuAbsolute: Float = 0f,
    @Json(name = "disk_bytes") val diskBytes: Long = 0,
    @Json(name = "network_rx_bytes") val networkRxBytes: Long = 0,
    @Json(name = "network_tx_bytes") val networkTxBytes: Long = 0,
    @Json(name = "uptime") val uptimeMs: Long = 0
)

@JsonClass(generateAdapter = true)
data class ClientAccountAttributes(
    @Json(name = "id") val id: Int = 1,
    @Json(name = "username") val username: String = "",
    @Json(name = "email") val email: String = "",
    @Json(name = "first_name") val firstName: String = "",
    @Json(name = "last_name") val lastName: String = "",
    @Json(name = "admin") val isAdmin: Boolean = false
)

@JsonClass(generateAdapter = true)
data class PowerSignalRequest(
    @Json(name = "signal") val signal: String
)

@JsonClass(generateAdapter = true)
data class ConsoleCommandRequest(
    @Json(name = "command") val command: String
)

// ==========================================
// Application API Models (Pterodactyl Admin)
// ==========================================

@JsonClass(generateAdapter = true)
data class ApplicationServerAttributes(
    @Json(name = "id") val id: Int = 1,
    @Json(name = "external_id") val externalId: String? = null,
    @Json(name = "uuid") val uuid: String = "",
    @Json(name = "identifier") val identifier: String = "",
    @Json(name = "name") val name: String = "",
    @Json(name = "description") val description: String? = "",
    @Json(name = "status") val status: String? = null,
    @Json(name = "suspended") val isSuspended: Boolean = false,
    @Json(name = "user") val user: Int = 1,
    @Json(name = "node") val node: Int = 1,
    @Json(name = "allocation") val allocation: Int? = 1,
    @Json(name = "nest") val nest: Int? = 1,
    @Json(name = "egg") val egg: Int? = 1,
    @Json(name = "limits") val limits: ServerLimitsDto? = null,
    @Json(name = "feature_limits") val featureLimits: FeatureLimitsDto? = null
)

@JsonClass(generateAdapter = true)
data class FeatureLimitsDto(
    @Json(name = "databases") val databases: Int = 1,
    @Json(name = "allocations") val allocations: Int = 1,
    @Json(name = "backups") val backups: Int = 1
)

@JsonClass(generateAdapter = true)
data class ApplicationUserAttributes(
    @Json(name = "id") val id: Int,
    @Json(name = "username") val username: String,
    @Json(name = "email") val email: String,
    @Json(name = "first_name") val firstName: String? = "",
    @Json(name = "last_name") val lastName: String? = "",
    @Json(name = "root_admin") val rootAdmin: Boolean = false
)

@JsonClass(generateAdapter = true)
data class ApplicationNodeAttributes(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "location_id") val locationId: Int? = 1,
    @Json(name = "fqdn") val fqdn: String? = "",
    @Json(name = "memory") val memory: Long = 16384,
    @Json(name = "disk") val disk: Long = 102400
)

@JsonClass(generateAdapter = true)
data class ApplicationNestAttributes(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String? = ""
)

@JsonClass(generateAdapter = true)
data class ApplicationEggAttributes(
    @Json(name = "id") val id: Int,
    @Json(name = "nest") val nest: Int = 1,
    @Json(name = "name") val name: String,
    @Json(name = "docker_image") val dockerImage: String = "ghcr.io/pterodactyl/yolks:java_17",
    @Json(name = "startup") val startup: String = "java -Xms128M -XX:+UseG1GC -jar {{SERVER_JARFILE}}"
)

@JsonClass(generateAdapter = true)
data class CreateServerRequestDto(
    @Json(name = "name") val name: String,
    @Json(name = "user") val user: Int,
    @Json(name = "egg") val egg: Int,
    @Json(name = "docker_image") val dockerImage: String,
    @Json(name = "startup") val startup: String,
    @Json(name = "environment") val environment: Map<String, String> = emptyMap(),
    @Json(name = "limits") val limits: ServerLimitsDto,
    @Json(name = "feature_limits") val featureLimits: FeatureLimitsDto = FeatureLimitsDto(),
    @Json(name = "allocation") val allocation: AllocationRequestDto = AllocationRequestDto()
)

@JsonClass(generateAdapter = true)
data class AllocationRequestDto(
    @Json(name = "default") val default: Int? = null,
    @Json(name = "additional") val additional: List<Int> = emptyList()
)

