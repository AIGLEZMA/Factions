package com.massivecraft.factions.util;

import org.bukkit.entity.Player;
import org.github.paperspigot.Title;

import java.util.HashMap;
import java.util.Map;

/**
 * With help from https://www.spigotmc.org/threads/send-titles-to-players-using-spigot-1-8-1-11-2.48819/
 */
public class TitleAPI {

    private static TitleAPI instance;
    private final Map<String, Class<?>> classCache = new HashMap<>();

    public TitleAPI() {
        instance = this;
    }

    public static TitleAPI getInstance() {
        return instance;
    }

    /**
     * Send a title to player
     *
     * @param player      Player to send the title to
     * @param title       The text displayed in the title
     * @param subtitle    The text displayed in the subtitle
     * @param fadeInTime  The time the title takes to fade in
     * @param showTime    The time the title is displayed
     * @param fadeOutTime The time the title takes to fade out
     */
    public void sendTitle(Player player, String title, String subtitle, int fadeInTime, int showTime, int fadeOutTime) {
        player.sendTitle(Title.builder().title(title).subtitle(subtitle).fadeIn(fadeInTime).fadeOut(fadeOutTime).build());
    }

    private String sanitize(String string) {
        return string.replace("\\", "\\\\").replace("\"", "\\\"");
    }

}
