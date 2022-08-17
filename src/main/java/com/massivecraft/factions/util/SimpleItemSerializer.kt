package com.massivecraft.factions.util

import com.google.common.reflect.TypeToken
import com.massivecraft.factions.gui.SimpleItem
import ninja.leaping.configurate.ConfigurationNode
import org.bukkit.DyeColor
import org.bukkit.Material


object SimpleItemSerializer {

    fun serialize(simpleItem: SimpleItem?, target: ConfigurationNode) {
        if (simpleItem == null) {
            return
        }

        target.getNode("enchant").value = simpleItem.isEnchant

        if (simpleItem.name != null) {
            target.getNode("name").value = simpleItem.name
        }
        if (simpleItem.lore != null) {
            target.getNode("lore").value = simpleItem.lore
        }
        if (simpleItem.material != null) {
            target.getNode("material").value = simpleItem.material.name
        }
        if (simpleItem.color != null) {
            target.getNode("color").value = simpleItem.color.name
        }
    }

    fun deserialize(source: ConfigurationNode): SimpleItem {
        val simpleItemBuilder = SimpleItem.builder()

        if (!source.getNode("enchant").isVirtual) {
            simpleItemBuilder.setEnchant(source.getNode("enchant").getBoolean(false))
        }

        if (!source.getNode("name").isVirtual) {
            simpleItemBuilder.setName(source.getNode("name").getString("NAME NULL"))
        }

        if (!source.getNode("lore").isVirtual) {
            simpleItemBuilder.setLore(
                source.getNode("lore").getList(TypeToken.of(String::class.java), mutableListOf("NULL LORE"))
            )
        }

        if (!source.getNode("material").isVirtual) {
            simpleItemBuilder.setMaterial(Material.valueOf(source.getNode("material").getString("DIRT")))
        }

        if (!source.getNode("color").isVirtual) {
            simpleItemBuilder.setColor(DyeColor.valueOf(source.getNode("color").getString("WHITE")))
        }

        return simpleItemBuilder.build()
    }

}