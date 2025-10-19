package com.example.marvelchampionscampaigncompanion

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import Classes.*
import DataBaseManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview


class DataAdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                DataAdministration()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataAdministration() {
    val context = LocalContext.current
    val dbManager = remember { DataBaseManager(context) }
    val scope = rememberCoroutineScope()

    // State for the "Check Heroes" dialog
    var showHeroesdialog by remember{mutableStateOf(false)}
    var heroList by remember { mutableStateOf<List<Hero>>(emptyList()) }

    //State for the "Check Campaigns" dialog
    var showCampaignsdialog by remember{mutableStateOf(false)}
    var campaignList by remember { mutableStateOf<List<Campaign>>(emptyList()) }

    //State for the "Check scenarios" dialog
    var showScenariosdialog by remember{mutableStateOf(false)}
    var scenariosList by remember { mutableStateOf<List<Scenario>>(emptyList()) }

    //State for the "Check questions" dialog
    var showQuestionsDialog by remember{mutableStateOf(false)}
    var questionsList by remember { mutableStateOf<List<MarvelQuestion>>(emptyList()) }

    //State for the "Check upgrades" dialog
    var showUpgradesDialog by remember{mutableStateOf(false)}
    var upgradesList by remember { mutableStateOf<List<Upgrade>>(emptyList()) }

    //State for the "show users" dialog

    var showUsersDialog by remember{mutableStateOf(false)}
    var usersList by remember{mutableStateOf<List<User>>(emptyList())}

    if(showHeroesdialog){
        CheckHeroesDialog(
            onDismiss = {
                showHeroesdialog = false
            },
            heroList = heroList
        )
    }

    if(showCampaignsdialog){
        CheckCampaignsDialog(
            onDismiss = {
                showCampaignsdialog = false
            },
            campaignList = campaignList
        )
    }

    if(showScenariosdialog){
        CheckScenariosDialog(
            onDismiss = {
                showScenariosdialog = false
            },

            scenarioList = scenariosList

        )
    }

    if(showQuestionsDialog){
        CheckQuestionsDialog(
            onDismiss = {
                showQuestionsDialog = false
            },

            questionList = questionsList

        )
    }

    if(showUpgradesDialog){
        CheckUpgradesDialog(
            onDismiss = {
                showUpgradesDialog = false
            },

            upgradeList = upgradesList



        )
    }

    if(showUsersDialog){
        CheckUsersDialog(
            onDismiss = {
                showUsersDialog = false
            },

            userList = usersList



        )
    }

    // Main Screen Layout
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Database Manager") })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // Section for viewing preset data
            item { Text("Preset Data", style = MaterialTheme.typography.titleLarge) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        scope.launch(Dispatchers.IO) {
                            val heroes = dbManager.getAllPresetHeroes()
                            withContext(Dispatchers.Main) {
                                heroList = heroes
                                showHeroesdialog = true
                            }
                        }
                    }) { Text("Check Heroes") }
                    Button(onClick = {
                        scope.launch(Dispatchers.IO) {
                            val questions = dbManager.getAllPresetQuestions()
                            withContext(Dispatchers.Main) {
                                questionsList = questions
                                showQuestionsDialog = true
                            }
                        }

                    }) { Text("Check Questions") }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        scope.launch(Dispatchers.IO) {
                            val scenarios = dbManager.getAllPresetScenarios()
                            withContext(Dispatchers.Main) {
                                scenariosList = scenarios
                                showScenariosdialog = true
                            }
                        }


                    }) { Text("Check Scenarios") }
                    Button(onClick = {
                    scope.launch(Dispatchers.IO) {
                            val campaigns = dbManager.getAllPresetCampaigns()
                        withContext(Dispatchers.Main) {
                                campaignList = campaigns
                                showCampaignsdialog = true
                        }
                    }
                    }) { Text("Check Campaigns") }
                }
            }

            item{
                Row( horizontalArrangement = Arrangement.Center) {
                    Button(onClick = {
                        scope.launch(Dispatchers.IO) {
                            val upgrades = dbManager.getAllPresetUpgrades()
                            withContext(Dispatchers.Main) {
                                upgradesList = upgrades
                                showUpgradesDialog = true
                            }

                        }
                    }

                    ) { Text("Check Upgrades")}
                }
            }

            item{
                Row( horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        scope.launch(Dispatchers.IO) {
                            val users = dbManager.readUsers()
                            withContext(Dispatchers.Main) {
                                usersList = users
                                showUsersDialog = true
                            }

                        }

                    }

                    ) { Text("Check Users")}

                    Button(onClick = {
                        scope.launch(Dispatchers.IO) {
                            val users = dbManager.readUsers()
                            withContext(Dispatchers.Main) {
                                usersList = users
                                showUsersDialog = true
                            }

                        }

                    }

                    ) { Text("Check user campaigns ")}
                }
            }
        }
    }
}

//region: show the database item on lists.

