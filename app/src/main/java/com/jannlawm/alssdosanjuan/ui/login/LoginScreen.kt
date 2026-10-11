package com.jannlawm.alssdosanjuan.ui.login

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jannlawm.alssdosanjuan.R
import com.jannlawm.alssdosanjuan.data.model.User
import com.jannlawm.alssdosanjuan.ui.theme.DepEdBlue
import com.jannlawm.alssdosanjuan.ui.theme.DepEdRed
import com.jannlawm.alssdosanjuan.ui.theme.DepEdYellow
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = viewModel(),
    onLoginSuccess: (User) -> Unit
) {
    val state by loginViewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        loginViewModel.resetState()
    }

    LaunchedEffect(state.loggedInUser) {
        state.loggedInUser?.let { user ->
            onLoginSuccess(user)
            loginViewModel.resetState()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Dynamic Blurred Moving Slideshow Background
        AnimatedBlurredBackground()

        // Centered Frosted Glass Login Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glassmorphism Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xCC0B1224) // Frosted Deep Navy Slate
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.20f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // DepEd Tricolor Accent Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(99.dp))
                    ) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(DepEdBlue))
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(DepEdRed))
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(DepEdYellow))
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Official Logo Badge with Glow
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.95f))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.als_logo),
                            contentDescription = "DepEd ALS Logo",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Typography Branding
                    Text(
                        text = "DEPARTMENT OF EDUCATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = DepEdYellow,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    )

                    Text(
                        text = "SDO SAN JUAN CITY",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Text(
                        text = "ALS Mobile LMS",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Sign in to access your self-learning modules",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.70f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    // Username / Email / LRN Field
                    OutlinedTextField(
                        value = state.loginId,
                        onValueChange = { loginViewModel.onLoginIdChanged(it) },
                        label = { Text("Username, Email, or LRN", color = Color.White.copy(alpha = 0.8f)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = DepEdYellow
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = DepEdYellow,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                            focusedContainerColor = Color.White.copy(alpha = 0.08f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Field
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { loginViewModel.onPasswordChanged(it) },
                        label = { Text("Password", color = Color.White.copy(alpha = 0.8f)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = DepEdYellow
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = Color.White.copy(alpha = 0.75f)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = DepEdYellow,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                            focusedContainerColor = Color.White.copy(alpha = 0.08f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                loginViewModel.login()
                            }
                        )
                    )

                    // Error Alert Card
                    if (state.errorMessage != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x3DF43F5E))
                                .border(1.dp, Color(0x66F43F5E), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFFF6B81),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = state.errorMessage!!,
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Sign In Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            loginViewModel.login()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DepEdBlue,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 2.dp
                        ),
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Authenticating...",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "Sign In",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Footer Note
                    Text(
                        text = "Alternative Learning System \u2022 San Juan City`r`nOffline Reading & Telemetry Synchronized",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.50f),
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedBlurredBackground(
    modifier: Modifier = Modifier
) {
    val images = remember {
        listOf(
            R.drawable.forbg,
            R.drawable.forbg1,
            R.drawable.forbg3
        )
    }

    var slideCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5500) // 5.5 seconds per slide
            slideCount++
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Crossfade(
            targetState = slideCount,
            animationSpec = tween(durationMillis = 1500, easing = LinearOutSlowInEasing),
            modifier = Modifier.fillMaxSize(),
            label = "BackgroundCrossfade"
        ) { targetCount ->
            val imgRes = images[targetCount % images.size]
            val isLeftToRight = targetCount % 2 == 0

            val infiniteTransition = rememberInfiniteTransition(label = "pan_transition_$targetCount")
            val panOffset by infiniteTransition.animateFloat(
                initialValue = if (isLeftToRight) -45f else 45f,
                targetValue = if (isLeftToRight) 45f else -45f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 6500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pan_offset"
            )

            Image(
                painter = painterResource(id = imgRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .scale(1.25f)
                    .graphicsLayer {
                        translationX = panOffset
                    }
                    .blur(radius = 18.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
            )
        }

        // Frosted Dark Gradient Overlay for optimal text contrast and rich aesthetics
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x6600102E), // Subtle dark DepEd navy
                            Color(0x8A081226), // Mid deep midnight
                            Color(0xD9040814)  // Bottom dark slate
                        )
                    )
                )
        )
    }
}