package Classes

data class Campaign (
    val id: Int,
    val name: String,
    val description: String,
    var scenarioList: List<Scenario>

)