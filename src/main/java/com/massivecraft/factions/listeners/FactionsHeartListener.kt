package com.massivecraft.factions.listeners

import com.massivecraft.factions.Board
import com.massivecraft.factions.FLocation
import com.massivecraft.factions.FPlayers
import com.massivecraft.factions.FactionsPlugin
import com.massivecraft.factions.event.FactionHeartHealthChangeEvent
import com.massivecraft.factions.gui.HeartRegenerationGUI
import com.massivecraft.factions.perms.Relation
import com.massivecraft.factions.util.TL
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent

class FactionsHeartListener(val plugin: FactionsPlugin) : Listener {

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

        if (factionAtLocation.heartHealth == 100.0) {
            fPlayer.msg(TL.HEART_RIGHTCLICK_FULLHEALTH)
            return
        }

        HeartRegenerationGUI(fPlayer).apply {
            build()
            open()
        }
    }

    @Suppress("SpellCheckingInspection")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onHeartDamage(e: EntityDamageEvent) {
        if (e.entityType != EntityType.ENDER_CRYSTAL) return
        val damaged = e.entity

        if (e !is EntityDamageByEntityEvent) {
            println(e.cause)
            e.isCancelled = true
        } else {
            val damager = e.damager
            if (damager !is Player) {
                // if the heart is attacked by an entity we cancel
                e.isCancelled = true
                return
            }

            val location = damaged.location
            val factionAtLocation = Board.getInstance().getFactionAt(FLocation(location))
            if (!factionAtLocation.isHeartPlaced || factionAtLocation.heartLocation != location) {
                return
            }

            e.isCancelled = true // we're sure it's a faction heart

            val fdamager = FPlayers.getInstance().getByPlayer(damager)

            when (factionAtLocation.getRelationTo(fdamager)) {
                Relation.MEMBER -> {
                    fdamager.msg(TL.HEART_DAMAGE_MEMBER)
                    return
                }

                Relation.ALLY, Relation.TRUCE -> {
                    fdamager.msg(TL.HEART_DAMAGE_ALLYORTRUCE)
                    return
                }

                Relation.NEUTRAL -> {
                    fdamager.msg(TL.HEART_DAMAGE_NEUTRAL)
                    return
                }

                Relation.ENEMY -> {} // good
            }

            // TODO: check if it's placed recently or is removing
            if (factionAtLocation.isHeartRecentlyPlaced) {
                fdamager.msg(TL.HEART_DAMAGE_RECENTLYPLACED)
                return
            }

            if (damager.itemInHand == null || damager.itemInHand.type != Material.DIAMOND_SWORD) {
                fdamager.msg(TL.HEART_DAMAGE_WRONGITEM)
                return
            }

            val damage = plugin.heartDamageProvider.provide(fdamager, factionAtLocation)
            if (damage == 0.0) {
                return
            }

            val event = FactionHeartHealthChangeEvent(
                factionAtLocation,
                factionAtLocation.heartHealth,
                (factionAtLocation.heartHealth - damage).coerceAtLeast(0.0),
                FactionHeartHealthChangeEvent.Cause.DAMAGE
            )
            Bukkit.getServer().pluginManager.callEvent(event)
            if (event.isCancelled) {
                return
            }

            factionAtLocation.lastHeartAttack = System.currentTimeMillis()
            factionAtLocation.setHeartHealth(event.newValue, false)
            if (factionAtLocation.heartHealth == 0.0) {
                // TODO: add points
            }
        }
    }
}