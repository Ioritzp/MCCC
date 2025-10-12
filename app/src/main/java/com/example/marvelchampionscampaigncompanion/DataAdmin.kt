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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import Classes.*
import DataBaseManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
    val heroesList = remember { mutableStateListOf<Hero>() }
    val scope = rememberCoroutineScope()

    // State to control if the "Add Hero" dialog is visible
    var showAddHeroDialog by remember { mutableStateOf(false) }

    // When showAddHeroDialog is true, this dialog will be displayed
    if (showAddHeroDialog) {
        AddHeroDialog(
            onDismiss = {
                // This is called when the user clicks outside the dialog or the back button
                showAddHeroDialog = false
            },
            onAddHero = { name, initialLife ->
                scope.launch(Dispatchers.IO) {
                    // Call the database function to add the hero
                    dbManager.addPresetHero(name, initialLife)
                }

                // Show a confirmation toast
                //Toast.makeText(context, "Hero '$name' created", Toast.LENGTH_SHORT).show()
                // Hide the dialog
                showAddHeroDialog = false
            }
        )
    }

    var showHeroesdialog by remember{mutableStateOf(false)}
    var heroList by remember { mutableStateOf<List<Hero>>(emptyList()) }
    if(showHeroesdialog){

        CheckHeroesDialog(
            onDismiss = {
                showHeroesdialog = false
            },
            heroList = heroList
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

            // "Add Presets" section
            item { Text("Add Presets", style = MaterialTheme.typography.titleLarge) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // This button now opens the dialog
                    Button(onClick = {
                        showAddHeroDialog = true
                    }) { Text("Add Hero") }
                    Button(onClick = { /* No functionality */ }) { Text("Add Question") }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { /* No functionality */ }) { Text("Add Scenario") }
                    Button(onClick = { /* No functionality */ }) { Text("Add Campaign") }
                }
            }

            // Spacer between sections
            item { Spacer(modifier = Modifier.height(24.dp)) }

            // "Check & Modify Presets" section
            item { Text("Check & Modify Presets", style = MaterialTheme.typography.titleLarge) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        scope.launch(Dispatchers.IO) {
                            val heroes = dbManager.getAllHeroes()
                            withContext(Dispatchers.Main) {
                                heroList = heroes
                                showHeroesdialog = true
                            }
                        }
                     }) { Text("Check Heroes") }
                    Button(onClick = { /* No functionality */ }) { Text("Check Questions") }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { /* No functionality */ }) { Text("Check Scenarios") }
                    Button(onClick = { /* No functionality */ }) { Text("Check Campaigns") }
                }
            }
        }
    }
}

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
                    text = "Preset heroes list",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))
                // 2. List of Heroes
                // LazyColumn is used to efficiently display scrollable lists.
                LazyColumn (modifier = Modifier.weight(1f, fill = false)){
                    if(heroList.isEmpty()){
                        item{
                            Text(
                                "no heroes in db",
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

                // 3. Close Button
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






/**
 * A dialog Composable for adding a new hero.
 * @param onDismiss Function to be called to close the dialog.
 * @param onAddHero Function that passes the entered name and life to be processed.
 */
@Composable
fun AddHeroDialog(
    onDismiss: () -> Unit,
    onAddHero: (name: String, initialLife: Int) -> Unit
) {
    // State for the text fields inside the dialog
    var name by remember { mutableStateOf("") }
    var initialLife by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Hero") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Hero Name") },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = initialLife,
                    onValueChange = { newValue ->
                        // Allow only digits in the life field
                        if (newValue.all { it.isDigit() }) {
                            initialLife = newValue
                        }
                    },
                    label = { Text("Initial Life") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val life = initialLife.toIntOrNull() ?: 0
                    if (name.isNotBlank() && life > 0) {
                        onAddHero(name, life)
                    }
                },
                // The create button is only enabled if both fields are valid
                enabled = name.isNotBlank() && (initialLife.toIntOrNull() ?: 0) > 0
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DataAdminScreenPreview() {
    MaterialTheme {
        DataAdministration()
    }
}