package fr.pickaria.teleport

import fr.pickaria.Main
import fr.pickaria.model.Home
import fr.pickaria.model.home
import fr.pickaria.utils.DoubleCache
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.*
import java.sql.SQLException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.ceil

class HomeController : Listener, DoubleCache<String, Home> {
	override val cache: ConcurrentHashMap<UUID, ConcurrentHashMap<String, Home>> = ConcurrentHashMap()

	override suspend fun onPlayerJoin(event: PlayerJoinEvent) {
		TODO("Not yet implemented")
	}

	override fun getFromCache(uniqueId: UUID): ConcurrentHashMap<String, Home>? {
		return if (cache.containsKey(uniqueId)) {
			cache[uniqueId]
		} else {
			val homes = ConcurrentHashMap<String, Home>()

			Main.database.home
				.filter { (it.playerUniqueId eq uniqueId) }
				.forEach {
					homes[it.name] = it
				}

			cache[uniqueId] = homes
			cache[uniqueId]
		}
	}

	override fun getFromCache(uniqueId: UUID, key: String): Home? =
		cache[uniqueId]?.get(key)
			?: Main.database.home.find { (it.playerUniqueId eq uniqueId) and (it.name eq key) }?.let {
				cache[uniqueId]?.set(key, it) ?: run {
					cache[uniqueId] = ConcurrentHashMap()
					cache[uniqueId]?.put(key, it)
				}
				cache[uniqueId]?.get(key)
			}

	fun getHomeNames(uniqueId: UUID): List<String> {
		return getFromCache(uniqueId)?.map {
			it.key
		} ?: listOf()
	}

	fun getHomeByName(uniqueId: UUID, name: String): Location? {
		val home = getFromCache(uniqueId, name)

		return if (home != null) {
			val world = try {
				Bukkit.getWorld(home.world!!)
			} catch (_: NullPointerException) {
				null
			}

			return Location(world, home.x.toDouble(), home.y.toDouble(), home.z.toDouble())
		} else {
			null
		}
	}

	fun removeHome(uniqueId: UUID, name: String): Boolean {
		return try {
			val playerCache = cache[uniqueId]!!
			val home = playerCache[name]!!
			if (home.delete() > 0) {
				playerCache.remove(name)
				return true
			}
			false
		} catch (_: NullPointerException) {
			false
		}
	}

	fun addHome(uniqueId: UUID, homeName: String, location: Location): Boolean {
		val home = Home {
			playerUniqueId = uniqueId
			name = homeName
			world = location.world?.uid
			x = location.x.toInt()
			y = ceil(location.y).toInt()
			z = location.z.toInt()
		}

		cache[uniqueId]?.let {
			it[homeName] = home
		} ?: run {
			val homes = ConcurrentHashMap<String, Home>()
			homes[homeName] = home
			cache[uniqueId] = homes
		}

		return try {
			Main.database.home.add(home) > 0
		} catch (_: SQLException) {
			Main.database.home.update(home)
			true
		}
	}
}