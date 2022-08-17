package com.massivecraft.factions.util

import com.massivecraft.factions.Faction
import com.massivecraft.factions.Factions
import com.massivecraft.factions.FactionsPlugin
import com.massivecraft.factions.integration.HolographicDisplays
import org.bukkit.scheduler.BukkitRunnable
import java.util.concurrent.TimeUnit

class HeartRegenTask : BukkitRunnable() {

    private val remember = mutableMapOf<Faction, Long>()

    init {
        runTaskTimer(FactionsPlugin.getInstance(), 20L, 20L)
    }

    override fun run() {
        for (faction in Factions.getInstance().allFactions.filter { faction -> faction.isNormal }) {
            if (!faction.isHeartPlaced || faction.heartLocation == null) {
                continue
            }

            // update the hologram:
            HolographicDisplays.updateHologram(faction)

            if (faction.heartHealth == 100.0) {
                if (faction.isHeartRecentlyPlaced) {
                    faction.isHeartRecentlyPlaced = false
                }
                continue
            }

            val counter = remember.getOrPut(faction) { 0 }

            val hasPaid = true
            val membersNearby = faction.fPlayers
                .filter { fPlayer -> fPlayer.isOnline }
                .map { fPlayer -> fPlayer.player }
                .any { player ->
                    player.location.world.uid == faction.heartLocation!!.world.uid
                            && player.location.distanceSquared(faction.heartLocation) <= 100
                }
            val attackedRecently = if (faction.lastHeartAttack == 0L)
                false
            else
                (System.currentTimeMillis() - faction.lastHeartAttack) >= LAST_ATTACK_THRESHOLD

            // if it's recently placed it's free
            if ((hasPaid || faction.isHeartRecentlyPlaced) && membersNearby && !attackedRecently) {
                if (counter == PHASE) {
                    faction.heartHealth += 25
                    remember[faction] = 0

                    println("Added 25HP to ${faction.tag}")
                } else {
                    remember[faction] = counter + 1
                }
            }
        }
    }

    companion object {

        private val LAST_ATTACK_THRESHOLD = TimeUnit.MINUTES.toMillis(5L)

        private val PHASE = TimeUnit.MINUTES.toSeconds(1L) // 15L

    }
}