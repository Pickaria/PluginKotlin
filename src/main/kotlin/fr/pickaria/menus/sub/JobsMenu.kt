package fr.pickaria.menus.sub

import fr.pickaria.Main
import fr.pickaria.jobs.JobEnum
import fr.pickaria.menus.BaseMenu
import fr.pickaria.menus.MenuEnum
import fr.pickaria.menus.MenuItem
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player

class JobsMenu(title: String = MenuEnum.JOBS.title, opener: HumanEntity?, previousMenu: BaseMenu?, size: Int = 54) :
	BaseMenu(title, opener, previousMenu, size) {

	constructor(opener: HumanEntity?, previousMenu: BaseMenu?) : this(MenuEnum.JOBS.title, opener, previousMenu, 54)

	init {
		fillMaterial = Material.GREEN_STAINED_GLASS_PANE
	}

	override fun initMenu() {
		var x = 1
		for (job in JobEnum.values()) {
			val lore = mutableListOf<String>()
			lore.add("§7${job.description}")

			opener?.let { opener ->
				Main.jobController.getFromCache(opener.uniqueId, job)?.let {
					val level = Main.jobController.getLevelFromExperience(job, it.level)
					lore.add("§6Niveau :§7 $level")
				}

				if (!Main.jobController.hasJob(opener.uniqueId, job)) {
					lore.add("Clic-gauche pour rejoindre le métier")
				} else {
					lore.add("Clic-droit pour quitter le métier")
				}
			}

			val menuItem = MenuItem(
				job.icon,
				job.label,
				lore
			)
				.setCallback { event ->
					with (event.whoClicked as Player) {
						if (event.isLeftClick) {
							this.chat("/job join ${job.name.lowercase()}")
						} else if (event.isRightClick) {
							this.chat("/job leave ${job.name.lowercase()}")
						}
					}
				}

			super.setMenuItem(x++, 2, menuItem)
		}
	}
}