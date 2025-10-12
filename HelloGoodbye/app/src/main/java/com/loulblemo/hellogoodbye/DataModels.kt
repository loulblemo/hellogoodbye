package com.loulblemo.hellogoodbye

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Country(
    val flag: String,
    val name: String,
    val language: String
)

data class TravelSection(
    val id: String,
    val flag: String,
    val name: String,
    val language: String,
    val isCompleted: Boolean = false,
    val isMixed: Boolean = false,
    val languages: List<String> = emptyList(),
    val isCompletionBadge: Boolean = false
)

data class ExerciseType(
    val id: String,
    val title: String,
    val description: String
)

data class QuestProgress(
    val questId: String,
    val completedExercises: Set<String> = emptySet(),
    val isUnlocked: Boolean = false,
    val isCompleted: Boolean = false,
    val languagesUsed: List<String> = emptyList()
)

data class TravelState(
    val questProgresses: Map<String, QuestProgress> = emptyMap(),
    val currentQuestId: String? = null,
    val currentExerciseIndex: Int = 0,
    val questExercises: Map<String, List<ExerciseType>> = emptyMap()
)

data class WordVariant(
    val word: String?,
    val ipa: String?,
    val text: String?,
    val googlePronunciation: String?,
    val audio: String?
)

data class WordEntry(
    val original: String,
    val byLang: Map<String, WordVariant>
)

data class PairItem(
    val id: String,
    val label: String,
    val isAudio: Boolean,
    val audioFile: String? = null,
    val matchKey: String
)

data class MatchingPair(
    val left: PairItem,
    val right: PairItem
)

enum class BadgeLevel {
    NONE,     // No badge - not started
    GREEN,    // Completed 1 exercise (green badge)
    BRONZE,   // Completed Level 1 (5 exercises)
    SILVER    // Completed Level 2 (10 exercises)
}

data class LanguageProgress(
    val languageCode: String,
    val completedExercisesCount: Int = 0,
    val badgeLevel: BadgeLevel = BadgeLevel.NONE
)

fun loadCorpusFromAssets(context: Context): List<WordEntry> {
    return runCatching {
        val jsonString = context.assets.open("corpus.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(jsonString)
        val list = mutableListOf<WordEntry>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val original = obj.optString("original")
            val map = mutableMapOf<String, WordVariant>()
            obj.keys().forEachRemaining { key ->
                if (key == "original") return@forEachRemaining
                val langObj = obj.optJSONObject(key) ?: return@forEachRemaining
                val variant = WordVariant(
                    word = langObj.optString("word", null),
                    ipa = langObj.optString("IPA", null),
                    text = langObj.optString("text", null),
                    googlePronunciation = langObj.optString("respelling", null).takeIf { it != "None" && it.isNotEmpty() },
                    audio = langObj.optString("audio_file", null)
                )
                map[key] = variant
            }
            list.add(WordEntry(original = original, byLang = map))
        }
        list
    }.getOrElse { emptyList() }
}

// Language metadata for flags and names
private var languageMetadata: JSONObject? = null

fun loadLanguageMetadata(context: Context): JSONObject? {
    // Always reload to ensure we have the latest metadata
    languageMetadata = runCatching {
        val jsonString = context.assets.open("language_metadata.json").bufferedReader().use { it.readText() }
        JSONObject(jsonString)
    }.getOrElse { null }
    return languageMetadata
}

// Function to clear the cached metadata (useful for development/testing)
fun clearLanguageMetadataCache() {
    languageMetadata = null
}

fun languageNameToCode(name: String): String? {
    return when (name.lowercase()) {
        "english" -> "en"
        "spanish" -> "es"
        "french" -> "fr"
        "german" -> "de"
        "italian" -> "it"
        "portuguese" -> "pt"
        "russian" -> "ru"
        "japanese" -> "ja"
        "korean" -> "ko"
        "chinese", "chinese (simplified)", "simplified chinese" -> "zh-cn"
        "dutch" -> "nl"
        "swedish" -> "sv"
        "thai" -> "th"
        "vietnamese" -> "vi"
        "indonesian" -> "id"
        "malay" -> "ms"
        "filipino", "tagalog" -> "tl"
        "greek" -> "el"
        "swedish" -> "sv"
        "finnish" -> "fi"
        "arabic" -> "ar"
        "turkish" -> "tr"
        "hindi" -> "hi"
        "polish" -> "pl"
        "hungarian" -> "hu"
        "swahili" -> "sw"

        else -> null
    }
}

fun languageCodeToFlag(code: String): String? {
    // This function is now deprecated in favor of getLanguageMetadata
    // Keeping for backward compatibility
    return when (code) {
        "en" -> "🇺🇸"
        "es" -> "🇪🇸"
        "fr" -> "🇫🇷"
        "de" -> "🇩🇪"
        "it" -> "🇮🇹"
        "pt" -> "🇵🇹"
        "ru" -> "🇷🇺"
        "ja" -> "🇯🇵"
        "ko" -> "🇰🇷"
        "zh-cn" -> "🇨🇳"
        "nl" -> "🇳🇱"
        "sv" -> "🇸🇪"
        "th" -> "🇹🇭"
        "vi" -> "🇻🇳"
        "id" -> "🇮🇩"
        "ms" -> "🇲🇾"
        "tl" -> "🇵🇭"
        "el" -> "🇬🇷"
        "sv" -> "🇸🇪"
        "fi" -> "🇫🇮"
        "ar" -> "🇸🇦"
        "tr" -> "🇹🇷"
        "hi" -> "🇮🇳"
        "pl" -> "🇵🇱"
        "hu" -> "🇭🇺"
        "sw" -> "🇹🇿"
        else -> null
    }
}

