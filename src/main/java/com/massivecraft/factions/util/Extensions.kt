package com.massivecraft.factions.util

import com.massivecraft.factions.Board
import com.massivecraft.factions.FLocation
import com.massivecraft.factions.Faction
import org.bukkit.Location

/**
 * An extension to check whether the faction has claimed the given location
 */
fun Faction.hasClaimed(location: Location): Boolean {
    return Board.getInstance().getFactionAt(FLocation(location)) == this
}