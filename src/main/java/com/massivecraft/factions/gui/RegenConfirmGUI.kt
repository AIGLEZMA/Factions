package com.massivecraft.factions.gui

import com.massivecraft.factions.FPlayer
import com.massivecraft.factions.FactionsPlugin
import com.massivecraft.factions.gui.GUI.Backable
import org.bukkit.ChatColor
import org.bukkit.event.inventory.ClickType

class RegenConfirmGUI(val fPlayer: FPlayer, val yesConsumer: () -> Unit, val noConsumer: () -> Unit) :
    GUI<Int>(fPlayer, FactionsPlugin.getInstance().configManager.heartConfig.regeneration.confirmGUI.size), Backable {

    private val config = FactionsPlugin.getInstance().configManager.heartConfig.regeneration

    init {
        back = -3
    }

    override fun getName(): String {
        return ChatColor.translateAlternateColorCodes('&', config.confirmGUI.title)
    }

    override fun createSlotMap(): MutableMap<Int, Int> {
        return mutableMapOf(
            config.confirmGUI.yesItem.slot to -1,
            config.confirmGUI.noItem.slot to -2,
            config.confirmGUI.backItem.slot to -3
        )
    }

    override fun createDummyItems(): MutableMap<Int, SimpleItem> {
        val dummyItems = mutableMapOf<Int, SimpleItem>()
        for (slot in config.confirmGUI.staticItemSlots) {
            dummyItems[slot] = SimpleItem(config.confirmGUI.staticItem)
        }
        return dummyItems
    }

    override fun getItem(slot: Int): SimpleItem {
        return when (slot) {
            -1 -> config.confirmGUI.yesItem
            -2 -> config.confirmGUI.noItem
            -3 -> config.confirmGUI.backItem
            else -> RegenerationGUI.BLANK
        }
    }

    override fun onClick(slot: Int, clickType: ClickType) {
        if (clickType != ClickType.LEFT) return

        when (slot) {
            -1 -> {
                yesConsumer()
            }

            -2 -> {
                noConsumer()
            }
        }
    }

    override fun parse(toParse: String, slot: Int): String {
        return toParse
    }

    override fun onBack() {
        fPlayer.player.closeInventory()
        RegenerationGUI(fPlayer).apply {
            build()
            open()
        }
    }
}