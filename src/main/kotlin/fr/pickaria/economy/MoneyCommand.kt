package fr.pickaria.economy

import fr.pickaria.Main
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class MoneyCommand : CommandExecutor {
	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
		if (sender is Player) {
			val balance = Main.economy.format(Main.economy.getBalance(sender.name))
			sender.sendMessage("§7Votre solde : §6$balance")
			return true
		}
		return false
	}
}