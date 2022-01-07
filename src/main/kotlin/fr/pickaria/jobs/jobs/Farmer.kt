package fr.pickaria.jobs.jobs

import fr.pickaria.Main
import fr.pickaria.coins.Coin
import fr.pickaria.jobs.JobEnum
import org.bukkit.Material
import org.bukkit.block.data.Ageable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class Farmer : Listener {
	private val materials = mapOf(
		Material.WHEAT to 1.0,
		Material.CARROTS to 1.0,
		Material.POTATOES to 1.0,
		Material.BEETROOTS to 1.0,
	)

	@EventHandler(priority = EventPriority.MONITOR)
	fun onBlockBreak(event: BlockBreakEvent) {
		if (!Main.jobController.hasJob(event.player.uniqueId, JobEnum.FARMER)) return

		val material = event.block.type
		if (materials.containsKey(material)) {
			val blockData = event.block.blockData as Ageable
			if (blockData.age == blockData.maximumAge) {
				Coin.dropCoin(event.block.location)
				Main.jobController.addExperienceAndAnnounce(event.player, JobEnum.FARMER, 1)
			}
		}
	}

	/*@EventHandler(priority = EventPriority.MONITOR)
	fun onPlayerInteract(event: PlayerInteractEvent) {
		if (!Main.jobController.hasJob(event.player.uniqueId, JobEnum.FARMER)) return
		val block = event.clickedBlock
		if (block != null) {
			if (block.type == Material.SWEET_BERRY_BUSH) {
				if (block.blockData is Ageable) {
					val blockData = block.blockData as Ageable
					if (blockData.age == blockData.maximumAge) {
						Coin.dropCoin(block.location, 1.0)
					}
				}
			}
		}
	}*/
}