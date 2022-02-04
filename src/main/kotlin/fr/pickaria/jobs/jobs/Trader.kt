package fr.pickaria.jobs.jobs

import fr.pickaria.Main
import fr.pickaria.jobs.JobEnum
import fr.pickaria.jobs.jobPayPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType

class Trader: Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	fun onInventoryClick(event: InventoryClickEvent) {
		val player = event.whoClicked as Player

		if (!event.isCancelled &&
			Main.jobController.hasJob(player.uniqueId, JobEnum.TRADER) &&
			event.inventory.type == InventoryType.MERCHANT &&
			event.slotType == InventoryType.SlotType.RESULT) {

			jobPayPlayer(player, 0.2, JobEnum.TRADER, 1)
		}
	}
}