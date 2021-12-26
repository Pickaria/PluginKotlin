package fr.pickaria.vote

import fr.pickaria.Main
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit.getServer
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable


class VoteCommand(plugin: Main) : CommandExecutor {
	init {
		val server = getServer()
		object : BukkitRunnable() {
			override fun run() {
				server.broadcastMessage("§7Votez pour le serveur et contribuez à son développement et obtenez une récompense pour votre vote ! §7Utiliser §6/vote§7 dans le chat pour voir la liste des sites de votes.")
			}
		}.runTaskTimer(plugin, 72000 /* 1 hour */, 144000 /* 2 hours */)
	}

	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
		if (sender is Player) {
			val c1: TextComponent = voteComponent(
				"Top Serveurs",
				"https://vote.top-serveurs.net/minecraft/vote/pickaria-serveur-survie-118"
			)
			val c2: TextComponent =
				voteComponent("Serveur Privé", "https://serveur-prive.net/minecraft/pickaria-10765/vote")
			val msg = TextComponent("§7Liste des sites où Pickaria est référencé :")
			msg.addExtra(c1)
			msg.addExtra(c2)
			sender.spigot().sendMessage(msg)
		}

		// If the player (or console) uses our command correct, we can return true
		return true
	}

	private fun voteComponent(name: String, url: String): TextComponent {
		val c = TextComponent(name)
		c.color = ChatColor.GOLD
		c.clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, url)
		c.hoverEvent = HoverEvent(
			HoverEvent.Action.SHOW_TEXT,
			ComponentBuilder("Clique pour ouvrir le site").create()
		)
		val msg = TextComponent("\n§7 - ")
		msg.addExtra(c)
		return msg
	}
}