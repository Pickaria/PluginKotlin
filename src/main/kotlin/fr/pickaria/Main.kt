package fr.pickaria

import org.bukkit.event.EventHandler
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

		server.pluginManager.registerEvents(Test(), this)
		getCommand("lol")?.setExecutor(Command()) ?: server.logger.log(Level.WARNING, "Command could not be registered")

		server.logger.log(Level.INFO, "Pickaria plugin enabled")
	}

	var admin = 0

	@EventHandler
	fun onAdminJoinEvent(event: AdminJoinEvent){
		admin++
		server.logger.log(Level.WARNING, "Admin ${event.admin} Logged")
		server.logger.log(Level.WARNING, "$admin connected")
	}

	@EventHandler
	fun onAdminLeaveEvent(event: AdminLeaveEvent){
		admin--
		server.logger.log(Level.WARNING, "Admin ${event.admin} Logged out")
		server.logger.log(Level.WARNING, "$admin connected")
	}

	override fun onDisable() {
		super.onDisable()
		server.logger.log(Level.INFO, "Pickaria plugin disabled")
	}
}