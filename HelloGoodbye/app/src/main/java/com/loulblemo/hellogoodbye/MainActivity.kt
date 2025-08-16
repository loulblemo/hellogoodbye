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
                MainScreen()
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
                onNavigateToTravel = { currentScreen = "travel" }
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
            TravelScreen(
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
    onNavigateToTravel: () -> Unit
) {
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

        // Practice and Travel buttons section (2 parts)
        PracticeAndTravelButtonsSection(
            onPracticeClick = onNavigateToPractice,
            onTravelClick = onNavigateToTravel
        )
    }
}