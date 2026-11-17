package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.SoundPool
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.data.GameSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

interface GameAudioController {
    fun applySettings(settings: GameSettings)
    fun onForeground()
    fun onBackground()
    fun playButtonClick()
    fun playTap(comboCount: Int)
    fun playCrit()
    fun playUpgrade()
    fun playError()
    fun playOverdrive()
    fun playAchievement()
    fun playRestart()
    fun playDiamond()
    fun playJackpot()
    fun playSpinTick()
    fun playBossHit()
    fun release()
}

val LocalSoundManager = staticCompositionLocalOf<GameAudioController> { NoOpAudioController() }

class NoOpAudioController : GameAudioController {
    override fun applySettings(settings: GameSettings) {}
    override fun onForeground() {}
    override fun onBackground() {}
    override fun playButtonClick() {}
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

/**
 * Sound effect & arcade ambient music manager backed by Android [SoundPool] for low-latency
 * UI/gameplay trigger sounds and a seamless streaming [AudioTrack] synthesizer for arcade
 * background ambient music.
 */
class GameAudioManager(
    context: Context,
    initialSettings: GameSettings
) : GameAudioController {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    private var currentSettings: GameSettings = initialSettings

    @Volatile
    private var isInForeground: Boolean = false

    @Volatile
    private var isReleased: Boolean = false

    private var bgmJob: Job? = null
    private var bgmTrack: AudioTrack? = null

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(MAX_STREAMS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val loadedSoundIds = ConcurrentHashMap<Int, Boolean>()
    private val soundIds = ConcurrentHashMap<String, Int>()
    private val tapKeys = List(8) { idx -> "tap_$idx" }

    private val bgmLoop: ShortArray by lazy { synthesizeArcadeAmbientLoop() }

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedSoundIds[sampleId] = true
            }
        }
        preloadSoundPoolAssets()
    }

    private fun preloadSoundPoolAssets() {
        scope.launch(Dispatchers.IO) {
            if (isReleased) return@launch
            val cacheDir = File(appContext.cacheDir, SFX_CACHE_DIR).apply { mkdirs() }

            val specs = buildList {
                add("button_click" to { ArcadeAudioSynth.buttonClick() })
                val tapFreqs = listOf(523.25, 587.33, 659.25, 783.99, 880.00, 1046.50, 1174.66, 1318.51)
                tapFreqs.forEachIndexed { idx, freq ->
                    add("tap_$idx" to { ArcadeAudioSynth.blip(freqHz = freq, durationMs = 68, decayRate = 28.0) })
                }
                add("crit" to { ArcadeAudioSynth.crit() })
                add("upgrade" to { ArcadeAudioSynth.arpeggio(listOf(523.25, 659.25, 783.99, 1046.50), noteMs = 55) })
                add("error" to { ArcadeAudioSynth.arpeggio(listOf(220.0, 174.61), noteMs = 80) })
                add("overdrive" to { ArcadeAudioSynth.sweep(startHz = 260.0, endHz = 1120.0, durationMs = 340) })
                add("achievement" to { ArcadeAudioSynth.arpeggio(listOf(659.25, 783.99, 987.77, 1318.51), noteMs = 70) })
                add("restart" to { ArcadeAudioSynth.arpeggio(listOf(392.0, 493.88, 587.33, 783.99, 1174.66), noteMs = 85) })
                add("diamond" to { ArcadeAudioSynth.arpeggio(listOf(1046.50, 1318.51, 1567.98, 2093.00), noteMs = 60) })
                add("jackpot" to { ArcadeAudioSynth.arpeggio(listOf(523.25, 659.25, 783.99, 1046.50, 1318.51, 1567.98, 2093.00), noteMs = 68) })
                add("spin_tick" to { ArcadeAudioSynth.blip(freqHz = 920.0, durationMs = 34, decayRate = 48.0) })
                add("boss_hit" to { ArcadeAudioSynth.sweep(startHz = 180.0, endHz = 65.0, durationMs = 110) })
            }

            for ((key, generator) in specs) {
                if (isReleased) break
                try {
                    val wavFile = File(cacheDir, "$key.wav")
                    if (!wavFile.exists() || wavFile.length() <= 44L) {
                        val samples = generator()
                        ArcadeAudioSynth.writeWavFile(wavFile, samples, SAMPLE_RATE)
                    }
                    val soundId = soundPool.load(wavFile.absolutePath, 1)
                    if (soundId > 0) {
                        soundIds[key] = soundId
                    }
                } catch (_: Throwable) {
                }
            }
        }
    }

    override fun applySettings(settings: GameSettings) {
        val prev = currentSettings
        currentSettings = settings
        try {
            bgmTrack?.setVolume(settings.bgmVolume.coerceIn(0f, 1f))
        } catch (_: Throwable) {
        }
        if (settings.bgmEnabled && isInForeground && (!prev.bgmEnabled || bgmJob == null)) {
            startBgm()
        } else if (!settings.bgmEnabled) {
            stopBgm()
        }
    }

    override fun onForeground() {
        isInForeground = true
        if (currentSettings.bgmEnabled) {
            startBgm()
        }
    }

    override fun onBackground() {
        isInForeground = false
        stopBgm()
    }

    override fun playButtonClick() {
        playPoolSound("button_click", gain = 0.55f, priority = 1)
    }

    override fun playTap(comboCount: Int) {
        val index = (comboCount - 1).coerceAtLeast(0) % tapKeys.size
        playPoolSound(tapKeys[index], gain = 0.65f, priority = 1)
    }

    override fun playCrit() {
        playPoolSound("crit", gain = 0.85f, priority = 2)
    }

    override fun playUpgrade() {
        playPoolSound("upgrade", gain = 0.75f, priority = 2)
    }

    override fun playError() {
        playPoolSound("error", gain = 0.65f, priority = 2)
    }

    override fun playOverdrive() {
        playPoolSound("overdrive", gain = 0.85f, priority = 3)
    }

    override fun playAchievement() {
        playPoolSound("achievement", gain = 0.80f, priority = 2)
    }

    override fun playRestart() {
        playPoolSound("restart", gain = 0.85f, priority = 3)
    }

    override fun playDiamond() {
        playPoolSound("diamond", gain = 0.90f, priority = 2)
    }

    override fun playJackpot() {
        playPoolSound("jackpot", gain = 0.95f, priority = 3)
    }

    override fun playSpinTick() {
        playPoolSound("spin_tick", gain = 0.45f, priority = 1)
    }

    override fun playBossHit() {
        playPoolSound("boss_hit", gain = 0.80f, priority = 2)
    }

    private fun playPoolSound(key: String, gain: Float, priority: Int) {
        if (isReleased || !currentSettings.sfxEnabled || currentSettings.sfxVolume <= 0.01f) return
        val soundId = soundIds[key] ?: return
        if (loadedSoundIds[soundId] != true) return
        val vol = (currentSettings.sfxVolume * gain).coerceIn(0f, 1f)
        try {
            soundPool.play(soundId, vol, vol, priority, 0, 1.0f)
        } catch (_: Throwable) {
        }
    }

    private fun startBgm() {
        if (isReleased || bgmJob?.isActive == true) return
        bgmJob = scope.launch {
            try {
                val loopSamples = bgmLoop
                val minBuf = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(4096)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBuf.coerceAtLeast(8192))
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                bgmTrack = track
                track.setVolume(currentSettings.bgmVolume.coerceIn(0f, 1f))
                track.play()

                val chunkSize = 2048
                var offset = 0
                while (isActive && !isReleased && currentSettings.bgmEnabled && isInForeground) {
                    val toWrite = minOf(chunkSize, loopSamples.size - offset)
                    val written = track.write(loopSamples, offset, toWrite, AudioTrack.WRITE_BLOCKING)
                    if (written < 0) break
                    offset = (offset + toWrite) % loopSamples.size
                }

                track.stop()
                track.release()
                if (bgmTrack === track) bgmTrack = null
            } catch (_: Throwable) {
                bgmTrack = null
            }
        }
    }

    private fun stopBgm() {
        bgmJob?.cancel()
        bgmJob = null
        try {
            bgmTrack?.pause()
            bgmTrack?.flush()
            bgmTrack?.release()
        } catch (_: Throwable) {
        }
        bgmTrack = null
    }

    override fun release() {
        isReleased = true
        stopBgm()
        loadedSoundIds.clear()
        soundIds.clear()
        try {
            soundPool.release()
        } catch (_: Throwable) {
        }
        scope.cancel()
    }

    private fun synthesizeArcadeAmbientLoop(): ShortArray {
        // 16-second seamless arcade ambient synthwave loop (4 chords x 4s each: Am9 -> Fmaj7 -> Cmaj7 -> G6)
        val totalSeconds = 16.0
        val count = (SAMPLE_RATE * totalSeconds).toInt()
        val roots = doubleArrayOf(110.0, 87.31, 130.81, 98.00) // A2, F2, C3, G2
        val fifths = doubleArrayOf(164.81, 130.81, 196.00, 146.83)
        val arpNotes = arrayOf(
            doubleArrayOf(220.0, 261.63, 329.63, 493.88, 440.0, 329.63, 261.63, 493.88), // Am9
            doubleArrayOf(174.61, 220.0, 261.63, 329.63, 349.23, 329.63, 261.63, 220.0), // Fmaj7
            doubleArrayOf(261.63, 329.63, 392.0, 493.88, 523.25, 493.88, 392.0, 329.63), // Cmaj7
            doubleArrayOf(196.0, 246.94, 293.66, 329.63, 392.0, 329.63, 293.66, 246.94)  // G6
        )
        val chordDuration = 4.0
        val stepDuration = 0.25 // 8th-note arcade shimmer at 120 BPM

        return ShortArray(count) { i ->
            val t = i.toDouble() / SAMPLE_RATE
            val chordIdx = (t / chordDuration).toInt().coerceIn(0, 3)
            val localChordT = t % chordDuration
            val stepIdx = (localChordT / stepDuration).toInt() % 8
            val localStepT = localChordT % stepDuration

            // Warm analog ambient pad
            val rootFreq = roots[chordIdx]
            val fifthFreq = fifths[chordIdx]
            val chordEnv = sin(PI * (localChordT / chordDuration)).coerceAtLeast(0.18)
            val pad = (
                sin(2.0 * PI * rootFreq * t) * 0.48 +
                    sin(2.0 * PI * fifthFreq * t) * 0.26 +
                    sin(2.0 * PI * (rootFreq * 2.0) * t) * 0.14
                ) * chordEnv

            // Crystalline retro-arcade arpeggio pluck
            val arpFreq = arpNotes[chordIdx][stepIdx]
            val arpEnv = (1.0 - exp(-localStepT * 130.0)) * exp(-localStepT * 7.5)
            val arp = (sin(2.0 * PI * arpFreq * t) * 0.8 + sin(2.0 * PI * arpFreq * 2.0 * t) * 0.2) * arpEnv * 0.42

            // Subtle cosmic shimmer octave chime on chord entry
            val shimmerEnv = (1.0 - exp(-localChordT * 20.0)) * exp(-localChordT * 1.4)
            val shimmer = sin(2.0 * PI * (arpNotes[chordIdx][3] * 2.0) * t) * shimmerEnv * 0.12

            // Gentle soft pulse on beat (every 0.5s)
            val beatT = t % 0.5
            val kickEnv = exp(-beatT * 24.0)
            val kickFreq = 50.0 + 58.0 * exp(-beatT * 36.0)
            val kick = sin(2.0 * PI * kickFreq * beatT) * kickEnv * 0.28

            val mixed = (pad + arp + shimmer + kick) * 0.46
            (mixed * 16000.0).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    companion object {
        const val SAMPLE_RATE = 22050
        private const val MAX_STREAMS = 10
        private const val SFX_CACHE_DIR = "arcade_sfx_v1"
    }
}

internal object ArcadeAudioSynth {

    fun buttonClick(): ShortArray {
        val count = GameAudioManager.SAMPLE_RATE * 44 / 1000
        return ShortArray(count) { i ->
            val t = i.toDouble() / GameAudioManager.SAMPLE_RATE
            val env = (1.0 - exp(-t * 600.0)) * exp(-t * 72.0)
            val wave = sin(2.0 * PI * 987.77 * t) * 0.65 + sin(2.0 * PI * 1479.98 * t) * 0.35
            (wave * env * 22000.0).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    fun blip(freqHz: Double, durationMs: Int, decayRate: Double): ShortArray {
        val count = GameAudioManager.SAMPLE_RATE * durationMs / 1000
        return ShortArray(count) { i ->
            val t = i.toDouble() / GameAudioManager.SAMPLE_RATE
            val env = (1.0 - exp(-t * 300.0)) * exp(-t * decayRate)
            val wave = sin(2.0 * PI * freqHz * t) * 0.75 + sin(2.0 * PI * freqHz * 2.0 * t) * 0.25
            (wave * env * 22000.0).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    fun crit(): ShortArray {
        val count = GameAudioManager.SAMPLE_RATE * 220 / 1000
        return ShortArray(count) { i ->
            val t = i.toDouble() / GameAudioManager.SAMPLE_RATE
            val env = (1.0 - exp(-t * 250.0)) * exp(-t * 12.0)
            val chord = (
                sin(2.0 * PI * 1046.50 * t) +
                    sin(2.0 * PI * 1318.51 * t) +
                    sin(2.0 * PI * 1567.98 * t) +
                    sin(2.0 * PI * 2093.00 * t) * 0.6
                ) / 3.6
            (chord * env * 25000.0).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    fun arpeggio(freqs: List<Double>, noteMs: Int): ShortArray {
        val samplesPerNote = GameAudioManager.SAMPLE_RATE * noteMs / 1000
        val total = samplesPerNote * freqs.size
        return ShortArray(total) { i ->
            val noteIndex = (i / samplesPerNote).coerceAtMost(freqs.lastIndex)
            val localSample = i % samplesPerNote
            val t = localSample.toDouble() / GameAudioManager.SAMPLE_RATE
            val env = (1.0 - exp(-t * 240.0)) * exp(-t * 18.0)
            val f = freqs[noteIndex]
            val wave = sin(2.0 * PI * f * t) * 0.8 + sin(2.0 * PI * f * 2.0 * t) * 0.2
            (wave * env * 21000.0).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    fun sweep(startHz: Double, endHz: Double, durationMs: Int): ShortArray {
        val count = GameAudioManager.SAMPLE_RATE * durationMs / 1000
        var phase = 0.0
        return ShortArray(count) { i ->
            val progress = i.toDouble() / count
            val t = i.toDouble() / GameAudioManager.SAMPLE_RATE
            val freq = startHz + (endHz - startHz) * (progress * progress)
            phase += 2.0 * PI * freq / GameAudioManager.SAMPLE_RATE
            val env = sin(PI * progress) * (1.0 - exp(-t * 100.0))
            val wave = sin(phase) * 0.7 + sin(phase * 1.5) * 0.3
            (wave * env * 23000.0).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    fun encodeWavBytes(samples: ShortArray, sampleRate: Int = GameAudioManager.SAMPLE_RATE): ByteArray {
        val dataSize = samples.size * 2
        val totalDataLen = 36 + dataSize
        val out = ByteArrayOutputStream(44 + dataSize)
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray(Charsets.US_ASCII))
            putInt(totalDataLen)
            put("WAVE".toByteArray(Charsets.US_ASCII))
            put("fmt ".toByteArray(Charsets.US_ASCII))
            putInt(16) // PCM chunk size
            putShort(1) // AudioFormat = PCM
            putShort(1) // Mono
            putInt(sampleRate)
            putInt(sampleRate * 2) // ByteRate
            putShort(2) // BlockAlign
            putShort(16) // BitsPerSample
            put("data".toByteArray(Charsets.US_ASCII))
            putInt(dataSize)
        }
        out.write(header.array())
        val sampleBuf = ByteBuffer.allocate(dataSize).order(ByteOrder.LITTLE_ENDIAN)
        for (s in samples) {
            sampleBuf.putShort(s)
        }
        out.write(sampleBuf.array())
        return out.toByteArray()
    }

    fun writeWavFile(target: File, samples: ShortArray, sampleRate: Int = GameAudioManager.SAMPLE_RATE) {
        val bytes = encodeWavBytes(samples, sampleRate)
        FileOutputStream(target).use { fos ->
            fos.write(bytes)
            fos.flush()
        }
    }
}
