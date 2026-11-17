package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.audio.NoOpAudioController
import com.example.data.AppDatabase
import com.example.data.GameRepository
import com.example.data.GameSettings
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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [36])
class GameViewModelTest {

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
    fun `point shop exchange cash shop and restart flow work accurately`() = runBlocking {
        repository.saveState(
            GameStateEntity(
                diem = 2500.0,
                dollars = 30.0
            )
        )
        val viewModel = GameViewModel(repository, settingsRepository, NoOpAudioController())
        delay(150)

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(2500.0, viewModel.uiState.value.game.diem, 0.01)

        // Buy Point Power (costs 25 -> x becomes 2, cost becomes 75, lv becomes 1)
        viewModel.buyPointPower()
        assertEquals(2475.0, viewModel.uiState.value.game.diem, 0.01)
        assertEquals(2.0, viewModel.uiState.value.game.pointsPerClick, 0.01)
        assertEquals(1, viewModel.uiState.value.game.pointPowerLv)

        // Exchange 1000 points -> $1
        viewModel.exchange1000Points()
        assertEquals(1475.0, viewModel.uiState.value.game.diem, 0.01)
        assertEquals(31.0, viewModel.uiState.value.game.dollars, 0.01)

        // Buy Auto Click ($25)
        viewModel.buyAutoClick()
        assertTrue(viewModel.uiState.value.game.autoUnlocked)
        assertTrue(viewModel.uiState.value.game.autoEnabled)
        assertEquals(6.0, viewModel.uiState.value.game.dollars, 0.01)

        // Confirm Restart (requires 150 points, we have 1475)
        viewModel.confirmRestart()
        val afterRestart = viewModel.uiState.value.game
        assertEquals(0.0, afterRestart.diem, 0.01)
        assertEquals(0.0, afterRestart.dollars, 0.01)
        assertEquals(1.5, afterRestart.multiplier, 0.01)
        assertEquals(1.5, afterRestart.baseMultiplier, 0.01)
        assertEquals(1, afterRestart.restartLv)
        assertFalse(afterRestart.autoUnlocked)

        // Tap 5 times waiting out 0.25s cooldown -> reaches 5x combo (1.2x combo multiplier)
        repeat(5) {
            viewModel.onTapCore()
            delay(300)
        }
        assertEquals(5, viewModel.uiState.value.comboCount)
        assertEquals(1.2, viewModel.uiState.value.comboMultiplier, 0.01)
        assertEquals(5, viewModel.uiState.value.game.bestCombo)
        assertTrue(viewModel.uiState.value.game.shards >= 1L)

        // Claim restart_1 achievement
        val restartAch = com.example.data.AchievementCatalog.all.first { it.id == "restart_1" }
        val shardsBeforeClaim = viewModel.uiState.value.game.shards
        viewModel.claimAchievement(restartAch)
        assertEquals(shardsBeforeClaim + restartAch.rewardShards, viewModel.uiState.value.game.shards)
        assertTrue(viewModel.uiState.value.game.isAchievementClaimed("restart_1"))

        // Buy permanent Start Points upgrade with shards
        viewModel.buyPermStartPoints()
        assertEquals(1, viewModel.uiState.value.game.permStartPointsLv)
    }

    @Test
    fun `settings persistence and hard reset work properly`() = runBlocking {
        repository.saveState(
            GameStateEntity(
                diem = 5000.0,
                dollars = 50.0,
                restartLv = 2,
                shards = 10L
            )
        )
        val viewModel = GameViewModel(repository, settingsRepository, NoOpAudioController())
        delay(150)

        val updatedSettings = GameSettings(
            bgmEnabled = false,
            sfxEnabled = true,
            bgmVolume = 0.25f,
            sfxVolume = 0.6f,
            hapticsEnabled = false,
            highGraphicsEnabled = false,
            floatingTextEnabled = false
        )
        viewModel.openSettings()
        assertTrue(viewModel.uiState.value.showSettings)

        viewModel.updateSettings(updatedSettings)
        assertEquals(updatedSettings, viewModel.uiState.value.settings)
        assertEquals(updatedSettings, settingsRepository.load())

        // Hard reset clears game data while keeping settings
        viewModel.hardResetGame()
        delay(50)
        assertFalse(viewModel.uiState.value.showSettings)
        assertEquals(0.0, viewModel.uiState.value.game.diem, 0.01)
        assertEquals(0.0, viewModel.uiState.value.game.dollars, 0.01)
        assertEquals(0, viewModel.uiState.value.game.restartLv)
        assertEquals(0L, viewModel.uiState.value.game.shards)
        assertEquals(updatedSettings, viewModel.uiState.value.settings)
    }

    @Test
    fun `premium shop purchases mythic upgrades and restart preservation work`() = runBlocking {
        repository.saveState(GameStateEntity(diem = 500.0))
        val viewModel = GameViewModel(repository, settingsRepository, NoOpAudioController())
        delay(150)

        // Grant real-money VIP Omega pack (+300 diamonds + VIP x3)
        viewModel.grantRealMoneyProduct("tapcore_vip_omega", 1)
        assertTrue(viewModel.uiState.value.game.hasVipPass)
        assertEquals(300L, viewModel.uiState.value.game.diamonds)

        // Buy small points pack (5 diamonds -> +5000+ points)
        val ptsBefore = viewModel.uiState.value.game.diem
        viewModel.buyPointsWithDiamonds(0)
        assertEquals(295L, viewModel.uiState.value.game.diamonds)
        assertTrue(viewModel.uiState.value.game.diem > ptsBefore + 1000.0)

        // Buy dollars pack (10 diamonds -> +$25)
        viewModel.buyDollarsWithDiamonds(0)
        assertEquals(285L, viewModel.uiState.value.game.diamonds)
        assertEquals(25.0, viewModel.uiState.value.game.dollars, 0.01)

        // Buy Mythic Tap God upgrade (20 diamonds -> LV 1, x2 tap)
        viewModel.buyPremTapGod()
        assertEquals(265L, viewModel.uiState.value.game.diamonds)
        assertEquals(1, viewModel.uiState.value.game.premTapGodLv)
        assertEquals(6.0, viewModel.uiState.value.game.effectivePointsPerClick, 0.01) // 1 * 3 (VIP) * 2 (TapGod)

        // Activate Golden Fever (+5 mins, costs 15 diamonds)
        viewModel.buyGoldenFeverWithDiamonds()
        assertEquals(250L, viewModel.uiState.value.game.diamonds)
        assertTrue(viewModel.uiState.value.goldenFeverRemainingSec > 200L)

        // Perform Restart -> Premium items & diamonds must be preserved!
        viewModel.confirmRestart()
        val afterRestart = viewModel.uiState.value.game
        assertTrue(afterRestart.hasVipPass)
        assertEquals(250L, afterRestart.diamonds)
        assertEquals(1, afterRestart.premTapGodLv)
    }
}
