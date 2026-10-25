package com.afnuke.halloweenfamilyfeud

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

/**
 * Intelligent Answer Matcher:
 * Matches what the player said against the round's answers.
 * Ignores case and punctuation; accepts a match if the spoken words contain the answer's key words.
 */
object SpeechAnswerMatcher {

    private val commonStopWords = setOf(
        "the", "a", "an", "and", "or", "of", "in", "on", "at", "to", "for", "with",
        "some", "something", "someone", "people", "is", "it", "its", "their", "her",
        "his", "like", "put", "find", "place", "thing", "wear"
    )

    fun findMatchingSlotIndex(
        spokenText: String,
        answers: List<SurveyAnswer>,
        revealedSlots: Set<Int>
    ): Int? {
        val cleanSpoken = cleanString(spokenText)
        if (cleanSpoken.isBlank()) return null
        val spokenWords = cleanSpoken.split(" ").filter { it.isNotBlank() }

        // Pass 1: Direct full string or substring match on unrevealed slots
        for (i in answers.indices) {
            if (i in revealedSlots) continue
            val cleanAnswer = cleanString(answers[i].text)
            if (cleanSpoken == cleanAnswer) return i
            if (cleanSpoken.contains(cleanAnswer) || (cleanSpoken.length >= 4 && cleanAnswer.contains(cleanSpoken))) {
                return i
            }
        }

        // Pass 2: Key word matching (ignore punctuation & stop words)
        for (i in answers.indices) {
            if (i in revealedSlots) continue
            val cleanAnswer = cleanString(answers[i].text)
            val answerKeyWords = cleanAnswer
                .split(" ")
                .filter { it.isNotBlank() && it.length >= 3 && it !in commonStopWords }

            // If spoken text contains any primary key word of the answer
            val matchedKeyWord = answerKeyWords.any { keyWord ->
                spokenWords.any { spokenWord ->
                    spokenWord == keyWord ||
                    (spokenWord.length >= 4 && (spokenWord.startsWith(keyWord) || keyWord.startsWith(spokenWord)))
                } || cleanSpoken.contains(keyWord)
            }

            if (matchedKeyWord) {
                return i
            }
        }

        return null
    }

    private fun cleanString(input: String): String {
        return input.lowercase(Locale.ROOT)
            .replace(Regex("['’\".,/\\\\!?;:()\\[\\]{}_-]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}

/**
 * Android SpeechRecognizer wrapper for Jetpack Compose.
 *
 * 1) On-device speech recognition on Android 12+ (createOnDeviceSpeechRecognizer)
 *    with RecognizerIntent.EXTRA_PREFER_OFFLINE set to true (no server dependence).
 * 2) Fully releases (cancel + destroy) the recognizer on ANY result or error,
 *    creating a pristine new instance for the next listen.
 * 3) In-character error message: "Jack didn't catch that — say it again!"
 */
class SpeechInputManager(
    private val context: Context,
    private val onResultReceived: (String) -> Unit,
    private val onErrorReceived: (String) -> Unit,
    private val onListeningStateChanged: (Boolean) -> Unit
) : RecognitionListener {

    companion object {
        const val IN_CHARACTER_ERROR = "Jack didn't catch that — say it again!"
    }

    private var speechRecognizer: SpeechRecognizer? = null
    var isListening: Boolean = false
        private set

    /**
     * Fully releases and destroys the recognizer so no stale or errored
     * session is ever reused.
     */
    private fun releaseRecognizer() {
        try {
            speechRecognizer?.setRecognitionListener(null)
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (_: Throwable) {
            // Ignore teardown errors
        }
        speechRecognizer = null
    }

    fun startListening() {
        // Always release any existing recognizer first to ensure clean state
        releaseRecognizer()
        isListening = false

        // Check availability
        val isAvailable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SpeechRecognizer.isOnDeviceRecognitionAvailable(context) || SpeechRecognizer.isRecognitionAvailable(context)
        } else {
            SpeechRecognizer.isRecognitionAvailable(context)
        }

        if (!isAvailable) {
            onListeningStateChanged(false)
            onErrorReceived(IN_CHARACTER_ERROR)
            return
        }

        try {
            // 1) Use ON-DEVICE speech recognition on Android 12+ when available
            speechRecognizer = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
                ) {
                    SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
                } else {
                    SpeechRecognizer.createSpeechRecognizer(context)
                }
            } catch (_: Throwable) {
                SpeechRecognizer.createSpeechRecognizer(context)
            }

            speechRecognizer?.setRecognitionListener(this@SpeechInputManager)

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                // Offline preference - must work with no network
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            }

            speechRecognizer?.startListening(intent)
            isListening = true
            onListeningStateChanged(true)
        } catch (_: Throwable) {
            isListening = false
            releaseRecognizer()
            onListeningStateChanged(false)
            onErrorReceived(IN_CHARACTER_ERROR)
        }
    }

    fun stopListening() {
        isListening = false
        releaseRecognizer()
        onListeningStateChanged(false)
    }

    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        // User finished speaking. Recognizer is processing on-device audio.
        // Keep recognizer alive until onResults or onError fires.
    }

    override fun onError(error: Int) {
        // 2) After ANY error, fully release recognizer (cancel + destroy)
        isListening = false
        releaseRecognizer()
        onListeningStateChanged(false)
        // 3) Never show raw error number. Always show the in-character line.
        onErrorReceived(IN_CHARACTER_ERROR)
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val topHypothesis = matches?.firstOrNull()?.trim()

        // 2) After ANY result, fully release recognizer (cancel + destroy)
        isListening = false
        releaseRecognizer()
        onListeningStateChanged(false)

        if (!topHypothesis.isNullOrBlank()) {
            onResultReceived(topHypothesis)
        } else {
            onErrorReceived(IN_CHARACTER_ERROR)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}

    fun destroy() {
        stopListening()
    }
}
