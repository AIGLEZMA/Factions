package com.massivecraft.factions.gui

import com.massivecraft.factions.FPlayer
import com.massivecraft.factions.FactionsPlugin
import com.massivecraft.factions.integration.Econ
import com.massivecraft.factions.util.TL
import com.massivecraft.factions.util.debug
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class RegenerationGUI(val fPlayer: FPlayer) :
    GUI<Int>(fPlayer, FactionsPlugin.getInstance().configManager.heartConfig.regeneration.gui.size) {

    private val config = FactionsPlugin.getInstance().configManager.heartConfig.regeneration
    private val cache = mutableMapOf<Int, Pair<Material, String>>()
    private val player = fPlayer.player

    // TODO: close when disband/leave

    override fun getName(): String = ChatColor.translateAlternateColorCodes('&', config.gui.title)

    override fun parse(toParse: String, type: Int): String = parseDefault(toParse)

    override fun createSlotMap(): MutableMap<Int, Int> {
        // TODO: chance
        val slotMap = mutableMapOf<Int, Int>()
        for ((slot, _) in config.items.mapKeys { it.key.toInt() }) {
            if (fPlayer.faction.heartRegenPaidItems.contains(slot)) {
                debug("[GUI] (SLOT) Faction ${fPlayer.faction.tag} already bought item $slot, Skipping...")
                continue
            }

            slotMap[slot] = slot
        }

        debug("[GUI] (SLOT) Registered ${slotMap.size} slots (items only)")

        slotMap[config.gui.closeItem.slot] = -1
        slotMap[config.gui.leaderItem.slot] = -2
        return slotMap
    }

    override fun createDummyItems(): MutableMap<Int, SimpleItem> {
        val dummyItems = mutableMapOf<Int, SimpleItem>()
        for (slot in config.gui.staticItemSlots) {
            dummyItems[slot] = SimpleItem(config.gui.staticItem)
        }

        debug("[GUI] Added ${dummyItems.size} dummy item")
        return dummyItems
    }

    override fun getItem(slot: Int): SimpleItem {
        if (slot == -1) {
            return config.gui.closeItem
        }
        if (slot == -2) {
            return SimpleItem(config.gui.leaderItem).apply {
                skullHolder = fPlayer.faction.fPlayerAdmin.offlinePlayer
            }
        }

        // TODO: put checks in a method to remove boilerplate code
        val item = config.items[slot.toString()]
        if (item == null) {
            debug("[GUI] (GET) Item at slot $slot was not found in the config BUT found on the GUI what?")
            return BLANK
        }

        val materialName = item["material"]
        if (materialName == null) {
            debug("[GUI] (GET) Material name of item at slot $slot is null, check the config")
            return BLANK
        }

        val material = Material.getMaterial(materialName)
        if (material == null) {
            debug("[GUI] (GET) Material of item at slot $slot with material name $materialName was not found, check the config")
            return BLANK
        }

        // we have material now check name, name is very important to check if the player has the said item in his inventory
        val name = item["name"]
        if (name == null) {
            debug("[GUI] (GET) Display name of item at slot $slot is null, check the config")
            return BLANK
        }

        // cache only items (not money)
        val type = item["type"]
        if (type != null && type.equals("item", true)) {
            cache[slot] = Pair(material, name)
        }

        return SimpleItem.builder()
            .setMaterial(material)
            .setName(ChatColor.translateAlternateColorCodes('&', name))
            .build()
    }

    override fun onClick(slot: Int, clickType: ClickType) {
        if (clickType != ClickType.LEFT) return

        if (slot == -1) {
            player.closeInventory()
            return
        }

        if (config.items.keys.contains(slot.toString())) {
            val item = config.items[slot.toString()]!!

            if (fPlayer.faction.heartRegenPaidItems.contains(slot)) {
                debug("[GUI] (CLICK) Type of item at slot $slot already bought by someone else from your faction")

                fPlayer.msg(TL.HEART_REGENGUI_ITEMALREADYBOUGHT)
                reopen()
                return
            }

            val type = item["type"]
            if (type == null) {
                debug("[GUI] (CLICK) Type of item at slot $slot is null, check the config")
                return
            }

            // money $$$$$
            if (type.startsWith("money", true)) {
                val amount = type.filter { it.isDigit() }.toDoubleOrNull()
                if (amount == null) {
                    debug("[GUI] (CLICK) Amount of item at slot $slot is not correct ($type) (${type.filter { it.isDigit() }})")
                    return
                }

                if (!Econ.has(fPlayer, amount)) {
                    fPlayer.msg(TL.HEART_REGENGUI_NOTENOUGHMONEY, amount)
                    return
                }

                player.closeInventory()
                RegenConfirmGUI(fPlayer,
                    {
                        val transaction = Econ.withdraw(fPlayer, amount)
                        if (!transaction) {
                            fPlayer.msg(TL.HEART_REGENGUI_TRANSACTIONFAILED)
                            return@RegenConfirmGUI
                        }

                        inventory.setItem(slot, ItemStack(Material.AIR))
                        slotMap.remove(slot) // remove from slot map to prevent it being clickable again

                        fPlayer.faction.addHeartRegenPaidItem(slot)
                        fPlayer.msg(TL.HEART_REGENGUI_BOUGHT, "$${amount}")
                        debug("[GUI] (CLICK) Added item $slot to items bought to regen for ${fPlayer.faction.tag} (${fPlayer.faction.heartRegenPaidItems})")

                        reopen()
                    },
                    { reopen() })
                    .apply {
                        build()
                        open()
                    }
            } else {
                debug("[GUI] (CLICK) Found ${cache.size} cached items")
                if (!cache.contains(slot)) {
                    debug("[GUI] (CLICK) Cache doesn't contain item with slot $slot (${cache.keys})")
                    return
                }

                val material = cache[slot]!!.first
                val name = cache[slot]!!.second

                // TODO: clean this itemstack creation
                val toRemove = ItemStack(material)
                val toRemoveMeta =
                    toRemove.itemMeta.apply { displayName = ChatColor.translateAlternateColorCodes('&', name) }
                toRemove.itemMeta = toRemoveMeta

                RegenConfirmGUI(fPlayer, {
                    val result = player.inventory.removeItem(toRemove)

                    if (result.isNotEmpty()) {
                        for (re in result.values) {
                            debug("[GUI] (CLICK) ${re.type} , ${re.itemMeta?.toString()}")
                        }

                        reopen()

                        return@RegenConfirmGUI
                    } else {
                        inventory.setItem(slot, ItemStack(Material.AIR))
                        slotMap.remove(slot) // remove from slot map to prevent it being clickable again

                        fPlayer.faction.addHeartRegenPaidItem(slot)
                        fPlayer.msg(TL.HEART_REGENGUI_BOUGHT, name)
                        debug("[GUI] (CLICK) Added item $slot to items bought to regen for ${fPlayer.faction.tag} (${fPlayer.faction.heartRegenPaidItems})")

                        reopen()
                    }
                },
                    { reopen() }
                ).apply {
                    build()
                    open()
                }
                //TODO: fire event
            }
        }
    }

    private fun reopen() {
        player.closeInventory()
        // delay opening
        Bukkit.getScheduler().runTaskLater(FactionsPlugin.getInstance(), {
            RegenerationGUI(fPlayer).apply {
                build()
                open()
            }
        }, 1L)
    }

    companion object {

        val BLANK: SimpleItem = SimpleItem.builder()
            .setName("BLANK")
            .setLore("If you see this then something is wrong with the config!")
            .setMaterial(Material.STONE)
            .build()

    }
}