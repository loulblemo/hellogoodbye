package com.loulblemo.hellogoodbye

private fun pickFivePairs(
    threeWords: List<WordEntry>,
    languageCodes: List<String>,
    buildLeft: (WordEntry, String, WordVariant) -> PairItem?,
    buildRight: (WordEntry, String, WordVariant) -> PairItem?,
    restrictToEncounteredLanguages: Boolean = false,
    availableLanguages: List<String> = emptyList()
): List<MatchingPair> {
    val combos = mutableListOf<MatchingPair>()
    
    // Use restricted languages if this is for mixed exercises with encountered words
    val effectiveLanguageCodes = if (restrictToEncounteredLanguages && availableLanguages.isNotEmpty()) {
        availableLanguages.intersect(languageCodes.toSet()).toList()
    } else {
        languageCodes
    }
    
    val shuffledLangs = effectiveLanguageCodes.shuffled()
    val targets = shuffledLangs.take(if (shuffledLangs.size >= 5) 5 else shuffledLangs.size)
    
    // Distribute three words across target languages
    var wordIdx = 0
    for (lang in targets) {
        val word = threeWords[wordIdx % threeWords.size]
        val variant = word.byLang[lang]
        if (variant != null) {
            val left = buildLeft(word, lang, variant)
            val right = buildRight(word, lang, variant)
            if (left != null && right != null) {
                combos.add(MatchingPair(left, right))
            }
        }
        wordIdx += 1
    }
    
    // If fewer than 5 pairs (e.g., not enough languages), try to fill with additional random combos
    if (combos.size < 5) {
        val fallbackLangs = if (restrictToEncounteredLanguages && availableLanguages.isNotEmpty()) {
            availableLanguages
        } else {
            effectiveLanguageCodes.ifEmpty { listOf("es", "fr", "de", "it", "pt", "ru", "ja", "ko", "zh-cn") }
        }
        
        val pool = mutableListOf<MatchingPair>()
        for (lang in fallbackLangs) {
            threeWords.forEach { word ->
                val variant = word.byLang[lang]
                if (variant != null) {
                    val left = buildLeft(word, lang, variant)
                    val right = buildRight(word, lang, variant)
                    if (left != null && right != null) pool.add(MatchingPair(left, right))
                }
            }
        }
        val existingRightIds = combos.map { it.right.id }.toMutableSet()
        pool.shuffled().forEach {
            if (combos.size >= 5) return@forEach
            if (!existingRightIds.contains(it.right.id)) {
                combos.add(it)
                existingRightIds.add(it.right.id)
            }
        }
    }
    return combos.take(5)
}

fun buildAudioToFlagPairs(
    threeWords: List<WordEntry>, 
    languageCodes: List<String>,
    restrictToEncountered: Boolean = false,
    availableLanguages: List<String> = emptyList()
): List<MatchingPair> {
    return pickFivePairs(
        threeWords,
        languageCodes,
        buildLeft = { _, lang, variant ->
            val audio = variant.audio ?: return@pickFivePairs null
            PairItem(id = "L_${lang}_$audio", label = "▶︎", isAudio = true, audioFile = audio, matchKey = lang)
        },
        buildRight = { _, lang, _ ->
            val flag = languageCodeToFlag(lang)
            PairItem(id = "R_${lang}", label = flag ?: lang.uppercase(), isAudio = false, matchKey = lang)
        },
        restrictToEncounteredLanguages = restrictToEncountered,
        availableLanguages = availableLanguages
    )
}

fun buildPronunciationToFlagPairs(
    threeWords: List<WordEntry>, 
    languageCodes: List<String>,
    restrictToEncountered: Boolean = false,
    availableLanguages: List<String> = emptyList()
): List<MatchingPair> {
    return pickFivePairs(
        threeWords,
        languageCodes,
        buildLeft = { _, lang, variant ->
            val text = variant.googlePronunciation ?: variant.word ?: variant.text ?: return@pickFivePairs null
            PairItem(id = "L_${lang}_$text", label = text, isAudio = false, matchKey = lang)
        },
        buildRight = { _, lang, _ ->
            val flag = languageCodeToFlag(lang)
            PairItem(id = "R_${lang}", label = flag ?: lang.uppercase(), isAudio = false, matchKey = lang)
        },
        restrictToEncounteredLanguages = restrictToEncountered,
        availableLanguages = availableLanguages
    )
}

fun buildAudioToEnglishPairs(
    threeWords: List<WordEntry>, 
    languageCodes: List<String>,
    restrictToEncountered: Boolean = false,
    availableLanguages: List<String> = emptyList()
): List<MatchingPair> {
    return pickFivePairs(
        threeWords,
        languageCodes,
        buildLeft = { word, lang, variant ->
            val audio = variant.audio ?: return@pickFivePairs null
            // Always use English translation for the label
            val englishLabel = word.byLang["en"]?.text ?: word.byLang["en"]?.word ?: word.original
            val key = englishLabel.lowercase()
            PairItem(id = "L_${lang}_${word.original}", label = "▶︎", isAudio = true, audioFile = audio, matchKey = key)
        },
        buildRight = { word, lang, _ ->
            // Always use English translation for the label
            val englishLabel = word.byLang["en"]?.text ?: word.byLang["en"]?.word ?: word.original
            val key = englishLabel.lowercase()
            PairItem(id = "R_${lang}_${word.original}", label = englishLabel, isAudio = false, matchKey = key)
        },
        restrictToEncounteredLanguages = restrictToEncountered,
        availableLanguages = availableLanguages
    )
}

fun buildPronunciationToEnglishPairs(
    threeWords: List<WordEntry>, 
    languageCodes: List<String>,
    restrictToEncountered: Boolean = false,
    availableLanguages: List<String> = emptyList()
): List<MatchingPair> {
    return pickFivePairs(
        threeWords,
        languageCodes,
        buildLeft = { word, lang, variant ->
            val text = variant.googlePronunciation ?: variant.word ?: variant.text ?: return@pickFivePairs null
            // Always use English translation for the label
            val englishLabel = word.byLang["en"]?.text ?: word.byLang["en"]?.word ?: word.original
            val key = englishLabel.lowercase()
            PairItem(id = "L_${lang}_${word.original}_$text", label = text, isAudio = false, matchKey = key)
        },
        buildRight = { word, lang, _ ->
            // Always use English translation for the label
            val englishLabel = word.byLang["en"]?.text ?: word.byLang["en"]?.word ?: word.original
            val key = englishLabel.lowercase()
            PairItem(id = "R_${lang}_${word.original}", label = englishLabel, isAudio = false, matchKey = key)
        },
        restrictToEncounteredLanguages = restrictToEncountered,
        availableLanguages = availableLanguages
    )
}