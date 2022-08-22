package com.massivecraft.factions.config.file

import com.massivecraft.factions.config.annotation.Comment
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
        var random = 4

        @Comment(
            "Items that will be show on the regeneration GUI \n" +
                    "For the type your must specify either MONEY[amount] or ITEM"
        )
                /*
                 <slot>:
                    type: (item/money[amount])
                    name: String
                    material: Material
                 */
        // BUG: we need to use String then parse it to Int later
        var items = mutableMapOf<String, MutableMap<String, String>>(
            "20" to mutableMapOf("type" to "money[5000]", "name" to "&a5000$", "material" to "PAPER"),
            "21" to mutableMapOf("type" to "money[10000]", "name" to "&a10000$", "material" to "PAPER"),
            "22" to mutableMapOf("type" to "money[10]", "name" to "&a10$", "material" to "PAPER"),
            "23" to mutableMapOf("type" to "money[25]", "name" to "&a25$", "material" to "PAPER"),
            "24" to mutableMapOf("type" to "item", "name" to "&eÉmeraude", "material" to "EMERALD"),
            "29" to mutableMapOf("type" to "item", "name" to "&bDiamant", "material" to "DIAMOND_ORE"),
            "30" to mutableMapOf("type" to "item", "name" to "&7Fer", "material" to "IRON_ORE")
        )

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