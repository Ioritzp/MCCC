package com.example.marvelchampionscampaigncompanion

import Classes.InstanceHero
import Classes.InstanceMarvelQuestion
import Classes.InstanceScenario
import Classes.InstanceUpgrade
import Classes.QuestionType
import Classes.Upgrade
import DataBaseManager
import Utils.SessionManager
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.marvelchampionscampaigncompanion.ui.theme.MarvelChampionsCampaignCompanionTheme
import java.time.LocalDateTime
import java.util.Locale
import kotlin.text.lowercase

@RequiresApi(Build.VERSION_CODES.O)
class ScenarioScreen : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val scenarioId = intent.getIntExtra("SCENARIO_ID", -1)
        val campaignId = intent.getIntExtra("CAMPAIGN_ID", -1)
        val isLastScenario = intent.getBooleanExtra("IS_LAST_SCENARIO", false)
        val campaignDifficulty = intent.getStringExtra("CAMPAIGN_DIFFICULTY")

        val sessionManager = SessionManager(this)
        val userId = sessionManager.getUserId()

        if(userId == null){
            Toast.makeText(this, "Session expired, please log in again.", Toast.LENGTH_LONG).show()
            val intent = Intent(this, MainActivity::class.java).apply{
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            }
            startActivity(intent)
            finish()
            return
        }


        setContent {
            MarvelChampionsCampaignCompanionTheme {
                if (scenarioId != -1 && campaignId != -1) {
                    ScenarioRoute(
                        scenarioId = scenarioId,
                        campaignId = campaignId,
                        isLastScenario = isLastScenario,
                        difficulty = campaignDifficulty,
                        userId = userId
                    )
                } else {
                    // A fallback screen in case the IDs are not passed correctly
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: Could not load scenario data.")
                    }
                }
            }
        }
    }
}

