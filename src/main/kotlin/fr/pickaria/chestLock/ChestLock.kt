package fr.pickaria.chestLock

import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.Chest
import org.bukkit.block.Sign
import org.bukkit.block.data.Directional
import org.bukkit.block.data.type.WallSign
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

/*          BEHAVIOR on this file

Sharped notes in comments (#note) stands for tags, or a set where it can be ambiguous in Minecraft, such as
`#minecraft:wooden_planks` in-game; here it does not represent a singleton and only blocks or entities.
*/

class ChestLock : Listener {
    /**
     * Anti-thief protection for chests.
     */
    companion object {
        val allChests = setOf(
            Material.BARREL,
            Material.CHEST,
            Material.ENDER_CHEST, // no interest except troll ?
            Material.SHULKER_BOX,
            Material.BLACK_SHULKER_BOX,
            Material.BLUE_SHULKER_BOX,
            Material.BROWN_SHULKER_BOX,
            Material.CYAN_SHULKER_BOX,
            Material.GRAY_SHULKER_BOX,
            Material.GREEN_SHULKER_BOX,
            Material.LIGHT_BLUE_SHULKER_BOX,
            Material.LIGHT_GRAY_SHULKER_BOX,
            Material.LIME_SHULKER_BOX,
            Material.MAGENTA_SHULKER_BOX,
            Material.ORANGE_SHULKER_BOX,
            Material.PINK_SHULKER_BOX,
            Material.PURPLE_SHULKER_BOX,
            Material.RED_SHULKER_BOX,
            Material.WHITE_SHULKER_BOX,
            Material.YELLOW_SHULKER_BOX,
            Material.TRAPPED_CHEST
        )
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        /**
         * Making possible to protect chests by adding a sign against.
         */

        /*TODO
            - check if player is in an correct building area, if so :                               v
                - check if player holds a #sign                                                     v
                - in the same time, check if the targeted block is a #chest (must be a SIDE)        v
                - in the same time, check for using (right-click on targeted block + NO sneaking)   v
                - if so, in order :                                                                 v
                    - place the sign on the chest (targeted block)                                  v
                    - write :   - "[Private]" on the first row                                      v
                                - owner name on the second row                                      v
                    - lock the chest for everyone, except trusted players, e.g. :                   -X-
                        - block interaction with the chest from everyone not trusted by the sign    -X-
        */

        // checks
        if (event.clickedBlock?.type !in allChests) return
        if (event.material !in Tag.STANDING_SIGNS.values) return
        if (event.action != Action.RIGHT_CLICK_BLOCK || event.player.isSneaking) return

        event.setUseInteractedBlock(Event.Result.DENY) // just place the sign, no other interaction
        // the locking sign's area
        val chest = (event.clickedBlock as Chest)
        val chestFacing = (chest.blockData as Directional).facing
        val relativeTarget = event.clickedBlock?.getRelative(chestFacing) // Frontal block
        if (relativeTarget?.type?.isAir != true) return // need an empty space

        // formatting
        signMapped[event.material]?.let {
            relativeTarget.type = it
        }
        val sign = (relativeTarget.state as Sign) // blockData (WallSign) != state (Sign)
        sign.setLine(0, "§6§l[Protected]")
        sign.setLine(1, event.player.name)
        sign.update() // needed for applying new state on Material
        val wallSign = (sign.blockData as WallSign) // blockData (WallSign) != state (Sign)
        wallSign.facing = chestFacing // makes the sign always collapse to the chest's frontal face
        relativeTarget.blockData = wallSign // applying new data

        // locking
//        chest.setLock("I have to find how to proper create a key...")
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onClickOnProtectionSign(event: PlayerInteractEvent) {
        /**
         * Clicking on the protection sign while being the owner opens a context menu
         * for trusting [0-2] other players on the protected chest and its interactions
         * (opening, breaking, etc.)
         */

        /*TODO
            - check if player is in a correct building area, if so :                                v
                - check if player does not hold anything
                - in the same time, check if the targeted block is a PrivateSign (see below)
                - in the same time, check for punching (using left-click on targeted block)
                - if so, in order :
                    - enter sign edition GUI
                    - after closing the GUI, force-edition on the first two rows :
                        - "[Private]" on the first row
                        - owner name on the second row
                    - check for the last two rows :
                        - check if row can represent a player (registered, online or not), if so :
                            - add perms to this player on the chest the sign protect
        */

        // to continue
    }
}
