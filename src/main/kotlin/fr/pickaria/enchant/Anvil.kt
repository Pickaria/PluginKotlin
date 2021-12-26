package fr.pickaria.enchant

import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.meta.EnchantmentStorageMeta


class Anvil: Listener {
    @EventHandler
    fun onAnvilEvent(event: PrepareAnvilEvent) {
        val anvilItems = event.inventory.contents
        if (anvilItems.size < 2) return
        val item1 = anvilItems[0]
        val item2 = anvilItems[1]
        if (item1 == null || item2 == null) return

        event.inventory.maximumRepairCost = event.inventory.repairCost + 1

        // Announce repair cost beyond limit
        val textComponent = TextComponent(String.format("§7Le coût de réparation de cet objet est de §r§6§l%s §r§7niveaux.", event.inventory.repairCost))

        (event.view.player as Player).spigot().sendMessage(
            ChatMessageType.ACTION_BAR,
            textComponent
        )

        val enchant1: Map<Enchantment, Int> = if (item1.type == Material.ENCHANTED_BOOK) {
            (item1.itemMeta as EnchantmentStorageMeta?)!!.storedEnchants
        } else {
            item1.enchantments
        }
        val enchant2: Map<Enchantment, Int> = if (item2.type == Material.ENCHANTED_BOOK) {
            (item2.itemMeta as EnchantmentStorageMeta?)!!.storedEnchants
        } else {
            item2.enchantments
        }

        // Enchantment calculation
        val result = event.result ?: return
        for (enchant in enchant2.entries) {
            if (enchant.key != Enchantment.SILK_TOUCH) continue
            var level = enchant.value
            // If enchant is on both items and is same level
            if (enchant1.containsKey(enchant.key) && enchant1[enchant.key] == level) level++

            // Silk touch level 2 max
            if (level > 2) level = 2
            if (result.type == Material.ENCHANTED_BOOK) {
                val bookMeta = result.itemMeta as EnchantmentStorageMeta?
                bookMeta!!.addStoredEnchant(enchant.key, level, true)
                result.itemMeta = bookMeta
            } else {
                result.addUnsafeEnchantment(enchant.key, level)
            }
        }
        event.result = result
    }
}