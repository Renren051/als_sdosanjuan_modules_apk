package com.jannlawm.alssdosanjuan.data.remote

object Constants {
    // Local PC Wi-Fi IP address (Connects physical phone 192.168.1.16 to PC 192.168.1.15)
    const val BASE_URL_DEVICE = "http://192.168.1.15:8000/api/v1/"
    const val BASE_URL_EMULATOR = "http://10.0.2.2:8000/api/v1/"
    
    // Active URL used by Retrofit (Set to BASE_URL_DEVICE for real phones)
    var currentBaseUrl: String = BASE_URL_DEVICE
}
