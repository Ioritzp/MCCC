package Classes

import java.io.Serializable
 enum class QuestionType{
     YES_NO,
     NUMBER_INPUT,
     TEXT_INPUT;


companion object {
    /**
     * Converts a string to a QuestionType, defaulting to TEXT_INPUT if the string is invalid.
     * This is a safe way to handle data from a database.
     * @param type The string to convert (e.g., "YES_NO").
     * @return The corresponding QuestionType.
     */
    fun fromString(type: String?): QuestionType {
        return try {
            // Find the enum constant matching the string (case-insensitive)
            enumValueOf<QuestionType>(type?.uppercase() ?: "")
        } catch (e: IllegalArgumentException) {
            // If the string doesn't match any enum, return a sensible default
            TEXT_INPUT
        }
    }
}
}

data class InstanceMarvelQuestion (
    var id: Int,
    val instanceScenarioId: Int,
    val presetQuestionId: Int,
    val text: String,
    var questionType: QuestionType,
    var answer: String

) : Serializable