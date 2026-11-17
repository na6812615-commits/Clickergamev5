package com.example.data

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

enum class LeaderboardSortBy(val fieldName: String, val label: String) {
    CLICKS("clicks", "Lượt Click"),
    LIFETIME_POINTS("lifetimePoints", "Tổng Điểm")
}

data class LeaderboardEntry(
    val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val clicks: Long = 0L,
    val lifetimePoints: Long = 0L,
    val bestCombo: Int = 0,
    val restartLv: Int = 0,
    val reactorCode: String = "Mk.I",
    val hasVipPass: Boolean = false,
    val updatedAtMillis: Long = 0L
)

class LeaderboardRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {

    fun observeTopPlayers(
        sortBy: LeaderboardSortBy = LeaderboardSortBy.CLICKS,
        limitCount: Long = DEFAULT_LIMIT
    ): Flow<List<LeaderboardEntry>> {
        val safeLimit = limitCount.coerceIn(1L, MAX_LIMIT)
        return firestore.collection(COLLECTION_LEADERBOARD)
            .orderBy(sortBy.fieldName, Query.Direction.DESCENDING)
            .limit(safeLimit)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc -> doc.toLeaderboardEntry() }
            }
            .catch { error ->
                if (error is CancellationException) throw error
                FirestoreErrorHandler.handleFirestoreError(
                    error = error,
                    operationType = OperationType.LIST,
                    path = COLLECTION_LEADERBOARD,
                    auth = auth
                )
                throw error
            }
    }

    suspend fun submitScore(
        displayName: String,
        game: GameStateEntity
    ): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Vui lòng đăng nhập Google để đồng bộ điểm"))

        val safeName = sanitizeDisplayName(displayName)
        val safeReactorCode = game.reactorTier.code.trim().take(12).ifBlank { "Mk.I" }
        val safeClicks = game.clicks.coerceAtLeast(0L)
        val safeLifetime = game.lifetimePoints
            .coerceAtLeast(0.0)
            .coerceAtMost(Long.MAX_VALUE.toDouble())
            .toLong()
        val safeCombo = game.bestCombo.coerceAtLeast(0)
        val safeRestarts = game.restartLv.coerceAtLeast(0)

        val docRef = firestore.collection(COLLECTION_LEADERBOARD).document(uid)
        return try {
            firestore.runTransaction { tx ->
                val existing = tx.get(docRef)
                if (!existing.exists()) {
                    tx.set(
                        docRef,
                        mapOf(
                            "userId" to uid,
                            "displayName" to safeName,
                            "clicks" to safeClicks,
                            "lifetimePoints" to safeLifetime,
                            "bestCombo" to safeCombo,
                            "restartLv" to safeRestarts,
                            "reactorCode" to safeReactorCode,
                            "hasVipPass" to game.hasVipPass,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                } else {
                    tx.update(
                        docRef,
                        mapOf(
                            "displayName" to safeName,
                            "clicks" to safeClicks,
                            "lifetimePoints" to safeLifetime,
                            "bestCombo" to safeCombo,
                            "restartLv" to safeRestarts,
                            "reactorCode" to safeReactorCode,
                            "hasVipPass" to game.hasVipPass,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FirestoreErrorHandler.handleFirestoreError(
                error = e,
                operationType = OperationType.WRITE,
                path = "$COLLECTION_LEADERBOARD/$uid",
                auth = auth
            )
            Result.failure(e)
        }
    }

    suspend fun getPlayerEntry(userId: String): Result<LeaderboardEntry?> {
        val docPath = "$COLLECTION_LEADERBOARD/$userId"
        return try {
            val snap = firestore.collection(COLLECTION_LEADERBOARD).document(userId).get().await()
            Result.success(if (snap.exists()) snap.toLeaderboardEntry() else null)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FirestoreErrorHandler.handleFirestoreError(
                error = e,
                operationType = OperationType.GET,
                path = docPath,
                auth = auth
            )
            Result.failure(e)
        }
    }

    private fun DocumentSnapshot.toLeaderboardEntry(): LeaderboardEntry? {
        if (!exists()) return null
        val uid = getString("userId") ?: id
        val name = getString("displayName")?.take(40)?.ifBlank { DEFAULT_PLAYER_NAME }
            ?: DEFAULT_PLAYER_NAME
        val clicks = getLong("clicks") ?: 0L
        val lifetime = getLong("lifetimePoints") ?: 0L
        val combo = (getLong("bestCombo") ?: 0L).toInt()
        val restarts = (getLong("restartLv") ?: 0L).toInt()
        val reactor = getString("reactorCode")?.take(12)?.ifBlank { "Mk.I" } ?: "Mk.I"
        val vip = getBoolean("hasVipPass") ?: false
        val updatedTs = getTimestamp(
            "updatedAt",
            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
        )
        return LeaderboardEntry(
            id = id,
            userId = uid,
            displayName = name,
            clicks = clicks,
            lifetimePoints = lifetime,
            bestCombo = combo,
            restartLv = restarts,
            reactorCode = reactor,
            hasVipPass = vip,
            updatedAtMillis = updatedTs?.toDate()?.time ?: 0L
        )
    }

    companion object {
        const val COLLECTION_LEADERBOARD = "leaderboard"
        const val DEFAULT_LIMIT = 50L
        const val MAX_LIMIT = 50L
        const val DEFAULT_PLAYER_NAME = "TapCore Player"

        fun sanitizeDisplayName(raw: String?): String {
            return raw?.trim()?.replace(Regex("\\s+"), " ")?.take(40)
                ?.ifBlank { DEFAULT_PLAYER_NAME }
                ?: DEFAULT_PLAYER_NAME
        }
    }
}
