package fr.pickaria.jobs.jobs

import fr.pickaria.Main
import fr.pickaria.jobs.JobEnum
import fr.pickaria.jobs.jobPayPlayer
import org.bukkit.Bukkit.getServer
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
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
		if (!Main.jobController.hasJob(uniqueId, JobEnum.ALCHEMIST)) return

		brewingStands.putIfAbsent(event.clickedBlock!!.location, uniqueId)
	}

	@EventHandler
	fun onPlayerInteract(event: BlockBreakEvent) {
		brewingStands.remove(event.block.location)
	}

	@EventHandler(priority = EventPriority.MONITOR)
	fun onBrew(event: BrewEvent) {
		val location = event.block.location
		val uniqueId = brewingStands[location]
		if (event.isCancelled || uniqueId == null || !Main.jobController.hasJob(uniqueId, JobEnum.ALCHEMIST)) return

		event.results.forEach {
			if (isPotion(it)) {
				getServer().getPlayer(uniqueId)?.let { player ->
					jobPayPlayer(player, 0.15, JobEnum.ALCHEMIST, 1)
				}
			}
		}
	}
}