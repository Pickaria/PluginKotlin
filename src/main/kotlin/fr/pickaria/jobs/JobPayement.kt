package fr.pickaria.jobs

import fr.pickaria.Main
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.Bukkit.getLogger
import java.time.Instant
import kotlin.math.pow

val lastPayment = mutableMapOf<Player, Long>()
const val LAST_PAYMENT_DELAY = 200L

fun jobPayPlayer(player: Player, amount: Double): Boolean {
	val now = System.currentTimeMillis() // This uses 32 bit, alert for future us

	if (now - (lastPayment[player] ?: 0L) < LAST_PAYMENT_DELAY) {
		return false
	}
	lastPayment[player] = now

	return if (Main.economy.depositPlayer(player, amount).type === EconomyResponse.ResponseType.SUCCESS) {
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent("§6+ ${Main.economy.format(amount)}"))

		val location = player.location
		player.playSound(location, Sound.ITEM_ARMOR_EQUIP_CHAIN, 1f, 1f)
		true
	} else {
		getLogger().severe("Error in payement of player ${player.name} [UUID: ${player.uniqueId}]. Amount: $amount")
		false
	}
}

fun jobPayPlayer(player: Player, amount: Double, job: JobEnum, experienceToGive: Int = 0) {
	val experience = Main.jobController.getFromCache(player.uniqueId, job)?.level ?: 0
	val level = Main.jobController.getLevelFromExperience(job, experience)

	if (jobPayPlayer(player, amount * job.revenueIncrease.pow(level)) && experienceToGive > 0) {
		Main.jobController.addExperienceAndAnnounce(player, job, experienceToGive)
	}
}
