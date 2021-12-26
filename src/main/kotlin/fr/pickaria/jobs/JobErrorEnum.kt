package fr.pickaria.jobs

enum class JobErrorEnum(label: String) {
	ALREADY("§cVous exercez déjà ce métier."),
	COOLDOWN("§cVous devez patienter avant de changer de métier."),
	NOT_EXERCICE("§cVous n'exercez pas ce métier."),
	UNKNOWN("§cErreur inconnue."),
	JOB_JOINED("§7Vous avez rejoint le métier."),
	JOB_LEFT("§7Vous avez quitté le métier."),
}