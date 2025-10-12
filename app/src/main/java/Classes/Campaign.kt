package Classes

data class Campaign (
    val id: Int,
    val name: String,
    var scenarioList: MutableList<Scenario> = mutableListOf()

)