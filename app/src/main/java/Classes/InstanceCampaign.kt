package Classes

import java.time.LocalDateTime

data class InstanceCampaign (
    val id: Int,
    var presetCampaignId: Int,
    val name: String,
    val description: String,
    var scenarioList: List<InstanceScenario>,
    var userId: Int,
    var userName: String,
    var playerNum: Int,
    var startDate: LocalDateTime,
    var endDate: LocalDateTime,
    var difficulty: String



)