fun languageCodeToName(code: String): String? {
    // This function is now deprecated in favor of getLanguageMetadata
    // Keeping for backward compatibility
    return when (code) {
        "en" -> "English"
        "es" -> "Spanish"
        "fr" -> "French"
        "de" -> "German"
        "it" -> "Italian"
        "pt" -> "Portuguese"
        "ru" -> "Russian"
        "ja" -> "Japanese"
        "ko" -> "Korean"
        "zh-cn" -> "Chinese"
        "nl" -> "Dutch"
        "sv" -> "Swedish"
        "th" -> "Thai"
        "vi" -> "Vietnamese"
        "id" -> "Indonesian"
        "ms" -> "Malay"
        "tl" -> "Filipino"
        "el" -> "Greek"
        "sv" -> "Swedish"
        "fi" -> "Finnish"
        "ar" -> "Arabic"
        "tr" -> "Turkish"
        "hi" -> "Hindi"
        "pl" -> "Polish"
        "hu" -> "Hungarian"
        "sw" -> "Swahili"
        else -> null
    }
}

// New data-driven functions using metadata
fun getLanguageMetadata(context: Context, code: String): JSONObject? {
    val metadata = loadLanguageMetadata(context)
    return metadata?.optJSONObject("languages")?.optJSONObject(code)
}

fun getLanguageNameFromMetadata(context: Context, code: String): String? {
    return getLanguageMetadata(context, code)?.optString("name")
}

fun getLanguageFlagFromMetadata(context: Context, code: String): String? {
    return getLanguageMetadata(context, code)?.optString("flag")
}

fun getLanguageFlagAssetFromMetadata(context: Context, code: String): String? {
    val raw = getLanguageMetadata(context, code)?.optString("flagAsset")
    return if (raw.isNullOrBlank()) null else raw
}

fun generateTravelSequence(allLangCodes: List<String>): List<TravelSection> {
    val sequence = mutableListOf<TravelSection>()
    // Start with Italy, France, then Mixed(it, fr)
    sequence.add(TravelSection(id = "italy", flag = "🇮🇹", name = "Italy", language = "Italian"))
    sequence.add(TravelSection(id = "france", flag = "🇫🇷", name = "France", language = "French"))
    sequence.add(
        TravelSection(
            id = "mixed_it_fr",
            flag = "🌍",
            name = "Mixed: 🇮🇹 + 🇫🇷",
            language = "Mixed",
            isMixed = true,
            languages = listOf("it", "fr")
        )
    )

    val remaining = allLangCodes.filter { it != "it" && it != "fr" }.shuffled()
    var idx = 0
    while (idx < remaining.size) {
        val a = remaining[idx]
        val aName = languageCodeToName(a)
        val aFlag = languageCodeToFlag(a)
        if (aName != null && aFlag != null) {
            sequence.add(
                TravelSection(
                    id = "lang_${a}",
                    flag = aFlag,
                    name = aName,
                    language = aName
                )
            )
        }

        val b = if (idx + 1 < remaining.size) remaining[idx + 1] else null
        if (b != null) {
            val bName = languageCodeToName(b)
            val bFlag = languageCodeToFlag(b)
            if (bName != null && bFlag != null) {
                sequence.add(
                    TravelSection(
                        id = "lang_${b}",
                        flag = bFlag,
                        name = bName,
                        language = bName
                    )
                )
                sequence.add(
                    TravelSection(
                        id = "mixed_${a}_${b}",
                        flag = "🌍",
                        name = "Mixed: ${aFlag} + ${bFlag}",
                        language = "Mixed",
                        isMixed = true,
                        languages = listOf(a, b)
                    )
                )
            }
            idx += 2
        } else {
            // Single leftover language, no mixed
            idx += 1
        }
    }

    return sequence
}

fun generateTravelSequenceForLanguage(startLangCode: String, allLangCodes: List<String>): List<TravelSection> {
    val startName = languageCodeToName(startLangCode) ?: "English"
    val startFlag = languageCodeToFlag(startLangCode) ?: "🇺🇸"
    val sections = mutableListOf<TravelSection>()
    
    // First 2 language quests
    sections.add(
        TravelSection(
            id = "${startLangCode}_1",
            flag = startFlag,
            name = startName,
            language = startName,
            isMixed = false,
            languages = listOf(startLangCode)
        )
    )
    sections.add(
        TravelSection(
            id = "${startLangCode}_2",
            flag = startFlag,
            name = startName,
            language = startName,
            isMixed = false,
            languages = listOf(startLangCode)
        )
    )
    
    // Mixed quest - basic structure, languages will be dynamically selected later
    val mixedLangs = listOf(startLangCode)
    
    val secondLangName = "Mixed"
    
    sections.add(
        TravelSection(
            id = "${startLangCode}_mixed",
            flag = "🌍",
            name = "Mixed",
            language = "Mixed",
            isMixed = true,
            languages = mixedLangs
        )
    )
    
    // Additional 2 language quests
    sections.add(
        TravelSection(
            id = "${startLangCode}_3",
            flag = startFlag,
            name = startName,
            language = startName,
            isMixed = false,
            languages = listOf(startLangCode)
        )
    )
    sections.add(
        TravelSection(
            id = "${startLangCode}_4",
            flag = startFlag,
            name = startName,
            language = startName,
            isMixed = false,
            languages = listOf(startLangCode)
        )
    )
    
    // Level 1 Complete badge (special section) — bronze
    sections.add(
        TravelSection(
            id = "${startLangCode}_complete",
            flag = "🏆", // Changed to trophy emoji to indicate cup
            name = "Level 1 Complete!",
            language = "Completion",
            isMixed = false,
            languages = listOf(startLangCode),
            isCompletionBadge = true
        )
    )
    
    return sections
}

