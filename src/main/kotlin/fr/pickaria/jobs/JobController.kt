package fr.pickaria.jobs

import fr.pickaria.Main
import fr.pickaria.executeSQL
import fr.pickaria.executeSelect
import fr.pickaria.jobs.jobs.Job
import fr.pickaria.jobs.jobs.Miner
import org.bukkit.Bukkit.getServer
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.time.Duration.Companion.hours
import kotlin.time.DurationUnit

class JobController(plugin: Main) {
	init {
		getServer().pluginManager.registerEvents(Miner(), plugin)
	}

	companion object {
		/**
		 * Get the currently active job of the user
		 */
		fun getJob(playerUuid: UUID): Job? {
			val rs =
				executeSelect("SELECT job, level, last_used FROM job WHERE player_uuid = '$playerUuid' AND active = true")

			if (rs?.next() == true) {
				return Job(JobEnum.valueOf(rs.getString(1)), rs.getInt(2), rs.getTimestamp(3).toLocalDateTime())
			}
			return null
		}

		fun changeJob(playerUuid: UUID, newJob: JobEnum): Int {
			// 1. check if previous job was joined less than a day ago

			// 2. when a player join the job, divide its previous level by 20% if any || set it to 0
			// keep all previous jobs in the database
			// 3. set previous as not "active" = false
			val previousDay = LocalDateTime.now().minusDays(1)

			getJob(playerUuid)?.let { job ->
				if (job.job == newJob) {
					return -2
				} else if (job.lastUsed > previousDay) {
					return previousDay.until(job.lastUsed, ChronoUnit.HOURS).hours.toInt(DurationUnit.HOURS)
				} else {
					executeSQL("UPDATE job SET active = false, level = level * 0.8 WHERE player_uuid = '$playerUuid' AND active = true")
				}
			}

			// 4. set current as "active" = true
			executeSQL("insert into job (player_uuid, job, active) values ('$playerUuid', '${newJob.name}', true) on conflict (player_uuid, job) do update set active = true, last_used = now()")

			return -1
		}
	}
}