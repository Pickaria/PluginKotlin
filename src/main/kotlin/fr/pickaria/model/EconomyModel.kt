package fr.pickaria.model

import org.ktorm.database.Database
import org.ktorm.entity.*
import org.ktorm.schema.*
import java.util.*

interface Economy : Entity<Economy> {
	companion object : Entity.Factory<Economy>()

	var playerUniqueId: UUID
	var balance: Double
}

object EconomyModel : Table<Economy>("economy") {
	val playerUniqueId = uuid("player_uuid").primaryKey().bindTo { it.playerUniqueId }
	val balance = double("balance").bindTo { it.balance }
}

val Database.economy get() = this.sequenceOf(EconomyModel)