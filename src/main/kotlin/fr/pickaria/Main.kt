package fr.pickaria

import fr.pickaria.economy.BaltopCommand
import fr.pickaria.economy.MoneyCommand
import fr.pickaria.economy.PayCommand
import fr.pickaria.randomtp.RandomCommand
import fr.pickaria.economy.PickariaEconomy
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import org.ktorm.database.Database
import org.ktorm.support.postgresql.PostgreSqlDialect
import java.sql.SQLException
import java.util.logging.Level


class Main: JavaPlugin() {
	companion object {
		lateinit var connection: Connection
		lateinit var economy: PickariaEconomy
		lateinit var database: Database
		lateinit var economy: Economy
	}

	override fun onEnable() {
		super.onEnable()

		saveDefaultConfig()

		try {
			val databaseConfiguration = this.config.getConfigurationSection("database")

			val url = databaseConfiguration?.getString("url") ?: "localhost"
			val user = databaseConfiguration?.getString("user") ?: "admin"
			val password = databaseConfiguration?.getString("password") ?: "admin"

			Class.forName("org.postgresql.Driver")
			database = Database.connect(url, user = user, password = password, dialect = PostgreSqlDialect())
		} catch (e: SQLException) {
			e.printStackTrace()
		}

		// Economy
		if (setupEconomy()) {
			// Economy commands
			getCommand("money")?.setExecutor(MoneyCommand()) ?: server.logger.log(Level.WARNING, "Command could not be registered")
			getCommand("pay")?.setExecutor(PayCommand()) ?: server.logger.log(Level.WARNING, "Command could not be registered")
			getCommand("baltop")?.setExecutor(BaltopCommand()) ?: server.logger.log(Level.WARNING, "Command could not be registered")

			// Teleport commands
			getCommand("tprandom")?.setExecutor(RandomCommand()) ?: server.logger.log(Level.WARNING, "Command could not be registered")
		}

		server.logger.log(Level.INFO, "Pickaria plugin enabled")
	}

	private fun setupEconomy(): Boolean {
		if (server.pluginManager.getPlugin("Vault") == null) {
			server.logger.log(Level.WARNING, "VaultAPI not found, economy is not available")
			return false
		}
		val rsp = server.servicesManager.getRegistration(
			Economy::class.java
		)
		if (rsp == null) {
			economy = PickariaEconomy()
			Bukkit.getServicesManager().register(Economy::class.java, economy, this, ServicePriority.Normal)

			server.logger.log(Level.INFO, "Pickaria is handling economy")
		} else {
			economy = rsp.provider
			server.logger.log(Level.INFO, "Third party plugin is handling economy")
		}
		return true
	}

	override fun onDisable() {
		super.onDisable()
		server.logger.log(Level.INFO, "Pickaria plugin disabled")
	}
}