package fr.pickaria.tablist

import fr.pickaria.Main
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent


class ChatFormat: Listener {
	@EventHandler
	fun onAsyncPlayerChat(e: AsyncPlayerChatEvent) {
		e.format = "§7%1\$s:§7 %2\$s"

		val prefix = Main.chat.getPlayerPrefix(e.player).replace("&", "§")
		e.player.setDisplayName(prefix + e.player.name)

		// Player mention
		for (player in e.recipients) {
			if (e.message.contains(player.displayName)) {
				e.recipients.remove(player)
				player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)

				player.sendMessage(
					java.lang.String.format(
						e.format,
						e.player.displayName,
						e.message.replace(player.displayName, String.format("§6%s§r", player.displayName))
					)
				)
			}
		}
	}
}