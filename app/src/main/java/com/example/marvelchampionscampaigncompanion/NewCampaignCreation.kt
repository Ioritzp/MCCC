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
import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.ColorFilter

// Data class to represent the structure of a campaign shown in the UI
data class CampaignBlueprint(
    val id: String,
    val name: String,
    val userId: Int,
    @DrawableRes val imageRes: Int? // Use a drawable resource ID for the image
)

class NewCampaignCreation : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val userId = intent.getIntExtra("USER_ID", -1)
        setContent {
            MarvelChampionsCampaignCompanionTheme {
                NewCampaignCreationRoute(userId = userId)
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
    userId:Int,
    modifier: Modifier = Modifier) {
    val context = LocalContext.current
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
                    userId,
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
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Image(
                painter = painterResource(id = R.drawable.soft_background),
                contentDescription = "Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(
                    Color.Black.copy(alpha = 0.2f),
                    blendMode = androidx.compose.ui.graphics.BlendMode.Darken
                )
            )
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.main_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .fillMaxWidth(0.8f) // Takes 80% of the screen width
                        .padding(top = 16.dp)
                )

                Text(

                    text = "Create Campaign",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
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
    }
}

/**
 * A composable representing a single large, interactable button for a campaign.
 */
@Composable
fun CampaignButton(blueprint: CampaignBlueprint, onClick: () -> Unit) {
    val color1 = when (blueprint.name) {
        "Rise of Red Skull" -> Color(0xFF962626)
        "Galaxy's Most Wanted" -> Color(0xFF211CAD)
        "The Mad Titan's Shadow" -> Color(0xFFC2973C)
        "Sinister Motives" -> Color(0xFF790D91)
        "Mutant Genesis" -> Color(0xFFB0A5B0)
        "NeXt Evolution" -> Color(0xFFD97311)
        "Age of Apocalypse" -> Color(0xFF449BBD)
        "Agents of S.H.I.E.L.D." -> Color(0xFF141313)

        else -> MaterialTheme.colorScheme.surfaceVariant // Default fallback colors
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp)
            .clickable(onClick = onClick), // Makes the entire card clickable
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = color1),
        border = BorderStroke(2.dp, Color.Black)
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
                        .clip(CircleShape)
                        .border(2.dp, Color.White.copy(alpha = 0.7f), CircleShape)
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
fun NewCampaignCreationRoute(userId: Int) {
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
                "Sinister Motives" -> R.drawable.sinister_motives
                "Mutant Genesis" -> R.drawable.mutant_gen
                "NeXt Evolution" -> R.drawable.next_evo
                "Age of Apocalypse" -> R.drawable.age_apo
                "Agents of S.H.I.E.L.D." -> R.drawable.agents
                else -> null
            }
            CampaignBlueprint(
                id = campaign.id.toString(),
                name = campaign.name,
                userId = userId,
                imageRes = imageResource
            )
        }
    }
    NewCampaignCreationScreen(availableCampaigns = availableCampaigns,
        presetCampaignsInfo = campaignsFromDb,
        allPresetHeroes = allPresetHeroesFromDb,
        userId = userId)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerCountDialog(onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val playerCounts = listOf("1", "2", "3", "4")
    var selectedPlayerCount by remember { mutableStateOf(playerCounts.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("Select Player Count", color = Color.Black) },
        text = {
            Column {
                Text("Select number of players:", color = Color.Black)
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
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true),
                        colors = TextFieldDefaults.colors(
                            unfocusedTextColor = Color.Black,
                            focusedTextColor = Color.Black,
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        modifier = Modifier.background(Color.White),
                        onDismissRequest = { expanded = false }) {
                        playerCounts.forEach { count ->
                            DropdownMenuItem(
                                text = { Text(count) },
                                onClick = {
                                    selectedPlayerCount = count
                                    expanded = false
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = Color.Black
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedPlayerCount.toInt()) },
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFED1D24),
                    contentColor = Color.White
                ),

            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss,
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFED1D24),
                    contentColor = Color.White
                )) {
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
            tonalElevation = 8.dp,
            color = Color.White
        ){
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Hero for Player $playerNumber",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f, fill = false),
                    horizontalArrangement = Arrangement.spacedBy(16.dp), // Add some space between columns
                    verticalArrangement = Arrangement.spacedBy(12.dp) // Add some space between rows
                ) {
                    if (heroList.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) { // Make the "empty" text span both columns
                            Text(
                                "No more heroes available.",
                                modifier = Modifier.padding(vertical = 16.dp),
                                color = Color.Black
                            )
                        }
                    } else {
                        items(heroList, key = { it.id }) { hero ->
                            // The HeroRow composable is reused without any changes needed
                            HeroRow(hero = hero, onHeroSelected = {
                                onConfirm(hero)
                            })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFED1D24),
                            contentColor = Color.Black
                        )

                ) {
                    Text("CANCEL")
                }
            }
        }
    }
}

