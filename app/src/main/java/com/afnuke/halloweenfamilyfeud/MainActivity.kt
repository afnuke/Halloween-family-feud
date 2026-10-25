package com.afnuke.halloweenfamilyfeud

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.afnuke.halloweenfamilyfeud.ui.theme.CrtBorderMonochrome
import com.afnuke.halloweenfamilyfeud.ui.theme.CrtGrayDark
import com.afnuke.halloweenfamilyfeud.ui.theme.CrtGrayLight
import com.afnuke.halloweenfamilyfeud.ui.theme.CrtGrayMid
import com.afnuke.halloweenfamilyfeud.ui.theme.CrtWhite
import com.afnuke.halloweenfamilyfeud.ui.theme.HalloweenFamilyFeudTheme
import com.afnuke.halloweenfamilyfeud.ui.theme.HalloweenOrange
import com.afnuke.halloweenfamilyfeud.ui.theme.HalloweenOrangeBright
import com.afnuke.halloweenfamilyfeud.ui.theme.HalloweenOrangeDark
import com.afnuke.halloweenfamilyfeud.ui.theme.HalloweenPurple
import com.afnuke.halloweenfamilyfeud.ui.theme.HalloweenPurpleBorder
import com.afnuke.halloweenfamilyfeud.ui.theme.HalloweenPurpleDeep
import com.afnuke.halloweenfamilyfeud.ui.theme.HalloweenPurpleLight
import com.afnuke.halloweenfamilyfeud.ui.theme.StrikeActiveColor
import com.afnuke.halloweenfamilyfeud.ui.theme.StrikeInactiveBg
import com.afnuke.halloweenfamilyfeud.ui.theme.StrikeInactiveBorder
import com.afnuke.halloweenfamilyfeud.ui.theme.TvBezelInner
import com.afnuke.halloweenfamilyfeud.ui.theme.TvBezelOuter
import com.afnuke.halloweenfamilyfeud.ui.theme.TvCabinetDark
import com.afnuke.halloweenfamilyfeud.ui.theme.TvScreenGlass

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HalloweenFamilyFeudTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                // Vintage CRT television cabinet with animated chasing marquee bulbs
                VintageCrtTvCabinet(
                    isGameWon = uiState.gameWinner != null
                ) {
                    if (uiState.isIntro) {
                        VintageIntroScreen(onStartGame = viewModel::onStartGame)
                    } else {
                        VintageGameShowBoardScreen(
                            uiState = uiState,
                            onSlotClicked = { slot -> viewModel.onSlotClicked(slot, isVoiceInput = false) },
                            onVoiceAnswerHeard = viewModel::onVoiceAnswerHeard,
                            setListeningState = viewModel::setListeningState,
                            onStrikeClicked = viewModel::onStrikeClicked,
                            onStealMissed = viewModel::onStealMissed,
                            onAwardToTeam = viewModel::onAwardToTeam,
                            onNextRound = viewModel::onNextRound,
                            onResetGame = viewModel::onResetGame,
                            onToggleMute = viewModel::onToggleMute
                        )
                    }
                }
            }
        }
    }
}

/**
 * 1) THE SET: Vintage CRT Console Television Frame with animated marquee bulbs.
 */
