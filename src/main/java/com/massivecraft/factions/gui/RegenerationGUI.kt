package com.massivecraft.factions.gui

import com.massivecraft.factions.FPlayer
import com.massivecraft.factions.FactionsPlugin
import com.massivecraft.factions.integration.Econ
import com.massivecraft.factions.tag.Tag
import com.massivecraft.factions.util.TL
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.inventory.ClickType

class RegenerationGUI(val fPlayer: FPlayer) :
    GUI<Int>(fPlayer, FactionsPlugin.getInstance().configManager.heartConfig.regeneration.gui.size) {

    private val config = FactionsPlugin.getInstance().configManager.heartConfig.regeneration

    override fun getName(): String {
        return ChatColor.translateAlternateColorCodes('&', config.gui.title)
    }

    override fun parse(toParse: String, type: Int): String {
        if (type == -2) {
            // we only parse leader item
            return Tag.parsePlain(fPlayer, toParse)
        }

        return toParse
    }

    override fun createSlotMap(): MutableMap<Int, Int> {
        // TODO: chance
        val slotMap = mutableMapOf<Int, Int>()
        for ((slot, _) in config.items) {
            slotMap[slot] = slot
        }

        slotMap[config.gui.closeItem.slot] = -1
        slotMap[config.gui.leaderItem.slot] = -2
        return slotMap
    }

    override fun createDummyItems(): MutableMap<Int, SimpleItem> {
        val dummyItems = mutableMapOf<Int, SimpleItem>()
        for (slot in config.gui.staticItemSlots) {
            dummyItems[slot] = SimpleItem(config.gui.staticItem)
        }
        return dummyItems
    }

    override fun getItem(type: Int): SimpleItem {
        if (type == -1) {
            return config.gui.closeItem
        }

        if (type == -2) {
            return config.gui.leaderItem
        }

        val item = config.items[type]
        if (item == null) {
            println("Item at slot $type is null")
            return BLANK
        }

        val materialName = item["material"]
        if (materialName == null) {
            println("Material of item at slot $type is null")
            return BLANK
        }

        val material = Material.getMaterial(materialName)
        if (material == null) {
            println("Material of item at slot $type with name $materialName is INVALID")
            return BLANK
        }

        // we have material now check name
        val name = item["name"]
        if (name == null) {
            println("Name of item at slot $type is null")
            return BLANK
        }

        return SimpleItem.builder()
            .setMaterial(material)
            .setName(ChatColor.translateAlternateColorCodes('&', name))
            .build()
    }

    override fun onClick(action: Int, clickType: ClickType) {
        if (action == -1) {
            fPlayer.player.closeInventory()
            return
        }

        if (config.items.keys.contains(action)) {
            val item = config.items[action]!!

            val type = item["type"]
            if (type == null) {
                println("Type of item at slot $action is undefined")
                return
            }

            // money $$$$$
            if (type.startsWith("money", true)) {
                val amount = type.filter { it.isDigit() }.toDoubleOrNull()
                if (amount == null) {
                    println("Amount of item at slot $action is not correct ($type) (${type.filter { it.isDigit() }})")
                }

                if (!Econ.has(fPlayer, amount!!)) {
                    fPlayer.msg(TL.HEART_GUI_NOTENOUGHMONEY, amount)
                    return
                }

                val transaction = Econ.withdraw(fPlayer, amount)
                if (!transaction) {
                    fPlayer.msg(TL.HEART_GUI_TRANSACTIONFAILED)
                    return
                }


            }

        }
    }

    companion object {

        private val BLANK = SimpleItem.builder()
            .setName("BLANK")
            .setMaterial(Material.STONE)
            .build()

    }
}