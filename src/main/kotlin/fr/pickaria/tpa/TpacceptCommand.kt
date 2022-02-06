package fr.pickaria.tpa


import org.bukkit.Bukkit.getPlayer
import fr.pickaria.Main
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import fr.pickaria.tpa.TpaCommand.Companion.map


class TpacceptCommand : CommandExecutor {

	companion object {

		private const val TELEPORT_COOLDOWN = 20L // Cooldown during teleports

	}

	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

		map.forEach {
			println("${it.key} : ${it.value}")
		}

		if (sender is Player) {
			val recipient: Player = try {
				map[sender]!!
			} catch (_: NullPointerException) {
				sender.sendMessage("§cJoueur non trouvé.")
				return true
			}

			sender.sendMessage("§7${recipient}")

			if (recipient == sender) {
				sender.sendMessage("§c???.")
			} else {
				teleportPlayer(recipient, sender)
				recipient.sendMessage("§7Vous avez été teleporté.")
			}
		}
		return true
	}

	private fun teleportPlayer(recipient: Player, sender: Player) {
		recipient.sendMessage("§7Téléportation dans ${TELEPORT_COOLDOWN / 20} secondes.")

		object : BukkitRunnable() {
			override fun run() {
				recipient.teleport(sender)
			}
		}.runTaskLater(Main.plugin, TELEPORT_COOLDOWN)
	}

}
