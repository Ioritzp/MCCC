package com.example.marvelchampionscampaigncompanion

import Classes.Campaign
import Classes.Hero
import Classes.InstanceCampaign
import Classes.InstanceHero
import Classes.InstanceMarvelQuestion
import Classes.InstanceScenario
import Classes.MarvelQuestion
import Classes.QuestionType
import Classes.Scenario
import DataBaseManager
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import com.example.marvelchampionscampaigncompanion.ui.theme.MarvelChampionsCampaignCompanionTheme
import java.time.LocalDateTime
import kotlin.Int

// Data class to represent the structure of a campaign shown in the UI
data class CampaignBlueprint(
    val id: String,
    val name: String,
    @DrawableRes val imageRes: Int? // Use a drawable resource ID for the image
)

class NewCampaignCreation : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MarvelChampionsCampaignCompanionTheme {
                NewCampaignCreationRoute()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewCampaignCreationScreen(
    availableCampaigns:List<CampaignBlueprint>,
    presetCampaignsInfo: List<Campaign>,
    allPresetHeroes: List<Hero>,
    modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val dbManager = remember { DataBaseManager(context) }
    val activity = LocalActivity.current


    // State management for dialogs
    var showPlayerCountDialog by remember { mutableStateOf(false) }
    var showDifficultyDialog by remember { mutableStateOf(false) }
    var showHeroesDialog by remember { mutableStateOf(false) }

    // State for collected data
    var selectedCampaignId by remember { mutableStateOf<Int?>(null) }
    var numberOfPlayers by remember { mutableIntStateOf(0) }
    var selectedDifficulty by remember { mutableStateOf("") }
    var selectedHeroes by remember { mutableStateOf<List<Hero>>(emptyList()) }


    // Dialog Chain
    if (showPlayerCountDialog) {
        PlayerCountDialog(
            onDismiss = { showPlayerCountDialog = false },
            onConfirm = { count ->
                numberOfPlayers = count
                showPlayerCountDialog = false
                showHeroesDialog = true
            }

        )
    }

    if (showHeroesDialog) {
        val availableHeroes = allPresetHeroes.filter { it !in selectedHeroes }
        SelectHeroesDialog(
            onDismiss = { showHeroesDialog = false },
            heroList = availableHeroes,
            playerNumber = selectedHeroes.size + 1,
            onConfirm = { selectedHero ->
                selectedHeroes = selectedHeroes + selectedHero
                if(selectedHeroes.size >= numberOfPlayers){
                    showHeroesDialog = false
                    showDifficultyDialog = true
                }

            }

        )
    }

    if(showDifficultyDialog){
        DifficultyDialog(
        onDismiss = {showDifficultyDialog = false},
        onConfirm = { difficulty ->
            selectedDifficulty = difficulty
            showDifficultyDialog = false
            val selectedCampaignInfo: Campaign? =
                presetCampaignsInfo.find { it.id == selectedCampaignId }

            if (selectedCampaignInfo != null) {


                createInstancedCampaign(
                    activity = activity,
                    selectedCampaignId!!,
                    numberOfPlayers,
                    difficulty,
                    selectedHeroes,
                    selectedCampaignInfo
                )
            } else{
                Toast.makeText(context, "error: no campaign with that details", Toast.LENGTH_SHORT).show()
            }

        }

        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("select campaign")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFF9C4), // Yellow background for the title area
                    titleContentColor = Color.Black
                )
            )
        },
        containerColor = Color(0xFFFFE0B2) // Light Orange background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(
                items = availableCampaigns,
                key = { campaign -> campaign.id }
            ) { campaign ->
                CampaignButton(
                    blueprint = campaign,
                    onClick = {
                        // Start the dialog chain by setting the state
                        selectedCampaignId = campaign.id.toInt()
                        showPlayerCountDialog = true
                    }
                )
            }
        }
    }
}

/**
 * A composable representing a single large, interactable button for a campaign.
 */
