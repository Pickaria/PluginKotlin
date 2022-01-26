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

class BaltopCommand : SuspendingCommandExecutor {
	companion object {
		const val PAGE_SIZE = 8
	}

	private lateinit var top: List<Pair<String?, Double>>

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
			if (Main.economy is PickariaEconomy) {
				with(Main.economy as PickariaEconomy) {
					if (Main.database.economy.totalRecords > this.cache.size) {
						sender.sendMessage("Mise en cache du baltop, veuillez patienter...")

						Main.database.economy.forEach {
							this.cache[it.playerUniqueId] = it
						}

						top = players.filter { Main.economy.hasAccount(it) }
							.map { Pair(it.name, Main.economy.getBalance(it)) }
							.sortedByDescending { it.second }
					}
				}
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