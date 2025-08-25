package com.loulblemo.hellogoodbye

import android.content.Context

fun trackWordsFromExercise(
    context: Context, 
    section: TravelSection, 
    corpus: List<WordEntry>, 
    languageCodes: List<String>
) {
    // Sample a few words from the corpus for this exercise and mark them as encountered
    val sampleWords = corpus.shuffled().take(3)
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

fun seedNewWordsForQuest(
    context: Context,
    corpus: List<WordEntry>,
    mainLanguageCode: String,
    newWordsCount: Int
) {
    if (newWordsCount <= 0) return
    val pool = corpus.shuffled()
    var added = 0
    for (entry in pool) {
        val variant = entry.byLang[mainLanguageCode]
        if (variant != null) {
            val word = variant.word ?: variant.text ?: entry.original
            addEncounteredWord(context, mainLanguageCode, word)
            added += 1
            if (added >= newWordsCount) break
        }
    }
}
