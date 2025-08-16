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
    val languages: List<String> = emptyList()
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
    val currentExerciseIndex: Int = 0
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

fun getExerciseTypes(): List<ExerciseType> {
    return listOf(
        ExerciseType("audio_to_flag", "Match Audio to Flag", "Listen and match the audio to the correct flag"),
        ExerciseType("pronunciation_to_flag", "Match Pronunciation to Flag", "Match written pronunciation to the correct flag"),
        ExerciseType("audio_to_english", "Match Audio to English", "Listen and match the audio to the English translation")
    )
}

fun initializeTravelState(travelSections: List<TravelSection>): TravelState {
    val questProgresses = travelSections.mapIndexed { index, section ->
        section.id to QuestProgress(
            questId = section.id,
            isUnlocked = index == 0 // Only first quest is unlocked initially
        )
    }.toMap()
    
    return TravelState(
        questProgresses = questProgresses,
        currentQuestId = null
    )
}

fun updateQuestProgress(
    travelState: TravelState,
    questId: String,
    completedExerciseId: String,
    travelSections: List<TravelSection>
): TravelState {
    val exerciseTypes = getExerciseTypes()
    val currentProgress = travelState.questProgresses[questId] ?: return travelState
    
    val newCompletedExercises = currentProgress.completedExercises + completedExerciseId
    val isQuestCompleted = newCompletedExercises.size >= exerciseTypes.size
    
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
            val nextProgress = updatedProgresses[nextQuestId]
            if (nextProgress != null) {
                updatedProgresses[nextQuestId] = nextProgress.copy(isUnlocked = true)
            }
        }
    }
    
    return travelState.copy(questProgresses = updatedProgresses)
}