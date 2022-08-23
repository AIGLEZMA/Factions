package com.massivecraft.factions.config.file

import com.massivecraft.factions.config.annotation.Comment
import com.massivecraft.factions.config.annotation.ConfigName
import com.massivecraft.factions.gui.SimpleItem
import org.bukkit.DyeColor
import org.bukkit.Material

@SuppressWarnings(
    "FieldCanBeLocal",
    "FieldMayBeFinal",
    "InnerClassMayBeStatic",
    "BooleanMethodIsAlwaysInverted",
    "MismatchedQueryAndUpdateOfCollection"
)
class HeartConfig {

    @Comment("Amount of damaged the attacked faction heart will get")
    var damage = 5.0

    @Comment("Holograms configuration")
    var holograms = Holograms()

    @Comment("Regeneration configuration")
    var regeneration = Regeneration()

    class Regeneration {

        @Comment("Number of items chosen randomly from the list above")
        var chance = 5

        fun itemById(id: String): Map<String, String>? = items.find { map -> map["id"] == id }

        @Suppress("PropertyName")
        @Comment(
            """
            Items that will be show on the regeneration GUI
            id: unique identifier
            slot: 0 < n < 54 (n must not be in static items slots
            type: money/item
            amount: x (only if type = money)
            name: &cA cool name
            material: BUKKITNAME (https://helpch.at/docs/1.8.8/org/bukkit/Material.html)
            """
        )

        @ConfigName("items")
        var _items = mutableListOf<Map<String, String>>(
            mutableMapOf(
                "id" to "item1",
                "slot" to "20",
                "type" to "money",
                "amount" to "5000",
                "name" to "&a$5000",
                "material" to "PAPER"
            ),
            mutableMapOf(
                "id" to "item2",
                "slot" to "21",
                "type" to "money",
                "amount" to "500",
                "name" to "&a$500",
                "material" to "PAPER"
            ),
            mutableMapOf(
                "id" to "item3",
                "slot" to "22",
                "type" to "money",
                "amount" to "50",
                "name" to "&a$50",
                "material" to "PAPER"
            ),
            mutableMapOf(
                "id" to "item4",
                "slot" to "23",
                "type" to "money",
                "amount" to "5",
                "name" to "&a$5",
                "material" to "PAPER"
            ),
            mutableMapOf(
                "id" to "item5",
                "slot" to "24",
                "type" to "money",
                "amount" to "1",
                "name" to "&a$1",
                "material" to "PAPER"
            ),

            mutableMapOf(
                "id" to "item6",
                "slot" to "29",
                "type" to "item",
                "name" to "&aEmerald",
                "material" to "EMERALD_ORE"
            ),
            mutableMapOf(
                "id" to "item7",
                "slot" to "30",
                "type" to "item",
                "name" to "&bDiamond",
                "material" to "DIAMOND"
            ),
            mutableMapOf(
                "id" to "item8",
                "slot" to "31",
                "type" to "item",
                "name" to "&6Gold",
                "material" to "GOLD_INGOT"
            ),
            mutableMapOf(
                "id" to "item9",
                "slot" to "32",
                "type" to "item",
                "name" to "&7Iron",
                "material" to "IRON_INGOT"
            ),
            mutableMapOf("id" to "item10", "slot" to "33", "type" to "item", "name" to "&0Coal", "material" to "COAL"),
        )

        @Transient
        val items = _items.shuffled().take(chance)

        @Comment("Regeneration GUI configuration")
        var gui = GUI()

        @Comment("Confirmation GUI configuration")
        var confirmGUI = ConfirmGUI()

        class GUI {

            @Comment("Inventory size (1..6)")
            var size = 6

            @Comment("Inventory title")
            var title = "Regeneration GUI"

            @Comment(
                "Slots that will be filled with static item \n" +
                        "https://images.app.goo.gl/7xPAZH7AwbLSnNDu8"
            )
            var staticItemSlots = mutableListOf(
                0, 1, 2, 3, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 50, 51, 52, 53
            )

            @Comment("Static item")
            var staticItem: SimpleItem = SimpleItem.builder()
                .setMaterial(Material.STAINED_GLASS_PANE)
                .setName("&7")
                .setColor(DyeColor.GRAY)
                .build()

            @Comment("Leader item")
            var leaderItem: SimpleItem = SimpleItem.builder()
                .setMaterial(Material.SKULL_ITEM)
                .setData(3)
                .setName("Faction : {faction}")
                .setLore(
                    "&7» &fLeader: &b{leader}",
                    "&7» &fCreation date: &b{create-date}",
                    "&7",
                    "&7» &fYour balance: &a\${balance}",
                    "&7» &fYour role: &b{player-role-prefix}",
                    "&7» &cTODO: upgrades etc"
                )
                .atSlot(4)
                .build()

            @Comment("Close item")
            var closeItem: SimpleItem = SimpleItem.builder()
                .setMaterial(Material.STAINED_GLASS_PANE)
                .setName("&cCLOSE")
                .setColor(DyeColor.RED)
                .atSlot(49)
                .build()

        }

        class ConfirmGUI {

            @Comment("Inventory size (1..6)")
            var size = 3

            @Comment("Inventory title")
            var title = "Confirmation GUI"

            @Comment(
                "Slots that will be filled with static item \n" +
                        "https://images.app.goo.gl/7xPAZH7AwbLSnNDu8"
            )
            var staticItemSlots = mutableListOf(
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 19, 20, 21, 23, 24, 25, 26
            )

            @Comment("Static item")
            var staticItem: SimpleItem = SimpleItem.builder()
                .setMaterial(Material.STAINED_GLASS_PANE)
                .setName("&7")
                .setColor(DyeColor.GRAY)
                .build()

            @Comment("Yes item")
            var yesItem: SimpleItem = SimpleItem.builder()
                .setMaterial(Material.STAINED_GLASS_PANE)
                .setColor(DyeColor.LIME)
                .setName("&aYES !")
                .atSlot(11)
                .build()

            @Comment("No item")
            var noItem: SimpleItem = SimpleItem.builder()
                .setMaterial(Material.STAINED_GLASS_PANE)
                .setColor(DyeColor.RED)
                .setName("&cNO !")
                .atSlot(15)
                .build()

            @Comment("Back item")
            var backItem: SimpleItem = SimpleItem.builder()
                .setMaterial(Material.STAINED_GLASS_PANE)
                .setName("&6BACK")
                .setColor(DyeColor.RED)
                .atSlot(22)
                .build()

        }

    }

    class Holograms {

        @Comment("Height (hauteur) in which the hologram will be placed")
        var height: Double = 3.75

        @Comment(
            "Lines which will be shown above the heart\n" +
                    "Supports faction placeholders, and i added {heart-fo-health}: ♥♥♥♥♥ ET {heart-health}: 100.0 "
        )
        var lines = mutableListOf(
            "&7&m---------------------------",
            "&fCoeur de faction : &b{faction} &f- &b{leader}",
            "&7» &fPlayers: &a{online} &fout of {members}",
            "&7» &fRemaining time: &a{heart-regen}",
            "&7» &fHealth: &r{heart-fo-health} &7(&f{heart-health}&7)",
            "&7&m---------------------------"
        )

    }

}