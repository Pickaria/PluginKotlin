package fr.pickaria.economy

import fr.pickaria.Main
import fr.pickaria.executeSQL
import fr.pickaria.executeSelect
import net.milkbowl.vault.economy.AbstractEconomy
import net.milkbowl.vault.economy.EconomyResponse
import net.milkbowl.vault.economy.EconomyResponse.ResponseType
import org.bukkit.Bukkit.getServer
import org.bukkit.OfflinePlayer
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.text.DecimalFormat


class Economy : AbstractEconomy() {
	override fun isEnabled() = true

	override fun getName() = "Pickaria economy"

	override fun fractionalDigits() = -1

	override fun format(amount: Double): String {
		return if (amount <= 1.0) {
			"${DecimalFormat("#.##").format(amount)} ${currencyNameSingular()}"
		} else {
			"${DecimalFormat("#.##").format(amount)} ${currencyNamePlural()}"
		}
	}

	override fun currencyNamePlural() = "$"

	override fun currencyNameSingular() = "$"

	override fun hasAccount(player: OfflinePlayer): Boolean {
		return executeSelect("SELECT balance FROM economy WHERE player_uuid = '${player.uniqueId}'")?.next() ?: false
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
		return executeSelect("SELECT balance FROM economy WHERE player_uuid = '${player.uniqueId}'")?.let { rs ->
			if (rs.next()) {
				rs.getDouble("balance")
			} else {
				createPlayerAccount(player)
				0.0
			}
		} ?: run {
			-1.0
		}
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
		return executeSelect("SELECT balance FROM economy WHERE player_uuid = '${player.uniqueId}'")?.let {
			if (it.next()) {
				it.getDouble("balance") >= amount
			} else {
				false
			}
		} ?: false
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
		val balance = getBalance(player)

		if (balance - amount < 0) {
			return EconomyResponse(amount, balance, ResponseType.FAILURE, "Not enough money")
		}

		if (executeSQL("UPDATE economy SET balance = ${balance - amount} WHERE player_uuid = '${player.uniqueId}'")) {
			return EconomyResponse(amount, balance, ResponseType.SUCCESS, "")
		}

		return EconomyResponse(amount, balance, ResponseType.FAILURE, "")
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
		val balance = getBalance(player)

		if (executeSQL("UPDATE economy SET balance = ${balance + amount} WHERE player_uuid = '${player.uniqueId}'")) {
			return EconomyResponse(amount, balance, ResponseType.SUCCESS, "")
		}

		return EconomyResponse(amount, balance, ResponseType.FAILURE, "")
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
		return executeSQL("INSERT INTO economy(player_uuid) VALUES ('${player.uniqueId}')")
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