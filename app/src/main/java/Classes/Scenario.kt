package Classes

data class Scenario (
    val id: Int,
    val name: String,
    val villainName: String,
    val description: String,
    var questionList: MutableList<Question> = mutableListOf()

)