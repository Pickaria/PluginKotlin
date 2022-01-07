package fr.pickaria.coins

import fr.pickaria.Main
import kotlinx.coroutines.*
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import net.milkbowl.vault.economy.EconomyResponse
import net.minecraft.nbt.NBTTagByte
import net.minecraft.nbt.NBTTagDouble
import org.bukkit.Bukkit.getLogger
import org.bukkit.Bukkit.getServer
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.ItemMergeEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import java.lang.Runnable
import kotlin.random.Random


class Coin(val plugin: Plugin): Listener {
	companion object {
		fun dropCoin(location: Location): Item? {
			return dropCoin(location, 1.0, 4.0)
		}

		fun dropCoin(location: Location, min: Double, max: Double): Item? {
			val amount = if (min == max) {
				min
			} else {
				Random.nextDouble(min, max)
			}
			return dropCoin(location, amount)
		}

		fun dropCoin(location: Location, amount: Double): Item? {
			val stack = createCoin(amount)
			val item = location.world?.dropItemNaturally(location, stack) ?: let {
				getLogger().warning("Could not drop coin.")
				return null
			}
			item.customName = Main.economy.format(amount)
			item.isCustomNameVisible = true
			return item
		}

		private fun createCoin(amount: Double): ItemStack {
			val itemStack = ItemStack(Material.SUNFLOWER, 1)

			val itemMeta = itemStack.itemMeta
			itemMeta?.setDisplayName("§6Coin")
			val lore = itemMeta?.lore ?: ArrayList()
			lore.add("§7Valeur : §6${Main.economy.format(amount)}")
			itemMeta?.lore = lore
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

			return (compound.c("value") as? NBTTagDouble)?.i() ?: 0.0
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

			return (compound.c("isCoin") as? NBTTagByte)?.h() == (1).toByte()
		}
	}

	@EventHandler
	fun onEntityPickupItem(event: EntityPickupItemEvent) {
		val itemStack = event.item.itemStack

		if (isCoin(itemStack)) {
			event.isCancelled = true

			if (event.entity is Player && !event.entity.isDead) {
				val player = event.entity as Player
				val amount = getCoinValue(itemStack)
				val response = Main.economy.depositPlayer(player, amount)

				if (response.type == EconomyResponse.ResponseType.SUCCESS) {
					player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent("§6+ ${Main.economy.format(response.amount)}"))
					player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_CHAIN, 1f, 1f)
					event.item.remove()
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

	@EventHandler
	fun onInventoryPickupItem(event: PlayerDropItemEvent) {
		if (isCoin(event.itemDrop.itemStack)) {
			event.itemDrop.customName = Main.economy.format(getCoinValue(event.itemDrop.itemStack))
			event.itemDrop.isCustomNameVisible = true
		}
	}

	@EventHandler
	fun onBlockPlace(event: BlockPlaceEvent) {
		if (isCoin(event.itemInHand)) event.isCancelled = true
	}

	@EventHandler
	fun onPlayerDeath(event: PlayerDeathEvent) {
		val player = event.entity
		val balance = Main.economy.getBalance(player)

		val percentage = Random.nextDouble(0.05, 0.10)
		val response  = Main.economy.withdrawPlayer(player, balance * percentage)
		if (response.type == EconomyResponse.ResponseType.SUCCESS) {
			dropCoin(player.location, response.amount)
		}
	}

	@EventHandler
	fun onPlayerClicks(event: PlayerInteractEvent){
		if(event.action == Action.RIGHT_CLICK_BLOCK){
			if(event.item?.type == Material.STICK){
				val coin = dropCoin(event.player.location) ?: return
				val a = getCoinValue(coin.itemStack)
				val res = Main.economy.depositPlayer(event.player, a)
				if(res.type == EconomyResponse.ResponseType.SUCCESS) {
					event.player.spigot()
						.sendMessage(ChatMessageType.ACTION_BAR, TextComponent("§6+ ${Main.economy.format(res.amount)}"))
					event.player.playSound(event.player.location, Sound.ITEM_ARMOR_EQUIP_CHAIN, 1f, 1f)
					object: BukkitRunnable(){
						override fun run() {
							coin.remove()
						}
					}.runTaskTimer(plugin, 1, 20)
				}
			}
		}
	}
}