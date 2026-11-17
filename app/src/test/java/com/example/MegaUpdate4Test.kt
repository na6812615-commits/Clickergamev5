package com.example

import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import com.example.audio.NoOpAudioController
import com.example.data.AppDatabase
import com.example.data.CyberDroneId
import com.example.data.CyberRuneId
import com.example.data.GameRepository
import com.example.data.GameStateEntity
import com.example.data.RaidDungeonCatalog
import com.example.data.SettingsRepository
import com.example.data.SingularityPerkId
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.SingularityEngine
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [36])
class MegaUpdate4Test {

    @Test
    fun aiDrones_and_swarmOverclock_scaleOutputWithoutDiamondLeaks() {
        val start = GameStateEntity(
            dollars = 500.0,
            techChips = 30L,
            quantumCores = 10L,
            diamonds = 0L
        )
        val afterPulse = SingularityEngine.upgradeDrone(start, CyberDroneId.PULSE_DRONE).updatedGame!!
        val afterStriker = SingularityEngine.upgradeDrone(afterPulse, CyberDroneId.STRIKER_DRONE).updatedGame!!
        val afterMiner = SingularityEngine.upgradeDrone(afterStriker, CyberDroneId.MINER_DRONE).updatedGame!!
        val afterChrono = SingularityEngine.upgradeDrone(afterMiner, CyberDroneId.CHRONO_DRONE).updatedGame!!

        assertEquals(4, afterChrono.totalDroneLevel)
        assertTrue(afterChrono.dronePointsPerSec > 0.0)
        assertTrue(afterChrono.droneBossDps > 0.0)
        assertTrue(afterChrono.droneDollarsPer10s > 0.0)
        assertTrue(afterChrono.effectivePointsPerClick > start.effectivePointsPerClick)
        assertTrue(afterChrono.skillCooldownFactor < 1.0)

        val overclocked = SingularityEngine.activateDroneOverclock(afterChrono, nowMillis = 10_000L).updatedGame!!
        assertEquals(40_000L, overclocked.droneOverclockEndAt)
        assertEquals(0L, overclocked.diamonds)
        assertFalse(overclocked.hasVipPass)
    }

    @Test
    fun cyberRunes_threeSlotMatrixAndSynergyBonus_enforcesMaxSlots() {
        val start = GameStateEntity(
            shards = 50L,
            quantumCores = 20L,
            voidEssence = 10L,
            diamonds = 0L
        )
        val r1 = SingularityEngine.upgradeRune(start, CyberRuneId.BERSERK).updatedGame!!
        val r2 = SingularityEngine.upgradeRune(r1, CyberRuneId.MIDAS).updatedGame!!
        val r3 = SingularityEngine.upgradeRune(r2, CyberRuneId.VAMPIRE).updatedGame!!
        val r4 = SingularityEngine.upgradeRune(r3, CyberRuneId.CHRONO).updatedGame!!

        // First 3 runes auto-equipped; 4th crafted but stays unequipped because max 3 slots
        assertEquals(3, r4.equippedRuneIds.size)
        assertEquals(1.45, r4.equippedRuneSynergyMultiplier, 0.001)
        assertFalse(r4.isRuneEquipped(CyberRuneId.CHRONO))

        // Trying to equip 4th rune while 3 equipped fails cleanly
        val equipBlocked = SingularityEngine.toggleEquipRune(r4, CyberRuneId.CHRONO)
        assertNull(equipBlocked.updatedGame)
        assertTrue(equipBlocked.isError)

        // Unequip one, then equip Chrono
        val unequipped = SingularityEngine.toggleEquipRune(r4, CyberRuneId.MIDAS).updatedGame!!
        assertEquals(1.20, unequipped.equippedRuneSynergyMultiplier, 0.001)
        val swapped = SingularityEngine.toggleEquipRune(unequipped, CyberRuneId.CHRONO).updatedGame!!
        assertTrue(swapped.isRuneEquipped(CyberRuneId.CHRONO))
        assertEquals(3, swapped.equippedRuneIds.size)
        assertEquals(0L, swapped.diamonds)
    }