fun getExerciseTypes(): List<ExerciseType> {
    return listOf(
        ExerciseType("audio_to_english", "Match Audio to Translation", "Listen and match the audio to the correct translation"),
        ExerciseType("pronunciation_audio_to_english", "Pronunciation + Audio to Translation", "See pronunciation, hear audio, choose the correct meaning")
    )
}

fun generateQuestExercises(numExercises: Int): List<ExerciseType> {
    val types = getExerciseTypes()
    if (types.isEmpty()) return emptyList()
    val list = mutableListOf<ExerciseType>()
    repeat(numExercises) {
        list.add(types.random())
    }
    return list
}

fun getExerciseTypesForSection(section: TravelSection): List<ExerciseType> {
    return if (section.isMixed) {
        // Mixed sections only contain mixed exercises (like practice mode)
        listOf(
            ExerciseType("audio_to_flag", "Match Audio to Flag", "Listen and match the audio to the correct flag"),
            ExerciseType("pronunciation_to_flag", "Match Pronunciation to Flag", "Match written pronunciation to the correct flag"),
            ExerciseType("audio_to_english", "Match Audio to Translation", "Listen and match the audio to the correct translation"),
            ExerciseType("pronunciation_audio_to_english", "Pronunciation + Audio to Translation", "See pronunciation, hear audio, choose the correct meaning")
        )
    } else {
        // Regular sections contain only the basic exercises
        getExerciseTypes()
    }
}

fun generateQuestExercisesForSection(section: TravelSection, numExercises: Int): List<ExerciseType> {
    val types = getExerciseTypesForSection(section)
    if (types.isEmpty()) return emptyList()
    val list = mutableListOf<ExerciseType>()
    repeat(numExercises) {
        list.add(types.random())
    }
    return list
}

fun initializeTravelState(context: Context, travelSections: List<TravelSection>, questExercises: Map<String, List<ExerciseType>>): TravelState {
    val questIds = travelSections.map { it.id }
    val loadedProgresses = loadQuestProgress(context, questIds)
    
    // Ensure first quest is always unlocked, merge with loaded progress
    val questProgresses = travelSections.mapIndexed { index, section ->
        val loaded = loadedProgresses[section.id]
        val progress = if (loaded != null) {
            // Use loaded progress but ensure first quest is unlocked
            if (index == 0) loaded.copy(isUnlocked = true) else loaded
        } else {
            // Create new progress with first quest unlocked
            QuestProgress(
                questId = section.id,
                isUnlocked = index == 0
            )
        }
        section.id to progress
    }.toMap()
    
    // Auto-unlock and complete completion badges when all previous quests are done
    // Also check mixed quest requirements
    val updatedProgresses = questProgresses.toMutableMap()
    travelSections.forEachIndexed { index, section ->
        if (section.isCompletionBadge) {
            // Check if all previous quests are completed
            val previousQuests = travelSections.subList(0, index)
            val allPreviousCompleted = previousQuests.all { prevSection ->
                updatedProgresses[prevSection.id]?.isCompleted == true
            }
            
            if (allPreviousCompleted) {
                // Auto-unlock and complete the completion badge
                updatedProgresses[section.id] = QuestProgress(
                    questId = section.id,
                    isUnlocked = true,
                    isCompleted = true,
                    completedExercises = emptySet()
                )
                
                // Also unlock the first level 2 exercise after completion badge
                if (index + 1 < travelSections.size) {
                    val nextSection = travelSections[index + 1]
                    val nextProgress = updatedProgresses[nextSection.id]
                    if (nextProgress != null && !nextProgress.isUnlocked) {
                        updatedProgresses[nextSection.id] = nextProgress.copy(isUnlocked = true)
                    }
                }
            }
        } else if (section.isMixed && index > 0) {
            // Check if mixed quest should be unlocked
            val previousQuest = travelSections[index - 1]
            val isPreviousCompleted = updatedProgresses[previousQuest.id]?.isCompleted == true
            val currentLanguageCode = startLangCodeFromQuestId(section.id)
            
            // Check language requirements based on quest type
            val canUnlockMixed = when {
                section.id.endsWith("level2_exercise3") -> {
                    // Level 2 Exercise 3 requires 3+ languages with 10+ encountered words each
                    hasEncounteredWordsInAtLeast3Languages(context)
                }
                else -> {
                    // Other mixed quests require equal or higher encountered words in another language
                    currentLanguageCode == null || hasEqualOrHigherEncounteredWordsInOtherLanguage(context, currentLanguageCode)
                }
            }
            
            if (isPreviousCompleted && canUnlockMixed) {
                val currentProgress = updatedProgresses[section.id]
                if (currentProgress != null && !currentProgress.isUnlocked) {
                    updatedProgresses[section.id] = currentProgress.copy(isUnlocked = true)
                }
            }
        }
    }
    
    return TravelState(
        questProgresses = updatedProgresses,
        currentQuestId = null,
        currentExerciseIndex = 0,
        questExercises = questExercises
    )
}

