package com.massivecraft.factions.cmd

import com.massivecraft.factions.struct.Permission
import com.massivecraft.factions.util.TL
import org.bukkit.Bukkit

class CmdTest : FCommand() {

    init {
        this.aliases += "test"
        requirements = CommandRequirements.Builder(Permission.DEFAULTRANK)
            .playerOnly()
            .build()
    }

    override fun perform(context: CommandContext) {
        context.msg("(Test) Creating faction")
        context.player.performCommand("f create TestFac")

        context.msg("(Test) Adding power and claiming")
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "f mp ${context.player.name} 5")
        context.player.performCommand("f claim")

        context.msg("(Test) Placing heart")
        context.player.performCommand("f heart set")
    }

    override fun getUsageTranslation(): TL {
        return TL.GENERIC_DEFAULTDESCRIPTION
    }
}