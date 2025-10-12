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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Shadow
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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.delay
import androidx.compose.ui.draw.scale
import androidx.compose.animation.animateContentSize
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
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize

// Helper function to determine if text contains Thai or Vietnamese characters
fun needsLilitaFont(text: String): Boolean {
    return text.any { char ->
        // Thai Unicode range: U+0E00-U+0E7F
        // Vietnamese uses Latin characters with diacritics, but we'll check for common Vietnamese diacritics
        val codePoint = char.code
        (codePoint in 0x0E00..0x0E7F) || // Thai characters
        (char in "àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđĐ") // Vietnamese diacritics
    }
}

/*
Test cases for needsLilitaFont function:
- needsLilitaFont("สวัสดี") should return true (Thai)
- needsLilitaFont("Xin chào") should return true (Vietnamese with diacritics)
- needsLilitaFont("hello") should return false (English)
- needsLilitaFont("Hola") should return false (Spanish)
- needsLilitaFont("привет") should return false (Russian - not Thai/Vietnamese)
*/

// Helper function to get the appropriate font family based on text content
@Composable
fun getAppropriateFontFamily(text: String): FontFamily {
    val provider = GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs
    )
    
    return remember(text) {
        val googleFont = if (needsLilitaFont(text)) {
            GoogleFont("Lilita One")
        } else {
            GoogleFont("Titan One")
        }
        FontFamily(Font(googleFont = googleFont, fontProvider = provider))
    }
}

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
    val context = LocalContext.current
    val debugModeEnabled = loadDebugMode(context)
    
    android.util.Log.d("FLASH_DEBUG", "=== TopBarSection called with currency=$currency ===")
    
    // Simple flash animation state
    val normalColor = colorResource(id = R.color.primary_container_purple)
    var shouldFlash by remember { mutableStateOf(false) }
    val previousCurrency = remember { mutableStateOf(currency) }
    
    android.util.Log.d("FLASH_DEBUG", "RENDER: currency=$currency, shouldFlash=$shouldFlash, previousCurrency=${previousCurrency.value}")
    
    // Flash green only when currency increases (point added)
    LaunchedEffect(currency) {
        println("DEBUG FLASH EFFECT: currency=$currency, previousCurrency=${previousCurrency.value}")
        if (currency > previousCurrency.value) { // Only flash when points are added, not spent
            println("DEBUG FLASH EFFECT: Triggering flash! Setting shouldFlash=true")
            shouldFlash = true
            println("DEBUG FLASH EFFECT: shouldFlash is now $shouldFlash")
            delay(2000) // Flash for 2 seconds
            println("DEBUG FLASH EFFECT: Ending flash, setting shouldFlash=false")
            shouldFlash = false
        }
        previousCurrency.value = currency
    }
    
    val flashColor = if (shouldFlash) {
        println("DEBUG FLASH: Using GREEN color")
        Color(0xFF4CAF50)
    } else {
        println("DEBUG FLASH: Using NORMAL color")
        normalColor
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: Currency + Debug indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Currency display
            Card(
                modifier = Modifier.size(60.dp),
                colors = CardDefaults.cardColors(
                    containerColor = flashColor
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
                        color = colorResource(id = R.color.purple_black)
                    )
                }
            }
            
            // Debug mode indicator (only show when enabled)
            if (debugModeEnabled) {
                Card(
                    modifier = Modifier.size(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = FlagRed
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🐛",
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
        
        // Settings button
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(colorResource(id = R.color.secondary_container_purple))
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = colorResource(id = R.color.purple_black)
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
                        containerColor = colorResource(id = R.color.background_light_purple)
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
                                .border(3.dp, colorResource(id = R.color.primary_purple).copy(alpha = 0.7f), RoundedCornerShape(12.dp)),
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
                            containerColor = colorResource(id = R.color.background_light_purple)
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
                                    .background(colorResource(id = R.color.primary_purple))
                                    .border(3.dp, colorResource(id = R.color.primary_purple).copy(alpha = 0.7f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add flag",
                                    modifier = Modifier.size(36.dp),
                                    tint = colorResource(id = R.color.white)
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
            .background(colorResource(id = R.color.secondary_container_purple))
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
        val xColor = colorResource(id = R.color.purple_black) // Move xColor definition here
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
                        color: Color = GreenMain,
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
                GreenMain.copy(alpha = overlayAlpha),
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
                    GreenDark.copy(alpha = 0.3f),
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
    val context = LocalContext.current
    
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .wrapContentWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = R.color.primary_purple),
            contentColor = colorResource(id = R.color.white),
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
        // Debug: Log font loading attempt (only in debug mode)
        LaunchedEffect(Unit) {
            if (loadDebugMode(context)) {
                println("DEBUG: Attempting to load Titan One font with provider: $provider")
            }
        }
        Text(
            text = "PRACTICE",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) colorResource(id = R.color.white).copy(alpha = 0.92f) else Color(0xFF7A7A7A),
            fontFamily = titanOne
        )
    }
}

@Composable
fun PronunciationWordBubble(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = colorResource(id = R.color.primary_purple),
    textColor: Color = colorResource(id = R.color.white)
) {
    // Button-like bubble styled like Practice: primary background, appropriate font, white text
    val appropriateFont = getAppropriateFontFamily(text)
    
    // Adjust font size range based on text length
    val (minFontSize, maxFontSize) = if (text.length > 20) {
        18.sp to 32.sp // Smaller range for longer words
    } else {
        20.sp to 40.sp // Normal range for shorter words
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(containerColor)
                .clickable { onClick() }
                .padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            BasicText(
                text = text,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    fontFamily = appropriateFont
                ),
                autoSize = TextAutoSize.StepBased(
                    minFontSize = minFontSize,
                    maxFontSize = maxFontSize,
                    stepSize = 1.sp
                ),
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2, // Allow wrapping to 2 lines
                overflow = TextOverflow.Clip
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
        
        // Continue Button - rectangular style like PronunciationWordBubble
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            val titanOneFont = remember {
                val provider = GoogleFont.Provider(
                    providerAuthority = "com.google.android.gms.fonts",
                    providerPackage = "com.google.android.gms",
                    certificates = R.array.com_google_android_gms_fonts_certs
                )
                val googleFont = GoogleFont("Titan One")
                FontFamily(Font(googleFont = googleFont, fontProvider = provider))
            }
            
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GreenMain)
                    .clickable { onContinue() }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CONTINUE",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontFamily = titanOneFont
                )
            }
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
    // Create a unique key that includes both pairs content and a timestamp to ensure state resets
    val exerciseKey = remember(pairs) { 
        pairs.map { "${it.left.id}-${it.right.id}" }.joinToString("-") + "-${System.currentTimeMillis()}"
    }
    var mistakes by remember(exerciseKey) { mutableStateOf(0) }
    var remaining by remember(exerciseKey) { mutableStateOf(pairs.size) }
    var leftSelection by remember(exerciseKey) { mutableStateOf<String?>(null) }
    var rightSelection by remember(exerciseKey) { mutableStateOf<String?>(null) }
    var completed by remember(exerciseKey) { mutableStateOf(false) }
    var wrongLeftId by remember(exerciseKey) { mutableStateOf<String?>(null) }
    var wrongRightId by remember(exerciseKey) { mutableStateOf<String?>(null) }
    var isFlashingWrong by remember(exerciseKey) { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Build lookup maps and shuffled lists
    val leftItems = remember(exerciseKey) { pairs.map { it.left }.shuffled() }
    val rightItems = remember(exerciseKey) { pairs.map { it.right }.shuffled() }
    val leftById = remember(exerciseKey) { pairs.associate { it.left.id to it.left } }
    val rightById = remember(exerciseKey) { pairs.associate { it.right.id to it.right } }
    val solvedLeftIds = remember(exerciseKey) { mutableStateListOf<String>() }
    val solvedRightIds = remember(exerciseKey) { mutableStateListOf<String>() }

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
    LaunchedEffect(exerciseKey) {
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
        error -> colorResource(id = R.color.primary_container_purple)
        solved -> GreenLight
        selected -> colorResource(id = R.color.primary_container_purple)
        else -> colorResource(id = R.color.surface_light_purple)
    }
    val borderColor = when {
        error -> FlagRed
        solved -> GreenDark
        selected -> colorResource(id = R.color.primary_purple)
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
        error -> colorResource(id = R.color.primary_container_purple)
        solved -> GreenLight // light green
        selected -> colorResource(id = R.color.primary_container_purple) // purple selection
        else -> colorResource(id = R.color.surface_light_purple)
    }
    val borderColor = when {
        error -> FlagRed
        solved -> GreenDark // dark green outline
        selected -> colorResource(id = R.color.primary_purple)
        else -> Color.Transparent
    }
    val textColor = when {
        error -> colorResource(id = R.color.purple_black)
        solved -> GreenDarker
        selected -> colorResource(id = R.color.purple_black)
        else -> colorResource(id = R.color.purple_black)
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            BasicText(
                text = label,
                style = TextStyle(
                    color = textColor,
                    textAlign = TextAlign.Center
                ),
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 10.sp,
                    maxFontSize = 16.sp,
                    stepSize = 1.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )
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
        // Map GREEN enum to plain green badge (1+ exercises)
        BadgeLevel.GREEN -> "badges/badge_plain_green.svg"
        // Map BRONZE enum to plain bronze badge (5+ exercises)
        BadgeLevel.BRONZE -> "badges/badge_plain_bronze.svg"
        // Map SILVER enum to plain silver badge (10+ exercises)
        BadgeLevel.SILVER -> "badges/badge_plain_silver.svg"
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
    onProgressCleared: () -> Unit = {},
    onSignIn: () -> Unit = {}
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var debugModeEnabled by remember { mutableStateOf(loadDebugMode(context)) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.background_light_purple))
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
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.purple_black)
            )
            TextButton(onClick = onBack) { Text("Back", color = colorResource(id = R.color.purple_black)) }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Clear Progress Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(id = R.color.white)
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = FlagRed.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚠",
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Reset Progress",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.purple_black)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Permanently delete all your learning progress and data",
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.purple_black).copy(alpha = 0.7f),
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    ProgressItem("Quest completions", fontSize = 13.sp)
                    ProgressItem("Badge achievements", fontSize = 13.sp)
                    ProgressItem("Learned vocabulary", fontSize = 13.sp)
                    ProgressItem("All saved preferences", fontSize = 13.sp)
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Button(
                    onClick = { showConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FlagRed,
                        contentColor = colorResource(id = R.color.white)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Reset All Data",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        
        // Only show Debug Mode Section in debug builds
        if (BuildConfig.DEBUG) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Debug Mode Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(id = R.color.white)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = colorResource(id = R.color.primary_purple).copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⚙",
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Developer Tools",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(id = R.color.purple_black)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Debug Mode",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colorResource(id = R.color.purple_black)
                            )
                            Text(
                                text = "Enable developer tools and debug features",
                                fontSize = 14.sp,
                                color = colorResource(id = R.color.purple_black).copy(alpha = 0.7f),
                                lineHeight = 18.sp
                            )
                            if (debugModeEnabled) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Bonus credits enabled",
                                    fontSize = 12.sp,
                                    color = GreenMain,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        Switch(
                            checked = debugModeEnabled,
                            onCheckedChange = { enabled ->
                                debugModeEnabled = enabled
                                saveDebugMode(context, enabled)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colorResource(id = R.color.white),
                                checkedTrackColor = colorResource(id = R.color.primary_purple),
                                uncheckedThumbColor = colorResource(id = R.color.white),
                                uncheckedTrackColor = colorResource(id = R.color.purple_black).copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        }
        
        // Only show Account Section if sign-in is enabled
        if (BuildConfig.ENABLE_SIGN_IN) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Account Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(id = R.color.white)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = colorResource(id = R.color.primary_purple).copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "👤",
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Account",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(id = R.color.purple_black)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (currentUser != null) {
                        // Show user email and sign out button for authenticated users
                        Text(
                            text = "Signed in as ${currentUser?.email ?: "Unknown"}",
                            fontSize = 14.sp,
                            color = colorResource(id = R.color.purple_black).copy(alpha = 0.7f),
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showSignOutDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.purple_black).copy(alpha = 0.1f),
                                contentColor = colorResource(id = R.color.purple_black)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Sign Out",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        // Show sign in button for anonymous users
                        Text(
                            text = "Continue learning as a guest or sign in to sync your progress",
                            fontSize = 14.sp,
                            color = colorResource(id = R.color.purple_black).copy(alpha = 0.7f),
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onSignIn,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.primary_purple),
                                contentColor = colorResource(id = R.color.white)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Sign In",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
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
            color = colorResource(id = R.color.purple_black).copy(alpha = 0.8f)
        )
    }
    
    // Confirmation dialog for clearing progress
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Clear All Progress?") },
            text = { 
                Text("This action cannot be undone. All your progress, badges, and learned words will be permanently deleted.", color = colorResource(id = R.color.purple_black))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        clearAllProgress(context)
                        showConfirmDialog = false
                        onProgressCleared()
                        // Don't call onBack() - let the parent handle the welcome screen redirect
                    }
                ) {
                    Text("Clear", color = FlagRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Sign out confirmation dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out?") },
            text = { 
                Text("You will need to sign in again to access your account.", color = colorResource(id = R.color.purple_black))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        showSignOutDialog = false
                        // AuthGate will automatically detect sign-out and show SignInScreen
                    }
                ) {
                    Text("Sign Out", color = colorResource(id = R.color.primary_purple))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProgressItem(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit = 12.sp
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(4.dp)
                .background(
                    color = colorResource(id = R.color.purple_black).copy(alpha = 0.3f),
                    shape = RoundedCornerShape(2.dp)
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = fontSize,
            color = colorResource(id = R.color.purple_black).copy(alpha = 0.6f)
        )
    }
}

// Helper functions for flashcard tutorial
fun hasSeenFlashcardTutorial(context: Context): Boolean {
    val prefs = context.getSharedPreferences("flashcard_tutorial", Context.MODE_PRIVATE)
    return prefs.getBoolean("has_seen_tutorial", false)
}

fun markTutorialSeen(context: Context) {
    val prefs = context.getSharedPreferences("flashcard_tutorial", Context.MODE_PRIVATE)
    prefs.edit().putBoolean("has_seen_tutorial", true).apply()
}

// Session-based tutorial memory
private var sessionTutorialSeen = false

fun getSessionTutorialSeen(): Boolean = sessionTutorialSeen
fun setSessionTutorialSeen() { sessionTutorialSeen = true }

@Composable
fun FlashcardsExercise(
    words: List<WordEntry>,
    languageCode: String,
    onDone: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val wordIndices = remember(words) { words.indices.toList() }
    var cardPile by remember(words) { mutableStateOf(words.zip(wordIndices).shuffled()) }
    var doneWords by remember(words, languageCode) { mutableStateOf(setOf<Int>()) }
    var showCompletion by remember(words, languageCode) { mutableStateOf(false) }
    var isProcessingSwipe by remember(words, languageCode) { mutableStateOf(false) }
    var textColor by remember(words, languageCode) { mutableStateOf(Color.Unspecified) }
    var swipeDirection by remember(words, languageCode) { mutableStateOf<String?>(null) }
    var showTutorial by remember(words, languageCode) { mutableStateOf(!hasSeenFlashcardTutorial(context) && !getSessionTutorialSeen()) }
    
    val currentCard = cardPile.firstOrNull()
    val currentWord = currentCard?.first
    val currentIndex = currentCard?.second ?: -1
    
    // Function to handle swipe feedback with delay (like HiraKata)
    fun moveToNextCard(success: Boolean) {
        if (isProcessingSwipe) return
        isProcessingSwipe = true
        
        // Set visual feedback
        textColor = if (success) Color(0xFF4CAF50) else Color(0xFFD14C4C) // Green for success, red for failure
        swipeDirection = if (success) "right" else "left"
        
        // Delay before moving to next card (like HiraKata)
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            delay(500)
            val card = cardPile.firstOrNull()
            if (card != null) {
                val (_, index) = card
                if (success) {
                    // Mark as done - remove from pile permanently
                    doneWords = doneWords + index
                    cardPile = cardPile.drop(1)
                } else {
                    // Skip this word - move it to the bottom of the pile (only if not done)
                    cardPile = cardPile.drop(1)
                    // Only add back to pile if not already marked as done
                    if (index !in doneWords) {
                        cardPile = cardPile + card
                    }
                }
            }
            // Reset visual feedback
            textColor = Color.Unspecified
            swipeDirection = null
            isProcessingSwipe = false
        }
    }
    
    // When pile is empty, all words are done
    LaunchedEffect(cardPile.isEmpty()) {
        if (cardPile.isEmpty() && !showCompletion) {
            showCompletion = true
            onDone(true) // Perfect score since user worked through all cards
        }
    }
    
    // Auto-play audio when new card appears
    LaunchedEffect(currentCard) {
        currentCard?.let { card ->
            val word = card.first
            val wordVariant = word.byLang[languageCode]
            val audioFile = wordVariant?.audio
            
            if (audioFile != null) {
                delay(200) // Small delay to let card appear first
                playAssetAudio(context, audioFile)
            }
        }
    }
    
    
    // If pile is empty, render an empty box to allow parent to show completion screen
    if (cardPile.isEmpty() && showCompletion) {
        Box(modifier = Modifier.fillMaxSize())
        return
    }
    
    if (currentWord != null) {
        val wordVariant = currentWord.byLang[languageCode]
        val pronunciation = wordVariant?.googlePronunciation ?: (wordVariant?.word ?: "")
        val audioFile = wordVariant?.audio
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp) // Add bottom padding to account for debug button
        ) {
            // Top section - Progress indicator
            Column(
                modifier = Modifier.align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))
                
                // Progress indicator
                Text(
                    text = "${doneWords.size} / ${words.size} cards completed",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(id = R.color.purple_black).copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            // Center section - Flashcard (PERFECTLY CENTERED)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center), // This centers it PERFECTLY
                contentAlignment = Alignment.Center
            ) {
                SwipeableCard(
                    onSwipeLeft = {
                        moveToNextCard(false) // Skip - show red feedback
                    },
                    onSwipeRight = {
                        moveToNextCard(true) // Got it - show green feedback
                    }
                ) { swipeDirection, dragProgress, currentDragDirection ->
                    var isFlipped by remember(currentCard) { mutableStateOf(false) }
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .aspectRatio(1.2f)
                            .clickable { 
                                isFlipped = !isFlipped
                                // Play audio when flipping back to pronunciation (front of card)
                                if (!isFlipped && audioFile != null) {
                                    playAssetAudio(context, audioFile)
                                }
                            }
                            .border(
                                width = 8.dp,
                                color = colorResource(id = R.color.primary_purple).copy(alpha = 0.6f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .align(Alignment.Center), // Explicitly center the card
                        colors = CardDefaults.cardColors(
                            containerColor = if (textColor == Color.Unspecified) {
                                colorResource(id = R.color.white)
                            } else if (textColor == Color(0xFF4CAF50)) {
                                Color(0xFFE8F5E8) // Light green background for correct
                            } else {
                                Color(0xFFFFEBEE) // Light red background for incorrect
                            }
                        ),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) { // This Box is for the color overlay
                            // Color feedback exactly like hirakata - no card movement, just overlay
                            if (swipeDirection != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            color = when (swipeDirection) {
                                                "right" -> Color(0xFF4CAF50).copy(alpha = 0.15f) // More prominent green
                                                "left" -> Color(0xFFD14C4C).copy(alpha = 0.15f) // More prominent red
                                                else -> Color.Transparent
                                            }
                                        )
                                )
                            } else if (dragProgress > 0.1f && currentDragDirection != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            color = when (currentDragDirection) {
                                                "right" -> Color(0xFF4CAF50).copy(alpha = dragProgress * 0.12f) // More prominent green
                                                "left" -> Color(0xFFD14C4C).copy(alpha = dragProgress * 0.12f) // More prominent red
                                                else -> Color(0xFFCCCCCC).copy(alpha = dragProgress * 0.1f)
                                            }
                                        )
                                )
                            }
                            
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (isFlipped) {
                                    // Translation (back of card) - always use English translation
                                    val translation = currentWord?.byLang?.get("en")?.word ?: currentWord?.original ?: "Translation not available"
                                    Text(
                                        text = translation,
                                        fontSize = 40.sp,
                                        fontFamily = getAppropriateFontFamily(translation),
                                        color = if (textColor == Color.Unspecified) colorResource(id = R.color.primary_purple) else textColor,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                    
                                    // Tap instruction for back
                                    Text(
                                        text = "Tap to see pronunciation",
                                        fontSize = 14.sp,
                                        color = colorResource(id = R.color.purple_black).copy(alpha = 0.6f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                } else {
                                    // Pronunciation (front of card)
                                    Text(
                                        text = pronunciation,
                                        fontSize = 36.sp,
                                        lineHeight = 44.sp,
                                        fontFamily = getAppropriateFontFamily(pronunciation),
                                        color = if (textColor == Color.Unspecified) colorResource(id = R.color.purple_black) else textColor,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                    
                                    // Tap instruction for front
                                    Text(
                                        text = "Tap to see translation",
                                        fontSize = 14.sp,
                                        color = colorResource(id = R.color.purple_black).copy(alpha = 0.6f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Bottom section - Action buttons
            Column(
                modifier = Modifier.align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Bumble-style action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    // Again button (swipe left equivalent)
                    Button(
                        onClick = {
                            moveToNextCard(false) // Skip - show red feedback
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.purple_black).copy(alpha = 0.1f),
                            contentColor = colorResource(id = R.color.purple_black)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "AGAIN",
                            fontSize = 16.sp,
                            fontFamily = getAppropriateFontFamily("AGAIN"),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Got it button (swipe right equivalent)
                    Button(
                        onClick = {
                            moveToNextCard(true) // Got it - show green feedback
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.primary_purple),
                            contentColor = colorResource(id = R.color.white)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "GOT IT",
                            fontSize = 16.sp,
                            fontFamily = getAppropriateFontFamily("GOT IT"),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    // Tutorial dialog
    if (showTutorial) {
        AlertDialog(
            onDismissRequest = { showTutorial = false },
            title = {
                Text(
                    text = "How to Use Flashcards",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.purple_black)
                )
            },
            text = {
                Column {
                    Text(
                        text = "• Tap the card to see the translation",
                        fontSize = 16.sp,
                        color = colorResource(id = R.color.purple_black),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "• Swipe right or tap 'Got it' if you remember the word",
                        fontSize = 16.sp,
                        color = colorResource(id = R.color.purple_black),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "• Swipe left or tap 'Again' if you want to review it",
                        fontSize = 16.sp,
                        color = colorResource(id = R.color.purple_black),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "• Audio plays automatically for each word",
                        fontSize = 16.sp,
                        color = colorResource(id = R.color.purple_black)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showTutorial = false
                        setSessionTutorialSeen() // Mark tutorial as seen for this session
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.primary_purple),
                        contentColor = colorResource(id = R.color.white)
                    )
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showTutorial = false
                        markTutorialSeen(context)
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorResource(id = R.color.purple_black)
                    )
                ) {
                    Text("Don't show again")
                }
            }
        )
    }
    
    // Note: When cardPile becomes empty (all cards processed), the LaunchedEffect triggers
    // completion and the early return at the top shows the completion screen.
}

@Composable
fun SwipeableCard(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    content: @Composable (swipeDirection: String?, dragProgress: Float, currentDragDirection: String?) -> Unit
) {
    var totalDragX by remember { mutableStateOf(0f) }
    var dragProgress by remember { mutableStateOf(0f) }
    var currentDragDirection by remember { mutableStateOf<String?>(null) }
    var isProcessingSwipe by remember { mutableStateOf(false) }
    var swipeDirection by remember { mutableStateOf<String?>(null) } // This will be set on swipe completion

    // Reset swipe direction after a delay, simulating hirakata's behavior
    LaunchedEffect(swipeDirection) {
        if (swipeDirection != null) {
            delay(500) // This matches the 500ms delay in hirakata before resetting visual feedback
            swipeDirection = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize() // Ensure the box fills the space to capture gestures
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        totalDragX = 0f
                        dragProgress = 0f
                        currentDragDirection = null
                        swipeDirection = null // Reset swipeDirection on new drag start
                        isProcessingSwipe = false
                    },
                    onDragEnd = {
                        if (!isProcessingSwipe) { // Only process if not already processed
                            if (totalDragX > 100f) { // Right swipe threshold
                                swipeDirection = "right"
                                onSwipeRight()
                            } else if (totalDragX < -100f) { // Left swipe threshold
                                swipeDirection = "left"
                                onSwipeLeft()
                            }
                        }
                        // Reset drag states regardless of whether a swipe occurred
                        totalDragX = 0f
                        dragProgress = 0f
                        currentDragDirection = null
                        isProcessingSwipe = false // Ensure it's reset
                    },
                    onDragCancel = {
                        totalDragX = 0f
                        dragProgress = 0f
                        currentDragDirection = null
                        swipeDirection = null
                        isProcessingSwipe = false
                    },
                    onDrag = { _, dragAmount ->
                        val (x, y) = dragAmount
                        totalDragX += x

                        // Only proceed if not already processing a swipe
                        if (!isProcessingSwipe) {
                            // Calculate dragProgress for visual feedback
                            dragProgress = (kotlin.math.abs(totalDragX) / 100f).coerceAtMost(1f)

                            // Determine current drag direction for immediate feedback
                            if (kotlin.math.abs(totalDragX) > 35) { // Threshold for determining direction
                                currentDragDirection = if (totalDragX > 0) "right" else "left"
                            } else {
                                currentDragDirection = null // No clear direction yet
                            }

                            // Trigger swipe completion if thresholds are met, matching hirakata's logic
                            // Hirakata uses a combination of totalDragX > 100 and horizontal dominance
                            if (kotlin.math.abs(totalDragX) > 100 && kotlin.math.abs(totalDragX) > kotlin.math.abs(y) * 1.5) {
                                isProcessingSwipe = true // Set to true to prevent further drag processing
                                if (totalDragX > 0) {
                                    swipeDirection = "right"
                                    onSwipeRight()
                                } else {
                                    swipeDirection = "left"
                                    onSwipeLeft()
                                }
                            }
                        }
                    }
                )
            }
    ) {
        // Pass swipe direction and drag progress to content for rendering background
        content(swipeDirection, dragProgress, currentDragDirection)
    }
}

@Composable
fun HelloGoodbyeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = colorResource(id = R.color.primary_purple),
            primaryContainer = colorResource(id = R.color.primary_container_purple),
            secondaryContainer = colorResource(id = R.color.secondary_container_purple),
            background = colorResource(id = R.color.background_light_purple),
            surface = colorResource(id = R.color.surface_light_purple)
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
    var selected by remember(options, correctOption, pronunciation) { mutableStateOf<String?>(null) }
    var completed by remember(options, correctOption, pronunciation) { mutableStateOf(false) }

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
            modifier = Modifier,
            textColor = colorResource(id = R.color.white)
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
                val isCorrectAnswer = completed && option == correctOption && selected != correctOption
                PracticeBubble(
                    label = option,
                    selected = isSelected,
                    solved = isCorrectSelected || isCorrectAnswer,
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
                    containerColor = if (isCorrect) GreenMain else FlagRed
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
    var input by remember(correctAnswer, pronunciation) { mutableStateOf("") }
    var completed by remember(correctAnswer, pronunciation) { mutableStateOf(false) }
    var isCorrect by remember(correctAnswer, pronunciation) { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(audioFile) {
        if (audioFile != null) {
            playAssetAudio(context, audioFile)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PronunciationWordBubble(
            text = pronunciation,
            onClick = { if (audioFile != null) playAssetAudio(context, audioFile) },
            modifier = Modifier,
            textColor = colorResource(id = R.color.white)
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
                    color = FlagRed
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
                    containerColor = FlagRed
                )
            }
        }
    }
}