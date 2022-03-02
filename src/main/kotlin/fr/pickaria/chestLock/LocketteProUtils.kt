package fr.pickaria.chestLock

import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.google.gson.JsonParser

import fr.pickaria.Main
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.Directional
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.plugin.Plugin

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashSet

class LocketteProUtils(plugin: Main): Listener {
    /**
     * Source code translated in Kotlin from LockettePro Utils (1.14-1.16) found on GitHub (Java) :
     * https://github.com/brunyman/LockettePro/blob/master/LockettePro/src/me/crafter/mc/lockettepro/Utils.java
     *
     * It stands here temporary for schedule ONLY
     * Do NOT distribute !
     */

    companion object {
        lateinit var newPlugin: Plugin
        const val userNamePattern = "^[a-zA-Z0-9_]*$"
        private val selectedSign: LoadingCache<UUID, Block> = CacheBuilder.newBuilder()
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .build(CacheLoader.from<UUID, Block> {
                null
            })
        private val notified = (HashSet<UUID>() as Set<UUID>)

        val newsFaces = setOf(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)
    }
    init {
        newPlugin = plugin
    }

    fun putSignOn(block: Block, blockFace: BlockFace, line1: String, line2: String, material: Material): Block {
        val newSign = block.getRelative(blockFace)
        val blockType = Material.getMaterial(material.name.replace("__SIGN", "_WALL_SIGN"))
        if (blockType != null && Tag.WALL_SIGNS.isTagged(blockType)) {
            newSign.type = blockType
        } else {
            newSign.type = Material.OAK_WALL_SIGN
        }
        val data = newSign.blockData
        if (data is Directional) {
            data.facing = blockFace
            newSign.setBlockData(data, true)
        }
        updateSign(newSign)
        val sign = (newSign.state as Sign)
        if (newSign.type in setOf(Material.DARK_OAK_WALL_SIGN, Material.CRIMSON_WALL_SIGN)) {
            sign.color = DyeColor.WHITE
        }
        sign.setLine(0, line1)
        sign.setLine(1, line2)
        sign.update()

        return newSign
    }

    fun setSignLine(block: Block, line: Int, text: String) {
        val sign = (block.state as Sign)
        sign.setLine(line, text)
        sign.update()
    }

    fun removeASign(player: Player) {
        if (player.gameMode == GameMode.CREATIVE) return
        if (player.inventory.itemInMainHand.amount == 1) {
            player.inventory.setItemInMainHand(ItemStack(Material.AIR))
        } else {
            player.inventory.itemInMainHand.amount = player.inventory.itemInMainHand.amount - 1
        }
    }

    fun updateSign(block: Block) {
        if (block.state is Sign) {
            (block.state as Sign).update()
        }
    }

    fun getSelectedSign(player: Player): Block? {
        val b = selectedSign.getIfPresent(player.uniqueId)
        if (b != null && player.world.name != b.world.name) {
            selectedSign.invalidate(player.uniqueId)
            return null
        }
        return b
    }

    fun selectSign(player: Player, block: Block) {
        selectedSign.put(player.uniqueId, block)
    }

    private fun playLockEffect(player: Player, block: Block) {
        player.playSound(block.location, Sound.BLOCK_WOODEN_DOOR_CLOSE, 0.3F, 1.4F)
//        player.spigot().playEffect(block.location.add(0.5, 0.5, 0.5), Effect.CRIT, 0, 0, 0.3F, 0.3F, 0.3F, 0.1F, 64, 64)
    }

    fun playAccessDenyEffect(player: Player, block: Block) {
        player.playSound(block.location, Sound.ENTITY_VILLAGER_NO, 0.3F, 0.9F)
//        player.spigot().playEffect(block.location.add(0.5, 0.5, 0.5), Effect.MOBSPAWNER_FLAMES, 0, 0, 0.3F, 0.3F, 0.3F, 0.1F, 64, 64)
    }

    fun sendMessages(sender: CommandSender, messages: String?) {
        if (messages == null || messages == "") return
        sender.sendMessage(messages)
    }

    fun shouldNotify(player: Player): Boolean {
        return if (notified.contains(player.uniqueId)) {
            false
        } else {
            notified.plus(player.uniqueId)
            true
        }
    }

    fun hasValidCache(block: Block): Boolean {
        val metadatas = block.getMetadata("expires")
        if (metadatas.isNotEmpty()) {
            val expires = metadatas[0].asLong()
            if (expires > System.currentTimeMillis()) {
                return true
            }
        }
        return false
    }

    fun getAccess(block: Block): Boolean {
        val metadatas = block.getMetadata("locked")
        return metadatas[0].asBoolean()
    }

    fun setCache(block: Block, access: Boolean) {
        block.removeMetadata("expires", newPlugin)
        block.removeMetadata("locked", newPlugin)
//        block.setMetadata("expires", FixedMetadataValue(newPlugin, System.currentTimeMillis() + Config.getCacheTimeMillis()))
        block.setMetadata("locked", FixedMetadataValue(newPlugin, access))
    }

    fun resetCache(block: Block) {
        block.removeMetadata("expires", newPlugin)
        block.removeMetadata("locked", newPlugin)
        for (blockFace in newsFaces) {
            val relative = block.getRelative(blockFace)
            if (relative.type == block.type) {
                relative.removeMetadata("expires", newPlugin)
                relative.removeMetadata("locked", newPlugin)
            }
        }
    }

