package com.example.marvelchampionscampaigncompanion

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.scale
import com.example.marvelchampionscampaigncompanion.ui.theme.MarvelChampionsCampaignCompanionTheme
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter



class StartSplashActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MarvelChampionsCampaignCompanionTheme {
                SplashScreen(onTimeout = {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                })
            }
        }
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    // 1. Create a list of all your background images
    val backgroundImages = listOf(
        R.drawable.background_1,
        R.drawable.background_2,
        R.drawable.background_3,
        R.drawable.background_4,
        R.drawable.background_5,
        R.drawable.background_6,
        R.drawable.background_7,
        R.drawable.background_8
        // Add more images here
    )

    // This will be the icon that animates.
    val iconRes = R.drawable.loading_icon
    // State to track the current background image index
    var currentImageIndex by remember { mutableStateOf(0) }

    // State to trigger the icon animation
    var startIconAnimation by remember { mutableStateOf(false) }

    // Animate the alpha (fade-in) and scale (zoom-in) properties for the icon
    val alphaAnim by animateFloatAsState(
        targetValue = if (startIconAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 2000), // 2-second fade-in
        label = "alphaAnimation"
    )

    val scaleAnim by animateFloatAsState(
        targetValue = if (startIconAnimation) 1f else 0.5f, // Start at half size, grow to full size
        animationSpec = tween(durationMillis = 2000),
        label = "scaleAnimation"
    )

    val rotationAnim by animateFloatAsState(
        // Animate from 0 degrees to a full 360-degree rotation
        targetValue = if (startIconAnimation) 360f else 0f,
        animationSpec = tween(durationMillis = 2000), // Same duration as other animations
        label = "rotationAnimation"
    )

    // This LaunchedEffect handles the overall splash screen timeout and icon animation start
    LaunchedEffect(Unit) {
        startIconAnimation = true // Start the icon animation immediately
        delay(4000) // Total splash screen duration (e.g., 4 seconds)
        onTimeout()
    }

    // 2. This separate LaunchedEffect handles the background image slideshow
    LaunchedEffect(backgroundImages) {
        // This loop will run for as long as the SplashScreen is on screen
        while (true) {
            delay(2000) // How long each background image is shown (e.g., 2 seconds)
            // Move to the next image, looping back to the start if at the end
            currentImageIndex = (currentImageIndex + 1) % backgroundImages.size
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 3. Use Crossfade to animate between background images
        Crossfade(
            targetState = currentImageIndex,
            animationSpec = tween(durationMillis = 1000), // Fade transition duration
            label = "backgroundCrossfade"
        ) { imageIndex ->
            Image(
                painter = painterResource(id = backgroundImages[imageIndex]),
                contentDescription = "Splash Screen Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(
                    Color.Black.copy(alpha = 0.5f),
                    blendMode = BlendMode.Darken
                )
            )
        }

        // Animated Icon
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = "Animated App Icon",
            modifier = Modifier
                .size(200.dp)
                .rotate(rotationAnim)
                .scale(scaleAnim)
                .alpha(alphaAnim)
        )
    }
}
