package fr.pickaria.model

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.uuid
import org.ktorm.schema.varchar
import java.util.*

interface Home : Entity<Home> {
	companion object : Entity.Factory<Home>()

	var playerUniqueId: UUID
	var name: String
	var world: UUID?
	var x: Int
	var y: Int
	var z: Int
}

object HomeModel : Table<Home>("home") {
	val playerUniqueId = uuid("player_uuid").primaryKey().bindTo { it.playerUniqueId }
	val name = varchar("name").bindTo { it.name }
	val world = uuid("world").bindTo { it.world }
	val x = int("location_x").bindTo { it.x }
	val y = int("location_y").bindTo { it.y }
	val z = int("location_z").bindTo { it.z }
}

val Database.home get() = this.sequenceOf(HomeModel)