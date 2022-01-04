package fr.pickaria.jobs

import fr.pickaria.Main
import fr.pickaria.model.JobModel
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.ktorm.dsl.*

class JobCommand : CommandExecutor, TabCompleter {
	companion object {
		val SUB_COMMANDS = listOf("join", "leave", "top")
	}

	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
		if (sender is Player) {
			if (args.isEmpty()) {
				val message = if (Main.jobController.jobCount(sender.uniqueId) == 0) {
					 "§cVous n'exercez actuellement pas de métier."
				} else {
					val jobs = Main.jobController.getFromCache(sender.uniqueId)?.filter { it.value.active }?.map { it.key.label }
					"§7Vous exercez le(s) métier(s) : ${jobs?.joinToString(", ")}."
				}
				sender.sendMessage(message)
				return true
			}

			if (args.size != 2) {
				sender.sendMessage("§cVeuillez entrer l'action et le nom du métier.")
				return false
			}

			val job = try {
				JobEnum.valueOf(args[1].uppercase())
			} catch (e: IllegalArgumentException) {
				sender.sendMessage("§cCe métier n'existe pas.")
				return true
			}

			when (args[0]) {
				"join" -> {
					if (Main.jobController.jobCount(sender.uniqueId) >= JobController.MAX_JOBS) {
						sender.sendMessage("§cVous ne pouvez pas avoir plus de ${JobController.MAX_JOBS} métier(s).")
					} else if (Main.jobController.hasJob(sender.uniqueId, job)) {
						sender.sendMessage("§cVous exercez déjà ce métier.")
					} else {
						val cooldown = Main.jobController.getCooldown(sender.uniqueId, job)

						if (cooldown > 0) {
							sender.sendMessage("§cVous devez attendre $cooldown heures avant de changer de métier.")
						} else {
							Main.jobController.joinJob(sender.uniqueId, job)
							sender.sendMessage("§7Vous avez rejoint le métier ${job.label}.")
						}
					}
				}
				"leave" -> {
					if (!Main.jobController.hasJob(sender.uniqueId, job)) {
						sender.sendMessage("§cVous n'exercez pas ce métier.")
					} else {
						val cooldown = Main.jobController.getCooldown(sender.uniqueId, job)

						if (cooldown > 0) {
							sender.sendMessage("§cVous devez attendre $cooldown heures avant de changer de métier.")
						} else {
							Main.jobController.leaveJob(sender.uniqueId, job)
							sender.sendMessage("§7Vous avez quitté le métier ${job.label}.")
						}
					}
				}
				"top" -> {
					val component = TextComponent("§6==== Top ${job.label} : ====")
					val server = Bukkit.getServer()

					Main.database
						.from(JobModel)
						.select()
						.orderBy(JobModel.level.desc())
						.where { JobModel.job eq job.name }
						.limit(0, 10)
						.forEach {
							val uuid = it[JobModel.playerUniqueId]
							val level = it[JobModel.level]
							val player = server.getOfflinePlayer(uuid!!)
							component.addExtra("\n§6${it.row} : §7${player.name} - ${level!!}")
						}

					sender.spigot().sendMessage(component)
				}
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
		return when (args.size) {
			1 -> SUB_COMMANDS.filter { it.startsWith(args[0]) }.toMutableList()
			2 -> JobEnum.values().map { it.name.lowercase() }.filter { it.startsWith(args[1]) }.toMutableList()
			else -> mutableListOf()
		}
	}
}