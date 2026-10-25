package com.afnuke.halloweenfamilyfeud

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Audio Synthesizer:
 * Generates ALL game audio via algorithmic PCM audio synthesis and AudioTrack.
 * ZERO audio files, ZERO external media dependencies.
 * Includes an original 1970s funky game-show theme loop and 4 classic game-show stingers.
 */
class AudioSynthesizer {
    private val sampleRate = 44100
    private val scope = CoroutineScope(Dispatchers.Default)

    var isMuted: Boolean = false
        set(value) {
            field = value
            if (value) {
                stopTheme()
            }
        }

    private var themeTrack: AudioTrack? = null
    private var themeJob: Job? = null
    private val activeStingerTracks = mutableListOf<AudioTrack>()

    // Master volume scales to keep music and stingers sitting UNDER the host's speech
    private val musicVolume = 0.26f
    private val stingerVolume = 0.32f

    // Pre-synthesized 4-bar 120 BPM theme loop buffer (~8 seconds)
    private val themePcmBuffer: ShortArray by lazy {
        generate1970sFunkyTheme()
    }

    /**
     * 1) THEME: Funky 1970s game-show theme loop on the intro screen.
     * 120 BPM bouncing bass line with bright brass lead notes and shaker pulse.
     */
    fun startTheme() {
        if (isMuted) return
        stopTheme()

        themeJob = scope.launch {
            try {
                val buffer = themePcmBuffer
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
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.setLoopPoints(0, buffer.size, -1) // Infinite seamless loop
                track.play()
                themeTrack = track
            } catch (e: Throwable) {
                // Ignore audio init errors
            }
        }
    }

