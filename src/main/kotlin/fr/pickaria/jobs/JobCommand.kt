package fr.pickaria.jobs

import fr.pickaria.Main
import fr.pickaria.model.EconomyModel
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
	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
		if (sender is Player) {
			if (args.isEmpty()) {
				val jobs = JobController.getJobs(sender.uniqueId).map { JobEnum.valueOf(it.job).label.lowercase() }
				val message = if (jobs.isEmpty()) {
					 "§cVous n'exercez actuellement pas de métier."
				} else {
					"§7Vous exercez le(s) métier(s) : ${jobs.joinToString(", ")}."
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
					if (JobController.jobCount(sender.uniqueId) >= JobController.MAX_JOBS) {
						sender.sendMessage("§cVous ne pouvez pas avoir plus de ${JobController.MAX_JOBS} métier(s).")
					} else if (JobController.hasJob(sender.uniqueId, job)) {
						sender.sendMessage("§cVous exercez déjà ce métier.")
					} else {
						val cooldown = JobController.getCooldown(sender.uniqueId, job)

						if (cooldown > 0) {
							sender.sendMessage("§cVous devez attendre $cooldown heures avant de changer de métier.")
						} else if (JobController.joinJob(sender.uniqueId, job)) {
							sender.sendMessage("§7Vous avez rejoint le métier ${job.label}.")
						} else {
							sender.sendMessage("§cUne erreur inconnue est survenue.")
						}
					}
				}
				"leave" -> {
					when (JobController.leaveJob(sender.uniqueId, job)) {
						JobErrorEnum.NOT_EXERCICE -> sender.sendMessage("§cVous n'exercez pas ce métier.")
						JobErrorEnum.COOLDOWN -> {
							val cooldown = JobController.getCooldown(sender.uniqueId, job)
							sender.sendMessage("§cVous devez attendre $cooldown heures avant de changer de métier.")
						}
						JobErrorEnum.JOB_LEFT -> sender.sendMessage("§7Vous avez quitté le métier de ${job.label}.")
						JobErrorEnum.UNKNOWN -> sender.sendMessage("§cUne erreur inconnue est survenue.")
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
			1 -> listOf("join", "leave", "top").filter { it.startsWith(args[0]) }.toMutableList()
			2 -> JobEnum.values().map { it.name.lowercase() }.filter { it.startsWith(args[1]) }.toMutableList()
			else -> mutableListOf()
		}
	}
}