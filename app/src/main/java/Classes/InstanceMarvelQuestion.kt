package Classes

data class InstanceMarvelQuestion (
    val id: Int,
    val instanceScenarioId: Int,
    val presetQuestionId: Int,
    val text: String,
    var answer: String

)