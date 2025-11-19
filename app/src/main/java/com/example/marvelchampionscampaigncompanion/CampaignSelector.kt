package com.example.marvelchampionscampaigncompanion

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import Classes.InstanceCampaign
import Classes.InstanceScenario
import DataBaseManager
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.marvelchampionscampaigncompanion.ui.theme.MarvelChampionsCampaignCompanionTheme
import java.time.LocalDateTime


// This is now an Activity
class CampaignSelector : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MarvelChampionsCampaignCompanionTheme {
                CampaignSelectorRoute()
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CampaignSelectorRoute() {
    val context = LocalContext.current
    val dbManager = remember { DataBaseManager(context) }

    var availableCampaigns by remember { mutableStateOf<List<InstanceCampaign>>(emptyList()) }

    var refreshTrigger by remember { mutableIntStateOf(0) }

    //delete variables
    var isDeleteModeActive by remember { mutableStateOf(false) }
    var campaignsToDelete by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }


    LaunchedEffect(refreshTrigger) {
        availableCampaigns = dbManager.readCampaigns()
    }

    val newCampaignLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {result ->
        if (result.resultCode == Activity.RESULT_OK) {
            refreshTrigger++
        }

    }

    if (showDeleteConfirmDialog){
        AlertDialog(
            onDismissRequest = {showDeleteConfirmDialog = false},
            title = {Text("Confirm campaign deletion")},
            text = {Text("Are you sure you want to permanently delete the campaigns?")},
            confirmButton = {
                TextButton(
                    onClick = {
                        campaignsToDelete.forEach { campaignId ->
                            dbManager.deleteCampaignAndAllRelatedData(campaignId)
                            }
                        showDeleteConfirmDialog = false
                        isDeleteModeActive = false
                        campaignsToDelete = emptySet()
                        refreshTrigger++
                        }
                ) {Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Pass the real data to the UI screen
    CampaignSelectorScreen(
        availableCampaigns = availableCampaigns,
        isDeleteModeActive = isDeleteModeActive, // Pass state down
        campaignsToDelete = campaignsToDelete, // Pass state down
        onToggleDeleteMode = { isDeleteModeActive = !isDeleteModeActive; campaignsToDelete = emptySet() }, // Toggle delete mode and clear selections
        onCampaignSelectedForDelete = { campaignId ->
            // Add or remove the campaign from the selection set
            campaignsToDelete = if (campaignId in campaignsToDelete) {
                campaignsToDelete - campaignId
            } else {
                campaignsToDelete + campaignId
            }
        },
        onDeleteConfirm = {
            if (campaignsToDelete.isNotEmpty()) {
                showDeleteConfirmDialog = true
            }
        },
        onLaunchNewCampaign = {
            val intent = Intent(context, NewCampaignCreation::class.java)
            newCampaignLauncher.launch(intent)
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CampaignSelectorScreen(
    availableCampaigns: List<InstanceCampaign>,
    isDeleteModeActive: Boolean,
    campaignsToDelete: Set<Int>,
    onToggleDeleteMode: () -> Unit,
    onCampaignSelectedForDelete: (Int) -> Unit,
    onDeleteConfirm: () -> Unit,
    onLaunchNewCampaign: () -> Unit
) {
    val context = LocalContext.current


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Marvel Champions Campaigns")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        },
        bottomBar = {
            if (!isDeleteModeActive) {
                BottomAppBar(
                    actions = {
                        IconButton(
                            onClick = onToggleDeleteMode,
                            enabled = availableCampaigns.isNotEmpty()
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete campaign(s)",
                                tint = if (isDeleteModeActive) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }
                    },
                    floatingActionButton = {
                        if (isDeleteModeActive) {
                            FloatingActionButton(
                                onClick = onDeleteConfirm,
                                containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                            ) {
                                Icon(Icons.Filled.Check, "Confirm Deletion")
                            }
                        } else {
                            FloatingActionButton(onClick = onLaunchNewCampaign) {
                                Icon(Icons.Filled.Add, "Create New Campaign")
                            }
                        }
                    }
                )
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (availableCampaigns.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "You have no saved campaigns.\nPress the '+' button to get started!")
                }
            } else {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 8.dp,
                        bottom = if(isDeleteModeActive) 80.dp else 8.dp
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(
                        items = availableCampaigns,
                        key = { campaign -> campaign.id }
                    ) { campaign ->
                        InstancedCampaignButton(
                            campaign = campaign,
                            isDeleteMode = isDeleteModeActive,
                            isSelectedForDelete = campaign.id in campaignsToDelete,
                            onClick = {
                                if (isDeleteModeActive) {
                                    // In delete mode, a simple click selects/deselects
                                    onCampaignSelectedForDelete(campaign.id)
                                } else {
                                    // In normal mode, a simple click navigates
                                    val intent =
                                        Intent(context, CampaignInstanceDetail::class.java).apply {
                                            putExtra("CAMPAIGN_INSTANCE_ID", campaign.id)
                                        }
                                    context.startActivity(intent)
                                }
                            },
                            // NEW: Long press gesture to enter delete mode
                            onLongClick = {
                                if (!isDeleteModeActive) {
                                    onToggleDeleteMode() // Enter delete mode
                                    onCampaignSelectedForDelete(campaign.id) // And select the long-pressed item
                                }
                            }
                        )
                    }
                }
            }
            if (isDeleteModeActive) {
                Button(
                    onClick = onDeleteConfirm,
                    // Disable the button if no campaigns are selected for deletion
                    enabled = campaignsToDelete.isNotEmpty(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter) // Pushes it to the bottom-center
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp) // Give it some padding
                        .height(56.dp), // A standard button height
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Delete Selected", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class) // Add this for onLongClick
@Composable
fun InstancedCampaignButton(
    campaign: InstanceCampaign,
    isDeleteMode: Boolean,
    isSelectedForDelete: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit // NEW: Add long click handler
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp)
            .combinedClickable( // Use combinedClickable for both click types
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Campaign Info Column (unchanged)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = campaign.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Players: ${campaign.playerNum}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Difficulty: ${campaign.difficulty}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (isDeleteMode) {
                Spacer(modifier = Modifier.width(16.dp))
                Checkbox(
                    checked = isSelectedForDelete,
                    onCheckedChange = { onClick() } // Let the click handler manage state
                )
            }
        }
    }
}



// --- PREVIEWS ---

@RequiresApi(Build.VERSION_CODES.O)
@Preview(name = "With Saved Campaigns", showBackground = true, showSystemUi = true)
@Composable
private fun CampaignSelectorPreview_WithCampaigns() {
    // Create a fake list of campaigns just for the preview
    val dummyCampaigns = listOf(
        InstanceCampaign(1, 1, "The Rise of Red Skull", "desc", arrayListOf(), 1, "user", 2, LocalDateTime.now(), LocalDateTime.now(), "Normal"),
        InstanceCampaign(2, 3, "The Mad Titan's Shadow", "desc", arrayListOf(), 1, "user", 4, LocalDateTime.now(), LocalDateTime.now(), "Expert"),
        InstanceCampaign(3, 2, "Galaxy's Most Wanted", "desc", arrayListOf(), 1, "user", 1, LocalDateTime.now(), LocalDateTime.now(), "Standard")
    )
    MarvelChampionsCampaignCompanionTheme {
        CampaignSelectorScreen(availableCampaigns = dummyCampaigns, false, emptySet(), {}, {}, {}, onLaunchNewCampaign = {})
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(name = "Empty State", showBackground = true, showSystemUi = true)
@Composable
private fun CampaignSelectorPreview_Empty() {
    MarvelChampionsCampaignCompanionTheme {
        // Pass an empty list to see how the "empty" message looks
        CampaignSelectorScreen(availableCampaigns = emptyList(),false, emptySet(), {}, {}, {}, onLaunchNewCampaign = {} )
    }
}