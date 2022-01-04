package fr.pickaria.jobs

import com.github.shynixn.mccoroutine.registerSuspendingEvents
import fr.pickaria.Main
import fr.pickaria.asyncFlushChanges
import fr.pickaria.jobs.jobs.*
import fr.pickaria.model.Job
import fr.pickaria.model.job
import fr.pickaria.utils.DoubleCache
import org.bukkit.Bukkit.getServer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.ktorm.dsl.*
import org.ktorm.entity.*
import java.sql.SQLException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.hours
import kotlin.time.DurationUnit

class JobController(plugin: Main) : Listener, DoubleCache<JobEnum, Job> {
	companion object {
		const val MAX_JOBS = 1
		const val COOLDOWN = 24L
	}

	override val cache: ConcurrentHashMap<UUID, ConcurrentHashMap<JobEnum, Job>> = ConcurrentHashMap()

	init {
		getServer().pluginManager.registerSuspendingEvents(this, plugin)

		getServer().pluginManager.registerEvents(Miner(), plugin)
		getServer().pluginManager.registerEvents(Hunter(), plugin)
		getServer().pluginManager.registerEvents(Farmer(), plugin)
		getServer().pluginManager.registerEvents(Breeder(), plugin)
		getServer().pluginManager.registerEvents(Alchemist(), plugin)
		getServer().pluginManager.registerEvents(Wizard(), plugin)
		getServer().pluginManager.registerEvents(Trader(), plugin)
	}

	@EventHandler
	override suspend fun onPlayerJoin(event: PlayerJoinEvent) {
		getFromCache(event.player.uniqueId)
	}

	override fun getFromCache(uniqueId: UUID, key: JobEnum): Job? =
		cache[uniqueId]?.get(key)
			?: Main.database.job.find { (it.playerUniqueId eq uniqueId) and (it.job eq key.name) }?.let {
				println("Requesting database")
				cache[uniqueId]?.set(key, it) ?: run {
					cache[uniqueId] = ConcurrentHashMap()
					cache[uniqueId]?.put(key, it)
				}
				cache[uniqueId]?.get(key)
			}

	override fun getFromCache(uniqueId: UUID): ConcurrentHashMap<JobEnum, Job>? {
		return if (cache.containsKey(uniqueId)) {
			cache[uniqueId]
		} else {
			println("Requesting database")
			val jobs = ConcurrentHashMap<JobEnum, Job>()

			Main.database.job.filter { it.playerUniqueId eq uniqueId }
				.forEach {
					jobs[JobEnum.valueOf(it.job)] = it
				}

			cache.put(uniqueId, jobs)
		}
	}

	fun hasJob(playerUuid: UUID, jobName: JobEnum): Boolean {
		val job = getFromCache(playerUuid, jobName)
		return job != null && job.active
	}

	fun jobCount(playerUuid: UUID): Int {
		return getFromCache(playerUuid)?.filter { it.value.active }?.size ?: 0
	}

	fun getCooldown(playerUuid: UUID, jobName: JobEnum): Int {
		val previousDay = LocalDateTime.now().minusHours(COOLDOWN)

		val job = getFromCache(playerUuid, jobName)
		return if (job == null || !job.active) {
			0
		} else {
			val local = LocalDateTime.ofInstant(job.lastUsed, ZoneOffset.systemDefault())
			previousDay.until(local, ChronoUnit.HOURS).hours.toInt(DurationUnit.HOURS)
		}
	}

	fun joinJob(playerUuid: UUID, jobName: JobEnum) {
		getFromCache(playerUuid, jobName)?.let {
			it.active = true
			it.lastUsed = Instant.now()
		} ?: run {
			val job = Job {
				playerUniqueId = playerUuid
				job = jobName.name
				active = true
				lastUsed = Instant.now()
			}

			cache[playerUuid]?.set(jobName, job) ?: run {
				cache[playerUuid] = ConcurrentHashMap()
				cache[playerUuid]?.set(jobName, job)
			}
		}
	}

	fun leaveJob(playerUuid: UUID, jobName: JobEnum) {
		getFromCache(playerUuid, jobName)?.let {
			it.active = false
			it.level = (it.level * 0.8).toInt()
		}
	}

	fun getExperienceFromLevel(level: Int): Int {
		return ((level.toDouble().pow(2.0) + level) / 2).toInt()
	}

	fun getLevelFromExperience(experience: Int): Int {
		return floor(0.5 * (sqrt(8.0 * experience + 1.0) - 1)).toInt()
	}

	fun addExperience(playerUuid: UUID, jobName: JobEnum, exp: Int): JobErrorEnum {
		return getFromCache(playerUuid, jobName)?.let {
			val previousLevel = getLevelFromExperience(it.level)
			it.level += exp

			if (getLevelFromExperience(it.level) > previousLevel) {
				JobErrorEnum.NEW_LEVEL
			} else {
				JobErrorEnum.NOTHING
			}
		} ?: JobErrorEnum.NOTHING
	}

	fun addExperienceAndAnnounce(player: Player, jobName: JobEnum, exp: Int): JobErrorEnum {
		val res = addExperience(player.uniqueId, jobName, exp)

		if (res == JobErrorEnum.NEW_LEVEL) {
			val job = getFromCache(player.uniqueId, jobName)!!
			player.sendMessage("§7Vous montez niveau §6${getLevelFromExperience(job.level)}§7 dans le métier §6${jobName.label}§7.")
		}

		return res
	}
}
