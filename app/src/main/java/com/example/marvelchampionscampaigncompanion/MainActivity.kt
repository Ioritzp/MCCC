package com.example.marvelchampionscampaigncompanion

import Classes.User
import DataBaseManager
import Utils.SessionManager
import Utils.Verifications
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.marvelchampionscampaigncompanion.ui.theme.MarvelChampionsCampaignCompanionTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MarvelChampionsCampaignCompanionTheme {
                LoginMenuCenteredInputScreen()
            }
        }
    }
}

@Composable
fun LoginMenuCenteredInputScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var emailInput by remember { mutableStateOf(TextFieldValue("")) }
    var passwordInput by remember { mutableStateOf(TextFieldValue("")) }
    var passwordVisible by remember { mutableStateOf(false) }


    Box(
        modifier = Modifier.fillMaxSize(),

    ) {
        Image(
            painter = painterResource(id = R.drawable.login_soft_background),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,

        )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // App Title
        Image(
            painter = painterResource(id = R.drawable.main_logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(width = 400.dp, height = 400.dp)
                .fillMaxWidth(0.8f) // Use 80% of screen width to avoid touching the edges
                .padding(top = 10.dp, bottom = 20.dp)
        )


        // Column for inputs and buttons
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.9f))
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedTextField(
                value = emailInput,
                onValueChange = { emailInput = it },
                label = { Text("Email Address",color = Color.Black) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.DarkGray,
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.DarkGray,
                    cursorColor = Color.Black,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = passwordInput,
                onValueChange = { passwordInput = it },
                label = { Text("Password",color = Color.Black) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Password
                    ),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.DarkGray,
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.DarkGray,
                    cursorColor = Color.Black,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                trailingIcon = {
                    val eyeImage = if (passwordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    // Localized description for accessibility services
                    val description = if (passwordVisible) "Hide password" else "Show password"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = eyeImage, description)
                    }
                }
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Row for Login and Register buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Login Button
                Button(
                    onClick = {
                        val loggedinUser = checkLogin(context, emailInput.text, passwordInput.text)
                        if (loggedinUser != null) {
                            val sessionManager = SessionManager(context)
                            sessionManager.saveUserId(loggedinUser.id)
                            val intent = Intent(context, CampaignSelector::class.java).apply{
                                putExtra("USER_ID", loggedinUser.id)
                            }
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, "Incorrect login", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFED1D24),
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text("Login")
                }

                // Register Button
                Button(
                    onClick = {
                        context.startActivity(Intent(context, RegisterActivity::class.java))
                    },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFED1D24),
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text("Register")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

        }
    }
    }
}

// Helper function remains the same
fun checkLogin(context: android.content.Context, email: String, password: String): User? {
    val db = DataBaseManager(context)
    val users = db.readUsers()
    return users.find { user ->
        user.email == email && Verifications.checkPassword(password, user.password)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginMenuCenteredInputScreenPreview() {
    MarvelChampionsCampaignCompanionTheme {
        LoginMenuCenteredInputScreen()
    }
}