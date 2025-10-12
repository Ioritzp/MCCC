package com.example.marvelchampionscampaigncompanion

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.marvelchampionscampaigncompanion.ui.theme.MarvelChampionsCampaignCompanionTheme
import kotlin.random.Random

// Data classes to hold player info
data class PlayerStatus(val name: String, val lifePoints: Int, val selectedTool: String? = null)

// Enum to manage the flow of completion dialogs
private enum class CompletionStep {
    LifePoints, RescuedHeroes, BlackSwan, Finished
}

class ScenarioScreen : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val scenarioId = intent.getStringExtra("SCENARIO_ID") ?: "s1"
        val scenarioName = intent.getStringExtra("SCENARIO_NAME") ?: "Scenario"

        setContent {
            MarvelChampionsCampaignCompanionTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(scenarioName) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    ScenarioScreenContent(
                        scenarioId = scenarioId,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun ScenarioScreenContent(scenarioId: String, modifier: Modifier = Modifier) {
    // Determine which scenario content to show
    when (scenarioId) {
        "s1" -> Scenario1Content(modifier)
        "s2" -> Scenario2Content(modifier)
        "s3" -> Scenario3Content(modifier)
        else -> Text("Unknown Scenario", modifier = modifier.padding(16.dp))
    }
}

// --- Content for each specific scenario ---

@Composable
fun Scenario1Content(modifier: Modifier = Modifier) {
    val players = remember {
        listOf(
            PlayerStatus("Player 1", Random.nextInt(5, 12)),
            PlayerStatus("Player 2", Random.nextInt(5, 12))
        )
    }
    PlayerInfoSection(title = "Final Status", players = players, modifier = modifier)
}

@Composable
fun Scenario2Content(modifier: Modifier = Modifier) {
    val players = remember {
        val tools = listOf("Web-Shooters", "Repulsor Tech", "Widow's Bite").shuffled()
        listOf(
            PlayerStatus("Player 1", Random.nextInt(5, 12), tools[0]),
            PlayerStatus("Player 2", Random.nextInt(5, 12), tools[1])
        )
    }
    val otherInfo = mapOf(
        "Phase Two Reached" to "Yes",
        "Weapons Stolen" to "Rifle, Gauntlets"
    )

    Column(modifier = modifier.fillMaxSize()) {
        PlayerInfoSection(title = "Final Status", players = players)
        OtherInfoSection(info = otherInfo)
    }
}

@Composable
fun Scenario3Content(modifier: Modifier = Modifier) {
    var showDialogs by remember { mutableStateOf(false) }

    val players = remember {
        val tools = listOf("Web-Shooters", "Repulsor Tech", "Widow's Bite", "Shield", "Bow").shuffled()
        listOf(
            PlayerStatus("Player 1", Random.nextInt(8, 12), tools[0]),
            PlayerStatus("Player 2", Random.nextInt(8, 12), tools[1]),
            PlayerStatus("Player 3", Random.nextInt(8, 12), tools[2])
        )
    }
    val otherInfo = mapOf("Counters over Main Plan" to "3")

    Box(modifier = modifier.fillMaxSize()) {
        Column {
            PlayerInfoSection(title = "Current Status", players = players)
            OtherInfoSection(info = otherInfo)
        }

        Button(
            onClick = { showDialogs = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text("Complete Level")
        }
    }

    if (showDialogs) {
        CompletionDialogFlow(
            playerCount = players.size,
            onDismiss = { showDialogs = false }
        )
    }
}


// --- Reusable UI Sections ---

@Composable
fun PlayerInfoSection(title: String, players: List<PlayerStatus>, modifier: Modifier = Modifier) {
    Column(modifier.padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        LazyColumn {
            items(players) { player ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(player.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Life Points: ${player.lifePoints}")
                        player.selectedTool?.let {
                            Text("Selected Tool: $it")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OtherInfoSection(info: Map<String, String>) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        Divider(thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
        info.forEach { (key, value) ->
            Row {
                Text("$key: ", fontWeight = FontWeight.Bold)
                Text(value)
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}


// --- Dialog Flow for Completing Scenario 3 ---

@Composable
fun CompletionDialogFlow(playerCount: Int, onDismiss: () -> Unit) {
    var step by remember { mutableStateOf(CompletionStep.LifePoints) }
    var currentDialogPlayerIndex by remember { mutableIntStateOf(1) }
    val context = LocalContext.current
    val activity = (LocalActivity.current as? Activity)

    when (step) {
        CompletionStep.LifePoints -> {
            LifePointsDialog(
                playerNumber = currentDialogPlayerIndex,
                onDismiss = onDismiss,
                onConfirm = {
                    if (currentDialogPlayerIndex < playerCount) {
                        currentDialogPlayerIndex++
                    } else {
                        step = CompletionStep.RescuedHeroes
                    }
                }
            )
        }
        CompletionStep.RescuedHeroes -> {
            RescuedHeroesDialog(
                onDismiss = onDismiss,
                onConfirm = { rescuedHeroes ->
                    // You can use the list of rescuedHeroes strings here if needed
                    step = CompletionStep.BlackSwan
                }
            )
        }
        CompletionStep.BlackSwan -> {
            YesNoDialog(
                title = "Black Swan",
                text = "Is Black Swan defeated?",
                onDismiss = onDismiss,
                onConfirm = { wasDefeated ->
                    Toast.makeText(context, "Scenario Completed!", Toast.LENGTH_SHORT).show()
                    // *** NEW: Set result for the previous activity ***
                    val resultIntent = Intent().putExtra("scenario_completed_id", "s3")
                    activity?.setResult(Activity.RESULT_OK, resultIntent)
                    step = CompletionStep.Finished
                }
            )
        }
        CompletionStep.Finished -> {
            // Dialog flow is over, go back to the previous screen
            LaunchedEffect(Unit) {
                activity?.finish()
            }
        }
    }
}

// --- Individual Dialog Composables ---

@Composable
fun LifePointsDialog(playerNumber: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var lifePoints by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Player $playerNumber: Life Points") },
        text = {
            TextField(
                value = lifePoints,
                onValueChange = { lifePoints = it.filter { char -> char.isDigit() } },
                label = { Text("Enter remaining life") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            Button(
                enabled = lifePoints.isNotEmpty(),
                onClick = { onConfirm(lifePoints.toInt()) }
            ) { Text("Next") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// *** MODIFIED to allow multi-select ***
@Composable
fun RescuedHeroesDialog(
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    val allHeroes = listOf("Elektra", "Shang-Chi", "Daredevil", "Moon Knight")
    var selectedHeroes by remember { mutableStateOf(setOf<String>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rescue Heroes") },
        text = {
            Column {
                Text("Select any number of heroes that were rescued.")
                Spacer(Modifier.height(8.dp))
                allHeroes.forEach { hero ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = hero in selectedHeroes,
                                onValueChange = {
                                    selectedHeroes = if (it) {
                                        selectedHeroes + hero
                                    } else {
                                        selectedHeroes - hero
                                    }
                                },
                                role = Role.Checkbox
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = hero in selectedHeroes,
                            onCheckedChange = null // null because toggleable handles it
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(hero)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedHeroes.toList()) }) {
                Text("Next")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun YesNoDialog(title: String, text: String, onDismiss: () -> Unit, onConfirm: (Boolean) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            Row(Modifier.padding(horizontal = 8.dp)) {
                Button(onClick = { onConfirm(true) }, modifier = Modifier.weight(1f)) {
                    Text("Yes")
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { onConfirm(false) }, modifier = Modifier.weight(1f)) {
                    Text("No")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}