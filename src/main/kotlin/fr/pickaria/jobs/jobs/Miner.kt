package fr.pickaria.jobs.jobs

import fr.pickaria.Main
import fr.pickaria.coins.Coin.Companion.dropCoin
import fr.pickaria.jobs.JobEnum
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class Miner: Listener {
	private val materials = mapOf(
		Material.COAL_ORE to 1.0,
		Material.COPPER_ORE to 1.0,
		Material.IRON_ORE to 1.0,
		Material.GOLD_ORE to 1.0,
		Material.DIAMOND_ORE to 1.0,
		Material.EMERALD_ORE to 1.0,
		Material.REDSTONE_ORE to 1.0,
		Material.LAPIS_ORE to 1.0,

		Material.DEEPSLATE_COAL_ORE to 1.0,
		Material.DEEPSLATE_COPPER_ORE to 1.0,
		Material.DEEPSLATE_IRON_ORE to 1.0,
		Material.DEEPSLATE_GOLD_ORE to 1.0,
		Material.DEEPSLATE_DIAMOND_ORE to 1.0,
		Material.DEEPSLATE_EMERALD_ORE to 1.0,
		Material.DEEPSLATE_REDSTONE_ORE to 1.0,
		Material.DEEPSLATE_LAPIS_ORE to 1.0,

		Material.NETHER_QUARTZ_ORE to 1.0,
		Material.NETHER_GOLD_ORE to 1.0,
	)

	@EventHandler(priority = EventPriority.MONITOR)
	fun onBlockBreak(event: BlockBreakEvent) {
		val player = event.player
		if (!Main.jobController.hasJob(player.uniqueId, JobEnum.MINER)) return

		val material = event.block.type
		if (materials.containsKey(material)) {
			// check if player is using silk touch
			val itemInHand = player.inventory.itemInMainHand
			if (!itemInHand.enchantments.contains(Enchantment.SILK_TOUCH) && event.block.getDrops(itemInHand).isNotEmpty()) {
				dropCoin(event.block.location)
				Main.jobController.addExperienceAndAnnounce(player, JobEnum.MINER, 1)
			}
		}
	}
}