fun updateQuestProgress(
    context: Context,
    travelState: TravelState,
    questId: String,
    completedExerciseId: String,
    travelSections: List<TravelSection>,
    questExercises: Map<String, List<ExerciseType>>,
    languagesUsed: List<String> = emptyList()
): TravelState {
    val currentProgress = travelState.questProgresses[questId] ?: return travelState
    
    val newCompletedExercises = currentProgress.completedExercises + completedExerciseId
    val questSize = questExercises[questId]?.size ?: 10
    val isQuestCompleted = newCompletedExercises.size >= questSize
    
    val updatedProgress = currentProgress.copy(
        completedExercises = newCompletedExercises,
        isCompleted = isQuestCompleted,
        languagesUsed = if (isQuestCompleted && languagesUsed.isNotEmpty()) languagesUsed else currentProgress.languagesUsed
    )
    
    // Unlock next quest if current one is completed
    val updatedProgresses = travelState.questProgresses.toMutableMap()
    updatedProgresses[questId] = updatedProgress
    
    if (isQuestCompleted) {
        val currentIndex = travelSections.indexOfFirst { it.id == questId }
        if (currentIndex >= 0 && currentIndex + 1 < travelSections.size) {
            val nextQuestId = travelSections[currentIndex + 1].id
            val nextSection = travelSections[currentIndex + 1]
            val nextProgress = updatedProgresses[nextQuestId]
            if (nextProgress != null) {
                if (nextSection.isCompletionBadge) {
                    // Auto-unlock and complete completion badges
                    updatedProgresses[nextQuestId] = nextProgress.copy(
                        isUnlocked = true,
                        isCompleted = true
                    )
                    
                    // Also unlock the first level 2 exercise after completion badge
                    if (currentIndex + 2 < travelSections.size) {
                        val level2Section = travelSections[currentIndex + 2]
                        val level2Progress = updatedProgresses[level2Section.id]
                        if (level2Progress != null && !level2Progress.isUnlocked) {
                            updatedProgresses[level2Section.id] = level2Progress.copy(isUnlocked = true)
                        }
                    }
                } else if (nextSection.isMixed) {
                    // Mixed quest - check language requirements based on quest type
                    val currentLanguageCode = startLangCodeFromQuestId(questId)
                    val canUnlockMixed = when {
                        nextSection.id.endsWith("level2_exercise3") -> {
                            // Level 2 Exercise 3 requires 3+ languages with 10+ encountered words each
                            hasEncounteredWordsInAtLeast3Languages(context)
                        }
                        else -> {
                            // Other mixed quests require equal or higher encountered words in another language
                            currentLanguageCode == null || hasEqualOrHigherEncounteredWordsInOtherLanguage(context, currentLanguageCode)
                        }
                    }
                    
                    if (canUnlockMixed) {
                        updatedProgresses[nextQuestId] = nextProgress.copy(isUnlocked = true)
                    }
                    // If can't unlock mixed, leave it locked (no change to progress)
                } else {
                    // Regular quest - just unlock
                    updatedProgresses[nextQuestId] = nextProgress.copy(isUnlocked = true)
                }
            }
        }
    }
    
    val newTravelState = travelState.copy(questProgresses = updatedProgresses)
    
    // Save quest progress to persistence
    saveQuestProgress(context, updatedProgresses)
    
    // Check if this quest completion unlocks any Level 2 Exercise 3 quests across ALL languages
    // This is needed because Level 2 Exercise 3 uses a global condition (3+ languages with 10+ words)
    android.util.Log.d("QUEST_PROGRESS", "Quest completed: $questId, checking all Level 2 Exercise 3 unlocks...")
    val finalProgresses = checkAndUnlockAllLevel2Exercise3(context, updatedProgresses)
    
    return travelState.copy(questProgresses = finalProgresses)
}

// Reset in-progress quest state (not marking it completed) — clears any partially completed exercises for the quest
fun resetQuestProgress(
    context: Context,
    travelState: TravelState,
    questId: String
): TravelState {
    val currentProgress = travelState.questProgresses[questId] ?: return travelState
    val reset = currentProgress.copy(
        completedExercises = emptySet(),
        isCompleted = false
    )
    val updated = travelState.questProgresses.toMutableMap()
    updated[questId] = reset
    // Persist immediately so the list view reflects the reset when returning
    saveQuestProgress(context, updated)
    return travelState.copy(
        questProgresses = updated,
        currentQuestId = null,
        currentExerciseIndex = 0
    )
}

// Simple persistence for tracking whether the first quest for a language was completed
fun supportedLanguageCodes(): List<String> {
    // This function is now deprecated in favor of getSupportedLanguageCodesFromMetadata
    // Keeping for backward compatibility
    return listOf("en", "es", "fr", "de", "it", "pt", "ru", "ja", "ko", "zh-cn", "nl", "sv")
}

