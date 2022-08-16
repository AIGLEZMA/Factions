package com.massivecraft.factions.event

import com.massivecraft.factions.Faction
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.event.Cancellable

/**
 * This event is called when a faction has placed it's heart
 */
class FactionHeartSetEvent(faction: Faction, var location: Location, var protectedRegion: Set<Block>) :
    FactionEvent(faction), Cancellable {

    private var cancelled: Boolean = false

    override fun isCancelled(): Boolean {
        return this.cancelled
    }

    override fun setCancelled(shouldCancel: Boolean) {
        this.cancelled = shouldCancel
    }
}