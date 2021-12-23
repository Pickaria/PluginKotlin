package fr.pickaria.jobs

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class JobCommand : CommandExecutor, TabCompleter {
	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
		if (sender is Player) {
			if (args.size != 2) {
				sender.sendMessage("§cVeuillez entrer le nom du métier.")
				return false
			}

			when (args[0]) {
				"join" -> {
					try {
						when (val it = JobController.changeJob(sender.uniqueId, JobEnum.valueOf(args[1].uppercase()))) {
							-1 -> sender.sendMessage("§7Vous avez rejoint le métier ${args[1]}.")
							-2 -> sender.sendMessage("§cVous exercez déjà ce métier.")
							else -> sender.sendMessage("§cVous devez attendre $it heures avant de changer de métier.")
						}
					} catch (e: IllegalArgumentException) {
						sender.sendMessage("§cCe métier n'existe pas.")
					}
				}
				"leave" -> {
					JobController.changeJob(sender.uniqueId, JobEnum.NONE)
					sender.sendMessage("§7Vous avez quitté votre métier actuel.")
				}
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
		return when (args.size) {
			1 -> mutableListOf("join", "leave")
			2 -> JobEnum.values().map { it.name.lowercase() }.toMutableList()
			else -> mutableListOf()
		}
	}
}