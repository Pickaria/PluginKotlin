package fr.pickaria.menus.sub

import fr.pickaria.Main
import fr.pickaria.menus.BaseMenu
import fr.pickaria.menus.MenuEnum
import fr.pickaria.menus.MenuItem
import org.bukkit.Material
import org.bukkit.entity.HumanEntity

class HomeMenu(title: String = MenuEnum.HOME.title, opener: HumanEntity?, previousMenu: BaseMenu?, size: Int = 54) :
	BaseMenu(title, opener, previousMenu, size) {

	constructor(opener: HumanEntity?, previousMenu: BaseMenu?) : this(MenuEnum.HOME.title, opener, previousMenu, 54)

	override fun initMenu() {
		super.setMenuItem(0, 1, MenuItem(Material.OAK_SIGN, "Towny", "§7Menus relatifs aux villes."))

		super.setMenuItem(2,
			1,
			MenuItem(
				Material.OAK_FENCE_GATE,
				"Villes",
				"§7Liste des villes du serveur.",
				"§7Vous permet de vous téléporter à chaque ville.",
				"Clic-gauche pour ouvrir le sous-menu"
			)
				.setCallback { event ->
					event.whoClicked.sendMessage("Hello")
				})

		super.setMenuItem(3,
			1,
			MenuItem(
				Material.OAK_FENCE_GATE,
				"Test menu"
			)
				.setCallback { event ->
					Main.menuController.openMenu(event.whoClicked, MenuEnum.JOBS, this)
				})
	}
}