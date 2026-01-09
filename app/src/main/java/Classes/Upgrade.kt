package Classes


data class Upgrade (
    val id: Int,
    val name: String,
    val type: String,
    var isDisposable: Int,
    val cost: Int?

)