package com.example.marvelchampionscampaigncompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.marvelchampionscampaigncompanion.ui.theme.ui.theme.MarvelChampionsCampaignCompanionTheme

class CampaignInstanceDetail : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val instanceId = intent.getStringExtra("CAMPAIGN_INSTANCE_ID")
        val instanceName = intent.getStringExtra("CAMPAIGN_INSTANCE_NAME") ?: "Campaign Details"
        setContent {
            MarvelChampionsCampaignCompanionTheme {
                Scaffold(                 topBar = {
                    TopAppBar(
                        title = { Text(instanceName) },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        navigationIcon = { // Optional: Add a back button
                            IconButton(onClick = { finish() }) { // finish() will take user back
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                }
                ) { innerPadding ->
                    CampaignInstanceDetailScreenContent(
                        modifier = Modifier.padding(innerPadding),
                        campaignId = instanceId,
                        campaignName = instanceName
                    )
                }
            }
        }
    }
}

@Composable
fun CampaignInstanceDetailScreenContent(
    modifier: Modifier = Modifier,
    campaignId: String?,
    campaignName: String?
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "This is the campaign: ${campaignName ?: "N/A"}\n(ID: ${campaignId ?: "Unknown"})",
            style = MaterialTheme.typography.headlineMedium
        )
        // TODO: todos los detalles, acciones, etc. Ahora mismo es solo visual.
    }
}


@Preview(showBackground = true)
@Composable
fun CampaignInstanceDetailScreenPreview() {
    MarvelChampionsCampaignCompanionTheme {
        CampaignInstanceDetailScreenContent(campaignId = "preview_id_123", campaignName = "Preview Campaign")
    }
}