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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.delay
import com.loulblemo.hellogoodbye.LanguageSelectionDialog
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.foundation.Image

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
                    var showAuthChoice by remember { mutableStateOf(false) }
                    var useAnonymousMode by remember { mutableStateOf(false) }
                    
                    LaunchedEffect(Unit) {
                        delay(2750) // Wait for animation to complete (~1.75s) + 1s pause on last frame
                        showSplash = false
                        if (BuildConfig.ENABLE_SIGN_IN) {
                            showAuthChoice = true
                        } else {
                            useAnonymousMode = true
                        }
                    }
                    
                    when {
                        showSplash -> SplashScreen()
                        showAuthChoice && BuildConfig.ENABLE_SIGN_IN -> AuthChoiceScreen(
                            onSignIn = { 
                                showAuthChoice = false
                                useAnonymousMode = false
                            },
                            onAnonymousStart = { 
                                showAuthChoice = false
                                useAnonymousMode = true
                            }
                        )
                        useAnonymousMode || !BuildConfig.ENABLE_SIGN_IN -> MainScreen()
                        else -> AuthGate(
                            onBackToChoice = { 
                                showAuthChoice = true
                                useAnonymousMode = false
                            }
                        ) { MainScreen() }
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
            .background(colorResource(id = R.color.background_light_purple)),
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
                color = colorResource(id = R.color.purple_black),
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
fun AuthChoiceScreen(
    onSignIn: () -> Unit,
    onAnonymousStart: () -> Unit
) {
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
            .background(colorResource(id = R.color.background_light_purple))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Icon
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "HelloGoodbye App Icon",
                modifier = Modifier.size(160.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome to",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.purple_black),
                    fontFamily = titanOneFont,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "HelloGoodbye",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.purple_black),
                    fontFamily = titanOneFont,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Sign In/Sign Up Button
            Button(
                onClick = onSignIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.medium_purple),
                    contentColor = colorResource(id = R.color.white)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LOG IN OR SIGN UP",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = titanOneFont
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sync across devices, access leaderboard and more",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Anonymous Start Button
            Button(
                onClick = onAnonymousStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.primary_purple),
                    contentColor = colorResource(id = R.color.white)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "START LEARNING",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = titanOneFont
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your progress will be saved on this device",
                        fontSize = 10.sp,
                        color = colorResource(id = R.color.white).copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun AuthGate(
    onBackToChoice: () -> Unit = {},
    content: @Composable () -> Unit
) {
    // If sign-in is disabled, skip authentication and go directly to content
    if (!BuildConfig.ENABLE_SIGN_IN) {
        content()
        return
    }
    
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    var currentUser by remember { mutableStateOf(auth.currentUser) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    if (currentUser == null) {
        SignInScreen(
            onSignedIn = {
                Toast.makeText(context, "Signed in", Toast.LENGTH_SHORT).show()
            },
            onBack = onBackToChoice
        )
    } else {
        content()
    }
}

@Composable
fun SignInScreen(
    onSignedIn: () -> Unit,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Email and password required"
            return
        }
        isLoading = true
        errorMessage = null
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    onSignedIn()
                } else {
                    errorMessage = task.exception?.localizedMessage ?: "Sign-in failed"
                }
            }
    }

    fun signUp(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Email and password required"
            return
        }
        isLoading = true
        errorMessage = null
        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    onSignedIn()
                } else {
                    errorMessage = task.exception?.localizedMessage ?: "Sign-up failed"
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // X button in top-right corner
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = colorResource(id = R.color.white),
                modifier = Modifier.size(24.dp)
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Sign in",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.purple_black)
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
            
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage!!, 
                    color = MaterialTheme.colorScheme.error,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Sign in button styled like practice button
            Button(
                onClick = { if (!isLoading) signIn(email, password) },
                enabled = !isLoading,
                modifier = Modifier.wrapContentWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = Color(0xFFE0E0E0),
                    disabledContentColor = Color(0xFF7A7A7A)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                val provider = GoogleFont.Provider(
                    providerAuthority = "com.google.android.gms.fonts",
                    providerPackage = "com.google.android.gms",
                    certificates = R.array.com_google_android_gms_fonts_certs
                )
                val titanOne = remember {
                    val googleFont = GoogleFont("Titan One")
                    FontFamily(Font(googleFont = googleFont, fontProvider = provider))
                }
                Text(
                    text = if (isLoading) "SIGNING IN..." else "SIGN IN",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isLoading) Color(0xFF7A7A7A) else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f),
                    fontFamily = titanOne
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = { if (!isLoading) signUp(email, password) },
                enabled = !isLoading,
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(
                    text = "Create account",
                    color = colorResource(id = R.color.primary_purple)
                )
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    var showSignInScreen by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        // Developer safety: ensure metadata and corpus stay aligned
        assertCorpusAndMetadataAligned(context)
        // Mark that the app has been launched
        markAppLaunched(context)
    }
    
    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
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
                onExit = { 
                    // Refresh currency from storage when returning to home
                    currency = loadCurrency(context)
                    currentScreen = "home" 
                },
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
                onExit = { 
                    // Refresh currency from storage when returning to home
                    currency = loadCurrency(context)
                    currentScreen = "home" 
                },
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
                },
                onSignIn = { showSignInScreen = true }
            )
        }
        }
    }
    
    // Show sign-in screen overlay when requested (only if sign-in is enabled)
    if (showSignInScreen && BuildConfig.ENABLE_SIGN_IN) {
        SignInScreen(
            onSignedIn = { 
                showSignInScreen = false
                Toast.makeText(context, "Signed in successfully!", Toast.LENGTH_SHORT).show()
            },
            onBack = { showSignInScreen = false }
        )
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
            .background(colorResource(id = R.color.dark_purple_background))
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
                color = colorResource(id = R.color.white),
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
                
                // Gradient fade at bottom to indicate more content
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    colorResource(id = R.color.dark_purple_background).copy(alpha = 0.8f),
                                    colorResource(id = R.color.dark_purple_background)
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Selection counter
            Text(
                text = "${selectedLanguages.size}/5 languages selected",
                fontSize = 18.sp,
                color = colorResource(id = R.color.white).copy(alpha = 0.8f),
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
                    containerColor = if (selectedLanguages.size == 5) GreenMain else colorResource(id = R.color.primary_container_purple),
                    contentColor = colorResource(id = R.color.white),
                    disabledContainerColor = colorResource(id = R.color.medium_purple).copy(alpha = 0.4f),
                    disabledContentColor = colorResource(id = R.color.white).copy(alpha = 0.4f)
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
                    containerColor = if (isSelected) GreenMain.copy(alpha = 0.2f) else colorResource(id = R.color.medium_purple)
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
                    color = if (isEnabled) colorResource(id = R.color.white) else colorResource(id = R.color.light_grey_purple),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}