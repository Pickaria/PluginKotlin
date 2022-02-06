package fr.pickaria.teleport

import fr.pickaria.Main
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable


class TeleportController {
	companion object {
		const val TELEPORT_COOLDOWN = 100L
		val map = mutableMapOf<Player, Pair<Player, Boolean>>()

		fun cooldownTeleport(player: Player, location: Location) {
			player.sendMessage("§7Téléportation dans ${TELEPORT_COOLDOWN / 20} secondes.")

			location.add(0.5, 0.0, 0.5)

			object : BukkitRunnable() {
				override fun run() {
					player.teleport(location)
					player.sendMessage("§7Vous avez été téléporté.")
				}
			}.runTaskLater(Main.plugin, TELEPORT_COOLDOWN)
		}

		/**
		 * Create a teleport request to teleport Sender to Recipient
		 */
		fun createTpRequest(sender: Player, recipient: Player, direction: Boolean = true): Boolean {
			return if (recipient == sender) {
				sender.sendMessage("§cVous ne pouvez pas vous teleporter à vous-même.")
				false
			} else {
				map[sender] = recipient to direction
				true
			}
		}

		fun sendTpRequestMessage(sender: Player, message: String) {
			val component = TextComponent(message)

			val acceptComponent = TextComponent("[ACCEPTER]")
			acceptComponent.setColor(ChatColor.GOLD)
			acceptComponent.setClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpyes"))

			val denyComponent = TextComponent("[REFUSER]")
			denyComponent.setColor(ChatColor.RED)
			denyComponent.setClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpno"))

			component.addExtra("\n > ")
			component.addExtra(acceptComponent)
			component.addExtra(" ou ")
			component.addExtra(denyComponent)
			component.addExtra(" <")

			sender.spigot().sendMessage(component)
		}
	}
}