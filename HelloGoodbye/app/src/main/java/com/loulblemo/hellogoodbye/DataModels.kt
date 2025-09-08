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
    val isCompleted: Boolean = false
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
    BRONZE,   // Completed at least one quest
    SILVER    // Completed 5 quests in the language
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
                    googlePronunciation = langObj.optString("google_pronunciation", null),
                    audio = langObj.optString("audio", null)
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
    if (languageMetadata == null) {
        languageMetadata = runCatching {
            val jsonString = context.assets.open("language_metadata.json").bufferedReader().use { it.readText() }
            JSONObject(jsonString)
        }.getOrElse { null }
    }
    return languageMetadata
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
            flag = "🥉",
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
            val hasOtherLanguageQuest = currentLanguageCode == null || hasCompletedQuestInOtherLanguage(context, currentLanguageCode)
            
            if (isPreviousCompleted && hasOtherLanguageQuest) {
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
    questExercises: Map<String, List<ExerciseType>>
): TravelState {
    val currentProgress = travelState.questProgresses[questId] ?: return travelState
    
    val newCompletedExercises = currentProgress.completedExercises + completedExerciseId
    val questSize = questExercises[questId]?.size ?: 10
    val isQuestCompleted = newCompletedExercises.size >= questSize
    
    val updatedProgress = currentProgress.copy(
        completedExercises = newCompletedExercises,
        isCompleted = isQuestCompleted
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
                    // Mixed quest - check if user has completed at least one quest in another language
                    val currentLanguageCode = startLangCodeFromQuestId(questId)
                    val canUnlockMixed = currentLanguageCode == null || hasCompletedQuestInOtherLanguage(context, currentLanguageCode)
                    
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
    
    return newTravelState
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
        
        progressMap[questId] = QuestProgress(
            questId = questId,
            completedExercises = completedExercises,
            isUnlocked = isUnlocked,
            isCompleted = isCompleted
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

fun getBadgeLevel(context: Context, languageCode: String): BadgeLevel {
    val count = getLanguageQuestCount(context, languageCode)
    return when {
        // For now: 1 quest = green badge, 5 quests = bronze badge
        // Map GREEN -> SILVER enum, BRONZE -> BRONZE enum
        count >= 5 -> BadgeLevel.BRONZE
        count >= 1 -> BadgeLevel.SILVER
        else -> BadgeLevel.NONE
    }
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
    
    // Reset currency to 50 points
    editor.putInt("user_currency", 50)
    
    // Reset selected languages to default 5
    editor.putInt("selected_languages_count", 5)
    editor.putString("selected_language_0_flag", "🇪🇸")
    editor.putString("selected_language_0_name", "Spain")
    editor.putString("selected_language_0_language", "Spanish")
    editor.putString("selected_language_1_flag", "🇫🇷")
    editor.putString("selected_language_1_name", "France")
    editor.putString("selected_language_1_language", "French")
    editor.putString("selected_language_2_flag", "🇩🇪")
    editor.putString("selected_language_2_name", "Germany")
    editor.putString("selected_language_2_language", "German")
    editor.putString("selected_language_3_flag", "🇮🇹")
    editor.putString("selected_language_3_name", "Italy")
    editor.putString("selected_language_3_language", "Italian")
    editor.putString("selected_language_4_flag", "🇯🇵")
    editor.putString("selected_language_4_name", "Japan")
    editor.putString("selected_language_4_language", "Japanese")
    
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
        // Return default languages if none saved
        return listOf(
            Country("🇪🇸", "Spain", "Spanish"),
            Country("🇫🇷", "France", "French"),
            Country("🇩🇪", "Germany", "German"),
            Country("🇮🇹", "Italy", "Italian"),
            Country("🇯🇵", "Japan", "Japanese")
        )
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
    val baseCurrency = prefs.getInt("user_currency", 50) // Default to 50 points
    
    // In debug mode, give user 500 credits
    return if (loadDebugMode(context)) 500 else baseCurrency
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