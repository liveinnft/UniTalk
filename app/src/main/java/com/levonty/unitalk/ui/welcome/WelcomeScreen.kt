package com.levonty.unitalk.ui.welcome

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

data class FeatureItem(val emoji: String, val title: String, val subtitle: String)

val features = listOf(
    FeatureItem("🌍", "Весь мир рядом", "Общайся с людьми из 190+ стран"),
    FeatureItem("🗣️", "Языковой обмен", "Учи языки в живом общении"),
    FeatureItem("🤝", "Найди друзей", "По интересам, хобби и культуре"),
    FeatureItem("🔒", "Безопасно", "Защита для всех, включая подростков 14+")
)

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    var currentFeature by remember { mutableStateOf(0) }
    var visible by remember { mutableStateOf(true) }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A1A2E),
            Color(0xFF16213E),
            Color(0xFF0F3460)
        )
    )

    LaunchedEffect(Unit) {
        while (true) {
            delay(2800)
            visible = false
            delay(300)
            currentFeature = (currentFeature + 1) % features.size
            visible = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        // Background decorative circles
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-60).dp, y = (-60).dp)
                .alpha(0.08f)
                .clip(CircleShape)
                .background(Color(0xFF4FC3F7))
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 60.dp, y = 60.dp)
                .alpha(0.08f)
                .clip(CircleShape)
                .background(Color(0xFFE040FB))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo / App name
            Text(
                text = "UniTalk",
                fontSize = 56.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = (-1).sp
            )
            Text(
                text = "Общение без границ",
                fontSize = 16.sp,
                color = Color(0xFF90CAF9),
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(64.dp))

            // Animated feature card
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 },
                exit = fadeOut(tween(200)) + slideOutVertically(tween(200)) { -it / 4 }
            ) {
                val f = features[currentFeature]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.08f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(28.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(f.emoji, fontSize = 40.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            f.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            f.subtitle,
                            fontSize = 14.sp,
                            color = Color(0xFFB0BEC5),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Page dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                features.forEachIndexed { i, _ ->
                    Box(
                        modifier = Modifier
                            .size(if (i == currentFeature) 24.dp else 8.dp, 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (i == currentFeature) Color(0xFF4FC3F7)
                                else Color.White.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            Spacer(Modifier.height(64.dp))

            // CTA Button
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4FC3F7)
                )
            ) {
                Text(
                    "Начать общение",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F3460)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Уже есть аккаунт? Войти",
                color = Color(0xFF90CAF9),
                fontSize = 14.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}