@Composable
fun VintageCrtTvCabinet(
    isGameWon: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TvCabinetDark)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(22.dp),
            color = TvBezelOuter,
            border = BorderStroke(3.dp, Color(0xFF141318)),
            shadowElevation = 8.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Animated Marquee Bulbs chasing around the bezel
                MarqueeBulbsBorder(
                    isGameWon = isGameWon,
                    modifier = Modifier.fillMaxSize()
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 9.dp)
                ) {
                    // TV Top Bezel Strip: Channel badge & simulated vintage pilot light
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = HalloweenOrangeBright.copy(alpha = 0.9f),
                                modifier = Modifier.size(6.dp)
                            ) {}
                            Text(
                                text = "CH 13 • VHF",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = CrtGrayMid,
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            text = "OCTOBER 31, 1958",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = CrtGrayMid.copy(alpha = 0.7f),
                            letterSpacing = 0.5.sp
                        )
                    }

                    // CRT Picture Tube Bezel
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = TvScreenGlass,
                        border = BorderStroke(2.5.dp, TvBezelInner)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .crtScreenEffects()
                        ) {
                            // Deep purple stage curtains & footlights
                            StageSetBackground()

                            // Game Content inside the screen
                            content()

                            // 5) THE WIN: Halloween Confetti Burst
                            if (isGameWon) {
                                HalloweenConfettiOverlay()
                            }
                        }
                    }

                    // TV Bottom Bezel Strip: Speaker slits & model label
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(5) {
                                Surface(
                                    shape = RoundedCornerShape(1.dp),
                                    color = Color(0xFF16151A),
                                    modifier = Modifier
                                        .width(18.dp)
                                        .height(3.dp)
                                ) {}
                            }
                        }

                        Text(
                            text = "CONSOLE MODEL T-58",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = CrtGrayMid.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Very subtle scanline texture and vignette. Text remains 100% crisp.
 */
@Composable
private fun Modifier.crtScreenEffects(): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "crtFlicker")
    val ambientFlicker by infiniteTransition.animateFloat(
        initialValue = 0.01f,
        targetValue = 0.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientFlicker"
    )

    return this.drawWithContent {
        drawContent()

        // Ultra-fine scanline texture (opacity ~0.08f so all text is razor-sharp)
        val scanlineStep = 3.5.dp.toPx()
        var currentY = 0f
        val scanlineColor = Color.Black.copy(alpha = 0.08f)
        val strokeWidth = 1.dp.toPx()
        while (currentY < size.height) {
            drawLine(
                color = scanlineColor,
                start = Offset(0f, currentY),
                end = Offset(size.width, currentY),
                strokeWidth = strokeWidth
            )
            currentY += scanlineStep
        }

        // Soft picture tube vignette
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.20f),
                    Color.Black.copy(alpha = 0.55f)
                ),
                center = center,
                radius = maxOf(size.width, size.height) * 0.75f
            )
        )

        drawRect(
            color = HalloweenPurpleLight.copy(alpha = ambientFlicker)
        )
    }
}

/**
 * Vintage CRT Intro Screen.
 */
@Composable
fun VintageIntroScreen(
    onStartGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = CrtGrayDark,
                border = BorderStroke(1.dp, CrtBorderMonochrome)
            ) {
                Text(
                    text = "••• LIVE SPECIAL BROADCAST •••",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp,
                    color = CrtGrayLight,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Icon(
                imageVector = Icons.Default.Tv,
                contentDescription = null,
                tint = CrtGrayLight,
                modifier = Modifier.size(54.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "HALLOWEEN",
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = HalloweenOrangeBright,
                textAlign = TextAlign.Center
            )

            Text(
                text = "FAMILY FEUD",
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                color = HalloweenPurpleLight,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "TRANSMITTED LIVE FOR HOME VIEWERS",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = CrtGrayMid,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Button(
                onClick = onStartGame,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(54.dp)
                    .testTag("tap_to_start_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrtGrayDark,
                    contentColor = CrtWhite
                ),
                border = BorderStroke(1.5.dp, CrtWhite)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = CrtWhite,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TAP TO START",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp,
                    color = CrtWhite
                )
            }
        }
    }
}

/**
 * Main Game-Show Board screen inside the CRT display.
 */
