package fr.pickaria.chestLock

import fr.pickaria.Main
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.type.WallSign
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

class ChestLock(plugin: Main) : Listener {
    /**
     * Anti-thief protection for chests.
     */
    companion object {
        lateinit var key: NamespacedKey

        val allLockable = setOf(
            Material.BARREL, // weird facing, have to find a proper way to lock them
            Material.CHEST,
            Material.ENDER_CHEST, // no interest but here it is
            Material.TRAPPED_CHEST
        ) union Tag.SHULKER_BOXES.values

        const val lockerWord = "§6§l[Protected]"

        val signMapped = mapOf(
            Material.ACACIA_SIGN    to Material.ACACIA_WALL_SIGN,
            Material.BIRCH_SIGN     to Material.BIRCH_WALL_SIGN,
            Material.CRIMSON_SIGN   to Material.CRIMSON_WALL_SIGN,
            Material.DARK_OAK_SIGN  to Material.DARK_OAK_WALL_SIGN,
            Material.JUNGLE_SIGN    to Material.JUNGLE_WALL_SIGN,
            Material.OAK_SIGN       to Material.OAK_WALL_SIGN,
            Material.SPRUCE_SIGN    to Material.SPRUCE_WALL_SIGN,
            Material.WARPED_SIGN    to Material.WARPED_WALL_SIGN
        )
    }
    init {
        key = NamespacedKey(plugin, "signKey")
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK || event.player.isSneaking) return
        val targetedBlock = event.clickedBlock
        when (targetedBlock?.type) {
            in allLockable -> {
                for (face in setOf(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)) {
                    val sign = (targetedBlock?.state as Sign)
                    if (!(event.clickedBlock?.getRelative(face)?.type in Tag.WALL_SIGNS.values && isLockerSign(event) && (isOwner(sign, event.player) || isTrusted(sign, event.player)))) {
                        event.isCancelled = true
                    }
                }
            }
            in Tag.WALL_SIGNS.values -> {
                val sign = (targetedBlock?.state as Sign) // blockData (WallSign) != state (Sign)
                if (isLockerSign(event) && isOwner(sign, event.player)) setTrustedPlayers(event)
            }
            else -> return
        }
    }

    private fun setLockedChest(event: PlayerInteractEvent, facing: BlockFace) {
        /**
         * Making possible to protect chests by adding a sign against.
         */

        val chest = event.clickedBlock
        val material = event.material
        event.setUseInteractedBlock(Event.Result.DENY) // just place the sign, no other interaction

        // the locking sign's area
        val relativeTarget = chest?.getRelative(facing) // Frontal block

        // formatting
        signMapped[material]?.let { relativeTarget?.type = it }
        val sign = (relativeTarget?.state as Sign) // blockData (WallSign) != state (Sign)
        sign.setLine(0, lockerWord)
        sign.setLine(1, event.player.name)
        sign.persistentDataContainer.set(key, PersistentDataType.STRING, "${event.player.uniqueId}")
        sign.update() // needed for applying new state on Material
        val wallSign = (sign.blockData as WallSign) // blockData (WallSign) != state (Sign)
        wallSign.facing = facing // makes the sign always collapse to the right face of the chest
        relativeTarget.blockData = wallSign // applying new data

        // debug
        event.player.sendMessage("Chest locked successfully !")
    }

    /*TODO
        - if checks passed, do:
            - check if the targeted block is a Locked Chest's Sign (see setLockedChest)
                + using (using left-click on targeted block)
            - if so, in order :
                - enter sign edition GUI
                - after closing the GUI, force-edition on the first two rows :
                    - "[Private]" on the first row
                    - owner name on the second row
                - check for the last two rows :
                    - check if row can represent a player (registered, online or not), if so :
                        - add perms to this player on the chest the sign protect
    */
    private fun setTrustedPlayers(event: PlayerInteractEvent) {
        /**
         * Interact with the protection sign while being the owner opens a context menu
         * for trusting [0-2] other players on the locked chest and its interactions.
         */

        // to complete

        // debug
        event.player.sendMessage("Trusted players on this chest have been set !")
    }

    private fun isLockerSign(event: PlayerInteractEvent): Boolean {
        /**
         * Checks if the targeted sign is used for protecting a (double) chest.
         */
        val sign = event.clickedBlock
        val signData = (sign?.blockData as WallSign) // blockData (WallSign) != state (Sign)
        if (sign.getRelative(signData.facing.oppositeFace).type !in allLockable) return false
        val signState = (sign.state as Sign) // blockData (WallSign) != state (Sign)
        val allPlayers = event.player.server.offlinePlayers
        // debug
        event.player.sendMessage("allPlayers = $allPlayers")

        for (line in signState.lines) {
            if (line != ""
                && event.player.server.getPlayer(line) !in allPlayers) return false
        }
        return (signState.getLine(0) == lockerWord)
    }

    private fun isOwner(sign: Sign, player: Player): Boolean {
        /**
         * Checks if the Player owns this chest, i.e. has locked it with the targeted Sign.
         */

        // return (sign.getLine(1) == player.name)
        return (sign.getLine(1) == player.name)
    }

    private fun isTrusted(sign: Sign, player: Player): Boolean {
        /**
         * Checks if the Player is trusted on this chest, i.e. the Sign recognizes it.
         */
        // to complete
        return false
    }
}