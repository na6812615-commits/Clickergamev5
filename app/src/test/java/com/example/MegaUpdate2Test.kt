package com.example

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.audio.NoOpAudioController
import com.example.data.AchievementCatalog
import com.example.data.AppDatabase
import com.example.data.CyberSkillId
import com.example.data.CyberStockTicker
import com.example.data.ExpeditionCatalog
import com.example.data.FleetShipType
import com.example.data.GameRepository
import com.example.data.GameStateEntity
import com.example.data.SettingsRepository
import com.example.data.TechNodeId
import com.example.viewmodel.GalaxyEngine
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.StockMarketSnapshot
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
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [36])
class MegaUpdate2Test {

    private lateinit var db: AppDatabase
    private lateinit var repository: GameRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var mainExecutor: ExecutorService
    private lateinit var mainDispatcher: CoroutineDispatcher

    @Before
    fun setup() {
        mainExecutor = Executors.newSingleThreadExecutor()
        mainDispatcher = mainExecutor.asCoroutineDispatcher()
        Dispatchers.setMain(mainDispatcher)

        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = GameRepository(db.gameStateDao())
        settingsRepository = SettingsRepository(context)
    }

    @After
    fun teardown() {
        db.close()
        Dispatchers.resetMain()
        mainExecutor.shutdownNow()
    }

    @Test
    fun `fleet upgrades and space expeditions work cleanly without diamond leaks`() {
        val base = GameStateEntity(dollars = 1000.0, shards = 20L, diamonds = 0L)

        val scoutUpgrade = GalaxyEngine.upgradeFleetShip(base, FleetShipType.SCOUT)
        assertFalse(scoutUpgrade.isError)
        val withScout = scoutUpgrade.updatedGame!!
        assertEquals(1, withScout.fleetScoutLv)
        assertEquals(1, withScout.fleetTotalPower)
        assertTrue(withScout.fleetPointsPerSec > 0.0)
        assertEquals(0L, withScout.diamonds)

        val beltZone = ExpeditionCatalog.zones.first() // minFleetPower = 1
        val startOutcome = GalaxyEngine.startExpedition(withScout, beltZone, nowMs = 10_000L)
        assertFalse(startOutcome.isError)
        val launched = startOutcome.updatedGame!!
        assertEquals(beltZone.id, launched.activeExpeditionId)

        // Claim too early is rejected
        val earlyClaim = GalaxyEngine.claimExpedition(launched, nowMs = 20_000L)
        assertTrue(earlyClaim.isError)
        assertNull(earlyClaim.updatedGame)

        // Claim after completion awards points, dollars, shards, techChips, seasonXp, and ZERO diamonds
        val claimOutcome = GalaxyEngine.claimExpedition(launched, nowMs = 10_000L + beltZone.durationSec * 1000L + 500L)
        assertFalse(claimOutcome.isError)
        val claimed = claimOutcome.updatedGame!!
        assertEquals("", claimed.activeExpeditionId)
        assertEquals(1L, claimed.totalExpeditionsCompleted)
        assertTrue(claimed.diem > launched.diem)
        assertTrue(claimed.dollars > launched.dollars)
        assertTrue(claimed.shards > launched.shards)
        assertTrue(claimed.techChips >= beltZone.baseTechChips)
        assertTrue(claimed.seasonXp > 0L)
        assertEquals(0L, claimed.diamonds)
        assertEquals(0L, claimed.premGoldenFeverEndAt)
    }

    @Test
    fun `cyber stock exchange tracks prices avg cost profit and respects bounds`() {
        var market = StockMarketSnapshot()
        repeat(30) {
            market = GalaxyEngine.tickMarket(market)
            CyberStockTicker.entries.forEach { ticker ->
                val p = market.prices[ticker]!!
                assertTrue(p >= ticker.minPrice)
                assertTrue(p <= ticker.maxPrice)
                assertEquals(20.coerceAtMost(17 + it + 1), market.history[ticker]!!.size)
            }
        }

        val initial = GameStateEntity(dollars = 1000.0, diamonds = 0L)
        val buy1 = GalaxyEngine.buyStock(initial, CyberStockTicker.CORE, unitPrice = 20.0, quantity = 10L)
        assertFalse(buy1.isError)
        val afterBuy1 = buy1.updatedGame!!
        assertEquals(10L, afterBuy1.stockCoreShares)
        assertEquals(1L, afterBuy1.stockTradesCount)
        assertTrue(afterBuy1.dollars < 800.0)
        assertEquals(0L, afterBuy1.diamonds)

        val sell = GalaxyEngine.sellStock(afterBuy1, CyberStockTicker.CORE, unitPrice = 35.0, quantity = 10L)
        assertFalse(sell.isError)
        val afterSell = sell.updatedGame!!
        assertEquals(0L, afterSell.stockCoreShares)
        assertEquals(0.0, afterSell.stockCoreAvgCost, 0.001)
        assertTrue(afterSell.stockTotalProfit > 100.0)
        assertTrue(afterSell.seasonXp > 0L)
        assertEquals(0L, afterSell.diamonds)
    }

