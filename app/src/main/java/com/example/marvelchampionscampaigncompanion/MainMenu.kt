package com.example.marvelchampionscampaigncompanion

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.example.marvelchampionscampaigncompanion.ui.theme.MarvelChampionsCampaignCompanionTheme


class BetterMainMenu : ComponentActivity(){

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MarvelChampionsCampaignCompanionTheme {
                MainMenuCenteredInputScreen(modifier = Modifier.fillMaxSize(),
                    onNewCampaignClick = { ->
                        val intent = Intent(this, SelectCampaign::class.java)
                        startActivity(intent)

                    })
            }
        }
    }


}

@Composable
fun MainMenuCenteredInputScreen(
    modifier: Modifier = Modifier,
    onNewCampaignClick: () -> Unit){

    var buttonAwidth by remember { mutableStateOf<Dp?>(null) }
    val density = LocalDensity.current
    var shortBUttonModifier = modifier

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(50.dp))
        Text(
            text = "MarvelCCC",
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

            Button(
                onClick = { },
                modifier = Modifier.onGloballyPositioned{coordinates ->
                    val newWidhtDP = with(density){coordinates.size.width.toDp()}
                    if (buttonAwidth != newWidhtDP){
                        buttonAwidth = newWidhtDP
                    }
                }
            ) {
                Text("Continue Campaign")
            }
            Spacer(modifier = Modifier.height(16.dp))

            shortBUttonModifier = if(buttonAwidth != null) {
                Modifier.width(buttonAwidth!!)
            } else{
                Modifier
            }
            }
                Button(
                    onClick = {onNewCampaignClick() },
                    modifier = shortBUttonModifier
                ) {
                    Text("New Campaign")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { },
                    modifier = shortBUttonModifier

                ) {
                    Text("Load Campaign")
                }
            }
        }





@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CenteredInputScreenPreview() {
    MarvelChampionsCampaignCompanionTheme {
        MainMenuCenteredInputScreen(modifier = Modifier.fillMaxSize(), onNewCampaignClick = {})
    }
}