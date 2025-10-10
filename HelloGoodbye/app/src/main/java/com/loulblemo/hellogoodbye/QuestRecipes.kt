package com.loulblemo.hellogoodbye

enum class QuestType {
    SINGLE,
    MIXED
}

data class QuestRecipe(
    val type: QuestType,
    val languageCount: Int,
    val exerciseOrder: List<String>,
    val randomOrder: Boolean,
    val wordRange: IntRange? = null, // Range of words from corpus to use (e.g., 0..4 for first 5 words)
    val useEncounteredWords: Boolean = false // For mixed exercises and practice mode
)

fun defaultQuestRecipes(): List<QuestRecipe> {
    // Define per-level quest recipes
    // Level 1 Exercise 1: Single language, fixed order
    val level1_exercise1 = QuestRecipe(
        type = QuestType.SINGLE,
        languageCount = 1,
        exerciseOrder = listOf(
            "flashcards",
            "pronunciation_audio_to_english",
            "pronunciation_audio_to_english",
            "pronunciation_audio_to_english",
            "audio_to_english_multi",
            "audio_to_english_multi",
            "pronunciation_to_english_multi",
            "pronunciation_to_english_multi",
        ),
        randomOrder = false,
        wordRange = 0..2 // First 3 words: hello, goodbye, please
    )

    // Level 1 Exercise 2: Single language, random order same pool
    val level1_exercise2 = QuestRecipe(
        type = QuestType.SINGLE,
        languageCount = 1,
        exerciseOrder = listOf(
            "flashcards",
            "pronunciation_audio_to_english",
            "pronunciation_audio_to_english",
            "pronunciation_audio_to_english",
            "pronunciation_audio_to_english",
            "pronunciation_audio_to_english",
            "audio_to_english_multi",
            "audio_to_english_multi",
            "pronunciation_to_english_multi",
            "pronunciation_to_english_multi",
        ),
        randomOrder = false,
        wordRange = 0..4 // Same 5 words as Level 1 Exercise 1
    )

    // Level 1 Exercise 3: Mixed, 2 languages, include flag-based rounds, random order
    val level1_exercise3 = QuestRecipe(
        type = QuestType.MIXED,
        languageCount = 2,
        exerciseOrder = listOf(
            "audio_to_flag_multi",
            "pronunciation_to_flag_multi",
            "audio_to_english_multi",
            "audio_to_flag_multi",
            "pronunciation_to_flag_multi",
            "audio_to_english_multi",
            "audio_to_flag_multi",
            "pronunciation_to_flag_multi"
        ),
        randomOrder = true,
        useEncounteredWords = true // Mixed exercises use encountered words
    )

    // Level 1 Exercise 4: Single language, fixed order
    val level1_exercise4 = QuestRecipe(
        type = QuestType.SINGLE,
        languageCount = 1,
        exerciseOrder = listOf(
            "audio_to_english_multi",
            "pronunciation_audio_to_english",
            "audio_to_english_multi",
            "pronunciation_audio_to_type_english",
            "pronunciation_audio_to_english",
        ),
        randomOrder = true,
        wordRange = 0..4 // Same 5 words as other Level 1 exercises
    )

    // Level 1 Exercise 5: Single language, random order
    val level1_exercise5 = QuestRecipe(
        type = QuestType.SINGLE,
        languageCount = 1,
        exerciseOrder = listOf(
            "audio_to_english_multi",
            "pronunciation_audio_to_english",
            "audio_to_english_multi",
            "pronunciation_audio_to_type_english",
            "pronunciation_audio_to_english",
        ),
        randomOrder = true,
        wordRange = 0..4 // Same 5 words as other Level 1 exercises
    )

    // Level 2 Exercise 1: Single language, fixed order (appears after Level 1 Complete badge)
    val level2_exercise1 = QuestRecipe(
        type = QuestType.SINGLE,
        languageCount = 1,
        exerciseOrder = listOf(
            "flashcards",
            "pronunciation_audio_to_english",
            "pronunciation_audio_to_english",
            "pronunciation_audio_to_english",
            "audio_to_english_multi",
            "audio_to_english_multi",
            "pronunciation_to_english_multi",
            "pronunciation_to_english_multi",
        ),
        randomOrder = false,
        wordRange = 5..7 // Next 3 words: help, sorry, water, yes, no
    )

    val level2_exercise2 = QuestRecipe(
        type = QuestType.SINGLE,
        languageCount = 1,
        exerciseOrder = listOf(
            "flashcards",
            "pronunciation_audio_to_english",
            "pronunciation_audio_to_english",
            "pronunciation_audio_to_english",
            "pronunciation_audio_to_english",
            "pronunciation_audio_to_english",
            "audio_to_english_multi",
            "audio_to_english_multi",
            "pronunciation_to_english_multi",
            "pronunciation_to_english_multi",
        ),
        randomOrder = false,
        wordRange = 5..9 // Next 5 words: help, sorry, water, yes, no
    )
    val level2_exercise3 = QuestRecipe(
        type = QuestType.MIXED,
        languageCount = 3,
        exerciseOrder = listOf(
            "audio_to_flag_multi",
            "pronunciation_to_flag_multi",
            "audio_to_english_multi",
            "audio_to_flag_multi",
            "pronunciation_to_flag_multi",
            "audio_to_english_multi",
            "audio_to_flag_multi",
            "pronunciation_to_flag_multi",
            "pronunciation_audio_to_type_english"
        ),
        randomOrder = true,
        useEncounteredWords = true, // Mixed exercises use encountered words
    )
    val level2_exercise4 = QuestRecipe(
        type = QuestType.SINGLE,
        languageCount = 1,
        exerciseOrder = listOf(
            "pronunciation_audio_to_english",
            "audio_to_english_multi",
            "pronunciation_audio_to_english",
            "audio_to_english_multi",
            "pronunciation_audio_to_english",
            "audio_to_english_multi",
            "pronunciation_audio_to_english",
            "audio_to_english_multi",
            "pronunciation_audio_to_english",
            "audio_to_english_multi"
        ),
        randomOrder = false,
        wordRange = 0..9 // Same 5 words as other Level 1 exercises
    )

    // Level 1 Exercise 5: Single language, random order
    val level2_exercise5 = QuestRecipe(
        type = QuestType.SINGLE,
        languageCount = 1,
        exerciseOrder = List(10) { if (it % 2 == 0) "audio_to_english_multi" else "pronunciation_audio_to_english" },
        randomOrder = true,
        wordRange = 0..9 // Same 5 words as other Level 1 exercises
    )

    return listOf(level1_exercise1, level1_exercise2, level1_exercise3, level1_exercise4, level1_exercise5, level2_exercise1, level2_exercise2, level2_exercise3, level2_exercise4, level2_exercise5)
}



