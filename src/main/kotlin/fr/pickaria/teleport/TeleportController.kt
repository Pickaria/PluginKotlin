package fr.pickaria.teleport

import fr.pickaria.Main
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class TeleportController {
	companion object {
		const val TELEPORT_COOLDOWN = 100L
		val map = mutableMapOf<Player, Pair<Player, Boolean>>()

		fun cooldownTeleport(player: Player, location: Location) {
			player.sendMessage("§7Téléportation dans ${TELEPORT_COOLDOWN / 20} secondes.")

			location.add(0.5, 0.0, 0.5)

			object : BukkitRunnable() {
				override fun run() {
					player.teleport(location)
					player.sendMessage("§7Vous avez été téléporté.")
				}
			}.runTaskLater(Main.plugin, TELEPORT_COOLDOWN)
		}

		/**
		 * Create a teleport request to teleport Sender to Recipient
		 */
		fun createTpRequest(sender: Player, recipient: Player, direction: Boolean = true): Boolean {
			return if (recipient == sender) {
				sender.sendMessage("§cVous ne pouvez pas vous teleporter à vous-même.")
				false
			} else {
				map[sender] = recipient to direction
				true
			}
		}
	}
}