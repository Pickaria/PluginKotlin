package fr.pickaria.economy

import com.github.shynixn.mccoroutine.SuspendingCommandExecutor
import fr.pickaria.Main
import fr.pickaria.model.economy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit.getServer
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.ktorm.entity.forEach
import java.time.Instant

class BaltopCommand : SuspendingCommandExecutor {
	companion object {
		const val PAGE_SIZE = 8
		const val BALTOP_SORT_DELAY = 60L // Delay between baltop sorts in seconds
	}

	private var top: List<Pair<String?, Double>> = mutableListOf()
	private var lastUpdate: Instant = Instant.MIN // Stores the last time the baltop was sorted

	override suspend fun onCommand(
		sender: CommandSender,
		command: Command,
		label: String,
		args: Array<out String>
	): Boolean {
		val page = try {
			(args[0].toInt() - 1).coerceAtLeast(0)
		} catch (_: ArrayIndexOutOfBoundsException) {
			0
		} catch (_: NumberFormatException) {
			0
		}

		val min = page * PAGE_SIZE
		val players = getServer().offlinePlayers

		if (min > players.size) {
			sender.sendMessage("§cIl n'y a pas autant de pages.")
			return true
		}

		CoroutineScope(Dispatchers.Default).launch {
			// Special treatment for PickariaEconomy
			if (Main.economy is PickariaEconomy) {
				sender.sendMessage("Récupération des comptes joueurs, veuillez patienter...")
				with(Main.economy as PickariaEconomy) {
					if (Main.database.economy.totalRecords > this.cache.size) {
						Main.database.economy.forEach {
							this.cache[it.playerUniqueId] = it
						}
					}
				}
			}

			val now = Instant.now()
			if (now.isAfter(lastUpdate.plusSeconds(BALTOP_SORT_DELAY))) {
				sender.sendMessage("Mise en cache du baltop, veuillez patienter...")

				top = players.filter { Main.economy.hasAccount(it) }
					.map { Pair(it.name, Main.economy.getBalance(it)) }
					.sortedByDescending { it.second }

				lastUpdate = now
			}

			val maxPage = top.size / PAGE_SIZE
			val component = TextComponent()

			if (maxPage >= page) {
				component.addExtra("§6==== Baltop (${page + 1}/${maxPage + 1}) ====")
				val max = (min + PAGE_SIZE - 1).coerceAtMost(top.size - 1)

				top.slice(min..max)
					.forEachIndexed { index, it ->
						component.addExtra("\n§f${index + 1 + min}. §7${it.first}, ${Main.economy.format(it.second)}")
					}

				if (maxPage != page) {
					component.addExtra("\n§7Tapez §6/baltop ${page + 2}§7 pour lire la page suivante.")
				}
			} else {
				component.addExtra("§cIl n'y a pas autant de pages.")
			}

			sender.spigot().sendMessage(component)
		}

		return true
	}
}