package com.example.data.announcement

import android.content.Context
import org.json.JSONObject

/**
 * Data Model untuk Pengumuman / Popup Pemberitahuan.
 */
data class AnnouncementData(
    val isActive: Boolean = false,
    val version: Int = 1,
    val title: String = "",
    val content: String = ""
)

/**
 * Helper Manager untuk membaca file mentah `assets/announcement.json`
 * dan mengelola status penutupan popup di SharedPreferences.
 */
object AnnouncementManager {

    private const val PREF_NAME = "announcement_prefs"
    private const val KEY_DISMISSED_VERSION = "dismissed_version"
    private const val FILE_NAME = "announcement.json"

    /**
     * Membaca dan mem-parse file JSON mentah dari folder assets.
     */
    fun loadAnnouncement(context: Context): AnnouncementData? {
        return try {
            val jsonString = context.assets.open(FILE_NAME).bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)

            AnnouncementData(
                isActive = jsonObject.optBoolean("isActive", false),
                version = jsonObject.optInt("version", 1),
                title = jsonObject.optString("title", "Pengumuman"),
                content = jsonObject.optString("content", "")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Memeriksa apakah popup harus ditampilkan berdasarkan status `isActive`
     * dan versi pengumuman yang tersimpan di SharedPreferences.
     */
    fun shouldShowAnnouncement(context: Context): AnnouncementData? {
        val announcement = loadAnnouncement(context) ?: return null
        if (!announcement.isActive) return null

        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val dismissedVersion = prefs.getInt(KEY_DISMISSED_VERSION, 0)

        return if (announcement.version > dismissedVersion) {
            announcement
        } else {
            null
        }
    }

    /**
     * Menyimpan versi pengumuman yang telah ditutup oleh user ke SharedPreferences.
     */
    fun markAsDismissed(context: Context, version: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_DISMISSED_VERSION, version).apply()
    }
}
