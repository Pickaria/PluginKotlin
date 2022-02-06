package fr.pickaria.tpa

import fr.pickaria.Main
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class TeleportController {
	companion object {
		const val TELEPORT_COOLDOWN = 100L
		val map = mutableMapOf<Player, Player>()

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

		fun createTpRequest(sender: Player, recipient: Player?): Boolean {
			return if (recipient == sender) {
				sender.sendMessage("§cVous ne pouvez pas vous teleporter à vous-même.")
				false
			} else if (recipient is Player) {
				sender.sendMessage("§7Demande de téléportation envoyée à ${recipient.displayName}.")
				map[recipient] = sender
				true
			} else {
				sender.sendMessage("§cLe joueur n'a pas été trouvé.")
				false
			}
		}
	}
}