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
				if (TeleportController.createTpRequest(sender, it)) {
					it.sendMessage("§7${sender.displayName} souhaite vous téléporter à lui/elle.\nTapez §6/tpyes§7 si vous accepté sa téléportation.\nTapez §6/tpdeny§7 si vous refusé.")
				}
			}
		}

		return true
	}

	fun onTabComplete(
		sender: CommandSender,
		command: Command,
		alias: String,
		args: Array<out String>
	): MutableList<String> {
		return if (args.size == 1) {
			getServer().onlinePlayers.filter {
				it.name.startsWith(args[0]) && sender !== it
			}.map {
				it.name
			}.toMutableList()
		} else {
			mutableListOf()
		}

	}

}
