package fr.pickaria

import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class Command: CommandExecutor {
	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
		if(sender is Player){
			val diamond = ItemStack(Material.DIAMOND)
			diamond.amount = 5
			sender.inventory.addItem(diamond)
			return true
		}
		return false
	}
}