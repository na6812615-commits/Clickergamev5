package com.example

import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import com.example.audio.NoOpAudioController
import com.example.data.AchievementCatalog
import com.example.data.AppDatabase
import com.example.data.GameRepository
import com.example.data.GameStateEntity
import com.example.data.SettingsRepository
import com.example.viewmodel.GameViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [36])
class AchievementRoomTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: GameRepository
    private lateinit var mainExecutor: ExecutorService
    private lateinit var mainDispatcher: CoroutineDispatcher

    @Before
    fun setup() {
        mainExecutor = Executors.newSingleThreadExecutor()
        mainDispatcher = mainExecutor.asCoroutineDispatcher()
        Dispatchers.setMain(mainDispatcher)

        val context = RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = GameRepository(db.gameStateDao(), db.achievementDao())
    }

    @After
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
        mainExecutor.shutdownNow()
    }

    private suspend fun waitUntil(timeoutMs: Long = 3_000L, predicate: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (!predicate() && System.currentTimeMillis() < deadline) {
            delay(25L)
        }
    }

    @Test
    fun syncAchievements_unlocks10kClicksAndFirstPrestige_andPersistsAcrossRestart() = runBlocking {
        val milestoneState = GameStateEntity(
            clicks = 10_000L,
            restartLv = 1,
            diamonds = 0L
        )
        val newlyUnlocked = repository.syncAchievements(milestoneState, nowMillis = 55_000L)
        val unlockedIds = newlyUnlocked.map { it.id }.toSet()
        assertTrue(unlockedIds.contains("clicks_10000"))
        assertTrue(unlockedIds.contains("restart_1"))

        val stored = db.achievementDao().observeAchievements().first().associateBy { it.id }
        assertTrue(stored["clicks_10000"]?.isUnlocked == true)
        assertEquals(55_000L, stored["clicks_10000"]?.unlockedAt)
        assertTrue(stored["restart_1"]?.isUnlocked == true)
        assertEquals(55_000L, stored["restart_1"]?.unlockedAt)

        // Simulate post-Prestige where clicks reset to 0: 'clicks_10000' MUST remain unlocked in Room
        val afterPrestige = GameStateEntity(clicks = 0L, restartLv = 1)
        val secondSync = repository.syncAchievements(afterPrestige, nowMillis = 60_000L)
        assertTrue(secondSync.isEmpty())

        val storedAfter = db.achievementDao().getAchievementsOnce().associateBy { it.id }
        assertTrue(storedAfter["clicks_10000"]?.isUnlocked == true)
        assertEquals(55_000L, storedAfter["clicks_10000"]?.unlockedAt)
    }

    @Test
    fun viewModel_claimAchievement_marksClaimedInRoom_andNeverGrantsDiamonds() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        val settingsRepo = SettingsRepository(context)
        val seeded = GameStateEntity(
            clicks = 10_000L,
            restartLv = 1,
            dollars = 10.0,
            shards = 1L,
            diamonds = 0L
        )
        repository.saveState(seeded)

        val vm = GameViewModel(repository, settingsRepo, NoOpAudioController())
        waitUntil {
            !vm.uiState.value.isLoading &&
                vm.uiState.value.achievementRecords["clicks_10000"]?.isUnlocked == true &&
                vm.uiState.value.achievementRecords["restart_1"]?.isUnlocked == true
        }

        // Open Badge Overlay & inspect synced records
        vm.openBadgeOverlay()
        assertTrue(vm.uiState.value.showBadgeOverlay)
        assertTrue(vm.uiState.value.achievementRecords["clicks_10000"]?.isUnlocked == true)
        assertTrue(vm.uiState.value.achievementRecords["restart_1"]?.isUnlocked == true)

        val tenKDef = AchievementCatalog.all.first { it.id == "clicks_10000" }
        val prestigeDef = AchievementCatalog.all.first { it.id == "restart_1" }

        vm.claimAchievement(tenKDef)
        vm.claimAchievement(prestigeDef)
        waitUntil {
            val recs = vm.uiState.value.achievementRecords
            recs["clicks_10000"]?.isClaimed == true && recs["restart_1"]?.isClaimed == true
        }

        val afterGame = vm.uiState.value.game
        assertEquals(10.0 + tenKDef.rewardDollars + prestigeDef.rewardDollars, afterGame.dollars, 0.01)
        assertEquals(1L + tenKDef.rewardShards + prestigeDef.rewardShards, afterGame.shards)
        assertEquals(0L, afterGame.diamonds)
        assertFalse(afterGame.hasVipPass)

        val dbRecords = db.achievementDao().getAchievementsOnce().associateBy { it.id }
        assertTrue(dbRecords["clicks_10000"]?.isClaimed == true)
        assertTrue(dbRecords["restart_1"]?.isClaimed == true)

        vm.closeBadgeOverlay()
        assertFalse(vm.uiState.value.showBadgeOverlay)
    }

    @Test
    fun roomMigration_7_to_8_createsAchievementsTableSafely() {
        val context = RuntimeEnvironment.getApplication()
        val dbName = "test_migration_7_to_8_${System.nanoTime()}.db"
        context.deleteDatabase(dbName)

        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(7) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS game_state (
                            id INTEGER NOT NULL PRIMARY KEY,
                            diem REAL NOT NULL,
                            dollars REAL NOT NULL,
                            clicks INTEGER NOT NULL,
                            multiplier REAL NOT NULL,
                            baseMultiplier REAL NOT NULL,
                            pointsPerClick REAL NOT NULL,
                            clickCooldownSec REAL NOT NULL,
                            cooldownStep REAL NOT NULL,
                            discountFactor REAL NOT NULL,
                            discountStep REAL NOT NULL,
                            pointPowerLv INTEGER NOT NULL,
                            pointPowerCost REAL NOT NULL,
                            multiplierLv INTEGER NOT NULL,
                            multiplierCost REAL NOT NULL,
                            cooldownLv INTEGER NOT NULL,
                            cooldownCost REAL NOT NULL,
                            discountLv INTEGER NOT NULL,
                            discountCost REAL NOT NULL,
                            menuSpeedLv INTEGER NOT NULL,
                            menuSpeedCost REAL NOT NULL,
                            menuTransitionSec REAL NOT NULL,
                            toastSpeedLv INTEGER NOT NULL,
                            toastSpeedCost REAL NOT NULL,
                            toastDurationSec REAL NOT NULL,
                            autoUnlocked INTEGER NOT NULL,
                            autoEnabled INTEGER NOT NULL,
                            autoPoints REAL NOT NULL,
                            autoCooldownSec REAL NOT NULL,
                            autoPowerLv INTEGER NOT NULL,
                            autoPowerCost REAL NOT NULL,
                            autoSpeedLv INTEGER NOT NULL,
                            autoSpeedCost REAL NOT NULL,
                            autoSpeedStep REAL NOT NULL,
                            restartLv INTEGER NOT NULL,
                            restartCost REAL NOT NULL,
                            bestCombo INTEGER NOT NULL,
                            lifetimePoints REAL NOT NULL,
                            totalCrits INTEGER NOT NULL,
                            critChanceLv INTEGER NOT NULL,
                            critChanceCost REAL NOT NULL,
                            critMultLv INTEGER NOT NULL,
                            critMultCost REAL NOT NULL,
                            comboWindowLv INTEGER NOT NULL,
                            comboWindowCost REAL NOT NULL,
                            passiveLv INTEGER NOT NULL,
                            passiveCost REAL NOT NULL,
                            overdriveCharge REAL NOT NULL,
                            overdriveLv INTEGER NOT NULL,
                            overdriveCost REAL NOT NULL,
                            shards INTEGER NOT NULL,
                            permStartPointsLv INTEGER NOT NULL,
                            permGlobalMultLv INTEGER NOT NULL,
                            permCritLv INTEGER NOT NULL,
                            permDiscountLv INTEGER NOT NULL,
                            claimedAchievements TEXT NOT NULL,
                            diamonds INTEGER NOT NULL,
                            hasVipPass INTEGER NOT NULL,
                            premTapGodLv INTEGER NOT NULL,
                            premMegaCritLv INTEGER NOT NULL,
                            premAutoGodLv INTEGER NOT NULL,
                            premZeroCdLv INTEGER NOT NULL,
                            premWealthMagnetLv INTEGER NOT NULL,
                            premGoldenFeverEndAt INTEGER NOT NULL,
                            dailyStreak INTEGER NOT NULL,
                            lastDailyClaimDay INTEGER NOT NULL,
                            totalDailyClaims INTEGER NOT NULL,
                            seasonXp INTEGER NOT NULL,
                            seasonPassPremium INTEGER NOT NULL,
                            claimedSeasonFree TEXT NOT NULL,
                            claimedSeasonPremium TEXT NOT NULL,
                            gambleSpins INTEGER NOT NULL,
                            gambleWins INTEGER NOT NULL,
                            gambleBiggestWin REAL NOT NULL,
                            gambleJackpotPool REAL NOT NULL,
                            bossLevel INTEGER NOT NULL,
                            bossCurrentHp REAL NOT NULL,
                            bossesDefeated INTEGER NOT NULL,
                            relicTapLv INTEGER NOT NULL,
                            relicCritLv INTEGER NOT NULL,
                            relicLuckLv INTEGER NOT NULL,
                            relicBossLv INTEGER NOT NULL,
                            relicPassiveLv INTEGER NOT NULL,
                            boosterTapSurgeEndAt INTEGER NOT NULL,
                            boosterCritStormEndAt INTEGER NOT NULL,
                            boosterAutoFrenzyEndAt INTEGER NOT NULL,
                            dailyQuestDay INTEGER NOT NULL,
                            dailyQuestTaps INTEGER NOT NULL,
                            dailyQuestCrits INTEGER NOT NULL,
                            dailyQuestGambles INTEGER NOT NULL,
                            dailyQuestBossKills INTEGER NOT NULL,
                            claimedDailyQuests TEXT NOT NULL,
                            techChips INTEGER NOT NULL,
                            fleetScoutLv INTEGER NOT NULL,
                            fleetHarvesterLv INTEGER NOT NULL,
                            fleetCruiserLv INTEGER NOT NULL,
                            fleetMothershipLv INTEGER NOT NULL,
                            activeExpeditionId TEXT NOT NULL,
                            expeditionEndAt INTEGER NOT NULL,
                            totalExpeditionsCompleted INTEGER NOT NULL,
                            stockCoreShares INTEGER NOT NULL,
                            stockCoreAvgCost REAL NOT NULL,
                            stockNeonShares INTEGER NOT NULL,
                            stockNeonAvgCost REAL NOT NULL,
                            stockQbitShares INTEGER NOT NULL,
                            stockQbitAvgCost REAL NOT NULL,
                            stockVoidShares INTEGER NOT NULL,
                            stockVoidAvgCost REAL NOT NULL,
                            stockTotalProfit REAL NOT NULL,
                            stockTradesCount INTEGER NOT NULL,
                            techOverclockLv INTEGER NOT NULL,
                            techAutoProtocolLv INTEGER NOT NULL,
                            techConverterLv INTEGER NOT NULL,
                            techReactorCoreLv INTEGER NOT NULL,
                            techMarketIntelLv INTEGER NOT NULL,
                            techSuperCapLv INTEGER NOT NULL,
                            skillEmpReadyAt INTEGER NOT NULL,
                            skillHyperReadyAt INTEGER NOT NULL,
                            skillHyperEndAt INTEGER NOT NULL,
                            skillRainReadyAt INTEGER NOT NULL,
                            totalSkillsCast INTEGER NOT NULL,
                            quantumCores INTEGER NOT NULL,
                            empireSolarLv INTEGER NOT NULL,
                            empireFoundryLv INTEGER NOT NULL,
                            empireExtractorLv INTEGER NOT NULL,
                            empireLabLv INTEGER NOT NULL,
                            empireCitadelLv INTEGER NOT NULL,
                            empirePendingPoints REAL NOT NULL,
                            empirePendingDollars REAL NOT NULL,
                            empirePendingShards INTEGER NOT NULL,
                            empirePendingCores INTEGER NOT NULL,
                            gearBladeLv INTEGER NOT NULL,
                            gearAegisLv INTEGER NOT NULL,
                            gearNeuralLv INTEGER NOT NULL,
                            gearNovaLv INTEGER NOT NULL,
                            arenaWave INTEGER NOT NULL,
                            arenaEnemyHp REAL NOT NULL,
                            arenaFury REAL NOT NULL,
                            arenaShieldCharges INTEGER NOT NULL,
                            arenaGlory INTEGER NOT NULL,
                            towerHighestFloorClaimed INTEGER NOT NULL,
                            updatedAt INTEGER NOT NULL
                        )
                        """.trimIndent()
                    )
                    db.execSQL(
                        """
                        INSERT INTO game_state (
                            id, diem, dollars, clicks, multiplier, baseMultiplier, pointsPerClick,
                            clickCooldownSec, cooldownStep, discountFactor, discountStep,
                            pointPowerLv, pointPowerCost, multiplierLv, multiplierCost,
                            cooldownLv, cooldownCost, discountLv, discountCost,
                            menuSpeedLv, menuSpeedCost, menuTransitionSec,
                            toastSpeedLv, toastSpeedCost, toastDurationSec,
                            autoUnlocked, autoEnabled, autoPoints, autoCooldownSec,
                            autoPowerLv, autoPowerCost, autoSpeedLv, autoSpeedCost, autoSpeedStep,
                            restartLv, restartCost, bestCombo, lifetimePoints, totalCrits,
                            critChanceLv, critChanceCost, critMultLv, critMultCost,
                            comboWindowLv, comboWindowCost, passiveLv, passiveCost,
                            overdriveCharge, overdriveLv, overdriveCost, shards,
                            permStartPointsLv, permGlobalMultLv, permCritLv, permDiscountLv,
                            claimedAchievements, diamonds, hasVipPass,
                            premTapGodLv, premMegaCritLv, premAutoGodLv, premZeroCdLv, premWealthMagnetLv,
                            premGoldenFeverEndAt, updatedAt,
                            dailyStreak, lastDailyClaimDay, totalDailyClaims, seasonXp, seasonPassPremium,
                            claimedSeasonFree, claimedSeasonPremium, gambleSpins, gambleWins, gambleBiggestWin,
                            gambleJackpotPool, bossLevel, bossCurrentHp, bossesDefeated, relicTapLv, relicCritLv,
                            relicLuckLv, relicBossLv, relicPassiveLv, boosterTapSurgeEndAt, boosterCritStormEndAt,
                            boosterAutoFrenzyEndAt, dailyQuestDay, dailyQuestTaps, dailyQuestCrits,
                            dailyQuestGambles, dailyQuestBossKills, claimedDailyQuests,
                            techChips, fleetScoutLv, fleetHarvesterLv, fleetCruiserLv, fleetMothershipLv,
                            activeExpeditionId, expeditionEndAt, totalExpeditionsCompleted,
                            stockCoreShares, stockCoreAvgCost, stockNeonShares, stockNeonAvgCost,
                            stockQbitShares, stockQbitAvgCost, stockVoidShares, stockVoidAvgCost,
                            stockTotalProfit, stockTradesCount,
                            techOverclockLv, techAutoProtocolLv, techConverterLv, techReactorCoreLv,
                            techMarketIntelLv, techSuperCapLv,
                            skillEmpReadyAt, skillHyperReadyAt, skillHyperEndAt, skillRainReadyAt, totalSkillsCast,
                            quantumCores, empireSolarLv, empireFoundryLv, empireExtractorLv, empireLabLv, empireCitadelLv,
                            empirePendingPoints, empirePendingDollars, empirePendingShards, empirePendingCores,
                            gearBladeLv, gearAegisLv, gearNeuralLv, gearNovaLv,
                            arenaWave, arenaEnemyHp, arenaFury, arenaShieldCharges, arenaGlory, towerHighestFloorClaimed
                        ) VALUES (
                            1, 5000.0, 50.0, 10000, 2.0, 1.5, 5.0,
                            0.25, 0.125, 1.0, 0.1,
                            1, 75.0, 1, 150.0,
                            0, 75.0, 0, 5.0,
                            0, 3.0, 1.0,
                            0, 3.0, 1.0,
                            1, 0, 2.0, 1.0,
                            0, 15.0, 0, 20.0, 0.25,
                            1, 450.0, 10, 5000.0, 2,
                            0, 120.0, 0, 200.0,
                            0, 90.0, 0, 8.0,
                            0.0, 0, 12.0, 3,
                            0, 0, 0, 0,
                            '', 0, 0,
                            0, 0, 0, 0, 0,
                            0, 4000,
                            1, 1, 1, 50, 0,
                            '', '', 0, 0, 0.0,
                            2500.0, 1, 250.0, 0, 0, 0,
                            0, 0, 0, 0, 0,
                            0, 1, 0, 0,
                            0, 0, '',
                            2, 0, 0, 0, 0,
                            '', 0, 0,
                            0, 0.0, 0, 0.0,
                            0, 0.0, 0, 0.0,
                            0.0, 0,
                            0, 0, 0, 0,
                            0, 0,
                            0, 0, 0, 0, 0,
                            1, 1, 0, 0, 0, 0,
                            0.0, 0.0, 0, 0,
                            0, 0, 0, 0,
                            2, 666.0, 0.0, 0, 25, 1
                        )
                        """.trimIndent()
                    )
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val rawDb = FrameworkSQLiteOpenHelperFactory().create(config).writableDatabase
        rawDb.close()

        val migratedRoomDb = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(AppDatabase.MIGRATION_7_8, AppDatabase.MIGRATION_8_9)
            .allowMainThreadQueries()
            .build()
        val loaded = runBlocking { migratedRoomDb.gameStateDao().getGameStateOnce() }
        val repo = GameRepository(migratedRoomDb.gameStateDao(), migratedRoomDb.achievementDao())
        val synced = runBlocking {
            repo.syncAchievements(loaded!!, nowMillis = 99_000L)
        }
        migratedRoomDb.close()
        context.deleteDatabase(dbName)

        assertNotNull(loaded)
        assertEquals(10_000L, loaded!!.clicks)
        assertEquals(1, loaded.restartLv)
        assertTrue(synced.any { it.id == "clicks_10000" })
        assertTrue(synced.any { it.id == "restart_1" })
    }
}
