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
    val newWordsFromMain: Int
)

fun defaultQuestRecipes(): List<QuestRecipe> {
    // Define per-level quest recipes
    // Level 1: Single language, fixed order
    val level1 = QuestRecipe(
        type = QuestType.SINGLE,
        languageCount = 1,
        exerciseOrder = listOf(
            "audio_to_english_multi",
            "pronunciation_audio_to_english",
            "audio_to_english_multi",
            "pronunciation_audio_to_english",
            "audio_to_english_multi",
            "pronunciation_audio_to_english",
            "audio_to_english_multi",
            "pronunciation_audio_to_english",
            "audio_to_english_multi",
            "pronunciation_audio_to_english"
        ),
        randomOrder = false,
        newWordsFromMain = 5
    )

    // Level 2: Single language, random order same pool
    val level2 = QuestRecipe(
        type = QuestType.SINGLE,
        languageCount = 1,
        exerciseOrder = listOf(
            "audio_to_english_multi",
            "pronunciation_audio_to_english"
        ).let { base -> List(10) { base[it % base.size] } },
        randomOrder = true,
        newWordsFromMain = 0
    )

    // Level 3: Mixed, 2 languages, include flag-based rounds, random order
    val level3 = QuestRecipe(
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
        newWordsFromMain = 0
    )

    // Level 4: Single language, fixed order
    val level4 = QuestRecipe(
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
        newWordsFromMain = 5
    )

    // Level 5: Single language, random order
    val level5 = QuestRecipe(
        type = QuestType.SINGLE,
        languageCount = 1,
        exerciseOrder = List(10) { if (it % 2 == 0) "audio_to_english_multi" else "pronunciation_audio_to_english" },
        randomOrder = true,
        newWordsFromMain = 5
    )

    return listOf(level1, level2, level3, level4, level5)
}

fun generateTravelSectionsFromRecipes(startLangCode: String, recipes: List<QuestRecipe>): List<TravelSection> {
    val startName = languageCodeToName(startLangCode) ?: "English"
    val startFlag = languageCodeToFlag(startLangCode) ?: "🇺🇸"
    val sections = mutableListOf<TravelSection>()

    recipes.forEachIndexed { index, recipe ->
        val isMixed = recipe.type == QuestType.MIXED
        sections.add(
            TravelSection(
                id = "${startLangCode}_${index + 1}",
                flag = if (isMixed) "🌍" else startFlag,
                name = if (isMixed) "Mixed" else startName,
                language = if (isMixed) "Mixed" else startName,
                isMixed = isMixed,
                languages = listOf(startLangCode)
            )
        )
    }

    sections.add(
        TravelSection(
            id = "${startLangCode}_complete",
            flag = "🥈",
            name = "Level 1 Complete!",
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
        else -> ExerciseType(
            id = id,
            title = "Pronunciation + Audio to Translation",
            description = "See pronunciation, hear audio, choose the correct meaning"
        )
    }
}