    @Test
    fun `tech tree upgrades chip synthesis boss chip drops and active cyber skills work with restart preservation`() = runBlocking {
        repository.saveState(
            GameStateEntity(
                diem = 500_000.0,
                dollars = 500.0,
                shards = 15L,
                diamonds = 42L,
                techChips = 4L,
                bossLevel = 1,
                bossCurrentHp = 50.0,
                pointsPerClick = 100.0,
                fleetScoutLv = 2,
                fleetHarvesterLv = 1,
                passiveLv = 2,
                autoUnlocked = true,
                autoEnabled = true
            )
        )
        val vm = GameViewModel(repository, settingsRepository, NoOpAudioController())
        delay(150)

        vm.synthesizeTechChips(1)
        assertEquals(7L, vm.uiState.value.game.techChips)
        assertEquals(420.0, vm.uiState.value.game.dollars, 0.01)

        val tapBeforeTech = vm.uiState.value.game.effectivePointsPerClick
        vm.upgradeTechNode(TechNodeId.OVERCLOCK)
        assertEquals(1, vm.uiState.value.game.techOverclockLv)
        assertEquals(5L, vm.uiState.value.game.techChips)
        assertTrue(vm.uiState.value.game.effectivePointsPerClick > tapBeforeTech)

        val chipsBeforeBoss = vm.uiState.value.game.techChips
        vm.attackBoss()
        assertEquals(2, vm.uiState.value.game.bossLevel)
        assertTrue(vm.uiState.value.game.techChips > chipsBeforeBoss)
        assertEquals(42L, vm.uiState.value.game.diamonds)

        val diemBeforeEmp = vm.uiState.value.game.diem
        vm.castSkill(CyberSkillId.EMP_BURST)
        assertTrue(vm.uiState.value.game.diem > diemBeforeEmp)
        assertTrue(vm.uiState.value.empCooldownSec > 0L)

        val totalCastsAfterEmp = vm.uiState.value.game.totalSkillsCast
        vm.castSkill(CyberSkillId.EMP_BURST)
        assertEquals(totalCastsAfterEmp, vm.uiState.value.game.totalSkillsCast)

        vm.castSkill(CyberSkillId.HYPER_FOCUS)
        assertTrue(vm.uiState.value.hyperActiveRemainingSec > 0L)

        val dollarsBeforeRain = vm.uiState.value.game.dollars
        vm.castSkill(CyberSkillId.QUANTUM_RAIN)
        assertTrue(vm.uiState.value.game.dollars > dollarsBeforeRain)
        assertEquals(3L, vm.uiState.value.game.totalSkillsCast)

        val megaState = vm.uiState.value.game.copy(
            fleetScoutLv = 10,
            fleetCruiserLv = 5,
            totalExpeditionsCompleted = 10L,
            stockTradesCount = 10L,
            stockTotalProfit = 1000.0,
            techOverclockLv = 10,
            techConverterLv = 10,
            totalSkillsCast = 20L,
            bossesDefeated = 10L,
            diamonds = 0L,
            hasVipPass = false,
            premGoldenFeverEndAt = 0L
        )
        AchievementCatalog.all.filter {
            it.id.startsWith("fleet_") || it.id.startsWith("expedition_") ||
                it.id.startsWith("stock_") || it.id.startsWith("tech_") ||
                it.id.startsWith("skill_") || it.id.startsWith("boss_slayer")
        }.forEach { ach ->
            assertTrue("Expected ${ach.id} to be claimable", ach.progressSelector(megaState) >= ach.targetValue)
        }

        val chipsBeforeRestart = vm.uiState.value.game.techChips
        val scoutBeforeRestart = vm.uiState.value.game.fleetScoutLv
        val techBeforeRestart = vm.uiState.value.game.techOverclockLv
        vm.confirmRestart()
        val afterRestart = vm.uiState.value.game
        assertEquals(1, afterRestart.restartLv)
        assertEquals(chipsBeforeRestart, afterRestart.techChips)
        assertEquals(scoutBeforeRestart, afterRestart.fleetScoutLv)
        assertEquals(techBeforeRestart, afterRestart.techOverclockLv)
        assertEquals(3L, afterRestart.totalSkillsCast)
        assertEquals(42L, afterRestart.diamonds)
        assertFalse(afterRestart.hasVipPass)
        assertEquals(0L, afterRestart.premGoldenFeverEndAt)
    }

