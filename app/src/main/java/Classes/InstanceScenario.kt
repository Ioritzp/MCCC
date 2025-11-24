package Classes


import java.time.LocalDateTime
import java.io.Serializable


data class InstanceScenario (
    var id: Int,
    var instanceCampaignId: Int,
    val presetScenarioId: Int,
    val name: String,
    val villainName: String,
    val description: String,
    var questionList: List<InstanceMarvelQuestion>,
    var startDate: LocalDateTime?,
    var endDate: LocalDateTime?,
    var status: String

) : Serializable