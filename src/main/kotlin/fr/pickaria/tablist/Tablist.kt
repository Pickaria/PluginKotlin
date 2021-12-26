package fr.pickaria.tablist

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.PlayerInfoData
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedGameProfile
import fr.pickaria.Main
import org.bukkit.Bukkit.getServer
import java.text.DecimalFormat


fun playerList(plugin: Main) {
	val server = getServer()
	val manager = ProtocolLibrary.getProtocolManager()
	val maxPlayers = server.maxPlayers
	val world = server.getWorld("world")!!;
	val formatter = DecimalFormat("00")

	manager.addPacketListener(object : PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_INFO) {
		override fun onPacketSending(event: PacketEvent?) {
			val packet = event?.packet!!
			val playerList = packet.playerInfoDataLists.read(0)

			val onlinePlayers = server.onlinePlayers

			onlinePlayers.forEachIndexed {
					index, it ->
				val gameMode = EnumWrappers.NativeGameMode.fromBukkit(it.gameMode)
				// TODO: Hide player in spectator mode from list

				// Build display name with prefix
				val prefix = Main.chat.getPlayerPrefix(it).replace("&", "§")
				val displayName = WrappedChatComponent.fromText(prefix + it.displayName)

				val gameProfile = WrappedGameProfile(it.uniqueId, it.name)
				val newInfoData = PlayerInfoData(gameProfile, it.ping, gameMode, displayName)
				playerList[index] = newInfoData
			}

			packet.playerInfoDataLists.write(0, playerList)

			// Build header and footer of player list
			val container = PacketContainer(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER)

			val ticks = world.time;
			val hours = formatter.format((ticks / 1000 + 6) % 24)
			val minutes = formatter.format(ticks % 1000 * 60 / 1000)

			val balance = Main.economy.format(Main.economy.getBalance(event.player))

			val headerText = "§6§lPickaria.fr\n§7${onlinePlayers.size}§6/§7${maxPlayers} §6joueurs\n"
			val footerText = "\n§6Heure : §7$hours:$minutes\n§6Solde : §7${balance}"

			val header = WrappedChatComponent.fromJson("{\"text\":\"$headerText\"}")
			val footer = WrappedChatComponent.fromJson("{\"text\":\"$footerText\"}")
			container.chatComponents.write(0, header)
			container.chatComponents.write(1, footer)

			manager.sendServerPacket(event.player, container)
		}
	})
}