@Composable
fun VintageGameShowBoardScreen(
    uiState: GameUiState,
    onSlotClicked: (Int) -> Unit,
    onVoiceAnswerHeard: (String) -> Unit,
    setListeningState: (Boolean, String?) -> Unit,
    onStrikeClicked: () -> Unit,
    onStealMissed: () -> Unit,
    onAwardToTeam: (Int) -> Unit,
    onNextRound: () -> Unit,
    onResetGame: () -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val speechManager = remember {
        SpeechInputManager(
            context = context,
            onResultReceived = { spokenText ->
                onVoiceAnswerHeard(spokenText)
            },
            onErrorReceived = { errorMsg ->
                setListeningState(false, errorMsg)
            },
            onListeningStateChanged = { listening ->
                if (listening) {
                    setListeningState(true, "Listening... say your answer out loud!")
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            speechManager.destroy()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            speechManager.startListening()
        } else {
            setListeningState(false, SpeechInputManager.IN_CHARACTER_ERROR)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Broadcast Header with Mute control & Turn Tracking
        BroadcastHeaderSection(
            currentRoundNumber = uiState.roundIndex + 1,
            totalRounds = uiState.rounds.size,
            playingTeam = uiState.playingTeam,
            isMuted = uiState.isMuted,
            onToggleMute = onToggleMute
        )

        // 2) THE BOARD: Big rounded board with metallic trim + SHOWMAN HOST standing beside it
        ShowpieceBoardSection(
            question = uiState.currentRound.question,
            answers = uiState.currentRound.answers,
            revealedSlots = uiState.revealedSlots,
            onSlotClicked = { slot ->
                speechManager.stopListening()
                onSlotClicked(slot)
            },
            hostExpression = uiState.hostExpression,
            hostReactionText = uiState.hostReactionText,
            isGameWon = uiState.gameWinner != null
        )

        // 5) THE WIN: Slam-in Show Graphic Winner Banner
        if (uiState.gameWinner != null) {
            SlamInGameWinnerBanner(
                gameWinner = uiState.gameWinner,
                onPlayAgain = onResetGame
            )
        }

        // Steal Mode or Round Over Banners
        BroadcastStatusBanners(
            roundState = uiState.roundState,
            otherTeam = uiState.otherTeam,
            winnerTeam = uiState.winnerTeam,
            playingTeam = uiState.playingTeam,
            lastAwardedPoints = uiState.lastAwardedPoints,
            onStealMissed = onStealMissed
        )

        // Big "ANSWER" Microphone Button
        VoiceAnswerBar(
            isListening = uiState.isListeningForSpeech,
            speechStatusMessage = uiState.speechStatusMessage,
            onAnswerMicTapped = {
                if (uiState.isListeningForSpeech) {
                    speechManager.stopListening()
                } else {
                    val hasRecordAudioPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasRecordAudioPermission) {
                        speechManager.startListening()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            }
        )

        // 3) ROUND BANK: Shining score display that ticks up with an animation
        TickingRoundBankSection(
            roundBank = uiState.roundBank
        )

        // 4) Three strike indicators
        BroadcastStrikesSection(
            strikeCount = uiState.strikes,
            roundState = uiState.roundState,
            onStrikeClicked = onStrikeClicked,
            onStealMissed = onStealMissed
        )

        // 3) PODIUMS: Team scores as lit studio podiums with marquee bulbs
        StudioPodiumsSection(
            team1Score = uiState.team1Score,
            team2Score = uiState.team2Score,
            roundBank = uiState.roundBank,
            playingTeam = uiState.playingTeam,
            otherTeam = uiState.otherTeam,
            roundState = uiState.roundState,
            winnerTeam = uiState.winnerTeam,
            onAwardToTeam = onAwardToTeam
        )

        // 6) Action Buttons: "Next Round" and "Reset Game"
        BroadcastControlsSection(
            onNextRound = onNextRound,
            onResetGame = onResetGame
        )

        Spacer(modifier = Modifier.height(4.dp))
    }
}

/**
 * 2) THE BOARD: Big rounded board with brushed metallic trim,
 * lit question banner in show-style lettering, and glossy answer tiles.
 */
@Composable
private fun ShowpieceBoardSection(
    question: String,
    answers: List<SurveyAnswer>,
    revealedSlots: Set<Int>,
    onSlotClicked: (Int) -> Unit,
    hostExpression: HostExpression,
    hostReactionText: String?,
    isGameWon: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("showpiece_board_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF14131A)),
        // Polished metallic trim gradient
        border = BorderStroke(
            width = 3.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    Color(0xFF6E6D78),
                    Color(0xFFD4D4DC),
                    Color(0xFF4A4954),
                    Color(0xFFB0AFC0)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Lit Question Banner + Host standing beside it
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // THE HOST: Bigger, bouncing, head-shaking, arms-up animation
                HostCharacterView(
                    expression = hostExpression,
                    hostReactionText = hostReactionText,
                    isWinning = isGameWon,
                    modifier = Modifier.width(92.dp)
                )

                // Lit Question Banner with metallic bezel and show typography
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("survey_question_card"),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0F0E13),
                    border = BorderStroke(1.5.dp, Color(0xFF908F9C))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "• SURVEY QUESTION •",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold,
                            color = CrtGrayLight,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Text(
                            text = "\"$question\"",
                            style = MaterialTheme.typography.headlineMedium,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CrtWhite,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // 8 Glossy Tiles (2 columns x 4 rows)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("answer_slots_grid"),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (row in 0 until 4) {
                    val leftIndex = row * 2
                    val rightIndex = row * 2 + 1

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GlossyAnswerTile(
                            slotNumber = leftIndex + 1,
                            slotIndex = leftIndex,
                            answer = answers.getOrNull(leftIndex),
                            isRevealed = leftIndex in revealedSlots,
                            onSlotClicked = { onSlotClicked(leftIndex) },
                            modifier = Modifier.weight(1f)
                        )

                        GlossyAnswerTile(
                            slotNumber = rightIndex + 1,
                            slotIndex = rightIndex,
                            answer = answers.getOrNull(rightIndex),
                            isRevealed = rightIndex in revealedSlots,
                            onSlotClicked = { onSlotClicked(rightIndex) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 2) Glossy Answer Tile with real 3D flip, shine sweep, and soft pulsing unrevealed state.
 */
@Composable
private fun GlossyAnswerTile(
    slotNumber: Int,
    slotIndex: Int,
    answer: SurveyAnswer?,
    isRevealed: Boolean,
    onSlotClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 3D Flip animation
    val rotation by animateFloatAsState(
        targetValue = if (isRevealed) 180f else 0f,
        animationSpec = tween(durationMillis = 440, easing = FastOutSlowInEasing),
        label = "slot_flip_$slotNumber"
    )

    // Unrevealed gentle breathing pulse
    val infiniteTransition = rememberInfiniteTransition(label = "slotPulse")
    val unrevealedPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "slotPulse"
    )

    // Shine sweep progress on reveal
    val shineSweepProgress by animateFloatAsState(
        targetValue = if (isRevealed) 1f else 0f,
        animationSpec = tween(durationMillis = 650, delayMillis = 150, easing = LinearEasing),
        label = "shineSweep"
    )

    Card(
        modifier = modifier
            .height(52.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density
                if (!isRevealed) {
                    scaleX = unrevealedPulseScale
                    scaleY = unrevealedPulseScale
                }
            }
            .clickable(enabled = !isRevealed && answer != null) {
                onSlotClicked()
            }
            .testTag("slot_$slotNumber"),
        shape = RoundedCornerShape(9.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (rotation <= 90f) Color(0xFF1B1B20) else Color(0xFF2A163B)
        ),
        border = BorderStroke(
            width = 1.5.dp,
            color = if (rotation <= 90f) CrtBorderMonochrome else HalloweenOrangeBright
        )
    ) {
        if (rotation <= 90f) {
            // Face-down: Glossy tile with soft pulsing number badge
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFF24242C),
                                Color(0xFF16161B)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = CrtGrayDark,
                    border = BorderStroke(1.dp, CrtGrayMid),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$slotNumber",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = CrtWhite
                        )
                    }
                }
            }
        } else {
            // Face-up: SPECIAL EFFECT — Flashes in Halloween Orange with a shining sweep!
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFF3B1A52),
                                Color(0xFF1E0C2C)
                            )
                        )
                    )
                    .drawWithContent {
                        drawContent()
                        // Shine sweep highlight sliding across the tile
                        if (shineSweepProgress in 0.05f..0.95f) {
                            val shineX = size.width * (shineSweepProgress * 1.5f - 0.25f)
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.40f),
                                        Color.Transparent
                                    ),
                                    start = Offset(shineX - 25.dp.toPx(), 0f),
                                    end = Offset(shineX + 25.dp.toPx(), size.height)
                                )
                            )
                        }
                    }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = answer?.text?.uppercase() ?: "—",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp)
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = HalloweenOrange,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = "${answer?.points ?: 0}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3) ROUND BANK: Shining scoreboard display that ticks up with an animation.
 */
