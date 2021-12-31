package fr.pickaria.jobs

import fr.pickaria.Main
import fr.pickaria.jobs.jobs.*
import fr.pickaria.model.Job
import fr.pickaria.model.job
import org.bukkit.Bukkit.getServer
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.ktorm.dsl.*
import org.ktorm.entity.*
import java.sql.SQLException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.hours
import kotlin.time.DurationUnit

class JobController(plugin: Main): Listener {
	init {
		getServer().pluginManager.registerEvents(this, plugin)

		getServer().pluginManager.registerEvents(Miner(), plugin)
		getServer().pluginManager.registerEvents(Hunter(), plugin)
		getServer().pluginManager.registerEvents(Farmer(), plugin)
		getServer().pluginManager.registerEvents(Breeder(), plugin)
		getServer().pluginManager.registerEvents(Alchemist(), plugin)
		getServer().pluginManager.registerEvents(Wizard(), plugin)
	}

	companion object {
		private val playerJobs: MutableMap<UUID, MutableMap<JobEnum, Job>> = mutableMapOf()
		const val MAX_JOBS = 1
		const val COOLDOWN = 24L

		fun getJob(playerUuid: UUID, jobName: JobEnum): Job {
			return try {
				playerJobs[playerUuid]!![jobName]!!
			} catch (_: NullPointerException) {
				val job = Main.database.job.find { (it.job eq jobName.name) and (it.playerUniqueId eq playerUuid) and it.active }!!
				val jobEnum = JobEnum.valueOf(job.job)

				playerJobs[playerUuid] = mutableMapOf(Pair(jobEnum, job))

				job
			}
		}

		fun hasJob(playerUuid: UUID, jobName: JobEnum): Boolean {
			if (playerJobs.contains(playerUuid)) {
				if (playerJobs[playerUuid]?.containsKey(jobName) == true) return true // Get from cache
				return false
			}

			return try {
				val job = Main.database.job.find { (it.job eq jobName.name) and (it.playerUniqueId eq playerUuid) and it.active }!!
				val jobEnum = JobEnum.valueOf(job.job)

				try {
					playerJobs[playerUuid]!![jobEnum] = job // Save in cache
				} catch (_: NullPointerException) {
					playerJobs[playerUuid] = mutableMapOf(Pair(jobEnum, job))
				}

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

			try {
				playerJobs[playerUuid]!![jobName] = job // Save in cache
			} catch (_: NullPointerException) {
				playerJobs[playerUuid] = mutableMapOf(Pair(jobName, job))
			}

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

			try {
				playerJobs[playerUuid]!!.remove(jobName) // Save in cache
			} catch (_: NullPointerException) {
				playerJobs[playerUuid] = mutableMapOf()
			}

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
				val job = getJob(playerUuid, jobName)
				val previousLevel = getLevelFromExperience(job.level)
				job.level += exp

				if (getLevelFromExperience(job.level) > previousLevel) {
					JobErrorEnum.NEW_LEVEL
				} else {
					JobErrorEnum.NOTHING
				}
			} catch (_: java.lang.NullPointerException) {
				JobErrorEnum.NOT_EXERCICE
			}
		}

		fun addExperienceAndAnnounce(player: Player, jobName: JobEnum, exp: Int): JobErrorEnum {
			val res = addExperience(player.uniqueId, jobName, exp);

			if (res == JobErrorEnum.NEW_LEVEL) {
				val job = getJob(player.uniqueId, jobName)
				player.sendMessage("§7Vous montez niveau §6${getLevelFromExperience(job.level)}§7 dans le métier §6${jobName.label}§7.")
			}

			return res
		}
	}
}