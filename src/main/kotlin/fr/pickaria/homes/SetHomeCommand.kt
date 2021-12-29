package fr.pickaria.homes

import fr.pickaria.Main
import fr.pickaria.model.Home
import fr.pickaria.model.home
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.ktorm.entity.add
import org.ktorm.entity.update
import java.sql.SQLException
import kotlin.math.ceil

class SetHomeCommand : CommandExecutor, TabCompleter {
	companion object {
		const val NAME = "home"
	}

	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
		if (sender is Player) {
			val location = sender.location

			if (!location.block.type.isAir) {
				sender.sendMessage("§cCe point de teleportation n'est pas sécurisé, vous ne pouvez pas créer un point de té.")
				return true
			}

			val homeName = try {
				args[0]
			} catch (_: ArrayIndexOutOfBoundsException) {
				NAME
			}

			val home = Home {
				playerUniqueId = sender.uniqueId
				name = homeName
				x = location.x.toInt()
				y = ceil(location.y).toInt()
				z = location.z.toInt()
			}

			try {
				if (Main.database.home.add(home) > 0) {
					sender.sendMessage("§7Point de téléportation créé.")
				} else {
					sender.sendMessage("§cLe point d'apparition n'a pas pu être créé.")
				}
			} catch (_: SQLException) {
				Main.database.home.update(home)
				sender.sendMessage("§7Point de téléportation mis à jour.")
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