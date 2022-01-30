package fr.pickaria.jobs.jobs

import fr.pickaria.Main
import fr.pickaria.jobs.JobEnum
import fr.pickaria.jobs.jobPayPlayer
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class Miner: Listener {
	private val materials = mapOf(
		// Level 1 ores
		Material.COAL_ORE to Pair(0.15, 1),
		Material.COPPER_ORE to Pair(0.15, 1),
		Material.REDSTONE_ORE to Pair(0.15, 1),

		Material.DEEPSLATE_COAL_ORE to Pair(0.15, 1),
		Material.DEEPSLATE_COPPER_ORE to Pair(0.15, 1),
		Material.DEEPSLATE_REDSTONE_ORE to Pair(0.15, 1),

		Material.NETHER_QUARTZ_ORE to Pair(0.15, 1),
		Material.NETHER_GOLD_ORE to Pair(0.15, 1),

		// Level 2 ores
		Material.IRON_ORE to Pair(0.2, 2),
		Material.GOLD_ORE to Pair(0.2, 2),
		Material.LAPIS_ORE to Pair(0.2, 2),

		Material.DEEPSLATE_IRON_ORE to Pair(0.2, 2),
		Material.DEEPSLATE_GOLD_ORE to Pair(0.2, 2),
		Material.DEEPSLATE_LAPIS_ORE to Pair(0.2, 2),

		// Level 3 ores
		Material.DIAMOND_ORE to Pair(0.4, 5),
		Material.EMERALD_ORE to Pair(0.4, 5),

		Material.DEEPSLATE_DIAMOND_ORE to Pair(0.4, 5),
		Material.DEEPSLATE_EMERALD_ORE to Pair(0.4, 5),
	)

	@EventHandler(priority = EventPriority.MONITOR)
	fun onBlockBreak(event: BlockBreakEvent) {
		val player = event.player
		if (event.isCancelled || !Main.jobController.hasJob(player.uniqueId, JobEnum.MINER)) return

		// check if player is using silk touch
		val itemInHand = player.inventory.itemInMainHand
		if (Enchantment.SILK_TOUCH !in itemInHand.enchantments && event.block.getDrops(itemInHand).isNotEmpty()) {
			materials[event.block.type]?.let {
				jobPayPlayer(player, it.first, JobEnum.MINER)
				Main.jobController.addExperienceAndAnnounce(player, JobEnum.MINER, it.second)
			}
		}
	}
}