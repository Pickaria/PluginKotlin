package fr.pickaria.economy

import fr.pickaria.Main
import fr.pickaria.model.EconomyModel
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit.getServer
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.ktorm.dsl.*

class BaltopCommand: CommandExecutor {
	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
		val page = try {
			args[0].toInt()
		} catch (_: ArrayIndexOutOfBoundsException) {
			0
		}

		val component = TextComponent("§6==== Baltop : ====")
		val server = getServer()

		Main.database
			.from(EconomyModel)
			.select()
			.orderBy(EconomyModel.balance.desc())
			.limit(0, page * 10)
			.forEach {
				val uuid = it[EconomyModel.playerUniqueId]
				val balance = it[EconomyModel.balance]
				val player = server.getOfflinePlayer(uuid!!)
				component.addExtra("\n§6${it.row} : §7${player.name} - ${Main.economy.format(balance!!)}")
			}

		sender.spigot().sendMessage(component)

		return true
	}
}