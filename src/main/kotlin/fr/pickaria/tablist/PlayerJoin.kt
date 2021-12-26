package fr.pickaria.tablist

import fr.pickaria.Main
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent

class PlayerJoin : Listener {
	@EventHandler
	fun onPlayerJoin(event: PlayerJoinEvent) {
		val player = event.player
		val prefix = Main.chat.getPlayerPrefix(player).replace("&", "§")
		event.joinMessage = "§7[§6+§7]§r $prefix${player.displayName}"
	}

	@EventHandler
	fun onPlayerQuit(event: PlayerQuitEvent) {
		val player = event.player
		val prefix = Main.chat.getPlayerPrefix(player).replace("&", "§")
		event.quitMessage = "§7[§c-§7]§r $prefix${player.displayName}"
	}
}