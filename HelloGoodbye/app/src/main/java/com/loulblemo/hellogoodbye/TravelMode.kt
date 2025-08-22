package com.loulblemo.hellogoodbye

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TravelScreen(
    startLanguageCode: String,
    onExit: () -> Unit,
    onAwardCoin: () -> Unit
) {
    val context = LocalContext.current
    val corpus by remember { mutableStateOf(loadCorpusFromAssets(context)) }
    val supportedLangCodes = remember(corpus) {
        corpus.flatMap { it.byLang.keys }.distinct()
    }
    val travelSections = remember(supportedLangCodes, startLanguageCode) {
        generateTravelSequenceForLanguage(startLanguageCode, supportedLangCodes)
    }
    val questExercises = remember(travelSections) {
        travelSections.associate { section ->
            val list = generateQuestExercisesForSection(section, 10)
            val filtered = if (section.isMixed) list else list.filter { it.id != "audio_to_flag" && it.id != "pronunciation_to_flag" }
            section.id to filtered
        }
    }
    var travelState by remember { mutableStateOf(initializeTravelState(context, travelSections, questExercises)) }
    
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
                text = "Travel Mode",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onExit) { Text("Close") }
        }
        
        if (travelState.currentQuestId == null) {
            // Travel quest list view
            TravelQuestListScreen(
                travelSections = travelSections,
                travelState = travelState,
                questExercises = questExercises,
                onQuestClick = { questId ->
                    travelState = travelState.copy(
                        currentQuestId = questId,
                        currentExerciseIndex = 0
                    )
                }
            )
        } else {
            // Quest practice view
            val currentSection = travelSections.find { it.id == travelState.currentQuestId }
            if (currentSection != null) {
                QuestPracticeScreen(
                    section = currentSection,
                    travelState = travelState,
                    questExercises = questExercises[currentSection.id] ?: emptyList(),
                    onExerciseComplete = { completedStepKey ->
                        // Track encountered words from this exercise
                        val languages = if (currentSection.isMixed) {
                            currentSection.languages
                        } else {
                            listOf(languageNameToCode(currentSection.language) ?: "en")
                        }
                        val langCodes = languages.filterNotNull()
                        trackWordsFromExercise(context, currentSection, corpus, langCodes)
                        
                        // Track badge progress for each language involved
                        langCodes.forEach { languageCode ->
                            incrementLanguageExerciseCount(context, languageCode)
                        }
                        
                        travelState = updateQuestProgress(
                            context,
                            travelState,
                            currentSection.id,
                            completedStepKey,
                            travelSections,
                            questExercises
                        )
                        onAwardCoin()
                        
                        val currentProgress = travelState.questProgresses[currentSection.id]
                        if (currentProgress?.isCompleted == true) {
                            if (currentSection.id.endsWith("_1")) {
                                markFirstQuestCompleted(context, startLanguageCode)
                            }
                            // Quest completed, return to list
                            travelState = travelState.copy(currentQuestId = null)
                        } else {
                            // Move to next exercise immediately
                            val total = questExercises[currentSection.id]?.size ?: 10
                            val nextIndex = travelState.currentExerciseIndex + 1
                            travelState = if (nextIndex < total) {
                                travelState.copy(currentExerciseIndex = nextIndex)
                            } else {
                                travelState
                            }
                        }
                    },
                    onDebugCompleteQuest = {
                        // Debug: Complete all exercises in this quest
                        val allExerciseIds = questExercises[currentSection.id]?.mapIndexed { index, _ ->
                            "${currentSection.id}#${index}"
                        } ?: emptyList()
                        
                        // Mark all exercises as completed
                        allExerciseIds.forEach { exerciseId ->
                            travelState = updateQuestProgress(
                                context,
                                travelState,
                                currentSection.id,
                                exerciseId,
                                travelSections,
                                questExercises
                            )
                        }
                        
                        // Award coins for all exercises
                        repeat(allExerciseIds.size) { onAwardCoin() }
                        
                        // Track badge progress for debug completion
                        val languages = if (currentSection.isMixed) {
                            currentSection.languages
                        } else {
                            listOf(languageNameToCode(currentSection.language) ?: "en")
                        }
                        val langCodes = languages.filterNotNull()
                        repeat(allExerciseIds.size) {
                            langCodes.forEach { languageCode ->
                                incrementLanguageExerciseCount(context, languageCode)
                            }
                        }
                        
                        // Mark first quest completed if applicable
                        if (currentSection.id.endsWith("_1")) {
                            markFirstQuestCompleted(context, startLanguageCode)
                        }
                        
                        // Quest completed, return to list
                        travelState = travelState.copy(currentQuestId = null)
                    },
                    onBack = { 
                        travelState = travelState.copy(currentQuestId = null)
                    }
                )
            }
        }
    }
}

