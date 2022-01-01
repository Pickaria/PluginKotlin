package fr.pickaria.utils

import fr.pickaria.model.Economy
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.ktorm.entity.Entity
import java.sql.SQLException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

interface Cache<V: Entity<V>>: Listener {
	val cache: ConcurrentHashMap<UUID, V>
		get() = ConcurrentHashMap()

	@EventHandler(priority = EventPriority.MONITOR)
	suspend fun onPlayerJoin(event: PlayerJoinEvent)

	@EventHandler(priority = EventPriority.MONITOR)
	suspend fun onPlayerQuit(event: PlayerQuitEvent) {
		cache[event.player.uniqueId]?.flushChanges()?.let {
			if (it > 0) {
				cache.remove(event.player.uniqueId)
			}
		}
	}

	fun flushAllAccounts(removeFromCache: Boolean = false, log: ((uuid: UUID, entity: V) -> Unit)? = null): Int {
		Bukkit.getLogger().info("Flushing all economy accounts...")
		var flushed = 0

		cache.forEach { (uuid, account) ->
			try {
				account.flushChanges()
				if (removeFromCache || !Bukkit.getServer().getOfflinePlayer(uuid).isOnline) {
					cache.remove(uuid)
				}
				flushed++
			} catch (err: SQLException) {
				err.printStackTrace()
				log?.invoke(uuid, account) ?: Bukkit.getLogger().severe("Cannot flush entity of $uuid")
			}
		}

		Bukkit.getLogger().info("Flushed $flushed economy accounts!")

		return flushed
	}

	 fun getFromCache(uniqueId: UUID): Economy?
}