    @Test
    fun raidDungeons_threeStageCombatAndSingularityAscension_neverGrantDiamonds() {
        val strongStart = GameStateEntity(
            pointsPerClick = 5_000.0,
            multiplier = 4.0,
            dollars = 100.0,
            raidEnergy = 3,
            restartLv = 3,
            lifetimePoints = 150_000.0,
            diamonds = 0L
        )
        val dungeon = RaidDungeonCatalog.dungeons.first() // red_datacenter
        val entered = SingularityEngine.enterDungeon(strongStart, dungeon).updatedGame!!
        assertEquals(dungeon.id, entered.activeDungeonId)
        assertEquals(1, entered.dungeonStage)
        assertEquals(2, entered.raidEnergy)

        val clear1 = SingularityEngine.strikeDungeonEnemy(entered, rollIsCrit = true, isOverdrive = false)
        assertTrue(clear1.stageCleared)
        assertFalse(clear1.dungeonCleared)
        assertEquals(2, clear1.updatedGame!!.dungeonStage)

        val clear2 = SingularityEngine.strikeDungeonEnemy(clear1.updatedGame!!, rollIsCrit = true, isOverdrive = false)
        assertTrue(clear2.stageCleared)
        assertFalse(clear2.dungeonCleared)
        assertEquals(3, clear2.updatedGame!!.dungeonStage)

        val clear3 = SingularityEngine.strikeDungeonEnemy(clear2.updatedGame!!, rollIsCrit = true, isOverdrive = true)
        assertTrue(clear3.dungeonCleared)
        val afterRaid = clear3.updatedGame!!
        assertEquals("", afterRaid.activeDungeonId)
        assertEquals(1L, afterRaid.totalDungeonsCleared)
        assertTrue(afterRaid.voidEssence >= dungeon.rewardVoidEssence)
        assertEquals(0L, afterRaid.diamonds)

        // Perform Singularity Ascension & spend Void Essence on Perk
        assertTrue(afterRaid.canAscendSingularity)
        val ascended = SingularityEngine.performSingularityAscension(afterRaid).updatedGame!!
        assertEquals(1, ascended.singularityLv)
        assertEquals(0, ascended.restartLv)
        assertTrue(ascended.voidEssence > afterRaid.voidEssence)
        assertEquals(0L, ascended.diamonds)
        assertFalse(ascended.hasVipPass)

        val perked = SingularityEngine.upgradeSingularityPerk(ascended, SingularityPerkId.TAP_AMPLIFIER).updatedGame!!
        assertEquals(1, perked.singularityTapPerkLv)
        assertTrue(perked.singularityGlobalMultiplier > ascended.singularityGlobalMultiplier)
        assertEquals(0L, perked.diamonds)
    }

