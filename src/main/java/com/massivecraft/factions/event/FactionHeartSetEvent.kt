package com.massivecraft.factions.event

import com.massivecraft.factions.Faction
import org.bukkit.Location
import org.bukkit.block.Block

/**
 * This event is fired when a faction has placed it's heart
 */
class FactionHeartSetEvent(faction: Faction, var location: Location, var protectedRegion: Set<Block>) :
    FactionEvent(faction)