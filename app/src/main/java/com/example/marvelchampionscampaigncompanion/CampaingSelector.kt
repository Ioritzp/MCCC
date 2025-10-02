package com.example.marvelchampionscampaigncompanion


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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.isEmpty
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

//genera el scroll de selector
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
    val campaignInstances = remember {
        mutableStateListOf<CampaingBlueprint>(
        CampaingBlueprint(
            "inst_001",
            "id_Campaign1",
            "Rise of Red Skull",
            "Standard",
            R.drawable.ic_launcher_background
        ),
        CampaingBlueprint(
            "inst_002",
            "id_Campaign2",
            "Most Wanted",
            "Expert",
            R.drawable.ic_launcher_background
        ),
        CampaingBlueprint("inst_003",
            "id_Campaign3",
            "Mad Titan",
            "Standard"
        )
        )
    }

    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("select campaign instance") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                // --- Logic to "create" a new campaign instance ---
                // This is where you'd typically open a new screen/dialog to select a blueprint
                // and configure the new instance. For this example, we'll add a dummy one.
                val newId = "inst_${System.currentTimeMillis()}"
                val blueprint = "bp_newA"
                val newName = "Newly Created Campaign"
                val newDifficulty = "Standard II"
                val newImage =  R.drawable.ic_launcher_background


                campaignInstances.add(
                    CampaingBlueprint(
                        id = newId,
                        blueprintId = blueprint,
                        name = newName,
                        difficulty = newDifficulty,
                        minatureImageRes = newImage
                    )
                )
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Create New Campaign Instance")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (campaignInstances.isEmpty()) {
                Box(
                    modifier = Modifier
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
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp) // Padding for the list itself
                ) {
                    items(
                        items = campaignInstances,
                        key = { instance -> instance.id } // Stable and unique key
                    ) { campaignInstance ->
                        SelectCampaignMenu(
                            campaignInstance = campaignInstance,
                            onClick = {

                                val intent = Intent(
                                    context,
                                    CampaignInstanceDetail::class.java
                                ).apply {
                                    putExtra("CAMPAIGN_INSTANCE_ID", campaignInstance.id)
                                    putExtra("CAMPAIGN_INSTANCE_NAME", campaignInstance.name)
                                    // You can pass the whole object if it's Parcelable, or individual fields
                                }
                                context.startActivity(intent)

                            }
                        )
                    }
                }

            }
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