    @Test
    fun regularRestart_preservesMegaUpdate4Progression() = runBlocking {
        val mainExecutor: ExecutorService = Executors.newSingleThreadExecutor()
        val mainDispatcher: CoroutineDispatcher = mainExecutor.asCoroutineDispatcher()
        Dispatchers.setMain(mainDispatcher)
        val context = RuntimeEnvironment.getApplication()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = GameRepository(db.gameStateDao(), db.achievementDao())
            val settingsRepo = SettingsRepository(context)
            repo.saveState(
                GameStateEntity(
                    diem = 500.0,
                    restartCost = 150.0,
                    voidEssence = 9L,
                    singularityLv = 2,
                    singularityTapPerkLv = 1,
                    dronePulseLv = 3,
                    runeBerserkLv = 2,
                    equippedRunes = "berserk",
                    totalDungeonsCleared = 4L,
                    diamonds = 0L
                )
            )
            val vm = GameViewModel(repo, settingsRepo, NoOpAudioController())
            val deadline = System.currentTimeMillis() + 3_000L
            while (vm.uiState.value.isLoading && System.currentTimeMillis() < deadline) {
                delay(25L)
            }

            vm.confirmRestart()
            val after = vm.uiState.value.game
            assertEquals(1, after.restartLv)
            assertEquals(9L, after.voidEssence)
            assertEquals(2, after.singularityLv)
            assertEquals(1, after.singularityTapPerkLv)
            assertEquals(3, after.dronePulseLv)
            assertEquals(2, after.runeBerserkLv)
            assertEquals("berserk", after.equippedRunes)
            assertEquals(4L, after.totalDungeonsCleared)
            assertEquals(0L, after.diamonds)
        } finally {
            db.close()
            Dispatchers.resetMain()
            mainExecutor.shutdownNow()
        }
    }

    @Test
    fun roomMigration_8_to_9_preservesVersion8AndInitializesSingularityDefaults() {
        val context = RuntimeEnvironment.getApplication()
        val dbName = "test_migration_8_to_9_${System.nanoTime()}.db"
        context.deleteDatabase(dbName)

        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(8) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS achievements (
                            id TEXT NOT NULL PRIMARY KEY,
                            isUnlocked INTEGER NOT NULL,
                            isClaimed INTEGER NOT NULL,
                            progressValue REAL NOT NULL,
                            targetValue REAL NOT NULL,
                            unlockedAt INTEGER NOT NULL,
                            claimedAt INTEGER NOT NULL
                        )
                        """.trimIndent()
                    )
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
                            1, 7777.0, 99.0, 500, 2.5, 2.0, 8.0,
                            0.25, 0.125, 1.0, 0.1,
                            2, 150.0, 1, 150.0,
                            0, 75.0, 0, 5.0,
                            0, 3.0, 1.0,
                            0, 3.0, 1.0,
                            1, 1, 3.0, 1.0,
                            0, 15.0, 0, 20.0, 0.25,
                            2, 900.0, 18, 25000.0, 9,
                            0, 120.0, 0, 200.0,
                            0, 90.0, 0, 8.0,
                            0.0, 0, 12.0, 11,
                            0, 0, 0, 0,
                            '', 33, 1,
                            0, 0, 0, 0, 0,
                            0, 4000,
                            2, 2, 2, 120, 0,
                            '', '', 0, 0, 0.0,
                            2500.0, 2, 500.0, 1, 0, 0,
                            0, 0, 0, 0, 0,
                            0, 2, 0, 0,
                            0, 0, '',
                            8, 1, 0, 0, 0,
                            '', 0, 1,
                            0, 0.0, 0, 0.0,
                            0, 0.0, 0, 0.0,
                            0.0, 0,
                            0, 0, 0, 0,
                            0, 0,
                            0, 0, 0, 0, 0,
                            4, 1, 0, 0, 0, 0,
                            0.0, 0.0, 0, 0,
                            1, 0, 0, 0,
                            3, 800.0, 10.0, 1, 40, 2
                        )
                        """.trimIndent()
                    )
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val rawDb = FrameworkSQLiteOpenHelperFactory().create(config).writableDatabase
        rawDb.close()

        val migrated = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(AppDatabase.MIGRATION_8_9)
            .allowMainThreadQueries()
            .build()
        val loaded = runBlocking { migrated.gameStateDao().getGameStateOnce() }
        migrated.close()
        context.deleteDatabase(dbName)

        assertNotNull(loaded)
        assertEquals(7777.0, loaded!!.diem, 0.01)
        assertEquals(33L, loaded.diamonds)
        assertTrue(loaded.hasVipPass)
        assertEquals(4L, loaded.quantumCores)
        assertEquals(0L, loaded.voidEssence)
        assertEquals(0, loaded.singularityLv)
        assertEquals(5, loaded.raidEnergy)
        assertEquals(0, loaded.totalDroneLevel)
        assertEquals("", loaded.equippedRunes)
    }

    @Test
    fun featureHub_openCloseAndHardReset_managesStateCleanly() = runBlocking {
        val mainExecutor: ExecutorService = Executors.newSingleThreadExecutor()
        val mainDispatcher: CoroutineDispatcher = mainExecutor.asCoroutineDispatcher()
        Dispatchers.setMain(mainDispatcher)
        val context = RuntimeEnvironment.getApplication()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = GameRepository(db.gameStateDao(), db.achievementDao())
            val settingsRepo = SettingsRepository(context)
            repo.saveState(GameStateEntity(diem = 100.0))
            val vm = GameViewModel(repo, settingsRepo, NoOpAudioController())
            val deadline = System.currentTimeMillis() + 3_000L
            while (vm.uiState.value.isLoading && System.currentTimeMillis() < deadline) {
                delay(25L)
            }

            assertFalse(vm.uiState.value.showFeatureHub)
            vm.openFeatureHub()
            assertTrue(vm.uiState.value.showFeatureHub)

            vm.closeFeatureHub()
            assertFalse(vm.uiState.value.showFeatureHub)

            vm.openFeatureHub()
            assertTrue(vm.uiState.value.showFeatureHub)
            vm.hardResetGame()
            assertFalse(vm.uiState.value.showFeatureHub)
        } finally {
            db.close()
            Dispatchers.resetMain()
            mainExecutor.shutdownNow()
        }
    }
}

