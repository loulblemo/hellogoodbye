package com.loulblemo.hellogoodbye

import android.content.Context
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
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
            // For mixed sections, work exactly like practice mode
            val startLangCode = section.id.split("_")[0] // Extract start language from section ID
            
            val availableLanguagesForMixed = mutableListOf<String>()
            availableLanguagesForMixed.add(startLangCode)
            
            // Find other languages that have at least bronze medal (completed at least 1 quest)
            val languagesWithBronzeMedal = allLangCodes.filter { langCode ->
                langCode != startLangCode && 
                langCode != "en" && 
                getLanguageQuestCount(context, langCode) >= 1
            }
            
            // Randomly select one language from those with bronze medal
            if (languagesWithBronzeMedal.isNotEmpty()) {
                availableLanguagesForMixed.add(languagesWithBronzeMedal.random())
            }
            
            val secondLangName = if (availableLanguagesForMixed.size > 1) {
                languageCodeToName(availableLanguagesForMixed[1]) ?: availableLanguagesForMixed[1]
            } else {
                "Mixed"
            }
            
            section.copy(
                languages = availableLanguagesForMixed,
                name = "Mixed: ${languageCodeToName(startLangCode) ?: startLangCode} + $secondLangName"
            )
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
    val travelSections = remember(basicTravelSections, travelState.questProgresses) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with coins and exit
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Currency display (matches home top bar style)
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
                    onExerciseComplete = { completedStepKey ->
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
                            questExercises
                        )
                        onAwardCoin()
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
                        // Refresh local display from storage to avoid double-counting
                        currency = loadCurrency(context)
                        
                        // Track encountered words from this quest (same as real completion)
                        val languages = if (currentSection.isMixed) {
                            currentSection.languages
                        } else {
                            listOf(languageNameToCode(currentSection.language) ?: startLanguageCode)
                        }
                        val langCodes = languages.filterNotNull()
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
                        val updatedSections = updateTravelSectionsWithMixedLanguages(
                            context,
                            basicTravelSections,
                            travelState.questProgresses,
                            supportedLangCodes
                        )
                        
                        // Force recomposition to update the UI with new mixed language selections
                        // This ensures the practice button shows up when it should
                        
                        // Quest completed, return to list
                        travelState = travelState.copy(currentQuestId = null)
                    }
                )
            }
        }
    }

    // Seed new words for main language when a quest starts, based on its recipe
    val seededQuestIds = remember { mutableStateOf(mutableSetOf<String>()) }
    LaunchedEffect(travelState.currentQuestId) {
        val questId = travelState.currentQuestId
        if (questId != null && !seededQuestIds.value.contains(questId)) {
            val recipe = recipeForQuestId(questRecipes, questId)
            if (recipe != null) {
                val mainLang = startLangCodeFromQuestId(questId) ?: startLanguageCode
                if (recipe.newWordsFromMain > 0) {
                    seedNewWordsForQuest(context, corpus, mainLang, recipe.newWordsFromMain)
                }
            }
            seededQuestIds.value.add(questId)
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
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Removed list title for cleaner UI
        
        // Show sections based on progression, but always display mixed sections when they become available
        val visibleSections = travelSections.filter { section ->
            val progress = travelState.questProgresses[section.id]
            // Show if unlocked OR if it's a mixed section that should be visible in progression
            progress?.isUnlocked == true || (section.isMixed && isMixedSectionVisible(section, travelSections, travelState))
        }
        
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
                        if (!section.isCompletionBadge && isUnlocked) {
                            onQuestClick(section.id)
                        }
                    }
                )
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
                    val outlineSize = androidx.compose.ui.geometry.Size(size.width, size.height)
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
                // Special appearance for completion badge (bronze)
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF4D3A2) // Bronze background
                    ),
                    shape = CircleShape,
                    border = BorderStroke(6.dp, Color(0xFFCD7F32)) // Bronze border (thicker)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = section.flag, // 🥉 bronze medal
                            fontSize = 60.sp
                        )
                    }
                }
            } else {
                // Regular quest appearance
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(enabled = isUnlocked) { onClick() },
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = CircleShape,
                    border = BorderStroke(
                        width = if (isCompleted) 10.dp else 6.dp,
                        color = if (isCompleted) FlagGreen else MaterialTheme.colorScheme.primary
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
            // Bottom flag badges: start flag + question mark placeholder (greyed)
            val firstCode = startLangCodeFromQuestId(section.id)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (firstCode != null) {
                    BottomFlagBadge(languageCode = firstCode, modifier = Modifier, isGreyscale = true)
                }
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
        Text(
            text = "Mixed mode requires:\n• Previous quest completed\n• First quest completed in another language",
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
    
    val borderColor = if (isCompleted) FlagGreen else Color.Transparent
    
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
                    // Removed progress text for cleaner UI
                }
            }
            
            // Completion status
            if (isCompleted) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(FlagGreen),
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
    onExerciseComplete: (String) -> Unit,
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
    val isEarlyQuest = !section.isMixed && section.id.split("_").lastOrNull()?.toIntOrNull()?.let { it in 1..5 } == true
    val mainLanguageCode = languageCodes.firstOrNull()
    val earlyWordsSet = if (isEarlyQuest && mainLanguageCode != null) getEarlyWords(context, mainLanguageCode) else emptySet()
    fun filterToEarly(base: List<WordEntry>): List<WordEntry> {
        if (!isEarlyQuest || mainLanguageCode == null || earlyWordsSet.isEmpty()) return base
        val filtered = base.filter { entry ->
            val v = entry.byLang[mainLanguageCode]
            val w = v?.word ?: v?.text ?: entry.original
            earlyWordsSet.contains(w)
        }
        return if (filtered.isNotEmpty()) filtered else base
    }
    
    val currentExercise = exerciseTypes.getOrNull(currentExerciseIndex)
    val stepKey = remember(section.id, currentExerciseIndex) { "${section.id}#${currentExerciseIndex}" }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Removed header flag and language name for cleaner UI
        
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
        
        val threeWords = remember(corpus, section.isMixed, languageCodes, isEarlyQuest, earlyWordsSet) {
            if (section.isMixed) {
                // For mixed exercises, only use words that have been encountered before
                getEncounteredWordsForMixed(context, corpus, languageCodes).shuffled()
            } else {
                filterToEarly(corpus).shuffled().take(3)
            }
        }
        val eligibleAudioWords = remember(corpus, section.isMixed, languageCodes, isEarlyQuest, earlyWordsSet) {
            val baseWords = if (section.isMixed) {
                // For mixed exercises, only use words that have been encountered before
                getEncounteredWordsForMixed(context, corpus, languageCodes)
            } else {
                filterToEarly(corpus)
            }
            baseWords.filter { entry -> languageCodes.any { code -> entry.byLang[code]?.audio != null } }
        }
        val eligiblePronunciationWords = remember(corpus, section.isMixed, languageCodes, isEarlyQuest, earlyWordsSet) {
            val baseWords = if (section.isMixed) {
                // For mixed exercises, only use words that have been encountered before
                getEncounteredWordsForMixed(context, corpus, languageCodes)
            } else {
                filterToEarly(corpus)
            }
            baseWords.filter { entry -> languageCodes.any { code ->
                val v = entry.byLang[code]
                // Require audio, and accept Google pronunciation or fallback to main word
                v?.audio != null && ((v.googlePronunciation != null) || (v.word != null))
            } }
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
                        onExerciseComplete(stepKey)
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
                        val sourceBase = (if (eligibleAudioWords.isNotEmpty()) eligibleAudioWords else filterToEarly(corpus)).shuffled()
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
                                triggerCompletion(stepKey)
                            }
                        )
                    }
                    "audio_to_flag_multi" -> {
                        val pool = (if (eligibleAudioWords.isNotEmpty()) eligibleAudioWords else filterToEarly(corpus))
                        val shuffled = pool.shuffled()
                        val words = if (section.isMixed) shuffled else shuffled.take(20)
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
                        val pool = (if (eligiblePronunciationWords.isNotEmpty()) eligiblePronunciationWords else filterToEarly(corpus))
                        val shuffled = pool.shuffled()
                        val words = if (section.isMixed) shuffled else shuffled.take(20)
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
                        val pool = (if (eligiblePronunciationWords.isNotEmpty()) eligiblePronunciationWords else filterToEarly(corpus))
                        val shuffled = pool.shuffled()
                        val words = if (section.isMixed) shuffled else shuffled.take(20)
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
                        val sourceBase = (if (eligibleAudioWords.isNotEmpty()) eligibleAudioWords else filterToEarly(corpus)).shuffled()
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
                        val sourceBase = (if (eligiblePronunciationWords.isNotEmpty()) eligiblePronunciationWords else filterToEarly(corpus)).shuffled()
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
                        val pool = if (eligiblePronunciationWords.isNotEmpty()) eligiblePronunciationWords else filterToEarly(corpus)
                        // pick one word and build 5 options
                        val chosen = pool.random()
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
                            .shuffled()
                            .distinct()
                            .take(4)
                        val options = (distractors + correctAnswer).shuffled()
                        PronunciationAudioToEnglishExercise(
                            title = currentExercise.title,
                            pronunciation = pronunciation,
                            audioFile = variant?.audio,
                            options = options,
                            correctOption = correctAnswer,
                            onDone = { _ ->
                                // Inline continue inside the exercise: advance without animation
                                onExerciseComplete(stepKey)
                            }
                        )
                    }
                    "pronunciation_audio_to_type_english" -> {
                        val pool = if (eligiblePronunciationWords.isNotEmpty()) eligiblePronunciationWords else filterToEarly(corpus)
                        val chosen = pool.random()
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
                                    onExerciseComplete(stepKey)
                                }
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