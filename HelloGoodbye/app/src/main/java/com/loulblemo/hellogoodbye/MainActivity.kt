package com.loulblemo.hellogoodbye

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HelloGoodbyeTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars)
                ) {
                    var showSplash by remember { mutableStateOf(true) }
                    
                    LaunchedEffect(Unit) {
                        delay(2750) // Wait for animation to complete (~1.75s) + 1s pause on last frame
                        showSplash = false
                    }
                    
                    if (showSplash) {
                        SplashScreen()
                    } else {
                        MainScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("Passport - Travel - Hospitality.json")
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = true,
        iterations = 1 // Play only once
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Lottie animation
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(300.dp)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Text underneath
            Text(
                text = "...where are you travelling today?",
                fontSize = 26.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 40.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        // Developer safety: ensure metadata and corpus stay aligned
        assertCorpusAndMetadataAligned(context)
    }
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
    var travelStartLang by remember { mutableStateOf<String?>(null) }
    
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
    
    when (currentScreen) {
        "home" -> {
            HomeScreen(
                currency = currency,
                selectedCountries = selectedCountries,
                availableCountries = availableCountries,
                onCurrencyChange = { currency = it },
                onCountriesChange = { selectedCountries = it },
                onNavigateToPractice = { currentScreen = "practice" },
                onNavigateToSettings = { currentScreen = "settings" },
                onFlagClick = { country ->
                    travelStartLang = languageNameToCode(country.language) ?: "es"
                    currentScreen = "travel"
                }
            )
        }
        "practice" -> {
            PracticeScreen(
                selectedCountries = selectedCountries,
                onExit = { currentScreen = "home" },
                onAwardCoin = { currency += 1 }
            )
        }
        "travel" -> {
            val startLang = travelStartLang ?: "es"
            TravelScreen(
                startLanguageCode = startLang,
                onExit = { currentScreen = "home" },
                onAwardCoin = { currency += 1 }
            )
        }
        "settings" -> {
            SettingsScreen(
                onBack = { currentScreen = "home" }
            )
        }
    }
}

@Composable
fun HomeScreen(
    currency: Int,
    selectedCountries: List<Country>,
    availableCountries: List<Country>,
    onCurrencyChange: (Int) -> Unit,
    onCountriesChange: (List<Country>) -> Unit,
    onNavigateToPractice: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onFlagClick: (Country) -> Unit
) {
    val context = LocalContext.current
    val practiceEnabled = canUnlockPractice(context)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top bar section (1 part)
        TopBarSection(
            currency = currency,
            onSettingsClick = onNavigateToSettings
        )

        // Central grid section (8 parts)
        CentralGridSection(
            selectedCountries = selectedCountries,
            onFlagClick = onFlagClick,
            onAddFlag = {
                Toast.makeText(context, "Buying a new language costs 50 coins", Toast.LENGTH_SHORT).show()
                val nextCountry = availableCountries.firstOrNull { candidate ->
                    selectedCountries.none { it.name == candidate.name && it.language == candidate.language }
                }
                if (nextCountry != null && currency >= 50) {
                    onCountriesChange(selectedCountries + nextCountry)
                    onCurrencyChange(currency - 50)
                }
            },
            canAddMore = availableCountries.any { candidate ->
                selectedCountries.none { it.name == candidate.name && it.language == candidate.language }
            },
            canAffordAdd = currency >= 50,
            price = 50,
            modifier = Modifier.weight(1f)
        )

        // Practice button section (full width, enabled only when two first quests are completed)
        PracticeAndTravelButtonsSection(
            onPracticeClick = onNavigateToPractice,
            onTravelClick = { /* removed */ },
            travelEnabled = practiceEnabled
        )
    }
}