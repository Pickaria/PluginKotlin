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
		player.setDisplayName("$prefix${player.name}")
		event.joinMessage = "§7[§6+§7]§r ${player.displayName}"
	}

	@EventHandler
	fun onPlayerQuit(event: PlayerQuitEvent) {
		val player = event.player
		event.quitMessage = "§7[§c-§7]§r ${player.displayName}"
	}
}