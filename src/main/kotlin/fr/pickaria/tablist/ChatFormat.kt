package fr.pickaria.tablist

import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent


class ChatFormat: Listener {
	@EventHandler
	fun onAsyncPlayerChat(e: AsyncPlayerChatEvent) {
		e.format = "§f%1\$s > §7%2\$s"

		// Player mention
		val playerToRemove = mutableListOf<Player>()

		for (player in e.recipients) {
			val pos = e.message.lowercase().indexOf(player.name.lowercase())

			if (pos >= 0) {
				playerToRemove.add(player)
				player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)

				player.sendMessage(
					String.format(
						e.format,
						e.player.displayName,
						e.message.replaceRange(pos, pos + player.name.length, "§6${player.name}§7")
					)
				)
			}
		}

		for (player in playerToRemove) {
			e.recipients.remove(player)
		}
	}
}