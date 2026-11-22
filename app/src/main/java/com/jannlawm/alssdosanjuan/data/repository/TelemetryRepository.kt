package com.jannlawm.alssdosanjuan.data.repository

import android.content.Context
import com.jannlawm.alssdosanjuan.data.local.AppDbHelper
import com.jannlawm.alssdosanjuan.data.local.GameScoreEntity
import com.jannlawm.alssdosanjuan.data.local.TokenManager
import com.jannlawm.alssdosanjuan.data.model.GameScoreItem
import com.jannlawm.alssdosanjuan.data.model.SyncGameScoresRequest
import com.jannlawm.alssdosanjuan.data.model.SyncProgressRequest
import com.jannlawm.alssdosanjuan.data.model.TelemetryItem
import com.jannlawm.alssdosanjuan.data.remote.ApiClient
import com.jannlawm.alssdosanjuan.data.remote.ApiService
import com.jannlawm.alssdosanjuan.data.worker.SyncWorker
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class TelemetryRepository(private val context: Context) {
    private val dbHelper = AppDbHelper(context)
    private val apiService: ApiService
        get() = ApiClient.getApiService(context)
    private val appContext = context.applicationContext

    suspend fun saveModuleGameScore(
        moduleId: Int,
        gameType: String,
        score: Int,
        moves: Int,
        elapsedSeconds: Int
    ) {
        withContext(Dispatchers.IO) {
            val learnerId = TokenManager(appContext).getUser()?.id ?: return@withContext
            val attemptedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date())
            dbHelper.saveGameScore(
                GameScoreEntity(
                    attemptUuid = UUID.randomUUID().toString(),
                    learnerId = learnerId,
                    moduleId = moduleId,
                    gameType = gameType,
                    score = score,
                    maxScore = 100,
                    moves = moves,
                    elapsedSeconds = elapsedSeconds,
                    attemptedAt = attemptedAt
                )
            )
            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(appContext).enqueueUniqueWork(
                "AlsImmediateTelemetrySyncWork",
                ExistingWorkPolicy.KEEP,
                syncRequest
            )
        }
    }

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
                if (unsynced.isNotEmpty()) {
                    val items = unsynced.map {
                        TelemetryItem(
                            module_id = it.moduleId,
                            completion_status = it.completionStatus,
                            last_page_read = it.lastPageRead,
                            total_pages = it.totalPages,
                            last_opened_at = it.lastOpenedAt
                        )
                    }

                    val response = apiService.syncProgress(
                        SyncProgressRequest(
                            device_identifier = getDeviceUuid(),
                            items = items
                        )
                    )
                    if (!response.isSuccessful || response.body()?.success != true) return@withContext false
                    dbHelper.markAsSynced(unsynced.map { it.moduleId })
                }

                val learnerId = TokenManager(appContext).getUser()?.id
                val unsyncedScores = learnerId?.let(dbHelper::getUnsyncedGameScores).orEmpty()
                for (batch in unsyncedScores.chunked(50)) {
                    val response = apiService.syncGameScores(
                        SyncGameScoresRequest(
                            device_identifier = getDeviceUuid(),
                            attempts = batch.map {
                                GameScoreItem(
                                    attempt_uuid = it.attemptUuid,
                                    module_id = it.moduleId,
                                    game_type = it.gameType,
                                    score = it.score,
                                    max_score = it.maxScore,
                                    moves = it.moves,
                                    elapsed_seconds = it.elapsedSeconds,
                                    attempted_at = it.attemptedAt
                                )
                            }
                        )
                    )
                    if (!response.isSuccessful || response.body()?.success != true) return@withContext false
                    dbHelper.markGameScoresAsSynced(batch.map { it.attemptUuid })
                }

                true
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun getDeviceUuid(): String {
        val prefs = appContext.getSharedPreferences("als_device_prefs", Context.MODE_PRIVATE)
        val existing = prefs.getString("device_uuid", null)
        if (existing != null) return existing
        return UUID.randomUUID().toString().also {
            prefs.edit().putString("device_uuid", it).apply()
        }
    }
}
