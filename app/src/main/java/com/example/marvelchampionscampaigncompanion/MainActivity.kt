package com.example.marvelchampionscampaigncompanion

import DataBaseManager
import Utils.Verifications
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
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
                // MainActivity now only shows the Login Screen.
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // App Title
        Text(
            text = "Marvel Champions Campaign Companion",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 80.dp, bottom = 60.dp)
        )

        // Column for inputs and buttons
        Column(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = emailInput,
                onValueChange = { emailInput = it },
                label = { Text("Email Address") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = passwordInput,
                onValueChange = { passwordInput = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
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
                        val correctLogin = checkLogin(context, emailInput.text, passwordInput.text)
                        if (correctLogin) {
                            // *** MODIFIED: Use Intent to go to CampaignSelector Activity ***
                            val intent = Intent(context, CampaignSelector::class.java)
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, "Incorrect login", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text("Login")
                }

                // Register Button
                Button(
                    onClick = {
                        context.startActivity(Intent(context, RegisterActivity::class.java))
                    },
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text("Register")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Admin Button
            Button(
                onClick = {
                    context.startActivity(Intent(context, DataAdminActivity::class.java))
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Admin")
            }
        }
    }
}

// Helper function remains the same
fun checkLogin(context: android.content.Context, email: String, password: String): Boolean {
    val db = DataBaseManager(context)
    val users = db.readUsers()
    return users.any { user ->
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