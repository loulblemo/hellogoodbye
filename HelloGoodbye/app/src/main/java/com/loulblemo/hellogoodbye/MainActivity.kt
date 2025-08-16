package com.loulblemo.hellogoodbye

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HelloGoodbyeTheme {
                MainScreen()
            }
        }
    }
}

data class Country(
    val flag: String,
    val name: String,
    val language: String
)

data class TravelSection(
    val id: String,
    val flag: String,
    val name: String,
    val language: String,
    val isCompleted: Boolean = false,
    val isMixed: Boolean = false,
    val languages: List<String> = emptyList()
)

data class ExerciseType(
    val id: String,
    val title: String,
    val description: String
)

data class QuestProgress(
    val questId: String,
    val completedExercises: Set<String> = emptySet(),
    val isUnlocked: Boolean = false,
    val isCompleted: Boolean = false
)

data class TravelState(
    val questProgresses: Map<String, QuestProgress> = emptyMap(),
    val currentQuestId: String? = null,
    val currentExerciseIndex: Int = 0
)

@Composable
fun MainScreen() {
    var currency by remember { mutableStateOf(10) }
    var selectedCountries by remember {
        mutableStateOf(
            listOf(
                Country("🇪🇸", "Spain", "Spanish"),
                Country("🇫🇷", "France", "French"),
                Country("🇩🇪", "Germany", "German"),
                Country("🇮🇹", "Italy", "Italian"),
                Country("🇯🇵", "Japan", "Japanese")
            )
        )
    }
    var currentScreen by remember { mutableStateOf("home") }
    
    val availableCountries = remember {
        listOf(
            Country("🇯🇵", "Japan", "Japanese"),
            Country("🇰🇷", "South Korea", "Korean"),
            Country("🇨🇳", "China", "Chinese"),
            Country("🇮🇹", "Italy", "Italian"),
            Country("🇧🇷", "Brazil", "Portuguese"),
            Country("🇷🇺", "Russia", "Russian"),
            Country("🇳🇱", "Netherlands", "Dutch"),
            Country("🇸🇪", "Sweden", "Swedish"),
            Country("🇵🇹", "Portugal", "Portuguese")
        )
    }
    
    if (currentScreen == "home") {
        val context = LocalContext.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Top bar section (1 part)
            TopBarSection(
                currency = currency,
                onSettingsClick = { /* TODO: Navigate to settings */ }
            )

            // Central grid section (8 parts)
            CentralGridSection(
                selectedCountries = selectedCountries,
                onAddFlag = {
                    Toast.makeText(context, "Buying a new language costs 100 coins", Toast.LENGTH_SHORT).show()
                    val nextCountry = availableCountries.firstOrNull { candidate ->
                        selectedCountries.none { it.name == candidate.name && it.language == candidate.language }
                    }
                    if (nextCountry != null && currency >= 100) {
                        selectedCountries = selectedCountries + nextCountry
                        currency -= 100
                    }
                },
                canAddMore = availableCountries.any { candidate ->
                    selectedCountries.none { it.name == candidate.name && it.language == candidate.language }
                },
                canAffordAdd = currency >= 100,
                price = 100,
                modifier = Modifier.weight(1f)
            )

            // Practice and Travel buttons section (2 parts)
            PracticeAndTravelButtonsSection(
                onPracticeClick = { currentScreen = "practice" },
                onTravelClick = { currentScreen = "travel" }
            )
        }
    } else if (currentScreen == "practice") {
        PracticeScreen(
            selectedCountries = selectedCountries,
            onExit = { currentScreen = "home" },
            onAwardCoin = { currency += 1 }
        )
    } else if (currentScreen == "travel") {
        TravelScreen(
            onExit = { currentScreen = "home" },
            onAwardCoin = { currency += 1 }
        )
    }
}

