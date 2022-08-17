package com.massivecraft.factions.config.file

import com.massivecraft.factions.config.annotation.Comment

@SuppressWarnings(
    "FieldCanBeLocal",
    "FieldMayBeFinal",
    "InnerClassMayBeStatic",
    "BooleanMethodIsAlwaysInverted",
    "MismatchedQueryAndUpdateOfCollection"
)
class HeartConfig {

    @Comment("Holograms configuration")
    var holograms = Holograms()

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
            "&7» &fPower: &a{power}&f/{maxPower}",
            "&7» &fPlayers: &a{online} &fout of {members}",
            "&7» &fHealth: &r{heart-fo-health} &7(&f{heart-health}&7)",
            "&7&m---------------------------"
        )

    }

}