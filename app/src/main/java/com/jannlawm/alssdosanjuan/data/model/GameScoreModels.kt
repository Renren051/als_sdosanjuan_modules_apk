package com.jannlawm.alssdosanjuan.data.model

data class SyncGameScoresRequest(
    val device_identifier: String,
    val attempts: List<GameScoreItem>
)

data class GameScoreItem(
    val attempt_uuid: String,
    val module_id: Int,
    val game_type: String,
    val score: Int,
    val max_score: Int,
    val moves: Int,
    val elapsed_seconds: Int,
    val attempted_at: String
)

data class SyncGameScoresResponse(
    val success: Boolean,
    val message: String? = null
)

data class ModuleGameStatusResponse(
    val success: Boolean,
    val data: ModuleGameStatusData? = null
)

data class ModuleGameStatusData(
    val played: Boolean
)
