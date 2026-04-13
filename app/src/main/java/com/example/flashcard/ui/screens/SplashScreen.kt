package com.example.flashcard.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashcard.R
import com.example.flashcard.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onComplete: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000L)
        onComplete()
    }

    Box(modifier = Modifier.fillMaxSize().background(NeoBackgroundPink)) {
        // Subtle Labyrinth texture for premium feel
        Image(
            painter = painterResource(id = R.drawable.labyrinth),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.15f
        )

        // Center FLIP logo card
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // Neo-Brutalism shadow (navy offset block)
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .offset(x = 7.dp, y = 7.dp)
                    .background(NeoNavy, RoundedCornerShape(20.dp))
            )
            // Main pink card
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(NeoBackgroundPink, RoundedCornerShape(20.dp))
                    .border(3.dp, NeoNavy, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Stacked mini cards icon (neo-brutalism style)
                    Box(contentAlignment = Alignment.TopStart) {
                        // Back card shadow
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .offset(x = 7.dp, y = 7.dp)
                                .background(NeoNavy.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        )
                        // Back card
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .offset(x = 5.dp, y = 5.dp)
                                .background(NeoBackgroundBlue, RoundedCornerShape(6.dp))
                                .border(2.dp, NeoNavy, RoundedCornerShape(6.dp))
                        )
                        // Front card
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(NeoWhite, RoundedCornerShape(6.dp))
                                .border(2.dp, NeoNavy, RoundedCornerShape(6.dp))
                        )
                    }

                    Text(
                        text = "FLIP",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = NeoNavy,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}
