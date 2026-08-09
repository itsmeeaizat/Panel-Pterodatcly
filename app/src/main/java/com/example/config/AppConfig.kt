package com.example.config

/**
 * Konfigurasi Default Panel Pterodactyl & API Key (File Mentah / Source Code Config).
 * 
 * Anda dapat mengonfigurasi URL Panel dan API Key langsung di file ini.
 * Jika variabel [DEFAULT_PANEL_URL] dan [DEFAULT_API_KEY] diisi, aplikasi akan
 * secara otomatis menggunakannya sebagai panel utama saat pertama kali dibuka.
 */
object AppConfig {
    // URL Web Panel Pterodactyl (contoh: "https://panel.domainanda.com")
    const val DEFAULT_PANEL_URL: String = ""

    // API Key Pterodactyl (Client Token "ptlc_..." atau Admin Token "ptla_...")
    const val DEFAULT_API_KEY: String = ""

    // Nama/Label untuk panel ini di dalam aplikasi
    const val DEFAULT_ACCOUNT_NAME: String = "Main Panel"
}
