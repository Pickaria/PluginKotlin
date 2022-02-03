package fr.pickaria.jobs

import fr.pickaria.Main
import fr.pickaria.menus.MenuEnum
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
		val SUB_COMMANDS = listOf("join", "leave", "top", "menu", "stats", "info")
	}

	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
		if (sender is Player) {
			if (args.isEmpty()) {
				val message = if (Main.jobController.jobCount(sender.uniqueId) == 0) {
					"§cVous n'exercez actuellement pas de métier."
				} else {
					val jobs = Main.jobController.getFromCache(sender.uniqueId)?.filter { it.value.active }
						?.map { it.key.label }
					"§7Vous exercez le(s) métier(s) : ${jobs?.joinToString(", ")}."
				}
				sender.sendMessage(message)
				return true
			}

			if (args.size <= 1) {
				sender.sendMessage("§cVeuillez entrer une sous-commande valide.")
				return false
			}

			val job = try {
				if (args.size == 2) {
					JobEnum.valueOf(args[1].uppercase())
				} else {
					null
				}
			} catch (e: IllegalArgumentException) {
				null
			}

			try {
				when (args[0]) {
					"join" -> {
						join(sender, job!!)
					}
					"leave" -> {
						leave(sender, job!!)
					}
					"top" -> {
						top(sender, job!!)
					}
					"menu" -> {
						Main.menuController.openMenu(sender, MenuEnum.JOBS, null)
					}
					"browse" -> {
						Main.menuController.openMenu(sender, MenuEnum.JOBS, null)
					}
					"stats" -> {
						stats(sender, job!!)
					}
				}
			} catch (_: NullPointerException) {
				sender.sendMessage("§cVeuillez fournir un nom de métier valide.")
			}
		}

		return true
	}

	private fun join(sender: Player, job: JobEnum) {
		if (Main.jobController.jobCount(sender.uniqueId) >= JobController.MAX_JOBS) {
			sender.sendMessage("§cVous ne pouvez pas avoir plus de ${JobController.MAX_JOBS} métier(s).")
		} else if (Main.jobController.hasJob(sender.uniqueId, job)) {
			sender.sendMessage("§cVous exercez déjà ce métier.")
		} else {
			val cooldown = Main.jobController.getCooldown(sender.uniqueId, job)

			if (cooldown > 0) {
				sender.sendMessage("§cVous devez attendre $cooldown minutes avant de changer de métier.")
			} else {
				Main.jobController.joinJob(sender.uniqueId, job)
				sender.sendMessage("§7Vous avez rejoint le métier ${job.label}.")
			}
		}
	}

	private fun leave(sender: Player, job: JobEnum) {
		if (!Main.jobController.hasJob(sender.uniqueId, job)) {
			sender.sendMessage("§cVous n'exercez pas ce métier.")
		} else {
			val cooldown = Main.jobController.getCooldown(sender.uniqueId, job)

			if (cooldown > 0) {
				sender.sendMessage("§cVous devez attendre $cooldown minutes avant de changer de métier.")
			} else {
				Main.jobController.leaveJob(sender.uniqueId, job)
				sender.sendMessage("§7Vous avez quitté le métier ${job.label}.")
			}
		}
	}

	private fun top(sender: Player, job: JobEnum) {
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
				val experience = it[JobModel.level] ?: 0
				val level = Main.jobController.getLevelFromExperience(job, experience)
				val player = server.getOfflinePlayer(uuid!!)
				component.addExtra("\n§f${it.row}. §7${player.name}, $level ($experience)")
			}

		sender.spigot().sendMessage(component)
	}

	private fun stats(sender: Player, job: JobEnum) {
		with(Main.jobController) {
			val experience = this.getFromCache(sender.uniqueId, job)?.level ?: 0
			val level = this.getLevelFromExperience(job, experience)
			val nextLevelExperience = this.getExperienceFromLevel(job, level + 1)
			sender.sendMessage("§7Vous êtes niveau §6$level§7 dans le métier §6${job.label}§7.\n" +
					"§7$experience / $nextLevelExperience (${nextLevelExperience - experience} restant) → niveau ${level + 1}.")
		}
	}

	override fun onTabComplete(
		sender: CommandSender,
		command: Command,
		alias: String,
		args: Array<out String>
	): MutableList<String> {
		if (sender is Player) {
			return when (args.size) {
				1 -> SUB_COMMANDS.filter { it.startsWith(args[0]) }.toMutableList()
				2 -> when (args[0]) {
					// Return list of jobs
					"top", "join", "stats", "info" -> JobEnum.values().map { it.name.lowercase() }.filter { it.startsWith(args[1]) }
						.toMutableList()
					// Get list of active jobs
					"leave" -> Main.jobController.getFromCache(sender.uniqueId)
						?.filter { it.value.active }
						?.map { it.value.job.lowercase() }
						?.toMutableList() ?: mutableListOf()
					else -> mutableListOf()
				}
				else -> mutableListOf()
			}
		}

		return mutableListOf()
	}
}