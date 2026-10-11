package com.jannlawm.alssdosanjuan.data.repository

import android.content.Context
import com.jannlawm.alssdosanjuan.data.local.AppDbHelper
import com.jannlawm.alssdosanjuan.data.local.TelemetryEntity
import com.jannlawm.alssdosanjuan.data.model.SyncProgressRequest
import com.jannlawm.alssdosanjuan.data.model.TelemetryItem
import com.jannlawm.alssdosanjuan.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class TelemetryRepository(context: Context) {
    private val dbHelper = AppDbHelper(context)
    private val apiService = ApiClient.getApiService(context)
    private val appContext = context.applicationContext

    suspend fun saveProgress(moduleId: Int, status: String, page: Int, total: Int, time: String) {
        withContext(Dispatchers.IO) {
            dbHelper.upsertTelemetry(
                moduleId = moduleId,
                status = status,
                page = page,
                total = total,
                time = time
            )
        }
    }

    suspend fun syncUnsyncedTelemetry(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val unsynced = dbHelper.getUnsyncedTelemetry()
                if (unsynced.isEmpty()) return@withContext true

                val items = unsynced.map {
                    TelemetryItem(
                        module_id = it.moduleId,
                        completion_status = it.completionStatus,
                        last_page_read = it.lastPageRead,
                        total_pages = it.totalPages,
                        last_opened_at = it.lastOpenedAt
                    )
                }

                // Get or create device UUID
                val prefs = appContext.getSharedPreferences("als_device_prefs", Context.MODE_PRIVATE)
                var deviceUuid = prefs.getString("device_uuid", null)
                if (deviceUuid == null) {
                    deviceUuid = UUID.randomUUID().toString()
                    prefs.edit().putString("device_uuid", deviceUuid).apply()
                }

                val request = SyncProgressRequest(
                    device_identifier = deviceUuid,
                    items = items
                )

                val response = apiService.syncProgress(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    dbHelper.markAsSynced(unsynced.map { it.moduleId })
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }
    }
}
