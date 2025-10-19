package com.example.marvelchampionscampaigncompanion

import Classes.*
import DataBaseManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.marvelchampionscampaigncompanion.ui.theme.MarvelChampionsCampaignCompanionTheme

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

//TODO: ALL DATA VERIFICATION HAS TO BE DONE ON IT'S CLASS AND CALL IT ACORDINGLY
@Composable
fun RegisterScreen(modifier: Modifier = Modifier) {
    // State variables to hold the text from each input field
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }

    val context = LocalContext.current

    val dbManager = remember { DataBaseManager(context) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(text = "Register", style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(32.dp))

        // Name input box
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Email input box
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Phone input box
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password input box
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation() // Hides the password text
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Repeat Password input box
        OutlinedTextField(
            value = repeatPassword,
            onValueChange = { repeatPassword = it },
            label = { Text("Repeat Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation() // Hides the password text
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Register button - this calls the database and stores the user.
        Button(
            onClick = {
                val user = User(0, name, email, password, phone.toInt())

                dbManager.addUser(user)

                //go back to main menu - TODO: make this only happen if the user has been created correctly
                val intent = Intent(context, BetterMainActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    MarvelChampionsCampaignCompanionTheme {
        RegisterScreen()
    }
}