fun getSupportedLanguageCodesFromMetadata(context: Context): List<String> {
    val metadata = loadLanguageMetadata(context)
    return metadata?.optJSONObject("languages")?.keys()?.asSequence()?.toList() ?: emptyList()
}

// Derive canonical set of language codes from the corpus (single source of truth)
fun getLanguageCodesFromCorpus(context: Context): List<String> {
    val entries = loadCorpusFromAssets(context)
    val codes = mutableSetOf<String>()
    entries.forEach { entry ->
        entry.byLang.keys.forEach { code -> codes.add(code) }
    }
    return codes.toList()
}

// Assert that metadata languages align with corpus languages on startup
fun assertCorpusAndMetadataAligned(context: Context) {
    val corpus = getLanguageCodesFromCorpus(context).toSet()
    val meta = getSupportedLanguageCodesFromMetadata(context).toSet()
    require(corpus == meta) {
        "Language metadata mismatch with corpus. corpus=$corpus meta=$meta"
    }
}

fun markFirstQuestCompleted(context: Context, languageCode: String) {
    val prefs = context.getSharedPreferences("hg_progress", Context.MODE_PRIVATE)
    prefs.edit().putBoolean("first_quest_completed_${'$'}languageCode", true).apply()
}

fun isFirstQuestCompleted(context: Context, languageCode: String): Boolean {
    val prefs = context.getSharedPreferences("hg_progress", Context.MODE_PRIVATE)
    return prefs.getBoolean("first_quest_completed_${'$'}languageCode", false)
}

fun countFirstQuestCompletedLanguages(context: Context): Int {
    return getSupportedLanguageCodesFromMetadata(context).count { code -> isFirstQuestCompleted(context, code) }
}

// Encountered words tracking - now using counters
fun addEncounteredWord(context: Context, languageCode: String, word: String) {
    val prefs = context.getSharedPreferences("hg_progress", Context.MODE_PRIVATE)
    val key = "word_count_${languageCode}_$word"
    val currentCount = prefs.getInt(key, 0)
    prefs.edit().putInt(key, currentCount + 1).apply()
}

// Check and unlock all Level 2 Exercise 3 quests across ALL languages when the global condition is met
fun checkAndUnlockAllLevel2Exercise3(context: Context, currentProgresses: Map<String, QuestProgress>): Map<String, QuestProgress> {
    val updatedProgresses = currentProgresses.toMutableMap()
    
    // Debug logging
    android.util.Log.d("MIXED_UNLOCK", "Checking Level 2 Exercise 3 unlocks across all languages...")
    android.util.Log.d("MIXED_UNLOCK", "Has 3+ languages with 10+ words: ${hasEncounteredWordsInAtLeast3Languages(context)}")
    
    // Check all Level 2 Exercise 3 quests across all languages
    val allQuestIds = getAllQuestIds(context)
    allQuestIds.forEach { questId ->
        if (questId.endsWith("level2_exercise3")) {
            val progress = updatedProgresses[questId]
            android.util.Log.d("MIXED_UNLOCK", "Checking quest: $questId, currently unlocked: ${progress?.isUnlocked}")
            
            if (progress != null && !progress.isUnlocked) {
                // Check if the previous quest is completed
                val currentLanguageCode = startLangCodeFromQuestId(questId)
                val previousQuestId = "${currentLanguageCode}_level2_exercise2"
                val previousProgress = updatedProgresses[previousQuestId]
                val isPreviousCompleted = previousProgress?.isCompleted == true
                
                android.util.Log.d("MIXED_UNLOCK", "  Previous quest ($previousQuestId) completed: $isPreviousCompleted")
                
                // Check if the global condition is met
                val canUnlock = isPreviousCompleted && hasEncounteredWordsInAtLeast3Languages(context)
                
                if (canUnlock) {
                    android.util.Log.d("MIXED_UNLOCK", "  ✅ Unlocking quest: $questId")
                    updatedProgresses[questId] = progress.copy(isUnlocked = true)
                } else {
                    android.util.Log.d("MIXED_UNLOCK", "  ❌ Cannot unlock yet")
                }
            }
        }
    }
    
    // Save updated progress
    saveQuestProgress(context, updatedProgresses)
    
    return updatedProgresses
}

// Helper function to get all quest IDs
fun getAllQuestIds(context: Context): List<String> {
    val supportedLangCodes = getSupportedLanguageCodesFromMetadata(context)
    val questIds = mutableListOf<String>()
    
    supportedLangCodes.forEach { langCode ->
        // Add all quest IDs for this language
        questIds.addAll(listOf(
            "${langCode}_level1_exercise1",
            "${langCode}_level1_exercise2", 
            "${langCode}_level1_exercise3",
            "${langCode}_level1_exercise4",
            "${langCode}_level1_exercise5",
            "${langCode}_complete",
            "${langCode}_level2_exercise1",
            "${langCode}_level2_exercise2",
            "${langCode}_level2_exercise3",
            "${langCode}_level2_exercise4",
            "${langCode}_level2_exercise5",
            "${langCode}_level2_complete"
        ))
    }
    
    return questIds
}

