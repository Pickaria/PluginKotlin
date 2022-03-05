package fr.pickaria.chestLock

import fr.pickaria.Main
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.type.Chest
import org.bukkit.block.data.type.WallSign
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class Locker(plugin: Main): Listener {
    /**
     * Anti-thief protection for chests.
     */
    companion object {
        lateinit var key: NamespacedKey
        lateinit var server: Server

        val lockableContainers = setOf(
            Material.BARREL,
            Material.CHEST,
            Material.ENDER_CHEST, // no interest but here it is
            Material.TRAPPED_CHEST
        ) union Tag.SHULKER_BOXES.values

        val cardinalFaces = setOf(
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST)

        const val lockerWord = "§6§l[Protected]"

        val signMapped = mapOf(
            Material.ACACIA_SIGN    to Material.ACACIA_WALL_SIGN,
            Material.BIRCH_SIGN     to Material.BIRCH_WALL_SIGN,
            Material.CRIMSON_SIGN   to Material.CRIMSON_WALL_SIGN,
            Material.DARK_OAK_SIGN  to Material.DARK_OAK_WALL_SIGN,
            Material.JUNGLE_SIGN    to Material.JUNGLE_WALL_SIGN,
            Material.OAK_SIGN       to Material.OAK_WALL_SIGN,
            Material.SPRUCE_SIGN    to Material.SPRUCE_WALL_SIGN,
            Material.WARPED_SIGN    to Material.WARPED_WALL_SIGN)
    }
    init {
        key = NamespacedKey(plugin, "signKey")
        server = plugin.server
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        /**
         * Manage interactions for LockedChest (LockerSign: set, edit)
         */
        val player = event.player

        // debug
        player.sendMessage("Passed: onPlayerInteract.")

        if (event.action != Action.RIGHT_CLICK_BLOCK || event.player.isSneaking) return
        val block = event.clickedBlock ?: return
        when (block.type) {
            in lockableContainers -> {
                var facing = event.blockFace
                if (facing !in cardinalFaces) // prevents on top/bottom wall sign (illegal placement)
                    facing = event.player.facing.oppositeFace
                if (isLockedContainer(block)) {
                    if (getLockerSignFromContainer(block)?.let { isPlayerTrustedByLockerSign(player, it) } != true) {
                        event.isCancelled = true
                        player.sendMessage("Access denied.")
                    }
                } else if (event.material in signMapped && block.getRelative(facing).type.isAir) {
                    setLockerSignFromContainer(block, facing, player, event.material)
                    event.setUseInteractedBlock(Event.Result.DENY) // no need to open
                    player.sendMessage("Successfully locked this container.")
                }
            }
            in Tag.WALL_SIGNS.values -> {
                if (isLockerSign(block) && isPlayerOwnerOfLockerSign(player, block)) {
                    updateLockerSignFromContainer(block)
                    player.sendMessage("Access perms have been updated.")
                }
            }
            else -> {}
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockDamage(event: BlockDamageEvent) {
        /**
         * Manage LockerSign destruction
         */
        val player = event.player

        // debug
        player.sendMessage("Passed: onBlockDamage.")

        val block = event.block
        when (block.type) {
            in lockableContainers -> {
                if (isLockedContainer(block)) {
                    event.isCancelled = true
                    player.sendMessage("This container is locked. Please remove the sign first.")
                }
            }
            in Tag.WALL_SIGNS.values -> {
                if (isLockerSign(block)) {
                    if (!isPlayerOwnerOfLockerSign(player, block)) {
                        event.isCancelled = true
                        player.sendMessage("You are not allowed to do this.")
                    }
                }
            }
            else -> {}
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        /**
         * Manage LockerSign destruction
         */
        val player = event.player

        // debug
        player.sendMessage("Passed: onBlockDamage.")

        if (player.gameMode == GameMode.CREATIVE) return
        val block = event.block
        when (block.type) {
            in lockableContainers -> {
                if (isLockedContainer(block)) {
                    event.isCancelled = true
                    player.sendMessage("This container is locked. Please remove the sign first.")
                }
            }
            in Tag.WALL_SIGNS.values -> {
                if (isLockerSign(block)) {
                    if (!isPlayerOwnerOfLockerSign(player, block)) {
                        event.isCancelled = true
                        player.sendMessage("You are not allowed to do this.")
                    }
                }
            }
            else -> {}
        }
    }

    private fun getDoubleChest(container: Block): Block {
        /**
         * get the other Chest linked to the Container
         */
        val chestData = (container.blockData as Chest)
        return if (chestData.type == Chest.Type.LEFT) {
            container.getRelative(cardinalFaces.elementAt((cardinalFaces.indexOf(chestData.facing) + 1) % cardinalFaces.size))
        } else {
            container.getRelative(cardinalFaces.elementAt((cardinalFaces.indexOf(chestData.facing) - 1) % cardinalFaces.size))
        }
    }

    private fun getLockerSignFromContainer(container: Block): Block? {
        /**
         * get the LockerSign assigned to this Container.
         * (both must exist)
         */
        if (isDoubleChest(container)) {
            // check for the other chest
            val doubleChest = getDoubleChest(container)
            for (facing in cardinalFaces) {
                val sign = doubleChest.getRelative(facing)
                if (isLockerSign(sign)) return sign
            }
        }
        for (facing in cardinalFaces) {
            val sign = container.getRelative(facing)
            if (isLockerSign(sign)) return sign
        }
        return null
    }

    private fun isDoubleChest(container: Block): Boolean {
        /**
         * Checks if the given container is double (chests only)
         */
        if (container.type !in setOf(Material.CHEST, Material.TRAPPED_CHEST)) return false
        return ((container.blockData as Chest).type != Chest.Type.SINGLE)
    }

    private fun isLockedContainer(container: Block): Boolean {
        /**
         * Checks if the given block is a Container locked by a LockerSign.
         */
        if (isDoubleChest(container)) {
            // check for the other chest
            val doubleChest = getDoubleChest(container)
            for (facing in cardinalFaces) {
                if (isLockerSign(doubleChest.getRelative(facing)))
                    return true
            }
        }
        for (facing in cardinalFaces) {
            if (isLockerSign(container.getRelative(facing)))
                return true
        }
        return false
    }

    private fun isLockerSign(sign: Block): Boolean {
        /**
         * Checks if the given block is the LockerSign of the Container behind.
         */
        if (sign.type !in Tag.WALL_SIGNS.values) return false
        if (sign.getRelative((sign.blockData as WallSign).facing.oppositeFace).type !in lockableContainers) return false
        val signState = sign.state as Sign
        for (i in 1..3) {
            if (server.getPlayer(signState.getLine(i)) !in server.offlinePlayers) return false
        }
        return signState.getLine(0) == lockerWord
    }

    private fun isPlayerOwnerOfLockerSign(player: Player, sign: Block): Boolean {
        /**
         * Checks if the Player owns the Container & its LockerSign
         */
        // at the moment, simple string comparison
        return (sign.state as Sign).getLine(1) == player.name
    }

    private fun isPlayerTrustedByLockerSign(player: Player, sign: Block): Boolean {
        /**
         * Checks if the LockerSign allows the Player to interact with the Container.
         */
        val signState = sign.state as Sign
        for (i in 1..3) {
            if (signState.getLine(i) == player.name) return true
        }
        return false
    }

    private fun setLockerSignFromContainer(container: Block, facing: BlockFace, player: Player, material: Material): Block {
        /**
         * Set a new LockerSign against the free-access Container.
         */
        val sign = container.getRelative(facing)

        signMapped[material]?.let { sign.type = it }
        setSignLine(sign, 0, lockerWord)
        setSignLine(sign, 1, player.name)

        val signData = (sign.blockData as WallSign)
        signData.facing = facing
        sign.blockData = signData

        updateSignInHands(player)
        return sign
    }

    private fun setSignLine(block: Block, line: Int, text: String) {
        val sign = (block.state as Sign)
        sign.setLine(line, text)
        sign.update()
    }

    private fun updateLockerSignFromContainer(block: Block) {
        /**
         * If the LockerSign belongs to the player,
         * enters a menu to pick another player (to or not trust anymore),
         * then adds the chosen player to trust on this container.
         *
         * Trusted players, except the owner, must be at most 2.
         */
//        val sign = (block.state as Sign)

        // A menu will open for choosing which action to do : add trust / remove trust
        /*
        val action = menuChoseAction("What do you want to change about trusting ?", "add", "remove")
        */

        // A sub-menu will replace the first one : choosing player to trust / do not trust
        /*
        val player = if (action == "add" && signState.getLine(3) == "") {
            menuChoosePlayer("Add trusted player", listOfEligiblePlayers)
        } else if (action == "remove" && signState.getLine(2) != "") {
            menuChoosePlayer("Remove trusted player", listOfEligiblePlayers)
        } else { // error
            return
        }
        */

        // Then, all we have to do is add/remove the selected player to/from the LockerSign
        /*
        // conditions are the same as above, it will be attached together at the end;
        // it's just a schema.
        if (action == "add" && signState.getLine(3) == "") {
            signState.setLine(signState.lines.indexOfFirst{ it == "" }, player)
        } else if (action == "remove" && signState.getLine(2) != "") {
            signState.setLine(signState.lines.indexOf{ it == player }, "")
        }
        */

        // Modify data stored into the sign to allow/restrict access for the selected player

        // ... to update soon
    }

    private fun updateSignInHands(player: Player) {
        /**
         * When a LockerSign is put, remove one from the Player's inventory.
         */
        if (player.gameMode == GameMode.CREATIVE) return
        if (player.inventory.itemInMainHand.amount == 1) {
            player.inventory.setItemInMainHand(ItemStack(Material.AIR))
        } else {
            player.inventory.itemInMainHand.amount = player.inventory.itemInMainHand.amount - 1
        }
    }

/*
    private fun isLockedContainer(container: Block, server: Server): Boolean {
        /**
         * Checks if the targeted block is locked by a LockerSign
         */
        if (container.type !in lockableContainers) return false
        for (face in cardinalFaces) {
            if (isLockerSign(container.getRelative(face), server)) return true
        }
        return false
    }

    private fun isLockerSign(sign: Block, server: Server): Boolean {
        /**
         * Checks if the targeted Sign (if so) is used for protecting a (double) chest.
         */
        if (sign.type !in Tag.WALL_SIGNS.values) return false
        if (sign.getRelative((sign.blockData as WallSign).facing.oppositeFace).type !in lockableContainers) return false

        val signState = (sign.state as Sign) // blockData (WallSign) != state (Sign)
        val allPlayers = server.offlinePlayers
        for (line in signState.lines) {
            if (line != ""
                && server.getPlayer(line) !in allPlayers) return false
        }
        return signState.getLine(0) == lockerWord
    }

    private fun isOwnedBy(sign: Sign, player: Player): Boolean {
        /**
         * Checks if the Player owns this chest, i.e. has locked it with the targeted Sign.
         */
//        return (player.uniqueId in sign.persistentDataContainer) // find a way for that
        return player.name == sign.getLine(1)
    }

    private fun isTrustedBy(sign: Sign, player: Player): Boolean {
        /**
         * Checks if the Player is trusted on this chest, i.e. the Sign recognizes it.
         */
//        return (player.uniqueId in sign.persistentDataContainer) // find a way for that
        for (i in 1..3) {
            if (player.name == (sign.getLine(i))) return true
        }
        return false
    }

    private fun setLockedChest(event: PlayerInteractEvent) {
        /**
         * Making possible to protect chest-like blocks (see allLockable) by adding a sign against.
         */

        val chest = event.clickedBlock
        val material = event.material
        val facing = event.player.facing
        event.setUseInteractedBlock(Event.Result.DENY) // just place the sign, no other interaction

        // the locking sign's area
        val relativeTarget = chest?.getRelative(facing) // Frontal block

        // formatting
        signMapped[material]?.let { relativeTarget?.type = it }
        val sign = (relativeTarget?.state as Sign) // blockData (WallSign) != state (Sign)

        // test lol
        sign.persistentDataContainer.set(key, PersistentDataType.STRING, "${event.player.uniqueId}")

        sign.setLine(0, lockerWord)
        sign.setLine(1, event.player.name)
        sign.update() // needed for applying new state on Material
        val wallSign = (sign.blockData as WallSign) // blockData (WallSign) != state (Sign)
        wallSign.facing = facing // makes the sign always collapse to the right face of the chest
        relativeTarget.blockData = wallSign // applying new data

        // debug
        event.player.sendMessage("Chest locked successfully !")
    }

    private fun setTrustedPlayers(event: PlayerInteractEvent, sign: Sign) {
        /**
         * Interact with the protection sign while being the owner opens a context menu
         * for trusting [0-2] other players on the locked chest and its interactions.
         */

        // to do

        // debug
        event.player.sendMessage("Trusted players on this chest have been set !")
    }
*/
}