package fr.pickaria.jobs

import com.github.shynixn.mccoroutine.registerSuspendingEvents
import fr.pickaria.Main
import fr.pickaria.jobs.jobs.*
import fr.pickaria.model.Job
import fr.pickaria.model.job
import fr.pickaria.utils.DoubleCache
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getServer
import org.bukkit.Sound
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.ktorm.dsl.*
import org.ktorm.entity.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

class JobController(private val plugin: Main) : Listener, DoubleCache<JobEnum, Job> {
	companion object {
		const val MAX_JOBS = 1
		const val COOLDOWN = 1L
	}

	override val cache: ConcurrentHashMap<UUID, ConcurrentHashMap<JobEnum, Job>> = ConcurrentHashMap()
	private val bossBars: ConcurrentHashMap<Player, BossBar> = ConcurrentHashMap()
	private val bossBarsTasks: ConcurrentHashMap<BossBar, BukkitTask> = ConcurrentHashMap()

	init {
		getServer().pluginManager.run {
			registerSuspendingEvents(this@JobController, plugin)

			registerEvents(Miner(), plugin)
			registerEvents(Hunter(), plugin)
			registerEvents(Farmer(), plugin)
			registerEvents(Breeder(), plugin)
			registerEvents(Alchemist(), plugin)
			registerEvents(Wizard(), plugin)
			registerEvents(Trader(), plugin)
		}

		// Write cache to database every 10 minutes
		object : BukkitRunnable() {
			override fun run() {
				flushAllEntities { uuid, account ->
					Bukkit.getLogger().severe("Cannot flush job ${account.job} of $uuid with level : ${account.level}")
				}
			}
		}.runTaskTimerAsynchronously(plugin, 12000, 12000 /* 10 minutes */)
	}

	@EventHandler
	override suspend fun onPlayerJoin(event: PlayerJoinEvent) {
		getFromCache(event.player.uniqueId)
	}

	@EventHandler
	override suspend fun onPlayerQuit(event: PlayerQuitEvent) {
		super.onPlayerQuit(event)
		bossBars.remove(event.player)
	}

	override fun getFromCache(uniqueId: UUID, key: JobEnum): Job? =
		cache[uniqueId]?.get(key)
			?: Main.database.job.find { (it.playerUniqueId eq uniqueId) and (it.job eq key.name) }?.let {
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
			val jobs = ConcurrentHashMap<JobEnum, Job>()

			Main.database.job.filter { it.playerUniqueId eq uniqueId }
				.forEach {
					jobs[JobEnum.valueOf(it.job)] = it
				}

			cache[uniqueId] = jobs
			cache[uniqueId]
		}
	}

	fun hasJob(playerUuid: UUID, jobName: JobEnum): Boolean {
		val job = getFromCache(playerUuid, jobName)
		return job?.active == true
	}

	fun jobCount(playerUuid: UUID): Int = getFromCache(playerUuid)?.filter { it.value.active }?.size ?: 0

	fun getCooldown(playerUuid: UUID, jobName: JobEnum): Int {
		val previousDay = LocalDateTime.now().minusHours(COOLDOWN)

		val job = getFromCache(playerUuid, jobName)
		return if (job == null || !job.active) {
			0
		} else {
			val local = LocalDateTime.ofInstant(job.lastUsed, ZoneOffset.systemDefault())
			previousDay.until(local, ChronoUnit.MINUTES).minutes.toInt(DurationUnit.MINUTES)
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

			Main.database.job.add(job)

			cache[playerUuid]?.set(jobName, job) ?: run {
				cache[playerUuid] = ConcurrentHashMap()
				cache[playerUuid]?.set(jobName, job)
			}
		}
	}

	fun leaveJob(playerUuid: UUID, jobName: JobEnum) {
		getFromCache(playerUuid, jobName)?.let {
			it.active = false
		}
	}

	private fun getExperienceFromLevel(job: JobEnum, level: Int): Int {
		return if (level >= 0) {
			ceil(job.startExperience * job.experiencePercentage.pow(level) + level * job.mult).toInt()
		} else {
			0
		}
	}

	fun getLevelFromExperience(job: JobEnum, experience: Int): Int {
		var level = 0
		var levelExperience = job.startExperience.toDouble()
		while ((levelExperience + level * job.mult) < experience && level < 100) {
			levelExperience *= job.experiencePercentage
			level++
		}
		return level
	}

	private fun addExperience(playerUuid: UUID, jobName: JobEnum, exp: Int): JobErrorEnum {
		return getFromCache(playerUuid, jobName)?.let {
			val previousLevel = getLevelFromExperience(jobName, it.level)
			it.level += exp
			val newLevel = getLevelFromExperience(jobName, it.level)

			if (newLevel > previousLevel) {
				JobErrorEnum.NEW_LEVEL
			} else {
				JobErrorEnum.NOTHING
			}
		} ?: JobErrorEnum.NOTHING
	}

	fun addExperienceAndAnnounce(player: Player, jobName: JobEnum, exp: Int): JobErrorEnum {
		return addExperience(player.uniqueId, jobName, exp).also {
			val job = getFromCache(player.uniqueId, jobName)!!
			val experience = job.level
			val level = getLevelFromExperience(jobName, experience)
			val currentLevelExperience = getExperienceFromLevel(jobName, level - 1)
			val nextLevelExperience = getExperienceFromLevel(jobName, level)
			val levelDiff = abs(nextLevelExperience - currentLevelExperience)
			val diff = abs(experience - currentLevelExperience)

			val bossBar: BossBar = bossBars[player] ?: run {
				val bossBar = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SOLID)
				bossBar.addPlayer(player)
				bossBars[player] = bossBar
				bossBar
			}

			bossBar.setTitle("${jobName.label} | Niveau $level ($experience / $nextLevelExperience)")
			bossBar.isVisible = true
			bossBar.progress = (diff / levelDiff.toDouble()).coerceAtLeast(0.0).coerceAtMost(1.0)

			bossBarsTasks[bossBar]?.cancel()

			bossBarsTasks[bossBar] = object : BukkitRunnable() {
				override fun run() {
					bossBar.isVisible = false
				}
			}.runTaskLater(plugin, 80)

			if (it == JobErrorEnum.NEW_LEVEL) {
				player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f)
				player.sendMessage("§7Vous montez niveau §6$level§7 dans le métier §6${jobName.label}§7.")
			}
		}
	}
}
