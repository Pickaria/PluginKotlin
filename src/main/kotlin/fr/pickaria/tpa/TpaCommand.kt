package fr.pickaria.tpa


import fr.pickaria.Main
import org.bukkit.Bukkit.getPlayer

import org.bukkit.Bukkit.getServer

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable


 class TpaCommand : CommandExecutor {

	companion object {

		private const val TELEPORT_COOLDOWN = 20L // Cooldown during teleports

	}

	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
		if (sender is Player) {

			val recipient =
				getPlayer(args[0])

			if (recipient == sender) {
				sender.sendMessage("§cVous ne pouvez pas vous teleporter à vous-même.")
				return true
			} else {


				if (recipient != null) {
					recipient.sendMessage("§7${sender.displayName} Souhaite se téléporter à vous\nTapez /tpyes si vous accepté sa téléportation.\nTapez /tpdeny si vous refusé.")
					teleportPlayer(sender, recipient)
					sender.sendMessage("§7Vous avez été teleporté.")
				}
			}
		}
		return true
	}
	public fun teleportPlayer(sender: Player, recipient: Player) {
		sender.sendMessage("§7Téléportation dans ${TELEPORT_COOLDOWN / 20} secondes.")

		object : BukkitRunnable() {
			override fun run() {
				sender.teleport(recipient)
			}
		}.runTaskLater(Main.plugin, TELEPORT_COOLDOWN)
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

