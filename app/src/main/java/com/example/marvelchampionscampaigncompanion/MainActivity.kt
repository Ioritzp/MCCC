package com.example.marvelchampionscampaigncompanion

import DataBaseManager
import Utils.Verifications
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.activity.addCallback
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import com.example.marvelchampionscampaigncompanion.ui.theme.MarvelChampionsCampaignCompanionTheme


class BetterMainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this) {
            finish()
        }
        enableEdgeToEdge()
        setContent {
            MarvelChampionsCampaignCompanionTheme {
                LoginMenuCenteredInputScreen(modifier = Modifier.fillMaxSize())

            }
        }
    }

    @Composable
    fun LoginMenuCenteredInputScreen(
        modifier: Modifier = Modifier,
    ) {
        var emailInput by remember { mutableStateOf(TextFieldValue("")) }
        var passwordInput by remember { mutableStateOf(TextFieldValue("")) }
        // Get the current context to use for starting a new activity
        val context = LocalContext.current

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))
            Text(
                text = "Marvel Champions Campaign Companion",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 200.dp),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Insert eMail") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Insert password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {

                    Button(
                        onClick = {
                            val correctLogin = checkLogin(emailInput.text, passwordInput.text)

                            if(correctLogin){
                                val intent = Intent(context, SelectCampaign()::class.java)
                                context.startActivity(intent)

                            } else{
                                Toast.makeText(context, "Incorrect login", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Login")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            val intent = Intent(context, RegisterActivity()::class.java)
                            context.startActivity(intent)

                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Register")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        // Navigate to DBManager Activity -- TODO: THIS BUTTON MUST BE HIDDEN FOR ADMIN ACCOUNT ONLY IN FINAL VERSION
                        val intent = Intent(context, DataAdminActivity()::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Admin")
                }
            }
        }
    }

    fun checkLogin(email: String, password: String): Boolean {
        //TODO:according to gemini, ghis can be simplified with the "find" method. Try both and comment the least efficient.
       var db = DataBaseManager(this)
        var isRegistered = false
        var checkedPassword = false
        val users = db.readUsers()
        for (user in users){
            checkedPassword = Verifications.checkPassword(password, user.password)
            if (user.email == email && checkedPassword){
                isRegistered = true
                break

            }
        }
         return isRegistered
    }


    @Preview(showBackground = true, showSystemUi = true)
    @Composable
    fun LoginMenuCenteredInputScreenPreview() {
        MarvelChampionsCampaignCompanionTheme {
            LoginMenuCenteredInputScreen(modifier = Modifier.fillMaxSize())
        }
    }
}