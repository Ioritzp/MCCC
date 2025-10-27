package Classes

import java.time.LocalDateTime

data class InstanceScenario (
    val id: Int,
    val instanceCampaignId: Int,
    val presetScenarioId: Int,
    val name: String,
    val villainName: String,
    val description: String,
    var questionList: List<InstanceMarvelQuestion>,
    var startDate: LocalDateTime,
    var endDate: LocalDateTime,
    var status: String

)