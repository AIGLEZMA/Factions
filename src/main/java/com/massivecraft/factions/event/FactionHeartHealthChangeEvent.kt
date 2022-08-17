package com.massivecraft.factions.event

import com.massivecraft.factions.Faction
import org.bukkit.event.Cancellable

/**
 * Fired when a faction's heart health has changed (either increased or decreased)
 */
class FactionHeartHealthChangeEvent(faction: Faction, val oldValue: Double, var newValue: Double, val cause: Cause) :
    FactionEvent(faction), Cancellable {

    private var cancelled: Boolean = false

    override fun isCancelled(): Boolean {
        return this.cancelled
    }

    override fun setCancelled(shouldCancel: Boolean) {
        this.cancelled = shouldCancel
    }

    enum class Cause {
        /**
         * Indicates the heart health change was caused by a plugin
         */
        PLUGIN,

        /**
         * Indicates the heart health change was caused by the admin command
         */
        COMMAND,

        /**
         * Indicates the heart health change was caused by the heart regeneration task
         */
        REGENERATION
    }
}