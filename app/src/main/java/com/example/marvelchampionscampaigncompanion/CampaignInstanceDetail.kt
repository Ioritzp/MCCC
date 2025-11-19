package com.example.marvelchampionscampaigncompanion

import Classes.InstanceCampaign
import Classes.InstanceScenario
import DataBaseManager
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    val dbManager = remember { DataBaseManager(context) }

    //state to hold campaign and its scenarios
    var campaign by remember { mutableStateOf<InstanceCampaign?> (null) }
    var scenarios by remember { mutableStateOf<List<InstanceScenario>>(emptyList()) }
    var refreshTrigger by remember { mutableStateOf(0) }
    Log.d("INTENT_DEBUG", "Campaigndetailroute: CAMPAIGN_ID: $campaignId")
    // This effect runs when campaignId or refreshTrigger changes
    LaunchedEffect(campaignId, refreshTrigger) {
        if (campaignId != -1) {
            campaign = dbManager.getCampaignById(campaignId)
            scenarios = dbManager.readScenariosForCampaign(campaignId)

            for(scenario in scenarios){
                println("status" + scenario.status)
            }
        }
    }

    // --- Activity Result Launcher to handle returning from a scenario ---
    val scenarioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // When we return, trigger a refresh to get the latest data from the DB
            refreshTrigger++
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(campaign?.name ?: "Campaign Details") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        CampaignInstanceDetailScreenContent(
            modifier = Modifier.padding(innerPadding),
            scenarios = scenarios,
            onScenarioClick = { scenario ->
                Log.d("INTENT_DEBUG", "Sending SCENARIO_ID: ${scenario.id}, CAMPAIGN_ID: ${scenario.instanceCampaignId}\")\n")
                val intent =
                    Intent(context, ScenarioScreen::class.java).apply {
                        putExtra("SCENARIO_ID", scenario.id)
                        putExtra("CAMPAIGN_ID", scenario.instanceCampaignId)
                        putExtra("CAMPAIGN_DIFFICULTY", campaign?.difficulty)
                    }
                scenarioLauncher.launch(intent)


            }
        )
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(scenarios, key = { it.id }) { scenario ->
            ScenarioRow(
                scenario = scenario,
                onClick = { onScenarioClick(scenario) }
            )
        }
    }
}

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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // TODO: Replace with dynamic image based on preset scenario
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.shapes.medium
                    )
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Name and Villain
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // TODO: Get scenario name and villain name from preset tables
                Text(text = "Scenario ${scenario.name}", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Villain: ${scenario.villainName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CampaignInstanceDetailScreenPreview() {
    // Create dummy data for the preview
    val dummyScenarios = listOf(
        InstanceScenario(1, 1, 1, "scenario 1", "villain", "villain", arrayListOf(), LocalDateTime.now(), LocalDateTime.now(), "completed"),
        InstanceScenario(2, 1, 2, "scenario 2", "villain", "villain", arrayListOf(), LocalDateTime.now(), LocalDateTime.now(), "current"),
        InstanceScenario(3, 1, 2, "scenario 3", "villain", "villain", arrayListOf(), LocalDateTime.now(), LocalDateTime.now(), "locked"),
    )
    MarvelChampionsCampaignCompanionTheme {
        Surface {
            CampaignInstanceDetailScreenContent(
                scenarios = dummyScenarios,
                onScenarioClick = {}
            )
        }
    }
}



