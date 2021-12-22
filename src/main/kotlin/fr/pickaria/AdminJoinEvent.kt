package fr.pickaria

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class AdminJoinEvent(val admin: String): Event() {
	companion object {
		val Handlers = HandlerList()
	}

	override fun getHandlers(): HandlerList = Handlers
}

class AdminLeaveEvent(val admin: String): Event(){
	companion object {
		val Handlers = HandlerList()
	}

	override fun getHandlers(): HandlerList = Handlers
}