// Example of a quest recipe that uses a specific word range
fun createCustomWordRangeRecipe(): QuestRecipe {
    return QuestRecipe(
        type = QuestType.SINGLE,
        languageCount = 1,
        exerciseOrder = listOf(
            "pronunciation_audio_to_english",
            "audio_to_english_multi",
            "pronunciation_audio_to_english",
            "audio_to_english_multi"
        ),
        randomOrder = false,
        wordRange = 0..2 // First 3 words only
    )
}

fun generateTravelSectionsFromRecipes(startLangCode: String, recipes: List<QuestRecipe>): List<TravelSection> {
    val startName = languageCodeToName(startLangCode) ?: "English"
    val startFlag = languageCodeToFlag(startLangCode) ?: "🇺🇸"
    val sections = mutableListOf<TravelSection>()

    // Level 1: First 5 exercises (indices 0-4)
    for (i in 0..4) {
        val recipe = recipes[i]
        val isMixed = recipe.type == QuestType.MIXED
        val exerciseNumber = i + 1
        sections.add(
            TravelSection(
                id = "${startLangCode}_level1_exercise${exerciseNumber}",
                flag = if (isMixed) "🌍" else startFlag,
                name = "Level 1 Exercise $exerciseNumber",
                language = if (isMixed) "Mixed" else startName,
                isMixed = isMixed,
                languages = listOf(startLangCode)
            )
        )
    }

    // Level 1 Complete badge
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

    // Level 2: Remaining exercises (starting from index 5)
    for (i in 5 until recipes.size) {
        val recipe = recipes[i]
        val isMixed = recipe.type == QuestType.MIXED
        val exerciseNumber = i - 4 // Level 2 exercises start from 1
        sections.add(
            TravelSection(
                id = "${startLangCode}_level2_exercise${exerciseNumber}",
                flag = if (isMixed) "🌍" else startFlag,
                name = "Level 2 Exercise $exerciseNumber",
                language = if (isMixed) "Mixed" else startName,
                isMixed = isMixed,
                languages = listOf(startLangCode)
            )
        )
    }

    // Level 2 Complete badge
    sections.add(
        TravelSection(
            id = "${startLangCode}_level2_complete",
            flag = "🥈",
            name = "Level 2 Complete!",
            language = "Completion",
            isMixed = false,
            languages = listOf(startLangCode),
            isCompletionBadge = true
        )
    )

    return sections
}

