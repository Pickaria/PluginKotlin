package fr.pickaria

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

class Main: JavaPlugin(), Listener {
	override fun onEnable() {
		super.onEnable()
		config.addDefault("coucou", true)
		config.options().copyDefaults(true)
		saveConfig()
		server.logger.log(Level.INFO, "Loaded")
		server.pluginManager.registerEvents(Test(), this)
		getCommand("lol")?.setExecutor(Command()) ?: println("Command Not found")
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
		server.logger.log(Level.INFO, "Unloaded")
	}
}