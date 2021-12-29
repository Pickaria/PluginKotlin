package fr.pickaria.homes

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player


class DelHomeCommand : CommandExecutor, TabCompleter {
	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
		if (sender is Player) {
			val homeName = try {
				args[0]
			} catch (_: ArrayIndexOutOfBoundsException) {
				SetHomeCommand.NAME
			}

			if (HomeController.removeHomeFromCache(sender.uniqueId, homeName)) {
				sender.sendMessage("§7Ce point de téléportation a été supprimé.")
			} else {
				sender.sendMessage("§cCe point de téléportation n'existe pas.")
			}
		}
		return true
	}

	override fun onTabComplete(
		sender: CommandSender,
		command: Command,
		alias: String,
		args: Array<out String>
	): MutableList<String> {
		return HomeController.getHomeNames((sender as Player).uniqueId).toMutableList()
	}
}