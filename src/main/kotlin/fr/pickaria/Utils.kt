package fr.pickaria

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

const val TELEPORT_COOLDOWN = 100L

fun cooldownTeleport(player: Player, location: Location) {
	player.sendMessage("§7Téléportation dans ${TELEPORT_COOLDOWN / 20} secondes.")

	object : BukkitRunnable() {
		override fun run() {
			player.teleport(location)
		}
	}.runTaskLater(Main.plugin, TELEPORT_COOLDOWN)
}