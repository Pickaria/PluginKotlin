package fr.pickaria.economy

import fr.pickaria.Main
import fr.pickaria.model.EconomyModel
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit.getServer
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.ktorm.dsl.*

class BaltopCommand : CommandExecutor {
	companion object {
		const val PAGE_SIZE = 8
		val server = getServer()
	}

	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
		val page = try {
			(args[0].toInt() - 1).coerceAtLeast(0)
		} catch (_: ArrayIndexOutOfBoundsException) {
			0
		}

		val min = page * PAGE_SIZE
		val players = getServer().offlinePlayers

		if (min > players.size) {
			sender.sendMessage("§cIl n'y a pas autant de pages.")
			return true
		}

		//val max = (min + PAGE_SIZE - 1).coerceAtMost(players.size - 1)
		val component = TextComponent("§6==== Baltop (${page + 1}/${players.size / PAGE_SIZE + 1}) ====")

		Main.database
			.from(EconomyModel)
			.select()
			.orderBy(EconomyModel.balance.desc())
			.limit(min, 8)
			.where { EconomyModel.balance greater 0.0 }
			.forEach {
				val uuid = it[EconomyModel.playerUniqueId]
				val balance = it[EconomyModel.balance]
				val player = server.getOfflinePlayer(uuid!!)
				component.addExtra("\n§6${it.row} : §7${player.name} - ${Main.economy.format(balance!!)}")
			}

		component.addExtra("\n§7Tapez §6/baltop ${page + 2}§7 pour lire la page suivante.")

		sender.spigot().sendMessage(component)

		return true
	}
}