package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.audio.GameAudioManager
import com.example.billing.PlayBillingManager
import com.example.data.AppDatabase
import com.example.data.GameRepository
import com.example.data.SettingsRepository
import com.example.ui.TapCoreApp
import com.example.ui.theme.TapCoreTheme
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(applicationContext)
    }

    private val audioManager: GameAudioManager by lazy {
        GameAudioManager(applicationContext, settingsRepository.load())
    }

    private val billingManager: PlayBillingManager by lazy {
        PlayBillingManager(
            context = applicationContext,
            onPurchaseGranted = { productId, quantity ->
                runOnUiThread {
                    gameViewModel.grantRealMoneyProduct(productId, quantity)
                }
            },
            onBillingMessage = { message, isError ->
                runOnUiThread {
                    gameViewModel.showBillingMessage(message, isError)
                }
            }
        )
    }

    private val gameViewModel: GameViewModel by viewModels {
        val db = AppDatabase.getInstance(applicationContext)
        GameViewModel.Factory(
            repository = GameRepository(db.gameStateDao(), db.achievementDao()),
            settingsRepository = settingsRepository,
            audioController = audioManager
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        billingManager.startConnection()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            TapCoreTheme {
                TapCoreApp(
                    viewModel = gameViewModel,
                    billingController = billingManager,
                    audioController = audioManager
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        audioManager.onForeground()
    }

    override fun onResume() {
        super.onResume()
        billingManager.refreshPurchasesAndProducts()
    }

    override fun onStop() {
        super.onStop()
        audioManager.onBackground()
        gameViewModel.flushSave()
    }

    override fun onDestroy() {
        billingManager.release()
        audioManager.release()
        super.onDestroy()
    }
}
