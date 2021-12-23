package fr.pickaria.coins

import fr.pickaria.Main
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.ItemMergeEvent
import org.bukkit.inventory.ItemStack

class Coin: Listener {
	companion object {
		fun dropCoin(location: Location, amount: Double) {
			val itemStack = ItemStack(Material.SUNFLOWER, amount.toInt())

			val itemMeta = itemStack.itemMeta
			itemMeta?.setDisplayName("§6Coin")
			itemStack.itemMeta = itemMeta

			val item = location.world?.dropItemNaturally(location, itemStack)
			item?.customName = amount.toString()
			item?.isCustomNameVisible = true
		}
	}

	@EventHandler
	fun onEntityPickupItem(e: EntityPickupItemEvent) {
		val itemStack = e.item.itemStack

		if (itemStack.type == Material.SUNFLOWER && itemStack.itemMeta?.displayName == "§6Coin") {
			e.isCancelled = true

			if (e.entity is Player) {
				val player = e.entity as Player
				val amount: Double = e.item.customName?.toDouble() ?: return
				val response = Main.economy.depositPlayer(player, amount)

				if (response.type == EconomyResponse.ResponseType.SUCCESS) {
					player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent("§6+ ${Main.economy.format(response.amount)}"))
					e.item.remove()
				}
			}
		}
	}

	@EventHandler
	fun onItemMerge(e: ItemMergeEvent) {
		val itemStack1 = e.entity.itemStack
		val itemStack2 = e.target.itemStack

		if (itemStack1.type == Material.SUNFLOWER && itemStack1.itemMeta?.displayName == "§6Coin" &&
			itemStack2.type == Material.SUNFLOWER && itemStack2.itemMeta?.displayName == "§6Coin") {

			e.target.customName = ((e.entity.customName?.toDouble() ?: 0.0) + (e.target.customName?.toDouble() ?: 0.0)).toString()
		}
	}
}