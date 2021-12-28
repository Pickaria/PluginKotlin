package fr.pickaria

import fr.pickaria.coins.Coin
import fr.pickaria.economy.BaltopCommand
import fr.pickaria.economy.MoneyCommand
import fr.pickaria.economy.PayCommand
import fr.pickaria.economy.PickariaEconomy
import fr.pickaria.enchant.Anvil
import fr.pickaria.jobs.JobCommand
import fr.pickaria.jobs.JobController
import fr.pickaria.spawners.CollectSpawner
import fr.pickaria.tablist.ChatFormat
import fr.pickaria.tablist.Motd
import fr.pickaria.tablist.PlayerJoin
import fr.pickaria.tablist.playerList
import fr.pickaria.vote.VoteCommand
import fr.pickaria.vote.VoteListener
import net.milkbowl.vault.chat.Chat
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
		lateinit var database: Database
		lateinit var chat: Chat
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

			// Setup jobs
			JobController(this)
			server.pluginManager.registerEvents(Coin(), this)
			getCommand("job")?.setExecutor(JobCommand()) ?: server.logger.log(Level.WARNING, "Command could not be registered")

			// Votes
			server.pluginManager.registerEvents(VoteListener(), this)
			getCommand("vote")?.setExecutor(VoteCommand(this)) ?: server.logger.log(Level.WARNING, "Command could not be registered")

			// Player list
			playerList(this)
		}

		// Player list
		setupChat()
		server.pluginManager.registerEvents(PlayerJoin(), this)
		server.pluginManager.registerEvents(ChatFormat(), this)
		server.pluginManager.registerEvents(Motd(), this)

		// Spawners
		server.pluginManager.registerEvents(Anvil(), this)
		server.pluginManager.registerEvents(CollectSpawner(), this)

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