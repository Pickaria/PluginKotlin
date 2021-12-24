package fr.pickaria.model

import org.ktorm.database.Database
import org.ktorm.entity.*
import org.ktorm.schema.*
import java.time.Instant
import java.util.*

interface Job : Entity<Job> {
	companion object : Entity.Factory<Job>()

	var playerUniqueId: UUID
	var job: String
	var level: Int
	var lastUsed: Instant
	var active: Boolean
}

object JobModel : Table<Job>("job") {
	val playerUniqueId = uuid("player_uuid").primaryKey().bindTo { it.playerUniqueId }
	val job = varchar("job").primaryKey().bindTo { it.job }
	val level = int("level").bindTo { it.level }
	val lastUsed = timestamp("last_used").bindTo { it.lastUsed }
	val active = boolean("active").bindTo { it.active }
}

val Database.job get() = this.sequenceOf(JobModel)