package fr.pickaria.jobs.jobs

import fr.pickaria.coins.Coin
import fr.pickaria.jobs.JobController
import fr.pickaria.jobs.JobEnum
import org.bukkit.Bukkit.getServer
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.inventory.BrewEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import java.util.*

class Alchemist: Listener {
	private fun isPotion(itemStack: ItemStack): Boolean {
		return if (itemStack.type == Material.POTION ||
			itemStack.type == Material.SPLASH_POTION ||
			itemStack.type == Material.LINGERING_POTION) {
			(itemStack.itemMeta as PotionMeta).basePotionData.type.effectType != null
		} else {
			false
		}
	}

	// Store ownership oof brewing stands
	private val brewingStands: MutableMap<Location, UUID> = mutableMapOf()

	@EventHandler
	fun onPlayerInteract(event: PlayerInteractEvent) {
		if (event.clickedBlock == null) return

		val uniqueId = event.player.uniqueId
		if (!JobController.hasJob(uniqueId, JobEnum.ALCHEMIST)) return

		brewingStands.putIfAbsent(event.clickedBlock!!.location, uniqueId)
	}

	@EventHandler
	fun onPlayerInteract(event: BlockBreakEvent) {
		brewingStands.remove(event.block.location)
	}

	@EventHandler
	fun onBrew(event: BrewEvent) {
		val location = event.block.location
		val uniqueId = brewingStands[location]
		if (uniqueId == null || !JobController.hasJob(uniqueId, JobEnum.ALCHEMIST)) return

		event.results.forEach {
			if (isPotion(it)) {
				Coin.dropCoin(location, 1.0, 2.0)
				val player = getServer().getOfflinePlayer(uniqueId) as Player
				JobController.addExperienceAndAnnounce(player, JobEnum.ALCHEMIST, 1)
			}
		}
	}
}