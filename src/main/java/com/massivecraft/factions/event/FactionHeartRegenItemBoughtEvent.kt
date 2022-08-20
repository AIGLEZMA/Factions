package com.massivecraft.factions.event

import com.massivecraft.factions.FPlayer

/**
 * Fired when a regeneration item has been bought
 */
@Suppress("MemberVisibilityCanBePrivate")
class FactionHeartRegenItemBoughtEvent(val buyer: FPlayer, val item: Int) : FactionEvent(buyer.faction)