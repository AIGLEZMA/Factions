package com.massivecraft.factions;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.massivecraft.factions.iface.EconomyParticipator;
import com.massivecraft.factions.perms.PermissibleAction;
import com.massivecraft.factions.perms.Relation;
import com.massivecraft.factions.perms.Role;
import com.massivecraft.factions.perms.Selectable;
import com.massivecraft.factions.struct.BanInfo;
import com.massivecraft.factions.util.LazyLocation;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Faction extends EconomyParticipator, Selectable {

    /**
     * Gets whether this faction has placed it's heart or not.
     *
     * @return whether this faction has placed it's heart or not
     */
    boolean isHeartPlaced();

    /**
     * Sets whether this faction has placed it's heart or not.
     *
     * @param value whether this faction has placed it's heart or not
     */
    void setHeartPlaced(final boolean value);

    /**
     * Gets the faction's heart location.
     * This method may not return null if the faction hasn't placed a heart.
     *
     * @return the faction's heart location
     */
    @Nullable
    Location getHeartLocation();

    /**
     * Sets the faction's heart location.
     *
     * @param location heart location
     */
    void setHeartLocation(@Nullable final Location location);

    /**
     * Gets the locations of blocks that should not be destroyed or replaced
     *
     * @return heart protected region
     */
    @NotNull
    Set<Location> getHeartProtectedRegion();

    /**
     * Sets the locations of blocks that should not be destroyed or replaced
     *
     * @param region heart protected region
     */
    void setHeartProtectedRegion(@NotNull final Set<Location> region);

    /**
     * Gets the heart health. On a scale of 100.
     *
     * @return heart health
     */
    double getHeartHealth();

    /**
     * Sets the heart health.
     *
     * @param value     heart health
     * @param fireEvent whether the plugin should fire {@link com.massivecraft.factions.event.FactionHeartHealthChangeEvent} or not
     */
    void setHeartHealth(final double value, final boolean fireEvent);

    /**
     * Sets the heart health.
     *
     * @param value heart health
     */
    default void setHeartHealth(final double value) {
        setHeartHealth(value, true);
    }

    /**
     * Gets whether the heart has recently been placed or not see the regen task
     *
     * @return true if the heart has recently been placed, otherwise this will return false
     */
    boolean isHeartRecentlyPlaced();

    /**
     * Sets whether the heart has recently been placed or not.
     *
     * @param value true if it has recently been placed otherwise false
     */
    void setHeartRecentlyPlaced(final boolean value);

    /**
     * Returns the last time the heart has been attacked.
     *
     * @return last heart attack
     */
    long getLastHeartAttack();

    /**
     * Sets the last time the heart has been attacked.
     *
     * @param lastHeartAttack last heart attack
     */
    void setLastHeartAttack(final long lastHeartAttack);

    /**
     * Returns the hologram above the heart.
     * Maybe null if the heart is no placed see {@link Faction#isHeartPlaced()}
     *
     * @return the hologram above the heart
     */
    @Nullable
    Hologram getHeartHologram();

    /**
     * Sets the hologram above the heart.
     *
     * @param hologram the hologram above the heart
     */
    void setHeartHologram(@Nullable final Hologram hologram);

    /**
     * Returns an immutable set of items bought by members to regenerate the heart
     * see {@link com.massivecraft.factions.config.file.HeartConfig.Regeneration}
     *
     * @return immutable set of items
     */
    @NotNull
    Set<Integer> getHeartRegenPaidItems();

    /**
     * Sets the items bought by members to regenerate the heart
     * see {@link Faction#getHeartRegenPaidItems()}
     *
     * @param set set of items
     */
    void setHeartRegenPaidItems(@NotNull Set<Integer> set);

    void addHeartRegenPaidItem(final int slot);

    Map<String, List<String>> getAnnouncements();

    Map<String, LazyLocation> getWarps();

    LazyLocation getWarp(String name);

    void setWarp(String name, LazyLocation loc);

    boolean isWarp(String name);

    boolean hasWarpPassword(String warp);

    boolean isWarpPassword(String warp, String password);

    void setWarpPassword(String warp, String password);

    boolean removeWarp(String name);

    void clearWarps();

    int getMaxVaults();

    void setMaxVaults(int value);

    void addAnnouncement(FPlayer fPlayer, String msg);

    void sendUnreadAnnouncements(FPlayer fPlayer);

    void removeAnnouncements(FPlayer fPlayer);

    Set<String> getInvites();

    String getId();

    void setId(String id);

    void invite(FPlayer fplayer);

    void deinvite(FPlayer fplayer);

    boolean isInvited(FPlayer fplayer);

    void ban(FPlayer target, FPlayer banner);

    void unban(FPlayer player);

    boolean isBanned(FPlayer player);

    Set<BanInfo> getBannedPlayers();

    boolean getOpen();

    void setOpen(boolean isOpen);

    boolean isPeaceful();

    void setPeaceful(boolean isPeaceful);

    boolean getPeacefulExplosionsEnabled();

    void setPeacefulExplosionsEnabled(boolean val);

    boolean noExplosionsInTerritory();

    boolean isPermanent();

    void setPermanent(boolean isPermanent);

    String getTag();

    void setTag(String str);

    String getTag(String prefix);

    String getTag(Faction otherFaction);

    String getTag(FPlayer otherFplayer);

    String getComparisonTag();

    String getDescription();

    void setDescription(String value);

    String getLink();

    void setLink(String value);

    void delHome();

    boolean hasHome();

    Location getHome();

    void setHome(Location home);

    long getFoundedDate();

    void setFoundedDate(long newDate);

    void confirmValidHome();

    boolean noPvPInTerritory();

    boolean noMonstersInTerritory();

    boolean isNormal();

    /**
     * Players in the wilderness faction are consdiered not in a faction.
     *
     * @return true if wilderness
     * @deprecated use {@link #isWilderness()} instead
     */
    @Deprecated
    default boolean isNone() {
        return isWilderness();
    }

    boolean isWilderness();

    boolean isSafeZone();

    boolean isWarZone();

    boolean isPlayerFreeType();

    void setLastDeath(long time);

    int getKills();

    int getDeaths();

    /**
     * Get the access of a selectable for a given chunk.
     *
     * @param selectable        selectable
     * @param permissibleAction permissible
     * @param location          location
     * @return player's access
     */
    boolean hasAccess(Selectable selectable, PermissibleAction permissibleAction, FLocation location);

    int getLandRounded();

    int getLandRoundedInWorld(String worldName);

    @Deprecated
    int getTNTBank();

    @Deprecated
    void setTNTBank(int amount);

    Relation getRelationWish(Faction otherFaction);

    void setRelationWish(Faction otherFaction, Relation relation);

    int getRelationCount(Relation relation);

    @Deprecated
    double getDTR();

    @Deprecated
    void setDTR(double dtr);

    @Deprecated
    double getDTRWithoutUpdate();

    @Deprecated
    long getLastDTRUpdateTime();

    @Deprecated
    long getFrozenDTRUntilTime();

    @Deprecated
    boolean isFrozenDTR();

    @Deprecated
    void setFrozenDTR(long time);

    double getPower();

    double getPowerMax();

    int getPowerRounded();

    int getPowerMaxRounded();

    Integer getPermanentPower();

    void setPermanentPower(Integer permanentPower);

    boolean hasPermanentPower();

    double getPowerBoost();

    void setPowerBoost(double powerBoost);

    boolean hasLandInflation();

    boolean isPowerFrozen();

    // maintain the reference list of FPlayers in this faction
    void refreshFPlayers();

    boolean addFPlayer(FPlayer fplayer);

    boolean removeFPlayer(FPlayer fplayer);

    int getSize();

    Set<FPlayer> getFPlayers();

    Set<FPlayer> getFPlayersWhereOnline(boolean online);

    Set<FPlayer> getFPlayersWhereOnline(boolean online, FPlayer viewer);

    FPlayer getFPlayerAdmin();

    List<FPlayer> getFPlayersWhereRole(Role role);

    List<Player> getOnlinePlayers();

    // slightly faster check than getOnlinePlayers() if you just want to see if
    // there are any players online
    boolean hasPlayersOnline();

    void memberLoggedOff();

    // used when current leader is about to be removed from the faction;
    // promotes new leader, or disbands faction if no other members left
    void promoteNewLeader();

    Role getDefaultRole();

    void setDefaultRole(Role role);

    void sendMessage(String message);

    void sendMessage(List<String> messages);

    Map<FLocation, Set<String>> getClaimOwnership();

    void clearAllClaimOwnership();

    void clearClaimOwnership(FLocation loc);

    void clearClaimOwnership(FPlayer player);

    int getCountOfClaimsWithOwners();

    boolean doesLocationHaveOwnersSet(FLocation loc);

    boolean isPlayerInOwnerList(FPlayer player, FLocation loc);

    void setPlayerAsOwner(FPlayer player, FLocation loc);

    void removePlayerAsOwner(FPlayer player, FLocation loc);

    Set<String> getOwnerList(FLocation loc);

    String getOwnerListString(FLocation loc);

    boolean playerHasOwnershipRights(FPlayer fplayer, FLocation loc);

    void remove();

    Set<FLocation> getAllClaims();
}