// Data class to hold all data required by the screen
private data class ScenarioScreenData(
    val scenario: InstanceScenario,
    val heroes: List<InstanceHero>,
    val campaignLog: List<InstanceScenario>
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScenarioRoute(scenarioId: Int, campaignId: Int, isLastScenario: Boolean, difficulty: String?, userId: Int) {
    val context = LocalContext.current
    val dbManager = remember { DataBaseManager(context) }

    // produceState is a reliable way to load async data for a composable.
    // It will re-load if the keys (scenarioId, campaignId) change.
    val screenDataState by produceState<ScenarioScreenData?>(initialValue = null, scenarioId, campaignId) {
        val loadedScenario = dbManager.getScenarioById(scenarioId)
        if (loadedScenario != null) {
            val allScenariosInCampaign = dbManager.readScenariosForCampaign(campaignId)
            val currentIndex = allScenariosInCampaign.indexOfFirst { it.id == loadedScenario.id }
            val heroesFromDb = dbManager.getHeroesByCampaignId(campaignId)
            val heroesWithUpgrades = heroesFromDb.map { hero ->
                val upgradesForHero = dbManager.getUpgradesForHero(hero.id)
                hero.copy(upgrades = upgradesForHero)
            }
            val pastScenarios = if (currentIndex > 0) allScenariosInCampaign.subList(0, currentIndex) else emptyList()
            val campaignLogWithQuestions = pastScenarios.map {scenario ->
                val questionsForScenario = dbManager.readQuestionsForScenario(scenario.id)
                scenario.copy(questionList = questionsForScenario.toMutableList())
            }
            value = ScenarioScreenData(
                scenario = loadedScenario,
                heroes = heroesWithUpgrades,
                campaignLog = campaignLogWithQuestions
            )
        }
        // If scenario fails to load, value remains null
    }
    var showUpdateLifeDialog by remember { mutableStateOf(false) }
    var updatedLifeTotals by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }


    var questionsToAsk by remember { mutableStateOf<List<InstanceMarvelQuestion>>(emptyList()) }
    var currentQuestion by remember { mutableStateOf<InstanceMarvelQuestion?>(null) }


    var availableUpgrades by remember { mutableStateOf<List<Upgrade>>(emptyList()) }
    var showUpgradeDialog by remember { mutableStateOf(false) }
    var currentHeroUpgradeIndex by remember { mutableIntStateOf(0) }

    var isCompletionFinished by remember { mutableStateOf(false) }

    // This will navigate back when the flow is totally finished.
    LaunchedEffect(isCompletionFinished) {
        if (isCompletionFinished) {
            val activity = context as? Activity
            // Let the previous screen know we succeeded.
            activity?.setResult(Activity.RESULT_OK)
            // Finish this activity.
            if(isLastScenario){
                val intent = Intent(context, CampaignSelector::class.java).apply{
                    //This flag clears the nav stack, so the user cant go back to a completed campaign
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("USER_ID", userId)
                }
                context.startActivity(intent)
            } else {
                activity?.finish()
            }
        }
    }

    val currentData = screenDataState
    Log.d("CURRENTDATA_DEBUG",currentData.toString())
    if (currentData == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        fun completeScenarioFlow(isEditing: Boolean) {
            if (!isEditing){
                dbManager.updateScenarioStatus(
                    currentData.scenario.id,
                    "completed",
                    setEndDate = true
                )
            val allScenarios = dbManager.readScenariosForCampaign(campaignId)
            val endDateCheck =
                allScenarios.find { scenario -> scenario.id == currentData.scenario.id }
            Log.d("DEBUG_ALL_SCENARIOS", "Actual scenario end date: ${endDateCheck?.endDate}")
            val currentIndex = allScenarios.indexOfFirst { it.id == currentData.scenario.id }
            Log.d("DEBUG_UPLOAD DATE", "islastscenario check: $isLastScenario}")
            if (!isLastScenario) {
                val nextScenario = allScenarios[currentIndex + 1]
                dbManager.updateScenarioStatus(nextScenario.id, "current", false)
            } else {
                val campaignToComplete = dbManager.getCampaignById(campaignId)
                Log.d("DEBUG_UPLOAD DATE", "campaingtocomplete ok: ${campaignToComplete?.name}")
                campaignToComplete?.let {
                    val finalCampaign = it.copy(endDate = LocalDateTime.now())
                    Log.d("DEBUG_UPLOAD DATE", "end date: finalCampaign: ${finalCampaign.endDate}")
                    dbManager.updateCampaign(finalCampaign)
                }
            }
        }
            isCompletionFinished = true
        }

        // Called after questions are answered (or if there are none).
        fun startUpgradeSelection(isEditing: Boolean) {
            if(isLastScenario){
                if(isEditing){
                    isCompletionFinished = true
                }else {
                    completeScenarioFlow(isEditing = false)
                }
                return
            }
            val presetCampaignId = dbManager.getPresetCampaignIdByInstanceId(campaignId)
            availableUpgrades =
                dbManager.getAvailableUpgrades(campaignId, presetCampaignId, currentData.scenario.presetScenarioId)

            // If there are no upgrades to select, just complete the flow.
            if (availableUpgrades.isEmpty()) {
                completeScenarioFlow(isEditing)
            } else {
                currentHeroUpgradeIndex = 0 // Start with the first hero
                showUpgradeDialog = true
            }
        }

        // Called after the life update dialog is confirmed.
        fun continueAfterLifeUpdate(isEditing: Boolean) {
            // Fetch preset questions for this scenario.
            val presetQuestions = dbManager.readQuestionsForScenario(currentData.scenario.id)
            //fetch existing questions and answers
            val existingAnswers = dbManager.getAnswersForInstanceScenario(currentData.scenario.id)
            val instanceQuestions = presetQuestions.map {presetQuestion ->
                val existingAnswer = existingAnswers.find { it.presetQuestionId == presetQuestion.id }
                InstanceMarvelQuestion(
                    id = existingAnswer?.id ?:0,
                    instanceScenarioId = currentData.scenario.id,
                    presetQuestionId = presetQuestion.id,
                    text = presetQuestion.text,
                    questionType = presetQuestion.questionType,
                    answer = existingAnswer?.answer ?:""
                )
            }
            questionsToAsk = instanceQuestions

            Log.d("EDITQUESTIONS_DEBUG", "questions edited:" + questionsToAsk)

            // 3. If there are questions, show the first one. Otherwise, start upgrade selection.
            if (questionsToAsk.isNotEmpty()) {
                currentQuestion = questionsToAsk.first()
            } else {
                startUpgradeSelection(isEditing)
            }
        }

        // This is the entry point, called when the main button is clicked.
        fun startCompletionProcess() {
            val isEditing = currentData.scenario.status.toLowerCase() == "completed"
            if(isLastScenario && isEditing){
                return
            }
            if(isEditing){

                for (hero in currentData.heroes){
                    dbManager.deleteUpgradesFromHeroByScenario(hero.id, currentData.scenario.id)
                }
            }
            if (difficulty == "Expert") {
                showUpdateLifeDialog = true
            } else {
                continueAfterLifeUpdate(isEditing) // Skip the life update step
            }
        }
        ScenarioScreenContent(
            scenario = currentData.scenario,
            heroes = currentData.heroes,
            campaignLog = currentData.campaignLog,
            onCompleteClick = {startCompletionProcess() },
            isCompleted = currentData.scenario.status.toLowerCase() == "completed",
            isLastScenario = isLastScenario

        )


        // --- DIALOGS ---

        // 1. Update Hero Life Dialog
        if (showUpdateLifeDialog) {
            UpdateHeroLifeDialog(
                heroes = currentData.heroes,
                lifeTotals = updatedLifeTotals,
                onLifeChange = { heroId, newLife ->
                    if (newLife.all { it.isDigit() }) {
                        updatedLifeTotals = updatedLifeTotals + (heroId to newLife)
                    }
                },
                onConfirm = {
                    val isEditing = currentData.scenario.status.toLowerCase() == "completed"
                    // Save new life totals to the database
                    currentData.heroes.forEach { hero ->
                        updatedLifeTotals[hero.id]?.toIntOrNull()?.let { newLife ->
                            dbManager.updateHero(hero.copy(currentLife = newLife))
                        }
                    }
                    showUpdateLifeDialog = false
                    continueAfterLifeUpdate(isEditing) // Proceed to the next step
                },
                onDismiss = { showUpdateLifeDialog = false } // Cancel the whole process
            )
        }

        // 2. Question Dialog
        currentQuestion?.let { question ->
            val isEditing = currentData.scenario.status.toLowerCase() == "completed"
            QuestionDialog(
                question = question,
                onAnswer = { answer ->
                    val answeredQuestion = question.copy(answer = answer)
                    // If the ID is not 0, it already exists in the DB, so update it.
                    if (answeredQuestion.id != 0) {
                        dbManager.updateQuestion(answeredQuestion)
                    } else {
                        // Otherwise, it's a new answer, so add it.
                        dbManager.addQuestion(answeredQuestion)
                    }

                    // Move to the next question or finish... (rest of logic is the same)
                    val remainingQuestions = questionsToAsk.drop(1)
                    questionsToAsk = remainingQuestions
                    currentQuestion = remainingQuestions.firstOrNull()

                    if (currentQuestion == null) {
                        startUpgradeSelection(isEditing)
                    }
                },
                onDismiss = { currentQuestion = null } // Cancel the whole process
            )
        }

        if (showUpgradeDialog && currentData.heroes.isNotEmpty()) {
            val currentHeroForUpgrade = currentData.heroes[currentHeroUpgradeIndex]
            val isEditing = currentData.scenario.status.toLowerCase() == "completed"
            UpgradeSelectionDialog(
                hero = currentHeroForUpgrade,
                availableUpgrades = availableUpgrades,
                onConfirm = { selectedUpgrades ->
                        selectedUpgrades.forEach { selectedUpgrade ->
                            dbManager.assignUpgradeToHero(
                                campaignId = campaignId,
                                presetUpgradeId = selectedUpgrade.id,
                                heroId = currentHeroForUpgrade.id
                            )
                        }
                    // Remove selected upgrade from the available list
                    val selectedIds = selectedUpgrades.map { it.id }.toSet()
                    availableUpgrades = availableUpgrades.filter { it.id !in selectedIds }
                    // Move to the next hero
                    val nextIndex = currentHeroUpgradeIndex + 1
                    if (nextIndex < currentData.heroes.size) {
                        currentHeroUpgradeIndex = nextIndex
                    } else {
                        // All heroes have had their turn
                        showUpgradeDialog = false
                        completeScenarioFlow(isEditing)
                    }
                },
                onDismiss = {
                    // User skips upgrade for this hero, move to the next.
                    val nextIndex = currentHeroUpgradeIndex + 1
                    if (nextIndex < currentData.heroes.size) {
                        currentHeroUpgradeIndex = nextIndex
                    } else {
                        // All heroes have had their turn
                        showUpgradeDialog = false
                        completeScenarioFlow(isEditing)
                    }
                },
                onCancel = { showUpgradeDialog = false } // Cancel the whole process
            )
        }
    }
}


