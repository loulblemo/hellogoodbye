package com.loulblemo.hellogoodbye

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    var step by remember { mutableStateOf(0) } // 0..3
    var perfectRunAwarded by remember { mutableStateOf(false) }

    val onExerciseDone: (Boolean) -> Unit = { perfect ->
        if (perfect) {
            onAwardCoin()
        }
        if (step < 3) step += 1 else perfectRunAwarded = true
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
            TextButton(onClick = onExit) { Text("Close") }
        }

        if (corpus.isEmpty() || languageCodes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Add languages first")
            }
            return
        }

        val threeWords = remember(corpus) {
            corpus.shuffled().take(3)
        }

        when (step) {
            0 -> MatchingExercise(
                title = "Match audio to flag",
                pairs = buildAudioToFlagPairs(threeWords, languageCodes),
                onDone = onExerciseDone
            )
            1 -> MatchingExercise(
                title = "Match pronunciation to flag",
                pairs = buildPronunciationToFlagPairs(threeWords, languageCodes),
                onDone = onExerciseDone
            )
            2 -> MatchingExercise(
                title = "Match audio to English",
                pairs = buildAudioToEnglishPairs(threeWords, languageCodes),
                onDone = onExerciseDone
            )
            else -> MatchingExercise(
                title = "Match pronunciation to English",
                pairs = buildPronunciationToEnglishPairs(threeWords, languageCodes),
                onDone = onExerciseDone
            )
        }

        if (perfectRunAwarded) {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                Button(onClick = { onExit() }) { Text("Back to Home") }
            }
        }
    }
}