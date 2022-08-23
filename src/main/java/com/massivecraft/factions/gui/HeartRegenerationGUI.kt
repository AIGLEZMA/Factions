package com.massivecraft.factions.gui

import com.massivecraft.factions.FPlayer
import com.massivecraft.factions.FactionsPlugin
import com.massivecraft.factions.event.FactionHeartRegenItemBoughtEvent
import com.massivecraft.factions.integration.Econ
import com.massivecraft.factions.util.TL
import com.massivecraft.factions.util.debug
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class HeartRegenerationGUI(val fPlayer: FPlayer) :
    GUI<String>(fPlayer, FactionsPlugin.getInstance().configManager.heartConfig.regeneration.gui.size) {

    private val config = FactionsPlugin.getInstance().configManager.heartConfig.regeneration
    private val cache = mutableMapOf<String, Pair<Material, String>>()
    private val player = fPlayer.player

    // TODO: close when disband/leave

    override fun getName(): String = ChatColor.translateAlternateColorCodes('&', config.gui.title)

    override fun parse(toParse: String, type: String): String = parseDefault(toParse)

    override fun createSlotMap(): MutableMap<Int, String> {
        val slotMap = mutableMapOf<Int, String>()
        for (item in config.items) {
            val slot = item["slot"]?.toInt() ?: continue
            val id = item["id"] ?: continue
            if (fPlayer.faction.heartRegenPaidItems.contains(id)) {
                continue
            }

            slotMap[slot] = id
        }

        slotMap[config.gui.closeItem.slot] = "CLOSE"
        slotMap[config.gui.leaderItem.slot] = "LEADER"
        return slotMap
    }

    override fun createDummyItems(): MutableMap<Int, SimpleItem> {
        val dummyItems = mutableMapOf<Int, SimpleItem>()
        for (slot in config.gui.staticItemSlots) {
            dummyItems[slot] = SimpleItem(config.gui.staticItem)
        }

        return dummyItems
    }

    override fun getItem(id: String): SimpleItem {
        if (id == "CLOSE") {
            return config.gui.closeItem
        }
        if (id == "LEADER") {
            return SimpleItem(config.gui.leaderItem)
                .apply {
                    skullHolder = fPlayer.faction.fPlayerAdmin.offlinePlayer
                }
        }

        val item = config.itemById(id)
        if (item == null) {
            debug("[GUI] Item with id $id not found")
            debug(config.items.toString())
            return BLANK
        }

        val materialName = item["material"] ?: return BLANK

        val material = Material.getMaterial(materialName) ?: return BLANK

        // we have material now check name, name is very important to check if the player has the said item in his inventory
        val name = item["name"] ?: return BLANK

        // cache only items (not money)
        val type = item["type"]
        if (type != null && type.equals("item", true)) {
            cache[id] = Pair(material, name)
        }

        return SimpleItem.builder()
            .setMaterial(material)
            .setName(ChatColor.translateAlternateColorCodes('&', name))
            .build()
    }

    override fun onClick(id: String, clickType: ClickType) {
        if (clickType != ClickType.LEFT) return
        if (id == "CLOSE") {
            player.closeInventory()
            return
        }

        val item = config.itemById(id) ?: return
        if (fPlayer.faction.heartRegenPaidItems.contains(id)) {
            fPlayer.msg(TL.HEART_REGENGUI_ITEMALREADYBOUGHT)
            reopen()
            return
        }

        val name = item["name"] ?: return
        val type = item["type"] ?: return
        val slot = item["slot"]?.toInt() ?: return

        if (type == "money") {
            val amount = item["amount"]?.toDouble() ?: return

            if (!Econ.has(fPlayer, amount)) {
                fPlayer.msg(TL.HEART_REGENGUI_NOTENOUGHMONEY, amount)
                return
            }

            player.closeInventory()
            HeartRegenConfirmGUI(fPlayer, {
                val transaction = Econ.withdraw(fPlayer, amount)
                if (!transaction) {
                    fPlayer.msg(TL.HEART_REGENGUI_TRANSACTIONFAILED)
                    reopen()
                    return@HeartRegenConfirmGUI
                }

                inventory.setItem(slot, ItemStack(Material.AIR))
                slotMap.remove(slot) // remove from slot map to prevent it being clickable again

                fPlayer.faction.addHeartRegenPaidItem(id)
                fPlayer.msg(TL.HEART_REGENGUI_BOUGHT, name)

                Bukkit.getServer().pluginManager.callEvent(FactionHeartRegenItemBoughtEvent(fPlayer, id))
                reopen()
            },
                { reopen() })
                .apply {
                    build()
                    open()
                }
        } else if (type == "item") {
            if (!cache.contains(id)) return

            val material = cache[id]!!.first
            val displayName = cache[id]!!.second

            val toRemove = ItemStack(material)
            val toRemoveMeta =
                toRemove.itemMeta.apply {
                    this.displayName = ChatColor.translateAlternateColorCodes('&', displayName)
                }
            toRemove.itemMeta = toRemoveMeta

            HeartRegenConfirmGUI(fPlayer, {
                val result = player.inventory.removeItem(toRemove)

                if (result.isNotEmpty()) {
                    fPlayer.msg(TL.HEART_REGENGUI_ITEMNOTFOUND, name)
                    reopen()

                    return@HeartRegenConfirmGUI
                } else {
                    inventory.setItem(slot, ItemStack(Material.AIR))
                    slotMap.remove(slot) // remove from slot map to prevent it being clickable again

                    fPlayer.faction.addHeartRegenPaidItem(id)
                    fPlayer.msg(TL.HEART_REGENGUI_BOUGHT, name)

                    Bukkit.getServer().pluginManager.callEvent(FactionHeartRegenItemBoughtEvent(fPlayer, id))
                    reopen()
                }
            },
                { reopen() }
            ).apply {
                build()
                open()
            }
        }
    }

    private fun reopen() {
        player.closeInventory()
        HeartRegenerationGUI(fPlayer)
            .apply {
                build()
                open()
            }
    }

    companion object {

        val BLANK: SimpleItem = SimpleItem.builder()
            .setName("BLANK")
            .setLore("If you see this then something is wrong with the config!")
            .setMaterial(Material.STONE)
            .build()

    }
}