    fun updateUuidOnSign(block: Block) {
        for (line in 1..3) {
            updateUuidByUsername(block, line)
        }
    }

    fun updateUuidByUsername(block: Block, line: Int) {
        val sign = (block.state as Sign)
        val original = sign.getLine(line)
        Bukkit.getScheduler().runTaskAsynchronously(newPlugin, Runnable {
            @Override
            fun run() {
                var username = original
                if (username.contains("#")) {
                    username = username.split("#")[0]
                }
                if (!isUserName(username)) return
                val user = Bukkit.getPlayerExact(username)
                val uuid = user?.uniqueId?.toString() ?: getUuidByUsernameFromMojang(username)
                if (uuid != null) {
                    val toWrite = "$username#$uuid"
                    Bukkit.getScheduler().runTask(newPlugin, Runnable {
                        @Override
                        fun run() {
                            setSignLine(block, line, toWrite)
                        }
                    })
                }
            }
        })
    }

    fun updateUsernameByUuid(block: Block, line: Int) {
        val sign = (block.state as Sign)
        val original = sign.getLine(line)
        if (isUsernameUuidLine(original)) {
            val uuid = getUuidFromLine(original)
            val player = Bukkit.getPlayer(UUID.fromString(uuid))
            if (player != null) {
                setSignLine(block, line, player.name)
            }
        }
    }

    fun updateLineByPlayer(block: Block, line: Int, player: Player) {
        setSignLine(block, line, player.name + "#" + player.uniqueId.toString())
    }

    fun updateLineWithTime(block: Block, noexpire: Boolean) {
        val sign = (block.state as Sign)
        if (noexpire) {
            sign.setLine(0, sign.getLine(0) + "#created:" + -1)
        } else {
            sign.setLine(0, sign.getLine(0) + "#created:" + (System.currentTimeMillis()/1000))
        }
        sign.update()
    }

    fun isUserName(text: String): Boolean {
        return text.length in 3..16 && text.matches(Regex.fromLiteral(userNamePattern))
    }

    // Warning: don't ise this in a sync way
    fun getUuidByUsernameFromMojang(username: String): String? {
        try {
            val url = URL("https://api.mojang.com/users/profiles/minecraft/$username")
            val connection = url.openConnection()
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            val buffer = BufferedReader(InputStreamReader(connection.getInputStream()))
            var inputLine: String? = buffer.readLine()
            val response = StringBuffer()
            while ((inputLine != null)) {
                response.append(inputLine)
                inputLine = buffer.readLine()
            }
            val responseString = response.toString()
            val json = JsonParser.parseString(responseString).asJsonObject
            val rawuuid = json.get("id").asString
            return rawuuid.substring(0, 8) + "-" + rawuuid.substring(8, 12) + "-" + rawuuid.substring(12, 16) + "-" + rawuuid.substring(16, 20) + "-" + rawuuid.substring(20)
        } catch (_: Exception) {}
        return null
    }

    fun isUsernameUuidLine(text: String): Boolean {
        if (text.contains("#")) {
            val splitted: List<String> = text.split("#")
            var cpt = 0
            for (i in 1..splitted.lastIndex) {
                cpt+= splitted[i].length
            }
            if (cpt == 36) {
                return true
            }
        }
        return false
    }

    fun isPrivateTimeLine(text: String): Boolean {
        if (text.contains("#")) {
            val splitted: List<String> = text.split("#")
            if (splitted[1].startsWith("created:")) {
                return true
            }
        }
        return false
    }

    fun stripSharpSign(text: String): String {
        return if (text.contains("#")) {
            text.split("#")[0]
        } else {
            text
        }
    }

    fun getUsernameFromLine(text: String): String {
        return if (isUsernameUuidLine(text)) {
            text.split("#")[0]
        } else {
            text
        }
    }

    fun getUuidFromLine(text: String): String? {
        return if (isUsernameUuidLine(text)) {
            val splitted: List<String> = text.split("#")
            var secondPart = ""
            for (i in 1..splitted.lastIndex) {
                secondPart+= splitted[i]
            }
            secondPart
        } else {
            null
        }
    }

/*    fun getCreatedFromLine(text: String): Long {
        if (isPrivateTimeLine(text)) {
            val splitted: List<String> = text.split("#")
            var secondPart = ""
            for (i in 1..splitted.lastIndex) {
                secondPart+= splitted[i]
            }
            return Long.parseLong(secondPart)
        } else {
            return Config.getLockDefaultCreateTimeUnix()
        }
    }
*/
/*    fun isPlayerOnline(player: Player, text: String): Boolean {
        return if (isUsernameUuidLine(text)) {
            if (Config.isUuidEnabled()) {
                player.uniqueId.toString() == getUuidFromLine(text)
            } else {
                player.name == getUsernameFromLine(text)
            }
        } else {
            text == player.name
        }
    }
*/
    fun getSignLineFromUnknown(rawline: WrappedChatComponent): String {
        val json = rawline.json
        return getSignLineFromUnknown(json)
    }

    fun getSignLineFromUnknown(json: String): String {
        try { // 1.9+
            if (json.length > 33) {
                val line = JsonParser.parseString(json).asJsonObject
                if (line.has("extra")) {
                    return line.get("extra").asJsonArray.get(0).asJsonObject.get("text").asString
                }
            }
            return ""
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return json
    }
}