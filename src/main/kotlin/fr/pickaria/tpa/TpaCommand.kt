package fr.pickaria.tpa

import org.bukkit.Bukkit.getPlayer
import org.bukkit.Bukkit.getServer

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.UUID


class TpaCommand : CommandExecutor {

	companion object {

		val map = mutableMapOf<Player, Player>()

	}

	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
		if (sender is Player) {

			val recipient = getPlayer(args[0])

			if (recipient == sender) {
				sender.sendMessage("§cVous ne pouvez pas vous teleporter à vous-même.")
			} else if (recipient is Player) {
				sender.sendMessage("§7Demande de téléportation envoyée à ${recipient.displayName}.")

				recipient.sendMessage("§7${sender.displayName} Souhaite se téléporter à vous\nTapez /tpyes si vous accepté sa téléportation.\nTapez /tpdeny si vous refusé.")
				map[recipient] = sender
			} else {
				sender.sendMessage("§cLe joueur n'a pas été trouvé.")
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
