package com.jannlawm.alssdosanjuan.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.jannlawm.alssdosanjuan.data.model.User
import com.jannlawm.alssdosanjuan.data.remote.Constants

class TokenManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "als_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val gson = Gson()

    fun saveToken(token: String) {
        sharedPreferences.edit().putString("auth_token", token).apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }

    fun saveUser(user: User) {
        sharedPreferences.edit()
            .putString("user_role", user.role)
            .putString("user_json", gson.toJson(user))
            .apply()
    }

    fun getUser(): User? {
        val json = sharedPreferences.getString("user_json", null) ?: return null
        return try {
            gson.fromJson(json, User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun saveUserRole(role: String) {
        sharedPreferences.edit().putString("user_role", role).apply()
    }

    fun getUserRole(): String? {
        return sharedPreferences.getString("user_role", null)
    }

    fun saveDarkMode(isDarkMode: Boolean) {
        sharedPreferences.edit().putBoolean("is_dark_mode", isDarkMode).apply()
    }

    fun isDarkMode(): Boolean {
        return sharedPreferences.getBoolean("is_dark_mode", false)
    }

    fun saveBaseUrl(url: String) {
        sharedPreferences.edit().putString("custom_base_url", url).apply()
    }

    fun getBaseUrl(): String {
        return sharedPreferences.getString("custom_base_url", Constants.DEFAULT_BASE_URL)
            ?: Constants.DEFAULT_BASE_URL
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}
