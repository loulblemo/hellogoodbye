package com.loulblemo.hellogoodbye

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.LocalIndication
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.PI
import kotlin.math.sin
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.draw.scale
import androidx.compose.animation.animateContentSize
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.airbnb.lottie.compose.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.decode.SvgDecoder
import coil.ImageLoader
import androidx.compose.foundation.border
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.CornerRadius

fun assetFlagPathForLanguage(code: String): String? {
    // This function is now deprecated in favor of getLanguageFlagAssetFromMetadata
    // Keeping for backward compatibility
    return when (code) {
        // Provided 4x3 assets
        "es" -> "flags/4x3/es.svg"
        "fr" -> "flags/4x3/fr.svg"
        "de" -> "flags/4x3/de.svg"
        "it" -> "flags/4x3/it.svg"
        "ja" -> "flags/4x3/jp.svg"
        else -> null
    }
}

@Composable
fun TopBarSection(currency: Int, onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Currency display
        Card(
            modifier = Modifier.size(60.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$currency",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        // Settings button
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun CentralGridSection(
    selectedCountries: List<Country>, 
    onFlagClick: (Country) -> Unit,
    onAddFlag: () -> Unit,
    canAddMore: Boolean,
    canAffordAdd: Boolean = true,
    price: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(selectedCountries) { country ->
            // Flag item with badge overlay
            val languageCode = languageNameToCode(country.language) ?: "es"
            val badgeLevel = getBadgeLevel(context, languageCode)
            
            val questCount = getLanguageQuestCount(context, languageCode)
            
            Box(
                modifier = Modifier.aspectRatio(1f)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onFlagClick(country) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val flagAsset = getLanguageFlagAssetFromMetadata(context, languageCode)
                        val imageLoader = remember(context) {
                            ImageLoader.Builder(context)
                                .components { add(SvgDecoder.Factory()) }
                                .build()
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.86f)
                                .aspectRatio(4f / 3f)
                                .clip(RoundedCornerShape(12.dp))
                                .drawBehind {
                                    // Subtle corner smudge: light translucent strokes with rounded corners
                                    val baseRadius = 12.dp.toPx()
                                    val outlineSize = androidx.compose.ui.geometry.Size(size.width, size.height)
                                    drawRoundRect(
                                        color = Color.Black.copy(alpha = 0.06f),
                                        size = outlineSize,
                                        style = Stroke(width = 4.dp.toPx()),
                                        cornerRadius = CornerRadius(baseRadius + 2.dp.toPx(), baseRadius + 2.dp.toPx())
                                    )
                                    drawRoundRect(
                                        color = Color.White.copy(alpha = 0.05f),
                                        size = outlineSize,
                                        style = Stroke(width = 2.dp.toPx()),
                                        cornerRadius = CornerRadius(baseRadius, baseRadius)
                                    )
                                }
                                .border(3.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (flagAsset != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data("file:///android_asset/${flagAsset}")
                                        .build(),
                                    contentDescription = "${country.language} flag",
                                    imageLoader = imageLoader,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = country.flag,
                                    fontSize = 64.sp
                                )
                            }
                        }
                    }
                }
                
                // Badge overlay
                if (badgeLevel != BadgeLevel.NONE) {
                    BadgeOverlay(
                        badgeLevel = badgeLevel,
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = 2.dp) // Minimal offset to align with flag edge
                    )
                }
            }
        }
        if (canAddMore) {
            item {
                // Plus button - matching flag shape
                Box(
                    modifier = Modifier.aspectRatio(1f)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(enabled = true) { onAddFlag() },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.background
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.86f)
                                    .aspectRatio(4f / 3f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .border(3.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add flag",
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResponsiveRedCross(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            // Round outline like the settings cog (same circular style)
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            ) {
                onClick()
            }
            .scale(if (isPressed) 0.9f else 1f)
            .animateContentSize(),
        contentAlignment = Alignment.Center
    ) {
        // Purple X with rounded stroke caps
        val xColor = MaterialTheme.colorScheme.onSecondaryContainer
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.minDimension * 0.09f
            val padding = size.minDimension * 0.30f

            // Draw X with two lines
            drawLine(
                color = xColor,
                start = Offset(padding, padding),
                end = Offset(size.width - padding, size.height - padding),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = xColor,
                start = Offset(size.width - padding, padding),
                end = Offset(padding, size.height - padding),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }

    LaunchedEffect(Unit) {
        // Handle press state for animation
        snapshotFlow { isPressed }
            .collect { pressed ->
                if (pressed) {
                    delay(100)
                    isPressed = false
                }
            }
    }
}

@Composable
fun ModernCheckmark(
    modifier: Modifier = Modifier,
    size: Float = 48f,
    color: Color = Color(0xFF4CAF50),
    strokeWidthFactor: Float = 0.08f
) {
    Canvas(modifier = modifier.size(size.dp)) {
        val strokeWidth = this.size.minDimension * strokeWidthFactor // Proportional stroke width in px
        val padding = this.size.minDimension * 0.15f // Proportional padding in px
        
        // Draw the checkmark with rounded caps
        drawLine(
            color = color,
            start = Offset(padding, this.size.height / 2),
            end = Offset(this.size.width * 0.4f, this.size.height * 0.7f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(this.size.width * 0.4f, this.size.height * 0.7f),
            end = Offset(this.size.width - padding, this.size.height * 0.3f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun ModernCheckmarkOverlay(
    modifier: Modifier = Modifier,
    size: Float = 120f,
    overlayAlpha: Float = 0.95f,
    checkScale: Float = 0.4f,
    strokeWidthFactor: Float = 0.08f
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(
                Color(0xFF4CAF50).copy(alpha = overlayAlpha),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Add a subtle inner shadow effect
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(
                    Color(0xFF2E7D32).copy(alpha = 0.3f),
                    CircleShape
                )
        )
        ModernCheckmark(
            size = size * checkScale,
            color = Color.White,
            strokeWidthFactor = strokeWidthFactor
        )
    }
}

@Composable
fun PracticeButtonSection(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .wrapContentWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = Color(0xFFE0E0E0),
            disabledContentColor = Color(0xFF7A7A7A)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        val provider = GoogleFont.Provider(
            providerAuthority = "com.google.android.gms.fonts",
            providerPackage = "com.google.android.gms",
            certificates = R.array.com_google_android_gms_fonts_certs
        )
        val titanOne = remember {
            val googleFont = GoogleFont("Titan One")
            FontFamily(Font(googleFont = googleFont, fontProvider = provider))
        }
        // Debug: Log font loading attempt
        LaunchedEffect(Unit) {
            println("DEBUG: Attempting to load Titan One font with provider: $provider")
        }
        Text(
            text = "PRACTICE",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f) else Color(0xFF7A7A7A),
            fontFamily = titanOne
        )
    }
}

@Composable
fun PronunciationWordBubble(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary
) {
    // Button-like bubble styled like Practice: primary background, Titan font, white text
    val provider = GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs
    )
    val titanOne = remember {
        val googleFont = GoogleFont("Titan One")
        FontFamily(Font(googleFont = googleFont, fontProvider = provider))
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Heuristic auto-sizing: shrink font for longer words
        val length = text.length
        val fontSp = when {
            length <= 8 -> 40.sp
            length <= 12 -> 34.sp
            length <= 18 -> 28.sp
            else -> 22.sp
        }
        Box(
            modifier = Modifier
                .wrapContentWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(containerColor)
                .clickable { onClick() }
                .padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = fontSp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontFamily = titanOne
            )
        }
    }
}

@Composable
fun TravelIconTicker(
    modifier: Modifier = Modifier,
    intervalMillis: Long = 6000L,
    travelDurationMs: Long = 2800L,
    iconSizeDp: Dp = 40.dp
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val iconFiles = remember {
        // Load available PNGs from assets/travel_icons once
        context.assets.list("travel_icons")?.filter { it.endsWith(".png") }?.toList().orEmpty()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(iconSizeDp * 1.6f) // lane height
    ) {
        val laneWidth = this.maxWidth
        var currentIcon by remember { mutableStateOf<String?>(null) }
        var xOffset by remember { mutableStateOf((-iconSizeDp)) }
        var yOffset by remember { mutableStateOf(0.dp) }

        LaunchedEffect(iconFiles, laneWidth, intervalMillis, travelDurationMs) {
            while (true) {
                if (iconFiles.isNotEmpty()) {
                    currentIcon = iconFiles.random()
                    val steps = 120
                    val stepDelay = (travelDurationMs / steps).coerceAtLeast(8)
                    val amplitudePx = with(density) { 5.dp.toPx() } // gentle vertical oscillation
                    for (i in 0..steps) {
                        val t = i / steps.toFloat()
                        val base = (-iconSizeDp) + (laneWidth + iconSizeDp) * t
                        xOffset = base
                        val phase = 2f * PI.toFloat() * (t * 1.5f) // ~1.5 vertical waves across
                        val yPx = sin(phase) * amplitudePx
                        yOffset = with(density) { yPx.toDp() }
                        delay(stepDelay)
                    }
                    // Immediately hide after finishing the travel to avoid lingering at the edge
                    currentIcon = null
                    xOffset = -iconSizeDp
                    yOffset = 0.dp
                }
                delay(intervalMillis)
            }
        }

        if (currentIcon != null) {
            // Simple shadow-ish background pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(iconSizeDp)
                    .padding(horizontal = 0.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data("file:///android_asset/travel_icons/" + currentIcon!!)
                        .build(),
                    contentDescription = "Travel icon",
                    modifier = Modifier
                        .size(iconSizeDp)
                        .offset(x = xOffset, y = yOffset),
                )
            }
        }
    }
}

@Composable
fun PracticeAndTravelButtonsSection(onPracticeClick: () -> Unit, onTravelClick: () -> Unit, travelEnabled: Boolean = true) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Animated travel icon ticker just above the Practice button
        TravelIconTicker(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        // Centered Practice button with fixed size
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
        ) {
            val spacing = 12.dp
            val tileWidth = (this.maxWidth - spacing * 3) / 4
            val innerFlagWidth = tileWidth * 0.86f
            val flagHeight = innerFlagWidth * (3f / 4f)

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                PracticeButtonSection(
                    onClick = onPracticeClick,
                    modifier = Modifier.height(flagHeight * 1.5f),
                    enabled = travelEnabled
                )
            }
        }
        
        if (!travelEnabled) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Study a few languages to enable practice",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ExerciseCompletionScreen(
    onContinue: () -> Unit
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("Confirmed_Tick.json"))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = true,
        iterations = 1,
        speed = 1.0f
    )
    
    // Simple flow: allow immediate continue without extra states
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Lottie Animation
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(200.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Continue Button - immediate advance, no special states/colors
        Button(
            onClick = onContinue,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Continue",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun MatchingExercise(
    title: String,
    pairs: List<MatchingPair>,
    onDone: (Boolean) -> Unit,
    useFlagAssets: Boolean = false
) {
    val context = LocalContext.current
    var mistakes by remember(pairs) { mutableStateOf(0) }
    var remaining by remember(pairs) { mutableStateOf(pairs.size) }
    var leftSelection by remember(pairs) { mutableStateOf<String?>(null) }
    var rightSelection by remember(pairs) { mutableStateOf<String?>(null) }
    var completed by remember(pairs) { mutableStateOf(false) }
    var wrongLeftId by remember(pairs) { mutableStateOf<String?>(null) }
    var wrongRightId by remember(pairs) { mutableStateOf<String?>(null) }
    var isFlashingWrong by remember(pairs) { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Build lookup maps and shuffled lists
    val leftItems = remember(pairs) { pairs.map { it.left }.shuffled() }
    val rightItems = remember(pairs) { pairs.map { it.right }.shuffled() }
    val leftById = remember(pairs) { pairs.associate { it.left.id to it.left } }
    val rightById = remember(pairs) { pairs.associate { it.right.id to it.right } }
    val solvedLeftIds = remember(pairs) { mutableStateListOf<String>() }
    val solvedRightIds = remember(pairs) { mutableStateListOf<String>() }

    fun tryMatch() {
        val l = leftSelection
        val r = rightSelection
        if (l != null && r != null) {
            val leftItem = leftById[l]
            val rightItem = rightById[r]
            if (leftItem != null && rightItem != null && leftItem.matchKey == rightItem.matchKey) {
                solvedLeftIds.add(l)
                solvedRightIds.add(r)
                remaining -= 1
                leftSelection = null
                rightSelection = null
                if (remaining == 0 && !completed) {
                    completed = true
                    onDone(mistakes == 0)
                }
            } else {
                mistakes += 1
                // Briefly flash the wrong choices in red, then clear
                wrongLeftId = l
                wrongRightId = r
                leftSelection = null
                rightSelection = null
                isFlashingWrong = true
                scope.launch {
                    delay(1000)
                    wrongLeftId = null
                    wrongRightId = null
                    isFlashingWrong = false
                }
            }
        }
    }

    // Handle edge-case: zero pairs — complete immediately
    LaunchedEffect(pairs) {
        if (pairs.isEmpty() && !completed) {
            completed = true
            onDone(true)
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Removed title for cleaner UI
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                leftItems.forEach { item ->
                    val solved = solvedLeftIds.contains(item.id)
                    PracticeBubble(
                        label = item.label,
                        selected = leftSelection == item.id,
                        solved = solved,
                        error = wrongLeftId == item.id,
                        onClick = {
                            if (isFlashingWrong) return@PracticeBubble
                            if (item.isAudio && item.audioFile != null) {
                                playAssetAudio(context, item.audioFile)
                            }
                            if (!solved) {
                                leftSelection = if (leftSelection == item.id) null else item.id
                                tryMatch()
                            }
                        }
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rightItems.forEach { item ->
                    val solved = solvedRightIds.contains(item.id)
                    if (useFlagAssets && !item.isAudio) {
                        val mk = item.matchKey
                        val langCode = if (mk.contains("_")) mk.substringBefore("_") else mk
                        PracticeBubbleFlag(
                            languageCode = langCode,
                            selected = rightSelection == item.id,
                            solved = solved,
                            error = wrongRightId == item.id,
                            onClick = {
                                if (isFlashingWrong) return@PracticeBubbleFlag
                                if (!solved) {
                                    rightSelection = if (rightSelection == item.id) null else item.id
                                    tryMatch()
                                }
                            }
                        )
                    } else {
                        PracticeBubble(
                            label = item.label,
                            selected = rightSelection == item.id,
                            solved = solved,
                            error = wrongRightId == item.id,
                            onClick = {
                                if (isFlashingWrong) return@PracticeBubble
                                if (item.isAudio && item.audioFile != null) {
                                    playAssetAudio(context, item.audioFile)
                                }
                                if (!solved) {
                                    rightSelection = if (rightSelection == item.id) null else item.id
                                    tryMatch()
                                }
                            }
                        )
                    }
                }
            }
        }
        // Hidden for cleaner UI
        // Row(
        //     modifier = Modifier
        //         .fillMaxWidth()
        //         .padding(horizontal = 16.dp),
        //     horizontalArrangement = Arrangement.SpaceBetween
        // ) {
        //     Text("Remaining: $remaining")
        //     Text("Mistakes: $mistakes")
        // }
    }
}

@Composable
fun PracticeBubbleFlag(
    languageCode: String,
    selected: Boolean,
    solved: Boolean,
    error: Boolean = false,
    onClick: () -> Unit
) {
    val containerColor = when {
        error -> MaterialTheme.colorScheme.errorContainer
        solved -> Color(0xFFC8E6C9)
        selected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        error -> MaterialTheme.colorScheme.error
        solved -> Color(0xFF2E7D32)
        selected -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onClick() }
            .border(
                width = if (borderColor == Color.Transparent) 0.dp else 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(24.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            BottomFlagBadge(languageCode = languageCode, modifier = Modifier)
        }
    }
}

@Composable
fun PracticeBubble(
    label: String,
    selected: Boolean,
    solved: Boolean,
    error: Boolean = false,
    onClick: () -> Unit
) {
    val containerColor = when {
        error -> MaterialTheme.colorScheme.errorContainer
        solved -> Color(0xFFC8E6C9) // light green
        selected -> MaterialTheme.colorScheme.primaryContainer // purple selection
        else -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        error -> MaterialTheme.colorScheme.error
        solved -> Color(0xFF2E7D32) // dark green outline
        selected -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }
    val textColor = when {
        error -> MaterialTheme.colorScheme.onErrorContainer
        solved -> Color(0xFF1B5E20)
        selected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onClick() }
            .border(
                width = if (borderColor == Color.Transparent) 0.dp else 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(24.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(label, color = textColor)
        }
    }
}

fun playAssetAudio(context: Context, fileName: String) {
    runCatching {
        val afd = context.assets.openFd("audio_files/$fileName")
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        afd.close()
        mediaPlayer.setOnPreparedListener { it.start() }
        mediaPlayer.setOnCompletionListener { it.release() }
        mediaPlayer.prepareAsync()
    }
}

@Composable
fun BadgeOverlay(
    badgeLevel: BadgeLevel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .build()
    }
    val assetPath = when (badgeLevel) {
        // Map SILVER enum to plain green badge (1 quest)
        BadgeLevel.SILVER -> "badges/badge_plain_green.svg"
        // Map BRONZE enum to plain bronze badge (5 quests)
        BadgeLevel.BRONZE -> "badges/badge_plain_bronze.svg"
        BadgeLevel.NONE -> return // Don't show anything for no badge
    }

    Box(
        modifier = modifier
            .size(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // White circle background
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = Color.White,
                    shape = CircleShape
                )
        )
        
        // Badge image centered within the white circle
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data("file:///android_asset/${assetPath}")
                .build(),
            contentDescription = "Badge",
            imageLoader = imageLoader,
            modifier = Modifier
                .size(22.dp) // Slightly smaller than the white circle to ensure perfect centering
        )
    }
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onProgressCleared: () -> Unit = {}
) {
    val context = LocalContext.current
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onBack) { Text("Back") }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Clear Progress Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "⚠️ Danger Zone",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "This will clear all your progress including:",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Column(
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text("• Quest progress and completions", fontSize = 12.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                    Text("• Badge progress (bronze/silver)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                    Text("• Learned words and counters", fontSize = 12.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                    Text("• All saved data", fontSize = 12.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { showConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "🗑️ Clear All Progress",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // App info at bottom
        Text(
            text = "HelloGoodbye Language Learning",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    
    // Confirmation dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Clear All Progress?") },
            text = { 
                Text("This action cannot be undone. All your progress, badges, and learned words will be permanently deleted.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        clearAllProgress(context)
                        showConfirmDialog = false
                        onProgressCleared()
                        onBack()
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun HelloGoodbyeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            // Subtle purple background across the app
            background = Color(0xFFF5F0FF)
        ),
        content = content
    )
}

@Composable
fun PronunciationAudioToEnglishExercise(
    title: String,
    pronunciation: String,
    audioFile: String?,
    options: List<String>,
    correctOption: String,
    onDone: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var selected by remember(options) { mutableStateOf<String?>(null) }
    var completed by remember(options) { mutableStateOf(false) }

    // Auto-play audio once when the exercise appears
    LaunchedEffect(audioFile) {
        if (audioFile != null) {
            playAssetAudio(context, audioFile)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Removed title for cleaner UI

        // Pronunciation bubble; tap to replay audio
        PronunciationWordBubble(
            text = pronunciation,
            onClick = { if (audioFile != null) playAssetAudio(context, audioFile) },
            modifier = Modifier
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = selected == option
                val isCorrectSelected = completed && isSelected && option == correctOption
                val isWrongSelected = completed && isSelected && option != correctOption
                PracticeBubble(
                    label = option,
                    selected = isSelected,
                    solved = isCorrectSelected,
                    error = isWrongSelected,
                    onClick = {
                        if (completed) return@PracticeBubble
                        selected = option
                        completed = true
                    }
                )
            }
        }

        // Continue bubble at bottom when a selection is made
        if (completed) {
            val isCorrect = selected == correctOption
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                PronunciationWordBubble(
                    text = "CONTINUE",
                    onClick = { onDone(isCorrect) },
                    containerColor = if (isCorrect) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun PronunciationAudioToTypeEnglishExercise(
    title: String,
    pronunciation: String,
    audioFile: String?,
    correctAnswer: String,
    onDone: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var input by remember(correctAnswer) { mutableStateOf("") }
    var completed by remember(correctAnswer) { mutableStateOf(false) }
    var isCorrect by remember(correctAnswer) { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(audioFile) {
        if (audioFile != null) {
            playAssetAudio(context, audioFile)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PronunciationWordBubble(
            text = pronunciation,
            onClick = { if (audioFile != null) playAssetAudio(context, audioFile) },
            modifier = Modifier
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { if (!completed) input = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Type the English translation") },
                singleLine = true,
                isError = completed && (isCorrect == false)
            )

            Button(
                onClick = {
                    if (completed) return@Button
                    completed = true
                    val normalizedInput = input.trim().lowercase()
                    val normalizedAnswer = correctAnswer.trim().lowercase()
                    val correct = normalizedInput == normalizedAnswer
                    isCorrect = correct
                    // If correct, immediately finish so parent can show animation; if wrong, show feedback first
                    if (correct) {
                        onDone(true)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Check")
            }

            // When wrong, show the correct answer and a red continue button (no animation)
            if (completed && (isCorrect == false)) {
                Text(
                    text = "Correct: " + correctAnswer,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // Bottom continue bubble when wrong answer
        if (completed && (isCorrect == false)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                PronunciationWordBubble(
                    text = "CONTINUE",
                    onClick = { onDone(false) },
                    containerColor = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}