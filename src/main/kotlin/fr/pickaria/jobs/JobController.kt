package fr.pickaria.jobs

import fr.pickaria.Main
import fr.pickaria.jobs.jobs.Breeder
import fr.pickaria.jobs.jobs.Farmer
import fr.pickaria.jobs.jobs.Hunter
import fr.pickaria.jobs.jobs.Miner
import fr.pickaria.model.Job
import fr.pickaria.model.job
import org.bukkit.Bukkit.getServer
import org.ktorm.dsl.*
import org.ktorm.entity.*
import java.sql.SQLException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.HashMap
import kotlin.time.Duration.Companion.hours
import kotlin.time.DurationUnit

class JobController(plugin: Main) {
	init {
		getServer().pluginManager.registerEvents(Miner(), plugin)
		getServer().pluginManager.registerEvents(Hunter(), plugin)
		getServer().pluginManager.registerEvents(Farmer(), plugin)
		getServer().pluginManager.registerEvents(Breeder(), plugin)
	}

	companion object {
		private val playerJobs = HashMap<UUID, Job>()
		const val MAX_JOBS = 1

		/**
		 * Get the currently active job of the user
		 */
		@Deprecated("Use hasJob() or getJobs()")
		fun getJob(playerUuid: UUID): Job? {
			// Get from cache
			/*if (playerJobs.contains(playerUuid)) {
				return playerJobs[playerUuid]
			}*/

			val job = Main.database.job.find { it.playerUniqueId eq playerUuid and it.active }

			return if (job != null) {
				// Save job in cache
				playerJobs[playerUuid] = job
				job
			} else {
				null
			}
		}

		/**
		 * @return -3 : Unknown error
		 * @return -2 : You already have this job
		 * @return -1 : Success
		 * @return Anything above or equal to 0 : you must wait this amount of time before changing job
		 */
		@Deprecated("Use joinJob() and leftJob()")
		fun changeJob(playerUuid: UUID, newJob: JobEnum): JobErrorEnum {
			// 1. check if previous job was joined less than a day ago

			// 2. when a player join the job, divide its previous level by 20% if any || set it to 0
			// keep all previous jobs in the database
			// 3. set previous as not "active" = false
			val previousDay = LocalDateTime.now().minusDays(1)

			getJob(playerUuid)?.let { job ->
				val local = LocalDateTime.ofInstant(job.lastUsed, ZoneOffset.systemDefault())
				if (job.job == newJob.name) {
					return JobErrorEnum.ALREADY // Already have this job
				} else if (local > previousDay) {
					// Must wait before changing job
					val cooldown = previousDay.until(local, ChronoUnit.HOURS).hours.toInt(DurationUnit.HOURS)
					return JobErrorEnum.COOLDOWN
				} else {
					// UPDATE job SET active = false, level = level * 0.8 WHERE player_uuid = '$playerUuid' AND active = true
					job.active = false
					job.level = (job.level * 0.8).toInt()
					job.flushChanges()
				}
			}

			// Invalidate job in cache
			playerJobs.remove(playerUuid)

			return if (joinJob(playerUuid, newJob)) JobErrorEnum.JOB_JOINED else JobErrorEnum.UNKNOWN
		}

		fun hasJob(playerUuid: UUID, jobName: JobEnum): Boolean {
			val job = Main.database.job.find { (it.job eq jobName.name) and (it.playerUniqueId eq playerUuid) }
			return job != null && job.active
		}

		fun getJobs(playerUuid: UUID): List<Job> {
			return Main.database.job.filter { it.playerUniqueId eq playerUuid and it.active }.toList()
		}

		fun jobCount(playerUuid: UUID): Int {
			return Main.database.job.filter { it.playerUniqueId eq playerUuid and it.active }.totalRecords
		}

		fun getCooldown(playerUuid: UUID, jobName: JobEnum): Int {
			val previousDay = LocalDateTime.now().minusDays(1)

			val job = Main.database.job.find { (it.job eq jobName.name) and (it.playerUniqueId eq playerUuid) }
			return if (job == null || !job.active) {
				0
			} else {
				val local = LocalDateTime.ofInstant(job.lastUsed, ZoneOffset.systemDefault())
				previousDay.until(local, ChronoUnit.HOURS).hours.toInt(DurationUnit.HOURS)
			}
		}

		fun joinJob(playerUuid: UUID, jobName: JobEnum): Boolean {
			val job = Job {
				playerUniqueId = playerUuid
				job = jobName.name
				active = true
				lastUsed = Instant.now()
			}

			// Set current as "active" = true
			// try {
			// 	 insert into job (player_uuid, job, active) values ('$playerUuid', '${newJob.name}', true)
			// } catch {
			// 	  on conflict (player_uuid, job) do update set active = true, last_used = now()
			// }

			return try {
				Main.database.job.add(job) > 0
			} catch (e: SQLException) {
				Main.database.job.update(job) > 0
			}
		}

		fun leaveJob(playerUuid: UUID, jobName: JobEnum): JobErrorEnum {
			val job = Main.database.job.find { (it.job eq jobName.name) and (it.playerUniqueId eq playerUuid) }
			if (job == null || !job.active) {
				return JobErrorEnum.NOT_EXERCICE
			}

			if (getCooldown(playerUuid, jobName) > 0) {
				return JobErrorEnum.COOLDOWN
			}

			job.active = false
			job.level = (job.level * 0.8).toInt()

			return if (job.flushChanges() > 0) {
				JobErrorEnum.JOB_LEFT
			} else {
				JobErrorEnum.UNKNOWN
			}
		}
	}
}