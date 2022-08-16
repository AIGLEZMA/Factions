package com.massivecraft.factions.cmd.heart

import com.massivecraft.factions.cmd.CommandContext
import com.massivecraft.factions.cmd.CommandRequirements
import com.massivecraft.factions.cmd.FCommand
import com.massivecraft.factions.event.FactionHeartSetEvent
import com.massivecraft.factions.perms.Role
import com.massivecraft.factions.struct.Permission
import com.massivecraft.factions.util.TL
import com.massivecraft.factions.util.hasClaimed
import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.entity.EntityType

class CmdHeartSet : FCommand() {

    init {
        this.aliases += "set"

        this.requirements = CommandRequirements.Builder(Permission.HEART_SET)
            .memberOnly()
            .withRole(Role.ADMIN)
            .build()
    }

    override fun perform(context: CommandContext) {
        if (context.faction.isHeartPlaced) {
            context.msg(TL.COMMAND_HEARTSET_HEARTALREADYPLACED)
            return
        }

        val location = context.player.location

        if (!context.faction.hasClaimed(location)) {
            context.msg(TL.COMMAND_HEARTSET_LOCATIONNOTCLAIMED)
            return
        }

        // check if the surrounding is clear
        val blocks = mutableSetOf<Block>()
        for (x in -3..3) {
            for (z in -3..3) {
                val block = location.block.getRelative(x, 0, z)
                for (y in 0..3) {
                    blocks += block.getRelative(0, y, 0)
                }
            }
        }
        // remove the heart location itself
        blocks.removeIf { b: Block -> b.x == location.blockX && b.y == location.blockY && b.z == location.blockZ }
        val shouldSet = blocks.all { block -> block.isEmpty }

        if (!shouldSet) {
            context.msg(TL.COMMAND_HEARTSET_SURROUNDINGNOTCLEAR)
            return
        }

        val factionHeatSetEvent = FactionHeartSetEvent(context.faction, location, blocks)
        Bukkit.getServer().pluginManager.callEvent(factionHeatSetEvent)
        if (factionHeatSetEvent.isCancelled) {
            return
        }

        context.faction.apply {
            this.isHeartPlaced = true
            this.isHeartRecentlyPlaced = true
            this.heartLocation = location
            this.heartProtectedRegion = blocks.map { block -> block.location }.toSet()
        }

        location.world.spawnEntity(location, EntityType.ENDER_CRYSTAL)


        context.msg(TL.COMMAND_HEARTSET_SUCCESSFUL)
        context.faction.fPlayers
            .filter { fPlayer -> fPlayer != context.fPlayer }
            .forEach { fPlayer -> fPlayer.msg(TL.COMMAND_HEARTSET_MEMBERS, context.fPlayer.tag) }


    }

    override fun getUsageTranslation(): TL {
        return TL.COMMAND_HEARTSET_DESCRIPTION
    }
}