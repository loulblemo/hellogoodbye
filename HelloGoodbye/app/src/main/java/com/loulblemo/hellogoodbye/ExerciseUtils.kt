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
    
    // Always aim for 5 pairs
    val desiredTotalPairs = 5
    
    // Build a pool of all possible valid pairs
    val pool = mutableListOf<MatchingPair>()
    for (lang in effectiveLanguageCodes) {
        threeWords.forEach { word ->
            val variant = word.byLang[lang]
            if (variant != null) {
                val left = buildLeft(word, lang, variant)
                val right = buildRight(word, lang, variant)
                if (left != null && right != null) {
                    pool.add(MatchingPair(left, right))
                }
            }
        }
    }
    
    // If pool is empty, try fallback languages
    if (pool.isEmpty()) {
        val fallbackLangs = effectiveLanguageCodes.ifEmpty { listOf("es", "fr", "de", "it", "pt", "ru", "ja", "ko", "zh-cn") }
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
    }
    
    // Shuffle the pool to get random selection
    val shuffledPool = pool.shuffled()
    
    // Select up to 5 pairs, allowing duplicates if necessary
    val selectedPairs = mutableListOf<MatchingPair>()
    val usedRightIds = mutableSetOf<String>()
    
    // First pass: try to get unique pairs
    for (pair in shuffledPool) {
        if (selectedPairs.size >= desiredTotalPairs) break
        if (!usedRightIds.contains(pair.right.id)) {
            selectedPairs.add(pair)
            usedRightIds.add(pair.right.id)
        }
    }
    
    // Second pass: if we still don't have 5 pairs, allow reusing words (cycle through pool)
    var poolIndex = 0
    while (selectedPairs.size < desiredTotalPairs && shuffledPool.isNotEmpty()) {
        val pair = shuffledPool[poolIndex % shuffledPool.size]
        if (!usedRightIds.contains(pair.right.id)) {
            selectedPairs.add(pair)
            usedRightIds.add(pair.right.id)
        }
        poolIndex++
        // Prevent infinite loop
        if (poolIndex > shuffledPool.size * 3) break
    }
    
    return selectedPairs.take(desiredTotalPairs)
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
        buildRight = { word, lang, _ ->
            // Use unique right ID per word to allow multiple pairs per language
            val key = "${lang}_${word.original}"
            PairItem(id = "R_${key}", label = lang.uppercase(), isAudio = false, matchKey = lang)
        },
        restrictToEncounteredLanguages = restrictToEncountered,
        availableLanguages = availableLanguages
    )
}

fun buildAudioToFlagPairsMulti(
    words: List<WordEntry>,
    languageCodes: List<String>,
    restrictToEncountered: Boolean = false,
    availableLanguages: List<String> = emptyList()
): List<MatchingPair> {
    return pickFivePairs(
        words,
        languageCodes,
        buildLeft = { word, lang, variant ->
            val audio = variant.audio ?: return@pickFivePairs null
            val key = "${lang}_${word.original}"
            PairItem(id = "L_${key}", label = "▶︎", isAudio = true, audioFile = audio, matchKey = key)
        },
        buildRight = { word, lang, _ ->
            val key = "${lang}_${word.original}"
            PairItem(id = "R_${key}", label = lang.uppercase(), isAudio = false, matchKey = key)
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
        buildRight = { word, lang, _ ->
            // Use unique right ID per word to allow multiple pairs per language
            val key = "${lang}_${word.original}"
            PairItem(id = "R_${key}", label = lang.uppercase(), isAudio = false, matchKey = lang)
        },
        restrictToEncounteredLanguages = restrictToEncountered,
        availableLanguages = availableLanguages
    )
}

fun buildPronunciationToFlagPairsMulti(
    words: List<WordEntry>,
    languageCodes: List<String>,
    restrictToEncountered: Boolean = false,
    availableLanguages: List<String> = emptyList()
): List<MatchingPair> {
    return pickFivePairs(
        words,
        languageCodes,
        buildLeft = { word, lang, variant ->
            val text = variant.googlePronunciation ?: variant.word ?: variant.text ?: return@pickFivePairs null
            val key = "${lang}_${word.original}"
            PairItem(id = "L_${key}", label = text, isAudio = false, matchKey = key)
        },
        buildRight = { word, lang, _ ->
            val key = "${lang}_${word.original}"
            PairItem(id = "R_${key}", label = lang.uppercase(), isAudio = false, matchKey = key)
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

fun buildPronunciationToEnglishPairsMulti(
    words: List<WordEntry>,
    languageCodes: List<String>,
    restrictToEncountered: Boolean = false,
    availableLanguages: List<String> = emptyList()
): List<MatchingPair> {
    return pickFivePairs(
        words,
        languageCodes,
        buildLeft = { word, lang, variant ->
            val text = variant.googlePronunciation ?: variant.word ?: variant.text ?: return@pickFivePairs null
            val audio = variant.audio ?: return@pickFivePairs null
            val englishLabel = word.byLang["en"]?.text ?: word.byLang["en"]?.word ?: word.original
            val key = englishLabel.lowercase()
            PairItem(id = "L_${lang}_${word.original}_$text", label = text, isAudio = true, audioFile = audio, matchKey = key)
        },
        buildRight = { word, _, _ ->
            val englishLabel = word.byLang["en"]?.text ?: word.byLang["en"]?.word ?: word.original
            val key = englishLabel.lowercase()
            PairItem(id = "R_en_${word.original}", label = englishLabel, isAudio = false, matchKey = key)
        },
        restrictToEncounteredLanguages = restrictToEncountered,
        availableLanguages = availableLanguages
    )
}