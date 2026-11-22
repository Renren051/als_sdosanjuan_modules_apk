package com.jannlawm.alssdosanjuan.data.remote

object Constants {
    // 1. Live Cloud Production Backend (Render)
    const val BASE_URL_LIVE = "https://als-sdosanjuan-for-live.onrender.com/api/v1/"

    // 2. Local PC Wi-Fi IP address (Physical phone on same Wi-Fi to host PC)
    const val BASE_URL_DEVICE = "http://192.168.1.15:8000/api/v1/"

    // 3. Android Studio Emulator (Routes to host PC's localhost:8000)
    const val BASE_URL_EMULATOR = "http://10.0.2.2:8000/api/v1/"

    // Default URL used if no custom preference is saved
    const val DEFAULT_BASE_URL = BASE_URL_LIVE

    // Active URL in memory
    var currentBaseUrl: String = DEFAULT_BASE_URL
}