fun recipeForQuestId(recipes: List<QuestRecipe>, questId: String): QuestRecipe? {
    val parts = questId.split("_")
    
    // Handle new naming pattern: level1_exercise1, level1_exercise2, level2_exercise1, etc.
    if (parts.size >= 3 && parts[1].startsWith("level") && parts[2].startsWith("exercise")) {
        val level = parts[1].removePrefix("level").toIntOrNull() ?: return null
        val exercise = parts[2].removePrefix("exercise").toIntOrNull() ?: return null
        
        return when {
            level == 1 && exercise in 1..5 -> recipes.getOrNull(exercise - 1) // Level 1 exercises: indices 0-4
            level == 2 && exercise >= 1 -> recipes.getOrNull(4 + exercise) // Level 2 exercises: start at index 5
            else -> null
        }
    }
    
    // Fallback to old pattern for backward compatibility
    val idx = parts.lastOrNull()?.toIntOrNull() ?: return null
    val zeroBased = idx - 1
    if (zeroBased < 0 || zeroBased >= recipes.size) return null
    return recipes[zeroBased]
}

fun mapExerciseIdToType(section: TravelSection, id: String): ExerciseType {
    val base = id.removeSuffix("_multi")
    return when (base) {
        "audio_to_flag" -> ExerciseType(
            id = id,
            title = "Match Audio to Flag",
            description = "Listen and match the audio to the correct flag"
        )
        "pronunciation_to_flag" -> ExerciseType(
            id = id,
            title = "Match Pronunciation to Flag",
            description = "Match written pronunciation to the correct flag"
        )
        "audio_to_english" -> ExerciseType(
            id = id,
            title = "Match Audio to Translation",
            description = "Listen and match the audio to the correct translation"
        )
        "pronunciation_audio_to_english" -> ExerciseType(
            id = id,
            title = "Pronunciation + Audio to Translation",
            description = "See pronunciation, hear audio, choose the correct meaning"
        )
        "pronunciation_audio_to_type_english" -> ExerciseType(
            id = id,
            title = "Type the Translation",
            description = "See pronunciation, hear audio, type the correct English meaning"
        )
        "pronunciation_to_english" -> ExerciseType(
            id = id,
            title = "Pronunciation to Translation",
            description = "See pronunciation and match to the correct meaning"
        )
        "pronunciation_to_english_multi" -> ExerciseType(
            id = id,
            title = "Pronunciation to Translation (5 pairs)",
            description = "See pronunciation, hear audio, match to the correct meaning"
        )
        "flashcards" -> ExerciseType(
            id = id,
            title = "Flashcards",
            description = "Swipe through all words to complete the exercise"
        )
        else -> ExerciseType(
            id = id,
            title = "Pronunciation + Audio to Translation",
            description = "See pronunciation, hear audio, choose the correct meaning"
        )
    }
}


