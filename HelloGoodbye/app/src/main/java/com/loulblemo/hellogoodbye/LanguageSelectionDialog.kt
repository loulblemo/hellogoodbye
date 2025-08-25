package com.loulblemo.hellogoodbye

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.decode.SvgDecoder
import coil.ImageLoader


@Composable
fun LanguageSelectionDialog(
    selectedLanguages: List<String>,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val allLanguageCodes = remember { getSupportedLanguageCodesFromMetadata(context) }
    val remainingLanguages = remember(selectedLanguages) {
        allLanguageCodes.filter { code ->
            val languageName = getLanguageMetadata(context, code)?.optString("name")
            languageName != null && !selectedLanguages.contains(languageName)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = "Add New Language",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Select a language to add to your study list",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Language grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(remainingLanguages) { languageCode ->
                        LanguageFlagItem(
                            languageCode = languageCode,
                            onClick = {
                                val languageName = getLanguageMetadata(context, languageCode)?.optString("name")
                                if (languageName != null) {
                                    onLanguageSelected(languageName)
                                }
                                onDismiss()
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Cancel button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun LanguageFlagItem(
    languageCode: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val languageMetadata = remember(languageCode) { getLanguageMetadata(context, languageCode) }
    val languageName = remember(languageMetadata) { languageMetadata?.optString("name") }
    val flagAsset = remember(languageMetadata) { languageMetadata?.optString("flagAsset") }
    
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Flag
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(8.dp))
                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (flagAsset != null) {
                    val imageLoader = remember(context) {
                        ImageLoader.Builder(context)
                            .components { add(SvgDecoder.Factory()) }
                            .build()
                    }
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data("file:///android_asset/${flagAsset}")
                            .build(),
                        contentDescription = "${languageName} flag",
                        imageLoader = imageLoader,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Fallback to emoji flag
                    val emojiFlag = when (languageCode) {
                        "en" -> "🇺🇸"
                        "es" -> "🇪🇸"
                        "fr" -> "🇫🇷"
                        "de" -> "🇩🇪"
                        "it" -> "🇮🇹"
                        "pt" -> "🇧🇷"
                        "ru" -> "🇷🇺"
                        "ja" -> "🇯🇵"
                        "ko" -> "🇰🇷"
                        "zh-cn" -> "🇨🇳"
                        else -> "🏳️"
                    }
                    Text(
                        text = emojiFlag,
                        fontSize = 32.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Language name
            Text(
                text = languageName ?: languageCode,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}