@Composable
private fun TickingRoundBankSection(
    roundBank: Int
) {
    // Score ticks up smoothly when points land
    val animatedBankPoints by animateIntAsState(
        targetValue = roundBank,
        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        label = "tickingBank"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("round_bank_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111116)),
        border = BorderStroke(
            width = 2.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    Color(0xFF5A5868),
                    HalloweenOrangeBright,
                    Color(0xFF5A5868)
                )
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ROUND BANK",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Black,
                    color = CrtWhite
                )
                Text(
                    text = "Broadcast tally accumulated this round",
                    style = MaterialTheme.typography.bodySmall,
                    color = CrtGrayMid,
                    fontSize = 10.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1F1E26),
                border = BorderStroke(1.5.dp, HalloweenOrangeBright)
            ) {
                Text(
                    text = "$animatedBankPoints",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = HalloweenOrangeBright,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 3.dp)
                )
            }
        }
    }
}

/**
 * 3) PODIUMS: Team scores rendered as lit studio podiums with marquee bulb dots.
 * The active playing team's podium glows brighter with an "ON AIR" indicator.
 */
@Composable
private fun StudioPodiumsSection(
    team1Score: Int,
    team2Score: Int,
    roundBank: Int,
    playingTeam: Int,
    otherTeam: Int,
    roundState: RoundState,
    winnerTeam: Int?,
    onAwardToTeam: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("team_panels_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Team 1 Podium
        StudioPodiumCard(
            teamName = "TEAM 1",
            score = team1Score,
            isTurn = playingTeam == 1,
            isStealing = roundState == RoundState.STEAL_MODE && otherTeam == 1,
            isWinner = winnerTeam == 1,
            roundBank = roundBank,
            podiumAccent = HalloweenPurple,
            onClick = { onAwardToTeam(1) },
            testTag = "team_1_panel",
            modifier = Modifier.weight(1f)
        )

        // Team 2 Podium
        StudioPodiumCard(
            teamName = "TEAM 2",
            score = team2Score,
            isTurn = playingTeam == 2,
            isStealing = roundState == RoundState.STEAL_MODE && otherTeam == 2,
            isWinner = winnerTeam == 2,
            roundBank = roundBank,
            podiumAccent = HalloweenOrange,
            onClick = { onAwardToTeam(2) },
            testTag = "team_2_panel",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StudioPodiumCard(
    teamName: String,
    score: Int,
    isTurn: Boolean,
    isStealing: Boolean,
    isWinner: Boolean,
    roundBank: Int,
    podiumAccent: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(106.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isWinner -> Color(0xFF2C1E40)
                isStealing -> Color(0xFF381A4C)
                isTurn -> Color(0xFF201B2E)
                else -> Color(0xFF141418)
            }
        ),
        border = BorderStroke(
            width = if (isWinner || isStealing || isTurn) 2.dp else 1.dp,
            color = when {
                isWinner -> podiumAccent
                isStealing -> HalloweenOrangeBright
                isTurn -> podiumAccent.copy(alpha = 0.9f)
                else -> CrtBorderMonochrome
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Podium Top Lip with miniature marquee bulb dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Marquee bulb dots on podium lip
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    repeat(3) {
                        Surface(
                            shape = CircleShape,
                            color = if (isTurn || isStealing) HalloweenOrangeBright else CrtGrayMid,
                            modifier = Modifier.size(3.5.dp)
                        ) {}
                    }
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = CrtGrayDark,
                    border = BorderStroke(1.dp, CrtGrayMid)
                ) {
                    Text(
                        text = teamName,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = CrtWhite,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Right bulb dots
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    repeat(3) {
                        Surface(
                            shape = CircleShape,
                            color = if (isTurn || isStealing) HalloweenPurpleLight else CrtGrayMid,
                            modifier = Modifier.size(3.5.dp)
                        ) {}
                    }
                }
            }

            // Status Badge: ON AIR / STEALING / ROUND WINNER
            if (isStealing) {
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = HalloweenOrangeDark
                ) {
                    Text(
                        text = "STEALING",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            } else if (isTurn && !isWinner) {
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = podiumAccent.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, podiumAccent.copy(alpha = 0.8f))
                ) {
                    Text(
                        text = "• ON AIR •",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = CrtWhite,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }

            Text(
                text = "$score",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = CrtWhite
            )

            Text(
                text = when {
                    isWinner -> "ROUND WINNER!"
                    isStealing -> "ONE answer to steal!"
                    isTurn && roundBank > 0 -> "Bank: $roundBank pts"
                    roundBank > 0 -> "Tap to award"
                    else -> "Total Score"
                },
                style = MaterialTheme.typography.bodySmall,
                fontSize = 9.sp,
                fontWeight = if (isWinner || isStealing) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isWinner -> podiumAccent
                    isStealing -> HalloweenOrangeBright
                    isTurn -> CrtWhite
                    else -> CrtGrayMid
                }
            )
        }
    }
}

/**
 * 5) THE WIN: Show graphic winner banner that slams in with spring bounce.
 */
@Composable
private fun SlamInGameWinnerBanner(
    gameWinner: Int,
    onPlayAgain: () -> Unit
) {
    val slamScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "winnerSlam"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = slamScale
                scaleY = slamScale
            }
            .testTag("game_winner_banner"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = HalloweenPurpleDeep),
        border = BorderStroke(2.5.dp, HalloweenOrangeBright)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "🏆 TEAM $gameWinner WINS THE GAME! 🏆",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                color = HalloweenOrangeBright,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Happy Halloween, everybody!",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = CrtWhite,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onPlayAgain,
                modifier = Modifier
                    .height(44.dp)
                    .testTag("play_again_button"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HalloweenOrangeDark,
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, HalloweenOrangeBright)
            ) {
                Text(
                    text = "Play Again",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

/**
 * Steal Mode and Round Over banner displays.
 */
@Composable
private fun BroadcastStatusBanners(
    roundState: RoundState,
    otherTeam: Int,
    winnerTeam: Int?,
    playingTeam: Int,
    lastAwardedPoints: Int,
    onStealMissed: () -> Unit
) {
    AnimatedVisibility(
        visible = roundState == RoundState.STEAL_MODE,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("steal_banner"),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = HalloweenPurpleDeep),
            border = BorderStroke(2.dp, HalloweenOrangeBright)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "STEAL! Other team: ONE answer.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    color = HalloweenOrangeBright,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Team $otherTeam: Tap ANSWER mic or tap 1 hidden slot to steal!",
                    style = MaterialTheme.typography.bodySmall,
                    color = CrtWhite,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = onStealMissed,
                    modifier = Modifier
                        .height(42.dp)
                        .testTag("steal_missed_banner_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HalloweenOrangeDark,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, HalloweenOrangeBright)
                ) {
                    Text(
                        text = "Steal Missed (Award to Team $playingTeam)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    AnimatedVisibility(
        visible = roundState == RoundState.ROUND_OVER && winnerTeam != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("round_over_banner"),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = HalloweenPurpleDeep),
            border = BorderStroke(1.5.dp, HalloweenOrangeBright)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ROUND OVER — TEAM $winnerTeam WINS!",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = HalloweenOrangeBright
                    )
                    Text(
                        text = "Awarded $lastAwardedPoints points to Team $winnerTeam",
                        style = MaterialTheme.typography.bodySmall,
                        color = CrtGrayLight,
                        fontSize = 11.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, HalloweenOrangeBright)
                ) {
                    Text(
                        text = "Next Round ➔",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CrtWhite,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

/**
 * Big "ANSWER" Microphone Button.
 */
@Composable
private fun VoiceAnswerBar(
    isListening: Boolean,
    speechStatusMessage: String?,
    onAnswerMicTapped: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Button(
            onClick = onAnswerMicTapped,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .graphicsLayer {
                    if (isListening) {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
                }
                .testTag("voice_answer_button"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isListening) HalloweenOrange else CrtGrayDark,
                contentColor = if (isListening) Color.Black else CrtWhite
            ),
            border = BorderStroke(
                width = 1.5.dp,
                color = if (isListening) HalloweenOrangeBright else CrtWhite
            )
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
                contentDescription = "Microphone",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isListening) "LISTENING... SAY YOUR ANSWER!" else "ANSWER",
                style = MaterialTheme.typography.titleSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        if (!speechStatusMessage.isNullOrBlank()) {
            Text(
                text = speechStatusMessage,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = if (isListening) HalloweenOrangeBright else CrtGrayLight,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

/**
 * Header Section.
 */
@Composable
private fun BroadcastHeaderSection(
    currentRoundNumber: Int,
    totalRounds: Int,
    playingTeam: Int,
    isMuted: Boolean,
    onToggleMute: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "HALLOWEEN FEUD",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = CrtWhite
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("mute_button")
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = if (isMuted) "Unmute Audio" else "Mute Audio",
                        tint = if (isMuted) CrtGrayMid else CrtWhite
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CrtGrayDark,
                    border = BorderStroke(1.dp, CrtBorderMonochrome)
                ) {
                    Text(
                        text = "ROUND $currentRoundNumber OF $totalRounds",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = CrtWhite,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("turn_indicator_bar"),
            shape = RoundedCornerShape(6.dp),
            color = CrtGrayDark,
            border = BorderStroke(1.dp, CrtBorderMonochrome)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = CrtWhite,
                    modifier = Modifier.size(6.dp)
                ) {}
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (playingTeam == 1) "Team 1 is playing" else "Team 2 is playing",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CrtWhite,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

/**
 * 4. Three strike indicators.
 */
@Composable
private fun BroadcastStrikesSection(
    strikeCount: Int,
    roundState: RoundState,
    onStrikeClicked: () -> Unit,
    onStealMissed: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("strikes_section"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (strikeIndex in 1..3) {
                val isStruck = strikeIndex <= strikeCount
                BroadcastStrikeBoxIndicator(
                    isStruck = isStruck,
                    strikeNumber = strikeIndex
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (roundState == RoundState.STEAL_MODE) {
                Button(
                    onClick = onStealMissed,
                    modifier = Modifier
                        .height(46.dp)
                        .testTag("steal_missed_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HalloweenOrangeDark,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, HalloweenOrangeBright)
                ) {
                    Text(
                        text = "Steal Missed",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Button(
                onClick = onStrikeClicked,
                enabled = roundState == RoundState.PLAYING,
                modifier = Modifier
                    .height(46.dp)
                    .testTag("strike_button"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (roundState == RoundState.PLAYING) CrtGrayDark else StrikeInactiveBg,
                    contentColor = if (roundState == RoundState.PLAYING) CrtWhite else CrtGrayMid
                ),
                border = BorderStroke(
                    1.dp,
                    if (roundState == RoundState.PLAYING) CrtWhite else StrikeInactiveBorder
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Strike Icon",
                    tint = if (roundState == RoundState.PLAYING) CrtWhite else CrtGrayMid,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "STRIKE",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun BroadcastStrikeBoxIndicator(
    isStruck: Boolean,
    strikeNumber: Int
) {
    Surface(
        modifier = Modifier
            .size(46.dp)
            .testTag("strike_indicator_$strikeNumber"),
        shape = RoundedCornerShape(8.dp),
        color = if (isStruck) HalloweenPurpleDeep else StrikeInactiveBg,
        border = BorderStroke(
            width = if (isStruck) 2.dp else 1.dp,
            color = if (isStruck) StrikeActiveColor else StrikeInactiveBorder
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isStruck) {
                Text(
                    text = "X",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = StrikeActiveColor
                )
            } else {
                Text(
                    text = "X",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CrtGrayDark
                )
            }
        }
    }
}

/**
 * 6. Action Buttons: "Next Round" and "Reset Game".
 */
@Composable
private fun BroadcastControlsSection(
    onNextRound: () -> Unit,
    onResetGame: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("controls_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onNextRound,
            modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .testTag("next_round_button"),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CrtGrayDark,
                contentColor = CrtWhite
            ),
            border = BorderStroke(1.dp, CrtWhite)
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = null,
                tint = CrtWhite,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Next Round",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = CrtWhite
            )
        }

        OutlinedButton(
            onClick = onResetGame,
            modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .testTag("reset_game_button"),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, CrtBorderMonochrome),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = CrtGrayLight
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = CrtGrayLight,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Reset Game",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = CrtGrayLight
            )
        }
    }
}
