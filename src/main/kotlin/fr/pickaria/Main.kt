package fr.pickaria

import com.github.shynixn.mccoroutine.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.registerSuspendingEvents
import com.github.shynixn.mccoroutine.setSuspendingExecutor
import fr.pickaria.economy.BaltopCommand
import fr.pickaria.economy.MoneyCommand
import fr.pickaria.economy.PayCommand
import fr.pickaria.randomtp.RandomCommand
import fr.pickaria.economy.PickariaEconomy
import fr.pickaria.enchant.Anvil
import fr.pickaria.jobs.JobCommand
import fr.pickaria.jobs.JobController
import fr.pickaria.menus.MenuCommand
import fr.pickaria.menus.MenuController
import fr.pickaria.spawners.CollectSpawner
import fr.pickaria.tablist.ChatFormat
import fr.pickaria.tablist.Motd
import fr.pickaria.tablist.PlayerJoin
import fr.pickaria.tablist.playerList
import fr.pickaria.vote.VoteCommand
import fr.pickaria.vote.VoteListener
import net.milkbowl.vault.chat.Chat
import kotlinx.coroutines.*
import fr.pickaria.homes.DelHomeCommand
import fr.pickaria.homes.HomeCommand
import fr.pickaria.homes.HomeController
import fr.pickaria.homes.SetHomeCommand
import fr.pickaria.tpa.TpaCommand
import fr.pickaria.tpa.TpacceptCommand
import fr.pickaria.tpa.TpdenyCommand
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
		var chat: Chat? = null
		lateinit var economy: Economy
		lateinit var jobController: JobController
		lateinit var menuController: MenuController
		lateinit var homeController: HomeController
		lateinit var plugin: Main
	}

	override fun onEnable() {
		super.onEnable()

		plugin = this

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

		menuController = MenuController(this)
		//getCommand("menu")?.setExecutor(MenuCommand()) ?: server.logger.warning("Command menu could not be registered")

		homeController = HomeController()

		// Economy
		if (setupEconomy()) {
			// Economy commands
			getCommand("money")?.setExecutor(MoneyCommand()) ?: server.logger.warning("Command money could not be registered")
			getCommand("pay")?.setExecutor(PayCommand()) ?: server.logger.warning("Command pay could not be registered")
			getCommand("baltop")?.setSuspendingExecutor(BaltopCommand()) ?: server.logger.warning("Command baltop could not be registered")

			// Jobs
			jobController = JobController(this)
			val jobCommand = JobCommand()
			getCommand("job")?.setExecutor(jobCommand) ?: server.logger.warning("Command job could not be registered")
			getCommand("jobs")?.setExecutor(jobCommand) ?: server.logger.warning("Command job could not be registered")

			// Votes
			server.pluginManager.registerEvents(VoteListener(), this)
			getCommand("vote")?.setExecutor(VoteCommand(this)) ?: server.logger.log(Level.WARNING, "Command could not be registered")

			// Player list
			playerList(this)
			
			// Teleport commands
			getCommand("tprandom")?.setExecutor(RandomCommand()) ?: server.logger.log(Level.WARNING, "Command could not be registered")

			// Home
			getCommand("sethome")?.setExecutor(SetHomeCommand()) ?: server.logger.log(Level.WARNING, "Command could not be registered")
			getCommand("home")?.setExecutor(HomeCommand()) ?: server.logger.log(Level.WARNING, "Command could not be registered")
			getCommand("delhome")?.setExecutor(DelHomeCommand()) ?: server.logger.log(Level.WARNING, "Command could not be registered")

			// TPA
			getCommand("tpa")?.setExecutor(TpaCommand()) ?: server.logger.log(Level.WARNING, "Command could not be registered")
			getCommand("tpaccept")?.setExecutor(TpacceptCommand()) ?: server.logger.log(Level.WARNING, "Command could not be registered")
			getCommand("tpdeny")?.setExecutor(TpdenyCommand()) ?: server.logger.log(Level.WARNING, "Command could not be registered")
		}

		setupChat()

		server.pluginManager.let{
			it.registerEvents(PlayerJoin(), this)

			it.registerEvents(ChatFormat(), this)
			it.registerEvents(Motd(), this)

			// Spawners
			it.registerEvents(Anvil(), this)
			it.registerEvents(CollectSpawner(), this)

			// Menus
			it.registerEvents(menuController, this)
		}

		server.logger.info("Pickaria plugin enabled")
	}

	private fun setupEconomy(): Boolean {
		server.pluginManager.getPlugin("Vault") ?: run{
			logger.warning("VaultAPI not found, economy is not available")
			return false
		}

		server.servicesManager.getRegistration(Economy::class.java)?.let {
			economy = it.provider
			logger.info("Third party plugin is handling economy")
		} ?: let{
			economy = PickariaEconomy(this)
			Bukkit.getServicesManager().register(Economy::class.java, economy, this, ServicePriority.Normal)
			server.pluginManager.registerSuspendingEvents(economy as PickariaEconomy, this)

			logger.info("Pickaria is handling economy")
		}
		return true
	}

	private fun setupChat(): Boolean {
		return server.servicesManager.getRegistration(Chat::class.java)?.let{
			chat = it.provider
			return true
		} ?: run {
			logger.warning("No chat provider found, some features may be disabled")
			false
		}

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

val Dispatchers.Menus: ExecutorCoroutineDispatcher
	get() = newSingleThreadContext("Menus")

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