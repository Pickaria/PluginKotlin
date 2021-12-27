package fr.pickaria.economy

import fr.pickaria.Main
import fr.pickaria.model.EconomyModel
import fr.pickaria.model.economy
import net.milkbowl.vault.economy.AbstractEconomy
import net.milkbowl.vault.economy.EconomyResponse
import net.milkbowl.vault.economy.EconomyResponse.ResponseType
import org.bukkit.Bukkit.getServer
import org.bukkit.OfflinePlayer
import org.ktorm.dsl.*
import org.ktorm.entity.add
import org.ktorm.entity.find
import java.text.DecimalFormat
import fr.pickaria.model.Economy


class PickariaEconomy : AbstractEconomy() {
	override fun isEnabled() = true

	override fun getName() = "Pickaria economy"

	override fun fractionalDigits() = -1

	override fun format(amount: Double): String {
		return if (amount <= 1.0) {
			"${DecimalFormat("0.00").format(amount)} ${currencyNameSingular()}"
		} else {
			"${DecimalFormat("0.00").format(amount)} ${currencyNamePlural()}"
		}
	}

	override fun currencyNamePlural() = "$"

	override fun currencyNameSingular() = "$"

	override fun hasAccount(player: OfflinePlayer): Boolean {
		return Main.database
			.from(EconomyModel)
			.select()
			.where { EconomyModel.playerUniqueId eq player.uniqueId }
			.totalRecords > 0
	}

	override fun hasAccount(playerName: String?): Boolean {
		return playerName?.let {
			hasAccount(getServer().getOfflinePlayer(playerName))
		} ?: false
	}

	override fun hasAccount(playerName: String?, worldName: String?): Boolean {
		return hasAccount(playerName)
	}

	override fun getBalance(player: OfflinePlayer): Double {
		if (!hasAccount(player)) {
			createPlayerAccount(player)
		}
		return Main.database.economy.find { it.playerUniqueId eq player.uniqueId }?.balance ?: 0.0
	}

	override fun getBalance(playerName: String?): Double {
		return playerName?.let {
			getBalance(getServer().getOfflinePlayer(playerName))
		} ?: 0.0
	}

	override fun getBalance(playerName: String?, world: String?): Double {
		return getBalance(playerName)
	}

	override fun has(player: OfflinePlayer, amount: Double): Boolean {
		return getBalance(player) >= amount
	}

	override fun has(playerName: String?, amount: Double): Boolean {
		return playerName?.let {
			has(getServer().getOfflinePlayer(playerName), amount)
		} ?: false
	}

	override fun has(playerName: String?, worldName: String?, amount: Double): Boolean {
		return has(playerName, amount)
	}

	override fun withdrawPlayer(player: OfflinePlayer, amount: Double): EconomyResponse {
		if (!hasAccount(player)) {
			createPlayerAccount(player)
		}

		val balance = getBalance(player)

		val rows = Main.database.update(EconomyModel) {
			set(it.balance, it.balance - amount)
			where {
				it.playerUniqueId eq player.uniqueId
			}
		}

		if (rows > 0) {
			return EconomyResponse(amount, balance, ResponseType.SUCCESS, "")
		}

		return EconomyResponse(0.0, balance, ResponseType.FAILURE, "")
	}

	override fun withdrawPlayer(playerName: String?, amount: Double): EconomyResponse {
		return playerName?.let {
			withdrawPlayer(getServer().getOfflinePlayer(playerName), amount)
		} ?: EconomyResponse(amount, 0.0, ResponseType.FAILURE, "Player not found")
	}

	override fun withdrawPlayer(playerName: String?, worldName: String?, amount: Double): EconomyResponse {
		return withdrawPlayer(playerName, amount)
	}

	override fun depositPlayer(player: OfflinePlayer, amount: Double): EconomyResponse {
		if (!hasAccount(player)) {
			createPlayerAccount(player)
		}

		val balance = getBalance(player)

		val rows = Main.database.update(EconomyModel) {
			set(it.balance, it.balance + amount)
			where {
				it.playerUniqueId eq player.uniqueId
			}
		}

		if (rows > 0) {
			return EconomyResponse(amount, balance, ResponseType.SUCCESS, "")
		}

		return EconomyResponse(0.0, balance, ResponseType.FAILURE, "")
	}

	override fun depositPlayer(playerName: String?, amount: Double): EconomyResponse {
		return playerName?.let {
			depositPlayer(getServer().getOfflinePlayer(playerName), amount)
		} ?: EconomyResponse(amount, 0.0, ResponseType.FAILURE, "Player not found")
	}

	override fun depositPlayer(playerName: String?, worldName: String?, amount: Double): EconomyResponse {
		return depositPlayer(playerName, amount)
	}

	override fun createPlayerAccount(player: OfflinePlayer): Boolean {
		val account = Economy {
			playerUniqueId = player.uniqueId
			balance = 0.0
		}

		return Main.database.economy.add(account) > 0
	}

	override fun createPlayerAccount(playerName: String?): Boolean {
		return playerName?.let {
			createPlayerAccount(getServer().getOfflinePlayer(playerName))
		} ?: false
	}

	override fun createPlayerAccount(playerName: String?, worldName: String?): Boolean {
		return createPlayerAccount(playerName)
	}

	override fun hasBankSupport() = false

	override fun createBank(name: String?, player: String?): EconomyResponse {
		return EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "We do not support bank accounts!")
	}

	override fun deleteBank(name: String?): EconomyResponse {
		return EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "We do not support bank accounts!")
	}

	override fun bankBalance(name: String?): EconomyResponse {
		return EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "We do not support bank accounts!")
	}

	override fun bankHas(name: String?, amount: Double): EconomyResponse {
		return EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "We do not support bank accounts!")
	}

	override fun bankWithdraw(name: String?, amount: Double): EconomyResponse {
		return EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "We do not support bank accounts!")
	}

	override fun bankDeposit(name: String?, amount: Double): EconomyResponse {
		return EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "We do not support bank accounts!")
	}

	override fun isBankOwner(name: String?, playerName: String?): EconomyResponse {
		return EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "We do not support bank accounts!")
	}

	override fun isBankMember(name: String?, playerName: String?): EconomyResponse {
		return EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "We do not support bank accounts!")
	}

	override fun getBanks(): MutableList<String> = mutableListOf()
}