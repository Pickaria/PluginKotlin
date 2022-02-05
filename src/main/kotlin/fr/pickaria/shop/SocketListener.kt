package fr.pickaria.shop

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import fr.pickaria.Socket
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit.getServer
import java.util.*


class SocketListener {
	private lateinit var serverSocket: ServerSocket
	private lateinit var selectorManager: ActorSelectorManager
	val gson = Gson()

	fun createSocket() {
		selectorManager = ActorSelectorManager(Dispatchers.Socket)
		serverSocket = aSocket(selectorManager).tcp().bind("127.0.0.1", 9002)
	}

	fun listen() {
		runBlocking {
			println("Server is listening at ${serverSocket.localAddress}")
			while (true) {
				val socket = serverSocket.accept()
				launch {
					val receiveChannel = socket.openReadChannel()
					val sendChannel = socket.openWriteChannel(autoFlush = true)

					try {
						var request = ""
						receiveChannel.awaitContent()
						while (receiveChannel.availableForRead > 0) {
							request += receiveChannel.readUTF8Line()
						}

						val response = JsonObject()

						gson.fromJson(request, JsonElement::class.java)?.let {
							try {
								when (it.asJsonObject["request"].asString) {
									"hasPlayed" -> {
										val uuid = UUID.fromString(it.asJsonObject["uuid"].asString)
										val hasPlayed = getServer().getOfflinePlayer(uuid).hasPlayedBefore()
										response.addProperty("hasPlayed", hasPlayed)
									}
									else -> {
										sendChannel.writeInt(404)
										sendChannel.close()
									}
								}
							} catch (_: IllegalStateException) {
								sendChannel.writeInt(400)
								sendChannel.close()
							}
						}

						sendChannel.writeStringUtf8(response.toString())
						sendChannel.close()
					} catch (e: Throwable) {
						socket.close()
					}
				}
			}
		}
	}

	suspend fun closeSocket() {
		serverSocket.close()
		selectorManager.close()
		serverSocket.awaitClosed()
	}
}
