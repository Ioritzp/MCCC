package com.example.marvelchampionscampaigncompanion

import Classes.*
import DataBaseManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.copy
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.marvelchampionscampaigncompanion.ui.theme.MarvelChampionsCampaignCompanionTheme
import kotlin.text.toIntOrNull

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MarvelChampionsCampaignCompanionTheme {

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Replace the Greeting with our new RegisterScreen
                    RegisterScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(modifier: Modifier = Modifier) {
    // State variables remain the same
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }

    val context = LocalContext.current
    val dbManager = remember { DataBaseManager(context) }

    var passwordVisible by remember { mutableStateOf(false) }
    var repeatPasswordVisible by remember { mutableStateOf(false) }


    // Box for the background image
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.login_soft_background),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(
                Color.Black.copy(alpha = 0.5f),
                blendMode = androidx.compose.ui.graphics.BlendMode.Darken
            )
        )

        // This outer Column centers everything, including the logo and the card
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(0.9f) // Control the overall width
        ) {
            // Logo Image (sits outside/above the white card)
            Image(
                painter = painterResource(id = R.drawable.main_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .fillMaxWidth(0.7f) // 70% of the parent Column's width
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Add a Card to create the white background area
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp), // Give it rounded corners
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.9f) // White, slightly transparent
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                // 2. This inner Column holds the form elements inside the card
                Column(
                    modifier = Modifier.padding(all = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title
                    Text(
                        text = "Register",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Name input box
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name",color = Color.Black) },
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email input box
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email",color = Color.Black) },
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Phone input box
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone",color = Color.Black) },
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password input box
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password",color = Color.Black) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            autoCorrectEnabled = false,
                            keyboardType = KeyboardType.Password
                        ),
                        visualTransformation = if(passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                                Icon(imageVector = eyeImage, description, tint = Color.Black)
                            }
                        }
                    )

                    Text(
                        text = "Password must be minimum 8 characters long and contain at least one letter, one number, an upper case and a special character",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black,
                        modifier = Modifier
                            .padding(start = 16.dp, top = 4.dp)
                            .fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Repeat Password input box
                    OutlinedTextField(
                        value = repeatPassword,
                        onValueChange = { repeatPassword = it },
                        label = { Text("Repeat Password", color = Color.Black) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            autoCorrectEnabled = false,
                            keyboardType = KeyboardType.Password
                        ),
                        visualTransformation = if(repeatPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                            val eyeImage = if (repeatPasswordVisible)
                                Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff

                            // Localized description for accessibility services
                            val description = if (repeatPasswordVisible) "Hide password" else "Show password"

                            IconButton(onClick = { repeatPasswordVisible = !repeatPasswordVisible }) {
                                Icon(imageVector = eyeImage, description, tint = Color.Black)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Register button
                    Button(
                        onClick = {
                            var verifiedUser = false
                            if(repeatPassword == password) {
                                if (phone.toIntOrNull() == null) {
                                    Toast.makeText(context, "The phone number is not valid", Toast.LENGTH_SHORT).show()

                                } else {
                                    val user = User(0, name, email, password, phone.toInt())
                                    verifiedUser = verifyUser(user, context)

                                    if (verifiedUser) {
                                        Toast.makeText(context, "User created!", Toast.LENGTH_SHORT).show()
                                        dbManager.addUser(user)
                                        val intent = Intent(context, MainActivity::class.java)
                                        context.startActivity(intent)
                                    }
                                }
                            }else{
                                Toast.makeText(context, "The passwords do not match", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFED1D24),
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Register")
                    }
                }
            }
        }
    }
}


fun verifyUser(user: User, context: Context): Boolean {
    val dbManager = DataBaseManager(context)

    if (user.name.length < 2) {
        Toast.makeText(context, "The user name is too short", Toast.LENGTH_SHORT).show()
        return false
    }


    if (user.email.length < 4 || !user.email.contains("@")) {
        Toast.makeText(context, "The email is not valid", Toast.LENGTH_SHORT).show()
        return false
    }
    val emailSecondLastChar = user.email[user.email.length - 3]
    val emailThirdLastChar = user.email[user.email.length - 4]
    if (emailSecondLastChar != '.' && emailThirdLastChar != '.') {

        Toast.makeText(context, "chars: ${emailThirdLastChar}, ${emailSecondLastChar}The email format is not valid (e.g., name@domain.com)", Toast.LENGTH_SHORT).show()
        return false
    }

    // 4. Password verification
    if (user.password.length < 8) {
        Toast.makeText(context, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show()
        return false
    }

    val hasUpperCase = user.password.any { it.isUpperCase() }
    val hasLowerCase = user.password.any { it.isLowerCase() } // Added missing check
    val hasDigit = user.password.any { it.isDigit() } // Added missing check
    val specialChars = setOf('$', '#', '_', '&', '%', '@')
    val hasSpecialChar = user.password.any { it in specialChars }

    if (!hasUpperCase || !hasLowerCase || !hasDigit || !hasSpecialChar) {
        Toast.makeText(context, "Password needs an uppercase, lowercase, number, and special character", Toast.LENGTH_LONG).show()
        return false
    }

    val existingUsers = dbManager.getAllUsersForVerification()

    for (existingUser in existingUsers) {
        if (existingUser.email == user.email){
            Log.d("VERIFICATION DEBUG", "EMAIL ALREADY EXISTS")
            Toast.makeText(context, "This email address is already registered", Toast.LENGTH_LONG).show()
            return false
        }
    }

    // If all checks passed, return true
    return true
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    MarvelChampionsCampaignCompanionTheme {
        RegisterScreen()
    }
}