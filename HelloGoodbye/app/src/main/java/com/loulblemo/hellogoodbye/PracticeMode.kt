package com.loulblemo.hellogoodbye

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PracticeScreen(
    selectedCountries: List<Country>,
    onExit: () -> Unit,
    onAwardCoin: () -> Unit
) {
    val context = LocalContext.current
    val corpus by remember {
        mutableStateOf(loadCorpusFromAssets(context))
    }
    val languageCodes = remember(selectedCountries) {
        selectedCountries.mapNotNull { languageNameToCode(it.language) }.distinct()
    }
    
    // Generate 10 diverse exercises for practice mode
    val practiceExercises = remember(languageCodes) {
        val exerciseTypes = listOf(
            "audio_to_flag",
            "pronunciation_to_flag", 
            "audio_to_english",
            "pronunciation_audio_to_english",
            "pronunciation_to_english_multi",
            "pronunciation_audio_to_type_english"
        )
        // Generate 10 exercises with variety
        val exercises = mutableListOf<String>()
        repeat(10) { index ->
            // Ensure variety by cycling through exercise types
            val exerciseType = exerciseTypes[index % exerciseTypes.size]
            exercises.add(exerciseType)
        }
        exercises.shuffled() // Randomize the order
    }
    
    var currentExerciseIndex by remember { mutableStateOf(0) }
    var correctExercises by remember { mutableStateOf(0) }
    var showCompletionScreen by remember { mutableStateOf(false) }
    var showCompletionAnimation by remember { mutableStateOf(false) }

    val onExerciseDone: (Boolean) -> Unit = { perfect ->
        if (perfect) {
            correctExercises += 1
            onAwardCoin()
        }
        if (!showCompletionAnimation) { // Prevent multiple triggers
            showCompletionAnimation = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Practice",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            ResponsiveRedCross(onClick = onExit)
        }
        
        // Progress bar only (no flag in practice mode)
        LinearProgressIndicator(
            progress = (currentExerciseIndex + 1).toFloat() / practiceExercises.size,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(12.dp),
            color = MaterialTheme.colorScheme.primary, // Dark purple brand color
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        if (corpus.isEmpty() || languageCodes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Add languages first")
            }
            return
        }

        val encounteredWords = remember(corpus, languageCodes) {
            val encountered = mutableSetOf<WordEntry>()
            languageCodes.forEach { langCode ->
                val encounteredWordsForLang = getEncounteredWords(context, langCode)
                corpus.forEach { wordEntry ->
                    val variant = wordEntry.byLang[langCode]
                    if (variant != null) {
                        val word = variant.word ?: variant.text ?: wordEntry.original
                        if (encounteredWordsForLang.contains(word)) {
                            encountered.add(wordEntry)
                        }
                    }
                }
            }
            encountered.toList()
        }
        
        val encounteredLanguages = remember(languageCodes) {
            languageCodes.filter { langCode ->
                getEncounteredWords(context, langCode).isNotEmpty()
            }
        }
        
        val threeWords = remember(encounteredWords) {
            if (encounteredWords.isNotEmpty()) {
                encounteredWords.shuffled().take(3)
            } else {
                corpus.shuffled().take(3) // Fallback if no encountered words
            }
        }

        if (showCompletionScreen) {
            // Define Titan One font for the button
            val fontFamily = remember<FontFamily> {
                val provider = GoogleFont.Provider(
                    providerAuthority = "com.google.android.gms.fonts",
                    providerPackage = "com.google.android.gms",
                    certificates = R.array.com_google_android_gms_fonts_certs
                )
                val googleFont = GoogleFont("Titan One")
                FontFamily(Font(googleFont = googleFont, fontProvider = provider))
            }
            
            // Show completion screen with congratulations and points
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🎉",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Congratulations!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                       	color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You completed the Practice section",
                        fontSize = 18.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "and made $correctExercises points",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { onExit() },
                        modifier = Modifier
                            .padding(horizontal = 32.dp)
                            .fillMaxWidth(0.8f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "AMAZING!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontFamily = fontFamily
                        )
                    }
                }
            }
        } else if (showCompletionAnimation) {
            ExerciseCompletionScreen(
                onContinue = {
                    showCompletionAnimation = false
                    if (currentExerciseIndex < practiceExercises.size - 1) {
                        currentExerciseIndex += 1 
                    } else {
                        // Practice session completed - no badge progress (badges only for Travel Mode)
                        showCompletionScreen = true
                    }
                }
            )
        } else {
            val currentExerciseType = practiceExercises[currentExerciseIndex]
            val exerciseTitle = when (currentExerciseType) {
                "audio_to_flag" -> "Match audio to flag"
                "pronunciation_to_flag" -> "Match pronunciation to flag"
                "audio_to_english" -> "Match audio to English"
                "pronunciation_audio_to_english" -> "Pronunciation + Audio to Translation"
                "pronunciation_to_english_multi" -> "Pronunciation to Translation (5 pairs)"
                "pronunciation_audio_to_type_english" -> "Type the Translation"
                else -> "Practice Exercise"
            }
            
            when (currentExerciseType) {
                "audio_to_flag" -> MatchingExercise(
                    title = exerciseTitle,
                    pairs = buildAudioToFlagPairs(
                        threeWords, 
                        languageCodes,
                        restrictToEncountered = true,
                        availableLanguages = encounteredLanguages
                    ),
                    useFlagAssets = true,
                    onDone = onExerciseDone
                )
                "pronunciation_to_flag" -> MatchingExercise(
                    title = exerciseTitle,
                    pairs = buildPronunciationToFlagPairs(
                        threeWords, 
                        languageCodes,
                        restrictToEncountered = true,
                        availableLanguages = encounteredLanguages
                    ),
                    useFlagAssets = true,
                    onDone = onExerciseDone
                )
                "audio_to_english" -> MatchingExercise(
                    title = exerciseTitle,
                    pairs = buildAudioToEnglishPairs(
                        threeWords, 
                        languageCodes,
                        restrictToEncountered = true,
                        availableLanguages = encounteredLanguages
                    ),
                    onDone = onExerciseDone
                )
                "pronunciation_audio_to_english" -> {
                    val pool = if (encounteredWords.isNotEmpty()) encounteredWords else corpus
                    val chosen = pool.random()
                    val code = languageCodes.firstOrNull {
                        val v = chosen.byLang[it]
                        v?.audio != null && (v.googlePronunciation != null || v.word != null)
                    } ?: languageCodes.first()
                    val variant = chosen.byLang[code]
                    val pronunciation = variant?.googlePronunciation ?: (variant?.word ?: "")
                    val correctAnswer = chosen.byLang["en"]?.text ?: chosen.byLang["en"]?.word ?: chosen.original
                    
                    val distractors = pool.filter { it !== chosen }
                        .mapNotNull { it.byLang["en"]?.text ?: it.byLang["en"]?.word ?: it.original }
                        .distinct()
                        .take(4)
                    val options = (distractors + correctAnswer).shuffled()
                    PronunciationAudioToEnglishExercise(
                        title = exerciseTitle,
                        pronunciation = pronunciation,
                        audioFile = variant?.audio,
                        options = options,
                        correctOption = correctAnswer,
                        onDone = { _ ->
                            onExerciseDone(true)
                        }
                    )
                }
                "pronunciation_to_english_multi" -> MatchingExercise(
                    title = exerciseTitle,
                    pairs = buildPronunciationToEnglishPairsMulti(
                        threeWords, 
                        languageCodes,
                        restrictToEncountered = true,
                        availableLanguages = encounteredLanguages
                    ),
                    onDone = onExerciseDone
                )
                "pronunciation_audio_to_type_english" -> {
                    val pool = if (encounteredWords.isNotEmpty()) encounteredWords else corpus
                    val chosen = pool.random()
                    val code = languageCodes.firstOrNull {
                        val v = chosen.byLang[it]
                        v?.audio != null && (v.googlePronunciation != null || v.word != null)
                    } ?: languageCodes.first()
                    val variant = chosen.byLang[code]
                    val pronunciation = variant?.googlePronunciation ?: (variant?.word ?: "")
                    val correctAnswer = chosen.byLang["en"]?.text ?: chosen.byLang["en"]?.word ?: chosen.original
                    PronunciationAudioToTypeEnglishExercise(
                        title = exerciseTitle,
                        pronunciation = pronunciation,
                        audioFile = variant?.audio,
                        correctAnswer = correctAnswer,
                        onDone = { isCorrect ->
                            onExerciseDone(isCorrect)
                        }
                    )
                }
                else -> MatchingExercise(
                    title = exerciseTitle,
                    pairs = buildAudioToEnglishPairs(
                        threeWords, 
                        languageCodes,
                        restrictToEncountered = true,
                        availableLanguages = encounteredLanguages
                    ),
                    onDone = onExerciseDone
                )
            }
        }
    }
}