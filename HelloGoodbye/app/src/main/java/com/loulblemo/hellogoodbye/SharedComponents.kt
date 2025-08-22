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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(selectedCountries) { country ->
            // Flag item
            Card(
                modifier = Modifier
                    .aspectRatio(1f)
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
                    Text(
                        text = country.flag,
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = country.language,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
    Row(
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
    
    // Debug effect to track completion
    LaunchedEffect(remaining, completed) {
        if (remaining == 0 && !completed && pairs.isNotEmpty()) {
            completed = true
            onDone(mistakes == 0)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Remaining: $remaining")
            Text("Mistakes: $mistakes")
        }
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
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

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