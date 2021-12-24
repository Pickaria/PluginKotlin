package fr.pickaria.jobs

import fr.pickaria.Main
import fr.pickaria.jobs.jobs.Miner
import fr.pickaria.model.Job
import fr.pickaria.model.job
import org.bukkit.Bukkit.getServer
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.find
import org.ktorm.entity.update
import java.sql.SQLException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.time.Duration.Companion.hours
import kotlin.time.DurationUnit

class JobController(plugin: Main) {
	init {
		getServer().pluginManager.registerEvents(Miner(), plugin)
	}

	companion object {
		/**
		 * Get the currently active job of the user
		 */
		fun getJob(playerUuid: UUID): Job? {
			return Main.database.job.find { it.playerUniqueId eq playerUuid and it.active }
		}

		/**
		 * @return -2 : You already have this job
		 * @return -1 : Success
		 * @return Anything above or equal to 0 : you must wait this amount of time before changing job
		 */
		fun changeJob(playerUuid: UUID, newJob: JobEnum): Int {
			// 1. check if previous job was joined less than a day ago

			// 2. when a player join the job, divide its previous level by 20% if any || set it to 0
			// keep all previous jobs in the database
			// 3. set previous as not "active" = false
			val previousDay = LocalDateTime.now().minusDays(1)

			getJob(playerUuid)?.let { job ->
				val local = LocalDateTime.ofInstant(job.lastUsed, ZoneOffset.UTC)
				if (job.job == newJob.name) {
					return -2
				} else if (local > previousDay) {
					return previousDay.until(local, ChronoUnit.HOURS).hours.toInt(DurationUnit.HOURS)
				} else {
					// executeSQL("UPDATE job SET active = false, level = level * 0.8 WHERE player_uuid = '$playerUuid' AND active = true")
					job.active = false
					job.level = (job.level * 0.8).toInt()
					job.flushChanges()
				}
			}

			val job = Job {
				playerUniqueId = playerUuid
				job = newJob.name
				active = true
				lastUsed = Instant.now()
			}

			// 4. set current as "active" = true
			//executeSQL("insert into job (player_uuid, job, active) values ('$playerUuid', '${newJob.name}', true) on conflict (player_uuid, job) do update set active = true, last_used = now()")

			return try {
				val insert = Main.database.job.add(job)
				-1
			} catch (e: SQLException) {
				Main.database.job.update(job)
				-1
			}
		}
	}
}