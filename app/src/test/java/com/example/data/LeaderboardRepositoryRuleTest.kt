package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class LeaderboardRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun submitScore_authenticatedOwner_createsAndUpdatesEntry() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = LeaderboardRepository(firestore, auth)

        val initialGame = GameStateEntity(
            clicks = 420L,
            lifetimePoints = 12_500.0,
            bestCombo = 35,
            restartLv = 1,
            hasVipPass = false
        )
        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.submitScore("Alice Cyber", initialGame)
        }
        assertTrue("Create score failed: ${createResult.exceptionOrNull()}", createResult.isSuccess)

        val updatedGame = initialGame.copy(
            clicks = 1050L,
            lifetimePoints = 98_000.0,
            bestCombo = 80,
            restartLv = 3,
            hasVipPass = true
        )
        val updateResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.submitScore("Alice Cyber", updatedGame)
        }
        assertTrue("Update score failed: ${updateResult.exceptionOrNull()}", updateResult.isSuccess)

        val fetched = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getPlayerEntry(uid).getOrThrow()
        }
        assertNotNull(fetched)
        assertEquals(uid, fetched!!.userId)
        assertEquals(1050L, fetched.clicks)
        assertEquals(98_000L, fetched.lifetimePoints)
        assertEquals(80, fetched.bestCombo)
        assertEquals(3, fetched.restartLv)
        assertTrue(fetched.hasVipPass)
    }

    @Test
    fun observeTopPlayers_authenticated_returnsSortedLeaderboard() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = LeaderboardRepository(firestore, auth)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.submitScore(
                "Alice",
                GameStateEntity(clicks = 300L, lifetimePoints = 5000.0)
            ).getOrThrow()
        }

        val bobUid = signInTestUser(BOB_EMAIL)
        val bobRepo = LeaderboardRepository(firestore, auth)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.submitScore(
                "Bob Pro",
                GameStateEntity(clicks = 2500L, lifetimePoints = 40000.0, restartLv = 2)
            ).getOrThrow()
        }

        val entries = withTimeout(FLOW_TIMEOUT_MS) {
            bobRepo.observeTopPlayers(LeaderboardSortBy.CLICKS).first { list ->
                list.any { it.userId == aliceUid } && list.any { it.userId == bobUid }
            }
        }
        val bobIndex = entries.indexOfFirst { it.userId == bobUid }
        val aliceIndex = entries.indexOfFirst { it.userId == aliceUid }
        assertTrue("Expected Bob above Alice by clicks", bobIndex in 0 until aliceIndex)
    }

    @Test
    fun writeAnotherUsersDocument_authenticated_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = LeaderboardRepository(firestore, auth)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.submitScore("Alice", GameStateEntity(clicks = 100L)).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection(LeaderboardRepository.COLLECTION_LEADERBOARD)
                    .document(aliceUid)
                    .update("clicks", 999999L)
                    .await()
            }
            fail("Expected PERMISSION_DENIED when Bob updates Alice's document")
        } catch (e: Exception) {
            val fsEx = e.unwrapFirestoreException()
            assertNotNull("Expected FirebaseFirestoreException, got $e", fsEx)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx!!.code)
        }
    }

    @Test
    fun observeTopPlayers_unauthenticated_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = LeaderboardRepository(firestore, auth)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeTopPlayers(LeaderboardSortBy.CLICKS).first()
            }
            fail("Expected PERMISSION_DENIED for unauthenticated leaderboard read")
        } catch (e: Exception) {
            val fsEx = e.unwrapFirestoreException()
            assertNotNull("Expected nested FirebaseFirestoreException, got $e", fsEx)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx!!.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice_tapcore@test.com"
        const val BOB_EMAIL = "bob_tapcore@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
