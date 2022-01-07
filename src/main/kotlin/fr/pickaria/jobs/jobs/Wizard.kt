package fr.pickaria.jobs.jobs

import fr.pickaria.Main
import fr.pickaria.coins.Coin
import fr.pickaria.jobs.JobEnum
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.enchantment.EnchantItemEvent

class Wizard: Listener {
	@EventHandler
	fun onEnchantItem(event: EnchantItemEvent) {
		val uniqueId = event.enchanter.uniqueId
		if (!Main.jobController.hasJob(uniqueId, JobEnum.WIZARD)) return

		Coin.dropCoin(event.enchantBlock.location, 1.0, (event.expLevelCost / 10.0).coerceAtLeast(1.5))
		Main.jobController.addExperienceAndAnnounce(event.enchanter, JobEnum.WIZARD, 1)
	}
}