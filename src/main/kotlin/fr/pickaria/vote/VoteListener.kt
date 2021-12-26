package fr.pickaria.vote

import com.vexsoftware.votifier.model.VotifierEvent
import fr.pickaria.Main
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.Bukkit.getServer
import org.bukkit.OfflinePlayer
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.*


class VoteListener: Listener {
	private var server: Server = getServer()
	private var random: Random = Random()

	@EventHandler
	fun onVote(event: VotifierEvent) {
		val amount: Double = random.nextDouble() * 90 + 10
		val username: String = event.vote.username
		val player: OfflinePlayer = server.getOfflinePlayer(username)
		val res = Main.economy.depositPlayer(player, amount)
		if (player.isOnline && res.type == EconomyResponse.ResponseType.SUCCESS) {
			(player as Player).sendMessage(
				"§7Merci §6$username§7 pour ton soutien ! Pour te remercier, §6${Main.economy.format(res.amount)}§7 ont été déposés sur ton compte."
			)
		}
	}
}