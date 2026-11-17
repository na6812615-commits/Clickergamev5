package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class GameRepository(
    private val dao: GameStateDao,
    private val achievementDao: AchievementDao? = null
) {
    fun observeGameState(): Flow<GameStateEntity?> = dao.observeGameState()

    fun observeAchievements(): Flow<List<AchievementEntity>> =
        achievementDao?.observeAchievements() ?: flowOf(emptyList())

    suspend fun getAchievementsOnce(): List<AchievementEntity> =
        achievementDao?.getAchievementsOnce() ?: emptyList()

    suspend fun loadOrCreate(): GameStateEntity {
        val existing = dao.getGameStateOnce()
        if (existing != null) return existing
        val initial = GameStateEntity()
        dao.upsertGameState(initial)
        return initial
    }

    suspend fun saveState(state: GameStateEntity) {
        dao.upsertGameState(state.copy(updatedAt = System.currentTimeMillis()))
    }

    /**
     * Evaluates all achievement milestones against the current [GameStateEntity] and persists
     * unlocked status, progress, and claim metadata into the Room `achievements` table.
     * Already-unlocked achievements remain unlocked permanently (even after Restarts/Prestige).
     *
     * @return list of [AchievementDef] newly unlocked during this evaluation.
     */
    suspend fun syncAchievements(
        game: GameStateEntity,
        nowMillis: Long = System.currentTimeMillis()
    ): List<AchievementDef> {
        val aDao = achievementDao ?: return emptyList()
        val existingMap = aDao.getAchievementsOnce().associateBy { it.id }
        val newlyUnlocked = mutableListOf<AchievementDef>()
        val toUpsert = mutableListOf<AchievementEntity>()

        for (def in AchievementCatalog.all) {
            val prev = existingMap[def.id]
            val currentVal = def.progressSelector(game).coerceAtLeast(0.0)
            val alreadyClaimed = (prev?.isClaimed == true) || game.isAchievementClaimed(def.id)
            val wasUnlocked = prev?.isUnlocked == true
            val isNowUnlocked = wasUnlocked || alreadyClaimed || (currentVal >= def.targetValue)
            val storedProgress = maxOf(prev?.progressValue ?: 0.0, currentVal).coerceAtMost(def.targetValue)
            val unlockedAt = when {
                wasUnlocked && (prev?.unlockedAt ?: 0L) > 0L -> prev!!.unlockedAt
                isNowUnlocked -> nowMillis
                else -> 0L
            }
            val claimedAt = when {
                alreadyClaimed && (prev?.claimedAt ?: 0L) > 0L -> prev!!.claimedAt
                alreadyClaimed -> nowMillis
                else -> 0L
            }

            if (isNowUnlocked && !wasUnlocked && !alreadyClaimed) {
                newlyUnlocked += def
            }

            val nextEntity = AchievementEntity(
                id = def.id,
                isUnlocked = isNowUnlocked,
                isClaimed = alreadyClaimed,
                progressValue = if (isNowUnlocked) def.targetValue else storedProgress,
                targetValue = def.targetValue,
                unlockedAt = unlockedAt,
                claimedAt = claimedAt
            )

            if (prev != nextEntity) {
                toUpsert += nextEntity
            }
        }

        if (toUpsert.isNotEmpty()) {
            aDao.upsertAll(toUpsert)
        }
        return newlyUnlocked
    }

    suspend fun markAchievementClaimed(
        def: AchievementDef,
        nowMillis: Long = System.currentTimeMillis()
    ) {
        val aDao = achievementDao ?: return
        val existing = aDao.getAchievementsOnce().firstOrNull { it.id == def.id }
        aDao.upsertAchievement(
            AchievementEntity(
                id = def.id,
                isUnlocked = true,
                isClaimed = true,
                progressValue = def.targetValue,
                targetValue = def.targetValue,
                unlockedAt = existing?.unlockedAt?.takeIf { it > 0L } ?: nowMillis,
                claimedAt = nowMillis
            )
        )
    }

    suspend fun clearAchievements() {
        achievementDao?.clearAll()
    }
}
