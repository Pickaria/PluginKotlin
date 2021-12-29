package fr.pickaria.homes

import fr.pickaria.Main
import fr.pickaria.cooldownTeleport
import fr.pickaria.model.home
import org.bukkit.Location
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


class HomeCommand : CommandExecutor, TabCompleter {
	companion object {
		const val NAME = "home"
	}

	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
		if (sender is Player) {
			val homeName = try {
				args[0]
			} catch (_: ArrayIndexOutOfBoundsException) {
				NAME
			}

			val home = try {
				Main.database.home
					.find { (it.name eq homeName) and (it.playerUniqueId eq sender.uniqueId) }!!
			} catch (_: NullPointerException) {
				sender.sendMessage("§cCe point de teleportation n'existe pas.")
				return true
			}

			val location = Location(sender.world, home.x.toDouble(), home.y.toDouble(), home.z.toDouble())
			if (!location.block.type.isAir) {
				sender.sendMessage("§cCe point de teleportation n'est pas sécurisé, vous ne pouvez pas y être téléporté.")
			} else {
				cooldownTeleport(sender, location)
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