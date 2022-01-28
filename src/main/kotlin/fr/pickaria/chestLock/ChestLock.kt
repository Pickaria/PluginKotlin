package fr.pickaria.chestLock

import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.Sign
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
        val chests = setOf(
            Material.CHEST,
            Material.TRAPPED_CHEST
        )
        val signs = listOf(
            "ACACIA_SIGN",
            "BIRCH_SIGN",
            "CRIMSON_SIGN",
            "DARK_OAK_SIGN",
            "JUNGLE_SIGN",
            "OAK_SIGN",
            "SPRUCE_SIGN",
            "WARPED_SIGN",

            "ACACIA_WALL_SIGN",
            "BIRCH_WALL_SIGN",
            "CRIMSON_WALL_SIGN",
            "DARK_OAK_WALL_SIGN",
            "JUNGLE_WALL_SIGN",
            "OAK_WALL_SIGN",
            "SPRUCE_WALL_SIGN",
            "WARPED_WALL_SIGN"
        )
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        /**
        Making possible to protect chests by adding a sign against.
         */

        // debug
        event.player.sendMessage("Pass: Player interact event.")

        /*TODO
            - check if player is in an correct building area, if so :                               v
                - check if player holds a #sign                                                     v
                - in the same time, check if the targeted block is a #chest (must be a SIDE)        v
                - in the same time, check for using (right-click on targeted block + NO sneaking)   v
                - if so, in order :                                                                 v
                    - place the sign on the chest (targeted block)                               ...~
                    - write :   - "[Private]" on the first row                                      v
                                - owner name on the second row                                      v
                    - lock the chest for everyone, except trusted players, e.g. :
                        - block interaction with the chest from everyone not trusted by the sign
        */

        // checks
        if (event.clickedBlock?.type !in chests) return
        if (event.material !in Tag.STANDING_SIGNS.values) return
        if (event.action != Action.RIGHT_CLICK_BLOCK || event.player.isSneaking) return
        val relativeTarget = event.clickedBlock?.getRelative(event.blockFace) // Frontal block
        if (relativeTarget?.type?.isAir != true) return // need an empty space

        // maybe optimize that
        val index = signs.indexOf(event.material.name.uppercase()) + signs.size/2
        val wallSign = Material.getMaterial(signs[index])!!
        // debug
        event.player.sendMessage("Material held: ${event.material.name}")
        event.player.sendMessage("Sign placed: ${wallSign.name}")

        relativeTarget.type = wallSign

        // edit the sign to make it private
        val signState = (relativeTarget.state as Sign)
        signState.setLine(0, "§6§l[Protected]")
        signState.setLine(1, event.player.name)
        signState.update()
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onClickOnProtectionSign(event: PlayerInteractEvent) {
        /**
        Clicking on the protection sign while being the owner opens a context menu for trusting players
        on this chest and its interactions (opening, breaking, etc.)
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
                    - check for the last rows (3rd and 4th are  :
                        - check if row can represent a player (registered, online or not), if so :
                            - add perms to this player on the chest the sign protect
        */

        // to continue
    }
}
