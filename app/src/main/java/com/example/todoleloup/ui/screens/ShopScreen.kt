package com.example.todoleloup.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todoleloup.data.BackgroundTheme
import com.example.todoleloup.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun ShopScreen(
    points: Int,
    activeBackground: BackgroundTheme,
    unlockedBackgrounds: Set<BackgroundTheme>,
    onBuyOrActivate: (BackgroundTheme) -> Boolean
) {
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf<BackgroundTheme?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Boutique",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = irishGroverFont
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = DarkSurface
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "🐾", fontSize = 16.sp)
                        Text(
                            text = "$points pts",
                            color = CyanPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = irishGroverFont
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Débloque des arrière-plans avec tes points",
                color = TextSecondary,
                fontSize = 13.sp,
                fontFamily = irishGroverFont
            )

            feedbackMessage?.let { msg ->
                LaunchedEffect(msg) {
                    delay(2000)
                    feedbackMessage = null
                }
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CyanPrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = msg,
                        color = CyanPrimary,
                        fontSize = 13.sp,
                        fontFamily = irishGroverFont,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(BackgroundTheme.entries) { theme ->
                    val isUnlocked = theme in unlockedBackgrounds
                    val isActive = theme == activeBackground
                    val canAfford = points >= theme.cost

                    BackgroundCard(
                        theme = theme,
                        isUnlocked = isUnlocked,
                        isActive = isActive,
                        canAfford = canAfford,
                        onClick = {
                            if (isUnlocked) {
                                onBuyOrActivate(theme)
                                feedbackMessage = if (isActive) "Déjà actif !" else "✓ ${theme.displayName} activé !"
                            } else if (canAfford) {
                                showConfirmDialog = theme
                            } else {
                                feedbackMessage = "Il te faut ${theme.cost - points} pts de plus 🐾"
                            }
                        }
                    )
                }
            }
        }

        showConfirmDialog?.let { theme ->
            AlertDialog(
                onDismissRequest = { showConfirmDialog = null },
                containerColor = CardBackground,
                titleContentColor = Color.White,
                textContentColor = TextSecondary,
                title = {
                    Text(
                        text = "Acheter ${theme.displayName} ?",
                        fontFamily = impactFont,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(text = theme.description, fontFamily = irishGroverFont)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Coût : ${theme.cost} 🐾  |  Solde : $points 🐾",
                            color = CyanPrimary,
                            fontFamily = irishGroverFont,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val success = onBuyOrActivate(theme)
                        feedbackMessage = if (success) "✓ ${theme.displayName} débloqué !" else "Pas assez de points !"
                        showConfirmDialog = null
                    }) {
                        Text("Acheter", color = CyanPrimary, fontFamily = impactFont, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = null }) {
                        Text("Annuler", color = TextSecondary, fontFamily = irishGroverFont)
                    }
                }
            )
        }
    }
}

@Composable
fun BackgroundCard(
    theme: BackgroundTheme,
    isUnlocked: Boolean,
    isActive: Boolean,
    canAfford: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = when {
            isActive -> CyanPrimary
            isUnlocked -> Color(0xFF4CAF50)
            canAfford -> TextSecondary
            else -> DarkSurface
        },
        animationSpec = tween(300),
        label = "border"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
    ) {
        // Aperçu : image réelle ou dégradé de fallback
        if (theme.drawableRes != null) {
            Image(
                painter = painterResource(id = theme.drawableRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            if (theme.colors.size >= 2) theme.colors
                            else listOf(theme.colors.first(), theme.colors.first())
                        )
                    )
            )
        }

        if (!isUnlocked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(text = theme.emoji, fontSize = 24.sp)
                when {
                    isActive -> Surface(shape = RoundedCornerShape(8.dp), color = CyanPrimary) {
                        Text(
                            text = "ACTIF",
                            color = Color.Black,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = irishGroverFont,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                    isUnlocked -> Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF4CAF50)) {
                        Text(
                            text = "✓",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                    !canAfford -> Text(text = "🔒", fontSize = 16.sp)
                }
            }

            Column {
                Text(
                    text = theme.displayName,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = irishGroverFont
                )
                if (!isUnlocked) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "🐾", fontSize = 10.sp)
                        Text(
                            text = "${theme.cost} pts",
                            color = if (canAfford) CyanPrimary else TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = irishGroverFont,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (!isActive) {
                    Text(
                        text = "Appuie pour activer",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = irishGroverFont
                    )
                }
            }
        }
    }
}
