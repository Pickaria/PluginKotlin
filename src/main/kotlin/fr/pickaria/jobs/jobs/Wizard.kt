package fr.pickaria.jobs.jobs

import fr.pickaria.Main
import fr.pickaria.jobs.JobEnum
import fr.pickaria.jobs.jobPayPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.enchantment.EnchantItemEvent

class Wizard: Listener {
	private val levels = mapOf(
		1 to 0.15,
		2 to 0.175,
		3 to 0.2,
	)

	@EventHandler(priority = EventPriority.MONITOR)
	fun onEnchantItem(event: EnchantItemEvent) {
		val uniqueId = event.enchanter.uniqueId
		if (event.isCancelled || !Main.jobController.hasJob(uniqueId, JobEnum.WIZARD)) return

		jobPayPlayer(event.enchanter, levels[event.whichButton()] ?: 0.15, JobEnum.WIZARD)
		Main.jobController.addExperienceAndAnnounce(event.enchanter, JobEnum.WIZARD, 1)
	}
}