@Composable
fun ScenarioScreenContent(
    modifier: Modifier = Modifier,
    scenario: InstanceScenario,
    heroes: List<InstanceHero>,
    campaignLog: List<InstanceScenario>,
    isCompleted: Boolean,
    isLastScenario: Boolean,
    onCompleteClick: () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.soft_background_yellow),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(
                Color.Black.copy(alpha = 0.2f),
                blendMode = BlendMode.Darken
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Scenario Name
            Text(
                text = scenario.name,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
            )

            // Heroes Grid
            val heroUpgradesMap = heroes.associate { hero ->
                hero.id to hero.upgrades
            }
            HeroInfoGrid(heroes = heroes, heroUpgradesMap = heroUpgradesMap)

            // Separator
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 24.dp),
                thickness = 2.dp,
                color = Color.White.copy(alpha = 0.5f)
            )

            // Campaign Log
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Campaign Decisions",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                if (campaignLog.isEmpty()) {
                    Text(
                        text = "No decisions have been made yet in this campaign.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black.copy(alpha = 0.8f)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(campaignLog, key = { it.id }) { pastScenario ->
                            Column {
                                Text(
                                    text = "From: ${pastScenario.name}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Spacer(Modifier.height(4.dp))
                                Log.d("QUESTIONS_LOG", "question list size:" +pastScenario.questionList.size)
                                pastScenario.questionList
                                    .filter { it.answer.isNotBlank() }
                                    .forEach { question ->
                                        Text(
                                            text = "• ${question.text}: ${question.answer}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.Black.copy(alpha = 0.9f)
                                        )
                                    }
                            }
                        }
                    }
                }
            }
        }

        // Complete Scenario Button
        if(!isLastScenario || !isCompleted) {
            Button(
                onClick = onCompleteClick,
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFED1D24),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                val buttonText = if (isCompleted) "Edit Answers" else "Complete Scenario"
                // Change button text based on scenario status
                Text(buttonText, fontSize = 18.sp)
            }
        }
    }
}

