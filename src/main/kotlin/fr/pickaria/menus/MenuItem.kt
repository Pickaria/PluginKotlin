package fr.pickaria.menus

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer

class MenuItem {
	var itemStack: ItemStack
	var callback: Consumer<InventoryClickEvent>? = null

	constructor(material: Material, name: String, vararg lore: String) {
		itemStack = ItemStack(material, 1)

		itemStack.itemMeta = itemStack.itemMeta!!.apply {
			this.setDisplayName(name)
			this.lore = listOf(*lore)
		}
	}

	constructor(material: Material?) {
		itemStack = ItemStack(material!!, 1)
	}

	constructor(material: Material, name: String, lore: List<String>) {
		itemStack = ItemStack(material, 1)

		itemStack.itemMeta = itemStack.itemMeta!!.apply {
			this.setDisplayName(name)
			this.lore = lore
		}
	}

	/**
	 * Set the callback called when the MenuItem is clicked
	 * @param callback
	 * @return
	 */
	fun setCallback(callback: Consumer<InventoryClickEvent>): MenuItem {
		this.callback = callback
		return this
	}

	fun setEnchanted(): MenuItem {
		itemStack.addUnsafeEnchantment(Enchantment.MENDING, 1)

		itemStack.itemMeta = itemStack.itemMeta!!.apply {
			this.itemFlags.plus(ItemFlag.HIDE_ENCHANTS)
		}

		return this
	}
}