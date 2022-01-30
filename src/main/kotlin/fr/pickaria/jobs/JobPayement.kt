package fr.pickaria.jobs

import fr.pickaria.Main
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.Bukkit.getLogger
import kotlin.math.pow
import kotlin.random.Random

fun jobPayPlayer(player: Player, min: Double, max: Double) {
	jobPayPlayer(player, Random.nextDouble(min, max))
}

fun jobPayPlayer(player: Player, amount: Double) {
	if (Main.economy.depositPlayer(player, amount).type === EconomyResponse.ResponseType.SUCCESS) {
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent("§6+ ${Main.economy.format(amount)}"))

		val location = player.location
		player.playSound(location, Sound.ITEM_ARMOR_EQUIP_CHAIN, 1f, 1f)
	} else {
		getLogger().severe("Error in payement of player ${player.name} [UUID: ${player.uniqueId}]. Amount: $amount")
	}
}

fun jobPayPlayer(player: Player, amount: Double, job: JobEnum) {
	val experience = Main.jobController.getFromCache(player.uniqueId, job)?.level ?: 0
	val level = Main.jobController.getLevelFromExperience(job, experience)
	jobPayPlayer(player, amount * job.revenueIncrease.pow(level))
}