@Composable
fun TopBarSection(currency: Int, onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Currency display
        Card(
            modifier = Modifier.size(60.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$currency",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        // Settings button
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun CentralGridSection(
    selectedCountries: List<Country>, 
    onAddFlag: () -> Unit,
    canAddMore: Boolean,
    canAffordAdd: Boolean = true,
    price: Int = 0,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(selectedCountries) { country ->
            // Flag item
            Card(
                modifier = Modifier.aspectRatio(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = country.flag,
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = country.language,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (canAddMore) {
            item {
                // Plus button
                Card(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable(enabled = canAffordAdd) { onAddFlag() },
                    colors = CardDefaults.cardColors(
                        containerColor = if (canAffordAdd) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add flag",
                                modifier = Modifier.size(32.dp),
                                tint = if (canAffordAdd) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (price > 0) "$price" else "",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PracticeButtonSection(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
            .padding(end = 8.dp)
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = "PRACTICE",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun PracticeAndTravelButtonsSection(onPracticeClick: () -> Unit, onTravelClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        PracticeButtonSection(
            onClick = onPracticeClick,
            modifier = Modifier.weight(1f)
        )
        Button(
            onClick = onTravelClick,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "TRAVEL",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondary
            )
        }
    }
}

@Composable
fun HelloGoodbyeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(),
        content = content
    )
}

// -------------------- Practice --------------------

data class WordVariant(
    val word: String?,
    val ipa: String?,
    val text: String?,
    val googlePronunciation: String?,
    val audio: String?
)

data class WordEntry(
    val original: String,
    val byLang: Map<String, WordVariant>
)

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

data class PairItem(
    val id: String,
    val label: String,
    val isAudio: Boolean,
    val audioFile: String? = null,
    val matchKey: String
)

data class MatchingPair(
    val left: PairItem,
    val right: PairItem
)

@Composable
fun MatchingExercise(
    title: String,
    pairs: List<MatchingPair>,
    onDone: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var mistakes by remember(pairs) { mutableStateOf(0) }
    var remaining by remember(pairs) { mutableStateOf(pairs.size) }
    var leftSelection by remember(pairs) { mutableStateOf<String?>(null) }
    var rightSelection by remember(pairs) { mutableStateOf<String?>(null) }
    var completed by remember(pairs) { mutableStateOf(false) }

    // Build lookup maps and shuffled lists
    val leftItems = remember(pairs) { pairs.map { it.left }.shuffled() }
    val rightItems = remember(pairs) { pairs.map { it.right }.shuffled() }
    val leftById = remember(pairs) { pairs.associate { it.left.id to it.left } }
    val rightById = remember(pairs) { pairs.associate { it.right.id to it.right } }
    val solvedLeftIds = remember(pairs) { mutableStateListOf<String>() }
    val solvedRightIds = remember(pairs) { mutableStateListOf<String>() }

    fun tryMatch() {
        val l = leftSelection
        val r = rightSelection
        if (l != null && r != null) {
            val leftItem = leftById[l]
            val rightItem = rightById[r]
            if (leftItem != null && rightItem != null && leftItem.matchKey == rightItem.matchKey) {
                solvedLeftIds.add(l)
                solvedRightIds.add(r)
                remaining -= 1
                leftSelection = null
                rightSelection = null
                if (remaining == 0 && !completed) {
                    completed = true
                    onDone(mistakes == 0)
                }
            } else {
                mistakes += 1
                leftSelection = null
                rightSelection = null
            }
        }
    }

    // Handle edge-case: zero pairs — complete immediately
    LaunchedEffect(pairs) {
        if (pairs.isEmpty() && !completed) {
            completed = true
            onDone(true)
        }
    }
    
    // Debug effect to track completion
    LaunchedEffect(remaining, completed) {
        println("MatchingExercise: remaining=$remaining, completed=$completed, pairs.size=${pairs.size}")
        if (remaining == 0 && !completed && pairs.isNotEmpty()) {
            println("Auto-completing exercise due to remaining=0")
            completed = true
            onDone(mistakes == 0)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                leftItems.forEach { item ->
                    val solved = solvedLeftIds.contains(item.id)
                    PracticeBubble(
                        label = item.label,
                        selected = leftSelection == item.id,
                        solved = solved,
                        onClick = {
                            if (item.isAudio && item.audioFile != null) {
                                playAssetAudio(context, item.audioFile)
                            }
                            if (!solved) {
                                leftSelection = if (leftSelection == item.id) null else item.id
                                tryMatch()
                            }
                        }
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rightItems.forEach { item ->
                    val solved = solvedRightIds.contains(item.id)
                    PracticeBubble(
                        label = item.label,
                        selected = rightSelection == item.id,
                        solved = solved,
                        onClick = {
                            if (item.isAudio && item.audioFile != null) {
                                playAssetAudio(context, item.audioFile)
                            }
                            if (!solved) {
                                rightSelection = if (rightSelection == item.id) null else item.id
                                tryMatch()
                            }
                        }
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Remaining: $remaining")
            Text("Mistakes: $mistakes")
        }
    }
}

@Composable
fun PracticeBubble(
    label: String,
    selected: Boolean,
    solved: Boolean,
    onClick: () -> Unit
) {
    val bg = when {
        solved -> MaterialTheme.colorScheme.secondaryContainer
        selected -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = bg),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(label)
        }
    }
}

fun playAssetAudio(context: Context, fileName: String) {
    runCatching {
        val afd = context.assets.openFd("audio_files/$fileName")
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        afd.close()
        mediaPlayer.setOnPreparedListener { it.start() }
        mediaPlayer.setOnCompletionListener { it.release() }
        mediaPlayer.prepareAsync()
    }
}

fun loadCorpusFromAssets(context: Context): List<WordEntry> {
    return runCatching {
        val jsonString = context.assets.open("corpus.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(jsonString)
        val list = mutableListOf<WordEntry>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val original = obj.optString("original")
            val map = mutableMapOf<String, WordVariant>()
            obj.keys().forEachRemaining { key ->
                if (key == "original") return@forEachRemaining
                val langObj = obj.optJSONObject(key) ?: return@forEachRemaining
                val variant = WordVariant(
                    word = langObj.optString("word", null),
                    ipa = langObj.optString("IPA", null),
                    text = langObj.optString("text", null),
                    googlePronunciation = langObj.optString("google_pronunciation", null),
                    audio = langObj.optString("audio", null)
                )
                map[key] = variant
            }
            list.add(WordEntry(original = original, byLang = map))
        }
        list
    }.getOrElse { emptyList() }
}

fun languageNameToCode(name: String): String? {
    return when (name.lowercase()) {
        "english" -> "en"
        "spanish" -> "es"
        "french" -> "fr"
        "german" -> "de"
        "italian" -> "it"
        "portuguese" -> "pt"
        "russian" -> "ru"
        "japanese" -> "ja"
        "korean" -> "ko"
        "chinese", "chinese (simplified)", "simplified chinese" -> "zh-cn"
        "dutch" -> "nl"
        "swedish" -> "sv"
        else -> null
    }
}

private fun pickFivePairs(
    threeWords: List<WordEntry>,
    languageCodes: List<String>,
    buildLeft: (WordEntry, String, WordVariant) -> PairItem?,
    buildRight: (WordEntry, String, WordVariant) -> PairItem?
): List<MatchingPair> {
    val combos = mutableListOf<MatchingPair>()
    val shuffledLangs = languageCodes.shuffled()
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
        val allLangs = languageCodes.ifEmpty { listOf("es", "fr", "de", "it", "pt", "ru", "ja", "ko", "zh-cn") }
        val pool = mutableListOf<MatchingPair>()
        for (lang in allLangs) {
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

fun buildAudioToFlagPairs(threeWords: List<WordEntry>, languageCodes: List<String>): List<MatchingPair> {
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
        }
    )
}

fun buildPronunciationToFlagPairs(threeWords: List<WordEntry>, languageCodes: List<String>): List<MatchingPair> {
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
        }
    )
}

fun buildAudioToEnglishPairs(threeWords: List<WordEntry>, languageCodes: List<String>): List<MatchingPair> {
    return pickFivePairs(
        threeWords,
        languageCodes,
        buildLeft = { word, lang, variant ->
            val audio = variant.audio ?: return@pickFivePairs null
            val en = word.byLang["en"]?.text ?: word.byLang["en"]?.word ?: word.original
            val key = (en ?: word.original).lowercase()
            PairItem(id = "L_${lang}_${word.original}", label = "▶︎", isAudio = true, audioFile = audio, matchKey = key)
        },
        buildRight = { word, lang, _ ->
            val en = word.byLang["en"]?.text ?: word.byLang["en"]?.word ?: word.original
            val label = en ?: word.original
            val key = label.lowercase()
            PairItem(id = "R_${lang}_${word.original}", label = label, isAudio = false, matchKey = key)
        }
    )
}

fun buildPronunciationToEnglishPairs(threeWords: List<WordEntry>, languageCodes: List<String>): List<MatchingPair> {
    return pickFivePairs(
        threeWords,
        languageCodes,
        buildLeft = { word, lang, variant ->
            val text = variant.googlePronunciation ?: variant.word ?: variant.text ?: return@pickFivePairs null
            val en = word.byLang["en"]?.text ?: word.byLang["en"]?.word ?: word.original
            val key = (en ?: word.original).lowercase()
            PairItem(id = "L_${lang}_${word.original}_$text", label = text, isAudio = false, matchKey = key)
        },
        buildRight = { word, lang, _ ->
            val en = word.byLang["en"]?.text ?: word.byLang["en"]?.word ?: word.original
            val label = en ?: word.original
            val key = label.lowercase()
            PairItem(id = "R_${lang}_${word.original}", label = label, isAudio = false, matchKey = key)
        }
    )
}

fun languageCodeToFlag(code: String): String? {
    return when (code) {
        "en" -> "🇺🇸"
        "es" -> "🇪🇸"
        "fr" -> "🇫🇷"
        "de" -> "🇩🇪"
        "it" -> "🇮🇹"
        "pt" -> "🇵🇹"
        "ru" -> "🇷🇺"
        "ja" -> "🇯🇵"
        "ko" -> "🇰🇷"
        "zh-cn" -> "🇨🇳"
        "nl" -> "🇳🇱"
        "sv" -> "🇸🇪"
        else -> null
    }
}

fun languageCodeToName(code: String): String? {
    return when (code) {
        "en" -> "English"
        "es" -> "Spanish"
        "fr" -> "French"
        "de" -> "German"
        "it" -> "Italian"
        "pt" -> "Portuguese"
        "ru" -> "Russian"
        "ja" -> "Japanese"
        "ko" -> "Korean"
        "zh-cn" -> "Chinese"
        "nl" -> "Dutch"
        "sv" -> "Swedish"
        else -> null
    }
}

private fun generateTravelSequence(allLangCodes: List<String>): List<TravelSection> {
    val sequence = mutableListOf<TravelSection>()
    // Start with Italy, France, then Mixed(it, fr)
    sequence.add(TravelSection(id = "italy", flag = "🇮🇹", name = "Italy", language = "Italian"))
    sequence.add(TravelSection(id = "france", flag = "🇫🇷", name = "France", language = "French"))
    sequence.add(
        TravelSection(
            id = "mixed_it_fr",
            flag = "🌍",
            name = "Mixed: 🇮🇹 + 🇫🇷",
            language = "Mixed",
            isMixed = true,
            languages = listOf("it", "fr")
        )
    )

    val remaining = allLangCodes.filter { it != "it" && it != "fr" }.shuffled()
    var idx = 0
    while (idx < remaining.size) {
        val a = remaining[idx]
        val aName = languageCodeToName(a)
        val aFlag = languageCodeToFlag(a)
        if (aName != null && aFlag != null) {
            sequence.add(
                TravelSection(
                    id = "lang_${a}",
                    flag = aFlag,
                    name = aName,
                    language = aName
                )
            )
        }

        val b = if (idx + 1 < remaining.size) remaining[idx + 1] else null
        if (b != null) {
            val bName = languageCodeToName(b)
            val bFlag = languageCodeToFlag(b)
            if (bName != null && bFlag != null) {
                sequence.add(
                    TravelSection(
                        id = "lang_${b}",
                        flag = bFlag,
                        name = bName,
                        language = bName
                    )
                )
                sequence.add(
                    TravelSection(
                        id = "mixed_${a}_${b}",
                        flag = "🌍",
                        name = "Mixed: ${aFlag} + ${bFlag}",
                        language = "Mixed",
                        isMixed = true,
                        languages = listOf(a, b)
                    )
                )
            }
            idx += 2
        } else {
            // Single leftover language, no mixed
            idx += 1
        }
    }

    return sequence
}

private fun getExerciseTypes(): List<ExerciseType> {
    return listOf(
        ExerciseType("audio_to_flag", "Match Audio to Flag", "Listen and match the audio to the correct flag"),
        ExerciseType("pronunciation_to_flag", "Match Pronunciation to Flag", "Match written pronunciation to the correct flag"),
        ExerciseType("audio_to_english", "Match Audio to English", "Listen and match the audio to the English translation")
    )
}

private fun initializeTravelState(travelSections: List<TravelSection>): TravelState {
    val questProgresses = travelSections.mapIndexed { index, section ->
        section.id to QuestProgress(
            questId = section.id,
            isUnlocked = index == 0 // Only first quest is unlocked initially
        )
    }.toMap()
    
    return TravelState(
        questProgresses = questProgresses,
        currentQuestId = null
    )
}

private fun updateQuestProgress(
    travelState: TravelState,
    questId: String,
    completedExerciseId: String,
    travelSections: List<TravelSection>
): TravelState {
    val exerciseTypes = getExerciseTypes()
    val currentProgress = travelState.questProgresses[questId] ?: return travelState
    
    val newCompletedExercises = currentProgress.completedExercises + completedExerciseId
    val isQuestCompleted = newCompletedExercises.size >= exerciseTypes.size
    
    val updatedProgress = currentProgress.copy(
        completedExercises = newCompletedExercises,
        isCompleted = isQuestCompleted
    )
    
    // Unlock next quest if current one is completed
    val updatedProgresses = travelState.questProgresses.toMutableMap()
    updatedProgresses[questId] = updatedProgress
    
    if (isQuestCompleted) {
        val currentIndex = travelSections.indexOfFirst { it.id == questId }
        if (currentIndex >= 0 && currentIndex + 1 < travelSections.size) {
            val nextQuestId = travelSections[currentIndex + 1].id
            val nextProgress = updatedProgresses[nextQuestId]
            if (nextProgress != null) {
                updatedProgresses[nextQuestId] = nextProgress.copy(isUnlocked = true)
            }
        }
    }
    
    return travelState.copy(questProgresses = updatedProgresses)
}

// -------------------- Travel Mode --------------------

@Composable
fun TravelScreen(
    onExit: () -> Unit,
    onAwardCoin: () -> Unit
) {
    val context = LocalContext.current
    val corpus by remember { mutableStateOf(loadCorpusFromAssets(context)) }
    val supportedLangCodes = remember(corpus) {
        corpus.flatMap { it.byLang.keys }.distinct()
    }
    val travelSections = remember(supportedLangCodes) { generateTravelSequence(supportedLangCodes) }
    var travelState by remember { mutableStateOf(initializeTravelState(travelSections)) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Travel Mode",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onExit) { Text("Close") }
        }
        
        if (travelState.currentQuestId == null) {
            // Travel quest list view
            TravelQuestListScreen(
                travelSections = travelSections,
                travelState = travelState,
                onQuestClick = { questId ->
                    travelState = travelState.copy(
                        currentQuestId = questId,
                        currentExerciseIndex = 0
                    )
                }
            )
        } else {
            // Quest practice view
            val currentSection = travelSections.find { it.id == travelState.currentQuestId }
            if (currentSection != null) {
                QuestPracticeScreen(
                    section = currentSection,
                    travelState = travelState,
                    onExerciseComplete = { exerciseId ->
                        travelState = updateQuestProgress(
                            travelState,
                            currentSection.id,
                            exerciseId,
                            travelSections
                        )
                        onAwardCoin()
                        
                        val currentProgress = travelState.questProgresses[currentSection.id]
                        if (currentProgress?.isCompleted == true) {
                            // Quest completed, return to list
                            travelState = travelState.copy(currentQuestId = null)
                        } else {
                            // Move to next exercise
                            val nextIndex = travelState.currentExerciseIndex + 1
                            val exerciseTypes = getExerciseTypes()
                            if (nextIndex < exerciseTypes.size) {
                                travelState = travelState.copy(currentExerciseIndex = nextIndex)
                            }
                        }
                    },
                    onBack = { 
                        travelState = travelState.copy(currentQuestId = null)
                    }
                )
            }
        }
    }
}

@Composable
fun TravelQuestListScreen(
    travelSections: List<TravelSection>,
    travelState: TravelState,
    onQuestClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        val unlockedSections = travelSections.filter { section ->
            travelState.questProgresses[section.id]?.isUnlocked == true
        }
        
        itemsIndexed(unlockedSections) { index, section ->
            Column {
                TravelQuestCard(
                    section = section,
                    questProgress = travelState.questProgresses[section.id],
                    onClick = { onQuestClick(section.id) }
                )
                
                // Add connecting line if not the last item and current quest is completed
                val progress = travelState.questProgresses[section.id]
                if (index < unlockedSections.size - 1 && progress?.isCompleted == true) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        Canvas(
                            modifier = Modifier
                                .width(4.dp)
                                .height(40.dp)
                                .offset(x = 48.dp)
                        ) {
                            drawLine(
                                color = Color(0xFF4CAF50),
                                start = androidx.compose.ui.geometry.Offset(size.width / 2, 0f),
                                end = androidx.compose.ui.geometry.Offset(size.width / 2, size.height),
                                strokeWidth = 4.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        }
                    }
                }
            }
        }
        
        // Add plus button if all sections are completed
        if (travelSections.all { section ->
            travelState.questProgresses[section.id]?.isCompleted == true
        }) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .size(100.dp)
                        .clickable { /* TODO: Add new language selection */ },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add new language",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TravelQuestCard(
    section: TravelSection,
    questProgress: QuestProgress?,
    onClick: () -> Unit
) {
    val exerciseTypes = getExerciseTypes()
    val completedCount = questProgress?.completedExercises?.size ?: 0
    val isCompleted = questProgress?.isCompleted == true
    val isUnlocked = questProgress?.isUnlocked == true
    
    val bgColor = when {
        isCompleted -> MaterialTheme.colorScheme.secondaryContainer
        isUnlocked -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val borderColor = if (isCompleted) Color(0xFF4CAF50) else Color.Transparent
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(enabled = isUnlocked) { onClick() },
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp),
        border = if (isCompleted) BorderStroke(3.dp, borderColor) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quest icon/flag
            Box(
                modifier = Modifier.size(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = section.flag,
                    fontSize = 32.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Quest info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = section.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (section.isMixed) {
                    Text(
                        text = "Mixed Languages",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = section.language,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Progress indicator
                if (isUnlocked && !isCompleted) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Progress: $completedCount/${exerciseTypes.size} exercises",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Completion status
            if (isCompleted) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else if (isUnlocked) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▶",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun QuestPracticeScreen(
    section: TravelSection,
    travelState: TravelState,
    onExerciseComplete: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val corpus by remember { mutableStateOf(loadCorpusFromAssets(context)) }
    val exerciseTypes = getExerciseTypes()
    val currentProgress = travelState.questProgresses[section.id]
    val currentExerciseIndex = travelState.currentExerciseIndex
    
    val languages = if (section.isMixed) {
        section.languages
    } else {
        listOf(languageNameToCode(section.language) ?: "en")
    }
    val languageCodes = languages.filterNotNull()
    
    val currentExercise = exerciseTypes.getOrNull(currentExerciseIndex)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = section.flag,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = section.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (currentExercise != null) {
                    Text(
                        text = "Exercise ${currentExerciseIndex + 1}/${exerciseTypes.size}: ${currentExercise.title}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            TextButton(onClick = onBack) { Text("Back") }
        }
        
        // Progress indicator
        LinearProgressIndicator(
            progress = (currentExerciseIndex + 1).toFloat() / exerciseTypes.size,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (corpus.isEmpty() || languageCodes.isEmpty() || currentExercise == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No content available for this exercise")
            }
            return
        }
        
        val threeWords = remember(corpus) {
            corpus.shuffled().take(3)
        }
        
        // Check if current exercise is already completed
        val isExerciseCompleted = currentProgress?.completedExercises?.contains(currentExercise.id) == true
        
        if (isExerciseCompleted) {
            // Show completion message and continue button
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Exercise Completed!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val nextIndex = currentExerciseIndex + 1
                            if (nextIndex < exerciseTypes.size) {
                                // Continue to next exercise
                                onExerciseComplete(currentExercise.id)
                            } else {
                                // Quest completed
                                onExerciseComplete(currentExercise.id)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text(
                            if (currentExerciseIndex + 1 < exerciseTypes.size) "Continue" else "Complete Quest",
                            color = Color.White
                        )
                    }
                }
            }
        } else {
            // Show the actual exercise
            when (currentExercise.id) {
                "audio_to_flag" -> {
                    val pairs = buildAudioToFlagPairs(threeWords, languageCodes)
                    MatchingExercise(
                        title = currentExercise.title,
                        pairs = pairs,
                        onDone = { perfect ->
                            onExerciseComplete(currentExercise.id)
                        }
                    )
                }
                "pronunciation_to_flag" -> {
                    val pairs = buildPronunciationToFlagPairs(threeWords, languageCodes)
                    MatchingExercise(
                        title = currentExercise.title,
                        pairs = pairs,
                        onDone = { perfect ->
                            onExerciseComplete(currentExercise.id)
                        }
                    )
                }
                "audio_to_english" -> {
                    val pairs = buildAudioToEnglishPairs(threeWords, languageCodes)
                    MatchingExercise(
                        title = currentExercise.title,
                        pairs = pairs,
                        onDone = { perfect ->
                            onExerciseComplete(currentExercise.id)
                        }
                    )
                }
            }
        }
    }
}
