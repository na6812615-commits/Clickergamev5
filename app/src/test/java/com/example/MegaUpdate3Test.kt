package com.example

import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import com.example.data.AlchemyRecipeId
import com.example.data.AppDatabase
import com.example.data.ArenaBossCatalog
import com.example.data.EmpireDistrictId
import com.example.data.GameStateEntity
import com.example.data.InfinityTowerCatalog
import com.example.data.QuantumGearId
import com.example.viewmodel.CosmicEmpireEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class MegaUpdate3Test {

    @Test
    fun cyberEmpire_buildAndCollectTreasury_neverGrantsDiamonds() {
        val start = GameStateEntity(
            dollars = 200.0,
            techChips = 10L,
            shards = 5L,
            diamonds = 0L,
            empirePendingPoints = 1200.0,
            empirePendingDollars = 15.0,
            empirePendingShards = 2L,
            empirePendingCores = 1L
        )
        val builtSolar = CosmicEmpireEngine.upgradeEmpireDistrict(start, EmpireDistrictId.SOLAR_MATRIX)
        assertNotNull(builtSolar.updatedGame)
        assertEquals(1, builtSolar.updatedGame!!.empireSolarLv)
        assertTrue(builtSolar.updatedGame!!.empireSolarPtsPerSec > 0.0)

        val builtCitadel = CosmicEmpireEngine.upgradeEmpireDistrict(builtSolar.updatedGame!!, EmpireDistrictId.NEXUS_CITADEL)
        assertNotNull(builtCitadel.updatedGame)
        assertEquals(1, builtCitadel.updatedGame!!.empireCitadelLv)
        assertEquals(3, builtCitadel.updatedGame!!.fleetTotalPower)

        val collected = CosmicEmpireEngine.collectEmpireTreasury(builtCitadel.updatedGame!!)
        assertNotNull(collected.updatedGame)
        val g = collected.updatedGame!!
        assertEquals(1200.0, g.diem, 0.01)
        assertEquals(1L, g.quantumCores)
        assertEquals(0.0, g.empirePendingPoints, 0.01)
        assertEquals(0L, g.empirePendingCores)
        assertEquals(0L, g.diamonds)
        assertFalse(g.hasVipPass)
    }

    @Test
    fun quantumAlchemy_and_gearForging_workCorrectlyWithoutPremiumLeaks() {
        val start = GameStateEntity(
            diem = 20_000.0,
            dollars = 150.0,
            shards = 10L,
            techChips = 10L,
            diamonds = 0L
        )
        val step1 = CosmicEmpireEngine.transmuteRecipe(start, AlchemyRecipeId.POINTS_TO_CHIPS).updatedGame!!
        assertEquals(12L, step1.techChips)

        val step2 = CosmicEmpireEngine.transmuteRecipe(step1, AlchemyRecipeId.SYNTH_QUANTUM_CORE).updatedGame!!
        assertEquals(1L, step2.quantumCores)

        val step3 = CosmicEmpireEngine.transmuteRecipe(step2, AlchemyRecipeId.SHARDS_TO_QUANTUM_CORE).updatedGame!!
        assertEquals(3L, step3.quantumCores)

        // Forge Cyber Blade LV.1 (costs 1 core + 2 chips)
        val forged = CosmicEmpireEngine.upgradeQuantumGear(step3, QuantumGearId.CYBER_BLADE).updatedGame!!
        assertEquals(1, forged.gearBladeLv)
        assertEquals(2L, forged.quantumCores)
        assertTrue(forged.effectivePointsPerClick > start.effectivePointsPerClick)

        // Grant extra core and open Royal Crate (3 cores)
        val withExtraCore = forged.copy(quantumCores = 3L)
        val crate = CosmicEmpireEngine.transmuteRecipe(withExtraCore, AlchemyRecipeId.QUANTUM_ROYAL_CRATE).updatedGame!!
        assertEquals(0L, crate.quantumCores)
        assertTrue(crate.diem > withExtraCore.diem)
        assertEquals(0L, crate.diamonds)
        assertFalse(crate.hasVipPass)
        assertEquals(0L, crate.premGoldenFeverEndAt)
    }

    @Test
    fun cosmicArena_combatOmegaAndShield_advanceWavesAndGrantCores() {
        val start = GameStateEntity(
            pointsPerClick = 200.0,
            dollars = 100.0,
            arenaWave = 1,
            arenaEnemyHp = ArenaBossCatalog.maxHpForWave(1),
            arenaFury = 0.0,
            diamonds = 0L
        )
        val shielded = CosmicEmpireEngine.activateArenaShield(start).updatedGame!!
        assertEquals(5, shielded.arenaShieldCharges)
        assertEquals(70.0, shielded.dollars, 0.01)

        // With 200 base PPC * 2.5 crit * 1.75 shield = 875 DMG -> clears Wave 1 (450 HP)
        val attack = CosmicEmpireEngine.attackArena(
            game = shielded,
            rollIsCrit = false,
            isTapSurge = false,
            isOverdrive = false
        )
        assertTrue(attack.waveCleared)
        val afterWave1 = attack.updatedGame!!
        assertEquals(2, afterWave1.arenaWave)
        assertTrue(afterWave1.quantumCores >= 1L)
        assertTrue(afterWave1.arenaGlory > 0L)
        assertEquals(1.04, afterWave1.arenaTapMultiplier, 0.001)

        // Omega strike requires 100 Fury
        val notReady = CosmicEmpireEngine.castArenaOmega(afterWave1)
        assertNull(notReady.updatedGame)

        val furyFull = afterWave1.copy(arenaFury = 100.0)
        val omega = CosmicEmpireEngine.castArenaOmega(furyFull)
        assertNotNull(omega.updatedGame)
        assertEquals(0.0, omega.updatedGame!!.arenaFury, 0.01)
        assertEquals(0L, omega.updatedGame!!.diamonds)
    }

    @Test
    fun infinityTower_sequentialClaim_and_globalMultiplier() {
        val floor1 = InfinityTowerCatalog.floors[0]
        val floor2 = InfinityTowerCatalog.floors[1]

        val lowGame = GameStateEntity(lifetimePoints = 100.0, bestCombo = 20)
        val fail1 = CosmicEmpireEngine.claimTowerFloor(lowGame, floor1)
        assertNull(fail1.updatedGame)

        val readyGame = GameStateEntity(lifetimePoints = 10_000.0, bestCombo = 20, diamonds = 0L)
        // Cannot skip floor 1 to claim floor 2
        val skipAttempt = CosmicEmpireEngine.claimTowerFloor(readyGame, floor2)
        assertNull(skipAttempt.updatedGame)

        val claim1 = CosmicEmpireEngine.claimTowerFloor(readyGame, floor1).updatedGame!!
        assertEquals(1, claim1.towerHighestFloorClaimed)
        assertEquals(1.15, claim1.towerGlobalMultiplier, 0.001)

        val claim2 = CosmicEmpireEngine.claimTowerFloor(claim1, floor2).updatedGame!!
        assertEquals(2, claim2.towerHighestFloorClaimed)
        assertEquals(1.30, claim2.towerGlobalMultiplier, 0.001)
        assertEquals(0L, claim2.diamonds)
        assertFalse(claim2.hasVipPass)
    }

    @Test
    fun roomMigration_6_to_7_preservesVersion6DataAndInitializesEmpireArenaFields() {
        val context = RuntimeEnvironment.getApplication()
        val dbName = "test_migration_6_to_7_${System.nanoTime()}.db"
        context.deleteDatabase(dbName)

        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(6) {
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
                            skillEmpReadyAt, skillHyperReadyAt, skillHyperEndAt, skillRainReadyAt, totalSkillsCast
                        ) VALUES (
                            1, 12345.0, 250.0, 120, 3.0, 2.0, 10.0,
                            0.25, 0.125, 1.0, 0.1,
                            3, 300.0, 2, 200.0,
                            0, 75.0, 0, 5.0,
                            0, 3.0, 1.0,
                            0, 3.0, 1.0,
                            1, 1, 5.0, 0.75,
                            0, 15.0, 0, 20.0, 0.25,
                            2, 900.0, 22, 45000.0, 15,
                            1, 150.0, 0, 200.0,
                            0, 90.0, 1, 16.0,
                            10.0, 0, 12.0, 14,
                            0, 0, 0, 0,
                            '', 55, 1,
                            0, 0, 0, 0, 0,
                            0, 3000,
                            4, 60, 4, 400, 1,
                            '1', '', 5, 3, 200.0,
                            3500.0, 6, 1100.0, 5, 2, 1,
                            0, 1, 0, 0, 0,
                            0, 60, 18, 4,
                            2, 1, '',
                            9, 2, 1, 1, 0,
                            '', 0, 3,
                            4, 15.0, 0, 0.0,
                            0, 0.0, 0, 0.0,
                            42.5, 4,
                            2, 1, 1, 0,
                            0, 0,
                            0, 0, 0, 0, 7
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
            .addMigrations(AppDatabase.MIGRATION_5_6, AppDatabase.MIGRATION_6_7, AppDatabase.MIGRATION_7_8, AppDatabase.MIGRATION_8_9)
            .allowMainThreadQueries()
            .build()
        val loaded = runBlocking { migratedRoomDb.gameStateDao().getGameStateOnce() }
        migratedRoomDb.close()
        context.deleteDatabase(dbName)

        assertNotNull(loaded)
        assertEquals(12345.0, loaded!!.diem, 0.01)
        assertEquals(55L, loaded.diamonds)
        assertTrue(loaded.hasVipPass)
        assertEquals(9L, loaded.techChips)
        assertEquals(0L, loaded.quantumCores)
        assertEquals(0, loaded.empireSolarLv)
        assertEquals(0, loaded.gearBladeLv)
        assertEquals(1, loaded.arenaWave)
        assertEquals(450.0, loaded.arenaEnemyHp, 0.01)
        assertEquals(0, loaded.towerHighestFloorClaimed)
    }
}
