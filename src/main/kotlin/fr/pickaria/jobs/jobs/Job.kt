package fr.pickaria.jobs.jobs

import fr.pickaria.jobs.JobEnum
import java.time.LocalDateTime

data class Job(val job: JobEnum, val level: Int, val lastUsed: LocalDateTime)
