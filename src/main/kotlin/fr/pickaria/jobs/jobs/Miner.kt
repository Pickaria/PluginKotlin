package fr.pickaria.jobs.jobs

import fr.pickaria.coins.Coin.Companion.dropCoin
import fr.pickaria.jobs.JobController
import fr.pickaria.jobs.JobEnum
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class Miner: Listener {
	private val materials = listOf(
		Material.COAL_ORE,
		Material.COPPER_ORE,
		Material.IRON_ORE,
		Material.GOLD_ORE,
		Material.DIAMOND_ORE,
		Material.EMERALD_ORE,
		Material.REDSTONE_ORE,
		Material.LAPIS_ORE,

		Material.DEEPSLATE_COAL_ORE,
		Material.DEEPSLATE_COPPER_ORE,
		Material.DEEPSLATE_IRON_ORE,
		Material.DEEPSLATE_GOLD_ORE,
		Material.DEEPSLATE_DIAMOND_ORE,
		Material.DEEPSLATE_EMERALD_ORE,
		Material.DEEPSLATE_REDSTONE_ORE,
		Material.DEEPSLATE_LAPIS_ORE,

		Material.NETHER_QUARTZ_ORE,
		Material.NETHER_GOLD_ORE
	)

	@EventHandler(priority = EventPriority.MONITOR)
	fun onBlockBreak(e: BlockBreakEvent) {
		if (JobController.hasJob(e.player.uniqueId, JobEnum.MINER)) {
			if (materials.contains(e.block.type)) {
				// check if player is using silk touch
				val itemInHand = e.player.inventory.itemInMainHand
				if (!itemInHand.enchantments.contains(Enchantment.SILK_TOUCH) && e.block.getDrops(itemInHand).isNotEmpty()) {
					dropCoin(e.block.location, 1.0)
				}
			}
		}
	}
}