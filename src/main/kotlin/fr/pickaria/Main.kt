package fr.pickaria

import com.github.shynixn.mccoroutine.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.registerSuspendingEvents
import com.github.shynixn.mccoroutine.setSuspendingExecutor
import fr.pickaria.coins.Coin
import fr.pickaria.economy.BaltopCommand
import fr.pickaria.economy.MoneyCommand
import fr.pickaria.economy.PayCommand
import fr.pickaria.jobs.JobCommand
import fr.pickaria.jobs.JobController
import fr.pickaria.economy.PickariaEconomy
import kotlinx.coroutines.*
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.plugin.ServicePriority
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.support.postgresql.PostgreSqlDialect
import java.sql.SQLException
import java.util.logging.Level


class Main: SuspendingJavaPlugin() {
	companion object {
		lateinit var database: Database
		lateinit var economy: Economy
		lateinit var jobController: JobController
	}

	override fun onEnable() {
		super.onEnable()

		saveDefaultConfig()

		logger.info("Main on thread ${Thread.currentThread().name}")

		try {
			val databaseConfiguration = this.config.getConfigurationSection("database")

			val url = databaseConfiguration?.getString("url") ?: "localhost"
			val user = databaseConfiguration?.getString("user") ?: "admin"
			val password = databaseConfiguration?.getString("password") ?: "admin"

			Class.forName("org.postgresql.Driver")
			database = Database.connect(url, user = user, password = password, dialect = PostgreSqlDialect())
		} catch (e: SQLException) {
			e.printStackTrace()
			server.logger.severe("An error occurred while trying to connect to the database, some features may not function properly")
		}

		server.pluginManager.registerEvents(Test(), this)

		// Economy
		if (setupEconomy()) {
			// Economy commands
			getCommand("money")?.setExecutor(MoneyCommand()) ?: server.logger.warning("Command money could not be registered")
			getCommand("pay")?.setExecutor(PayCommand()) ?: server.logger.warning("Command pay could not be registered")
			getCommand("baltop")?.setSuspendingExecutor(BaltopCommand()) ?: server.logger.warning("Command baltop could not be registered")

			// Jobs
			jobController = JobController(this)
			getCommand("job")?.setExecutor(JobCommand()) ?: server.logger.warning("Command job could not be registered")
			server.pluginManager.registerEvents(Coin(), this)
		}

		server.logger.info("Pickaria plugin enabled")
	}

	private fun setupEconomy(): Boolean {
		if (server.pluginManager.getPlugin("Vault") == null) {
			logger.warning("VaultAPI not found, economy is not available")
			return false
		}

		val rsp = server.servicesManager.getRegistration(Economy::class.java)

		if (rsp == null) {
			economy = PickariaEconomy(this)
			Bukkit.getServicesManager().register(Economy::class.java, economy, this, ServicePriority.Normal)
			server.pluginManager.registerSuspendingEvents(economy as PickariaEconomy, this)

			logger.info("Pickaria is handling economy")
		} else {
			economy = rsp.provider
			logger.info("Third party plugin is handling economy")
		}
		return true
	}

	override fun onDisable() {
		super.onDisable()

		if (economy is PickariaEconomy) {
			(economy as PickariaEconomy).flushAllEntities(true)
			jobController.flushAllEntities(true)
		}

		logger.log(Level.INFO, "Pickaria plugin disabled")
		Dispatchers.DB.close()
	}
}

val Dispatchers.DB: ExecutorCoroutineDispatcher
	get() = newSingleThreadContext("Database")

fun <T>DBAsync(block: suspend () -> T){
	CoroutineScope(Dispatchers.DB).launch {
		block.invoke()
	}
}

fun <T : Entity<T>>Entity<T>.asyncFlushChanges(){
	CoroutineScope(Dispatchers.IO).launch{
		flushChanges()
	}
}