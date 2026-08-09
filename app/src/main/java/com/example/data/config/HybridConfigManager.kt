package com.example.data.config

import android.content.Context
import com.example.config.AppConfig
import org.json.JSONObject

/**
 * Mode Sumber Konfigurasi Aplikasi (Hybrid Configuration).
 */
enum class ConfigSourceMode {
    RAW_FILE,      // Memuat dari file mentah assets/app_config.json
    CUSTOM_MANUAL  // Memuat dari input manual user di dalam aplikasi
}

/**
 * Data Model Konfigurasi Mentah dari File Local `assets/app_config.json`.
 */
data class RawConfigFileData(
    val panelUrl: String = "",
    val apiKey: String = "",
    val accountName: String = "Account Bawaan",
    val isDefault: Boolean = true,
    val source: String = "Bawaan Sistem",
    val nodeCount: Int = 0,
    val defaultServerCount: Int = 0,
    val rawJsonString: String = "{}"
)

/**
 * Manager Konfigurasi Ganda (Hybrid Configuration Manager)
 * Mengelola pembacaan file mentah assets/app_config.json, pilihan sumber konfigurasi,
 * serta penyimpanan API Key kustom yang diubah secara dinamis melalui UI aplikasi.
 */
class HybridConfigManager(private val context: Context) {

    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "hybrid_config_prefs"
        private const val KEY_CONFIG_MODE = "config_source_mode"
        private const val KEY_CUSTOM_API_KEY = "custom_api_key"
        private const val KEY_CUSTOM_PANEL_URL = "custom_panel_url"
        private const val KEY_CUSTOM_ACCOUNT_NAME = "custom_account_name"
        private const val RAW_FILE_NAME = "app_config.json"
    }

    /**
     * Membaca file mentah `assets/default_pterodactyl.json` atau `assets/app_config.json` dengan aman.
     */
    fun loadRawConfigFile(): RawConfigFileData {
        val candidateFiles = listOf("default_pterodactyl.json", "app_config.json")
        for (fileName in candidateFiles) {
            try {
                val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(jsonString)

                val url = jsonObject.optString("panelUrl", AppConfig.DEFAULT_PANEL_URL)
                val key = jsonObject.optString("apiKey", AppConfig.DEFAULT_API_KEY)
                val name = jsonObject.optString("accountName", AppConfig.DEFAULT_ACCOUNT_NAME)
                val isDefault = jsonObject.optBoolean("isDefault", true)
                val source = jsonObject.optString("source", "Bawaan Sistem")
                val nodesArr = jsonObject.optJSONArray("nodes")
                val serversArr = jsonObject.optJSONArray("defaultServers")

                return RawConfigFileData(
                    panelUrl = url,
                    apiKey = key,
                    accountName = name,
                    isDefault = isDefault,
                    source = source,
                    nodeCount = nodesArr?.length() ?: 0,
                    defaultServerCount = serversArr?.length() ?: 0,
                    rawJsonString = jsonString
                )
            } catch (e: Exception) {
                // Lanjut ke file berikutnya jika ada
            }
        }

        // Fallback ke AppConfig.kt jika tidak ada file assets yang bisa dibaca
        return RawConfigFileData(
            panelUrl = AppConfig.DEFAULT_PANEL_URL,
            apiKey = AppConfig.DEFAULT_API_KEY,
            accountName = AppConfig.DEFAULT_ACCOUNT_NAME,
            isDefault = true,
            source = "Bawaan Sistem",
            rawJsonString = "{\n  \"panelUrl\": \"${AppConfig.DEFAULT_PANEL_URL}\",\n  \"apiKey\": \"${AppConfig.DEFAULT_API_KEY}\",\n  \"accountName\": \"Account Bawaan\",\n  \"isDefault\": true,\n  \"source\": \"Bawaan Sistem\"\n}"
        )
    }

    /**
     * Mendapatkan Mode Sumber Konfigurasi Aktif.
     */
    fun getConfigSourceMode(): ConfigSourceMode {
        val modeStr = prefs.getString(KEY_CONFIG_MODE, ConfigSourceMode.RAW_FILE.name)
        return try {
            ConfigSourceMode.valueOf(modeStr ?: ConfigSourceMode.RAW_FILE.name)
        } catch (e: Exception) {
            ConfigSourceMode.RAW_FILE
        }
    }

    /**
     * Mengubah Mode Sumber Konfigurasi (RAW_FILE vs CUSTOM_MANUAL).
     */
    fun setConfigSourceMode(mode: ConfigSourceMode) {
        prefs.edit().putString(KEY_CONFIG_MODE, mode.name).apply()
    }

    /**
     * Mendapatkan Custom API Key yang diinput pengguna dari aplikasi.
     */
    fun getCustomApiKey(): String {
        return prefs.getString(KEY_CUSTOM_API_KEY, "") ?: ""
    }

    /**
     * Mendapatkan Custom Panel URL yang diinput pengguna dari aplikasi.
     */
    fun getCustomPanelUrl(): String {
        return prefs.getString(KEY_CUSTOM_PANEL_URL, "") ?: ""
    }

    /**
     * Mendapatkan Custom Account Name.
     */
    fun getCustomAccountName(): String {
        return prefs.getString(KEY_CUSTOM_ACCOUNT_NAME, "Custom Panel") ?: "Custom Panel"
    }

    /**
     * Menyimpan/Memperbarui Custom API Key dan Panel URL secara dinamis dari aplikasi.
     */
    fun saveCustomConfig(panelUrl: String, apiKey: String, accountName: String = "Custom Panel") {
        prefs.edit()
            .putString(KEY_CUSTOM_PANEL_URL, panelUrl.trim().trimEnd('/'))
            .putString(KEY_CUSTOM_API_KEY, apiKey.trim())
            .putString(KEY_CUSTOM_ACCOUNT_NAME, accountName.trim().ifEmpty { "Custom Panel" })
            .apply()
    }

    /**
     * Mendapatkan Nilai Efektif URL & API Key berdasarkan pilihan mode pengguna.
     */
    fun getEffectiveConfig(): EffectiveConfig {
        val mode = getConfigSourceMode()
        val customApiKey = getCustomApiKey()
        val customUrl = getCustomPanelUrl()
        val customName = getCustomAccountName()

        val rawData = loadRawConfigFile()

        return when (mode) {
            ConfigSourceMode.RAW_FILE -> {
                // Jika di mode RAW_FILE tapi pengguna memasukkan custom API key dinamis,
                // prioritaskan API Key baru jika diisi.
                val effectiveApiKey = if (customApiKey.isNotBlank()) customApiKey else rawData.apiKey
                val effectiveUrl = if (customUrl.isNotBlank()) customUrl else rawData.panelUrl
                EffectiveConfig(
                    sourceMode = ConfigSourceMode.RAW_FILE,
                    panelUrl = effectiveUrl,
                    apiKey = effectiveApiKey,
                    accountName = rawData.accountName,
                    isCustomApiKeyApplied = customApiKey.isNotBlank() && customApiKey != rawData.apiKey
                )
            }
            ConfigSourceMode.CUSTOM_MANUAL -> {
                EffectiveConfig(
                    sourceMode = ConfigSourceMode.CUSTOM_MANUAL,
                    panelUrl = customUrl.ifBlank { rawData.panelUrl },
                    apiKey = customApiKey.ifBlank { rawData.apiKey },
                    accountName = customName,
                    isCustomApiKeyApplied = customApiKey.isNotBlank()
                )
            }
        }
    }
}

/**
 * Model Hasil Kombinasi Konfigurasi Efektif yang Digunakan Sistem.
 */
data class EffectiveConfig(
    val sourceMode: ConfigSourceMode,
    val panelUrl: String,
    val apiKey: String,
    val accountName: String,
    val isCustomApiKeyApplied: Boolean
)
