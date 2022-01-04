package fr.pickaria

import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent

class Test: Listener {
	@EventHandler
	fun onPlayerJoin(event: PlayerJoinEvent) {
		event.joinMessage = String.format("§7[§6+§7]§r %s", event.player.displayName)
	}

	@EventHandler
	fun onPlayerQuit(event: PlayerQuitEvent) {
		event.quitMessage = String.format("§7[§6-§7]§r %s", event.player.displayName)
	}
}