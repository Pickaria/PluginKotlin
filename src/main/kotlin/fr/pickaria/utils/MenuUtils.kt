package fr.pickaria.utils

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

fun createMenuItem(material: Material?, name: String?, vararg lore: String?): ItemStack {
    val itemStack = ItemStack(material!!, 1)

    itemStack.itemMeta = itemStack.itemMeta!!.apply{
        this.setDisplayName(name)
        this.lore = listOf(*lore)
    }

    return itemStack
}