package com.massivecraft.factions.integration

import com.gmail.filoghost.holographicdisplays.api.Hologram
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI
import com.gmail.filoghost.holographicdisplays.api.line.TextLine
import com.massivecraft.factions.Faction
import com.massivecraft.factions.FactionsPlugin
import com.massivecraft.factions.tag.Tag
import org.bukkit.ChatColor
import kotlin.math.max

object HolographicDisplays {

    private val config = FactionsPlugin.getInstance().configManager.heartConfig.holograms

    fun createHologram(faction: Faction): Hologram {
        if (!faction.isHeartPlaced || faction.heartLocation == null) {
            throw IllegalArgumentException("Faction must have a heart before creating the hologram")
        }

        val hologram = HologramsAPI.createHologram(
            FactionsPlugin.getInstance(),
            faction.heartLocation!!.clone().add(0.0, max(config.height, 1.5), 0.0)
        )

        getLines(faction).forEach { line -> hologram.appendTextLine(line) }

        return hologram
    }

    fun updateHologram(faction: Faction) {
        if (!faction.isHeartPlaced || faction.heartLocation == null) {
            throw IllegalArgumentException("Faction must have a heart before creating the hologram")
        }

        val hologram = faction.heartHologram!!

        for ((index, line) in getLines(faction).withIndex()) {
            if (index > hologram.size()) {
                hologram.appendTextLine(line)
            } else {
                (hologram.getLine(index) as TextLine).text = line
            }
        }
    }

    private fun getLines(faction: Faction): List<String> {
        val list = mutableListOf<String>()
        for (line in config.lines) {
            list += ChatColor.translateAlternateColorCodes('&', Tag.parsePlain(faction, faction.fPlayerAdmin, line))
        }
        return list
    }

    fun formatHeartHealthBar(amount: Double, max: Double = 100.0): String {
        val percent = (amount / max).toFloat()
        val progressBars = (5 * percent).toInt() // ❤

        return "${ChatColor.LIGHT_PURPLE}❤".repeat(progressBars) + "${ChatColor.GRAY}❤".repeat(5 - progressBars)
    }
}