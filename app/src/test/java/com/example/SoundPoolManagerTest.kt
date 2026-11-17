package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.audio.ArcadeAudioSynth
import com.example.audio.GameAudioController
import com.example.audio.GameAudioManager
import com.example.data.AppDatabase
import com.example.data.BetCurrency
import com.example.data.GameRepository
import com.example.data.GameSettings
import com.example.data.SettingsRepository
import com.example.viewmodel.GameTab
import com.example.viewmodel.GameViewModel
import java.io.File
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [36])
class SoundPoolManagerTest {

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
    fun `wav synthesizer writes valid RIFF WAVE PCM header for button click`() {
        val samples = ArcadeAudioSynth.buttonClick()
        assertTrue(samples.isNotEmpty())

        val wavBytes = ArcadeAudioSynth.encodeWavBytes(samples)
        assertEquals(44 + samples.size * 2, wavBytes.size)
        assertEquals("RIFF", String(wavBytes, 0, 4, Charsets.US_ASCII))
        assertEquals("WAVE", String(wavBytes, 8, 4, Charsets.US_ASCII))
        assertEquals("fmt ", String(wavBytes, 12, 4, Charsets.US_ASCII))
        assertEquals("data", String(wavBytes, 36, 4, Charsets.US_ASCII))
    }

    @Test
    fun `soundpool manager caches sfx wav assets and handles lifecycle safely`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = GameAudioManager(context, GameSettings(bgmEnabled = false, sfxEnabled = true))

        // Wait briefly for IO cache preload
        delay(400)

        val cacheDir = File(context.cacheDir, "arcade_sfx_v1")
        assertTrue(cacheDir.exists())
        assertTrue(File(cacheDir, "button_click.wav").length() > 44L)
        assertTrue(File(cacheDir, "crit.wav").length() > 44L)

        manager.onForeground()
        manager.playButtonClick()
        manager.playTap(3)
        manager.playCrit()
        manager.applySettings(GameSettings(bgmEnabled = false, sfxEnabled = false))
        manager.playButtonClick()
        manager.onBackground()
        manager.release()
    }

    @Test
    fun `view model triggers button click sfx on navigation and modal actions`() = runBlocking {
        var buttonClickCount = 0
        val spyAudio = object : GameAudioController {
            override fun applySettings(settings: GameSettings) {}
            override fun onForeground() {}
            override fun onBackground() {}
            override fun playButtonClick() {
                buttonClickCount++
            }
            override fun playTap(comboCount: Int) {}
            override fun playCrit() {}
            override fun playUpgrade() {}
            override fun playError() {}
            override fun playOverdrive() {}
            override fun playAchievement() {}
            override fun playRestart() {}
            override fun playDiamond() {}
            override fun playJackpot() {}
            override fun playSpinTick() {}
            override fun playBossHit() {}
            override fun release() {}
        }

        val vm = GameViewModel(repository, settingsRepository, spyAudio)
        delay(120)

        vm.selectTab(GameTab.POINT_SHOP)
        vm.selectTab(GameTab.POINT_SHOP) // same tab shouldn't fire duplicate click
        vm.selectTab(GameTab.TAP)
        vm.openSettings()
        vm.closeSettings()
        vm.openDailyRewards()
        vm.closeDailyRewards()
        vm.openSeasonPass()
        vm.closeSeasonPass()
        vm.openCasino()
        vm.selectCasinoGame(1)
        vm.selectCasinoCurrency(BetCurrency.DOLLARS)
        vm.selectCasinoBetTier(2)
        vm.closeCasino()
        vm.openBossAndRelics()
        vm.closeBossAndRelics()

        assertEquals(15, buttonClickCount)
    }
}
