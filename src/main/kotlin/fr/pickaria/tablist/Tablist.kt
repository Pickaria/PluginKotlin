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


fun playerList(plugin: Main) {
	val server = getServer()
	val manager = ProtocolLibrary.getProtocolManager()

	manager.addPacketListener(object : PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_INFO) {
		override fun onPacketSending(event: PacketEvent?) {
			val packet = event?.packet!!
			val playerList = packet.playerInfoDataLists.read(0)

			server.onlinePlayers.forEachIndexed {
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
		}
	})
}