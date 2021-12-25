package fr.pickaria.jobs.jobs

import fr.pickaria.coins.Coin
import fr.pickaria.jobs.JobController
import fr.pickaria.jobs.JobEnum
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityBreedEvent

class Breeder: Listener {
	@EventHandler
	fun onEntityBreed(event: EntityBreedEvent) {
		val player = event.breeder as Player
		if (JobController.getJob(player.uniqueId)?.job != JobEnum.BREEDER.name) return

		Coin.dropCoin(event.entity.location, 1.0)
	}
}