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
    val isEarlyQuest = !section.isMixed && section.id.split("_").lastOrNull()?.toIntOrNull()?.let { it in 1..5 } == true
    val mainLanguageCode = languageCodes.firstOrNull()
    val earlyWords = if (isEarlyQuest && mainLanguageCode != null) getEarlyWords(context, mainLanguageCode) else emptySet()
    languageCodes.forEach { langCode ->
        sampleWords.forEach { wordEntry ->
            val variant = wordEntry.byLang[langCode]
            if (variant != null) {
                val word = variant.word ?: variant.text ?: wordEntry.original
                if (!isEarlyQuest || earlyWords.isEmpty() || earlyWords.contains(word)) {
                    addEncounteredWord(context, langCode, word)
                }
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
    val existingEarly = getEarlyWords(context, mainLanguageCode)
    if (existingEarly.isNotEmpty()) {
        existingEarly.forEach { w -> addEncounteredWord(context, mainLanguageCode, w) }
        return
    }
    val pool = corpus.shuffled()
    val chosen = mutableListOf<String>()
    for (entry in pool) {
        val variant = entry.byLang[mainLanguageCode]
        if (variant != null) {
            val word = variant.word ?: variant.text ?: entry.original
            if (!chosen.contains(word)) {
                chosen.add(word)
            }
            if (chosen.size >= newWordsCount) break
        }
    }
    if (chosen.isNotEmpty()) {
        saveEarlyWords(context, mainLanguageCode, chosen.toSet())
        chosen.forEach { w -> addEncounteredWord(context, mainLanguageCode, w) }
    }
}

// Early-words persistence helpers for enforcing a fixed 5-word set in early quests
fun getEarlyWords(context: Context, languageCode: String): Set<String> {
    val prefs = context.getSharedPreferences("hg_progress", Context.MODE_PRIVATE)
    return prefs.getStringSet("early_words_$languageCode", emptySet()) ?: emptySet()
}

fun saveEarlyWords(context: Context, languageCode: String, words: Set<String>) {
    val prefs = context.getSharedPreferences("hg_progress", Context.MODE_PRIVATE)
    prefs.edit().putStringSet("early_words_$languageCode", words).apply()
}
