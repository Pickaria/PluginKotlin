package fr.pickaria.jobs.jobs

import fr.pickaria.Main
import fr.pickaria.jobs.JobEnum
import fr.pickaria.jobs.jobPayPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityBreedEvent
import org.bukkit.event.entity.EntityTameEvent

class Breeder: Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	fun onEntityBreed(event: EntityBreedEvent) {
		if (event.breeder is Player) {
			val player = event.breeder as Player
			if (event.isCancelled || !Main.jobController.hasJob(player.uniqueId, JobEnum.BREEDER)) return

			jobPayPlayer(player, 0.2, JobEnum.BREEDER)
			Main.jobController.addExperienceAndAnnounce(player, JobEnum.BREEDER, 1)
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	fun onEntityTame(event: EntityTameEvent) {
		val player = event.owner as Player
		if (event.isCancelled || !Main.jobController.hasJob(player.uniqueId, JobEnum.BREEDER)) return

		jobPayPlayer(player, 0.3, JobEnum.BREEDER)
		Main.jobController.addExperienceAndAnnounce(player, JobEnum.BREEDER, 1)
	}
}