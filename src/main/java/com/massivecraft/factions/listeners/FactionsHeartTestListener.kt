package com.massivecraft.factions.listeners

import com.massivecraft.factions.event.FactionHeartHealthChangeEvent
import com.massivecraft.factions.event.FactionHeartPreSetEvent
import com.massivecraft.factions.event.FactionHeartRegenItemBoughtEvent
import com.massivecraft.factions.event.FactionHeartSetEvent
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/**
 * Listeners to test our custom listeners
 */
class FactionsHeartTestListener : Listener {

    @EventHandler
    fun onHeartPreSetEvent(e: FactionHeartPreSetEvent) {
        //println("(FactionHeartPreSetEvent) INIT ${e.faction.tag} - ${debug(e.location)} - ${debug(e.protectedRegion)}")
    }

    @EventHandler
    fun onHeartSetEvent(e: FactionHeartSetEvent) {
        //println("(FactionHeartSetEvent) INIT ${e.faction.tag} - ${debug(e.location)} - ${debug(e.protectedRegion)}")
    }

    @EventHandler
    fun onHeartHealthChangeEvent(e: FactionHeartHealthChangeEvent) {
        //println("(FactionHeartHealthChangeEvent) INIT ${e.faction.tag} - ${e.oldValue} - ${e.newValue} - ${e.cause}")
    }

    @EventHandler
    fun onFactionHeartRegenItemBoughtEvent(e: FactionHeartRegenItemBoughtEvent) {
        com.massivecraft.factions.util.debug("[EVENT] (FactionHeartRegenItemBoughtEvent) ${e.faction.tag} - ${e.item} - ${e.buyer.tag}")
    }

    private fun debug(location: Location): String {
        return "${location.blockX}, ${location.blockY}, ${location.blockZ}"
    }

    private fun debug(protectedRegion: Set<Block>): String {
        val materials = mutableListOf<Material>()
        for (block in protectedRegion) {
            if (materials.contains(block.type)) {
                continue
            }
            materials.add(block.type)
        }
        return "${protectedRegion.size} (${materials.joinToString(",")})"
    }

}