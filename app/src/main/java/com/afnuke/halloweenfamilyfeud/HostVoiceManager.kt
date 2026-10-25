package com.afnuke.halloweenfamilyfeud

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale
import kotlin.random.Random

/**
 * Showman Host Voice Manager powered by Android TextToSpeech.
 * Pitch = 1.0 (natural), Rate = 1.05 (lively, high-energy game-show host).
 * Lines are selected at random without repeating back-to-back.
 */
class HostVoiceManager(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    var isMuted: Boolean = false
        set(value) {
            field = value
            if (value) {
                stop()
            }
        }

    private val pendingQueue = mutableListOf<String>()
    private val lastUsedIndexMap = mutableMapOf<String, Int>()

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Throwable) {
            tts = null
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            try {
                tts?.let {
                    it.language = Locale.US
                    it.setPitch(1.0f)      // Natural pitch
                    it.setSpeechRate(1.05f) // Lively, energetic game-show cadence
                    isInitialized = true

                    for (line in pendingQueue) {
                        speakInternal(line, flush = false)
                    }
                    pendingQueue.clear()
                }
            } catch (e: Throwable) {
                // Ignore initialization failures in test environments
            }
        }
    }

    fun speak(text: String, flush: Boolean = true) {
        if (isMuted) return
        if (!isInitialized || tts == null) {
            if (pendingQueue.size < 6) {
                pendingQueue.add(text)
            }
            return
        }
        speakInternal(text, flush)
    }

    private fun speakInternal(text: String, flush: Boolean) {
        try {
            val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            tts?.speak(text, queueMode, null, "host_voice_${System.currentTimeMillis()}")
        } catch (e: Throwable) {
            // Ignore
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Throwable) {
            // Ignore
        }
    }

    fun shutdown() {
        try {
            stop()
            tts?.shutdown()
        } catch (e: Throwable) {
            // Ignore
        }
        tts = null
        isInitialized = false
        pendingQueue.clear()
    }

    // --- REACTION LINES BANKS (No back-to-back repeats) ---

    private fun pickRandomLine(category: String, lines: List<String>): String {
        if (lines.isEmpty()) return ""
        if (lines.size == 1) return lines[0]
        val lastIndex = lastUsedIndexMap[category] ?: -1
        var newIndex: Int
        do {
            newIndex = Random.nextInt(lines.size)
        } while (newIndex == lastIndex)
        lastUsedIndexMap[category] = newIndex
        return lines[newIndex]
    }

    fun getWelcomeLine(): String {
        val lines = listOf(
            "Welcome, folks, to Halloween Family Feud! Where the families are strange and the answers are even stranger!",
            "Good evening America, and welcome to Halloween Family Feud! We've got a sensational show for you tonight!",
            "Welcome everybody to Halloween Family Feud! The pumpkin lights are shining, and our families are ready to play!",
            "Here we go, folks! Live from the studio, it's Halloween Family Feud! Let's meet our players and have some fun!"
        )
        return pickRandomLine("welcome", lines)
    }

    fun getSurveySaysLine(): String {
        val lines = listOf(
            "The survey says...!",
            "Let's see if it's up there! Survey says...!",
            "Is it on the board? Survey says...!",
            "Our hundred people answered! Survey says...!"
        )
        return pickRandomLine("surveySays", lines)
    }

    fun getGoodAnswerLine(): String {
        val lines = listOf(
            "Good answer! Good answer!",
            "Oh baby, that's what I'm talking about!",
            "Look at that board light up! Bingo!",
            "Right on the money! Fantastic guess!",
            "Yes sirree! That is a beauty!",
            "Boom! Right up there on the board!"
        )
        return pickRandomLine("goodAnswer", lines)
    }

    fun getStrikeDisbeliefLine(strikeNumber: Int): String {
        val countWord = when (strikeNumber) {
            1 -> "Strike one!"
            2 -> "Strike two!"
            else -> "Strike three!"
        }
        val lines = listOf(
            "$countWord Oh no. No no no...",
            "$countWord Aw, shucks! Not up there!",
            "$countWord Ouch! That is gonna hurt!",
            "$countWord The survey said absolutely NOT!",
            "$countWord Tough break! Not a single person said that!"
        )
        return pickRandomLine("strike", lines)
    }

    fun getStealModeLine(): String {
        val lines = listOf(
            "Three strikes! Other team — one answer... steals it all!",
            "Three strikes, folks! The door is wide open for a steal!",
            "Three strikes! Here comes the steal! One shot, all the marbles!",
            "Three strikes! It all comes down to this one answer!"
        )
        return pickRandomLine("stealMode", lines)
    }

    fun getStealSuccessLine(): String {
        val lines = listOf(
            "It's up there! You steal the bank! What a clutch move!",
            "Bingo! It's on the board! You stole the whole bank!",
            "Yes! You got it! The bank is yours! What a steal!"
        )
        return pickRandomLine("stealSuccess", lines)
    }

    fun getStealMissedLine(): String {
        val lines = listOf(
            "Not there! The bank stays home!",
            "No! Not on the board! The bank goes to the playing team!",
            "Aw, tough break! Not up there! The bank stays home!"
        )
        return pickRandomLine("stealMissed", lines)
    }

    fun getGameWinLine(teamNumber: Int): String {
        val lines = listOf(
            "Team $teamNumber wins the game! Happy Halloween, everybody!",
            "What a game! What a show! Team $teamNumber takes it all home! Happy Halloween, everybody!",
            "Ladies and gentlemen, we have our grand champions! Team $teamNumber wins the game! Happy Halloween, everybody!"
        )
        return pickRandomLine("gameWin", lines)
    }
}
