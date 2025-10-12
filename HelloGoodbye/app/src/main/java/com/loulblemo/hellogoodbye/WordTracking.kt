package com.loulblemo.hellogoodbye

import android.content.Context

fun trackWordsFromExercise(
    context: Context, 
    section: TravelSection, 
    corpus: List<WordEntry>, 
    languageCodes: List<String>,
    questRecipe: QuestRecipe? = null
) {
    // Debug logging
    android.util.Log.d("WORD_TRACKING", "Tracking words for quest: ${section.id}")
    android.util.Log.d("WORD_TRACKING", "Quest recipe: $questRecipe")
    
    // Determine which words to track based on quest recipe
    val wordsToTrack = when {
        questRecipe?.wordRange != null -> {
            // Use specific word range from quest recipe
            val range = questRecipe.wordRange
            val startIndex = range.first.coerceAtLeast(0)
            val endIndex = range.last.coerceAtMost(corpus.size - 1)
            if (startIndex <= endIndex) {
                corpus.subList(startIndex, endIndex + 1)
            } else {
                emptyList()
            }
        }
        questRecipe?.useEncounteredWords == true -> {
            // Mixed exercises don't track new words - they only use words already encountered
            emptyList()
        }
        else -> {
            // Fallback: track first 3 words (original behavior)
            corpus.take(3)
        }
    }
    
    android.util.Log.d("WORD_TRACKING", "Words to track: ${wordsToTrack.size} words")
    
    languageCodes.forEach { langCode ->
        wordsToTrack.forEach { wordEntry ->
            val variant = wordEntry.byLang[langCode]
            if (variant != null) {
                val word = variant.word ?: variant.text ?: wordEntry.original
                android.util.Log.d("WORD_TRACKING", "Adding word '$word' for language '$langCode'")
                addEncounteredWord(context, langCode, word)
            }
        }
    }
}

// Note: Early words and new words tracking functions have been removed
// The new system uses corpus ranges directly from QuestRecipe.wordRange
