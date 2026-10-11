package com.jannlawm.alssdosanjuan.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannlawm.alssdosanjuan.R
import com.jannlawm.alssdosanjuan.ui.theme.DepEdBlue
import com.jannlawm.alssdosanjuan.ui.theme.DepEdRed
import com.jannlawm.alssdosanjuan.ui.theme.DepEdYellow
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // Infinite transition for alternating cross-fade between the 2 logos
    val infiniteTransition = rememberInfiniteTransition(label = "SplashLogoTransition")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LogoFadeAlpha"
    )

    LaunchedEffect(Unit) {
        delay(3200) // Total splash duration
        onSplashFinished()
    }

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
            // DepEd Header Accent Lines
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(6.dp)
                    .background(DepEdRed)
            )
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(4.dp)
                    .background(DepEdYellow)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Alternating Logos Cross-Fade Box
            Box(
                modifier = Modifier.size(150.dp),
                contentAlignment = Alignment.Center
            ) {
                // ALS Logo (Fades In / Out)
                Image(
                    painter = painterResource(id = R.drawable.als_logo),
                    contentDescription = "ALS Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(alphaAnim)
                )

                // SDO ALS Logo (Inverse Fade Alpha)
                Image(
                    painter = painterResource(id = R.drawable.sdo_als_logo),
                    contentDescription = "SDO Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(1f - alphaAnim)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "DepEd SDO San Juan",
                style = MaterialTheme.typography.titleMedium,
                color = DepEdBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Alternative Learning System",
                style = MaterialTheme.typography.bodyMedium,
                color = DepEdRed,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Mobile LMS",
                style = MaterialTheme.typography.headlineMedium,
                color = DepEdBlue,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
