package com.example.marvelchampionscampaigncompanion

import Classes.InstanceHero
import Classes.InstanceMarvelQuestion
import Classes.InstanceScenario
import Classes.QuestionType
import Classes.Upgrade
import DataBaseManager
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.forEach
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.marvelchampionscampaigncompanion.ui.theme.MarvelChampionsCampaignCompanionTheme
import java.time.LocalDateTime
import androidx.core.content.IntentCompat

@RequiresApi(Build.VERSION_CODES.O)
class ScenarioScreen : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Retrieve the Parcelable InstanceScenario object from the Intent
        //val scenario = IntentCompat.getParcelableExtra(intent, "SCENARIO_OBJECT", InstanceScenario::class.java)
        val scenarioId = intent.getIntExtra("SCENARIO_ID", -1)
        val campaignId = intent.getIntExtra("CAMPAIGN_ID", -1)
        val campaignDifficulty = intent.getStringExtra("CAMPAIGN_DIFFICULTY")

        setContent {

            MarvelChampionsCampaignCompanionTheme {
                if (scenarioId != -1 && campaignId != -1) {

                    ScenarioRoute(scenarioId = scenarioId, campaignId = campaignId,
                        difficulty = campaignDifficulty,
                        onNavigateBack = { finish() })
                } else {
                    // Fallback screen if the scenario object fails to pass
                    ErrorScreen(onNavigateBack = { finish() })
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScenarioRoute(scenarioId: Int, campaignId: Int, difficulty: String?, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val dbManager = DataBaseManager(context)

    var scenario by remember{mutableStateOf<InstanceScenario?>(null)}
    var heroes by remember { mutableStateOf<List<InstanceHero>>(emptyList()) }
    var questionsToAsk by remember { mutableStateOf<List<InstanceMarvelQuestion>>(emptyList()) }

    //State for previous scenario data
    var previousScenariosLog by remember { mutableStateOf<List<InstanceScenario>>(emptyList()) }

    var showUpgradeDialog by remember { mutableStateOf(false) }

    var currentHeroUpgradeIndex by remember { mutableIntStateOf(0) }
// This holds the list of upgrades that can be chosen.
    var availableUpgrades by remember { mutableStateOf<List<Upgrade>>(emptyList()) }

    // Use LaunchedEffect to run the database queries safely.
    // It will run once when the screen is first displayed
    LaunchedEffect(key1 = scenarioId, key2=campaignId) {
        //fetch new data

        scenario = dbManager.getScenarioById(scenarioId)
        heroes = dbManager.getHeroesByCampaignId(campaignId)
        questionsToAsk = dbManager.readQuestionsForScenario(scenarioId)


        //fetch data from prev scenario
        scenario?.let{ current ->
            val allScenarios = dbManager.readScenariosForCampaign(campaignId)
            val currentIndex = allScenarios.indexOfFirst { it.id == current.id }

            if (currentIndex > 0){

                previousScenariosLog = allScenarios.subList(0, currentIndex)
            }
        }
    }
    // 2. The question currently being shown in the dialog. Can be null.
    var currentQuestion by remember { mutableStateOf<InstanceMarvelQuestion?>(null) }
    
    var showUpdateLifeDialog by remember { mutableStateOf(false) }
    var updatedLifeTotals by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }



    scenario?.let{currentScenario ->

        fun completeTheScenario() {
                // 1. Get all scenarios for the campaign to find the next one
                val allScenarios = dbManager.readScenariosForCampaign(currentScenario.instanceCampaignId)
                val currentIndex = allScenarios.indexOfFirst { it.id == currentScenario.id }

                // 2. Update the CURRENT scenario to "completed"
                dbManager.updateScenarioStatus(currentScenario.id, "completed")

                // 3. Find and update the NEXT scenario to "current"
                if (currentIndex != -1 && currentIndex < allScenarios.size - 1) {
                    val nextScenario = allScenarios[currentIndex + 1]
                    dbManager.updateScenarioStatus(nextScenario.id, "current")
                }

                // 4. Set the result to RESULT_OK and navigate back
                (context as? Activity)?.setResult(Activity.RESULT_OK)
                onNavigateBack()
            }
        fun showQuestionsOrComplete(){
            currentQuestion = questionsToAsk.firstOrNull()
            if (currentQuestion == null){
                completeTheScenario()
            }
        }

        fun startUpgradeSelectionProcess(){
            val presetCampaignId = dbManager.getPresetCampaignIdByInstanceId(campaignId)
            availableUpgrades = dbManager.getAvailableUpgrades(campaignId, presetCampaignId, currentScenario.presetScenarioId)
            showUpgradeDialog = true
        }

        fun continueCompletionProcessAfterLifeUpdate() {
            // Check if there are any questions to ask for the current scenario
            if (questionsToAsk.isNotEmpty()) {
                // If there are questions, start the question sequence as before.
                // The last question's onAnswer will trigger the upgrades.
                currentQuestion = questionsToAsk.first()
            } else {
                // --- THIS IS THE CRITICAL FIX ---
                // If there are NO questions, skip directly to the upgrade process.
                Log.d("COMPLETION_FLOW", "No questions to ask. Starting upgrade process directly.")
                startUpgradeSelectionProcess()
            }
        }

        fun startCompletionProcess() {
            // This function becomes the single entry point after clicking "Complete Level"

            val startWithLifeUpdate = difficulty == "Expert" && !scenario!!.status.equals("completed", true)

            if (startWithLifeUpdate) {
                // If we need to update life first, the confirmation button of THAT dialog
                // will be responsible for continuing the process.
                showUpdateLifeDialog = true
            } else {
                // If not updating life, we check for questions directly.
                continueCompletionProcessAfterLifeUpdate()
            }
        }

        fun goToNextheroOrContinue(){
            val presetCampaignId = dbManager.getPresetCampaignIdByInstanceId(campaignId)
            val nextIndex = currentHeroUpgradeIndex +1
            if(nextIndex < heroes.size){
                currentHeroUpgradeIndex = nextIndex
                availableUpgrades = dbManager.getAvailableUpgrades(campaignId, presetCampaignId, currentScenario.presetScenarioId)
                showUpgradeDialog = true

            }else{
                showUpgradeDialog = false
                completeTheScenario()
            }
        }

        if (showUpdateLifeDialog){
            UpdateHeroLifeDialog(
                heroes = heroes,
                lifeTotals = updatedLifeTotals,
                onLifeChange = { heroId, newLife ->
                    // Only allow numeric input
                    if (newLife.all { it.isDigit() }) {
                        updatedLifeTotals = updatedLifeTotals + (heroId to newLife)
                    }
                },
                onConfirm = {
                    // Save the new life totals to the database
                    for (hero in heroes) {
                        val newLifeStr = updatedLifeTotals[hero.id]
                        if (newLifeStr != null) {
                            val newLife = newLifeStr.toIntOrNull() ?: hero.currentLife
                            val updatedHero = hero.copy(currentLife = newLife)
                            dbManager.updateHero(updatedHero)
                        }
                    }
                    // Close this dialog and proceed
                    showUpdateLifeDialog = false
                    continueCompletionProcessAfterLifeUpdate()
                },
                onDismiss = { showUpdateLifeDialog = false }
            )
        }


            currentQuestion?.let { question ->
                QuestionDialog(
                    question = question,
                    onDismiss = { currentQuestion = null },
                    onAnswer = { response ->

                        val answeredQuestion = question.copy(answer = response)
                        dbManager.updateQuestion(answeredQuestion)
                        questionsToAsk = questionsToAsk.filter { it.id != question.id }

                        val nextQuestion = questionsToAsk.firstOrNull()
                        if(nextQuestion != null){
                            currentQuestion = nextQuestion
                        } else{
                            currentQuestion = null
                            startUpgradeSelectionProcess()
                        }
                    }
                )
            }

        if (showUpgradeDialog && heroes.isNotEmpty()) {
            val currentHeroForUpgrade = heroes[currentHeroUpgradeIndex]
            UpgradeSelectionDialog(
                hero = currentHeroForUpgrade,
                availableUpgrades = availableUpgrades,
                onConfirm = { selectedUpgrades ->
                    // Assign the upgrade to the hero in the DB
                    selectedUpgrades.forEach { upgrade ->
                        dbManager.assignUpgradeToHero(
                            campaignId = currentScenario.instanceCampaignId,
                            presetUpgradeId = upgrade.id,
                            heroId = currentHeroForUpgrade.id
                        )
                    }
                    // Move to the next hero
                    goToNextheroOrContinue()
                },
                onDismiss = {
                    // If they dismiss, we treat it as skipping for everyone and finish
                    showUpgradeDialog = false
                    completeTheScenario()
                }
            )
        }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(currentScenario.name) },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                            }
                        }
                    )
                }
            ) { innerPadding ->
                ScenarioScreenContent(
                    modifier = Modifier.padding(innerPadding),
                    scenario = currentScenario, // Pass the loaded, non-null scenario
                    heroes = heroes,
                    previousScenariosLog = previousScenariosLog,
                    onCompleteClick = {startCompletionProcess()}
                )
            }
        } ?: run {
            // Show a loading indicator while the scenario object is null (being fetched)
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }

