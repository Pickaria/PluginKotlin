package fr.pickaria.tablist

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.WrappedChatComponent
import fr.pickaria.Main
import org.bukkit.Bukkit.getServer
import java.text.DecimalFormat


fun playerList(plugin: Main) {
	val server = getServer()
	val manager = ProtocolLibrary.getProtocolManager()
	val maxPlayers = server.maxPlayers
	val world = server.getWorld("world")!!
	val formatter = DecimalFormat("00")

	manager.addPacketListener(object : PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_INFO) {
		override fun onPacketSending(event: PacketEvent?) {
			val onlinePlayers = server.onlinePlayers
			val player = event?.player ?: return

			// Build header and footer of player list
			val container = PacketContainer(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER)

			val ticks = world.time;
			val hours = formatter.format((ticks / 1000 + 6) % 24)
			val minutes = formatter.format(ticks % 1000 * 60 / 1000)

			val balance = Main.economy.format(Main.economy.getBalance(player))

			val headerText = "§6§lPickaria.fr\n§7${onlinePlayers.size}§6/§7${maxPlayers} §6joueurs\n"
			val footerText = "\n§6Heure : §7$hours:$minutes\n§6Solde : §7${balance}"

			val header = WrappedChatComponent.fromJson("{\"text\":\"$headerText\"}")
			val footer = WrappedChatComponent.fromJson("{\"text\":\"$footerText\"}")
			container.chatComponents.write(0, header)
			container.chatComponents.write(1, footer)

			manager.sendServerPacket(player, container)
		}
	})
}