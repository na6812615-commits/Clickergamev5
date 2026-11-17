package com.example.data

import android.content.Context
import android.content.SharedPreferences

data class GameSettings(
    val bgmEnabled: Boolean = true,
    val sfxEnabled: Boolean = true,
    val bgmVolume: Float = 0.65f,
    val sfxVolume: Float = 0.85f,
    val hapticsEnabled: Boolean = true,
    val highGraphicsEnabled: Boolean = true,
    val floatingTextEnabled: Boolean = true
)

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("tapcore_settings", Context.MODE_PRIVATE)

    fun load(): GameSettings = GameSettings(
        bgmEnabled = prefs.getBoolean(KEY_BGM_ENABLED, true),
        sfxEnabled = prefs.getBoolean(KEY_SFX_ENABLED, true),
        bgmVolume = prefs.getFloat(KEY_BGM_VOLUME, 0.65f).coerceIn(0f, 1f),
        sfxVolume = prefs.getFloat(KEY_SFX_VOLUME, 0.85f).coerceIn(0f, 1f),
        hapticsEnabled = prefs.getBoolean(KEY_HAPTICS_ENABLED, true),
        highGraphicsEnabled = prefs.getBoolean(KEY_HIGH_GRAPHICS, true),
        floatingTextEnabled = prefs.getBoolean(KEY_FLOATING_TEXT, true)
    )

    fun save(settings: GameSettings) {
        prefs.edit()
            .putBoolean(KEY_BGM_ENABLED, settings.bgmEnabled)
            .putBoolean(KEY_SFX_ENABLED, settings.sfxEnabled)
            .putFloat(KEY_BGM_VOLUME, settings.bgmVolume.coerceIn(0f, 1f))
            .putFloat(KEY_SFX_VOLUME, settings.sfxVolume.coerceIn(0f, 1f))
            .putBoolean(KEY_HAPTICS_ENABLED, settings.hapticsEnabled)
            .putBoolean(KEY_HIGH_GRAPHICS, settings.highGraphicsEnabled)
            .putBoolean(KEY_FLOATING_TEXT, settings.floatingTextEnabled)
            .apply()
    }

    private companion object {
        const val KEY_BGM_ENABLED = "bgm_enabled"
        const val KEY_SFX_ENABLED = "sfx_enabled"
        const val KEY_BGM_VOLUME = "bgm_volume"
        const val KEY_SFX_VOLUME = "sfx_volume"
        const val KEY_HAPTICS_ENABLED = "haptics_enabled"
        const val KEY_HIGH_GRAPHICS = "high_graphics"
        const val KEY_FLOATING_TEXT = "floating_text"
    }
}
