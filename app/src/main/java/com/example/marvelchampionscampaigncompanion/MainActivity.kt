package com.example.marvelchampionscampaigncompanion

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.activity.addCallback
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
                LoginMenuCenteredInputScreen(
                    modifier = Modifier.fillMaxSize(),
                    onLoginClick = { ->
                        Toast.makeText(this, "Going to main menu", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, BetterMainMenu::class.java)
                        startActivity(intent)

                    })
            }
        }


    }

    @Composable
    fun LoginMenuCenteredInputScreen(
        modifier: Modifier = Modifier,
        onLoginClick: () -> Unit
    ) {
        var emailInput by remember { mutableStateOf(TextFieldValue("")) }
        var passwordInput by remember { mutableStateOf(TextFieldValue("")) }
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
                    .fillMaxWidth(0.8f)
                //.weight(1f)
                ,
                horizontalAlignment = Alignment.CenterHorizontally,
                //verticalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Insert eMail") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Insert password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,


                    ) {

                    Button(
                        onClick = { onLoginClick() },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Login")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Register")
                    }
                }
            }

        }
    }


    @Preview(showBackground = true, showSystemUi = true)
    @Composable
    fun LoginMenuCenteredInputScreenPreview() {
        MarvelChampionsCampaignCompanionTheme {
            LoginMenuCenteredInputScreen(modifier = Modifier.fillMaxSize(), onLoginClick = {})
        }
    }
}