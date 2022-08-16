package com.massivecraft.factions.cmd.heart

import com.massivecraft.factions.cmd.CommandContext
import com.massivecraft.factions.cmd.FCmdRoot
import com.massivecraft.factions.cmd.FCommand
import com.massivecraft.factions.util.TL

class CmdHeart : FCommand() {

    init {
        this.aliases += "heart"

        this.addSubCommand(CmdHeartSet())

        this.helpLong.add(plugin.txt().parseTags(TL.COMMAND_HEART_LONG.toString()))
    }

    override fun perform(context: CommandContext) {
        context.commandChain.add(this)
        FCmdRoot.getInstance().cmdAutoHelp.execute(context)
    }

    override fun getUsageTranslation(): TL {
        return TL.COMMAND_HEART_DESCRIPTION
    }
}