fun getEncounteredWords(context: Context, languageCode: String): Set<String> {
    val prefs = context.getSharedPreferences("hg_progress", Context.MODE_PRIVATE)
    val allKeys = prefs.all.keys
    val prefix = "word_count_${languageCode}_"
    return allKeys.filter { it.startsWith(prefix) }
        .map { it.removePrefix(prefix) }
        .toSet()
}

fun getWordExerciseCount(context: Context, languageCode: String, word: String): Int {
    val prefs = context.getSharedPreferences("hg_progress", Context.MODE_PRIVATE)
    return prefs.getInt("word_count_${languageCode}_$word", 0)
}

fun getEncounteredWordsCount(context: Context, languageCode: String): Int {
    return getEncounteredWords(context, languageCode).size
}

fun canUnlockPractice(context: Context): Boolean {
    // Practice is unlocked when user has completed at least one quest in 2 different languages
    val languagesWithCompletedQuests = getSupportedLanguageCodesFromMetadata(context).count { code ->
        getLanguageQuestCount(context, code) >= 1
    }
    return languagesWithCompletedQuests >= 2
}

// Quest progress persistence
fun saveQuestProgress(context: Context, questProgress: Map<String, QuestProgress>) {
    val prefs = context.getSharedPreferences("hg_progress", Context.MODE_PRIVATE)
    val editor = prefs.edit()
    
    questProgress.forEach { (questId, progress) ->
        // Save completed exercises as a string set
        editor.putStringSet("quest_${questId}_completed", progress.completedExercises)
        editor.putBoolean("quest_${questId}_unlocked", progress.isUnlocked)
        editor.putBoolean("quest_${questId}_completed_flag", progress.isCompleted)
        // Save languages used as a string set
        editor.putStringSet("quest_${questId}_languages", progress.languagesUsed.toSet())
    }
    
    editor.apply()
}

fun loadQuestProgress(context: Context, questIds: List<String>): Map<String, QuestProgress> {
    val prefs = context.getSharedPreferences("hg_progress", Context.MODE_PRIVATE)
    val progressMap = mutableMapOf<String, QuestProgress>()
    
    questIds.forEach { questId ->
        val completedExercises = prefs.getStringSet("quest_${questId}_completed", emptySet()) ?: emptySet()
        val isUnlocked = prefs.getBoolean("quest_${questId}_unlocked", false)
        val isCompleted = prefs.getBoolean("quest_${questId}_completed_flag", false)
        val languagesUsed = prefs.getStringSet("quest_${questId}_languages", emptySet())?.toList() ?: emptyList()
        
        progressMap[questId] = QuestProgress(
            questId = questId,
            completedExercises = completedExercises,
            isUnlocked = isUnlocked,
            isCompleted = isCompleted,
            languagesUsed = languagesUsed
        )
    }
    
    return progressMap
}

// Badge progress tracking - based on completed quests
fun incrementLanguageQuestCount(context: Context, languageCode: String) {
    val prefs = context.getSharedPreferences("hg_progress", Context.MODE_PRIVATE)
    val key = "language_quest_count_$languageCode"
    val currentCount = prefs.getInt(key, 0)
    prefs.edit().putInt(key, currentCount + 1).apply()
}

fun getLanguageQuestCount(context: Context, languageCode: String): Int {
    val prefs = context.getSharedPreferences("hg_progress", Context.MODE_PRIVATE)
    return prefs.getInt("language_quest_count_$languageCode", 0)
}

// Extract language code from quest ID (e.g. "de_1" -> "de", "de_mixed" -> "de")
fun startLangCodeFromQuestId(questId: String): String? {
    return questId.split("_").firstOrNull()
}

// Check if user has completed at least one quest in any other language
fun hasCompletedQuestInOtherLanguage(context: Context, currentLanguageCode: String): Boolean {
    return getSupportedLanguageCodesFromMetadata(context)
        .filter { it != currentLanguageCode }
        .any { langCode -> getLanguageQuestCount(context, langCode) > 0 }
}

// Check if user has encountered words in another language with equal or higher count than current language
fun hasEqualOrHigherEncounteredWordsInOtherLanguage(context: Context, currentLanguageCode: String): Boolean {
    val currentWordCount = getEncounteredWordsCount(context, currentLanguageCode)
    return getSupportedLanguageCodesFromMetadata(context)
        .filter { it != currentLanguageCode }
        .any { langCode -> getEncounteredWordsCount(context, langCode) >= currentWordCount }
}

// Check if user has completed at least one quest in at least 3 different languages
fun hasCompletedQuestsInAtLeast3Languages(context: Context): Boolean {
    val languagesWithCompletedQuests = getSupportedLanguageCodesFromMetadata(context)
        .count { langCode -> getLanguageQuestCount(context, langCode) > 0 }
    return languagesWithCompletedQuests >= 3
}

// Check if user has encountered 10+ words in at least 3 different languages
fun hasEncounteredWordsInAtLeast3Languages(context: Context): Boolean {
    val languagesWithEnoughWords = getSupportedLanguageCodesFromMetadata(context)
        .count { langCode -> getEncounteredWordsCount(context, langCode) >= 10 }
    return languagesWithEnoughWords >= 3
}

