package com.massivecraft.factions.struct

import com.massivecraft.factions.FPlayer
import com.massivecraft.factions.Faction
import com.massivecraft.factions.FactionsPlugin

abstract class HeartDamageProvider {

    /**
     * Returns the amount of damage the damaged faction's heart will get
     */
    abstract fun provide(damager: FPlayer, damaged: Faction): Double

}

class DefaultHeartDamageProvider(val plugin: FactionsPlugin) : HeartDamageProvider() {

    override fun provide(damager: FPlayer, damaged: Faction): Double {
        return plugin.configManager.heartConfig.damage.coerceAtLeast(0.0)
    }

}