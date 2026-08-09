package com.example.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ClientApiService {

    @GET("api/client")
    suspend fun getAccountServers(): Response<PterodactylListResponse<ClientServerAttributes>>

    @GET("api/client/account")
    suspend fun getAccountDetails(): Response<PterodactylObjectResponse<ClientAccountAttributes>>

    @GET("api/client/servers/{serverIdentifier}")
    suspend fun getServerDetails(
        @Path("serverIdentifier") serverIdentifier: String
    ): Response<PterodactylObjectResponse<ClientServerAttributes>>

    @GET("api/client/servers/{serverIdentifier}/resources")
    suspend fun getServerUtilization(
        @Path("serverIdentifier") serverIdentifier: String
    ): Response<ResourceUtilizationResponse>

    @POST("api/client/servers/{serverIdentifier}/power")
    suspend fun sendPowerSignal(
        @Path("serverIdentifier") serverIdentifier: String,
        @Body request: PowerSignalRequest
    ): Response<Unit>

    @POST("api/client/servers/{serverIdentifier}/command")
    suspend fun sendConsoleCommand(
        @Path("serverIdentifier") serverIdentifier: String,
        @Body request: ConsoleCommandRequest
    ): Response<Unit>
}

interface AdminApiService {

    @GET("api/application/servers")
    suspend fun getAdminServers(): Response<PterodactylListResponse<ApplicationServerAttributes>>

    @POST("api/application/servers")
    suspend fun createServer(
        @Body request: CreateServerRequestDto
    ): Response<PterodactylObjectResponse<ApplicationServerAttributes>>

    @GET("api/application/users")
    suspend fun getAdminUsers(): Response<PterodactylListResponse<ApplicationUserAttributes>>

    @GET("api/application/nodes")
    suspend fun getAdminNodes(): Response<PterodactylListResponse<ApplicationNodeAttributes>>

    @GET("api/application/nests")
    suspend fun getAdminNests(): Response<PterodactylListResponse<ApplicationNestAttributes>>

    @GET("api/application/nests/{nestId}/eggs")
    suspend fun getAdminEggs(
        @Path("nestId") nestId: Int
    ): Response<PterodactylListResponse<ApplicationEggAttributes>>
}

interface PterodactylApiService : ClientApiService, AdminApiService

