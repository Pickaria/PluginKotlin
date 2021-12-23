package fr.pickaria

import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement


fun executeSQL(sql: String): Boolean {
	return try {
		val st: Statement = Main.connection.createStatement()
		return (st.executeUpdate(sql) > 0).also {
			st.close()
		}
	} catch (e: SQLException) {
		e.printStackTrace()
		false
	}
}

fun executeSelect(sql: String): ResultSet? {
	return try {
		val st: Statement = Main.connection.createStatement()
		st.executeQuery(sql)
	} catch (e: SQLException) {
		e.printStackTrace()
		null
	}
}