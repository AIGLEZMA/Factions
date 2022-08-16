package com.massivecraft.factions.perms;

import org.bukkit.Material;

public interface PermissibleAction {
    @Deprecated
    static PermissibleAction valueOf(String name) {
        PermissibleAction action = PermissibleActionRegistry.get(name);
        if (action == null) {
            throw new IllegalArgumentException("Invalid name '" + name + "'");
        }
        return action;
    }

    @Deprecated
    boolean isFactionOnly();

    @Deprecated
    Material getMaterial();

    String getName();

    String getDescription();

    String getShortDescription();
}
