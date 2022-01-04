package fr.pickaria.utils

import fr.pickaria.Main
import fr.pickaria.model.job
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.ktorm.entity.Entity
import org.ktorm.entity.add
import java.sql.SQLException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

interface DoubleCache<K, V : Entity<V>> {
	val cache: ConcurrentHashMap<UUID, ConcurrentHashMap<K, V>>

	@EventHandler(priority = EventPriority.MONITOR)
	suspend fun onPlayerJoin(event: PlayerJoinEvent)

	@EventHandler(priority = EventPriority.MONITOR)
	suspend fun onPlayerQuit(event: PlayerQuitEvent) {
		cache[event.player.uniqueId]?.let { playerCache ->
			playerCache.forEach {
				it.value.flushChanges()
			}
		}

		cache.remove(event.player.uniqueId)
	}

	fun flushAllEntities(removeFromCache: Boolean = false, log: ((uuid: UUID, entity: V) -> Unit)? = null): Int {
		Bukkit.getLogger().info("Flushing all entities...")
		var flushed = 0

		cache.forEach { (uuid, account) ->
			account.forEach {
				try {
					it.value.flushChanges()
					if (removeFromCache || !Bukkit.getServer().getOfflinePlayer(uuid).isOnline) {
						cache.remove(uuid)
					}
					flushed++
				} catch (err: SQLException) {
					err.printStackTrace()
					log?.invoke(uuid, it.value) ?: Bukkit.getLogger().severe("Cannot flush entity of $uuid")
				}
			}
		}

		Bukkit.getLogger().info("Flushed $flushed entities!")

		return flushed
	}

	fun getFromCache(uniqueId: UUID): ConcurrentHashMap<K, V>?
	fun getFromCache(uniqueId: UUID, key: K): V?
}