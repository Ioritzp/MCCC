package com.example.marvelchampionscampaigncompanion

import Classes.InstanceCampaign
import Classes.InstanceScenario
import DataBaseManager
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.Image
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.marvelchampionscampaigncompanion.ui.theme.MarvelChampionsCampaignCompanionTheme
import java.time.LocalDateTime

class CampaignInstanceDetail : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //retrieve the campaignId
        val campaignId = intent.getIntExtra("CAMPAIGN_INSTANCE_ID", -1)
        setContent {
            MarvelChampionsCampaignCompanionTheme {
                CampaignDetailRoute(
                    campaignId = campaignId,
                    onNavigateBack = { finish() }
                )


            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignDetailRoute(campaignId: Int, onNavigateBack: () -> Unit){
    Log.d("DEBUG_CAMPAIGN", "CampaignId: $campaignId")
    val context = LocalContext.current
    val dbManager = remember { DataBaseManager(context) }

    //state to hold campaign and its scenarios
    var campaign by remember { mutableStateOf<InstanceCampaign?> (null) }
    var scenarios by remember { mutableStateOf<List<InstanceScenario>>(emptyList()) }
    var refreshTrigger by remember { mutableIntStateOf(0) }


    // This effect runs when campaignId or refreshTrigger changes
    LaunchedEffect(campaignId, refreshTrigger) {
        if (campaignId != -1) {
            // This effect is now ONLY responsible for reading data and updating the UI state.
            campaign = dbManager.getCampaignById(campaignId)
            scenarios = dbManager.readScenariosForCampaign(campaignId)
        }
    }

    // --- Activity Result Launcher to handle returning from a scenario ---
    val scenarioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
            refreshTrigger++
    }

    Scaffold(
        containerColor = Color.Transparent,
    ) { innerPadding ->
        Box (modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.red_background),
                contentDescription = "Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(
                    Color.Black.copy(alpha = 0.6f),
                    blendMode = BlendMode.Darken
                )
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.main_logo), // <-- Use your logo image
                    contentDescription = "Logo",
                    modifier = Modifier
                        .fillMaxWidth(0.8f) // Takes 80% of the screen width
                        .padding(top = 16.dp)
                )

                Text(
                    //TODO: change the typography
                    text = "Campaigns",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                CampaignInstanceDetailScreenContent(
                    modifier = Modifier.padding(innerPadding),
                    scenarios = scenarios,
                    onScenarioClick = { scenario ->
                        val lastScenarioId = scenarios.last().id
                        val isLastScenario = scenario.id == lastScenarioId
                        Log.d("LAST_SCENARIO_ID_CHECK", "last scenario id for campaign: $lastScenarioId")
                        val intent =
                            Intent(context, ScenarioScreen::class.java).apply {
                                putExtra("SCENARIO_ID", scenario.id)
                                putExtra("CAMPAIGN_ID", scenario.instanceCampaignId)
                                putExtra("IS_LAST_SCENARIO", isLastScenario)
                                putExtra("CAMPAIGN_DIFFICULTY", campaign?.difficulty)
                            }
                        scenarioLauncher.launch(intent)

                    }
                )
            }
        }
    }
}


@Composable
fun CampaignInstanceDetailScreenContent(
    modifier: Modifier = Modifier,
    scenarios: List<InstanceScenario>,
    onScenarioClick: (InstanceScenario) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(scenarios, key = { it.id }) { scenario ->
            ScenarioRow(
                scenario = scenario,
                onClick = { onScenarioClick(scenario) }
            )
        }
    }
}

@SuppressLint("LocalContextResourcesRead", "DiscouragedApi")
@Composable
fun ScenarioRow(scenario: InstanceScenario, onClick: () -> Unit, modifier: Modifier = Modifier) {
    // Determine the state based on the logic
    val isLocked = scenario.status == "locked"
    val isClickable = !isLocked
    val opacity = if (isLocked) 0.6f else 1.0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp)
            .graphicsLayer(alpha = opacity) // Apply opacity for locked state
            .clickable(enabled = isClickable, onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val context = LocalContext.current
            val resourceName = "s${scenario.presetScenarioId}"
            val resourceId = context.resources.getIdentifier(
                resourceName,
                "drawable",
                context.packageName
            )

            Image(
                painter = if(resourceId != 0){
                    painterResource(id = resourceId)
                } else {
                    painterResource(id = R.drawable.fallback_image)
                },
                contentDescription = "Scenario: ${scenario.name}",
                modifier = Modifier
                    .size(72.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )


            Spacer(modifier = Modifier.width(16.dp))

            // Name and Villain
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = scenario.name, style = MaterialTheme.typography.titleLarge, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Villain: ${scenario.villainName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
            }

            // Status Icon/Text
            when (scenario.status) {
                "completed" -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Completed",
                            tint = Color(0xFF006400) // Darker Green
                        )
                        Text(
                            text = "Completed",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF006400)
                        )
                    }
                }
                "locked" -> {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Locked",
                        tint = Color.Black.copy(alpha = 0.7f)
                    )
                }
                "current" -> {
                    // Current can be indicated by the lack of an icon, or you could add one
                    Box(modifier = Modifier.width(48.dp)) // Placeholder to maintain alignment
                }
            }
        }
    }
}

// --- Preview ---

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CampaignInstanceDetailScreenPreview() {
    // 1. Create dummy data for the preview
    val dummyScenarios = listOf(
        InstanceScenario(1, 1, 1, "The Rise of Red Skull", "Crossbones", "villain", arrayListOf(), LocalDateTime.now(), LocalDateTime.now(), "completed"),
        InstanceScenario(2, 1, 2, "Absorbing Man", "Absorbing Man", "villain", arrayListOf(), LocalDateTime.now(), LocalDateTime.now(), "current"),
        InstanceScenario(3, 1, 3, "Taskmaster", "Taskmaster", "villain", arrayListOf(), LocalDateTime.now(), LocalDateTime.now(), "locked"),
    )

    MarvelChampionsCampaignCompanionTheme {
        // 2. Use the same Scaffold structure as the main composable
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Campaign Preview") }, // Use static text for the preview title
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    navigationIcon = {
                        IconButton(onClick = {}) { // Empty action for preview
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                )
            }
        ) { innerPadding ->
            // 3. Use the Box with the background image
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.red_background),
                    contentDescription = "Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.tint(
                        Color.Black.copy(alpha = 0.6f),
                        blendMode = BlendMode.Darken
                    )
                )

                // 4. Display the content with the dummy data
                CampaignInstanceDetailScreenContent(
                    modifier = Modifier.padding(innerPadding),
                    scenarios = dummyScenarios,
                    onScenarioClick = {} // Provide an empty click listener for the preview
                )
            }
        }
    }
}




