package fr.pickaria.tpa

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class TpacceptCommand : CommandExecutor {

	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

		if (sender is Player) {
			val recipient: Player = try {
				TeleportController.map[sender]!!
			} catch (_: NullPointerException) {
				sender.sendMessage("§cAucune téléportation en cours.")
				return true
			}

			sender.sendMessage("§7${recipient}")

			if (recipient == sender) {
				sender.sendMessage("§c???.")
			} else {
				TeleportController.cooldownTeleport(recipient, sender.location)
				TeleportController.map.remove(sender)
			}
		}
		return true
	}

}
