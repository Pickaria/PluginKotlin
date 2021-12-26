package fr.pickaria.coins

import fr.pickaria.Main
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import net.milkbowl.vault.economy.EconomyResponse
import net.minecraft.nbt.NBTTagByte
import net.minecraft.nbt.NBTTagDouble
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.ItemMergeEvent
import org.bukkit.inventory.ItemStack


class Coin: Listener {
	companion object {
		fun dropCoin(location: Location, amount: Double) {
			val item = location.world?.dropItemNaturally(location, createCoin(amount)) ?: return println("Error")
			item.customName = Main.economy.format(amount)
			item.isCustomNameVisible = true
		}

		private fun createCoin(amount: Double): ItemStack {
			val itemStack = ItemStack(Material.SUNFLOWER, 1)

			val itemMeta = itemStack.itemMeta
			itemMeta?.setDisplayName("§6Coin")
			itemStack.itemMeta = itemMeta

			val coin = CraftItemStack.asNMSCopy(itemStack)
			val compound = coin.t() // Get compound or create if null
			compound.a("value", NBTTagDouble.a(amount)) // Add value to compound
			compound.a("isCoin", NBTTagByte.a(true)) // Add boolean
			coin.c(compound) // Set compound
			return CraftItemStack.asBukkitCopy(coin)
		}

		private fun getCoinValue(itemStack: ItemStack): Double {
			val coin = CraftItemStack.asNMSCopy(itemStack)
			val compound = coin.t()
			val tag = compound.c("value") as NBTTagDouble // Get tag
			return tag.i() // Get Double
		}

		private fun setCoinValue(itemStack: ItemStack, amount: Double): ItemStack {
			val coin = CraftItemStack.asNMSCopy(itemStack)
			val compound = coin.t() // Get compound or create if null
			compound.a("value", NBTTagDouble.a(amount)) // Add value to compound
			coin.c(compound) // Set compound
			return CraftItemStack.asBukkitCopy(coin)
		}

		private fun isCoin(itemStack: ItemStack): Boolean {
			val coin = CraftItemStack.asNMSCopy(itemStack)
			val compound = coin.t()
			val tag = compound.c("isCoin") as NBTTagByte // Get tag
			return tag.h() == (1).toByte() // Is equals to 1
		}
	}

	@EventHandler
	fun onEntityPickupItem(e: EntityPickupItemEvent) {
		val itemStack = e.item.itemStack

		if (isCoin(itemStack)) {
			e.isCancelled = true

			if (e.entity is Player) {
				val player = e.entity as Player

				val amount = getCoinValue(itemStack)

				val response = Main.economy.depositPlayer(player, amount)

				if (response.type == EconomyResponse.ResponseType.SUCCESS) {
					player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent("§6+ ${Main.economy.format(response.amount)}"))
					e.item.remove()
				}
			}
		}
	}

	@EventHandler
	fun onItemMerge(event: ItemMergeEvent) {
		val source = event.entity.itemStack
		val target = event.target.itemStack

		val sourceCoin = isCoin(source)
		val targetCoin = isCoin(target)

		if (sourceCoin || targetCoin) event.isCancelled = true

		if (sourceCoin && targetCoin) {
			val amount1 = getCoinValue(source)
			val amount2 = getCoinValue(target)

			val newStack = setCoinValue(target, amount1 + amount2)
			event.target.itemStack = newStack

			event.target.customName = Main.economy.format(amount1 + amount2)
			event.entity.remove()
		}
	}
}