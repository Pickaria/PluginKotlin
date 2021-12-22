package fr.pickaria

import fr.pickaria.economy.MoneyCommand
import fr.pickaria.economy.PayCommand
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*
import java.util.logging.Level
import fr.pickaria.economy.Economy as PickariaEconomy


class Main: JavaPlugin() {
	companion object {
		lateinit var connection: Connection
		lateinit var economy: PickariaEconomy
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

			Class.forName("org.postgresql.Driver")
			connection = DriverManager.getConnection(url, props)
		} catch (e: SQLException) {
			e.printStackTrace()
		}

		server.pluginManager.registerEvents(Test(), this)
		getCommand("lol")?.setExecutor(Command()) ?: server.logger.log(Level.WARNING, "Command could not be registered")
		getCommand("money")?.setExecutor(MoneyCommand()) ?: server.logger.log(Level.WARNING, "Command could not be registered")
		getCommand("pay")?.setExecutor(PayCommand()) ?: server.logger.log(Level.WARNING, "Command could not be registered")

		economy = PickariaEconomy()
		Bukkit.getServicesManager().register(Economy::class.java, economy, this, ServicePriority.Normal)

		server.logger.log(Level.INFO, "Pickaria plugin enabled")
	}

	override fun onDisable() {
		super.onDisable()
		server.logger.log(Level.INFO, "Pickaria plugin disabled")
	}
}