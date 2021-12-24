package fr.pickaria

import fr.pickaria.tablist.playerList
import net.milkbowl.vault.chat.Chat
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*
import java.util.logging.Level


class Main: JavaPlugin(), Listener {
	companion object {
		lateinit var connection: Connection
		lateinit var chat: Chat
	}

	override fun onEnable() {
		super.onEnable()

		saveDefaultConfig()

		try {
			val database = this.config.getConfigurationSection("database")
			val url = database?.getString("url") ?: "localhost"
			val user = database?.getString("user") ?: "admin"
			val password = database?.getString("password") ?: "admin"

			val props = Properties()
			props.setProperty("user", user)
			props.setProperty("password", password)

			connection = DriverManager.getConnection(url, props)
		} catch (e: SQLException) {
			e.printStackTrace()
		}

		setupChat()

		server.pluginManager.registerEvents(Test(), this)
		getCommand("lol")?.setExecutor(Command()) ?: server.logger.log(Level.WARNING, "Command could not be registered")

		playerList(this)

		server.logger.log(Level.INFO, "Pickaria plugin enabled")
	}

	private fun setupChat() {
		val rsp = server.servicesManager.getRegistration(
			Chat::class.java
		)
		chat = rsp!!.provider
	}

	override fun onDisable() {
		super.onDisable()
		server.logger.log(Level.INFO, "Pickaria plugin disabled")
	}
}