package com.loulblemo.hellogoodbye

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
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
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = run {
                    val provider = GoogleFont.Provider(
                        providerAuthority = "com.google.android.gms.fonts",
                        providerPackage = "com.google.android.gms",
                        certificates = R.array.com_google_android_gms_fonts_certs
                    )
                    val googleFont = GoogleFont("Titan One")
                    FontFamily(Font(googleFont = googleFont, fontProvider = provider))
                },
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
        // Mark that the app has been launched
        markAppLaunched(context)
    }
    var currency by remember { mutableStateOf(loadCurrency(context)) }
    var selectedCountries by remember { mutableStateOf(loadSelectedLanguages(context)) }
    var currentScreen by remember { mutableStateOf("home") }
    var travelStartLang by remember { mutableStateOf<String?>(null) }
    var showWelcome by remember { mutableStateOf(shouldShowWelcomeScreen(context)) }
    
    // Debug logging for welcome screen state
    LaunchedEffect(showWelcome) {
        if (loadDebugMode(context)) {
            println("DEBUG: showWelcome state changed to: $showWelcome")
            debugWelcomeScreenState(context)
        }
    }
    
    
    val availableCountries = remember(selectedCountries) {
        val supportedLanguageCodes = getSupportedLanguageCodesFromMetadata(context)
        val allLanguages = supportedLanguageCodes
            .filter { it != "en" } // Exclude English as it's the base language
            .mapNotNull { code ->
                val name = getLanguageNameFromMetadata(context, code)
                val flag = getLanguageFlagFromMetadata(context, code)
                if (name != null && flag != null) {
                    Country(flag = flag, name = name, language = name)
                } else null
            }
        
        // Filter out already selected languages
        allLanguages.filter { candidate ->
            selectedCountries.none { selected -> 
                selected.language == candidate.language
            }
        }
    }
    
    if (showWelcome) {
        WelcomeScreen(
            onComplete = { selectedLanguages ->
                if (loadDebugMode(context)) {
                    println("DEBUG: Welcome screen completed with ${selectedLanguages.size} languages")
                }
                selectedCountries = selectedLanguages
                saveSelectedLanguages(context, selectedLanguages)
                markWelcomeScreenCompleted(context)
                showWelcome = false
                if (loadDebugMode(context)) {
                    println("DEBUG: showWelcome set to false, shouldShowWelcomeScreen = ${shouldShowWelcomeScreen(context)}")
                }
            }
        )
    } else {
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
                onBack = { 
                    // Refresh currency and selected languages when returning from settings
                    // This ensures developer mode changes are reflected in the UI
                    currency = loadCurrency(context)
                    selectedCountries = loadSelectedLanguages(context)
                    currentScreen = "home" 
                },
                onProgressCleared = {
                    // Refresh currency and selected languages after clearing progress
                    currency = loadCurrency(context)
                    selectedCountries = loadSelectedLanguages(context)
                    // Reset welcome screen to show again after clearing progress
                    val shouldShow = shouldShowWelcomeScreen(context)
                    if (loadDebugMode(context)) {
                        println("DEBUG: Progress cleared, shouldShowWelcomeScreen = $shouldShow")
                    }
                    showWelcome = shouldShow
                }
            )
        }
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

@Composable
fun WelcomeScreen(onComplete: (List<Country>) -> Unit) {
    val context = LocalContext.current
    var selectedLanguages by remember { mutableStateOf(emptyList<Country>()) }
    
    val allLanguages = remember {
        val supportedLanguageCodes = getSupportedLanguageCodesFromMetadata(context)
        supportedLanguageCodes
            .filter { it != "en" } // Exclude English as it's the base language
            .mapNotNull { code ->
                val name = getLanguageNameFromMetadata(context, code)
                val flag = getLanguageFlagFromMetadata(context, code)
                if (name != null && flag != null) {
                    Country(flag = flag, name = name, language = name)
                } else null
            }
    }
    
    val titanOneFont = remember {
        val provider = GoogleFont.Provider(
            providerAuthority = "com.google.android.gms.fonts",
            providerPackage = "com.google.android.gms",
            certificates = R.array.com_google_android_gms_fonts_certs
        )
        val googleFont = GoogleFont("Titan One")
        FontFamily(Font(googleFont = googleFont, fontProvider = provider))
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2D1B69)) // Dark purple background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            // Title
            Text(
                text = "Select your first 5 languages",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = titanOneFont,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Language grid
            Box(
                modifier = Modifier.weight(1f)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(allLanguages) { country ->
                        WelcomeLanguageCard(
                            country = country,
                            isSelected = selectedLanguages.contains(country),
                            onClick = {
                                if (selectedLanguages.contains(country)) {
                                    selectedLanguages = selectedLanguages - country
                                } else if (selectedLanguages.size < 5) {
                                    selectedLanguages = selectedLanguages + country
                                }
                            },
                            isEnabled = selectedLanguages.contains(country) || selectedLanguages.size < 5
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Selection counter
            Text(
                text = "${selectedLanguages.size}/5 languages selected",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.8f),
                fontFamily = titanOneFont
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Continue button
            Button(
                onClick = { onComplete(selectedLanguages) },
                enabled = selectedLanguages.size == 5,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedLanguages.size == 5) GreenMain else Color.Gray,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Continue",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = titanOneFont
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun WelcomeLanguageCard(
    country: Country,
    isSelected: Boolean,
    onClick: () -> Unit,
    isEnabled: Boolean
) {
    val context = LocalContext.current
    val flagAsset = getLanguageFlagAssetFromMetadata(context, languageNameToCode(country.language) ?: "")
    
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(enabled = isEnabled) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) GreenMain.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = 3.dp,
                            color = GreenMain,
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Flag - use BottomFlagBadge for smudged effect
                val languageCode = languageNameToCode(country.language)
                if (languageCode != null) {
                    BottomFlagBadge(
                        languageCode = languageCode,
                        modifier = Modifier.size(48.dp, 36.dp)
                    )
                } else {
                    Text(
                        text = country.flag,
                        fontSize = 32.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Language name
                Text(
                    text = country.language,
                    fontSize = 12.sp,
                    color = if (isEnabled) Color.White else Color.White.copy(alpha = 0.5f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}