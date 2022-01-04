package fr.pickaria.jobs.jobs

import fr.pickaria.Main
import fr.pickaria.coins.Coin
import fr.pickaria.jobs.JobEnum
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityDeathEvent
import java.util.*


class Hunter: Listener {
	private val spawnerMobs = HashSet<UUID>()

	@EventHandler(priority = EventPriority.MONITOR)
	fun preventSpawnerCoin(event: CreatureSpawnEvent) {
		if (event.spawnReason != CreatureSpawnEvent.SpawnReason.SPAWNER && event.spawnReason != CreatureSpawnEvent.SpawnReason.SLIME_SPLIT) return
		spawnerMobs.add(event.entity.uniqueId)
	}

	private fun fromSpawner(entity: Entity): Boolean {
		return spawnerMobs.contains(entity.uniqueId)
	}

	private fun isHostile(entity: Entity?): Boolean {
		return (entity is Monster
				|| entity is Flying
				|| entity is Slime
				|| entity is Golem && entity !is Snowman
				|| entity is Wolf
				|| entity is Boss)
	}

	@EventHandler(priority = EventPriority.MONITOR)
	fun onEntityDeath(event: EntityDeathEvent) {
		spawnerMobs.remove(event.entity.uniqueId)
		if (!isHostile(event.entity) || fromSpawner(event.entity)) return
		val player = event.entity.killer ?: return
		if (!Main.jobController.hasJob(player.uniqueId, JobEnum.HUNTER)) return

		Coin.dropCoin(event.entity.location)
		Main.jobController.addExperienceAndAnnounce(player, JobEnum.HUNTER, 1)
	}
}