package com.massivecraft.factions.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FactionsPluginRegistrationTimeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
