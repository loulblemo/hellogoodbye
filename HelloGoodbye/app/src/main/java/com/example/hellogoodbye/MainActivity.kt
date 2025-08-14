package com.example.hellogoodbye

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

@Composable
fun MainScreen() {
    var currency by remember { mutableStateOf(10) }
    var selectedCountries by remember { mutableStateOf(listOf(
        Country("🇺🇸", "United States", "English"),
        Country("🇬🇧", "United Kingdom", "English"),
        Country("🇪🇸", "Spain", "Spanish"),
        Country("🇫🇷", "France", "French"),
        Country("🇩🇪", "Germany", "German")
    )) }
    
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
            Country("🇵🇹", "Portugal", "Portuguese"),
            Country("🇨🇦", "Canada", "English/French"),
            Country("🇦🇺", "Australia", "English"),
            Country("🇮🇳", "India", "Hindi/English")
        )
    }
    
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
                val nextCountry = availableCountries.getOrNull(selectedCountries.size - 5)
                if (nextCountry != null) {
                    selectedCountries = selectedCountries + nextCountry
                }
            },
            canAddMore = selectedCountries.size < availableCountries.size + 5,
            modifier = Modifier.weight(1f)
        )
        
        // Practice button section (2 parts)
        PracticeButtonSection()
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
                        .clickable { onAddFlag() },
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
                            contentDescription = "Add flag",
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
fun PracticeButtonSection() {
    Button(
        onClick = { /* TODO: Navigate to practice */ },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
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
fun HelloGoodbyeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(),
        content = content
    )
}
