package Classes

import java.time.LocalDateTime

data class InstanceHero (
    val id: Int,
    val presetHeroId: Int,
    val instanceCampaignId: Int,
    var credits: Int,
    val name: String,
    var currentLife: Int,
    val upgrades: List<InstanceUpgrade>,
    var modDate: LocalDateTime, //includes date and hour


)