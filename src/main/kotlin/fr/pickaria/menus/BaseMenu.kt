package fr.pickaria.menus

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.*
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack


class BaseMenu : Listener {
	private var opener: HumanEntity? = null
	private var inventory: Inventory

	constructor(title: String) {
		inventory = Bukkit.createInventory(null, 54, title)
	}

	constructor(title: String, opener: HumanEntity?, previousMenu: BaseMenu?, size: Int = 54) {
		inventory = Bukkit.createInventory(null, size, title)
		this.opener = opener
	}

	private fun buildView(view: InventoryView) {
		view.setItem(0, ItemStack(Material.ACACIA_BOAT))
	}

	fun openInventory(entity: HumanEntity?) {
		(opener ?: entity)?.openInventory(inventory)?.let { buildView(it) }
	}

	@EventHandler
	fun onInventoryClick(e: InventoryClickEvent) {
		if (inventory === e.clickedInventory || (inventory === e.clickedInventory && (e.action == InventoryAction.MOVE_TO_OTHER_INVENTORY || e.action == InventoryAction.HOTBAR_SWAP))) {
			e.isCancelled = true
		}
	}

	@EventHandler
	fun onInventoryDrag(e: InventoryDragEvent) {
		if (e.inventory === inventory) {
			e.isCancelled = true
		}
	}
}