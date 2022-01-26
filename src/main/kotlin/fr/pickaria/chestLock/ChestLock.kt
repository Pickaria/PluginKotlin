package fr.pickaria.chestLock

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

/*          BEHAVIOR on this file

Sharp marks in comments (#) stands for tags, or a set where it can be ambiguous in Minecraft, such as
`#minecraft:wooden_planks` in-game; here it does not represent a singleton and only blocks or entities.
*/

class ChestLock : Listener {
    @EventHandler
    fun privateSign(event: PlayerInteractEvent) {
        /**
        Making possible to protect chests by adding a sign against.
         */
        event.player.sendMessage("Passing by: player interact event.")

        /*  TODO
            - check if player is in an correct building area, if so :                               X
                - check if player holds a #sign                                                     v
                - in the same time, check if the targeted block is a #chest (must be a SIDE)        v
                - in the same time, check for using (right-click on targeted block + NO sneaking)   v
                - if so, in order :
                    - place the sign on the chest (targeted block)
                    - write :   - "[Private]" on the first row
                                - owner name on the second row
                                - trusted players on 3rd and 4th rows (empty by default)
                    - lock the chest for everyone, except trusted players, e.g. :
                        - block interaction with the chest from everyone not trusted by the sign
        */
        val player = event.player
        val isPlayerEligible = (event.action == Action.RIGHT_CLICK_BLOCK && !event.player.isSneaking)

        val sourceItem = event.material.toString()
        val isItemSign = (sourceItem.subSequence(sourceItem.length-4, sourceItem.length) == "SIGN")
        val targetedBlock = event.clickedBlock?.type.toString()
        val isTargetChestType = (targetedBlock.subSequence(targetedBlock.length-5, targetedBlock.length) == "CHEST")

        if (! (isPlayerEligible && isItemSign && isTargetChestType)) return

        player.sendMessage("${player.name} has locked this chest successfully !")

        // to continue
    }

    fun onClickOnProtectionSign() {
        /**
        Clicking on the protection sign while being the owner opens a context menu for trusting players
        on this chest and its interactions (opening, breaking, etc.)
         */

        /*  TODO
            - check if player is in a correct building area, if so :
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
    }
}