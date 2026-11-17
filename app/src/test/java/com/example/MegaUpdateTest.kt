package com.example

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.audio.NoOpAudioController
import com.example.data.AppDatabase
import com.example.data.BetCurrency
import com.example.data.BoosterType
import com.example.data.BossCatalog
import com.example.data.DailyQuestCatalog
import com.example.data.DailyRewardCatalog
import com.example.data.DayClock
import com.example.data.GameRepository
import com.example.data.GameStateEntity
import com.example.data.RelicId
import com.example.data.SeasonPassCatalog
import com.example.data.SettingsRepository
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.MegaUpdateEngine
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
class MegaUpdateTest {

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
    fun `daily rewards streak VIP bonus and zero diamond leak work accurately`() {
        val day100 = 100L
        val base = GameStateEntity(hasVipPass = true, diamonds = 0L)
        assertTrue(DailyRewardCatalog.canClaimToday(base, day100))

        val claim1 = MegaUpdateEngine.claimDailyReward(base, today = day100)
        assertFalse(claim1.isError)
        val afterDay1 = claim1.updatedGame!!
        assertEquals(1, afterDay1.dailyStreak)
        assertEquals(day100, afterDay1.lastDailyClaimDay)
        assertEquals(0L, afterDay1.diamonds) // ZERO free diamonds!
        assertEquals(0L, afterDay1.premGoldenFeverEndAt) // ZERO free Golden Fever!
        assertEquals(50.0, afterDay1.dollars, 0.01) // $25 * 2 (VIP)
        assertEquals(2L, afterDay1.shards) // 1 * 2 (VIP)
        assertFalse(DailyRewardCatalog.canClaimToday(afterDay1, day100))

        // Same day attempt blocked
        val repeatSameDay = MegaUpdateEngine.claimDailyReward(afterDay1, today = day100)
        assertTrue(repeatSameDay.isError)
        assertNull(repeatSameDay.updatedGame)

        // Consecutive next day advances streak to 2
        val claim2 = MegaUpdateEngine.claimDailyReward(afterDay1, today = day100 + 1L)
        assertFalse(claim2.isError)
        assertEquals(2, claim2.updatedGame!!.dailyStreak)
        assertEquals(0L, claim2.updatedGame.diamonds)
        assertEquals(50.0 + 90.0, claim2.updatedGame.dollars, 0.01) // +$45 * 2 (VIP)
        assertEquals(2L + 4L, claim2.updatedGame.shards) // +2 * 2 (VIP)
    }

    @Test
    fun `season pass progression dollars shards elite unlock and claim all work without free diamonds`() = runBlocking {
        repository.saveState(
            GameStateEntity(
                dollars = 600.0,
                shards = 20L,
                diamonds = 0L,
                seasonXp = 500L
            )
        )
        val vm = GameViewModel(repository, settingsRepository, NoOpAudioController())
        delay(150)

        val expectedLv = SeasonPassCatalog.levelForXp(500L)
        assertTrue(expectedLv >= 3)
        assertTrue(vm.uiState.value.unclaimedSeasonRewardsCount >= expectedLv)

        vm.unlockEliteSeasonPass()
        assertTrue(vm.uiState.value.game.seasonPassPremium)
        assertEquals(150.0, vm.uiState.value.game.dollars, 0.01) // 600 - 450
        assertEquals(10L, vm.uiState.value.game.shards) // 20 - 10
        assertEquals(0L, vm.uiState.value.game.diamonds)
        assertEquals(expectedLv * 2, vm.uiState.value.unclaimedSeasonRewardsCount)

        vm.claimAllSeasonRewards()
        assertEquals(0, vm.uiState.value.unclaimedSeasonRewardsCount)
        assertTrue(vm.uiState.value.game.isSeasonFreeClaimed(1))
        assertTrue(vm.uiState.value.game.isSeasonEliteClaimed(1))
        // Claiming all Season Pass tiers must NEVER grant diamonds or Golden Fever
        assertEquals(0L, vm.uiState.value.game.diamonds)
        assertEquals(0L, vm.uiState.value.game.premGoldenFeverEndAt)
    }

