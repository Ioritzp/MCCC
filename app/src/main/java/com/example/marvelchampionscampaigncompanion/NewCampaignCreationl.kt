package com.example.marvelchampionscampaigncompanion

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.marvelchampionscampaigncompanion.ui.theme.MarvelChampionsCampaignCompanionTheme

// Data class to represent a campaign blueprint that the user can choose
data class CampaignBlueprint(
    val id: String,
    val name: String,
    @DrawableRes val imageRes: Int?
)

class NewCampaignCreation : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MarvelChampionsCampaignCompanionTheme {
                NewCampaignCreationScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewCampaignCreationScreen(modifier: Modifier = Modifier) {
    // List of available campaign blueprints to start from
    val availableCampaigns = remember {
        listOf(
            CampaignBlueprint("bp_red_skull", "Rise of Red Skull", R.drawable.ic_launcher_background),
            CampaignBlueprint("bp_mutant_genesis", "Mutant Genesis", R.drawable.ic_launcher_foreground),
            CampaignBlueprint("bp_mad_titan", "The Mad Titan's Shadow", null)
        )
    }

    // --- State Management ---
    var showPlayerCountDialog by remember { mutableStateOf(false) }
    var showHeroSelectionDialog by remember { mutableStateOf(false) }
    var showDifficultyDialog by remember { mutableStateOf(false) }
    var selectedCampaign by remember { mutableStateOf<CampaignBlueprint?>(null) }
    var playerCount by remember { mutableIntStateOf(0) }
    var heroSelections by remember { mutableStateOf<List<String>>(emptyList()) }

    val context = LocalContext.current

    // --- Dialog Chain Logic ---
    if (showPlayerCountDialog) {
        PlayerCountDialog(
            onDismiss = { showPlayerCountDialog = false },
            onConfirm = { count ->
                playerCount = count
                heroSelections = List(count) { "" } // Reset hero selections
                showPlayerCountDialog = false
                showHeroSelectionDialog = true
            }
        )
    }

    if (showHeroSelectionDialog) {
        HeroSelectionDialog(
            playerCount = playerCount,
            onDismiss = { showHeroSelectionDialog = false },
            onConfirm = { heroes ->
                heroSelections = heroes
                showHeroSelectionDialog = false
                showDifficultyDialog = true
            }
        )
    }

    if (showDifficultyDialog) {
        DifficultyDialog(
            onDismiss = { showDifficultyDialog = false },
            onConfirm = { difficulty ->
                // This is the final step. Process the data and navigate.
                Toast.makeText(context, "Campaign '${selectedCampaign?.name}' created with $playerCount players and $difficulty difficulty.", Toast.LENGTH_LONG).show()
                showDifficultyDialog = false

                // Navigate to Campaign Selector
                context.startActivity(Intent(context, SelectCampaign::class.java))
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select campaign to start playing") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2E6BAB), // Slightly darker blue
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF8645BF) // Light Purple background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(availableCampaigns, key = { it.id }) { campaign ->
                BlueprintRow(
                    blueprint = campaign,
                    onClick = {
                        selectedCampaign = campaign
                        showPlayerCountDialog = true
                    }
                )
            }
        }
    }
}

// Composable for a single row in the list
@Composable
fun BlueprintRow(blueprint: CampaignBlueprint, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            blueprint.imageRes?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = blueprint.name,
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
            Text(
                text = blueprint.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// --- Dialog Composables ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerCountDialog(onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedPlayerCount by remember { mutableStateOf("1") }
    val playerCounts = listOf("1", "2", "3", "4")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Player Count") },
        text = {
            Column {
                Text("How many players will there be?")
                Spacer(modifier = Modifier.height(16.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = selectedPlayerCount,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        playerCounts.forEach { count ->
                            DropdownMenuItem(
                                text = { Text(count) },
                                onClick = {
                                    selectedPlayerCount = count
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedPlayerCount.toInt()) }) {
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
fun HeroSelectionDialog(playerCount: Int, onDismiss: () -> Unit, onConfirm: (List<String>) -> Unit) {
    var heroSelections by remember { mutableStateOf(List(playerCount) { "" }) }
    val allHeroes = listOf("Captain America", "Black Widow", "Iron Man", "Spider-Man", "Captain Marvel", "Hawkeye")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Heroes") },
        text = {
            Column {
                (1..playerCount).forEach { playerIndex ->
                    val availableHeroes = allHeroes - heroSelections.toSet()
                    HeroDropdown(
                        playerNumber = playerIndex,
                        availableHeroes = availableHeroes,
                        selectedHero = heroSelections[playerIndex - 1],
                        onHeroSelected = { newHero ->
                            val updatedSelections = heroSelections.toMutableList()
                            updatedSelections[playerIndex - 1] = newHero
                            heroSelections = updatedSelections
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
        confirmButton = {
            TextButton(
                // A hero must be selected for each player to continue
                enabled = heroSelections.all { it.isNotEmpty() },
                onClick = { onConfirm(heroSelections) }
            ) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeroDropdown(
    playerNumber: Int,
    availableHeroes: List<String>,
    selectedHero: String,
    onHeroSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Text("Player $playerNumber's hero:", style = MaterialTheme.typography.bodyMedium)
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedHero.ifEmpty { "Select a hero" },
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableHeroes.forEach { hero ->
                DropdownMenuItem(
                    text = { Text(hero) },
                    onClick = {
                        onHeroSelected(hero)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DifficultyDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Difficulty") },
        text = { Text("Choose the campaign difficulty.") },
        confirmButton = {
            Row {
                TextButton(onClick = { onConfirm("Standard") }) {
                    Text("Standard")
                }
                TextButton(onClick = { onConfirm("Expert") }) {
                    Text("Expert")
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

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun NewCampaignCreationScreenPreview() {
    MarvelChampionsCampaignCompanionTheme {
        NewCampaignCreationScreen()
    }
}