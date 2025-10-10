package com.loulblemo.hellogoodbye

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.ui.geometry.CornerRadius
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.decode.SvgDecoder
import coil.ImageLoader

// Helper function to get encountered words for mixed exercises
private fun getEncounteredWordsForMixed(
    context: Context,
    corpus: List<WordEntry>,
    languageCodes: List<String>
): List<WordEntry> {
    val encounteredWords = mutableSetOf<WordEntry>()
    
    // For mixed exercises, we want to include words from both languages
    // but we need to ensure we have words from both languages represented
    val wordsFromLanguage1 = mutableSetOf<WordEntry>()
    val wordsFromLanguage2 = mutableSetOf<WordEntry>()
    
    if (languageCodes.size >= 2) {
        val lang1 = languageCodes[0]
        val lang2 = languageCodes[1]
        
        // Get words from first language
        val encounteredWordsForLang1 = getEncounteredWords(context, lang1)
        corpus.forEach { wordEntry ->
            val variant = wordEntry.byLang[lang1]
            if (variant != null) {
                val word = variant.word ?: variant.text ?: wordEntry.original
                if (encounteredWordsForLang1.contains(word)) {
                    wordsFromLanguage1.add(wordEntry)
                }
            }
        }
        
        // Get words from second language
        val encounteredWordsForLang2 = getEncounteredWords(context, lang2)
        corpus.forEach { wordEntry ->
            val variant = wordEntry.byLang[lang2]
            if (variant != null) {
                val word = variant.word ?: variant.text ?: wordEntry.original
                if (encounteredWordsForLang2.contains(word)) {
                    wordsFromLanguage2.add(wordEntry)
                }
            }
        }
        
        // Combine words from both languages, ensuring we have representation from both
        encounteredWords.addAll(wordsFromLanguage1)
        encounteredWords.addAll(wordsFromLanguage2)
    } else {
        // Fallback to single language logic
        languageCodes.forEach { langCode ->
            val encounteredWordsForLang = getEncounteredWords(context, langCode)
            corpus.forEach { wordEntry ->
                val variant = wordEntry.byLang[langCode]
                if (variant != null) {
                    val word = variant.word ?: variant.text ?: wordEntry.original
                    if (encounteredWordsForLang.contains(word)) {
                        encounteredWords.add(wordEntry)
                    }
                }
            }
        }
    }
    
    return if (encounteredWords.isNotEmpty()) {
        encounteredWords.toList()
    } else {
        corpus // Fallback if no encountered words
    }
}

// Update travel sections with dynamic mixed language selection based on quest progress
private fun updateTravelSectionsWithMixedLanguages(
    context: Context,
    basicSections: List<TravelSection>,
    questProgresses: Map<String, QuestProgress>,
    allLangCodes: List<String>
): List<TravelSection> {
    return basicSections.map { section ->
        if (section.isMixed) {
            // Check if this quest is completed and has saved languages
            val questProgress = questProgresses[section.id]
            val isCompleted = questProgress?.isCompleted == true
            val savedLanguages = questProgress?.languagesUsed ?: emptyList()
            
            if (isCompleted && savedLanguages.isNotEmpty()) {
                // Use the saved languages from when the quest was completed
                val startLangCode = section.id.split("_")[0]
                val additionalLangNames = if (savedLanguages.size > 1) {
                    savedLanguages.drop(1).joinToString(" + ") { langCode ->
                        languageCodeToName(langCode) ?: langCode
                    }
                } else {
                    "Mixed"
                }
                
                section.copy(
                    languages = savedLanguages,
                    name = "Mixed: ${languageCodeToName(startLangCode) ?: startLangCode} + $additionalLangNames"
                )
            } else {
                // For incomplete mixed sections, work exactly like practice mode
                val startLangCode = section.id.split("_")[0] // Extract start language from section ID
                val isLevel2Exercise3 = section.id.endsWith("level2_exercise3")
                
                val availableLanguagesForMixed = mutableListOf<String>()
                availableLanguagesForMixed.add(startLangCode)
                
                // Find other languages that have at least bronze medal (completed at least 1 quest)
                val languagesWithBronzeMedal = allLangCodes.filter { langCode ->
                    langCode != startLangCode && 
                    langCode != "en" && 
                    getLanguageQuestCount(context, langCode) >= 1
                }
                
                if (isLevel2Exercise3) {
                    // Level 2 Exercise 3 needs 3 languages total
                    if (languagesWithBronzeMedal.size >= 2) {
                        // Randomly select 2 additional languages
                        availableLanguagesForMixed.addAll(languagesWithBronzeMedal.shuffled().take(2))
                    }
                } else {
                    // Other mixed quests need 2 languages total
                    if (languagesWithBronzeMedal.isNotEmpty()) {
                        availableLanguagesForMixed.add(languagesWithBronzeMedal.random())
                    }
                }
                
                val additionalLangNames = if (availableLanguagesForMixed.size > 1) {
                    availableLanguagesForMixed.drop(1).joinToString(" + ") { langCode ->
                        languageCodeToName(langCode) ?: langCode
                    }
                } else {
                    "Mixed"
                }
                
                section.copy(
                    languages = availableLanguagesForMixed,
                    name = "Mixed: ${languageCodeToName(startLangCode) ?: startLangCode} + $additionalLangNames"
                )
            }
        } else {
            section
        }
    }
}

