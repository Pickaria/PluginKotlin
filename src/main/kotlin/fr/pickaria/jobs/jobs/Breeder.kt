package fr.pickaria.jobs.jobs

import fr.pickaria.Main
import fr.pickaria.coins.Coin
import fr.pickaria.jobs.JobEnum
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityBreedEvent

class Breeder: Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	fun onEntityBreed(event: EntityBreedEvent) {
		val player = event.breeder as Player
		if (!Main.jobController.hasJob(player.uniqueId, JobEnum.BREEDER)) return

		Coin.dropCoin(event.entity.location, 1.0)
		Main.jobController.addExperienceAndAnnounce(player, JobEnum.BREEDER, 1)
	}
}