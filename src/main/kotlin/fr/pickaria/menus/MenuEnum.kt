package fr.pickaria.menus

import fr.pickaria.menus.sub.HomeMenu
import fr.pickaria.menus.sub.JobsMenu
import kotlin.reflect.KClass

enum class MenuEnum(val title: String, val menu: KClass<*>) {
	EMPTY("§6§lTest vide", BaseMenu::class),
	HOME("§6§lAccueil", HomeMenu::class),
	JOBS("§6§lMétiers", JobsMenu::class);
}