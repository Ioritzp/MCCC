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
import DataBaseManager
import Utils.SessionManager
import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.marvelchampionscampaigncompanion.ui.theme.MarvelChampionsCampaignCompanionTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp


// This is now an Activity
class CampaignSelector : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userId = intent.getIntExtra("USER_ID", -1)
        enableEdgeToEdge()
        setContent {
            MarvelChampionsCampaignCompanionTheme {
                CampaignSelectorRoute(userId = userId)
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CampaignSelectorRoute(userId: Int) {
    val context = LocalContext.current
    val dbManager = remember { DataBaseManager(context) }

    var availableCampaigns by remember { mutableStateOf<List<InstanceCampaign>>(emptyList()) }

    var refreshTrigger by remember { mutableIntStateOf(0) }

    //delete variables
    var isDeleteModeActive by remember { mutableStateOf(false) }
    var campaignsToDelete by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    //user menu variables
    var showUserMenu by remember { mutableStateOf(false) }
    var showUserDeleteDialog by remember { mutableStateOf(false) }

    var userMap by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }



    LaunchedEffect(userId, refreshTrigger) {
        availableCampaigns = dbManager.readCampaigns(userId)
        Log.d("Campaign selector", "List size: ${availableCampaigns.size} and user id: $userId")

        userMap = dbManager.readUsers().associateBy({ it.id }, { it.name })
    }

    val newCampaignLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {result ->
            refreshTrigger++
    }

    if (showUserDeleteDialog){
        AlertDialog(
            onDismissRequest = { showUserDeleteDialog = false },
            containerColor = Color.White,
            title = { Text("Delete User Account?") },
            text = { Text("This action is permanent and will delete both your user and the associated campaigns. Are you sure?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        dbManager.deleteUser(userId)
                        showUserDeleteDialog = false
                        // Navigate back to MainActivity and clear the task stack
                        val intent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        context.startActivity(intent)
                    }
                ) { Text("Confirm Delete", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showUserDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteConfirmDialog){
        AlertDialog(
            onDismissRequest = {showDeleteConfirmDialog = false},
            containerColor = Color.White,
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
        userMap = userMap,
        isDeleteModeActive = isDeleteModeActive,
        campaignsToDelete = campaignsToDelete,
        showUserMenu = showUserMenu,
        onToggleDeleteMode = { isDeleteModeActive = !isDeleteModeActive; campaignsToDelete = emptySet() },
        onCampaignSelectedForDelete = { campaignId ->

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
            val intent = Intent(context, NewCampaignCreation::class.java).apply {
                putExtra("USER_ID", userId)
            }
            newCampaignLauncher.launch(intent)
        },

        onUserMenuClick = {showUserMenu = !showUserMenu},
        onDismissUserMenu = {showUserMenu = false},
        onCloseSession = {
            val sessionManager = SessionManager(context)
            sessionManager.clearSession()
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            context.startActivity(intent)
        },
        onDeleteUser = {
            showUserMenu = false
            showUserDeleteDialog = true
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CampaignSelectorScreen(
    availableCampaigns: List<InstanceCampaign>,
    userMap: Map<Int, String>,
    isDeleteModeActive: Boolean,
    campaignsToDelete: Set<Int>,
    showUserMenu:Boolean,
    onToggleDeleteMode: () -> Unit,
    onCampaignSelectedForDelete: (Int) -> Unit,
    onDeleteConfirm: () -> Unit,
    onLaunchNewCampaign: () -> Unit,
    onUserMenuClick: () -> Unit,
    onDismissUserMenu: () -> Unit,
    onCloseSession: () -> Unit,
    onDeleteUser: () -> Unit
) {
    val context = LocalContext.current


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (!isDeleteModeActive) {
                BottomAppBar(

                    modifier = Modifier.height(92.dp),
                    containerColor = Color(0xFFBA3A3A),

                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp), // Add padding inside the row
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // LEFT BUTTON
                        val isEnabled = availableCampaigns.isNotEmpty()
                        FloatingActionButton(
                            onClick = { if (isEnabled) onToggleDeleteMode() },
                            containerColor = Color.White,
                            contentColor = Color.Black,
                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp)
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete campaign(s)"
                            )
                        }

                        // MIDDLE BUTTON
                        FloatingActionButton(
                            onClick = onLaunchNewCampaign,
                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp),
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ) {
                            Icon(Icons.Filled.Add, "Create New Campaign", tint = Color.Black)
                        }

                        Box {
                            // RIGHT BUTTON
                            FloatingActionButton(
                                onClick = onUserMenuClick,
                                containerColor = Color.White,
                                contentColor = Color.Black,
                                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp),
                                modifier = Modifier.padding(end = 16.dp)
                            ) {
                                Icon(Icons.Filled.AccountCircle, "User Menu")
                            }
                            // User Dropdown Menu
                            DropdownMenu(
                                expanded = showUserMenu,
                                onDismissRequest = onDismissUserMenu
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Close Session") },
                                    onClick = onCloseSession
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete User", color = Color.Red) },
                                    onClick = onDeleteUser
                                )
                            }
                        }
                    }
                }
            }


        },
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
            Image(
                painter = painterResource(id = R.drawable.soft_background),
                contentDescription = "Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.2f),
                blendMode = androidx.compose.ui.graphics.BlendMode.Darken)
            )
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.main_logo), // <-- Use your logo image
                    contentDescription = "Logo",
                    modifier = Modifier
                        .fillMaxWidth(0.8f) // Takes 80% of the screen width
                        .padding(top = 16.dp)
                )

                Text(
                    //TODO: change the typography
                    text = "Campaigns",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                if (availableCampaigns.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val createCampaignTextStyle = TextStyle(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "You have no saved campaigns.\nPress the '+' button to get started!",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            style = createCampaignTextStyle.copy(
                                drawStyle = Stroke(
                                    miter = 10f,
                                    width = 4f,
                                    join = StrokeJoin.Round
                                )
                            )

                        )
                        Text(
                            text = "You have no saved campaigns.\nPress the '+' button to get started!",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            style = createCampaignTextStyle
                        )
                    }
                } else {

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = 8.dp,
                            bottom = if (isDeleteModeActive) 80.dp else 8.dp
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(
                            items = availableCampaigns,
                            key = { campaign -> campaign.id }
                        ) { campaign ->
                            val userName = userMap[campaign.userId] ?:"Unknown user"
                            InstancedCampaignButton(
                                campaign = campaign,
                                userName = userName,
                                isDeleteMode = isDeleteModeActive,
                                context = context,
                                isSelectedForDelete = campaign.id in campaignsToDelete,
                                onClick = {
                                    if (isDeleteModeActive) {
                                        // In delete mode, a simple click selects/deselects
                                        onCampaignSelectedForDelete(campaign.id)
                                    } else {
                                        // In normal mode, a simple click navigates
                                        val intent =
                                            Intent(
                                                context,
                                                CampaignInstanceDetail::class.java
                                            ).apply {
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
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFED1D24),
                        contentColor = Color.White
                    )
                ) {
                    Text("Delete Selected", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.O) // Annotation needed for LocalDateTime
@Composable
fun InstancedCampaignButton(
    campaign: InstanceCampaign,
    userName: String,
    isDeleteMode: Boolean,
    isSelectedForDelete: Boolean,
    onClick: () -> Unit,
    context: Context,
    onLongClick: () -> Unit
) {
    val dbManager = DataBaseManager(context)
    val (color1, color2) = when (campaign.presetCampaignId) {
        1 -> Color(0xFF962626) to Color(0xFF962626)
        2 -> Color(0xFF211CAD) to Color(0xFF211CAD)
        3 -> Color(0xFFC2973C) to Color(0xFFC2973C)
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.surface // Default fallback colors
    }

    // Create the striped brush
    val colorStops = mutableListOf<Pair<Float, Color>>()
    val step = 0.2f

    for (i in 0 until 10){
        var currenStep = i*step

        colorStops.add((currenStep + 0.0f) to color1)
        colorStops.add((currenStep + 0.1f) to color1)

        colorStops.add((currenStep + 0.11f) to color2)
        colorStops.add((currenStep + 0.2f) to color2)
    }

    val stripedBrush = Brush.linearGradient(colorStops = colorStops.toTypedArray())

    val imageRes = when (campaign.presetCampaignId) {
        1 -> R.drawable.red_skull
        2 -> R.drawable.most_wanted
        3 -> R.drawable.mad_titan
        else -> R.drawable.fallback_image
    }

    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }


    // Use a Box to draw the background brush behind the Card content
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .border(
                width = if (isDeleteMode) 5.dp else 3.dp,
                color = Color.Black,
                shape = MaterialTheme.shapes.extraLarge
            )
            .background(stripedBrush)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- Campaign Portrait Image ---
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "${campaign.name} Portrait",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .border(
                        2.dp,
                        Color.White.copy(alpha = 0.7f), CircleShape
                    )
            )

            Spacer(modifier = Modifier.width(16.dp))

            // --- Campaign Info Column ---
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = campaign.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White // White text for good contrast on dark colors
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "User: $userName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Players: ${campaign.playerNum}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    text = "Difficulty: ${campaign.difficulty}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                // --- Creation Date Display ---
                Text(
                    text = "Created: ${campaign.startDate.format(dateFormatter)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )

                if(campaign.endDate != null) {
                    Text(
                        text = "Finished: ${campaign.endDate?.format(dateFormatter)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            if(campaign.endDate !=null){
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Campaign Completed",
                    tint = Color(0xFF388E3C),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(24.dp)
                )
            }

            // --- Delete Mode Checkbox ---
            if (isDeleteMode) {
                Spacer(modifier = Modifier.width(16.dp))
                Checkbox(
                    checked = isSelectedForDelete,
                    onCheckedChange = { onClick() },
                    colors = CheckboxDefaults.colors(
                        checkmarkColor = color1,
                        checkedColor = Color.White,
                        uncheckedColor = Color.White
                    )
                )
            }
        }
    }
}




// --- PREVIEWS ---

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
        // Add the missing arguments for the new parameters
        CampaignSelectorScreen(
            availableCampaigns = dummyCampaigns,
            userMap = emptyMap(),
            isDeleteModeActive = false,
            campaignsToDelete = emptySet(),
            showUserMenu = false,
            onToggleDeleteMode = {},
            onCampaignSelectedForDelete = {},
            onDeleteConfirm = {},
            onLaunchNewCampaign = {},
            onUserMenuClick = {},
            onDismissUserMenu = {},
            onCloseSession = {},
            onDeleteUser = {}
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(name = "Empty State", showBackground = true, showSystemUi = true)
@Composable
private fun CampaignSelectorPreview_Empty() {
    MarvelChampionsCampaignCompanionTheme {
        // Pass an empty list and add the missing arguments
        CampaignSelectorScreen(
            availableCampaigns = emptyList(),
            userMap = emptyMap(),
            isDeleteModeActive = false,
            campaignsToDelete = emptySet(),
            showUserMenu = false,
            onToggleDeleteMode = {},
            onCampaignSelectedForDelete = {},
            onDeleteConfirm = {},
            onLaunchNewCampaign = {},
            onUserMenuClick = {},
            onDismissUserMenu = {},
            onCloseSession = {},
            onDeleteUser = {}
        )
    }
}