@Composable
fun UpdateHeroLifeDialog(
    heroes: List<InstanceHero>,
    lifeTotals: Map<Int, String>,
    onLifeChange: (heroId: Int, newLife: String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Hero Life (Expert)") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(heroes, key = { index, hero -> hero.id }) { index, hero ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = hero.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = lifeTotals[hero.id] ?: "",
                            onValueChange = { newLife -> onLifeChange(hero.id, newLife) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(80.dp),
                            singleLine = true
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Confirm & Continue")
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
fun ScenarioScreenContent(
    modifier: Modifier = Modifier,
    scenario: InstanceScenario,
    heroes: List<InstanceHero>,
    previousScenariosLog: List<InstanceScenario>,
    onCompleteClick: () -> Unit
) {

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 1. Heroes Section
            HeroInfoGrid(heroes = heroes)

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 24.dp),
                thickness = 8.dp,
                color = Color.Black
            )

            // 2. Previous Scenarios Information Section
            Text("Campaign Log", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    Text(
                        text = scenario.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                    itemsIndexed(previousScenariosLog, key = {index, item -> item.id}) {index, pastScenario ->
                        Text(
                            //TODO: this needs to be changed to a more natural language.
                            text = "Log from ${pastScenario.name}:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))

                        val answeredQuestions = pastScenario.questionList.filter {
                            !it.answer.isNullOrBlank()
                        }
                        if (answeredQuestions.isEmpty()) {

                                Text(
                                    text = "No log entries were recorded for this scenario.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                        } else {
                            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                                answeredQuestions.forEach { question ->
                                    Text(
                                        text = "Q: ${question.text}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "A: ${question.answer}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                            }
                        }
                    }

            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
        }


        // 3. "Complete Level" Button at the bottom
        if(scenario.status.equals("current", ignoreCase = true)) {
            Button(
                onClick = onCompleteClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Complete Level!")
            }
        }
    }
}

@Composable
fun HeroInfoGrid(heroes: List<InstanceHero>) {
    // Create pairs of heroes to display two per row
    if (heroes.isEmpty()) {
        Text(
            "Loading hero data",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    } else {
        val heroPairs = heroes.chunked(2)

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            heroPairs.forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    pair.forEach { hero ->
                        HeroStatusCard(
                            hero = hero,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // If there's an odd number of heroes, add a spacer to fill the row
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionDialog(
    question: InstanceMarvelQuestion,
    onAnswer: (response: String) -> Unit,
    onDismiss: () -> Unit
)
{
    var questionNumber = 1;
 var textResponse by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {Text("Question $questionNumber") },
        text = {
            Column {
                Text(question.text, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(16.dp))
                when(question.questionType){
                QuestionType.YES_NO ->{

                }
                    QuestionType.NUMBER_INPUT ->{
                        OutlinedTextField(
                            value = textResponse,
                            onValueChange = { textResponse = it.filter { char -> char.isDigit() } },
                            label = { Text("Enter a number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    QuestionType.TEXT_INPUT -> {
                        OutlinedTextField(
                            value = textResponse,
                            onValueChange = { textResponse = it },
                            label = { Text("Enter your response") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val response = when (question.questionType) {
                        QuestionType.YES_NO -> "Yes"
                        else -> textResponse
                    }
                    onAnswer(response)
                },
                // Disable the button for text/number inputs if they are empty
                enabled = !(question.questionType != QuestionType.YES_NO && textResponse.isBlank())
            ) {
                Text(if (question.questionType == QuestionType.YES_NO) "Yes" else "Confirm")
            }
        },
        dismissButton = {
            if (question.questionType == QuestionType.YES_NO) {
                TextButton(onClick = { onAnswer("No") }) {
                    Text("No")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )

}


//idea for the future: keep the database standard, but have rules for each campaign/scenario to set this screen differently
@Composable
fun HeroStatusCard(hero: InstanceHero, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                hero.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text("Current life: ${hero.currentLife}", style = MaterialTheme.typography.bodyMedium)

            // Placeholder for future upgrade info
            if (hero.upgrades.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Upgrades:",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    hero.upgrades.forEach { upgrade ->
                        Text(
                            text = upgrade.name,
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UpgradeSelectionDialog(
    hero: InstanceHero,
    availableUpgrades: List<Upgrade>,
    onConfirm:(selectedUpgrades: List<Upgrade>) -> Unit,
    onDismiss: () -> Unit
){
    var selectedUpgradeIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Upgrade for ${hero.name}") },
        text = {
            if (availableUpgrades.isEmpty()) {
                Text("There are no available upgrades to choose from at this time.")
            } else {
                LazyColumn {
                    itemsIndexed(availableUpgrades, key = {index, upgrade -> upgrade.id }) {index, upgrade ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Toggle selection: add if not present, remove if present
                                    selectedUpgradeIds = if (upgrade.id in selectedUpgradeIds) {
                                        selectedUpgradeIds - upgrade.id
                                    } else {
                                        selectedUpgradeIds + upgrade.id
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = upgrade.id in selectedUpgradeIds,
                                onCheckedChange = { isChecked ->
                                    // Toggle selection logic mirrors the clickable modifier
                                    selectedUpgradeIds = if (isChecked) {
                                        selectedUpgradeIds + upgrade.id
                                    } else {
                                        selectedUpgradeIds - upgrade.id
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(upgrade.name, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Find the full Upgrade objects that match the selected IDs
                    val selectedUpgrades = availableUpgrades.filter { it.id in selectedUpgradeIds }
                    onConfirm(selectedUpgrades)
                }
            ) {

                Text("Continue")
            }
        },


    )
}

// A simple fallback screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorScreen(onNavigateBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("Error") }, navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
        })
    }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding), contentAlignment = Alignment.Center
        ) {
            Text("Could not load scenario data.", textAlign = TextAlign.Center)
        }
    }
}


// --- PREVIEW ---
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ScenarioScreenPreview() {
    val dummyScenario = InstanceScenario(
        id = 1,
        instanceCampaignId = 1,
        presetScenarioId = 1,
        status = "current",
        startDate = LocalDateTime.now(),
        endDate = LocalDateTime.now(),
        name = "The Wrecking Crew",
        description = "A dummy description.",
        villainName = "Wrecker",
        questionList = emptyList()
    )
    val dummyHeroes = listOf(
        InstanceHero(1, 1, 1, 0, "Spider-Man", 10, emptyList(), LocalDateTime.now()),
        InstanceHero(2, 2, 1, 0, "Captain Marvel", 12, emptyList(), LocalDateTime.now()),
        InstanceHero(3, 3, 1, 0, "She-Hulk", 15, emptyList(), LocalDateTime.now())
    )
    MarvelChampionsCampaignCompanionTheme {
        ScenarioScreenContent(
            scenario = dummyScenario,
            heroes = dummyHeroes,
            previousScenariosLog = emptyList(),
            onCompleteClick = {}
        )
    }
}
