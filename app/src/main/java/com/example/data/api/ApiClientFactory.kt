package com.example.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClientFactory {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    fun createService(baseUrl: String, apiKey: String): PterodactylApiService? {
        return try {
            val sanitizedUrl = formatBaseUrl(baseUrl)
            if (sanitizedUrl.isBlank()) return null

            val cleanKey = apiKey.trim()

            val authInterceptor = Interceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $cleanKey")
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")

                chain.proceed(requestBuilder.build())
            }

            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build()

            Retrofit.Builder()
                .baseUrl(sanitizedUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(PterodactylApiService::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun createClientService(baseUrl: String, apiKey: String): ClientApiService? {
        return createService(baseUrl, apiKey)
    }

    fun createAdminService(baseUrl: String, apiKey: String): AdminApiService? {
        return createService(baseUrl, apiKey)
    }

    private fun formatBaseUrl(url: String): String {
        return try {
            var cleanUrl = url.trim()
            if (cleanUrl.isBlank()) return "https://localhost/"
            cleanUrl = cleanUrl.replace(" ", "")
            if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
                cleanUrl = "https://$cleanUrl"
            }
            if (!cleanUrl.endsWith("/")) {
                cleanUrl = "$cleanUrl/"
            }
            val parsedHttpUrl = cleanUrl.toHttpUrlOrNull()
            parsedHttpUrl?.toString() ?: "https://localhost/"
        } catch (e: Exception) {
            "https://localhost/"
        }
    }
}
