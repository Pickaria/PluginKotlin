package fr.pickaria.randomtp

import fr.pickaria.Main
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.Location
import org.bukkit.block.Biome
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.HashMap
import java.util.Random

class RandomCommand : CommandExecutor {
	companion object {
		private const val TAG = "FIRST_RTP_USED"
		private const val PRICE = 100.0
		private const val TELEPORT_COOLDOWN = 100L // Cooldown before teleporting
		private const val BETWEEN_TELEPORT_COOLDOWN = 20 // Cooldown during teleports

		private val EXCLUDED_BIOMES = setOf(
			// Oceans
			Biome.OCEAN,
			Biome.COLD_OCEAN,
			Biome.DEEP_OCEAN,
			Biome.DEEP_COLD_OCEAN,
			Biome.DEEP_FROZEN_OCEAN,
			Biome.DEEP_LUKEWARM_OCEAN,
			Biome.FROZEN_OCEAN,
			Biome.LUKEWARM_OCEAN,
			Biome.WARM_OCEAN,

			// Rivers
			Biome.RIVER,
			Biome.FROZEN_RIVER,

			// Powder snow biomes
			Biome.SNOWY_SLOPES,
			Biome.GROVE,
		)
	}

	private val random: Random = Random()
	private val maxRadius = 10240
	private val lastTeleport = HashMap<Player, LocalDateTime>()

	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
		if (sender is Player) {
			val containsTag = sender.scoreboardTags.contains(TAG)

			val timeRemaining = lastTeleport[sender]?.until(
				LocalDateTime.now(),
				ChronoUnit.MINUTES
			) ?: 0

			if (timeRemaining >= BETWEEN_TELEPORT_COOLDOWN) {
				if (containsTag && Main.economy.has(sender, PRICE)) {
					val withdrawResponse = Main.economy.withdrawPlayer(sender, PRICE)

					if (withdrawResponse.type == EconomyResponse.ResponseType.SUCCESS) {
						teleportPlayer(sender)
					} else {
						sender.sendMessage("§cUne erreur s'est produite.")
					}
				} else if (!containsTag) {
					teleportPlayer(sender)
					sender.addScoreboardTag(TAG)
				} else {
					sender.sendMessage("§cVous n'avez pas assez d'argent.")
				}
			} else {
				sender.sendMessage("§cVous devez patienter encore $timeRemaining minutes avant de vous téléporter.")
			}
		}

		return true
	}

	private fun teleportPlayer(sender: Player) {
		sender.sendMessage("§7Téléportation dans ${TELEPORT_COOLDOWN / 20} secondes.")

		var location: Location
		var tries = 0

		do {
			val x = random.nextDouble() * maxRadius
			val z = random.nextDouble() * maxRadius
			location = Location(sender.world, x, 0.0, z)
			location.y = sender.world.getHighestBlockYAt(location) + 1.0
		} while (EXCLUDED_BIOMES.contains(location.world?.getBiome(location)) && tries++ < 5)

		lastTeleport[sender] = LocalDateTime.now()

		object : BukkitRunnable() {
			override fun run() {
				sender.teleport(location)
			}
		}.runTaskLater(Main.plugin, TELEPORT_COOLDOWN)
	}
}