    /**
     * Stops the theme loop cleanly when the game starts.
     */
    fun stopTheme() {
        themeJob?.cancel()
        themeJob = null
        try {
            themeTrack?.let {
                if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Throwable) {
            // Ignore
        }
        themeTrack = null
    }

    /**
     * 2) STINGER: Bright rising two-note "ding-ding!" under host's "Survey says!".
     */
    fun playRevealDing() {
        if (isMuted) return
        scope.launch {
            val samples = generateRevealDingSamples()
            playStaticStinger(samples, stingerVolume)
        }
    }

    /**
     * 2) STINGER: Descending slide — classic sad "wah-wah" fall.
     */
    fun playStrikeSlide() {
        if (isMuted) return
        scope.launch {
            val samples = generateStrikeSlideSamples()
            playStaticStinger(samples, stingerVolume * 1.1f)
        }
    }

    /**
     * 2) STINGER: Tense little drumroll-rattle leading into steal mode.
     */
    fun playStealDrumroll() {
        if (isMuted) return
        scope.launch {
            val samples = generateStealDrumrollSamples()
            playStaticStinger(samples, stingerVolume * 0.95f)
        }
    }

    /**
     * 2) STINGER: Fanfare arpeggio + synthesized crowd applause swell for 300+ win.
     */
    fun playWinFanfareAndApplause() {
        if (isMuted) return
        scope.launch {
            val samples = generateWinFanfareAndApplauseSamples()
            playStaticStinger(samples, stingerVolume * 1.15f)
        }
    }

    private fun playStaticStinger(samples: ShortArray, volume: Float) {
        try {
            val scaledSamples = ShortArray(samples.size) { i ->
                (samples[i] * volume).toInt().coerceIn(-32768, 32767).toShort()
            }
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(scaledSamples.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(scaledSamples, 0, scaledSamples.size)
            track.play()

            synchronized(activeStingerTracks) {
                activeStingerTracks.add(track)
                // Clean up finished tracks
                activeStingerTracks.removeAll {
                    try {
                        it.playState != AudioTrack.PLAYSTATE_PLAYING
                    } catch (e: Throwable) {
                        true
                    }
                }
            }
        } catch (e: Throwable) {
            // Ignore
        }
    }

    // ==========================================
    // SYNTHESIS ALGORITHMS
    // ==========================================

    /**
     * Synthesizes an original 4-bar 1970s game show theme loop (120 BPM, ~8.0 seconds).
     * Bouncing square/triangle bassline + bright lead melody + funky shaker pulse.
     */
    private fun generate1970sFunkyTheme(): ShortArray {
        val totalSeconds = 8.0f // 16 beats at 120 BPM
        val totalSamples = (sampleRate * totalSeconds).toInt()
        val floatBuffer = FloatArray(totalSamples)

        val beatDuration = 0.5f // seconds per quarter note
        val sixteenthDuration = 0.125f // seconds per 16th note

        // Bassline Notes (Hz): C2=65.4, D2=73.4, E2=82.4, F2=87.3, G2=98.0, A2=110.0, Bb2=116.5, B2=123.5, C3=130.8
        val bassEvents = listOf(
            // Bar 1
            0.000f to 65.4f, 0.125f to 130.8f, 0.250f to 65.4f, 0.375f to 130.8f,
            0.500f to 65.4f, 0.625f to 98.0f,  0.750f to 116.5f, 0.875f to 123.5f,
            1.000f to 65.4f, 1.125f to 130.8f, 1.250f to 65.4f, 1.375f to 130.8f,
            1.500f to 98.0f, 1.625f to 116.5f, 1.750f to 130.8f, 1.875f to 98.0f,
            // Bar 2
            2.000f to 65.4f, 2.125f to 130.8f, 2.250f to 65.4f, 2.375f to 130.8f,
            2.500f to 87.3f, 2.625f to 92.5f,  2.750f to 98.0f,  2.875f to 196.0f,
            3.000f to 65.4f, 3.125f to 130.8f, 3.250f to 65.4f, 3.375f to 130.8f,
            3.500f to 98.0f, 3.625f to 110.0f, 3.750f to 116.5f, 3.875f to 123.5f,
            // Bar 3 (F funk change)
            4.000f to 87.3f, 4.125f to 174.6f, 4.250f to 87.3f, 4.375f to 174.6f,
            4.500f to 87.3f, 4.625f to 98.0f,  4.750f to 110.0f, 4.875f to 116.5f,
            5.000f to 98.0f, 5.125f to 196.0f, 5.250f to 98.0f, 5.375f to 196.0f,
            5.500f to 98.0f, 5.625f to 110.0f, 5.750f to 123.5f, 5.875f to 130.8f,
            // Bar 4 (Turnaround)
            6.000f to 65.4f, 6.125f to 82.4f,  6.250f to 98.0f,  6.375f to 116.5f,
            6.500f to 110.0f, 6.625f to 98.0f, 6.750f to 87.3f, 6.875f to 73.4f,
            7.000f to 65.4f, 7.125f to 130.8f, 7.250f to 98.0f,  7.375f to 65.4f,
            7.500f to 98.0f, 7.625f to 110.0f, 7.750f to 123.5f, 7.875f to 130.8f
        )

        // Render Bassline
        for ((startTime, freq) in bassEvents) {
            val startSample = (startTime * sampleRate).toInt()
            val noteSamples = (sixteenthDuration * 0.90f * sampleRate).toInt()
            for (i in 0 until noteSamples) {
                val idx = startSample + i
                if (idx >= totalSamples) break
                val t = i.toFloat() / sampleRate
                val env = (1.0f - i.toFloat() / noteSamples).coerceIn(0f, 1f)
                // Warm square-sine blend
                val s1 = sin(2f * PI.toFloat() * freq * t)
                val s3 = 0.35f * sin(6f * PI.toFloat() * freq * t)
                floatBuffer[idx] += (s1 + s3) * env * 0.40f
            }
        }

        // Lead Melody Notes (Hz): G4=392.0, A4=440.0, B4=493.9, C5=523.3, D5=587.3, E5=659.3, F5=698.5, G5=784.0
        val melodyEvents = listOf(
            // Bar 1
            0.25f to (392.0f to 0.20f), 0.50f to (523.3f to 0.30f), 0.875f to (659.3f to 0.20f),
            1.125f to (587.3f to 0.25f), 1.375f to (523.3f to 0.45f),
            // Bar 2
            2.00f to (392.0f to 0.20f), 2.25f to (440.0f to 0.20f), 2.50f to (523.3f to 0.25f),
            2.875f to (587.3f to 0.35f), 3.25f to (659.3f to 0.45f),
            // Bar 3
            4.00f to (698.5f to 0.35f), 4.375f to (659.3f to 0.20f), 4.625f to (587.3f to 0.20f),
            4.875f to (523.3f to 0.35f), 5.375f to (440.0f to 0.45f),
            // Bar 4
            6.00f to (392.0f to 0.20f), 6.25f to (440.0f to 0.20f), 6.50f to (523.3f to 0.20f),
            6.75f to (587.3f to 0.20f), 7.00f to (523.3f to 0.70f)
        )

        // Render Lead Melody
        for ((startTime, note) in melodyEvents) {
            val (freq, duration) = note
            val startSample = (startTime * sampleRate).toInt()
            val noteSamples = (duration * sampleRate).toInt()
            for (i in 0 until noteSamples) {
                val idx = startSample + i
                if (idx >= totalSamples) break
                val t = i.toFloat() / sampleRate
                val progress = i.toFloat() / noteSamples
                val env = sin(PI.toFloat() * progress.coerceIn(0f, 1f))
                // Bright brass/bell synth harmonic
                val s1 = sin(2f * PI.toFloat() * freq * t)
                val s2 = 0.45f * sin(4f * PI.toFloat() * freq * t)
                val s3 = 0.20f * sin(6f * PI.toFloat() * freq * t)
                floatBuffer[idx] += (s1 + s2 + s3) * env * 0.35f
            }
        }

        // Add 16th note shaker/hi-hat groove
        val numSixteenths = (totalSeconds / sixteenthDuration).toInt()
        for (step in 0 until numSixteenths) {
            val startSample = (step * sixteenthDuration * sampleRate).toInt()
            val isAccented = (step % 4 == 2) // Beats 2 and 4 backbeat
            val hatLength = (0.045f * sampleRate).toInt()
            val hatVolume = if (isAccented) 0.16f else 0.08f

            var noiseFilter = 0f
            for (i in 0 until hatLength) {
                val idx = startSample + i
                if (idx >= totalSamples) break
                val whiteNoise = Random.nextFloat() * 2f - 1f
                noiseFilter = 0.75f * noiseFilter + 0.25f * whiteNoise
                val env = 1f - i.toFloat() / hatLength
                floatBuffer[idx] += noiseFilter * env * hatVolume
            }
        }

        // Convert float buffer to 16-bit PCM ShortArray with soft limiter
        val pcm = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val sample = floatBuffer[i] * musicVolume
            val clamped = sample.coerceIn(-0.95f, 0.95f)
            pcm[i] = (clamped * 32767).toInt().toShort()
        }
        return pcm
    }

    /**
     * Stinger: Bright rising two-note "ding-ding!" (E6 -> A6).
     */
    private fun generateRevealDingSamples(): ShortArray {
        val note1Duration = 0.11f
        val note2Duration = 0.24f
        val totalDuration = note1Duration + note2Duration
        val totalSamples = (sampleRate * totalDuration).toInt()
        val pcm = ShortArray(totalSamples)

        val f1 = 1318.5f // E6
        val f2 = 1760.0f // A6

        val n1Samples = (note1Duration * sampleRate).toInt()
        val n2Samples = (note2Duration * sampleRate).toInt()

        // Note 1: E6
        for (i in 0 until n1Samples) {
            val t = i.toFloat() / sampleRate
            val env = exp(-22.0f * t)
            val s1 = sin(2f * PI.toFloat() * f1 * t)
            val s2 = 0.4f * sin(4f * PI.toFloat() * f1 * t)
            val v = (s1 + s2) * env * 0.85f
            pcm[i] = (v.coerceIn(-1f, 1f) * 32767).toInt().toShort()
        }

        // Note 2: A6
        for (i in 0 until n2Samples) {
            val idx = n1Samples + i
            if (idx >= totalSamples) break
            val t = i.toFloat() / sampleRate
            val env = exp(-12.0f * t)
            val s1 = sin(2f * PI.toFloat() * f2 * t)
            val s2 = 0.45f * sin(4f * PI.toFloat() * f2 * t)
            val v = (s1 + s2) * env * 0.90f
            pcm[idx] = (v.coerceIn(-1f, 1f) * 32767).toInt().toShort()
        }
        return pcm
    }

    /**
     * Stinger: Classic sad descending "wah-wah" slide.
     * Portamento glide from ~420 Hz down to ~150 Hz with 7 Hz wobble vibrato.
     */
    private fun generateStrikeSlideSamples(): ShortArray {
        val duration = 0.52f
        val totalSamples = (sampleRate * duration).toInt()
        val pcm = ShortArray(totalSamples)

        var phase = 0.0
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / duration

            // Frequency glides down from 420 Hz to 145 Hz
            val baseFreq = 420.0 - 275.0 * (progress * progress)
            // 7.5 Hz "wah-wah" vibrato modulation
            val vibrato = 12.0 * sin(2.0 * PI * 7.5 * t)
            val currentFreq = (baseFreq + vibrato).coerceAtLeast(60.0)

            phase += 2.0 * PI * currentFreq / sampleRate
            if (phase > 2.0 * PI) phase -= 2.0 * PI

            // Sawtooth-sine brass sound
            val s1 = sin(phase).toFloat()
            val s2 = 0.4f * sin(2.0 * phase).toFloat()
            val s3 = 0.2f * sin(3.0 * phase).toFloat()

            // Envelope: quick attack, swelling wah, then fade out
            val env = (sin(PI.toFloat() * progress.coerceIn(0f, 1f)) * (1.0f - 0.3f * progress)).coerceIn(0f, 1f)
            val v = (s1 + s2 + s3) * env * 0.75f
            pcm[i] = (v.coerceIn(-1f, 1f) * 32767).toInt().toShort()
        }
        return pcm
    }

