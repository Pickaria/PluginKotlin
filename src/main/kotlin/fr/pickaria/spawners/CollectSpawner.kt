package fr.pickaria.spawners

import com.meowj.langutils.lang.LanguageHelper
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.CreatureSpawner
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta

class CollectSpawner : Listener {
    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if (event.block.type != Material.SPAWNER) return

        if (event.player.inventory.itemInMainHand.getEnchantmentLevel(Enchantment.SILK_TOUCH) >= 2) {
            event.expToDrop = 0

            val spawner = event.block.state as CreatureSpawner
            val entityType = spawner.spawnedType
            val location: Location = event.block.location
            val itemStack = ItemStack(Material.SPAWNER)

            // Set entity type
            val blockStateMeta = itemStack.itemMeta as BlockStateMeta?
            val blockState = blockStateMeta!!.blockState as CreatureSpawner
            blockState.spawnedType = entityType
            blockStateMeta.blockState = blockState
            itemStack.itemMeta = blockStateMeta

            // Set custom name
            val itemMeta = itemStack.itemMeta
            val name = java.lang.String.format("§eCage à %s", LanguageHelper.getEntityName(entityType, "fr_FR"))
            itemMeta?.setDisplayName(name)
            itemStack.itemMeta = itemMeta

            if (!event.isCancelled) {
                location.world!!.dropItemNaturally(location, itemStack)
            }
        }
    }
}