@Composable
fun TravelQuestListScreen(
    travelSections: List<TravelSection>,
    travelState: TravelState,
    questExercises: Map<String, List<ExerciseType>>,
    onQuestClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "Your Language Journey",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        val unlockedSections = travelSections.filter { section ->
            travelState.questProgresses[section.id]?.isUnlocked == true
        }
        
        items(unlockedSections) { section ->
            CircleQuestBubble(
                section = section,
                questProgress = travelState.questProgresses[section.id],
                onClick = { onQuestClick(section.id) }
            )
        }
        
        // Add plus button if all sections are completed
        if (travelSections.all { section ->
            travelState.questProgresses[section.id]?.isCompleted == true
        }) {
            item {
                Card(
                    modifier = Modifier
                        .size(120.dp)
                        .clickable { /* TODO: Add new language selection */ },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    shape = CircleShape
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add new language",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
                Text(
                    text = "Add Language",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun CircleQuestBubble(
    section: TravelSection,
    questProgress: QuestProgress?,
    onClick: () -> Unit
) {
    val isCompleted = questProgress?.isCompleted == true
    val isUnlocked = questProgress?.isUnlocked == true
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            // Yellow circle background
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(enabled = isUnlocked) { onClick() },
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = CircleShape,
                border = BorderStroke(4.dp, Color(0xFFFFD700)) // Yellow border
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Flag emoji
                    Text(
                        text = section.flag,
                        fontSize = 60.sp
                    )
                }
            }
            
            // Green checkmark overlay when completed
            if (isCompleted) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50).copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        fontSize = 48.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Language name only
        Text(
            text = section.name,
            style = MaterialTheme.typography.titleMedium,
            color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TravelQuestCard(
    section: TravelSection,
    questProgress: QuestProgress?,
    onClick: () -> Unit
) {
    val exerciseTypes = getExerciseTypesForSection(section)
    val completedCount = questProgress?.completedExercises?.size ?: 0
    val isCompleted = questProgress?.isCompleted == true
    val isUnlocked = questProgress?.isUnlocked == true
    
    val bgColor = when {
        isCompleted -> MaterialTheme.colorScheme.secondaryContainer
        isUnlocked -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val borderColor = if (isCompleted) Color(0xFF4CAF50) else Color.Transparent
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(enabled = isUnlocked) { onClick() },
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp),
        border = if (isCompleted) BorderStroke(3.dp, borderColor) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quest icon/flag
            Box(
                modifier = Modifier.size(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = section.flag,
                    fontSize = 32.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Quest info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = section.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (section.isMixed) {
                    Text(
                        text = "Mixed Languages",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = section.language,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Progress indicator
                if (isUnlocked && !isCompleted) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Progress: $completedCount/${exerciseTypes.size} exercises",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Completion status
            if (isCompleted) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else if (isUnlocked) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▶",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun QuestPracticeScreen(
    section: TravelSection,
    travelState: TravelState,
    questExercises: List<ExerciseType>,
    onExerciseComplete: (String) -> Unit,
    onDebugCompleteQuest: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val corpus by remember { mutableStateOf(loadCorpusFromAssets(context)) }
    val exerciseTypes = questExercises
    val currentProgress = travelState.questProgresses[section.id]
    val currentExerciseIndex = travelState.currentExerciseIndex
    
    val languages = if (section.isMixed) {
        section.languages
    } else {
        listOf(languageNameToCode(section.language) ?: "en")
    }
    val languageCodes = languages.filterNotNull()
    
    val currentExercise = exerciseTypes.getOrNull(currentExerciseIndex)
    val stepKey = remember(section.id, currentExerciseIndex) { "${section.id}#${currentExerciseIndex}" }
    
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
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = section.flag,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = section.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (currentExercise != null) {
                    Text(
                        text = "Exercise ${currentExerciseIndex + 1}/${exerciseTypes.size}: ${currentExercise.title}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            TextButton(onClick = onBack) { Text("Back") }
        }
        
        // Progress indicator
        LinearProgressIndicator(
            progress = (currentExerciseIndex + 1).toFloat() / exerciseTypes.size,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (corpus.isEmpty() || languageCodes.isEmpty() || currentExercise == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No content available for this exercise")
            }
            return
        }
        
        val threeWords = remember(corpus) {
            corpus.shuffled().take(3)
        }
        val eligibleAudioWords = remember(corpus, languageCodes) {
            corpus.filter { entry -> languageCodes.any { code -> entry.byLang[code]?.audio != null } }
        }
        val eligiblePronunciationWords = remember(corpus, languageCodes) {
            corpus.filter { entry -> languageCodes.any { code ->
                val v = entry.byLang[code]
                (v?.googlePronunciation ?: v?.ipa) != null && v?.audio != null
            } }
        }
        
        // Check if current exercise is already completed
        val isExerciseCompleted = currentProgress?.completedExercises?.contains(stepKey) == true
        
        // Exercise content with debug button at bottom
        Box(modifier = Modifier.fillMaxSize()) {
            // Main exercise content
            if (isExerciseCompleted) {
                // Already completed: parent will advance index. Keep UI minimal.
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Great! Moving on...")
                }
            } else {
                // Show the actual exercise
                val effectiveExerciseId = if (!section.isMixed && currentExercise.id == "audio_to_flag") {
                    "pronunciation_audio_to_english"
                } else currentExercise.id
                when (effectiveExerciseId) {
                    "audio_to_flag" -> {
                        val source = (if (eligibleAudioWords.isNotEmpty()) eligibleAudioWords else corpus).shuffled().take(10)
                        val pairs = buildAudioToFlagPairs(source, languageCodes)
                        MatchingExercise(
                            title = currentExercise.title,
                            pairs = pairs,
                            onDone = { perfect ->
                                onExerciseComplete(stepKey)
                            }
                        )
                    }
                    "pronunciation_to_flag" -> {
                        val pairs = buildPronunciationToFlagPairs(threeWords, languageCodes)
                        MatchingExercise(
                            title = currentExercise.title,
                            pairs = pairs,
                            onDone = { perfect ->
                                onExerciseComplete(stepKey)
                            }
                        )
                    }
                    "audio_to_english" -> {
                        val source = (if (eligibleAudioWords.isNotEmpty()) eligibleAudioWords else corpus).shuffled().take(10)
                        val pairs = buildAudioToEnglishPairs(source, languageCodes)
                        MatchingExercise(
                            title = currentExercise.title,
                            pairs = pairs,
                            onDone = { perfect ->
                                onExerciseComplete(stepKey)
                            }
                        )
                    }
                    "pronunciation_audio_to_english" -> {
                        val pool = if (eligiblePronunciationWords.isNotEmpty()) eligiblePronunciationWords else corpus
                        // pick one word and build 5 options
                        val chosen = pool.random()
                        val code = languageCodes.firstOrNull { chosen.byLang[it]?.audio != null && ((chosen.byLang[it]?.googlePronunciation ?: chosen.byLang[it]?.ipa) != null) } ?: languageCodes.first()
                        val variant = chosen.byLang[code]
                        val pronunciation = (variant?.googlePronunciation ?: variant?.ipa) ?: ""
                        val correctEnglish = chosen.byLang["en"]?.word ?: chosen.original
                        val distractors = pool.filter { it !== chosen }
                            .mapNotNull { it.byLang["en"]?.word ?: it.original }
                            .shuffled()
                            .distinct()
                            .take(4)
                        val options = (distractors + correctEnglish).shuffled()
                        PronunciationAudioToEnglishExercise(
                            title = currentExercise.title,
                            pronunciation = pronunciation,
                            audioFile = variant?.audio,
                            options = options,
                            correctOption = correctEnglish,
                            onDone = { _ ->
                                onExerciseComplete(stepKey)
                            }
                        )
                    }
                }
            }
            
            // Debug button at bottom
            Button(
                onClick = onDebugCompleteQuest,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "🐛 DEBUG: Complete Quest",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}