package fr.pickaria.economy

import fr.pickaria.Main
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.Bukkit.getServer
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.lang.NumberFormatException

class PayCommand: CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(sender is Player){
            val amount = try {
                args[1].toDouble()
            }catch (_: NumberFormatException){
                return false
            }catch(_: ArrayIndexOutOfBoundsException){
                return false
            }

            val withdrawResponse = Main.economy.withdrawPlayer(sender, amount)
            if (withdrawResponse.type == EconomyResponse.ResponseType.SUCCESS) {
                val recipient = getServer().getOfflinePlayer(args[0])
                if(!recipient.hasPlayedBefore()){
                    sender.sendMessage("§cCe joueur n'est jamais venu sur ce serveur")
                    return true
                }
                val depositResponse = Main.economy.depositPlayer(recipient, withdrawResponse.amount)

                if (depositResponse.type != EconomyResponse.ResponseType.SUCCESS) {
                    sender.sendMessage("§cLe destinataire n'a pas pu recevoir l'argent.")
                    Main.economy.depositPlayer(sender, withdrawResponse.amount)
                }
            } else {
                sender.sendMessage("§cVous n'avez pas assez d'argent.")
            }
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
        return if(args.size == 1) {
            getServer().onlinePlayers.filter{
                it.name.startsWith(args[0]) && (sender as Player).name != it.name
            }.map{
                it.name
            }.toMutableList()
        }else {
            mutableListOf()
        }
    }
}