@SuppressLint("DiscouragedApi", "LocalContextResourcesRead")
@Composable
fun HeroRow(hero: Hero, onHeroSelected: () -> Unit) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onHeroSelected() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val context = LocalContext.current
        val resourceName = "h${hero.id}"
        val resourceId = remember(resourceName) {
            context.resources.getIdentifier(
                resourceName,
                "drawable",
                context.packageName
            )
        }

        Image(
            painter = if (resourceId != 0) {
                painterResource(id = resourceId)
            } else {
                painterResource(id = R.drawable.ic_launcher_background)
            },
            contentDescription = hero.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(2.dp))
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = hero.name,
            style = MaterialTheme.typography.titleMedium, // A slightly larger style
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Small divider line between heroes
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(0.8f), // Make the divider not span the full width
            thickness = 1.dp,
            color = Color.Gray.copy(alpha = 0.5f)
        )
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
        containerColor = Color.White,
        title = { Text("Select Difficulty", color = Color.Black) },
        text = {
            Column {
                Text("Choose the campaign difficulty.", color = Color.Black)
                Spacer(Modifier.height(16.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    modifier = Modifier.background(color = Color.White),
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = selectedDifficulty,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true).background(color = Color.White),
                        colors = TextFieldDefaults.colors(
                            unfocusedTextColor = Color.Black,
                            focusedTextColor = Color.Black,
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White

                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        modifier = Modifier.background(Color.White),
                        onDismissRequest = { expanded = false }) {
                        difficulties.forEach { difficulty ->
                            DropdownMenuItem(
                                text = { Text(difficulty) },
                                onClick = {
                                    selectedDifficulty = difficulty
                                    expanded = false
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = Color.Black
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFED1D24),
                        contentColor = Color.White
                    )
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = { onConfirm(selectedDifficulty) },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFED1D24),
                        contentColor = Color.White
                    )
                ) {
                    Text("Next")
                }
            }
        },
        dismissButton = {}
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun createInstancedCampaign(
    activity: Activity?,
    presetCampaignId: Int,
    playerCount: Int,
    difficulty: String,
    heroes: List<Hero>,
    userId: Int,
    selectedCampaignInfo: Campaign?
) {
    val context = activity?.applicationContext
    if (selectedCampaignInfo == null) {
        Toast.makeText(context, "error: no campaign info provided", Toast.LENGTH_SHORT).show()
        return
    }
    val dbManager = DataBaseManager(context)


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
        endDate = null,
        difficulty = difficulty
    )

    // Add the campaign to the DB to get its REAL ID
    val finalCampaignId = dbManager.addCampaign(newInstancedCampaign).toInt()

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
            endDate = null,
            status = scenarioStatus
        )
        // Add the scenario to the DB to get its real ID

        val finalScenarioId = dbManager.addScenario(instancedScenario).toInt()
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
    }

    // --- Finalize and finish the activity ---
    activity?.setResult(Activity.RESULT_OK)
    activity?.finish()
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NewCampaignCreationScreenPreview() {
    MarvelChampionsCampaignCompanionTheme {
        // Dummy data for the CampaignBlueprint list (for the UI cards)
        val sampleCampaignBlueprints = listOf(
            CampaignBlueprint("1", "The Rise of Red Skull",1, R.drawable.red_skull),
            CampaignBlueprint("2", "Galaxy's Most Wanted",1, R.drawable.most_wanted),
            CampaignBlueprint("3", "The Mad Titan's Shadow",1, null)
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
                3,
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
            userId = 1,
            allPresetHeroes = sampleHeroes // <-- PASS THE FAKE HEROES
        )
    }
}