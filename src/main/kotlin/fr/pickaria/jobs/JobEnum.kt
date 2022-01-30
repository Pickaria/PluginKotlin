package fr.pickaria.jobs

import org.bukkit.Material

enum class JobEnum(
	val label: String,
	val icon: Material,
	val description: String,
	val experiencePercentage: Double = 1.08,
	val mult: Int = 1,
	val revenueIncrease: Double = 1.05,
	val startExperience: Int = 25,
) {
	MINER("Mineur", Material.IRON_PICKAXE, "Miner des minerais.",
		1.075, 50),

	HUNTER("Chasseur", Material.IRON_SWORD, "Tuer des monstres.",
		1.1, 50, 1.04, 30),

	FARMER("Fermier", Material.WHEAT_SEEDS, "Récolter des plantes qui sont entièrement poussées.",
		1.09, 50, 1.03),

	BREEDER("Éleveur", Material.LEAD, "Reproduire des animaux et apprivoiser des animaux.",
		1.092, 50, 1.04),

	ALCHEMIST("Alchimiste", Material.GLASS_BOTTLE, "Fabriquer des potions.",
		1.089, 50, 1.03),

	WIZARD("Enchanteur", Material.BOOK, "Enchanter des objets.",
		1.08, 50),

	TRADER("Commerçant", Material.EMERALD_BLOCK, "Faire des échanges avec des villageois.",
		1.084, 50, 1.04);
}