package Classes

import java.io.Serializable
 enum class QuestionType{
     YES_NO,
     NUMBER_INPUT,
     TEXT_INPUT
 }

data class InstanceMarvelQuestion (
    var id: Int,
    val instanceScenarioId: Int,
    val presetQuestionId: Int,
    val text: String,
    var questionType: QuestionType,
    var answer: String

) : Serializable