package fr.pickaria.jobs.jobs

import fr.pickaria.coins.Coin
import fr.pickaria.jobs.JobController
import fr.pickaria.jobs.JobEnum
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.MerchantInventory

class Trader: Listener {
	@EventHandler
	fun onInventoryClick(event: InventoryClickEvent) {
		val player = event.whoClicked as Player

		if (JobController.hasJob(player.uniqueId, JobEnum.TRADER) &&
			event.inventory.type == InventoryType.MERCHANT &&
			event.slotType == InventoryType.SlotType.RESULT) {

			val index = (event.inventory as MerchantInventory).selectedRecipeIndex

			Coin.dropCoin(event.inventory.location ?: player.location, 1.0, index + 1.0)
			JobController.addExperienceAndAnnounce(player, JobEnum.TRADER, 1)
		}
	}
}