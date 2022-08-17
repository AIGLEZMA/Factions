package com.massivecraft.factions.util

import com.massivecraft.factions.Faction
import com.massivecraft.factions.Factions
import com.massivecraft.factions.FactionsPlugin
import com.massivecraft.factions.event.FactionHeartHealthChangeEvent
import com.massivecraft.factions.integration.HolographicDisplays
import org.apache.commons.lang3.time.DurationFormatUtils
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import java.util.concurrent.TimeUnit
import kotlin.Pair

class HeartRegenTask : BukkitRunnable() {

    private val remember = mutableMapOf<Faction, Pair<Int, Long>>() // <Phase>, <Counter>

    init {
        runTaskTimer(FactionsPlugin.getInstance(), 20L, 20L)
    }

    override fun run() {
        for (faction in Factions.getInstance().allFactions.filter { faction -> faction.isNormal }) {
            if (!faction.isHeartPlaced || faction.heartLocation == null) {
                continue
            }

            // update the hologram
            HolographicDisplays.updateHologram(faction)

            if (faction.heartHealth == 100.0) {
                if (faction.isHeartRecentlyPlaced) {
                    faction.isHeartRecentlyPlaced = false
                }
                continue
            }

            val counter = remember.getOrPut(faction) { Pair(1, 0) }

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
            if (faction.isHeartRecentlyPlaced || (hasPaid && membersNearby && !attackedRecently)) {
                if (counter.second == PHASE) {
                    val event = FactionHeartHealthChangeEvent(
                        faction,
                        faction.heartHealth,
                        (faction.heartHealth + 25),
                        FactionHeartHealthChangeEvent.Cause.REGENERATION
                    )
                    Bukkit.getServer().pluginManager.callEvent(event)
                    if (event.isCancelled) {
                        continue
                    }

                    faction.heartHealth = event.newValue
                    remember[faction] = Pair(remember[faction]!!.first + 1, 0)

                    println("Added 25HP to ${faction.tag} | ${remember[faction]}")
                } else {
                    remember[faction] = Pair(remember[faction]!!.first, remember[faction]!!.second + 1)
                }
            }
        }
    }

    fun remainingTime(faction: Faction): String {
        val phase = remember[faction]?.first
        val remember = remember[faction]?.second

        if (phase == null || remember == null) {
            return "NaN"
        }

        var elapsedTime = remember
        if (phase > 1) {
            /*
             PHASE 1 = PHASE
             PHASE 2 = PHASE
             PHASE 3 = REMEMBER
             */
            elapsedTime += (phase - 1) * PHASE
        }

        val regenTime = PHASE * 4

        val diff = regenTime - elapsedTime

        return DurationFormatUtils.formatDuration(TimeUnit.SECONDS.toMillis(diff), "mm:ss")
    }

    companion object {

        private val LAST_ATTACK_THRESHOLD = TimeUnit.MINUTES.toMillis(5L)

        private val PHASE = TimeUnit.MINUTES.toSeconds(1L) // 15L

    }
}