@Composable
fun CheckHeroesDialog(
    onDismiss: () -> Unit,
    heroList: List<Hero>
){
    // Use the 'Dialog' composable for a fullscreen-like pop-up experience
    Dialog(onDismissRequest = onDismiss) {
        // Surface provides a background color and elevation for the dialog
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp
        ){
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Preset Heroes List",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))
                // List of Heroes
                LazyColumn (modifier = Modifier.weight(1f, fill = false)){
                    if(heroList.isEmpty()){
                        item{
                            Text(
                                "No heroes found in the database.",
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    } else {
                        items(heroList){ hero ->
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(
                                    text = hero.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "ID: ${hero.id}, Initial Life: ${hero.initialLife}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Divider(modifier = Modifier.padding(top = 8.dp)) // Separator line
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun CheckCampaignsDialog(
    onDismiss: () -> Unit,
    campaignList: List<Campaign>
){
    // Use the 'Dialog' composable for a fullscreen-like pop-up experience
    Dialog(onDismissRequest = onDismiss) {
        // Surface provides a background color and elevation for the dialog
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp
        ){
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Preset campaign List",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))
                // List of campaigns
                LazyColumn (modifier = Modifier.weight(1f, fill = false)){
                    if(campaignList.isEmpty()){
                        item{
                            Text(
                                "No campaigns found in the database.",
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    } else {
                        items(campaignList){ campaign ->
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(
                                    text = campaign.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "ID: ${campaign.id}, Description: ${campaign.description}",
                                    style = MaterialTheme.typography.bodySmall
                                )

                                if (campaign.scenarioList.isNotEmpty()){
                                    val scenarioNames = campaign.scenarioList.joinToString(separator = ", ") {it.name}
                                    Text(
                                        text = "Scenarios: $scenarioNames",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(top = 8.dp),
                                thickness = 1.dp,
                                color = Color.Black
                            ) // Separator line
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun CheckScenariosDialog(
    onDismiss: () -> Unit,
    scenarioList: List<Scenario>
){
    // Use the 'Dialog' composable for a fullscreen-like pop-up experience
    Dialog(onDismissRequest = onDismiss) {
        // Surface provides a background color and elevation for the dialog
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp
        ){
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Preset scenario List",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))
                // List of scenarios
                LazyColumn (modifier = Modifier.weight(1f, fill = false)){
                    if(scenarioList.isEmpty()){
                        item{
                            Text(
                                "No scenarios found in the database.",
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    } else {
                        items(scenarioList){ scenario ->
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(
                                    text = scenario.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "ID: ${scenario.id}, Description: ${scenario.description}",
                                    style = MaterialTheme.typography.bodySmall
                                )

                                if (scenario.questionList.isNotEmpty()){
                                    val questionText = scenario.questionList.joinToString(separator = ", ") {it.text}
                                    Text(
                                        text = "Questions: $questionText",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(top = 8.dp),
                                thickness = 1.dp,
                                color = Color.Black
                            ) // Separator line
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun CheckQuestionsDialog(
    onDismiss: () -> Unit,
    questionList: List<MarvelQuestion>
){
    // Use the 'Dialog' composable for a fullscreen-like pop-up experience
    Dialog(onDismissRequest = onDismiss) {
        // Surface provides a background color and elevation for the dialog
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp
        ){
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Preset questions List",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))
                // List of questions
                LazyColumn (modifier = Modifier.weight(1f, fill = false)){
                    if(questionList.isEmpty()){
                        item{
                            Text(
                                "No questions found in the database.",
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    } else {
                        items(questionList){ question ->
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(
                                    text = "ID: ${question.id}, Text: ${question.text}",
                                    style = MaterialTheme.typography.bodySmall
                                )

                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(top = 8.dp),
                                thickness = 1.dp,
                                color = Color.Black
                            ) // Separator line
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun CheckUpgradesDialog(
    onDismiss: () -> Unit,
    upgradeList: List<Upgrade>
){
    // Use the 'Dialog' composable for a fullscreen-like pop-up experience
    Dialog(onDismissRequest = onDismiss) {
        // Surface provides a background color and elevation for the dialog
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp
        ){
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Preset upgrades List",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))
                // List of upgrades
                LazyColumn (modifier = Modifier.weight(1f, fill = false)){
                    if(upgradeList.isEmpty()){
                        item{
                            Text(
                                "No upgrades found in the database.",
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    } else {
                        items(upgradeList){ upgrade ->
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(
                                    text = "ID: ${upgrade.id}, Name: ${upgrade.name}",
                                    style = MaterialTheme.typography.bodySmall
                                )

                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(top = 8.dp),
                                thickness = 1.dp,
                                color = Color.Black
                            ) // Separator line
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

//user table show - this is not a pre-populated table, but a user populated one.

@Composable
fun CheckUsersDialog(
    onDismiss: () -> Unit,
    userList: List<User>
){
    // Use the 'Dialog' composable for a fullscreen-like pop-up experience
    Dialog(onDismissRequest = onDismiss) {
        // Surface provides a background color and elevation for the dialog
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp
        ){
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "User List",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))
                // List of users
                LazyColumn (modifier = Modifier.weight(1f, fill = false)){
                    if(userList.isEmpty()){
                        item{
                            Text(
                                "No users registered yet.",
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    } else {
                        items(userList){ user ->
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(
                                    text = "ID: ${user.id}, Name: ${user.name}, email: ${user.email}",
                                    style = MaterialTheme.typography.bodySmall
                                )

                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(top = 8.dp),
                                thickness = 1.dp,
                                color = Color.Black
                            ) // Separator line
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DataAdminScreenPreview() {
    MaterialTheme {
        DataAdministration()
    }
}