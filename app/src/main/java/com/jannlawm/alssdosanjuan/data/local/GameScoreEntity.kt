package com.jannlawm.alssdosanjuan.data.local

data class GameScoreEntity(
    val attemptUuid: String,
    val learnerId: Int,
    val moduleId: Int,
    val gameType: String,
    val score: Int,
    val maxScore: Int,
    val moves: Int,
    val elapsedSeconds: Int,
    val attemptedAt: String,
    val isSynced: Boolean = false
)
