package com.massivecraft.factions.listeners

import com.massivecraft.factions.Board
import com.massivecraft.factions.FLocation
import com.massivecraft.factions.FPlayers
import com.massivecraft.factions.gui.RegenerationGUI
import com.massivecraft.factions.util.TL
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent

class FactionsHeartListener : Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onRightClickOnHeart(e: PlayerInteractAtEntityEvent) {
        val player = e.player
        val entity = e.rightClicked
        if (entity.type != EntityType.ENDER_CRYSTAL) {
            return
        }

        val fPlayer = FPlayers.getInstance().getByPlayer(player)
        if (!fPlayer.hasFaction() || !fPlayer.faction?.isHeartPlaced!!) {
            return
        }

        // TODO: placing ? removing ? you can't click

        // check location
        val factionAtLocation = Board.getInstance().getFactionAt(FLocation(entity.location))
        if (factionAtLocation != fPlayer.faction) {
            fPlayer.msg(TL.HEART_RIGHTCLICK_ERROR)
            return
        }

        RegenerationGUI(fPlayer).apply {
            build()
            open()
        }
    }

}