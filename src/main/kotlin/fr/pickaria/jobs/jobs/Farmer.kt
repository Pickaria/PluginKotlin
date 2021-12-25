package fr.pickaria.jobs.jobs

import fr.pickaria.coins.Coin
import fr.pickaria.jobs.JobController
import fr.pickaria.jobs.JobEnum
import org.bukkit.block.data.Ageable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class Farmer: Listener {
	@EventHandler
	fun onBlockBreak(event: BlockBreakEvent) {
		if (event.block.blockData is Ageable) {
			val blockData = event.block.blockData as Ageable
			if (blockData.age == blockData.maximumAge) {
				if (JobController.getJob(event.player.uniqueId)?.job != JobEnum.FARMER.name) return

				Coin.dropCoin(event.block.location, 1.0)
			}
		}
	}
}