// Check if user has completed Level 2 Exercise 2 in at least 2 different languages
fun hasCompletedLevel2Exercise2InAtLeast2Languages(context: Context): Boolean {
    val languagesWithLevel2Exercise2 = getSupportedLanguageCodesFromMetadata(context)
        .count { langCode -> 
            val questId = "${langCode}_level2_exercise2"
            val progress = loadQuestProgress(context, listOf(questId))[questId]
            progress?.isCompleted == true
        }
    return languagesWithLevel2Exercise2 >= 2
}

// Check if user has completed Level 2 Exercise 2 in another language (similar to hasEqualOrHigherEncounteredWordsInOtherLanguage)
fun hasCompletedLevel2Exercise2InOtherLanguage(context: Context, currentLanguageCode: String): Boolean {
    return getSupportedLanguageCodesFromMetadata(context)
        .filter { it != currentLanguageCode }
        .any { langCode -> 
            val questId = "${langCode}_level2_exercise2"
            val progress = loadQuestProgress(context, listOf(questId))[questId]
            progress?.isCompleted == true
        }
}

fun getBadgeLevel(context: Context, languageCode: String): BadgeLevel {
    val count = getLanguageQuestCount(context, languageCode)
    return when {
        // 10+ exercises = silver badge (Level 2 Complete)
        count >= 10 -> BadgeLevel.SILVER
        // 5+ exercises = bronze badge (Level 1 Complete)
        count >= 5 -> BadgeLevel.BRONZE
        // 1+ exercises = green badge (starting)
        count >= 1 -> BadgeLevel.GREEN
        else -> BadgeLevel.NONE
    }
}

fun isLevel2CompleteBadge(section: TravelSection): Boolean {
    return section.isCompletionBadge && section.id.endsWith("_level2_complete")
}

fun getLanguageProgress(context: Context, languageCode: String): LanguageProgress {
    val count = getLanguageQuestCount(context, languageCode)
    val badgeLevel = getBadgeLevel(context, languageCode)
    return LanguageProgress(
        languageCode = languageCode,
        completedExercisesCount = count,
        badgeLevel = badgeLevel
    )
}

// Legacy function for backward compatibility - now tracks quests instead of exercises
fun incrementLanguageExerciseCount(context: Context, languageCode: String) {
    // This is now a no-op since we track quests, not individual exercises
}

// Debug function to reset all badge progress
fun resetAllBadgeProgress(context: Context) {
    val prefs = context.getSharedPreferences("hg_progress", Context.MODE_PRIVATE)
    val editor = prefs.edit()
    getSupportedLanguageCodesFromMetadata(context).forEach { languageCode ->
        editor.remove("language_exercise_count_$languageCode")
        editor.remove("language_quest_count_$languageCode")
    }
    editor.apply()
}

// Clear all progress function for settings
fun clearAllProgress(context: Context) {
    val prefs = context.getSharedPreferences("hg_progress", Context.MODE_PRIVATE)
    val editor = prefs.edit()
    
    // Clear all progress-related keys but preserve currency and selected languages
    val allKeys = prefs.all.keys
    allKeys.forEach { key ->
        if (!key.startsWith("user_currency") && !key.startsWith("selected_language")) {
            editor.remove(key)
        }
    }
    
    // Reset welcome screen completion status so it shows again
    editor.remove("welcome_screen_completed")
    // Also reset app launch status so welcome screen shows after clearing progress
    editor.remove("app_ever_launched")
    
    // Reset currency to 10 points
    editor.putInt("user_currency", 10)
    
    // Reset selected languages to default 5 from corpus
    val supportedLanguageCodes = getSupportedLanguageCodesFromMetadata(context)
    val defaultLanguages = supportedLanguageCodes
        .filter { it != "en" } // Exclude English as it's the base language
        .take(5) // Take first 5 languages
        .mapNotNull { code ->
            val name = getLanguageNameFromMetadata(context, code)
            val flag = getLanguageFlagFromMetadata(context, code)
            if (name != null && flag != null) {
                Triple(flag, name, name) // flag, name, language
            } else null
        }
    
    editor.putInt("selected_languages_count", defaultLanguages.size)
    defaultLanguages.forEachIndexed { index, (flag, name, language) ->
        editor.putString("selected_language_${index}_flag", flag)
        editor.putString("selected_language_${index}_name", name)
        editor.putString("selected_language_${index}_language", language)
    }
    
    editor.apply()
}

// Selected languages persistence functions
fun saveSelectedLanguages(context: Context, languages: List<Country>) {
    val prefs = context.getSharedPreferences("hg_progress", Context.MODE_PRIVATE)
    val editor = prefs.edit()
    
    // Save the count
    editor.putInt("selected_languages_count", languages.size)
    
    // Save each language
    languages.forEachIndexed { index, country ->
        editor.putString("selected_language_${index}_flag", country.flag)
        editor.putString("selected_language_${index}_name", country.name)
        editor.putString("selected_language_${index}_language", country.language)
    }
    
    editor.apply()
}

