package fr.pickaria.tpa

import org.bukkit.Bukkit.getPlayer
import org.bukkit.Bukkit.getServer

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.UUID


class TpaCommand : CommandExecutor {

	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
		if (sender is Player) {
			getPlayer(args[0])?.let {
				if (TeleportController.createTpRequest(it, sender, false)) {
					sender.sendMessage("§7Demande de téléportation envoyée à ${it.displayName}.")
					it.sendMessage("§7${sender.displayName} souhaite se téléporter à vous.\nTapez §6/tpyes§7 si vous accepté sa téléportation.\nTapez §6/tpdeny§7 si vous refusé.")
				}
			}
		}

		return true
	}

}
