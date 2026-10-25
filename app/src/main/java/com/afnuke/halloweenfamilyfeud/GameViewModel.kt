package com.afnuke.halloweenfamilyfeud

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class RoundState {
    PLAYING,
    STEAL_MODE,
    ROUND_OVER
}

data class GameUiState(
    val isIntro: Boolean = true,
    val isMuted: Boolean = false,
    val roundIndex: Int = 0,
    val rounds: List<SurveyRound> = surveyBank,
    val revealedSlots: Set<Int> = emptySet(),
    val roundBank: Int = 0,
    val strikes: Int = 0,
    val team1Score: Int = 0,
    val team2Score: Int = 0,
    val roundState: RoundState = RoundState.PLAYING,
    val winnerTeam: Int? = null,
    val gameWinner: Int? = null,
    val lastAwardedPoints: Int = 0,
    // Showman Host State
    val hostExpression: HostExpression = HostExpression.GRINNING,
    val hostReactionText: String? = null,
    // Voice Answer State
    val isListeningForSpeech: Boolean = false,
    val lastSpokenAnswer: String? = null,
    val speechStatusMessage: String? = null
) {
    val currentRound: SurveyRound
        get() = rounds.getOrElse(roundIndex) { rounds.first() }

    // Team 1 plays odd rounds (1, 3, 5 -> index 0, 2, 4), Team 2 plays even rounds (2, 4, 6 -> index 1, 3, 5)
    val playingTeam: Int
        get() = if (roundIndex % 2 == 0) 1 else 2

    val otherTeam: Int
        get() = if (playingTeam == 1) 2 else 1
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    val audioSynth = AudioSynthesizer()
    val hostVoice = HostVoiceManager(application.applicationContext)

    init {
        // Start the funky 1970s game show theme loop on the intro screen
        audioSynth.startTheme()
    }

    /**
     * Start the game from the Intro screen.
     * Stops the theme loop so the host's voice is completely clear.
     */
    fun onStartGame() {
        audioSynth.stopTheme()

        val firstQuestion = _uiState.value.currentRound.question
        val welcomeLine = hostVoice.getWelcomeLine()

        _uiState.update { current ->
            current.copy(
                isIntro = false,
                hostExpression = HostExpression.GRINNING,
                hostReactionText = "Welcome to the show!"
            )
        }

        hostVoice.speak(welcomeLine, flush = true)
        hostVoice.speak("Here is your first question: $firstQuestion", flush = false)
    }

    /**
     * Speaker mute button controls music, synthesized stingers, and host voice together.
     */
    fun onToggleMute() {
        val newMute = !_uiState.value.isMuted
        audioSynth.isMuted = newMute
        hostVoice.isMuted = newMute
        _uiState.update { it.copy(isMuted = newMute) }

        if (!newMute && _uiState.value.isIntro) {
            audioSynth.startTheme()
        }
    }

    /**
     * Updates speech listening UI status.
     */
    fun setListeningState(isListening: Boolean, message: String? = null) {
        _uiState.update { current ->
            current.copy(
                isListeningForSpeech = isListening,
                speechStatusMessage = message
            )
        }
    }

    /**
     * Voice Answer received via SpeechRecognizer.
     */
    fun onVoiceAnswerHeard(spokenAnswer: String) {
        val state = _uiState.value
        val answers = state.currentRound.answers
        val matchedSlot = SpeechAnswerMatcher.findMatchingSlotIndex(
            spokenText = spokenAnswer,
            answers = answers,
            revealedSlots = state.revealedSlots
        )

        _uiState.update { current ->
            current.copy(
                lastSpokenAnswer = spokenAnswer,
                isListeningForSpeech = false
            )
        }

        if (matchedSlot != null) {
            _uiState.update { current ->
                current.copy(speechStatusMessage = "Heard: \"$spokenAnswer\" -> MATCH!")
            }
            onSlotClicked(matchedSlot, isVoiceInput = true)
        } else {
            _uiState.update { current ->
                current.copy(
                    speechStatusMessage = "Heard: \"$spokenAnswer\" -> Not on the board!",
                    hostExpression = HostExpression.SHOCKED
                )
            }

            if (state.roundState == RoundState.STEAL_MODE) {
                onStealMissed()
            } else {
                onStrikeClicked()
            }
        }
    }

    /**
     * Resolves tapping or voice-revealing an answer slot.
     */
    fun onSlotClicked(slotIndex: Int, isVoiceInput: Boolean = false) {
        val state = _uiState.value
        val answers = state.currentRound.answers
        if (slotIndex >= answers.size) return
        if (slotIndex in state.revealedSlots) return

        val answer = answers[slotIndex]
        val slotPoints = answer.points

        when (state.roundState) {
            RoundState.PLAYING -> {
                val updatedBank = state.roundBank + slotPoints
                val updatedRevealed = state.revealedSlots + slotIndex
                val allAnswersRevealed = updatedRevealed.size >= answers.size
                val playingTeam = state.playingTeam

                val surveyLine = hostVoice.getSurveySaysLine()
                val goodAnswerLine = hostVoice.getGoodAnswerLine()

                // Synthesized ding-ding sits under the host's "Survey says!"
                audioSynth.playRevealDing()

                if (allAnswersRevealed) {
                    val newTeam1Score = if (playingTeam == 1) state.team1Score + updatedBank else state.team1Score
                    val newTeam2Score = if (playingTeam == 2) state.team2Score + updatedBank else state.team2Score
                    val gameWinner = checkGameWinner(newTeam1Score, newTeam2Score)

                    _uiState.update { current ->
                        current.copy(
                            revealedSlots = updatedRevealed,
                            roundBank = 0,
                            roundState = RoundState.ROUND_OVER,
                            winnerTeam = playingTeam,
                            gameWinner = gameWinner,
                            lastAwardedPoints = updatedBank,
                            team1Score = newTeam1Score,
                            team2Score = newTeam2Score,
                            hostExpression = HostExpression.LAUGHING,
                            hostReactionText = "Board cleared! Fantastic!"
                        )
                    }

                    if (gameWinner != null) {
                        audioSynth.playWinFanfareAndApplause()
                    }

                    hostVoice.speak("$surveyLine ${answer.text}! $slotPoints points! $goodAnswerLine", flush = true)
                    if (gameWinner != null) {
                        val winLine = hostVoice.getGameWinLine(gameWinner)
                        hostVoice.speak(winLine, flush = false)
                    }
                } else {
                    _uiState.update { current ->
                        current.copy(
                            revealedSlots = updatedRevealed,
                            roundBank = updatedBank,
                            hostExpression = HostExpression.GRINNING,
                            hostReactionText = goodAnswerLine
                        )
                    }
                    hostVoice.speak("$surveyLine ${answer.text}! $slotPoints points! $goodAnswerLine", flush = true)
                }
            }

            RoundState.STEAL_MODE -> {
                val finalBank = state.roundBank + slotPoints
                val stealingTeam = state.otherTeam
                val newTeam1Score = if (stealingTeam == 1) state.team1Score + finalBank else state.team1Score
                val newTeam2Score = if (stealingTeam == 2) state.team2Score + finalBank else state.team2Score
                val gameWinner = checkGameWinner(newTeam1Score, newTeam2Score)

                val stealSuccessLine = hostVoice.getStealSuccessLine()

                // Ding under reveal
                audioSynth.playRevealDing()

                _uiState.update { current ->
                    current.copy(
                        revealedSlots = current.revealedSlots + slotIndex,
                        roundBank = 0,
                        roundState = RoundState.ROUND_OVER,
                        winnerTeam = stealingTeam,
                        gameWinner = gameWinner,
                        lastAwardedPoints = finalBank,
                        team1Score = newTeam1Score,
                        team2Score = newTeam2Score,
                        hostExpression = HostExpression.LAUGHING,
                        hostReactionText = "They stole the bank!"
                    )
                }

                if (gameWinner != null) {
                    audioSynth.playWinFanfareAndApplause()
                }

                hostVoice.speak(
                    "Survey says... ${answer.text}! $slotPoints points! $stealSuccessLine",
                    flush = true
                )
                if (gameWinner != null) {
                    val winLine = hostVoice.getGameWinLine(gameWinner)
                    hostVoice.speak(winLine, flush = false)
                }
            }

            RoundState.ROUND_OVER -> {
                audioSynth.playRevealDing()
                _uiState.update { current ->
                    current.copy(
                        revealedSlots = current.revealedSlots + slotIndex,
                        hostExpression = HostExpression.TALKING,
                        hostReactionText = "And there was ${answer.text}!"
                    )
                }
                hostVoice.speak("Survey says! ${answer.text}! $slotPoints points!", flush = true)
            }
        }
    }

    /**
     * Strike logic: plays descending slide ("wah-wah").
     * On 3rd strike: tense drumroll rattle leads into steal mode.
     */
    fun onStrikeClicked() {
        val state = _uiState.value
        if (state.roundState != RoundState.PLAYING) {
            audioSynth.playStrikeSlide()
            return
        }

        val nextStrikes = state.strikes + 1
        audioSynth.playStrikeSlide()

        if (nextStrikes >= 3) {
            audioSynth.playStealDrumroll()
            val stealModeLine = hostVoice.getStealModeLine()
            _uiState.update { current ->
                current.copy(
                    strikes = 3,
                    roundState = RoundState.STEAL_MODE,
                    hostExpression = HostExpression.SHOCKED,
                    hostReactionText = "Three strikes! Steal mode!"
                )
            }
            hostVoice.speak(stealModeLine, flush = true)
        } else {
            val disbeliefLine = hostVoice.getStrikeDisbeliefLine(nextStrikes)
            _uiState.update { current ->
                current.copy(
                    strikes = nextStrikes,
                    hostExpression = HostExpression.SHOCKED,
                    hostReactionText = disbeliefLine
                )
            }
            hostVoice.speak(disbeliefLine, flush = true)
        }
    }

    /**
     * Steal Missed: descending slide, host speaks, bank goes to playing team.
     */
    fun onStealMissed() {
        val state = _uiState.value
        if (state.roundState != RoundState.STEAL_MODE) return

        val playingTeam = state.playingTeam
        val bank = state.roundBank
        val newTeam1Score = if (playingTeam == 1) state.team1Score + bank else state.team1Score
        val newTeam2Score = if (playingTeam == 2) state.team2Score + bank else state.team2Score
        val gameWinner = checkGameWinner(newTeam1Score, newTeam2Score)

        val stealMissedLine = hostVoice.getStealMissedLine()

        audioSynth.playStrikeSlide()

        _uiState.update { current ->
            current.copy(
                roundBank = 0,
                roundState = RoundState.ROUND_OVER,
                winnerTeam = playingTeam,
                gameWinner = gameWinner,
                lastAwardedPoints = bank,
                team1Score = newTeam1Score,
                team2Score = newTeam2Score,
                hostExpression = HostExpression.SHOCKED,
                hostReactionText = "Not there! Bank stays home!"
            )
        }

        if (gameWinner != null) {
            audioSynth.playWinFanfareAndApplause()
        }

        hostVoice.speak(stealMissedLine, flush = true)
        if (gameWinner != null) {
            val winLine = hostVoice.getGameWinLine(gameWinner)
            hostVoice.speak(winLine, flush = false)
        }
    }

    /**
     * Manual host award override.
     */
    fun onAwardToTeam(teamNumber: Int) {
        val state = _uiState.value
        val bank = state.roundBank
        val newTeam1Score = if (teamNumber == 1) state.team1Score + bank else state.team1Score
        val newTeam2Score = if (teamNumber == 2) state.team2Score + bank else state.team2Score
        val gameWinner = checkGameWinner(newTeam1Score, newTeam2Score)

        _uiState.update { current ->
            current.copy(
                team1Score = newTeam1Score,
                team2Score = newTeam2Score,
                roundBank = 0,
                roundState = RoundState.ROUND_OVER,
                winnerTeam = teamNumber,
                gameWinner = gameWinner,
                lastAwardedPoints = bank,
                hostExpression = HostExpression.LAUGHING,
                hostReactionText = "Bank awarded to Team $teamNumber!"
            )
        }

        if (gameWinner != null) {
            audioSynth.playWinFanfareAndApplause()
            val winLine = hostVoice.getGameWinLine(gameWinner)
            hostVoice.speak(winLine, flush = true)
        }
    }

    /**
     * Move to next round.
     */
    fun onNextRound() {
        val state = _uiState.value
        val nextRoundIndex = if (state.rounds.isNotEmpty()) {
            (state.roundIndex + 1) % state.rounds.size
        } else {
            0
        }
        val nextQuestion = state.rounds.getOrElse(nextRoundIndex) { state.rounds.first() }.question

        _uiState.update { current ->
            current.copy(
                roundIndex = nextRoundIndex,
                revealedSlots = emptySet(),
                roundBank = 0,
                strikes = 0,
                roundState = RoundState.PLAYING,
                winnerTeam = null,
                lastAwardedPoints = 0,
                hostExpression = HostExpression.GRINNING,
                hostReactionText = "Round ${nextRoundIndex + 1}! Let's play!",
                speechStatusMessage = null,
                lastSpokenAnswer = null
            )
        }

        hostVoice.speak("Round ${nextRoundIndex + 1}! Here is the question: $nextQuestion", flush = true)
    }

    /**
     * Reset game or Play Again.
     */
    fun onResetGame() {
        val firstQuestion = _uiState.value.rounds.firstOrNull()?.question ?: ""
        _uiState.update { current ->
            current.copy(
                roundIndex = 0,
                revealedSlots = emptySet(),
                roundBank = 0,
                strikes = 0,
                team1Score = 0,
                team2Score = 0,
                roundState = RoundState.PLAYING,
                winnerTeam = null,
                gameWinner = null,
                lastAwardedPoints = 0,
                hostExpression = HostExpression.GRINNING,
                hostReactionText = "Fresh game, fresh start!",
                speechStatusMessage = null,
                lastSpokenAnswer = null
            )
        }
        if (firstQuestion.isNotEmpty()) {
            hostVoice.speak("Here is your first question: $firstQuestion", flush = true)
        }
    }

    private fun checkGameWinner(team1Score: Int, team2Score: Int): Int? {
        return when {
            team1Score >= 300 -> 1
            team2Score >= 300 -> 2
            else -> null
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioSynth.shutdown()
        hostVoice.shutdown()
    }
}
