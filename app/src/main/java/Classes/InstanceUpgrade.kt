package Classes

data class InstanceUpgrade(
    val id: Int,
    val instanceCampaignId: Int,
    val presetUpgradeId: Int,
    val instanceHeroId: Int,
    val status: String,
    // Joined data from preset table
    val name: String,
    val isDisposable: Boolean
)