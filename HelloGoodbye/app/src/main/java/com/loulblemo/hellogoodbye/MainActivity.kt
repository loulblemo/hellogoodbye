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
                    MainScreen()
                }
            }
        }
    }
}

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
                onFlagClick = { country ->
                    travelStartLang = languageNameToCode(country.language) ?: "en"
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
            val startLang = travelStartLang ?: "en"
            TravelScreen(
                startLanguageCode = startLang,
                onExit = { currentScreen = "home" },
                onAwardCoin = { currency += 1 }
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
    onFlagClick: (Country) -> Unit
) {
    val context = LocalContext.current
    val practiceEnabled = countFirstQuestCompletedLanguages(context) >= 2
    
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
            onFlagClick = onFlagClick,
            onAddFlag = {
                Toast.makeText(context, "Buying a new language costs 100 coins", Toast.LENGTH_SHORT).show()
                val nextCountry = availableCountries.firstOrNull { candidate ->
                    selectedCountries.none { it.name == candidate.name && it.language == candidate.language }
                }
                if (nextCountry != null && currency >= 100) {
                    onCountriesChange(selectedCountries + nextCountry)
                    onCurrencyChange(currency - 100)
                }
            },
            canAddMore = availableCountries.any { candidate ->
                selectedCountries.none { it.name == candidate.name && it.language == candidate.language }
            },
            canAffordAdd = currency >= 100,
            price = 100,
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