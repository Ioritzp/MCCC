package com.example.marvelchampionscampaigncompanion

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.marvelchampionscampaigncompanion.ui.theme.MarvelChampionsCampaignCompanionTheme

// Data class to represent the state of each scenario
enum class ScenarioStatus {
    COMPLETED,
    CURRENT,
    LOCKED
}

// Data class for a single scenario item
data class ScenarioItem(
    val id: String,
    val name: String,
    val villainName: String,
    @DrawableRes val imageRes: Int?,
    val status: ScenarioStatus
)

class CampaignInstanceDetail : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val instanceName = intent.getStringExtra("CAMPAIGN_INSTANCE_NAME") ?: "Campaign Details"
        setContent {
            MarvelChampionsCampaignCompanionTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(instanceName) },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    CampaignInstanceDetailScreenContent(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun CampaignInstanceDetailScreenContent(modifier: Modifier = Modifier) {
    // --- State now holds the list so it can be updated ---
    var scenarios by remember {
        mutableStateOf(
            listOf(
                ScenarioItem("s1", "Scenario 1", "Rhino", R.drawable.ic_launcher_background, ScenarioStatus.COMPLETED),
                ScenarioItem("s2", "Scenario 2", "Klaw", R.drawable.ic_launcher_foreground, ScenarioStatus.COMPLETED),
                ScenarioItem("s3", "Scenario 3", "Ultron", R.drawable.ic_launcher_background, ScenarioStatus.CURRENT),
                ScenarioItem("s4", "Scenario 4", "Kang", null, ScenarioStatus.LOCKED),
                ScenarioItem("s5", "Scenario 5", "Thanos", null, ScenarioStatus.LOCKED)
            )
        )
    }

    // --- Activity Result Launcher ---
    val scenarioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val completedId = result.data?.getStringExtra("scenario_completed_id")
            if (completedId != null) {
                // Update the list state, which triggers a recomposition
                scenarios = scenarios.map { scenario ->
                    if (scenario.id == completedId) {
                        scenario.copy(status = ScenarioStatus.COMPLETED)
                    } else {
                        scenario
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Campaign 1",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(scenarios, key = { it.id }) { scenario ->
                val context = LocalContext.current
                ScenarioRow(
                    scenario = scenario,
                    onClick = {
                        val intent = Intent(context, ScenarioScreen::class.java).apply {
                            putExtra("SCENARIO_ID", scenario.id)
                            putExtra("SCENARIO_NAME", scenario.name)
                        }
                        // Use the launcher to start the activity for a result
                        scenarioLauncher.launch(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun ScenarioRow(scenario: ScenarioItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val isLocked = scenario.status == ScenarioStatus.LOCKED
    val isClickable = !isLocked
    val opacity = if (isLocked) 0.5f else 1.0f

    Card(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 6.dp, horizontal = 12.dp)
            .graphicsLayer(alpha = opacity) // Apply opacity for locked state
            .clickable(enabled = isClickable, onClick = onClick), // onClick is now passed in
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image or Placeholder
            scenario.imageRes?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = scenario.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
            } ?: Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Name and Villain
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = scenario.name, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = scenario.villainName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Status Icon/Text
            when (scenario.status) {
                ScenarioStatus.COMPLETED -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Completed",
                            tint = Color(0xFF008000) // Dark Green
                        )
                        Text(
                            text = "Completed",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF008000)
                        )
                    }
                }
                ScenarioStatus.LOCKED -> {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Locked",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                ScenarioStatus.CURRENT -> {
                    // No visual element
                }
            }
        }
    }


}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CampaignInstanceDetailScreenPreview() {
    MarvelChampionsCampaignCompanionTheme {
        CampaignInstanceDetailScreenContent()
    }
}