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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.delay
import com.loulblemo.hellogoodbye.LanguageSelectionDialog

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
                text = "Where to today?",
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = FontFamily.Cursive,
                letterSpacing = 0.5.sp,
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
    var currency by remember { mutableStateOf(loadCurrency(context)) }
    var selectedCountries by remember { mutableStateOf(loadSelectedLanguages(context)) }
    var currentScreen by remember { mutableStateOf("home") }
    var travelStartLang by remember { mutableStateOf<String?>(null) }
    
    val availableCountries = remember(selectedCountries) {
        val allLanguages = listOf(
            Country("🇪🇸", "Spain", "Spanish"),
            Country("🇫🇷", "France", "French"),
            Country("🇩🇪", "Germany", "German"),
            Country("🇮🇹", "Italy", "Italian"),
            Country("🇯🇵", "Japan", "Japanese"),
            Country("🇰🇷", "South Korea", "Korean"),
            Country("🇨🇳", "China", "Chinese"),
            Country("🇧🇷", "Brazil", "Portuguese"),
            Country("🇷🇺", "Russia", "Russian"),
            Country("🇳🇱", "Netherlands", "Dutch"),
            Country("🇸🇪", "Sweden", "Swedish"),
            Country("🇵🇹", "Portugal", "Portuguese")
        )
        
        // Filter out already selected languages
        allLanguages.filter { candidate ->
            selectedCountries.none { selected -> 
                selected.language == candidate.language
            }
        }
    }
    
    when (currentScreen) {
        "home" -> {
            HomeScreen(
                currency = currency,
                selectedCountries = selectedCountries,
                availableCountries = availableCountries,
                onCurrencyChange = { newCurrency -> 
                    currency = newCurrency
                    saveCurrency(context, newCurrency)
                },
                onCountriesChange = { 
                    selectedCountries = it
                    saveSelectedLanguages(context, it)
                },
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
                onAwardCoin = { 
                    val newCurrency = currency + 1
                    currency = newCurrency
                    saveCurrency(context, newCurrency)
                }
            )
        }
        "travel" -> {
            val startLang = travelStartLang ?: "es"
            TravelScreen(
                startLanguageCode = startLang,
                onExit = { currentScreen = "home" },
                onAwardCoin = { 
                    val newCurrency = currency + 1
                    currency = newCurrency
                    saveCurrency(context, newCurrency)
                }
            )
        }
        "settings" -> {
            SettingsScreen(
                onBack = { currentScreen = "home" },
                onProgressCleared = {
                    // Refresh currency and selected languages after clearing progress
                    currency = loadCurrency(context)
                    selectedCountries = loadSelectedLanguages(context)
                }
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
    var showLanguageDialog by remember { mutableStateOf(false) }
    
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
                // Always open language selection; affordability handled in dialog UI
                showLanguageDialog = true
            },
            canAddMore = availableCountries.any { candidate ->
                selectedCountries.none { it.name == candidate.name && it.language == candidate.language }
            },
            canAffordAdd = true, // Always allow clicking to show cost info
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
    
    // Language selection dialog
    if (showLanguageDialog) {
        val selectedLanguageNames = selectedCountries.map { it.language }
        LanguageSelectionDialog(
            selectedLanguages = selectedLanguageNames,
            canAffordAdd = currency >= 50,
            onLanguageSelected = { languageName ->
                // Find the country from availableCountries that matches the selected language
                val selectedCountry = availableCountries.find { it.language == languageName }
                if (selectedCountry != null && currency >= 50) {
                    onCountriesChange(selectedCountries + selectedCountry)
                    onCurrencyChange(currency - 50)
                    Toast.makeText(context, "Added ${languageName} to your study list!", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
}