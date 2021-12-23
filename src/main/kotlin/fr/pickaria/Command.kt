package fr.pickaria

import fr.pickaria.menus.BaseMenu
import org.bukkit.Bukkit.getServer
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class Command(plugin: Main) : CommandExecutor {
	private val menu = BaseMenu("Test")

	init {
		getServer().pluginManager.registerEvents(menu, plugin)
	}

	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
		if (sender is Player) {
			menu.openInventory(sender)
		}
		return true
	}
}