@SuppressLint("DiscouragedApi")
@Composable
fun HeroInfoGrid(heroes: List<InstanceHero>, heroUpgradesMap: Map<Int, List<InstanceUpgrade>>) { // MODIFIED
    val context = LocalContext.current
    val dbManager = remember { DataBaseManager(context) }
    Log.d("HERO UPGRADES CHECK", "Upgrades: $heroUpgradesMap")
    //preset upgrades
    val presetUpgradesMap = remember { dbManager.getAllPresetUpgrades().associateBy { it.id } }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(heroes, key = { it.id }) { hero ->
            val heroUpgrades = heroUpgradesMap[hero.id] ?: emptyList()
            val resourceName = "h${hero.presetHeroId}"
            val resourceId = remember(resourceName) {
                context.resources.getIdentifier(resourceName, "drawable", context.packageName)
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f)),
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = if (resourceId != 0) painterResource(id = resourceId) else painterResource(id = R.drawable.ic_launcher_background),
                        contentDescription = hero.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RectangleShape)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(text = hero.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(text = "Life: ${hero.currentLife}", style = MaterialTheme.typography.bodyMedium)

                    val upgradesByType = heroUpgrades.groupBy {instanceUpgrade ->
                        val preset = presetUpgradesMap[instanceUpgrade.presetUpgradeId]
                        when (preset?.type?.lowercase()) {
                            "ally" -> "Allies"
                            "obligation" -> "Obligations"
                            else -> "Upgrades"
                        }
                    }

                    listOf("Upgrades", "Allies", "Obligations").forEach { type ->
                        upgradesByType[type]?.let { upgrades ->
                            if (upgrades.isNotEmpty()) {
                                Spacer(Modifier.height(6.dp))
                                Text(text = type, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                                upgrades.forEach { upgrade ->
                                    val preset = presetUpgradesMap[upgrade.presetUpgradeId]
                                    Text(text = "• ${preset?.name ?: "Unknown Upgrade"}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// In ScenarioScreen.kt

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
        containerColor = Color.White,
        title = { Text("Update Hero Life (Expert)", color = Color.Black) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(heroes) { hero ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(hero.name, Modifier.weight(1f))
                        OutlinedTextField(
                            value = lifeTotals[hero.id] ?: "",
                            onValueChange = { newLife -> onLifeChange(hero.id, newLife) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(80.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.DarkGray,
                                focusedBorderColor = Color.Black,
                                unfocusedBorderColor = Color.DarkGray,
                                cursorColor = Color.Black,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            label = { Text("Life",color = Color.Black) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
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
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFED1D24),
                    contentColor = Color.White
                ),
                ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun QuestionDialog(
    question: InstanceMarvelQuestion,
    onAnswer: (response: String) -> Unit,
    onDismiss: () -> Unit
) {
    var textResponse by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("Campaign Question", color = Color.Black) },
        text = {
            Column {
                Text(question.text,color = Color.Black, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(16.dp))
                // Adapt UI based on question type
                when (question.questionType) {
                    QuestionType.NUMBER_INPUT -> {
                        OutlinedTextField(
                            value = textResponse,
                            onValueChange = { textResponse = it.filter { char -> char.isDigit() } },
                            label = { Text("Enter a number",color = Color.Black) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.DarkGray,
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.DarkGray,
                            cursorColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                        )
                    }
                    QuestionType.TEXT_INPUT -> {
                        OutlinedTextField(
                            value = textResponse,
                            onValueChange = { textResponse = it },
                            label = { Text("Enter your response",color = Color.Black) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.DarkGray,
                                focusedBorderColor = Color.Black,
                                unfocusedBorderColor = Color.DarkGray,
                                cursorColor = Color.Black,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    }
                    QuestionType.YES_NO -> {
                        // For YES/NO, the buttons are the answer. No input field needed.
                    }
                }
            }
        },
        confirmButton = {
            val response = if (question.questionType == QuestionType.YES_NO) "Yes" else textResponse
            Button(
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFED1D24),
                    contentColor = Color.White
                ),
                onClick = { onAnswer(response) },
                enabled = !(question.questionType != QuestionType.YES_NO && textResponse.isBlank())
            ) {
                Text(if (question.questionType == QuestionType.YES_NO) "Yes" else "Confirm")
            }
        },
        dismissButton = {
            if (question.questionType == QuestionType.YES_NO) {
                TextButton(onClick = { onAnswer("No") }) {
                    Text("No", color = Color.Black)
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun UpgradeSelectionDialog(
    hero: InstanceHero,
    availableUpgrades: List<Upgrade>,
    onConfirm: (selectedUpgrades:List <Upgrade>) -> Unit,
    onDismiss: () -> Unit,
    onCancel: () -> Unit
) {
    var selectedUpgradeIds by remember { mutableStateOf<Set<Int>>(emptySet()) }

    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = Color.White,
        title = { Text("Choose Upgrade for ${hero.name}", color = Color.Black) },
        text = {
            if (availableUpgrades.isEmpty()) {
                Text("No more upgrades available for this scenario.", color = Color.Black)
            } else {
                LazyColumn {
                    items(availableUpgrades) { upgrade ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedUpgradeIds = if(upgrade.id in selectedUpgradeIds)
                                        selectedUpgradeIds - upgrade.id
                                    else
                                        selectedUpgradeIds + upgrade.id

                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = (upgrade.id in selectedUpgradeIds),
                                colors = CheckboxDefaults.colors(checkedColor = Color.Black, uncheckedColor = Color.Black),
                                onCheckedChange = { isChecked ->
                                    selectedUpgradeIds = if (isChecked) {
                                        selectedUpgradeIds + upgrade.id
                                    } else {
                                        selectedUpgradeIds - upgrade.id
                                    }
                                }
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(upgrade.name, color = Color.Black)
                        }
                    }
                }
            }
        },
        confirmButton = { },
        dismissButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End // Aligns buttons to the right
            ) {
                // This is the original "Confirm" button, now placed manually
                Button(
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFED1D24),
                        contentColor = Color.White
                    ),
                    onClick = {
                        val selected = availableUpgrades.filter { it.id in selectedUpgradeIds }
                        onConfirm(selected)
                    },
                    enabled = selectedUpgradeIds.isNotEmpty()
                ) {
                    Text("Confirm for ${hero.name}", color = Color.Black)
                }

                // These are the original dismiss buttons
                TextButton(onClick = onDismiss) {
                    Text("Skip for ${hero.name}", color = Color.Black)
                }
                TextButton(onClick = onCancel) {
                    Text("Cancel All", color = Color.Black)
                }
            }
        }
    )
}


// --- PREVIEW ---
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ScenarioScreenPreview() {
    // 1. Create Dummy Data
    val dummyScenario = InstanceScenario(
        id = 1,
        instanceCampaignId = 1,
        presetScenarioId = 1,
        name = "The Wrecking Crew",
        villainName = "Wrecker",
        description = "A dummy description.",
        questionList = emptyList(),
        startDate = LocalDateTime.now(),
        endDate = null,
        status = "current"
    )

    val dummyHeroes = mutableListOf(
        InstanceHero(1, 1, 1, 0, "Spider-Man", 10, emptyList(), LocalDateTime.now()),
        InstanceHero(2, 2, 1, 0, "Captain Marvel", 12, emptyList(), LocalDateTime.now()),
        InstanceHero(3, 3, 1, 0, "She-Hulk", 15, emptyList(), LocalDateTime.now()),
        InstanceHero(4, 4, 1, 0, "Iron Man", 9, emptyList(), LocalDateTime.now())
    )

    // Dummy Upgrades for preview purposes
    val dummyUpgrades = listOf(
        InstanceUpgrade(1,1,1,1, "Web-Shooter", true),
        InstanceUpgrade(2,2,2,2, "Super-Soldier Serum", true),
        InstanceUpgrade(3,3,3,3,"Nick Fury", true),
        InstanceUpgrade(4,4,4,4, "Family Emergency", true)
    )
    // Manually add upgrades to heroes for the preview
    dummyHeroes[0] = dummyHeroes[0].copy(upgrades = listOf(dummyUpgrades[0]))
    dummyHeroes[1] = dummyHeroes[1].copy(upgrades = listOf(dummyUpgrades[1], dummyUpgrades[2]))
    dummyHeroes[2] = dummyHeroes[2].copy(upgrades = listOf(dummyUpgrades[3]))


    val dummyCampaignLog = listOf(
        InstanceScenario(
            id = 0, instanceCampaignId = 1, presetScenarioId = 0, name = "Rise of the Red Skull", villainName = "Crossbones",
            description = "", status = "completed", startDate = LocalDateTime.now(), endDate = LocalDateTime.now(),
            questionList = mutableListOf(
                InstanceMarvelQuestion(1, 1, 1, "Did you rescue the agents?", QuestionType.YES_NO, "Yes"),
                InstanceMarvelQuestion(2, 1, 1, "How many allies were defeated?", QuestionType.YES_NO, "yes")
            )
        )
    )

    // 2. Render the Content
    MarvelChampionsCampaignCompanionTheme {
        ScenarioScreenContent(
            scenario = dummyScenario,
            heroes = dummyHeroes,
            campaignLog = dummyCampaignLog,
            onCompleteClick = {},
            isCompleted = false,
            isLastScenario = false
        )
    }
}
