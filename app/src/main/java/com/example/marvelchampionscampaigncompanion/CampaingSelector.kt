package com.example.marvelchampionscampaigncompanion


import Classes.InstanceCampaign
import DataBaseManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.marvelchampionscampaigncompanion.ui.theme.CampaingBlueprint
import com.example.marvelchampionscampaigncompanion.ui.theme.MarvelChampionsCampaignCompanionTheme


class SelectCampaign : ComponentActivity(){

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MarvelChampionsCampaignCompanionTheme {
                SelectCampaignScreen()
            }
        }
    }


}

//The SelectCampaignMenu is no longer used in this simplified view, but can be kept for future use
@Composable
fun SelectCampaignMenu(campaignInstance: CampaingBlueprint, onClick: () -> Unit, modifier: Modifier = Modifier) {

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier= Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically

        ){
            campaignInstance.minatureImageRes?.let { imageRes ->
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "${campaignInstance.name} preview image",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
            } ?: Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.shapes.medium
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = campaignInstance.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Difficulty: ${campaignInstance.difficulty}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                //detalles a añadir, opcional
            }


        }


    }

}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectCampaignScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val dbManager = remember { DataBaseManager(context) }
    val availableCampaigns = remember { dbManager.getAllPresetCampaigns() }


    Scaffold(
        modifier = modifier.fillMaxSize(),
        // Set background color for the main content area
        containerColor = Color(0xFFE8F5E9), // Light Green
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Use a simple Text composable. The TopAppBar will center it by default.
                        Text("Campaign menu")
                    }
                },
                // Let the TopAppBar determine its own size, but we can customize colors.
                colors = TopAppBarDefaults.topAppBarColors(
                    // Set TopAppBar background color
                    containerColor = Color(0xFFFFF9C4), // Light Yellow
                    // Set title color
                    titleContentColor = Color.Black
                )
            )
        },
        floatingActionButton = {
            // Wrap FAB in a Box to control its size
            Box(modifier = Modifier.size(72.dp)) {
                FloatingActionButton(
                    onClick = {
                        context.startActivity(Intent(context, NewCampaignCreation::class.java))
                    },
                    // Fill the parent Box
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Create New Campaign Instance",
                        // Make the icon inside the FAB larger
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        // The main content area now always shows the "empty" state text.
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No campaigns yet.\nTap the '+' button to create one!",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SelectCampaignScreenPreview() {
    MarvelChampionsCampaignCompanionTheme {
        SelectCampaignScreen()
    }
}