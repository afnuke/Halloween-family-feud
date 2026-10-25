package com.afnuke.halloweenfamilyfeud

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleUnitTest {
  private val app: Application
    get() = ApplicationProvider.getApplicationContext()

  @Test
  fun surveyBank_validation() {
    assertEquals(12, surveyBank.size)
    for ((index, round) in surveyBank.withIndex()) {
      assertEquals("Round $index should have 8 answers", 8, round.answers.size)
      val totalPoints = round.answers.sumOf { it.points }
      assertEquals("Round $index points should equal 100", 100, totalPoints)
    }
  }

  @Test
  fun audioSynthesizer_methodsRunSafely() {
    val synth = AudioSynthesizer()
    assertFalse(synth.isMuted)

    // Verify theme start/stop
    synth.startTheme()
    synth.stopTheme()

    // Verify all 4 synthesized stingers execute cleanly
    synth.playRevealDing()
    synth.playStrikeSlide()
    synth.playStealDrumroll()
    synth.playWinFanfareAndApplause()

    synth.isMuted = true
    assertTrue(synth.isMuted)

    synth.shutdown()
  }

  @Test
  fun speechAnswerMatcher_matchesCorrectly() {
    val round1 = surveyBank[0]
    // Answers: Witch (32), Vampire/Dracula (22), Ghost (14), Zombie (10), Pirate (8), Clown (6), Superhero (5), Cat (3)

    // 1. Direct and case/punctuation variations
    assertEquals(0, SpeechAnswerMatcher.findMatchingSlotIndex("witch", round1.answers, emptySet()))
    assertEquals(0, SpeechAnswerMatcher.findMatchingSlotIndex("A WITCH!", round1.answers, emptySet()))
    assertEquals(0, SpeechAnswerMatcher.findMatchingSlotIndex("the wicked witch", round1.answers, emptySet()))

    // 2. Slashed and compound answers
    assertEquals(1, SpeechAnswerMatcher.findMatchingSlotIndex("dracula", round1.answers, emptySet()))
    assertEquals(1, SpeechAnswerMatcher.findMatchingSlotIndex("a vampire", round1.answers, emptySet()))

    // 3. Substrings & multi-word answers
    val round2 = surveyBank[1]
    assertEquals(0, SpeechAnswerMatcher.findMatchingSlotIndex("candy corn", round2.answers, emptySet()))
    assertEquals(0, SpeechAnswerMatcher.findMatchingSlotIndex("corn", round2.answers, emptySet()))
    assertEquals(2, SpeechAnswerMatcher.findMatchingSlotIndex("reeses peanut butter cups", round2.answers, emptySet()))
    assertEquals(2, SpeechAnswerMatcher.findMatchingSlotIndex("peanut butter cup", round2.answers, emptySet()))

    // 4. No match
    assertNull(SpeechAnswerMatcher.findMatchingSlotIndex("lasagna", round1.answers, emptySet()))

    // 5. Already revealed slot does not match
    assertNull(SpeechAnswerMatcher.findMatchingSlotIndex("witch", round1.answers, setOf(0)))
  }

  @Test
  fun voiceAnswer_correctGuessRevealsSlotAndPoints() {
    val viewModel = GameViewModel(app)
    viewModel.onStartGame()

    assertEquals(0, viewModel.uiState.value.roundBank)
    assertEquals(0, viewModel.uiState.value.revealedSlots.size)

    // Player speaks "Witch" (Slot 0, 32 pts)
    viewModel.onVoiceAnswerHeard("I think it's a witch!")

    assertEquals(32, viewModel.uiState.value.roundBank)
    assertTrue(0 in viewModel.uiState.value.revealedSlots)
    assertEquals(HostExpression.GRINNING, viewModel.uiState.value.hostExpression)
  }

  @Test
  fun voiceAnswer_wrongGuessAddsStrikeAndHostReacts() {
    val viewModel = GameViewModel(app)
    viewModel.onStartGame()

    assertEquals(0, viewModel.uiState.value.strikes)

    // Player speaks something wrong: "Pizza"
    viewModel.onVoiceAnswerHeard("Pepperoni Pizza")

    assertEquals(1, viewModel.uiState.value.strikes)
    assertEquals(HostExpression.SHOCKED, viewModel.uiState.value.hostExpression)
    assertNotNull(viewModel.uiState.value.hostReactionText)
  }

  @Test
  fun manualSlotTapping_stillWorksAsBackup() {
    val viewModel = GameViewModel(app)
    viewModel.onStartGame()

    viewModel.onSlotClicked(1)
    assertEquals(22, viewModel.uiState.value.roundBank)
    assertTrue(1 in viewModel.uiState.value.revealedSlots)
  }

  @Test
  fun toggleMute_controlsVoiceAndMusicTogether() {
    val viewModel = GameViewModel(app)
    assertFalse(viewModel.uiState.value.isMuted)
    assertFalse(viewModel.audioSynth.isMuted)
    assertFalse(viewModel.hostVoice.isMuted)

    viewModel.onToggleMute()
    assertTrue(viewModel.uiState.value.isMuted)
    assertTrue(viewModel.audioSynth.isMuted)
    assertTrue(viewModel.hostVoice.isMuted)

    viewModel.onToggleMute()
    assertFalse(viewModel.uiState.value.isMuted)
    assertFalse(viewModel.audioSynth.isMuted)
    assertFalse(viewModel.hostVoice.isMuted)
  }

  @Test
  fun gameFlow_alternatesPlayingTeam() {
    val viewModel = GameViewModel(app)
    viewModel.onStartGame()

    assertEquals(1, viewModel.uiState.value.playingTeam)
    assertEquals(2, viewModel.uiState.value.otherTeam)

    viewModel.onNextRound()
    assertEquals(2, viewModel.uiState.value.playingTeam)
    assertEquals(1, viewModel.uiState.value.otherTeam)
  }

  @Test
  fun gameFlow_threeStrikesTriggersStealMode() {
    val viewModel = GameViewModel(app)
    viewModel.onStartGame()

    viewModel.onStrikeClicked()
    viewModel.onStrikeClicked()
    viewModel.onStrikeClicked()

    assertEquals(3, viewModel.uiState.value.strikes)
    assertEquals(RoundState.STEAL_MODE, viewModel.uiState.value.roundState)
    assertEquals(HostExpression.SHOCKED, viewModel.uiState.value.hostExpression)
  }

  @Test
  fun gameFlow_reach300PointsTriggersGameWinner() {
    val viewModel = GameViewModel(app)
    viewModel.onStartGame()

    // Round 1 (Team 1 plays, sweeps 100 pts)
    for (i in 0 until 8) {
      viewModel.onSlotClicked(i)
    }
    assertEquals(100, viewModel.uiState.value.team1Score)

    // Round 2 (Team 2 plays, Team 1 steals 44 pts)
    viewModel.onNextRound()
    viewModel.onSlotClicked(0)
    viewModel.onStrikeClicked()
    viewModel.onStrikeClicked()
    viewModel.onStrikeClicked()
    viewModel.onSlotClicked(1)
    assertEquals(144, viewModel.uiState.value.team1Score)

    // Round 3 (Team 1 plays, sweeps 100 pts -> 244 pts)
    viewModel.onNextRound()
    for (i in 0 until 8) {
      viewModel.onSlotClicked(i)
    }
    assertEquals(244, viewModel.uiState.value.team1Score)

    // Round 4 (Team 2 plays, Team 1 steals 100 pts -> 344 pts >= 300!)
    viewModel.onNextRound()
    for (i in 0 until 7) {
      viewModel.onSlotClicked(i)
    }
    viewModel.onStrikeClicked()
    viewModel.onStrikeClicked()
    viewModel.onStrikeClicked()
    viewModel.onSlotClicked(7)

    assertEquals(344, viewModel.uiState.value.team1Score)
    assertEquals(1, viewModel.uiState.value.gameWinner)
    assertEquals(HostExpression.LAUGHING, viewModel.uiState.value.hostExpression)
  }

  @Test
  fun speechInputManager_inCharacterErrorAndLifecycle() {
    assertEquals("Jack didn't catch that — say it again!", SpeechInputManager.IN_CHARACTER_ERROR)

    var capturedError: String? = null
    var listeningState: Boolean? = null
    val manager = SpeechInputManager(
      context = app,
      onResultReceived = {},
      onErrorReceived = { err -> capturedError = err },
      onListeningStateChanged = { listening -> listeningState = listening }
    )

    // Simulate error 11 (ERROR_SERVER_DISCONNECTED)
    manager.onError(11)

    assertEquals("Jack didn't catch that — say it again!", capturedError)
    assertFalse(manager.isListening)
    assertEquals(false, listeningState)

    // Verify tap-to-reveal still works on ViewModel after error
    val viewModel = GameViewModel(app)
    viewModel.onStartGame()
    viewModel.setListeningState(false, capturedError)
    assertEquals("Jack didn't catch that — say it again!", viewModel.uiState.value.speechStatusMessage)

    // Player can tap slot 0 to reveal
    viewModel.onSlotClicked(0)
    assertTrue(0 in viewModel.uiState.value.revealedSlots)
    assertEquals(32, viewModel.uiState.value.roundBank)
  }
}
