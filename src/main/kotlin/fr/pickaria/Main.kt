package fr.pickaria

import fr.pickaria.economy.BaltopCommand
import fr.pickaria.economy.MoneyCommand
import fr.pickaria.economy.PayCommand
import fr.pickaria.vote.VoteCommand
import fr.pickaria.vote.VoteListener
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import org.ktorm.database.Database
import java.sql.SQLException
import java.util.logging.Level
import fr.pickaria.economy.PickariaEconomy as PickariaEconomy


class Main: JavaPlugin() {
	companion object {
		lateinit var database: Database
		lateinit var economy: PickariaEconomy
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
			database = Database.connect(url, user = user, password = password)
		} catch (e: SQLException) {
			e.printStackTrace()
		}

		server.pluginManager.registerEvents(Test(), this)
		getCommand("lol")?.setExecutor(Command()) ?: server.logger.log(Level.WARNING, "Command could not be registered")
		getCommand("money")?.setExecutor(MoneyCommand()) ?: server.logger.log(Level.WARNING, "Command could not be registered")
		getCommand("pay")?.setExecutor(PayCommand()) ?: server.logger.log(Level.WARNING, "Command could not be registered")
		getCommand("baltop")?.setExecutor(BaltopCommand()) ?: server.logger.log(Level.WARNING, "Command could not be registered")

		server.pluginManager.registerEvents(VoteListener(), this)
		getCommand("vote")?.setExecutor(VoteCommand(this)) ?: server.logger.log(Level.WARNING, "Command could not be registered")

		economy = PickariaEconomy()
		Bukkit.getServicesManager().register(Economy::class.java, economy, this, ServicePriority.Normal)

		server.logger.log(Level.INFO, "Pickaria plugin enabled")
	}

	override fun onDisable() {
		super.onDisable()
		server.logger.log(Level.INFO, "Pickaria plugin disabled")
	}
}