package fr.pickaria.tpa

import org.bukkit.Bukkit.getPlayer
import org.bukkit.Bukkit.getServer

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class TpahereCommand : CommandExecutor {

	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
		if (sender is Player) {
			getPlayer(args[0])?.let {
				if (TeleportController.map.contains(it)) {
					sender.sendMessage("§cUne demande est déjà en cours pour ce joueur.")
				} else
				if (TeleportController.createTpRequest(it, sender, true)) {
					sender.sendMessage("§7Demande de téléportation envoyée à ${it.displayName}.")
					it.sendMessage("§7${sender.displayName} souhaite vous téléporter à lui/elle.\nTapez §6/tpyes§7 si vous accepté sa téléportation.\nTapez §6/tpdeny§7 si vous refusé.")
				}
			}
		}

		return true
	}

}