@Composable
fun CampaignButton(blueprint: CampaignBlueprint, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp)
            .clickable(onClick = onClick), // Makes the entire card clickable
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            blueprint.imageRes?.let { imageResource ->
                Image(
                    painter = painterResource(id = imageResource),
                    contentDescription = blueprint.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
            } ?: Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.shapes.medium
                    )
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NewCampaignCreationRoute() {
    val context = LocalContext.current
    val dbManager = remember { DataBaseManager(context) }
    val campaignsFromDb = remember { dbManager.getAllPresetCampaigns() }
    val allPresetHeroesFromDb = remember { dbManager.getAllPresetHeroes() }

    val availableCampaigns = remember(campaignsFromDb) {
        campaignsFromDb.map { campaign ->
            val imageResource = when (campaign.name) {
                // Ensure your drawable names are correct (e.g., lowercase with underscores)
                "Rise of Red Skull" -> R.drawable.red_skull
                "Galaxy's Most Wanted" -> R.drawable.most_wanted
                "The Mad Titan's Shadow" -> R.drawable.mad_titan
                else -> null
            }
            CampaignBlueprint(
                id = campaign.id.toString(),
                name = campaign.name,
                imageRes = imageResource
            )
        }
    }
    NewCampaignCreationScreen(availableCampaigns = availableCampaigns,
        presetCampaignsInfo = campaignsFromDb,
        allPresetHeroes = allPresetHeroesFromDb)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerCountDialog(onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val playerCounts = listOf("1", "2", "3", "4")
    var selectedPlayerCount by remember { mutableStateOf(playerCounts.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Player Count") },
        text = {
            Column {
                Text("How many players will there be?")
                Spacer(Modifier.height(16.dp))
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
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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
            Button(onClick = { onConfirm(selectedPlayerCount.toInt()) }) {
                Text("Confirm")
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
fun SelectHeroesDialog(
    onDismiss: () -> Unit,
    onConfirm: (Hero) -> Unit, // Callback to send the selected hero back
    heroList: List<Hero>,
    playerNumber: Int // To show which player is currently selecting
){
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp
        ){
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Hero for Player $playerNumber",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn (modifier = Modifier.weight(1f, fill = false)){
                    if(heroList.isEmpty()){
                        item{
                            Text(
                                "No more heroes available.",
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    } else {
                        items(heroList, key = { it.id }){ hero ->
                            HeroRow(hero = hero, onHeroSelected = {
                                onConfirm(hero)
                            })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("CANCEL")
                }
            }
        }
    }
}

@Composable
fun HeroRow(hero: Hero, onHeroSelected: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onHeroSelected() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = hero.name, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DifficultyDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val difficulties = listOf("Standard", "Expert")
    var selectedDifficulty by remember { mutableStateOf(difficulties.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Difficulty") },
        text = {
            Column {
                Text("Choose the campaign difficulty.")
                Spacer(Modifier.height(16.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = selectedDifficulty,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        difficulties.forEach { difficulty ->
                            DropdownMenuItem(
                                text = { Text(difficulty) },
                                onClick = {
                                    selectedDifficulty = difficulty
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedDifficulty) }) {
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

@RequiresApi(Build.VERSION_CODES.O)
fun createInstancedCampaign(
    activity: Activity?,
    presetCampaignId: Int,
    playerCount: Int,
    difficulty: String,
    heroes: List<Hero>,
    selectedCampaignInfo: Campaign?
) {
    val context = activity?.applicationContext
    if (selectedCampaignInfo == null) {
        Toast.makeText(context, "error: no campaign info provided", Toast.LENGTH_SHORT).show()
        return
    }
    val dbManager = DataBaseManager(context)
    val userId = 1 // Placeholder

    // --- STEP 1: Create the Campaign shell first to get its real ID ---
    val newInstancedCampaign = InstanceCampaign(
        id = 0, // Will get from DB
        presetCampaignId = presetCampaignId,
        name = selectedCampaignInfo.name,
        description = selectedCampaignInfo.description,
        scenarioList = arrayListOf(), // Will be populated later
        userId = userId,
        userName = "", // Placeholder
        playerNum = playerCount,
        startDate = LocalDateTime.now(),
        endDate = LocalDateTime.now(),
        difficulty = difficulty
    )
    // Add the campaign to the DB to get its REAL ID
    val finalCampaignId = dbManager.addCampaign(newInstancedCampaign).toInt()
    Toast.makeText(context, "${newInstancedCampaign.name} has been created!", Toast.LENGTH_SHORT).show()
    Log.d("DEBUG_QUESTIONS_SCENARIO", "scenario list size: ${selectedCampaignInfo.scenarioList[0].questionList.size}")

    // --- STEP 2: Now create Scenarios and Questions using the final campaign ID ---
    for ((index, presetScenario) in selectedCampaignInfo.scenarioList.withIndex()) {
        val scenarioStatus = if (index == 0) "current" else "locked"

        // Create the scenario with the CORRECT campaign ID from the start
        val instancedScenario = InstanceScenario(
            id = 0, // Will get from DB
            instanceCampaignId = finalCampaignId, // USE THE REAL ID!
            presetScenarioId = presetScenario.id,
            name = presetScenario.name,
            villainName = presetScenario.villainName,
            description = presetScenario.description,
            questionList = arrayListOf(),
            startDate = LocalDateTime.now(),
            endDate = LocalDateTime.now(),
            status = scenarioStatus
        )
        // Add the scenario to the DB to get its real ID
        val finalScenarioId = dbManager.addScenario(instancedScenario).toInt()
        Log.d("DEBUG_QUESTIONLIST_ON_CAMPAIGN_CREATION", "question list: ${presetScenario.questionList.size}")
        Log.d("DEBUG_QUESTIONLIST_ON_CAMPAIGN_CREATION", "question list: ${presetScenario.questionList.size}")
        // Now create questions, linking them to the real scenario ID
        for (presetQuestion in presetScenario.questionList) {
            val instancedQuestion = InstanceMarvelQuestion(
                id = 0, // Will get from DB
                instanceScenarioId = finalScenarioId, // Use the final scenario ID
                presetQuestionId = presetQuestion.id,
                text = presetQuestion.text,
                answer = " ", // Default empty answer
                questionType = QuestionType.valueOf(presetQuestion.type)
            )
            dbManager.addQuestion(instancedQuestion)
        }
    }

    // --- STEP 3: Create the Heroes using the final campaign ID ---
    for (hero in heroes) {
        val instancedHero = InstanceHero(
            id = 0,
            presetHeroId = hero.id,
            instanceCampaignId = finalCampaignId, // Use the final campaign ID
            credits = 0,
            name = hero.name,
            currentLife = hero.initialLife,
            upgrades = emptyList(),
            modDate = LocalDateTime.now()
        )
        dbManager.addHero(instancedHero)
        Toast.makeText(context, "${instancedHero.name} has been added!", Toast.LENGTH_SHORT).show()
    }

    // --- Finalize and finish the activity ---
    Toast.makeText(context, "${newInstancedCampaign.name} campaign fully configured!", Toast.LENGTH_LONG).show()
    activity?.setResult(Activity.RESULT_OK)
    activity?.finish()
}



    //TODO: MAYBE UPGRADES TOO, BUT NOT YET




@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NewCampaignCreationScreenPreview() {
    MarvelChampionsCampaignCompanionTheme {
        // Dummy data for the CampaignBlueprint list (for the UI cards)
        val sampleCampaignBlueprints = listOf(
            CampaignBlueprint("1", "The Rise of Red Skull", R.drawable.red_skull),
            CampaignBlueprint("2", "Galaxy's Most Wanted", R.drawable.most_wanted),
            CampaignBlueprint("3", "The Mad Titan's Shadow", null)
        )

        // Dummy data for the Campaign list (the detailed info)
        val sampleCampaignsInfo = listOf(
            Campaign(
                1,
                "The Rise of Red Skull",
                "The Red Skull is rising...",
                listOf(Scenario(1, "dummy", "dummy", "dummy", listOf(MarvelQuestion(1, "q", "a", "type"))))
            ),
            Campaign(
                2,
                "Galaxy's Most Wanted",
                "The Guardians of the Galaxy are in trouble...",
                listOf(Scenario(2, "dummy2", "dummy2", "dummy2", listOf(MarvelQuestion(2, "q2", "a2", "type"))))
            ),
            Campaign(
                3, // <--- FIX: ADD THE COMMA HERE
                "The Mad Titan's Shadow",
                "Thanos is coming...",
                listOf(Scenario(3, "dummy3", "dummy3", "dummy3", listOf(MarvelQuestion(3, "q3", "a3", "type"))))
            )
        )

        val sampleHeroes = listOf(
            Hero(1, "Spider-Man", 10, 10),
            Hero(2, "Captain Marvel", 12, 12),
            Hero(3, "She-Hulk", 15, 15)
        )

        // Call the function with all required arguments
        NewCampaignCreationScreen(
            availableCampaigns = sampleCampaignBlueprints,
            presetCampaignsInfo = sampleCampaignsInfo,
            allPresetHeroes = sampleHeroes // <-- PASS THE FAKE HEROES
        )
    }
}