    /**
     * Stinger: Tense snare drumroll-rattle leading into steal mode.
     */
    private fun generateStealDrumrollSamples(): ShortArray {
        val duration = 0.65f
        val totalSamples = (sampleRate * duration).toInt()
        val pcm = ShortArray(totalSamples)

        val rollInterval = 0.024f // ~40 rolls per second
        var noiseVal = 0f

        for (i in 0 until totalSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / duration
            val rollPhase = (t % rollInterval) / rollInterval
            val tapEnv = exp(-18f * rollPhase)
            val crescendo = 0.25f + 0.75f * progress

            val whiteNoise = Random.nextFloat() * 2f - 1f
            noiseVal = 0.65f * noiseVal + 0.35f * whiteNoise

            // Subtle low drum body
            val thud = sin(2f * PI.toFloat() * 115f * t) * 0.3f

            val v = (noiseVal * 0.7f + thud) * tapEnv * crescendo * 0.85f
            pcm[i] = (v.coerceIn(-1f, 1f) * 32767).toInt().toShort()
        }
        return pcm
    }

    /**
     * Stinger: Triumphant fanfare arpeggio (C5 - E5 - G5 - C6) + cheering crowd applause swell.
     */
    private fun generateWinFanfareAndApplauseSamples(): ShortArray {
        val duration = 2.4f
        val totalSamples = (sampleRate * duration).toInt()
        val floatBuffer = FloatArray(totalSamples)

        // Fanfare notes: C5 (523.3), E5 (659.3), G5 (784.0), C6 (1046.5)
        val fanfare = listOf(
            0.00f to (523.3f to 0.12f),
            0.12f to (659.3f to 0.12f),
            0.24f to (784.0f to 0.12f),
            0.36f to (1046.5f to 0.55f)
        )

        for ((startTime, note) in fanfare) {
            val (freq, noteDur) = note
            val startSample = (startTime * sampleRate).toInt()
            val noteSamples = (noteDur * sampleRate).toInt()
            for (i in 0 until noteSamples) {
                val idx = startSample + i
                if (idx >= totalSamples) break
                val t = i.toFloat() / sampleRate
                val env = (1.0f - i.toFloat() / noteSamples).coerceIn(0f, 1f)
                val s1 = sin(2f * PI.toFloat() * freq * t)
                val s2 = 0.5f * sin(4f * PI.toFloat() * freq * t)
                val s3 = 0.25f * sin(6f * PI.toFloat() * freq * t)
                floatBuffer[idx] += (s1 + s2 + s3) * env * 0.50f
            }
        }

        // Crowd applause: Filtered white noise with random bursts starting at 0.45s
        val applauseStart = (0.40f * sampleRate).toInt()
        var fNoise1 = 0f
        var fNoise2 = 0f

        for (i in applauseStart until totalSamples) {
            val t = (i - applauseStart).toFloat() / sampleRate
            val progress = t / (duration - 0.40f)

            // Crowd swell envelope (rise and fade)
            val swell = (sin(PI.toFloat() * progress.coerceIn(0f, 1f))).coerceIn(0f, 1f)

            val rawNoise = Random.nextFloat() * 2f - 1f
            fNoise1 = 0.82f * fNoise1 + 0.18f * rawNoise
            fNoise2 = 0.45f * fNoise2 + 0.55f * rawNoise

            // Add discrete clapping impulses
            val isClap = Random.nextFloat() > 0.94f
            val clapVal = if (isClap) (Random.nextFloat() * 1.5f - 0.75f) else 0f

            val crowdSound = (fNoise1 * 0.4f + fNoise2 * 0.3f + clapVal * 0.3f) * swell * 0.55f
            floatBuffer[i] += crowdSound
        }

        val pcm = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val v = floatBuffer[i]
            pcm[i] = (v.coerceIn(-1f, 1f) * 32767).toInt().toShort()
        }
        return pcm
    }

    fun shutdown() {
        stopTheme()
        synchronized(activeStingerTracks) {
            for (track in activeStingerTracks) {
                try {
                    track.stop()
                    track.release()
                } catch (e: Throwable) {
                    // Ignore
                }
            }
            activeStingerTracks.clear()
        }
    }
}
