package fr.pickaria.homes

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import fr.pickaria.Main
import fr.pickaria.model.Home
import fr.pickaria.model.home
import it.unimi.dsi.fastutil.Hash
import org.bukkit.Bukkit
import org.bukkit.Location
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.*
import java.sql.SQLException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap
import kotlin.math.ceil

class HomeController {
	companion object {
		private var cache: Cache<UUID, HashMap<String, Location>> = Caffeine.newBuilder()
			.expireAfterWrite(10, TimeUnit.MINUTES)
			.maximumSize(10000)
			.build()

		fun getHomeNames(uniqueId: UUID): List<String> {
			var map = cache.getIfPresent(uniqueId)

			if (map == null) {
				map = HashMap()

				Main.database.home
					.filter { it.playerUniqueId eq uniqueId }
					.forEach {
						val world = try {
							Bukkit.getWorld(it.world!!)
						} catch (_: NullPointerException) {
							null
						}

						val location = Location(world, it.x.toDouble(), it.y.toDouble(), it.z.toDouble())
						map[it.name] = location
					}

				cache.put(uniqueId, map)
			}

			return map.map { it.key }
		}

		fun getHomeByName(uniqueId: UUID, name: String): Location? {
			val map = cache.getIfPresent(uniqueId)

			if (map == null || map[name] == null) {
				val home = Main.database.home
					.find { (it.name eq name) and (it.playerUniqueId eq uniqueId) }!!

				val world = try {
					Bukkit.getWorld(home.world!!)
				} catch (_: NullPointerException) {
					null
				}

				return Location(world, home.x.toDouble(), home.y.toDouble(), home.z.toDouble())
			}

			return map[name]
		}

		fun removeHomeFromCache(uniqueId: UUID, name: String): Boolean {
			val map = cache.getIfPresent(uniqueId)
			if (map != null) {
				map.remove(name)
				cache.put(uniqueId, map)
			}

			return try {
				val home = Main.database.home
					.find { (it.name eq name) and (it.playerUniqueId eq uniqueId) }!!

				home.delete() > 0
			} catch (_: NullPointerException) {
				false
			}
		}

		fun addHomeToCache(uniqueId: UUID, homeName: String, location: Location): Boolean {
			val map = cache.getIfPresent(uniqueId) ?: HashMap()
			map[homeName] = location
			cache.put(uniqueId, map)

			val home = Home {
				playerUniqueId = uniqueId
				name = homeName
				world = location.world?.uid
				x = location.x.toInt()
				y = ceil(location.y).toInt()
				z = location.z.toInt()
			}

			return try {
				Main.database.home.add(home) > 0
			} catch (_: SQLException) {
				Main.database.home.update(home)
				true
			}
		}
	}
}