package fr.pickaria.economy

import fr.pickaria.Main
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.Bukkit.getServer
import org.bukkit.Color
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.lang.NumberFormatException

class PayCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            val recipient = try {
                getServer().getOfflinePlayer(args[0])
            } catch (_: ArrayIndexOutOfBoundsException) {
                return false
            }

            if (recipient == sender) {
                sender.sendMessage("${Color.RED}Vous ne pouvez pas envoyer de l'argent à vous-même.")
                return true
            }

            if (!recipient.hasPlayedBefore()) {
                sender.sendMessage("${Color.RED}Ce joueur n'est jamais venu sur le serveur.")
                return true
            }

            val amount = try {
                args[1].toDouble()
            } catch (_: NumberFormatException) {
                sender.sendMessage("${Color.RED}La valeur que vous avez entrée n'est pas un chiffre.")
                return true
            } catch (_: ArrayIndexOutOfBoundsException) {
                return false
            }

            if (amount <= 0) {
                sender.sendMessage("${Color.RED}Le montant doit être suppérieur à 0.")
                return true
            }

            if (Main.economy.has(sender, amount)) {
                val withdrawResponse = Main.economy.withdrawPlayer(sender, amount)

                if (withdrawResponse.type == EconomyResponse.ResponseType.SUCCESS) {

                    val depositResponse = Main.economy.depositPlayer(recipient, withdrawResponse.amount)

                    if (depositResponse.type != EconomyResponse.ResponseType.SUCCESS) {
                        sender.sendMessage("${Color.RED}Le destinataire n'a pas pu recevoir l'argent.")
                        Main.economy.depositPlayer(sender, withdrawResponse.amount)
                    }
                }
            } else {
                sender.sendMessage("${Color.RED}Vous n'avez pas assez d'argent.")
            }
        }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        return if (args.size == 1) {
            getServer().onlinePlayers.filter {
                it.name.startsWith(args[0]) && sender != it
            }.map {
                it.name
            }.toMutableList()
        } else {
            mutableListOf()
        }
    }
}