// Check if a mixed section should be visible in progression (when previous quest is completed)
private fun isMixedSectionVisible(
    section: TravelSection,
    travelSections: List<TravelSection>,
    travelState: TravelState
): Boolean {
    if (!section.isMixed) return false
    
    // For mixed quests, show them when the previous quest is completed
    val mixedIndex = travelSections.indexOf(section)
    if (mixedIndex <= 0) return false
    
    val previousQuest = travelSections[mixedIndex - 1]
    val isPreviousCompleted = travelState.questProgresses[previousQuest.id]?.isCompleted == true
    
    return isPreviousCompleted
}

@Composable
fun TravelScreen(
    startLanguageCode: String,
    onExit: () -> Unit,
    onAwardCoin: () -> Unit
) {
    val context = LocalContext.current
    var currency by remember { mutableStateOf(loadCurrency(context)) }
    val corpus by remember { mutableStateOf(loadCorpusFromAssets(context)) }
    val supportedLangCodes = remember(corpus) {
        corpus.flatMap { it.byLang.keys }.distinct().filter { it != "en" }
    }
    // Quest recipes define type, language count, exercise order, randomization, and new words to seed
    val questRecipes = remember { defaultQuestRecipes() }
    // Phase 1: Generate basic travel sections
    val basicTravelSections = remember(supportedLangCodes, startLanguageCode) {
        generateTravelSectionsFromRecipes(startLanguageCode, questRecipes)
    }
    
    // Initialize travel state with basic sections
    var travelState by remember { 
        mutableStateOf(initializeTravelState(context, basicTravelSections, emptyMap()))
    }
    
    // Phase 2: Update travel sections with dynamic mixed language selection based on quest progress
    var travelSections = remember(basicTravelSections, travelState.questProgresses) {
        updateTravelSectionsWithMixedLanguages(context, basicTravelSections, travelState.questProgresses, supportedLangCodes)
    }
    
    val questExercises = remember(travelSections) {
        travelSections.associate { section ->
            if (section.isCompletionBadge) {
                // Completion badges have no exercises
                section.id to emptyList<ExerciseType>()
            } else {
                val recipe = recipeForQuestId(questRecipes, section.id)
                val ids = if (recipe != null) {
                    val base = recipe.exerciseOrder
                    if (recipe.randomOrder) base.shuffled() else base
                } else {
                    // Fallback if recipe not found
                    if (section.isMixed) listOf("audio_to_flag", "pronunciation_to_flag", "audio_to_english", "pronunciation_to_english_multi", "pronunciation_audio_to_english", "pronunciation_audio_to_type_english") else listOf("audio_to_english", "pronunciation_to_english_multi", "pronunciation_audio_to_english", "pronunciation_audio_to_type_english")
                }
                val types = ids.map { id -> mapExerciseIdToType(section, id) }
                section.id to types
            }
        }
    }
    
    // Update travel state when quest exercises change
    LaunchedEffect(questExercises) {
        if (travelState.questProgresses.isNotEmpty() && travelState.questProgresses.keys != questExercises.keys) {
            // Only update if the quest structure has changed
            travelState = travelState.copy()
        }
    }
    
    // Intercept system back while inside a quest: reset that quest and return to list
    BackHandler(enabled = travelState.currentQuestId != null) {
        val qid = travelState.currentQuestId
        if (qid != null) {
            travelState = resetQuestProgress(context, travelState, qid)
        }
    }

    // Flash animation for currency box
    var shouldFlash by remember { mutableStateOf(false) }
    val previousCurrency = remember { mutableStateOf(currency) }
    
    LaunchedEffect(currency) {
        android.util.Log.d("TRAVEL_FLASH", "Currency changed: $currency, previous: ${previousCurrency.value}")
        if (currency > previousCurrency.value) {
            android.util.Log.d("TRAVEL_FLASH", "Triggering flash!")
            shouldFlash = true
            delay(2000)
            shouldFlash = false
            android.util.Log.d("TRAVEL_FLASH", "Flash ended")
        }
        previousCurrency.value = currency
    }
    
    val currencyBoxColor = if (shouldFlash) {
        Color(0xFF4CAF50) // Green
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with coins, debug indicator, language name, and exit
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
                // Currency display (matches home top bar style)
                Card(
                    modifier = Modifier.size(60.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = currencyBoxColor
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
                
                // Debug mode indicator (only show when enabled)
                if (loadDebugMode(context)) {
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
            
            // Center: Language name
            val languageName = languageCodeToName(startLanguageCode) ?: startLanguageCode
            Text(
                text = languageName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            
            // Right side: Exit button
            ResponsiveRedCross(onClick = {
                val qid = travelState.currentQuestId
                if (qid != null) {
                    travelState = resetQuestProgress(context, travelState, qid)
                }
                onExit()
            })
        }
        
        if (travelState.currentQuestId == null) {
            // Travel quest list view
            TravelQuestListScreen(
                context = context,
                travelSections = travelSections,
                travelState = travelState,
                questExercises = questExercises,
                onQuestClick = { questId ->
                    // Always restart quest from the beginning: clear any partial progress first
                    travelState = resetQuestProgress(context, travelState, questId)
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
                    basicTravelSections = basicTravelSections,
                    supportedLangCodes = supportedLangCodes,
                    questRecipes = questRecipes,
                    onExerciseComplete = { completedStepKey, isPerfect ->
                        // Track encountered words from this exercise
                        val languages = if (currentSection.isMixed) {
                            currentSection.languages
                        } else {
                            listOf(languageNameToCode(currentSection.language) ?: startLanguageCode)
                        }
                        val langCodes = languages.filterNotNull()
                        trackWordsFromExercise(context, currentSection, corpus, langCodes)
                        
                        // Badge progress is tracked when quest is completed, not per exercise
                        
                        travelState = updateQuestProgress(
                            context,
                            travelState,
                            currentSection.id,
                            completedStepKey,
                            travelSections,
                            questExercises,
                            langCodes
                        )
                        // Only award coin if perfect (no mistakes)
                        if (isPerfect) {
                            onAwardCoin()
                        }
                        // Refresh local display from storage to avoid double-counting
                        currency = loadCurrency(context)
                        
                        val currentProgress = travelState.questProgresses[currentSection.id]
                        if (currentProgress?.isCompleted == true) {
                            if (currentSection.id.endsWith("_1")) {
                                markFirstQuestCompleted(context, startLanguageCode)
                            }
                            
                            // Track badge progress when quest is completed
                            langCodes.forEach { languageCode ->
                                incrementLanguageQuestCount(context, languageCode)
                            }
                            
                            // Update travel sections to reflect new progress (important for mixed quests)
                            travelSections = updateTravelSectionsWithMixedLanguages(
                                context,
                                basicTravelSections,
                                travelState.questProgresses,
                                supportedLangCodes
                            )
                            
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
                        // Debug: Complete all exercises in this quest - simulate real completion
                        val allExerciseIds = questExercises[currentSection.id]?.mapIndexed { index, _ ->
                            "${currentSection.id}#${index}"
                        } ?: emptyList()
                        
                        // Get languages used for this quest
                        val languages = if (currentSection.isMixed) {
                            currentSection.languages
                        } else {
                            listOf(languageNameToCode(currentSection.language) ?: startLanguageCode)
                        }
                        val langCodes = languages.filterNotNull()
                        
                        // Mark all exercises as completed
                        allExerciseIds.forEach { exerciseId ->
                            travelState = updateQuestProgress(
                                context,
                                travelState,
                                currentSection.id,
                                exerciseId,
                                travelSections,
                                questExercises,
                                langCodes
                            )
                        }
                        
                        // Award coins for all exercises
                        repeat(allExerciseIds.size) { onAwardCoin() }
                        // Refresh local display from storage to avoid double-counting
                        currency = loadCurrency(context)
                        
                        // Track encountered words from this quest (same as real completion)
                        trackWordsFromExercise(context, currentSection, corpus, langCodes)
                        
                        // Track badge progress for debug completion (quest completed) - same as real completion
                        langCodes.forEach { languageCode ->
                            incrementLanguageQuestCount(context, languageCode)
                        }
                        
                        // Mark first quest completed if applicable - same as real completion
                        if (currentSection.id.endsWith("_1")) {
                            markFirstQuestCompleted(context, startLanguageCode)
                        }
                        
                        // Update travel sections to reflect new progress (important for mixed quests)
                        travelSections = updateTravelSectionsWithMixedLanguages(
                            context,
                            basicTravelSections,
                            travelState.questProgresses,
                            supportedLangCodes
                        )
                        
                        // Quest completed, return to list
                        travelState = travelState.copy(currentQuestId = null)
                    }
                )
            }
        }
    }

    // Track words encountered during exercises for mixed exercises and practice mode
    LaunchedEffect(travelState.currentQuestId) {
        val questId = travelState.currentQuestId
        if (questId != null) {
            // Mark words as encountered when starting a quest
            val recipe = recipeForQuestId(questRecipes, questId)
            if (recipe?.wordRange != null) {
                val mainLang = startLangCodeFromQuestId(questId) ?: startLanguageCode
                val wordsToTrack = corpus.subList(
                    recipe.wordRange.first.coerceAtLeast(0),
                    (recipe.wordRange.last + 1).coerceAtMost(corpus.size)
                )
                wordsToTrack.forEach { wordEntry ->
                    val variant = wordEntry.byLang[mainLang]
                    if (variant != null) {
                        val word = variant.word ?: variant.text ?: wordEntry.original
                        addEncounteredWord(context, mainLang, word)
                    }
                }
            }
        }
    }
}

@Composable
fun TravelQuestListScreen(
    context: Context,
    travelSections: List<TravelSection>,
    travelState: TravelState,
    questExercises: Map<String, List<ExerciseType>>,
    onQuestClick: (String) -> Unit
) {
    val listState = rememberLazyListState()
    
    // Show sections based on progression, but always display mixed sections when they become available
    val visibleSections = travelSections.filter { section ->
        val progress = travelState.questProgresses[section.id]
        // Show if unlocked OR if it's a mixed section that should be visible in progression
        progress?.isUnlocked == true || (section.isMixed && isMixedSectionVisible(section, travelSections, travelState))
    }
    
    // Auto-scroll to the end when the screen is loaded
    LaunchedEffect(visibleSections.size) {
        if (visibleSections.isNotEmpty()) {
            listState.animateScrollToItem(visibleSections.size - 1)
        }
    }
    
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Removed list title for cleaner UI
        
        items(visibleSections) { section ->
            val questProgress = travelState.questProgresses[section.id]
            val isUnlocked = questProgress?.isUnlocked == true
            
            if (section.isMixed && !isUnlocked) {
                // Show locked mixed quest with explanation
                LockedMixedQuestBubble(
                    section = section,
                    questProgress = questProgress
                )
            } else {
                CircleQuestBubble(
                    section = section,
                    questProgress = questProgress,
                    onClick = { 
                        if (!section.isCompletionBadge && isUnlocked && questProgress?.isCompleted != true) {
                            onQuestClick(section.id)
                        }
                    }
                )
            }
            
            // Add separator after completion badges
            if (section.isCompletionBadge) {
                val isLevel2Complete = isLevel2CompleteBadge(section)
                val separatorColor = if (isLevel2Complete) Color(0xFFC0C0C0) else Color(0xFFCD7F32) // Silver or Bronze
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Cool diagonal separator line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    separatorColor,
                                    separatorColor,
                                    Color.Transparent
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(1000f, 0f)
                            ),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Debug word tracking display - only show in debug mode
        if (loadDebugMode(context)) {
            item {
                DebugWordTrackingDisplay(context = context)
            }
        }

    }
}

@Composable
private fun DebugWordTrackingDisplay(context: Context) {
    val supportedLangCodes = remember { getSupportedLanguageCodesFromMetadata(context) }
    val corpus = remember { loadCorpusFromAssets(context) }
    
    // Filter to only show languages with non-zero encountered words
    val languagesWithWords = remember(supportedLangCodes) {
        supportedLangCodes.filter { langCode ->
            getEncounteredWordsCount(context, langCode) > 0
        }
    }
    
    if (languagesWithWords.isEmpty()) {
        return // Don't show anything if no languages have encountered words
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D2D2D).copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🐛 Debug: Encountered Words (English)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            languagesWithWords.forEach { langCode ->
                val encounteredWords = remember(langCode) {
                    getEncounteredWords(context, langCode)
                }
                val wordCount = encounteredWords.size
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${getLanguageNameFromMetadata(context, langCode) ?: langCode}:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Text(
                        text = "$wordCount words",
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50)
                    )
                }
                
                // Show first few words in English as examples
                val sampleWords = encounteredWords.take(5)
                if (sampleWords.isNotEmpty()) {
                    val englishWords = sampleWords.mapNotNull { word ->
                        // Find the corresponding WordEntry in corpus and get the original (English) word
                        corpus.find { entry ->
                            entry.byLang[langCode]?.word == word || 
                            entry.byLang[langCode]?.text == word
                        }?.original
                    }
                    
                    if (englishWords.isNotEmpty()) {
                        Text(
                            text = "  ${englishWords.joinToString(", ")}${if (wordCount > 5) "..." else ""}",
                            fontSize = 11.sp,
                            color = Color(0xFFB0B0B0),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RandomTravelIcon(
    questId: String,
    modifier: Modifier = Modifier,
    sizeDp: Int = 64,
    isGreyscale: Boolean = false
) {
    val context = LocalContext.current
    val iconFiles = remember {
        context.assets.list("travel_icons")?.filter { it.endsWith(".png") }?.toList().orEmpty()
    }
    // Persist one icon per quest the first time we need it
    val chosen = remember(questId, iconFiles) {
        val prefs = context.getSharedPreferences("hg_progress", Context.MODE_PRIVATE)
        val key = "quest_icon_" + questId
        val existing = prefs.getString(key, null)
        if (!existing.isNullOrBlank()) {
            existing
        } else {
            val first = iconFiles.randomOrNull()
            if (first != null) {
                prefs.edit().putString(key, first).apply()
            }
            first
        }
    }
    if (chosen != null) {
        val greyFilter = if (isGreyscale) {
            val m = ColorMatrix()
            m.setToSaturation(0f)
            ColorFilter.colorMatrix(m)
        } else null
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data("file:///android_asset/travel_icons/$chosen")
                .build(),
            contentDescription = "Travel icon",
            modifier = modifier.size(sizeDp.dp),
            colorFilter = greyFilter
        )
    }
}

@Composable
fun BottomFlagBadge(
    languageCode: String?,
    modifier: Modifier = Modifier,
    isGreyscale: Boolean = false
) {
    if (languageCode == null) return
    val context = LocalContext.current
    val assetPath = getLanguageFlagAssetFromMetadata(context, languageCode)
    if (assetPath == null) return
    val imageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .build()
    }
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .drawBehind {
                    val baseRadius = 8.dp.toPx()
                    val outlineSize = androidx.compose.ui.geometry.Size(48.dp.toPx(), 36.dp.toPx())
                    drawRoundRect(
                        color = Color.Black.copy(alpha = 0.06f),
                        size = outlineSize,
                        style = Stroke(width = 3.dp.toPx()),
                        cornerRadius = CornerRadius(baseRadius + 1.5.dp.toPx(), baseRadius + 1.5.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.05f),
                        size = outlineSize,
                        style = Stroke(width = 1.5.dp.toPx()),
                        cornerRadius = CornerRadius(baseRadius, baseRadius)
                    )
                }
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data("file:///android_asset/$assetPath")
                    .build(),
                contentDescription = "Flag badge",
                imageLoader = imageLoader,
                modifier = Modifier.fillMaxSize(),
                colorFilter = if (isGreyscale) {
                    val m = ColorMatrix()
                    m.setToSaturation(0f)
                    ColorFilter.colorMatrix(m)
                } else null
            )
        }
    }
}

@Composable
fun CircleQuestBubble(
    section: TravelSection,
    questProgress: QuestProgress?,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val isCompleted = questProgress?.isCompleted == true
    val isUnlocked = questProgress?.isUnlocked == true
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            if (section.isCompletionBadge) {
                // Special appearance for completion badge
                val isLevel2Complete = isLevel2CompleteBadge(section)
                val backgroundColor = if (isLevel2Complete) Color(0xFFE8E8E8) else Color(0xFFF4D3A2) // Silver or Bronze background
                val borderColor = if (isLevel2Complete) Color(0xFFC0C0C0) else Color(0xFFCD7F32) // Silver or Bronze border
                
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(
                        containerColor = backgroundColor
                    ),
                    shape = CircleShape,
                    border = BorderStroke(10.dp, borderColor) // Same width as completed green bubbles
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLevel2Complete) {
                            // Silver medal - use SVG cup
                            val imageLoader = remember(context) {
                                ImageLoader.Builder(context)
                                    .components { add(SvgDecoder.Factory()) }
                                    .build()
                            }
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data("file:///android_asset/cup_silver.svg")
                                    .build(),
                                contentDescription = "Silver Cup",
                                imageLoader = imageLoader,
                                modifier = Modifier.size(60.dp)
                            )
                        } else {
                            // Bronze medal - use SVG cup
                            val imageLoader = remember(context) {
                                ImageLoader.Builder(context)
                                    .components { add(SvgDecoder.Factory()) }
                                    .build()
                            }
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data("file:///android_asset/cup_bronze.svg")
                                    .build(),
                                contentDescription = "Bronze Cup",
                                imageLoader = imageLoader,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }
                }
            } else {
                // Regular quest appearance
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(enabled = isUnlocked && !isCompleted) { onClick() },
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = CircleShape,
                    border = BorderStroke(
                        width = if (isCompleted) 10.dp else 6.dp,
                        color = if (isCompleted) GreenMain else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Centered icon: persist one random travel icon per quest
                        RandomTravelIcon(questId = section.id, sizeDp = 60)
                    }
                }
                
                // Modern checkmark overlay when completed
                if (isCompleted) {
                    ModernCheckmarkOverlay(
                        size = 120f,
                        overlayAlpha = 0.6f, // Softer green overlay
                        checkScale = 0.55f,  // Bigger check
                        strokeWidthFactor = 0.12f // Thicker check stroke
                    )
                }
                // Bottom flag badge overlay
                if (section.isMixed) {
                    val firstCode = startLangCodeFromQuestId(section.id)
                    val isLevel2Exercise3 = section.id.endsWith("level2_exercise3")
                    
                    if (isLevel2Exercise3) {
                        // 3 flags in a row, smaller size
                        val secondCode = section.languages.getOrNull(1)
                        val thirdCode = section.languages.getOrNull(2)
                        
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            if (firstCode != null) {
                                Box(
                                    modifier = Modifier
                                        .width(34.dp)
                                        .height(25.dp)
                                ) {
                                    BottomFlagBadge(languageCode = firstCode, modifier = Modifier.scale(0.7f))
                                }
                            }
                            if (secondCode != null) {
                                Box(
                                    modifier = Modifier
                                        .width(34.dp)
                                        .height(25.dp)
                                ) {
                                    BottomFlagBadge(languageCode = secondCode, modifier = Modifier.scale(0.7f))
                                }
                            }
                            if (thirdCode != null) {
                                Box(
                                    modifier = Modifier
                                        .width(34.dp)
                                        .height(25.dp)
                                ) {
                                    BottomFlagBadge(languageCode = thirdCode, modifier = Modifier.scale(0.7f))
                                }
                            }
                        }
                    } else {
                        // Regular 2-flag display for other mixed quests
                        val secondCode = section.languages.getOrNull(1)
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (firstCode != null) {
                                BottomFlagBadge(languageCode = firstCode, modifier = Modifier)
                            }
                            if (secondCode != null) {
                                BottomFlagBadge(languageCode = secondCode, modifier = Modifier)
                            }
                        }
                    }
                } else {
                    val badgeCode = languageNameToCode(section.language)
                    if (badgeCode != null) {
                        BottomFlagBadge(
                            languageCode = badgeCode,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = 6.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Text styling based on section type
        Text(
            text = section.name,
            style = if (section.isCompletionBadge) {
                MaterialTheme.typography.titleSmall
            } else {
                MaterialTheme.typography.titleMedium
            },
            color = if (section.isCompletionBadge) {
                Color(0xFF808080) // Silver text
            } else if (isUnlocked) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = if (section.isCompletionBadge) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LockedMixedQuestBubble(
    section: TravelSection,
    questProgress: QuestProgress?
) {
    val context = LocalContext.current
    val currentLanguageCode = startLangCodeFromQuestId(section.id) ?: ""
    val isLevel2Exercise3 = section.id.endsWith("level2_exercise3")
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            // Greyed out circle
            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE0E0E0) // Light grey background
                ),
                shape = CircleShape,
                border = BorderStroke(6.dp, Color(0xFFBDBDBD)) // Grey border (thicker)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Sampled travel icon in greyscale
                    RandomTravelIcon(
                        questId = section.id,
                        sizeDp = 60,
                        isGreyscale = true
                    )
                }
            }
            
            // Removed lock overlay
            // Bottom flag badges: show appropriate number of languages based on quest type
            val firstCode = startLangCodeFromQuestId(section.id)
            
            if (isLevel2Exercise3) {
                // 3 flags in a row, smaller size
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    if (firstCode != null) {
                        Box(
                            modifier = Modifier
                                .width(34.dp)
                                .height(25.dp)
                        ) {
                            BottomFlagBadge(languageCode = firstCode, modifier = Modifier.scale(0.7f), isGreyscale = true)
                        }
                    }
                    // First placeholder
                    Box(
                        modifier = Modifier
                            .width(34.dp)
                            .height(25.dp)
                            .scale(0.7f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE0E0E0))
                            .border(2.dp, Color(0xFFBDBDBD), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF757575)
                        )
                    }
                    // Second placeholder
                    Box(
                        modifier = Modifier
                            .width(34.dp)
                            .height(25.dp)
                            .scale(0.7f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE0E0E0))
                            .border(2.dp, Color(0xFFBDBDBD), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF757575)
                        )
                    }
                }
            } else {
                // Regular 2-flag display for other mixed quests
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (firstCode != null) {
                        BottomFlagBadge(languageCode = firstCode, modifier = Modifier, isGreyscale = true)
                    }
                    // Single placeholder
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE0E0E0))
                            .border(2.dp, Color(0xFFBDBDBD), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF757575)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Quest name - greyed out
        Text(
            text = section.name,
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF9E9E9E),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Explanatory text
        val explanationText = if (isLevel2Exercise3) {
            "Mixed mode requires:\n• Previous quest completed\n• First quest completed in 3 different languages"
        } else {
            val currentWordCount = getEncounteredWordsCount(context, currentLanguageCode)
            "Mixed mode requires:\n• Previous quest completed\n• $currentWordCount words encountered in two languages"
        }
        Text(
            text = explanationText,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF757575),
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
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
    
    val borderColor = if (isCompleted) GreenMain else Color.Transparent
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(enabled = isUnlocked && !isCompleted) { onClick() },
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
                    // Removed progress text for cleaner UI
                }
            }
            
            // Completion status
            if (isCompleted) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(GreenMain),
                    contentAlignment = Alignment.Center
                ) {
                    ModernCheckmark(
                        size = 24f,
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
    basicTravelSections: List<TravelSection>,
    supportedLangCodes: List<String>,
    questRecipes: List<QuestRecipe>,
    onExerciseComplete: (String, Boolean) -> Unit,
    onDebugCompleteQuest: () -> Unit
) {
    val context = LocalContext.current
    val corpus by remember { mutableStateOf(loadCorpusFromAssets(context)) }
    val exerciseTypes = questExercises
    val currentProgress = travelState.questProgresses[section.id]
    val currentExerciseIndex = travelState.currentExerciseIndex
    
    // State for showing completion animation
    var showCompletionAnimation by remember { mutableStateOf(false) }
    var pendingCompletionStepKey by remember { mutableStateOf<String?>(null) }
    
    // Track if any mistakes were made in current exercise
    var hadMistakes by remember(currentExerciseIndex) { mutableStateOf(false) }
    
    // Helper function to trigger completion animation
    val triggerCompletion = { stepKey: String ->
        if (!showCompletionAnimation) { // Prevent multiple triggers
            pendingCompletionStepKey = stepKey
            showCompletionAnimation = true
        }
    }
    
    val languages = if (section.isMixed) {
        section.languages
    } else {
        listOf(languageNameToCode(section.language) ?: "es")
    }
    val languageCodes = languages.filterNotNull()
    // Get the recipe for this quest to determine word selection strategy
    val recipe = recipeForQuestId(questRecipes, section.id)
    
    fun getWordsForQuest(base: List<WordEntry>): List<WordEntry> {
        return when {
            section.isMixed || recipe?.useEncounteredWords == true -> {
                // Mixed exercises and practice mode use encountered words
                getEncounteredWordsForMixed(context, base, languageCodes)
            }
            recipe?.wordRange != null -> {
                // Use specific word range from corpus
                val range = recipe.wordRange
                val startIndex = range.first.coerceAtLeast(0)
                val endIndex = range.last.coerceAtMost(base.size - 1)
                if (startIndex <= endIndex) {
                    base.subList(startIndex, endIndex + 1)
                } else {
                    emptyList()
                }
            }
            else -> {
                // Fallback to all words
                base
            }
        }
    }
    
    val currentExercise = exerciseTypes.getOrNull(currentExerciseIndex)
    val stepKey = remember(section.id, currentExerciseIndex) { "${section.id}#${currentExerciseIndex}" }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Progress row: flag badge (smudged SVG) + dark purple progress bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val badgeCode = if (section.isMixed) startLangCodeFromQuestId(section.id) else languageNameToCode(section.language)
            if (badgeCode != null) {
                BottomFlagBadge(
                    languageCode = badgeCode,
                    modifier = Modifier
                        .padding(end = 10.dp)
                )
            }
            LinearProgressIndicator(
                progress = (currentExerciseIndex + 1).toFloat() / exerciseTypes.size,
                modifier = Modifier
                    .weight(1f)
                    .height(12.dp),
                color = MaterialTheme.colorScheme.primary, // Dark purple brand color
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (corpus.isEmpty() || languageCodes.isEmpty() || currentExercise == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No content available")
            }
            return
        }
        
        val encounteredLanguages = remember(corpus, section.isMixed, languageCodes) {
            if (section.isMixed) {
                // Get languages where user has encountered words
                languageCodes.filter { langCode ->
                    getEncounteredWords(context, langCode).isNotEmpty()
                }
            } else {
                emptyList()
            }
        }
        
        val threeWords = remember(corpus, section.isMixed, languageCodes, recipe) {
            getWordsForQuest(corpus).take(3)
        }
        val eligibleAudioWords = remember(corpus, section.isMixed, languageCodes, recipe) {
            getWordsForQuest(corpus).filter { entry -> languageCodes.any { code -> entry.byLang[code]?.audio != null } }
        }
        val eligiblePronunciationWords = remember(corpus, section.isMixed, languageCodes, recipe) {
            getWordsForQuest(corpus).filter { entry -> languageCodes.any { code ->
                val v = entry.byLang[code]
                // Require audio, and accept Google pronunciation or fallback to main word
                v?.audio != null && ((v.googlePronunciation != null) || (v.word != null))
            } }
        }

        // Shuffled looping selector for single-word exercises so we don't repeat until pool is exhausted
        val singleWordOrderByExercise = remember(section.id) { mutableStateMapOf<String, List<WordEntry>>() }
        val singleWordIndexByExercise = remember(section.id) { mutableStateMapOf<String, Int>() }
        fun nextSingleWord(exerciseKey: String, pool: List<WordEntry>): WordEntry {
            val existing = singleWordOrderByExercise[exerciseKey]
            val list = if (existing != null && existing.size == pool.size && existing.containsAll(pool)) {
                existing
            } else {
                val shuffled = pool.shuffled()
                singleWordOrderByExercise[exerciseKey] = shuffled
                singleWordIndexByExercise[exerciseKey] = 0
                shuffled
            }
            val idx = singleWordIndexByExercise[exerciseKey] ?: 0
            val chosen = list.getOrNull(idx) ?: pool.firstOrNull() ?: pool.random()
            val nextIdx = if (list.isNotEmpty()) (idx + 1) % list.size else 0
            singleWordIndexByExercise[exerciseKey] = nextIdx
            return chosen
        }
        
        // Check if current exercise is already completed
        val isExerciseCompleted = currentProgress?.completedExercises?.contains(stepKey) == true
        
        // Show completion animation or exercise content
        if (showCompletionAnimation) {
            ExerciseCompletionScreen(
                onContinue = {
                    showCompletionAnimation = false
                    // Process the pending completion: advance exercise
                    pendingCompletionStepKey?.let { stepKey ->
                        val isPerfect = !hadMistakes
                        onExerciseComplete(stepKey, isPerfect)
                        pendingCompletionStepKey = null
                    }
                }
            )
        } else {
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
                val normalized = currentExercise.id.removeSuffix("_multi")
                val effectiveExerciseId = if (!section.isMixed && normalized == "audio_to_flag") {
                    "pronunciation_audio_to_english"
                } else normalized
                when (effectiveExerciseId) {
                    "audio_to_flag" -> {
                        val sourceBase = if (eligibleAudioWords.isNotEmpty()) eligibleAudioWords else getWordsForQuest(corpus)
                        val source = if (section.isMixed) sourceBase else sourceBase.take(10)
                        val pairs = buildAudioToFlagPairs(
                            source, 
                            languageCodes,
                            restrictToEncountered = section.isMixed,
                            availableLanguages = encounteredLanguages
                        )
                        MatchingExercise(
                            title = currentExercise.title,
                            pairs = pairs,
                            useFlagAssets = true,
                            onDone = { perfect ->
                                if (!perfect) hadMistakes = true
                                triggerCompletion(stepKey)
                            }
                        )
                    }
                    "audio_to_flag_multi" -> {
                        val pool = if (eligibleAudioWords.isNotEmpty()) eligibleAudioWords else getWordsForQuest(corpus)
                        val words = if (section.isMixed) pool else pool.take(20)
                        val pairs = buildAudioToFlagPairsMulti(
                            words,
                            languageCodes,
                            restrictToEncountered = section.isMixed,
                            availableLanguages = encounteredLanguages
                        )
                        MatchingExercise(
                            title = currentExercise.title,
                            pairs = pairs,
                            useFlagAssets = true,
                            onDone = { perfect ->
                                triggerCompletion(stepKey)
                            }
                        )
                    }
                    "pronunciation_to_flag" -> {
                        val pairs = buildPronunciationToFlagPairs(
                            threeWords, 
                            languageCodes,
                            restrictToEncountered = section.isMixed,
                            availableLanguages = encounteredLanguages
                        )
                        MatchingExercise(
                            title = currentExercise.title,
                            pairs = pairs,
                            useFlagAssets = true,
                            onDone = { perfect ->
                                triggerCompletion(stepKey)
                            }
                        )
                    }
                    "pronunciation_to_flag_multi" -> {
                        val pool = if (eligiblePronunciationWords.isNotEmpty()) eligiblePronunciationWords else getWordsForQuest(corpus)
                        val words = if (section.isMixed) pool else pool.take(20)
                        val pairs = buildPronunciationToFlagPairsMulti(
                            words,
                            languageCodes,
                            restrictToEncountered = section.isMixed,
                            availableLanguages = encounteredLanguages
                        )
                        MatchingExercise(
                            title = currentExercise.title,
                            pairs = pairs,
                            useFlagAssets = true,
                            onDone = { perfect ->
                                triggerCompletion(stepKey)
                            }
                        )
                    }
                    "pronunciation_to_english_multi" -> {
                        val pool = if (eligiblePronunciationWords.isNotEmpty()) eligiblePronunciationWords else getWordsForQuest(corpus)
                        val words = if (section.isMixed) pool else pool.take(20)
                        val pairs = buildPronunciationToEnglishPairsMulti(
                            words,
                            languageCodes,
                            restrictToEncountered = section.isMixed,
                            availableLanguages = encounteredLanguages
                        )
                        MatchingExercise(
                            title = currentExercise.title,
                            pairs = pairs,
                            onDone = { perfect ->
                                triggerCompletion(stepKey)
                            }
                        )
                    }
                    "audio_to_english" -> {
                        val sourceBase = if (eligibleAudioWords.isNotEmpty()) eligibleAudioWords else getWordsForQuest(corpus)
                        val source = if (section.isMixed) sourceBase else sourceBase.take(10)
                        val pairs = buildAudioToEnglishPairs(
                            source, 
                            languageCodes,
                            restrictToEncountered = section.isMixed,
                            availableLanguages = encounteredLanguages
                        )
                        MatchingExercise(
                            title = currentExercise.title,
                            pairs = pairs,
                            onDone = { perfect ->
                                triggerCompletion(stepKey)
                            }
                        )
                    }
                    "pronunciation_to_english" -> {
                        val sourceBase = if (eligiblePronunciationWords.isNotEmpty()) eligiblePronunciationWords else getWordsForQuest(corpus)
                        val source = if (section.isMixed) sourceBase else sourceBase.take(10)
                        val pairs = buildPronunciationToEnglishPairsMulti(
                            source,
                            languageCodes,
                            restrictToEncountered = section.isMixed,
                            availableLanguages = encounteredLanguages
                        )
                        MatchingExercise(
                            title = currentExercise.title,
                            pairs = pairs,
                            onDone = { perfect ->
                                triggerCompletion(stepKey)
                            }
                        )
                    }
                    "pronunciation_audio_to_english" -> {
                        val pool = if (eligiblePronunciationWords.isNotEmpty()) eligiblePronunciationWords else getWordsForQuest(corpus)
                        // pick one word and build 5 options
                        val chosen = remember(stepKey, pool) { nextSingleWord(effectiveExerciseId, pool) }
                        // Choose a language variant that has audio and either Google pronunciation or main word
                        val code = languageCodes.firstOrNull {
                            val v = chosen.byLang[it]
                            v?.audio != null && (v.googlePronunciation != null || v.word != null)
                        } ?: languageCodes.first()
                        val variant = chosen.byLang[code]
                        // Use Google pronunciation, fallback to the main word
                        val pronunciation = variant?.googlePronunciation ?: (variant?.word ?: "")
                        
                        // Always match to English translation (fallback to original)
                        val correctAnswer = chosen.byLang["en"]?.text ?: chosen.byLang["en"]?.word ?: chosen.original
                        
                        val distractors = pool.filter { it !== chosen }
                            .mapNotNull { it.byLang["en"]?.text ?: it.byLang["en"]?.word ?: it.original }
                            .distinct()
                            .take(4)
                        val options = (distractors + correctAnswer).shuffled()
                        PronunciationAudioToEnglishExercise(
                            title = currentExercise.title,
                            pronunciation = pronunciation,
                            audioFile = variant?.audio,
                            options = options,
                            correctOption = correctAnswer,
                            onDone = { isCorrect ->
                                // Inline continue inside the exercise: advance without animation
                                if (!isCorrect) hadMistakes = true
                                val isPerfect = !hadMistakes
                                onExerciseComplete(stepKey, isPerfect)
                            }
                        )
                    }
                    "pronunciation_audio_to_type_english" -> {
                        val pool = if (eligiblePronunciationWords.isNotEmpty()) eligiblePronunciationWords else getWordsForQuest(corpus)
                        val chosen = remember(stepKey, pool) { nextSingleWord(effectiveExerciseId, pool) }
                        val code = languageCodes.firstOrNull {
                            val v = chosen.byLang[it]
                            v?.audio != null && (v.googlePronunciation != null || v.word != null)
                        } ?: languageCodes.first()
                        val variant = chosen.byLang[code]
                        val pronunciation = variant?.googlePronunciation ?: (variant?.word ?: "")
                        val correctAnswer = chosen.byLang["en"]?.text ?: chosen.byLang["en"]?.word ?: chosen.original
                        PronunciationAudioToTypeEnglishExercise(
                            title = currentExercise.title,
                            pronunciation = pronunciation,
                            audioFile = variant?.audio,
                            correctAnswer = correctAnswer,
                            onDone = { isCorrect ->
                                if (isCorrect) {
                                    // Correct: show completion animation
                                    triggerCompletion(stepKey)
                                } else {
                                    // Wrong: advance inline without animation
                                    hadMistakes = true
                                    val isPerfect = !hadMistakes
                                    onExerciseComplete(stepKey, isPerfect)
                                }
                            }
                        )
                    }
                    "flashcards" -> {
                        val pool = if (eligiblePronunciationWords.isNotEmpty()) eligiblePronunciationWords else getWordsForQuest(corpus)
                        val words = if (section.isMixed) pool else pool.take(5) // Use 5 words for flashcards
                        val code = languageCodes.firstOrNull {
                            val v = words.firstOrNull()?.byLang[it]
                            v?.audio != null && (v.googlePronunciation != null || v.word != null)
                        } ?: languageCodes.first()
                        FlashcardsExercise(
                            words = words,
                            languageCode = code,
                            onDone = { perfect ->
                                if (perfect) {
                                    // Perfect: show completion animation
                                    triggerCompletion(stepKey)
                                } else {
                                    // Not perfect: advance inline without animation
                                    hadMistakes = true
                                    val isPerfect = !hadMistakes
                                    onExerciseComplete(stepKey, isPerfect)
                                }
                            }
                        )
                    }
                }
            }
            
            // Debug button at bottom - only show when debug mode is enabled
            if (loadDebugMode(context)) {
                Button(
                    onClick = onDebugCompleteQuest,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FlagRed,
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
        }
}