    @Test
    fun `room migration 5 to 6 preserves v5 data and initializes galaxy 2 defaults`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "migration_5_6_test.db"
        context.deleteDatabase(dbName)

        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(5) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS game_state (
                            id INTEGER NOT NULL PRIMARY KEY,
                            diem REAL NOT NULL, dollars REAL NOT NULL, clicks INTEGER NOT NULL,
                            multiplier REAL NOT NULL, baseMultiplier REAL NOT NULL, pointsPerClick REAL NOT NULL,
                            clickCooldownSec REAL NOT NULL, cooldownStep REAL NOT NULL, discountFactor REAL NOT NULL,
                            discountStep REAL NOT NULL, pointPowerLv INTEGER NOT NULL, pointPowerCost REAL NOT NULL,
                            multiplierLv INTEGER NOT NULL, multiplierCost REAL NOT NULL, cooldownLv INTEGER NOT NULL,
                            cooldownCost REAL NOT NULL, discountLv INTEGER NOT NULL, discountCost REAL NOT NULL,
                            menuSpeedLv INTEGER NOT NULL, menuSpeedCost REAL NOT NULL, menuTransitionSec REAL NOT NULL,
                            toastSpeedLv INTEGER NOT NULL, toastSpeedCost REAL NOT NULL, toastDurationSec REAL NOT NULL,
                            autoUnlocked INTEGER NOT NULL, autoEnabled INTEGER NOT NULL, autoPoints REAL NOT NULL,
                            autoCooldownSec REAL NOT NULL, autoPowerLv INTEGER NOT NULL, autoPowerCost REAL NOT NULL,
                            autoSpeedLv INTEGER NOT NULL, autoSpeedCost REAL NOT NULL, autoSpeedStep REAL NOT NULL,
                            restartLv INTEGER NOT NULL, restartCost REAL NOT NULL, bestCombo INTEGER NOT NULL,
                            lifetimePoints REAL NOT NULL, totalCrits INTEGER NOT NULL, critChanceLv INTEGER NOT NULL,
                            critChanceCost REAL NOT NULL, critMultLv INTEGER NOT NULL, critMultCost REAL NOT NULL,
                            comboWindowLv INTEGER NOT NULL, comboWindowCost REAL NOT NULL, passiveLv INTEGER NOT NULL,
                            passiveCost REAL NOT NULL, overdriveCharge REAL NOT NULL, overdriveLv INTEGER NOT NULL,
                            overdriveCost REAL NOT NULL, shards INTEGER NOT NULL, permStartPointsLv INTEGER NOT NULL,
                            permGlobalMultLv INTEGER NOT NULL, permCritLv INTEGER NOT NULL, permDiscountLv INTEGER NOT NULL,
                            claimedAchievements TEXT NOT NULL, diamonds INTEGER NOT NULL, hasVipPass INTEGER NOT NULL,
                            premTapGodLv INTEGER NOT NULL, premMegaCritLv INTEGER NOT NULL, premAutoGodLv INTEGER NOT NULL,
                            premZeroCdLv INTEGER NOT NULL, premWealthMagnetLv INTEGER NOT NULL, premGoldenFeverEndAt INTEGER NOT NULL,
                            updatedAt INTEGER NOT NULL
                        )
                        """.trimIndent()
                    )
                    AppDatabase.MIGRATION_4_5.migrate(db)
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
                            dailyQuestGambles, dailyQuestBossKills, claimedDailyQuests
                        ) VALUES (
                            1, 7777.0, 125.0, 50, 2.5, 2.0, 8.0,
                            0.25, 0.125, 1.0, 0.1,
                            3, 300.0, 2, 200.0,
                            0, 75.0, 0, 5.0,
                            0, 3.0, 1.0,
                            0, 3.0, 1.0,
                            1, 1, 5.0, 0.75,
                            0, 15.0, 0, 20.0, 0.25,
                            2, 900.0, 18, 25000.0, 10,
                            1, 150.0, 0, 200.0,
                            0, 90.0, 1, 16.0,
                            10.0, 0, 12.0, 11,
                            0, 0, 0, 0,
                            '', 33, 0,
                            0, 0, 0, 0, 0,
                            0, 2000,
                            3, 50, 3, 250, 1,
                            '1', '', 4, 2, 100.0,
                            3200.0, 4, 800.0, 3, 2, 1,
                            0, 1, 0, 0, 0,
                            0, 50, 12, 2,
                            1, 0, ''
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
            .addMigrations(AppDatabase.MIGRATION_4_5, AppDatabase.MIGRATION_5_6, AppDatabase.MIGRATION_6_7, AppDatabase.MIGRATION_7_8, AppDatabase.MIGRATION_8_9)
            .allowMainThreadQueries()
            .build()
        val loaded = runBlocking { migratedRoomDb.gameStateDao().getGameStateOnce() }
        migratedRoomDb.close()
        context.deleteDatabase(dbName)

        assertNotNull(loaded)
        assertEquals(7777.0, loaded!!.diem, 0.01)
        assertEquals(4, loaded.bossLevel)
        assertEquals(33L, loaded.diamonds)
        assertEquals(0L, loaded.techChips)
        assertEquals(0, loaded.fleetScoutLv)
        assertEquals("", loaded.activeExpeditionId)
        assertEquals(0L, loaded.stockCoreShares)
        assertEquals(0, loaded.techOverclockLv)
        assertEquals(0L, loaded.totalSkillsCast)
    }
}
