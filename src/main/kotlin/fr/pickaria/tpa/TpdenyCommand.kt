package fr.pickaria.tpa


import fr.pickaria.Main
import org.bukkit.Bukkit.getPlayer

import org.bukkit.Bukkit.getServer

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable


class TpdenyCommand : CommandExecutor {

	companion object {

		private const val TELEPORT_COOLDOWN = 20L // Cooldown during teleports
		private val flag = 1

	}

	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
		if (sender is Player) {

			val recipient =
				getPlayer(args[0])
			if(flag !=0){
				if (recipient != null) {
					recipient.sendMessage("§7 votre demande à été refusée ")
				}else {
					sender.sendMessage("§7Aucune demande de téléportation en cours")
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
		return true
}}

