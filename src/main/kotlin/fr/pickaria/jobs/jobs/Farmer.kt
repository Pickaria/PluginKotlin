package fr.pickaria.jobs.jobs

import fr.pickaria.Main
import fr.pickaria.jobs.JobEnum
import fr.pickaria.jobs.jobPayPlayer
import org.bukkit.Material
import org.bukkit.block.data.Ageable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.world.StructureGrowEvent

class Farmer : Listener {
	private val crops = mapOf(
		// Ageable
		Material.WHEAT to 0.2,
		Material.CARROTS to 0.2,
		Material.POTATOES to 0.2,
		Material.BEETROOTS to 0.2,
		Material.COCOA_BEANS to 0.2,

		Material.SUGAR_CANE to 0.2,
		Material.BAMBOO to 0.2,

		/*Material.PUMPKIN to 0.1,
		Material.MELON to 0.1,

		Material.MUSHROOM_STEM to 0.1,
		Material.RED_MUSHROOM_BLOCK to 0.1,
		Material.BROWN_MUSHROOM_BLOCK to 0.1,*/
	)

	@EventHandler(priority = EventPriority.MONITOR)
	fun onBlockBreak(event: BlockBreakEvent) {
		if (event.isCancelled) return
		if (!Main.jobController.hasJob(event.player.uniqueId, JobEnum.FARMER)) return

		crops[event.block.type]?.let {
			(event.block.blockData as? Ageable)?.let { blockData ->
				if (blockData.age == blockData.maximumAge) {
					jobPayPlayer(event.player, it, JobEnum.FARMER)
					Main.jobController.addExperienceAndAnnounce(event.player, JobEnum.FARMER, 1)
				}
			} ?: run {
				jobPayPlayer(event.player, it, JobEnum.FARMER)
				Main.jobController.addExperienceAndAnnounce(event.player, JobEnum.FARMER, 1)
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	fun onStructureGrow(event: StructureGrowEvent) {
		if (event.isCancelled) return

		event.player?.let {
			if (!Main.jobController.hasJob(it.uniqueId, JobEnum.FARMER)) return
			if (event.isFromBonemeal) {
				jobPayPlayer(it, 0.05, JobEnum.FARMER)
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