    @Test
    fun `casino games boss raid relic forge shard boosters daily quests and restart preservation work without diamond leaks`() = runBlocking {
        val today = DayClock.localEpochDay()
        repository.saveState(
            GameStateEntity(
                diem = 10_000.0,
                dollars = 500.0,
                diamonds = 50L,
                shards = 20L,
                pointsPerClick = 100.0,
                bossLevel = 1,
                bossCurrentHp = 150.0,
                dailyQuestDay = today,
                dailyQuestTaps = 100
            )
        )
        val vm = GameViewModel(repository, settingsRepository, NoOpAudioController())
        delay(150)

        // Casino Lucky Wheel & Slot 777 & Quantum Dice & Minefield
        vm.selectCasinoCurrency(BetCurrency.DOLLARS)
        vm.selectCasinoBetTier(0) // $10
        repeat(5) {
            vm.spinLuckyWheel()
            vm.playSlot777()
            vm.playQuantumDice(2)
        }
        assertEquals(15L, vm.uiState.value.game.gambleSpins)
        assertEquals(15, vm.uiState.value.game.dailyQuestGambles)
        assertEquals(50L, vm.uiState.value.game.diamonds) // ZERO diamond payout from Casino!

        vm.startMinefieldGame()
        assertNotNull(vm.uiState.value.minefield)
        val safeIdx = (0 until 9).first { it !in vm.uiState.value.minefield!!.bombIndices }
        vm.revealMinefieldTile(safeIdx)
        assertTrue(vm.uiState.value.minefield!!.revealedSafe.contains(safeIdx))
        vm.cashOutMinefield()
        assertFalse(vm.uiState.value.minefield!!.isActive)
        assertEquals(50L, vm.uiState.value.game.diamonds)

        // Boss Raid kill (Boss HP 150, effectivePointsPerClick 100+ -> 1 or 2 hits defeats Lv 1)
        val shardsBeforeBoss = vm.uiState.value.game.shards
        vm.attackBoss()
        if (vm.uiState.value.game.bossLevel == 1) {
            vm.attackBoss()
        }
        assertEquals(2, vm.uiState.value.game.bossLevel)
        assertEquals(1L, vm.uiState.value.game.bossesDefeated)
        assertEquals(BossCatalog.maxHpForLevel(2), vm.uiState.value.game.bossCurrentHp, 0.01)
        assertTrue(vm.uiState.value.game.shards > shardsBeforeBoss)
        assertEquals(50L, vm.uiState.value.game.diamonds) // Boss drops shards & $, never diamonds!

        // Relic Forge upgrade
        val ppcBeforeRelic = vm.uiState.value.game.effectivePointsPerClick
        vm.upgradeRelic(RelicId.TITAN)
        assertEquals(1, vm.uiState.value.game.relicTapLv)
        assertTrue(vm.uiState.value.game.effectivePointsPerClick > ppcBeforeRelic)

        // Activate Mega Booster (uses shards ✦, never diamonds!)
        val shardsBeforeBooster = vm.uiState.value.game.shards
        vm.activateBooster(BoosterType.TAP_SURGE)
        assertEquals(shardsBeforeBooster - BoosterType.TAP_SURGE.shardCost, vm.uiState.value.game.shards)
        assertEquals(50L, vm.uiState.value.game.diamonds)
        assertTrue(vm.uiState.value.tapSurgeRemainingSec > 20L)

        // Claim completed Daily Quest (100 taps) -> awards $, shards, XP, zero diamonds
        val tapQuest = DailyQuestCatalog.all.first { it.id == "dq_taps_100" }
        val xpBeforeQuest = vm.uiState.value.game.seasonXp
        vm.claimDailyQuest(tapQuest)
        assertTrue(vm.uiState.value.game.isDailyQuestClaimed("dq_taps_100"))
        assertTrue(vm.uiState.value.game.seasonXp > xpBeforeQuest)
        assertEquals(50L, vm.uiState.value.game.diamonds)

        // Opening Mystery Crate deducts 30 diamonds and never grants free diamonds back
        vm.openMysteryCrateWithDiamonds()
        assertEquals(20L, vm.uiState.value.game.diamonds)
        assertEquals(0L, vm.uiState.value.game.premGoldenFeverEndAt)

        // Confirm Restart preserves Bosses, Relics, Season Pass, Casino stats, Daily Rewards!
        val bossLvBeforeRestart = vm.uiState.value.game.bossLevel
        val seasonXpBeforeRestart = vm.uiState.value.game.seasonXp
        val gamblesBeforeRestart = vm.uiState.value.game.gambleSpins
        vm.confirmRestart()
        val afterRestart = viewModelConfirmRestartCheck(vm)
        assertEquals(1, afterRestart.restartLv)
        assertEquals(bossLvBeforeRestart, afterRestart.bossLevel)
        assertEquals(1, afterRestart.relicTapLv)
        assertEquals(seasonXpBeforeRestart, afterRestart.seasonXp)
        assertEquals(gamblesBeforeRestart, afterRestart.gambleSpins)
        assertEquals(20L, afterRestart.diamonds)
    }

    private fun viewModelConfirmRestartCheck(vm: GameViewModel): GameStateEntity = vm.uiState.value.game

    @Test
    fun `room migration 4 to 5 preserves v4 columns and initializes v5 defaults`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "migration_4_5_test.db"
        context.deleteDatabase(dbName)

        // Create a minimal v4 database manually and run MIGRATION_4_5
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(4) {
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
                            updatedAt INTEGER NOT NULL
                        )
                        """.trimIndent()
                    )
                    db.execSQL(
                        """
                        INSERT INTO game_state VALUES (
                            1, 4321.0, 77.0, 99, 2.0, 1.5, 4.0, 0.25, 0.125, 1.0, 0.1,
                            2, 225.0, 1, 150.0, 0, 75.0, 0, 5.0, 0, 3.0, 1.0, 0, 3.0, 1.0,
                            0, 0, 1.0, 1.0, 0, 15.0, 0, 20.0, 0.25, 1, 450.0, 12, 9999.0, 5,
                            0, 120.0, 0, 200.0, 0, 90.0, 0, 8.0, 0.0, 0, 12.0, 7, 0, 0, 0, 0,
                            '', 88, 1, 0, 0, 0, 0, 0, 0, 1000
                        )
                        """.trimIndent()
                    )
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val helper = FrameworkSQLiteOpenHelperFactory().create(config)
        val rawDb = helper.writableDatabase
        rawDb.close()

        val migratedRoomDb = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(AppDatabase.MIGRATION_4_5, AppDatabase.MIGRATION_5_6, AppDatabase.MIGRATION_6_7, AppDatabase.MIGRATION_7_8, AppDatabase.MIGRATION_8_9)
            .allowMainThreadQueries()
            .build()
        val loaded = runBlocking { migratedRoomDb.gameStateDao().getGameStateOnce() }
        migratedRoomDb.close()
        context.deleteDatabase(dbName)

        assertNotNull(loaded)
        assertEquals(4321.0, loaded!!.diem, 0.01)
        assertEquals(88L, loaded.diamonds)
        assertTrue(loaded.hasVipPass)
        assertEquals(1, loaded.bossLevel)
        assertEquals(250.0, loaded.bossCurrentHp, 0.01)
        assertEquals(2500.0, loaded.gambleJackpotPool, 0.01)
    }
}
