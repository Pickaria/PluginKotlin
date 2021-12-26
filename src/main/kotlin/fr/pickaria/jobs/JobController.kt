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
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt
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
		private val playerJobs = HashMap<UUID, JobEnum>()
		const val MAX_JOBS = 1
		const val COOLDOWN = 24L

		fun hasJob(playerUuid: UUID, jobName: JobEnum): Boolean {
			if (playerJobs.contains(playerUuid)) {
				if (playerJobs[playerUuid] == jobName) return true // Get from cache
				return false
			}

			return try {
				val job = Main.database.job.find { (it.job eq jobName.name) and (it.playerUniqueId eq playerUuid) }!!
				playerJobs[playerUuid] = JobEnum.valueOf(job.job) // Save in cache
				job.active
			} catch (_: NullPointerException) {
				false
			}
		}

		fun getJobs(playerUuid: UUID): List<Job> {
			return Main.database.job.filter { it.playerUniqueId eq playerUuid and it.active }.toList()
		}

		fun jobCount(playerUuid: UUID): Int {
			return Main.database.job.filter { it.playerUniqueId eq playerUuid and it.active }.totalRecords
		}

		fun getCooldown(playerUuid: UUID, jobName: JobEnum): Int {
			val previousDay = LocalDateTime.now().minusHours(COOLDOWN)

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

			playerJobs.remove(playerUuid) // Invalidate cache

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

			playerJobs.remove(playerUuid) // Invalidate cache

			return if (job.flushChanges() > 0) {
				JobErrorEnum.JOB_LEFT
			} else {
				JobErrorEnum.UNKNOWN
			}
		}

		fun getExperienceFromLevel(level: Int): Int {
			return ((level.toDouble().pow(2.0) + level) / 2).toInt()
		}

		fun getLevelFromExperience(experience: Int): Int {
			return floor(0.5 * (sqrt(8.0 * experience + 1.0) - 1)).toInt()
		}

		fun addExperience(playerUuid: UUID, jobName: JobEnum, exp: Int): JobErrorEnum {
			return try {
				val job = Main.database.job.find {
					(it.job eq jobName.name) and (it.playerUniqueId eq playerUuid) and it.active
				}!!
				val previousLevel = getLevelFromExperience(job.level)
				job.level += exp
				job.flushChanges()
				if (getLevelFromExperience(job.level + exp) > previousLevel) {
					println(previousLevel)
					println(getLevelFromExperience(job.level + exp))
					JobErrorEnum.NEW_LEVEL
				} else {
					JobErrorEnum.NOTHING
				}
			} catch (_: java.lang.NullPointerException) {
				JobErrorEnum.NOT_EXERCICE
			}
		}
	}
}