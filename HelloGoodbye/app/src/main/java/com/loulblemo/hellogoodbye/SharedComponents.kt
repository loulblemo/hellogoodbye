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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                        containerColor = MaterialTheme.colorScheme.surface
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
                // Plus button
                Card(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable(enabled = canAffordAdd) { onAddFlag() },
                    colors = CardDefaults.cardColors(
                        containerColor = if (canAffordAdd) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add flag",
                                modifier = Modifier.size(32.dp),
                                tint = if (canAffordAdd) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (price > 0) "$price" else "",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
            .size(32.dp)
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
        // Outer cross (darker red)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 3.dp.toPx()
            val color = Color(0xFFB71C1C) // Darker red
            
            // Draw X with two lines
            drawLine(
                color = color,
                start = Offset(8.dp.toPx(), 8.dp.toPx()),
                end = Offset(24.dp.toPx(), 24.dp.toPx()),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = color,
                start = Offset(24.dp.toPx(), 8.dp.toPx()),
                end = Offset(8.dp.toPx(), 24.dp.toPx()),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
        
        // Inner cross (lighter red) - slightly smaller
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 2.dp.toPx()
            val color = Color(0xFFF44336) // Lighter red
            
            // Draw X with two lines (slightly smaller)
            drawLine(
                color = color,
                start = Offset(10.dp.toPx(), 10.dp.toPx()),
                end = Offset(22.dp.toPx(), 22.dp.toPx()),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = color,
                start = Offset(22.dp.toPx(), 10.dp.toPx()),
                end = Offset(10.dp.toPx(), 22.dp.toPx()),
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
    color: Color = Color(0xFF4CAF50)
) {
    Canvas(modifier = modifier.size(size.dp)) {
        val strokeWidth = this.size.minDimension * 0.08f // Proportional stroke width in px
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
    size: Float = 120f
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(
                Color(0xFF4CAF50).copy(alpha = 0.95f),
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
            size = size * 0.4f,
            color = Color.White
        )
    }
}

@Composable
fun PracticeButtonSection(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = "PRACTICE",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PracticeAndTravelButtonsSection(onPracticeClick: () -> Unit, onTravelClick: () -> Unit, travelEnabled: Boolean = true) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Single full-width Practice button; travel removed from main screen
        PracticeButtonSection(
            onClick = onPracticeClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = travelEnabled
        )
        
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
    onDone: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var mistakes by remember(pairs) { mutableStateOf(0) }
    var remaining by remember(pairs) { mutableStateOf(pairs.size) }
    var leftSelection by remember(pairs) { mutableStateOf<String?>(null) }
    var rightSelection by remember(pairs) { mutableStateOf<String?>(null) }
    var completed by remember(pairs) { mutableStateOf(false) }

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
                leftSelection = null
                rightSelection = null
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
                        onClick = {
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
                    PracticeBubble(
                        label = item.label,
                        selected = rightSelection == item.id,
                        solved = solved,
                        onClick = {
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
fun PracticeBubble(
    label: String,
    selected: Boolean,
    solved: Boolean,
    onClick: () -> Unit
) {
    val bg = when {
        solved -> MaterialTheme.colorScheme.secondaryContainer
        selected -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = bg),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(label)
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
        colorScheme = lightColorScheme(),
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

        // Pronunciation display; tap to replay audio
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(80.dp)
                .clickable { if (audioFile != null) playAssetAudio(context, audioFile) },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = pronunciation,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = selected == option
                PracticeBubble(
                    label = option,
                    selected = isSelected,
                    solved = completed && option == correctOption,
                    onClick = {
                        if (completed) return@PracticeBubble
                        selected = option
                        completed = true
                        onDone(option == correctOption)
                    }
                )
            }
        }
    }
}