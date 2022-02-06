package fr.pickaria.homes

import fr.pickaria.Main
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class SetHomeCommand : CommandExecutor, TabCompleter {
	companion object {
		const val NAME = "home"
	}

	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
		if (sender is Player) {
			val location = sender.location

			if (location.block.type.isOccluding) {
				sender.sendMessage("§cCe point de teleportation n'est pas sécurisé, vous ne pouvez pas créer un point ici.")
				return true
			}

			val homeName = try {
				args[0]
			} catch (_: ArrayIndexOutOfBoundsException) {
				NAME
			}

			if (Main.homeController.addHome(sender.uniqueId, homeName, location)) {
				sender.sendMessage("§7Point de téléportation créé.")
			} else {
				sender.sendMessage("§cLe point d'apparition n'a pas pu être créé.")
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
		return mutableListOf()
	}
}