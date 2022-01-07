package fr.pickaria.tablist

import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent


class ChatFormat: Listener {
	@EventHandler
	fun onAsyncPlayerChat(e: AsyncPlayerChatEvent) {
		e.format = "§7%1\$s » §7%2\$s"

		// Player mention
		val playerToRemove = mutableListOf<Player>()

		for (player in e.recipients) {
			if (e.message.lowercase().contains(player.name.lowercase())) {
				playerToRemove.add(player)
				player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)

				player.sendMessage(
					String.format(
						e.format,
						e.player.displayName,
						e.message.replace(player.name, String.format("§6%s§r", player.name))
					)
				)
			}
		}

		for (player in playerToRemove) {
			e.recipients.remove(player)
		}
	}
}