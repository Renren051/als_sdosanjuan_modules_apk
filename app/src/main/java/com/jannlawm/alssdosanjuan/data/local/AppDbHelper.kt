package com.jannlawm.alssdosanjuan.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDbHelper(context: Context) : SQLiteOpenHelper(context, "als_lms.db", null, 4) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS cached_modules (
                id INTEGER PRIMARY KEY,
                title TEXT,
                description TEXT,
                level TEXT,
                sub_level TEXT,
                language TEXT,
                module_number TEXT,
                file_size TEXT,
                strand_title TEXT,
                local_pdf_path TEXT
            )
        """)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS telemetry_logs (
                module_id INTEGER PRIMARY KEY,
                completion_status TEXT,
                last_page_read INTEGER,
                total_pages INTEGER,
                last_opened_at TEXT,
                is_synced INTEGER
            )
        """)
        createGameScoresTable(db)
        createPlayedGamesTable(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            createGameScoresTable(db)
        }
        if (oldVersion < 4) {
            createPlayedGamesTable(db)
            db.execSQL("""
                INSERT OR IGNORE INTO played_module_games (learner_id, module_id)
                SELECT learner_id, module_id FROM module_game_scores WHERE learner_id IS NOT NULL
            """)
        }
    }

    private fun createGameScoresTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS module_game_scores (
                attempt_uuid TEXT PRIMARY KEY,
                learner_id INTEGER,
                module_id INTEGER NOT NULL,
                game_type TEXT NOT NULL,
                score INTEGER NOT NULL,
                max_score INTEGER NOT NULL,
                moves INTEGER NOT NULL,
                elapsed_seconds INTEGER NOT NULL,
                attempted_at TEXT NOT NULL,
                is_synced INTEGER NOT NULL DEFAULT 0
            )
        """)
    }

    private fun createPlayedGamesTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS played_module_games (
                learner_id INTEGER NOT NULL,
                module_id INTEGER NOT NULL,
                PRIMARY KEY (learner_id, module_id)
            )
        """)
    }

    fun clearModules() {
        val db = writableDatabase
        db.execSQL("DELETE FROM cached_modules")
    }

    fun insertModules(modules: List<CachedModuleEntity>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (m in modules) {
                val cv = ContentValues().apply {
                    put("id", m.id)
                    put("title", m.title)
                    put("description", m.description)
                    put("level", m.level)
                    put("sub_level", m.subLevel)
                    put("language", m.language)
                    put("module_number", m.moduleNumber)
                    put("file_size", m.fileSize)
                    put("strand_title", m.strandTitle)
                    put("local_pdf_path", m.localPdfPath)
                }
                db.insertWithOnConflict("cached_modules", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getCachedModules(
        level: String? = null,
        subLevel: String? = null,
        language: String? = null,
        query: String? = null
    ): List<CachedModuleEntity> {
        val db = readableDatabase
        val conditions = mutableListOf<String>()
        val args = mutableListOf<String>()

        if (!level.isNullOrBlank()) {
            conditions.add("level = ?")
            args.add(level)
        }
        if (!subLevel.isNullOrBlank()) {
            conditions.add("sub_level = ?")
            args.add(subLevel)
        }
        if (!language.isNullOrBlank()) {
            conditions.add("language = ?")
            args.add(language)
        }
        if (!query.isNullOrBlank()) {
            conditions.add("(title LIKE ? OR description LIKE ? OR module_number LIKE ?)")
            val q = "%$query%"
            args.add(q)
            args.add(q)
            args.add(q)
        }

        val whereClause = if (conditions.isNotEmpty()) "WHERE " + conditions.joinToString(" AND ") else ""
        val cursor = db.rawQuery("SELECT * FROM cached_modules $whereClause ORDER BY id ASC", args.toTypedArray())

        val list = mutableListOf<CachedModuleEntity>()
        while (cursor.moveToNext()) {
            list.add(
                CachedModuleEntity(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    level = cursor.getString(cursor.getColumnIndexOrThrow("level")),
                    subLevel = cursor.getString(cursor.getColumnIndexOrThrow("sub_level")),
                    language = cursor.getString(cursor.getColumnIndexOrThrow("language")),
                    moduleNumber = cursor.getString(cursor.getColumnIndexOrThrow("module_number")),
                    fileSize = cursor.getString(cursor.getColumnIndexOrThrow("file_size")),
                    strandTitle = cursor.getString(cursor.getColumnIndexOrThrow("strand_title")),
                    localPdfPath = cursor.getString(cursor.getColumnIndexOrThrow("local_pdf_path"))
                )
            )
        }
        cursor.close()
        return list
    }

    fun upsertTelemetry(moduleId: Int, status: String, page: Int, total: Int, time: String) {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("module_id", moduleId)
            put("completion_status", status)
            put("last_page_read", page)
            put("total_pages", total)
            put("last_opened_at", time)
            put("is_synced", 0)
        }
        db.insertWithOnConflict("telemetry_logs", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getUnsyncedTelemetry(): List<TelemetryEntity> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM telemetry_logs WHERE is_synced = 0", null)
        val list = mutableListOf<TelemetryEntity>()
        while (cursor.moveToNext()) {
            list.add(
                TelemetryEntity(
                    moduleId = cursor.getInt(cursor.getColumnIndexOrThrow("module_id")),
                    completionStatus = cursor.getString(cursor.getColumnIndexOrThrow("completion_status")),
                    lastPageRead = cursor.getInt(cursor.getColumnIndexOrThrow("last_page_read")),
                    totalPages = cursor.getInt(cursor.getColumnIndexOrThrow("total_pages")),
                    lastOpenedAt = cursor.getString(cursor.getColumnIndexOrThrow("last_opened_at")),
                    isSynced = false
                )
            )
        }
        cursor.close()
        return list
    }

    fun markAsSynced(moduleIds: List<Int>) {
        if (moduleIds.isEmpty()) return
        val db = writableDatabase
        val idsStr = moduleIds.joinToString(",")
        db.execSQL("UPDATE telemetry_logs SET is_synced = 1 WHERE module_id IN ($idsStr)")
    }

    fun saveGameScore(score: GameScoreEntity) {
        val values = ContentValues().apply {
            put("attempt_uuid", score.attemptUuid)
            put("learner_id", score.learnerId)
            put("module_id", score.moduleId)
            put("game_type", score.gameType)
            put("score", score.score)
            put("max_score", score.maxScore)
            put("moves", score.moves)
            put("elapsed_seconds", score.elapsedSeconds)
            put("attempted_at", score.attemptedAt)
            put("is_synced", 0)
        }
        writableDatabase.insertWithOnConflict(
            "module_game_scores",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
        markModuleGamePlayed(score.learnerId, score.moduleId)
    }

    fun hasPlayedModuleGame(learnerId: Int, moduleId: Int): Boolean {
        val cursor = readableDatabase.rawQuery(
            "SELECT 1 FROM played_module_games WHERE learner_id = ? AND module_id = ? LIMIT 1",
            arrayOf(learnerId.toString(), moduleId.toString())
        )
        return cursor.use { it.moveToFirst() }
    }

    fun markModuleGamePlayed(learnerId: Int, moduleId: Int) {
        val values = ContentValues().apply {
            put("learner_id", learnerId)
            put("module_id", moduleId)
        }
        writableDatabase.insertWithOnConflict(
            "played_module_games",
            null,
            values,
            SQLiteDatabase.CONFLICT_IGNORE
        )
    }

    fun getUnsyncedGameScores(learnerId: Int): List<GameScoreEntity> {
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM module_game_scores WHERE is_synced = 0 AND learner_id = ? ORDER BY attempted_at ASC",
            arrayOf(learnerId.toString())
        )
        val scores = mutableListOf<GameScoreEntity>()
        cursor.use {
            while (it.moveToNext()) {
                scores.add(
                    GameScoreEntity(
                        attemptUuid = it.getString(it.getColumnIndexOrThrow("attempt_uuid")),
                        learnerId = it.getInt(it.getColumnIndexOrThrow("learner_id")),
                        moduleId = it.getInt(it.getColumnIndexOrThrow("module_id")),
                        gameType = it.getString(it.getColumnIndexOrThrow("game_type")),
                        score = it.getInt(it.getColumnIndexOrThrow("score")),
                        maxScore = it.getInt(it.getColumnIndexOrThrow("max_score")),
                        moves = it.getInt(it.getColumnIndexOrThrow("moves")),
                        elapsedSeconds = it.getInt(it.getColumnIndexOrThrow("elapsed_seconds")),
                        attemptedAt = it.getString(it.getColumnIndexOrThrow("attempted_at")),
                        isSynced = it.getInt(it.getColumnIndexOrThrow("is_synced")) == 1
                    )
                )
            }
        }
        return scores
    }

    fun markGameScoresAsSynced(attemptUuids: List<String>) {
        if (attemptUuids.isEmpty()) return
        val placeholders = attemptUuids.joinToString(",") { "?" }
        writableDatabase.execSQL(
            "UPDATE module_game_scores SET is_synced = 1 WHERE attempt_uuid IN ($placeholders)",
            attemptUuids.toTypedArray()
        )
    }
}