fun loadSelectedLanguages(context: Context): List<Country> {
    val prefs = context.getSharedPreferences("hg_progress", Context.MODE_PRIVATE)
    val count = prefs.getInt("selected_languages_count", 0)
    
    if (count == 0) {
        // Return default languages if none saved - use first 5 languages from corpus
        val supportedLanguageCodes = getSupportedLanguageCodesFromMetadata(context)
        val defaultLanguages = supportedLanguageCodes
            .filter { it != "en" } // Exclude English as it's the base language
            .take(5) // Take first 5 languages
            .mapNotNull { code ->
                val name = getLanguageNameFromMetadata(context, code)
                val flag = getLanguageFlagFromMetadata(context, code)
                if (name != null && flag != null) {
                    Country(flag = flag, name = name, language = name)
                } else null
            }
        return defaultLanguages
    }
    
    val languages = mutableListOf<Country>()
    for (i in 0 until count) {
        val flag = prefs.getString("selected_language_${i}_flag", "") ?: ""
        val name = prefs.getString("selected_language_${i}_name", "") ?: ""
        val language = prefs.getString("selected_language_${i}_language", "") ?: ""
        
        if (flag.isNotEmpty() && name.isNotEmpty() && language.isNotEmpty()) {
            languages.add(Country(flag, name, language))
        }
    }
    
    return languages
}

// Currency persistence functions
fun saveCurrency(context: Context, currency: Int) {
    val prefs = context.getSharedPreferences("hg_progress", Context.MODE_PRIVATE)
    prefs.edit().putInt("user_currency", currency).apply()
}

fun loadCurrency(context: Context): Int {
    val prefs = context.getSharedPreferences("hg_progress", Context.MODE_PRIVATE)
    val baseCurrency = prefs.getInt("user_currency", 10) // Default to 10 points
    
    // In debug mode, start with 500 credits but allow growth beyond that
    return if (loadDebugMode(context)) {
        if (baseCurrency < 500) 500 else baseCurrency
    } else {
        baseCurrency
    }
}

// Debug mode settings
fun saveDebugMode(context: Context, enabled: Boolean) {
    val prefs = context.getSharedPreferences("hg_progress", Context.MODE_PRIVATE)
    prefs.edit().putBoolean("debug_mode_enabled", enabled).apply()
}

fun loadDebugMode(context: Context): Boolean {
    val prefs = context.getSharedPreferences("hg_progress", Context.MODE_PRIVATE)
    return prefs.getBoolean("debug_mode_enabled", false)
}

// Respelling explanation screen functions
fun shouldShowRespellingExplanation(context: Context): Boolean {
    val prefs = context.getSharedPreferences("hg_progress", Context.MODE_PRIVATE)
    val dontShowAgain = prefs.getBoolean("respelling_dont_show_again", false)
    return !dontShowAgain
}

fun markRespellingExplanationSeen(context: Context) {
    val prefs = context.getSharedPreferences("hg_progress", Context.MODE_PRIVATE)
    prefs.edit().putBoolean("respelling_explanation_seen", true).apply()
}

fun setRespellingDontShowAgain(context: Context, dontShow: Boolean) {
    val prefs = context.getSharedPreferences("hg_progress", Context.MODE_PRIVATE)
    prefs.edit().putBoolean("respelling_dont_show_again", dontShow).apply()
}

// Welcome screen functions
fun shouldShowWelcomeScreen(context: Context): Boolean {
    val prefs = context.getSharedPreferences("hg_progress", Context.MODE_PRIVATE)
    val hasCompletedWelcome = prefs.getBoolean("welcome_screen_completed", false)
    val hasEverLaunched = prefs.getBoolean("app_ever_launched", false)
    
    // Show welcome screen only if:
    // 1. App has never been launched before (first time ever)
    // 2. OR welcome screen was explicitly reset (after clearing progress)
    val shouldShow = !hasEverLaunched || !hasCompletedWelcome
    
    // Debug logging
    if (loadDebugMode(context)) {
        println("DEBUG: shouldShowWelcomeScreen - hasEverLaunched: $hasEverLaunched, hasCompletedWelcome: $hasCompletedWelcome, shouldShow: $shouldShow")
    }
    
    return shouldShow
}

fun markWelcomeScreenCompleted(context: Context) {
    val prefs = context.getSharedPreferences("hg_progress", Context.MODE_PRIVATE)
    val editor = prefs.edit()
    editor.putBoolean("welcome_screen_completed", true)
    editor.putBoolean("app_ever_launched", true) // Mark that app has been launched
    editor.apply()
    
    // Debug logging
    if (loadDebugMode(context)) {
        println("DEBUG: markWelcomeScreenCompleted called")
        println("DEBUG: welcome_screen_completed = ${prefs.getBoolean("welcome_screen_completed", false)}")
        println("DEBUG: app_ever_launched = ${prefs.getBoolean("app_ever_launched", false)}")
    }
}

fun markAppLaunched(context: Context) {
    val prefs = context.getSharedPreferences("hg_progress", Context.MODE_PRIVATE)
    prefs.edit().putBoolean("app_ever_launched", true).apply()
}

// Debug function to check welcome screen state
fun debugWelcomeScreenState(context: Context) {
    val prefs = context.getSharedPreferences("hg_progress", Context.MODE_PRIVATE)
    val hasCompletedWelcome = prefs.getBoolean("welcome_screen_completed", false)
    val hasEverLaunched = prefs.getBoolean("app_ever_launched", false)
    val shouldShow = shouldShowWelcomeScreen(context)
    
    println("DEBUG: Welcome Screen State:")
    println("  - hasCompletedWelcome: $hasCompletedWelcome")
    println("  - hasEverLaunched: $hasEverLaunched")
    println("  - shouldShowWelcomeScreen: $shouldShow")
}