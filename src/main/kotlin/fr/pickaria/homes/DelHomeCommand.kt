package fr.pickaria.homes

import fr.pickaria.Main
import fr.pickaria.model.home
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.map

class DelHomeCommand : CommandExecutor, TabCompleter {
	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
		if (sender is Player) {
			val homeName = try {
				args[0]
			} catch (_: ArrayIndexOutOfBoundsException) {
				SetHomeCommand.NAME
			}

			try {
				val home = Main.database.home
					.find { (it.name eq homeName) and (it.playerUniqueId eq sender.uniqueId) }!!

				home.delete()
				sender.sendMessage("§7Point de téléportation supprimé.")
			} catch (_: NullPointerException) {
				sender.sendMessage("§cLe point d'apparition n'existe pas.")
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
		return Main.database.home
			.filter { it.playerUniqueId eq (sender as Player).uniqueId }
			.map { it.name }
			.toMutableList()
	}
}