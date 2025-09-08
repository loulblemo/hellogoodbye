package com.loulblemo.hellogoodbye

import android.content.Context

fun trackWordsFromExercise(
    context: Context, 
    section: TravelSection, 
    corpus: List<WordEntry>, 
    languageCodes: List<String>
) {
    // Sample a few words from the corpus for this exercise and mark them as encountered
    // Use corpus in order instead of shuffled for consistency
    val sampleWords = corpus.take(3)
    languageCodes.forEach { langCode ->
        sampleWords.forEach { wordEntry ->
            val variant = wordEntry.byLang[langCode]
            if (variant != null) {
                val word = variant.word ?: variant.text ?: wordEntry.original
                addEncounteredWord(context, langCode, word)
            }
        }
    }
}

// Note: Early words and new words tracking functions have been removed
// The new system uses corpus ranges directly from QuestRecipe.wordRange
