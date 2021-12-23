package fr.pickaria.randomtp

import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.Random

class RandomCommand : CommandExecutor {
	private val random: Random = Random()
	private val maxRadius = 10240

	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
		if (sender is Player) {
			val x = random.nextDouble() * maxRadius
			val z = random.nextDouble() * maxRadius
			val location = Location(sender.world, x, 0.0, z)
			location.y = sender.world.getHighestBlockYAt(location).toDouble()

			sender.teleport(location)
			return true
		}
		return false
	}
}