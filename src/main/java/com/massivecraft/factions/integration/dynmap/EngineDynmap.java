package com.massivecraft.factions.integration.dynmap;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.config.file.DynmapConfig;
import com.massivecraft.factions.data.MemoryBoard;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.perms.Role;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;
import org.dynmap.markers.PlayerSet;
import org.dynmap.utils.TileFlags;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

// This source code is a heavily modified version of mikeprimms plugin Dynmap-Factions.
public class EngineDynmap {
    // -------------------------------------------- //
    // CONSTANTS
    // -------------------------------------------- //

    public final static int BLOCKS_PER_CHUNK = 16;

    public final static String DYNMAP_INTEGRATION = "\u00A7dDynmap Integration: \u00A7e";

    public final static String FACTIONS = "factions";
    public final static String FACTIONS_ = FACTIONS + "_";

    public final static String FACTIONS_MARKERSET = FACTIONS_ + "markerset";

    public final static String FACTIONS_HOME = FACTIONS_ + "home";
    public final static String FACTIONS_HOME_ = FACTIONS_HOME + "_";

    public final static String FACTIONS_PLAYERSET = FACTIONS_ + "playerset";
    public final static String FACTIONS_PLAYERSET_ = FACTIONS_PLAYERSET + "_";

    // -------------------------------------------- //
    // INSTANCE & CONSTRUCT
    // -------------------------------------------- //

    private static final EngineDynmap instance = new EngineDynmap();
    private DynmapConfig dynmapConf;

    public static EngineDynmap getInstance() {
        return instance;
    }

    public DynmapAPI dynmapApi;
    public MarkerAPI markerApi;
    public MarkerSet markerset;
    private boolean enabled;
    private boolean stillNeedsToRunOnce = true;

    public boolean isRunning() {
        return enabled;
    }

    public String getVersion() {
        return this.dynmapApi == null ? null : this.dynmapApi.getDynmapVersion();
    }

    public void init(Plugin dynmap) {
        this.dynmapApi = (DynmapAPI) dynmap;

        dynmapConf = FactionsPlugin.getInstance().getConfigManager().getDynmapConfig();

        // Should we even use dynmap?
        if (!dynmapConf.dynmap().isEnabled()) {
            if (this.markerset != null) {
                this.markerset.deleteMarkerSet();
                this.markerset = null;
            }
            return;
        }

        // Schedule non thread safe sync at the end!
        Bukkit.getScheduler().scheduleSyncRepeatingTask(FactionsPlugin.getInstance(), () -> {

            if (!updateCore()) {
                return;
            }

            final Map<String, Set<String>> playerSets = createPlayersets();

            updatePlayersets(playerSets);
        }, 101L, 100L);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(FactionsPlugin.getInstance(), () -> {
            boolean doIt = true;
            if (FactionsPlugin.getInstance().getConfigManager().getDynmapConfig().dynmap().isOnlyUpdateWorldOnce()) {
                if (this.stillNeedsToRunOnce) {
                    this.stillNeedsToRunOnce = false;
                } else {
                    doIt = false;
                }
            }

            if (!updateCore()) {
                return;
            }
            // createLayer() is thread safe but it makes use of fields set in updateCore() so we must have it after.
            if (!updateLayer(createLayer())) {
                return;
            }


            if (doIt) {
                Map<String, Map<String, List<FLocation>>> worldFactionChunks = createWorldFactionChunks();
                Map<String, String> factionTag = new HashMap<>();
                Map<String, String> factionDesc = new HashMap<>();
                Map<String, DynmapStyle> factionStyle = new HashMap<>();
                worldFactionChunks.values().stream().flatMap(m -> m.keySet().stream()).distinct().forEach(factionId -> {
                    Faction faction = Factions.getInstance().getFactionById(factionId);
                    factionTag.put(factionId, faction.getTag());
                    factionDesc.put(factionId, getDescription(faction));
                    factionStyle.put(factionId, getStyle(faction));
                });

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Map<String, TempAreaMarker> areas = createAreas(worldFactionChunks, factionTag, factionDesc, factionStyle);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                updateAreas(areas);
                            }
                        }.runTask(FactionsPlugin.getInstance());
                    }
                }.runTaskAsynchronously(FactionsPlugin.getInstance());

            }

            final Map<String, TempMarker> homes = createHomes();
            updateHomes(homes);
        }, 100L, Math.max(1, dynmapConf.dynmap().getClaimUpdatePeriod()) * 20L);

        this.enabled = true;
        FactionsPlugin.getInstance().getLogger().info("Enabled Dynmap integration");
    }

    // Thread Safe / Asynchronous: No
    public boolean updateCore() {
        // Get DynmapAPI
        this.dynmapApi = (DynmapAPI) Bukkit.getPluginManager().getPlugin("dynmap");
        if (this.dynmapApi == null) {
            severe("Could not retrieve the DynmapAPI.");
            return false;
        }

        // Get MarkerAPI
        this.markerApi = this.dynmapApi.getMarkerAPI();
        if (this.markerApi == null) {
            severe("Could not retrieve the MarkerAPI.");
            return false;
        }

        return true;
    }

    // Thread Safe / Asynchronous: Yes
    public TempMarkerSet createLayer() {
        TempMarkerSet ret = new TempMarkerSet();
        ret.label = dynmapConf.dynmap().getLayerName();
        ret.minimumZoom = dynmapConf.dynmap().getLayerMinimumZoom();
        ret.priority = dynmapConf.dynmap().getLayerPriority();
        ret.hideByDefault = !dynmapConf.dynmap().isLayerVisible();
        return ret;
    }

    // Thread Safe / Asynchronous: No
    public boolean updateLayer(TempMarkerSet temp) {
        this.markerset = this.markerApi.getMarkerSet(FACTIONS_MARKERSET);
        if (this.markerset == null) {
            this.markerset = temp.create(this.markerApi, FACTIONS_MARKERSET);
            if (this.markerset == null) {
                severe("Could not create the Faction Markerset/Layer");
                return false;
            }
        } else {
            temp.update(this.markerset);
        }
        return true;
    }

    // -------------------------------------------- //
    // UPDATE: HOMES
    // -------------------------------------------- //

    // Thread Safe / Asynchronous: Yes
    public Map<String, TempMarker> createHomes() {
        Map<String, TempMarker> ret = new HashMap<>();

        // Loop current factions
        for (Faction faction : Factions.getInstance().getAllFactions()) {
            Location ps = faction.getHome();
            if (ps == null) {
                continue;
            }

            DynmapStyle style = getStyle(faction);

            String markerId = FACTIONS_HOME_ + faction.getId();

            TempMarker temp = new TempMarker();
            temp.label = ChatColor.stripColor(faction.getTag());
            temp.world = ps.getWorld().toString();
            temp.x = ps.getX();
            temp.y = ps.getY();
            temp.z = ps.getZ();
            temp.iconName = style.getHomeMarker();
            temp.description = getDescription(faction);

            ret.put(markerId, temp);
        }

        return ret;
    }

    // Thread Safe / Asynchronous: No
    // This method places out the faction home markers into the factions markerset.
    public void updateHomes(Map<String, TempMarker> homes) {
        // Put all current faction markers in a map
        Map<String, Marker> markers = new HashMap<>();
        for (Marker marker : this.markerset.getMarkers()) {
            markers.put(marker.getMarkerID(), marker);
        }

        // Loop homes
        for (Entry<String, TempMarker> entry : homes.entrySet()) {
            String markerId = entry.getKey();
            TempMarker temp = entry.getValue();

            // Get Creative
            // NOTE: I remove from the map created just in the beginning of this method.
            // NOTE: That way what is left at the end will be outdated markers to remove.
            Marker marker = markers.remove(markerId);
            if (marker == null) {
                marker = temp.create(this.markerApi, this.markerset, markerId);
                if (marker == null) {
                    EngineDynmap.severe("Could not get/create the home marker " + markerId);
                }
            } else {
                temp.update(this.markerApi, marker);
            }
        }

        // Delete Deprecated Markers
        // Only old markers should now be left
        for (Marker marker : markers.values()) {
            marker.deleteMarker();
        }
    }

    // -------------------------------------------- //
    // UPDATE: AREAS
    // -------------------------------------------- //

    // Thread Safe: YES
    public Map<String, Map<String, List<FLocation>>> createWorldFactionChunks() {
        // Create map "world name --> faction --> set of chunk coords"
        Map<String, Map<String, List<FLocation>>> worldFactionChunks = new HashMap<>();

        // Note: The board is the world. The board id is the world name.
        MemoryBoard board = (MemoryBoard) Board.getInstance();

        for (Entry<FLocation, String> entry : board.flocationIds.entrySet()) {
            String world = entry.getKey().getWorldName();

            Map<String, List<FLocation>> factionChunks = worldFactionChunks.computeIfAbsent(world, k -> new HashMap<>());

            List<FLocation> factionTerritory = factionChunks.computeIfAbsent(entry.getValue(), k -> new ArrayList<>());

            factionTerritory.add(entry.getKey());
        }

        return worldFactionChunks;
    }

    // Thread Safe: YES
    public Map<String, TempAreaMarker> createAreas(Map<String, Map<String, List<FLocation>>> worldFactionChunks, Map<String, String> factionTag, Map<String, String> factionDesc, Map<String, DynmapStyle> factionStyle) {
        Map<String, TempAreaMarker> ret = new HashMap<>();

        // For each world
        for (Entry<String, Map<String, List<FLocation>>> entry : worldFactionChunks.entrySet()) {
            String world = entry.getKey();
            Map<String, List<FLocation>> factionChunks = entry.getValue();

            // For each faction and its chunks in that world
            for (Entry<String, List<FLocation>> entry1 : factionChunks.entrySet()) {
                String factionId = entry1.getKey();
                List<FLocation> chunks = entry1.getValue();
                Map<String, TempAreaMarker> worldFactionMarkers = createAreas(world, factionId, chunks, factionTag, factionDesc, factionStyle);
                ret.putAll(worldFactionMarkers);
            }
        }

        return ret;
    }

    // Thread Safe: YES
    // Handle specific faction on specific world
    // "handle faction on world"
    public Map<String, TempAreaMarker> createAreas(String world, String factionId, List<FLocation> chunks, Map<String, String> factionTag, Map<String, String> factionDesc, Map<String, DynmapStyle> factionStyle) {
        Map<String, TempAreaMarker> ret = new HashMap<>();

        // If the faction is visible ...
        if (!isVisible(factionId, factionTag.get(factionId), world)) {
            return ret;
        }

        // ... and has any chunks ...
        if (chunks.isEmpty()) {
            return ret;
        }

        // Index of polygon for given faction
        int markerIndex = 0;

        // Create the info window
        String description = factionDesc.get(factionId);

        // Fetch Style
        DynmapStyle style = factionStyle.get(factionId);

        // Loop through chunks: set flags on chunk map
        TileFlags allChunkFlags = new TileFlags();
        ArrayList<FLocation> allChunks = new ArrayList<>(chunks.size());
        for (FLocation chunk : chunks) {
            allChunkFlags.setFlag((int) chunk.getX(), (int) chunk.getZ(), true); // Set flag for chunk
            allChunks.add(chunk);
        }

        // Loop through until we don't find more areas
        while (allChunks != null) {
            TileFlags ourChunkFlags = null;
            ArrayList<FLocation> newChunks = null;

            int minimumX = Integer.MAX_VALUE;
            int minimumZ = Integer.MAX_VALUE;
            for (FLocation chunk : allChunks) {
                int chunkX = (int) chunk.getX();
                int chunkZ = (int) chunk.getZ();

                // If we need to start shape, and this block is not part of one yet
                if (ourChunkFlags == null && allChunkFlags.getFlag(chunkX, chunkZ)) {
                    ourChunkFlags = new TileFlags(); // Create map for shape
                    floodFillTarget(allChunkFlags, ourChunkFlags, chunkX, chunkZ); // Copy shape
                    minimumX = chunkX;
                    minimumZ = chunkZ;
                }
                // If shape found, and we're in it, add to our node list
                else if (ourChunkFlags != null && ourChunkFlags.getFlag(chunkX, chunkZ)) {
                    if (chunkX < minimumX) {
                        minimumX = chunkX;
                        minimumZ = chunkZ;
                    } else if (chunkX == minimumX && chunkZ < minimumZ) {
                        minimumZ = chunkZ;
                    }
                }
                // Else, keep it in the list for the next polygon
                else {
                    if (newChunks == null) {
                        newChunks = new ArrayList<>();
                    }
                    newChunks.add(chunk);
                }
            }

            // Replace list (null if no more to process)
            allChunks = newChunks;

            if (ourChunkFlags == null) {
                continue;
            }

            // Trace outline of blocks - start from minx, minz going to x+
            int initialX = minimumX;
            int initialZ = minimumZ;
            int currentX = minimumX;
            int currentZ = minimumZ;
            Direction direction = Direction.XPLUS;
            ArrayList<int[]> linelist = new ArrayList<>();
            linelist.add(new int[]{initialX, initialZ}); // Add start point
            while ((currentX != initialX) || (currentZ != initialZ) || (direction != Direction.ZMINUS)) {
                switch (direction) {
                    case XPLUS: // Segment in X+ direction
                        if (!ourChunkFlags.getFlag(currentX + 1, currentZ)) { // Right turn?
                            linelist.add(new int[]{currentX + 1, currentZ}); // Finish line
                            direction = Direction.ZPLUS; // Change direction
                        } else if (!ourChunkFlags.getFlag(currentX + 1, currentZ - 1)) { // Straight?
                            currentX++;
                        } else { // Left turn
                            linelist.add(new int[]{currentX + 1, currentZ}); // Finish line
                            direction = Direction.ZMINUS;
                            currentX++;
                            currentZ--;
                        }
                        break;
                    case ZPLUS: // Segment in Z+ direction
                        if (!ourChunkFlags.getFlag(currentX, currentZ + 1)) { // Right turn?
                            linelist.add(new int[]{currentX + 1, currentZ + 1}); // Finish line
                            direction = Direction.XMINUS; // Change direction
                        } else if (!ourChunkFlags.getFlag(currentX + 1, currentZ + 1)) { // Straight?
                            currentZ++;
                        } else { // Left turn
                            linelist.add(new int[]{currentX + 1, currentZ + 1}); // Finish line
                            direction = Direction.XPLUS;
                            currentX++;
                            currentZ++;
                        }
                        break;
                    case XMINUS: // Segment in X- direction
                        if (!ourChunkFlags.getFlag(currentX - 1, currentZ)) { // Right turn?
                            linelist.add(new int[]{currentX, currentZ + 1}); // Finish line
                            direction = Direction.ZMINUS; // Change direction
                        } else if (!ourChunkFlags.getFlag(currentX - 1, currentZ + 1)) { // Straight?
                            currentX--;
                        } else { // Left turn
                            linelist.add(new int[]{currentX, currentZ + 1}); // Finish line
                            direction = Direction.ZPLUS;
                            currentX--;
                            currentZ++;
                        }
                        break;
                    case ZMINUS: // Segment in Z- direction
                        if (!ourChunkFlags.getFlag(currentX, currentZ - 1)) { // Right turn?
                            linelist.add(new int[]{currentX, currentZ}); // Finish line
                            direction = Direction.XPLUS; // Change direction
                        } else if (!ourChunkFlags.getFlag(currentX - 1, currentZ - 1)) { // Straight?
                            currentZ--;
                        } else { // Left turn
                            linelist.add(new int[]{currentX, currentZ}); // Finish line
                            direction = Direction.XMINUS;
                            currentX--;
                            currentZ--;
                        }
                        break;
                }
            }

            int sz = linelist.size();
            double[] x = new double[sz];
            double[] z = new double[sz];
            for (int i = 0; i < sz; i++) {
                int[] line = linelist.get(i);
                x[i] = (double) line[0] * (double) BLOCKS_PER_CHUNK;
                z[i] = (double) line[1] * (double) BLOCKS_PER_CHUNK;
            }

            // Build information for specific area
            String markerId = FACTIONS_ + world + "__" + factionId + "__" + markerIndex;

            TempAreaMarker temp = new TempAreaMarker();
            temp.label = factionTag.get(factionId);
            temp.world = world;
            temp.x = x;
            temp.z = z;
            temp.description = description;

            temp.lineColor = style.getLineColor();
            temp.lineOpacity = style.getLineOpacity();
            temp.lineWeight = style.getLineWeight();

            temp.fillColor = style.getFillColor();
            temp.fillOpacity = style.getFillOpacity();

            temp.boost = style.getBoost();

            ret.put(markerId, temp);

            markerIndex++;
        }

        return ret;
    }

    // Thread Safe: NO
    public void updateAreas(Map<String, TempAreaMarker> areas) {
        // Map Current
        Map<String, AreaMarker> markers = new HashMap<>();
        for (AreaMarker marker : this.markerset.getAreaMarkers()) {
            markers.put(marker.getMarkerID(), marker);
        }

        // Loop New
        for (Entry<String, TempAreaMarker> entry : areas.entrySet()) {
            String markerId = entry.getKey();
            TempAreaMarker temp = entry.getValue();

            // Get Creative
            // NOTE: I remove from the map created just in the beginning of this method.
            // NOTE: That way what is left at the end will be outdated markers to remove.
            AreaMarker marker = markers.remove(markerId);
            if (marker == null) {
                marker = temp.create(this.markerset, markerId);
                if (marker == null) {
                    severe("Could not get/create the area marker " + markerId);
                }
            } else {
                temp.update(marker);
            }
        }

        // Only old/outdated should now be left. Delete them.
        for (AreaMarker marker : markers.values()) {
            marker.deleteMarker();
        }
    }

    // -------------------------------------------- //
    // UPDATE: PLAYERSET
    // -------------------------------------------- //

    // Thread Safe / Asynchronous: Yes
    public String createPlayersetId(Faction faction) {
        if (faction == null) {
            return null;
        }
        if (faction.isWilderness()) {
            return null;
        }
        String factionId = faction.getId();
        if (factionId == null) {
            return null;
        }
        return FACTIONS_PLAYERSET_ + factionId;
    }

    // Thread Safe / Asynchronous: Yes
    public Set<String> createPlayerset(Faction faction) {
        if (faction == null) {
            return null;
        }
        if (faction.isWilderness()) {
            return null;
        }

        Set<String> ret = new HashSet<>();

        for (FPlayer fplayer : faction.getFPlayers()) {
            // NOTE: We add both UUID and name. This might be a good idea for future proofing.
            ret.add(fplayer.getId());
            ret.add(fplayer.getName());
        }

        return ret;
    }

    // Thread Safe / Asynchronous: Yes
    public Map<String, Set<String>> createPlayersets() {
        if (!dynmapConf.dynmap().isVisibilityByFaction()) {
            return null;
        }

        Map<String, Set<String>> ret = new HashMap<>();

        for (Faction faction : Factions.getInstance().getAllFactions()) {
            String playersetId = createPlayersetId(faction);
            if (playersetId == null) {
                continue;
            }
            Set<String> playerIds = createPlayerset(faction);
            if (playerIds == null) {
                continue;
            }
            ret.put(playersetId, playerIds);
        }

        return ret;
    }

    // Thread Safe / Asynchronous: No
    public void updatePlayersets(Map<String, Set<String>> playersets) {
        if (playersets == null) {
            return;
        }

        // Remove
        for (PlayerSet set : this.markerApi.getPlayerSets()) {
            if (!set.getSetID().startsWith(FACTIONS_PLAYERSET_)) {
                continue;
            }

            // (Null means remove all)
            if (playersets.containsKey(set.getSetID())) {
                continue;
            }

            set.deleteSet();
        }

        // Add / Update
        for (Entry<String, Set<String>> entry : playersets.entrySet()) {
            // Extract from Entry
            String setId = entry.getKey();
            Set<String> playerIds = entry.getValue();

            // Get Creatively
            PlayerSet set = this.markerApi.getPlayerSet(setId);
            if (set == null) {
                set = this.markerApi.createPlayerSet(setId, // id
                        true, // symmetric
                        playerIds, // players
                        false // persistent
                );
            }
            if (set == null) {
                severe("Could not get/create the player set " + setId);
                continue;
            }

            // Set Content
            set.setPlayers(playerIds);
        }
    }

    // -------------------------------------------- //
    // UTIL & SHARED
    // -------------------------------------------- //

    // Thread Safe / Asynchronous: Yes
    private String getDescription(Faction faction) {
        String ret = "<div class=\"regioninfo\">" + dynmapConf.dynmap().getDescription() + "</div>";

        // Name
        String name = faction.getTag();
        name = ChatColor.stripColor(name);
        name = escapeHtml(name);
        ret = ret.replace("%name%", name);

        // Description
        String description = faction.getDescription();
        description = ChatColor.stripColor(description);
        description = escapeHtml(description);
        ret = ret.replace("%description%", description);

        // Money

        String money = "unavailable";
        if (FactionsPlugin.getInstance().conf().economy().isBankEnabled() && dynmapConf.dynmap().isDescriptionMoney()) {
            money = String.format("%.2f", Econ.getBalance(faction));
        }
        ret = ret.replace("%money%", money);


        // Players
        Set<FPlayer> playersList = faction.getFPlayers();
        String playersCount = String.valueOf(playersList.size());
        String players = getHtmlPlayerString(playersList);

        FPlayer playersLeaderObject = faction.getFPlayerAdmin();
        String playersLeader = getHtmlPlayerName(playersLeaderObject);

        List<FPlayer> playersAdminsList = faction.getFPlayersWhereRole(Role.ADMIN);
        String playersAdminsCount = String.valueOf(playersAdminsList.size());
        String playersAdmins = getHtmlPlayerString(playersAdminsList);

        List<FPlayer> playersModeratorsList = faction.getFPlayersWhereRole(Role.MODERATOR);
        String playersModeratorsCount = String.valueOf(playersModeratorsList.size());
        String playersModerators = getHtmlPlayerString(playersModeratorsList);


        List<FPlayer> playersNormalsList = faction.getFPlayersWhereRole(Role.NORMAL);
        String playersNormalsCount = String.valueOf(playersNormalsList.size());
        String playersNormals = getHtmlPlayerString(playersNormalsList);


        ret = ret.replace("%players%", players);
        ret = ret.replace("%players.count%", playersCount);
        ret = ret.replace("%players.leader%", playersLeader);
        ret = ret.replace("%players.admins%", playersAdmins);
        ret = ret.replace("%players.admins.count%", playersAdminsCount);
        ret = ret.replace("%players.moderators%", playersModerators);
        ret = ret.replace("%players.moderators.count%", playersModeratorsCount);
        ret = ret.replace("%players.normals%", playersNormals);
        ret = ret.replace("%players.normals.count%", playersNormalsCount);

        return ret;
    }

    public static String getHtmlPlayerString(Collection<FPlayer> playersOfficersList) {
        StringBuilder ret = new StringBuilder();
        for (FPlayer fplayer : playersOfficersList) {
            if (ret.length() > 0) {
                ret.append(", ");
            }
            ret.append(getHtmlPlayerName(fplayer));
        }
        return ret.toString();
    }

    public static String getHtmlPlayerName(FPlayer fplayer) {
        if (fplayer == null) {
            return "none";
        }
        return escapeHtml(fplayer.getName());
    }

    public static String escapeHtml(String string) {
        if (string == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(Math.max(16, string.length()));
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c > 127 || c == '"' || c == '<' || c == '>' || c == '&') {
                out.append("&#");
                out.append((int) c);
                out.append(';');
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    // Thread Safe / Asynchronous: Yes
    private boolean isVisible(String factionId, String factionTag, String world) {
        Set<String> visible = dynmapConf.dynmap().getVisibleFactions();
        Set<String> hidden = dynmapConf.dynmap().getHiddenFactions();

        if (!visible.isEmpty() && !visible.contains(factionId) && !visible.contains(factionTag) && !visible.contains("world:" + world)) {
            return false;
        }

        return !hidden.contains(factionId) && !hidden.contains(factionTag) && !hidden.contains("world:" + world);
    }

    // Thread Safe / Asynchronous: Yes
    public DynmapStyle getStyle(Faction faction) {
        DynmapStyle ret;

        ret = dynmapConf.dynmap().getFactionStyles().get(faction.getId());
        if (ret != null) {
            return ret;
        }

        ret = dynmapConf.dynmap().getFactionStyles().get(faction.getTag());
        if (ret != null) {
            return ret;
        }

        return DynmapStyle.getEmpty();
    }

    // Thread Safe / Asynchronous: Yes
    static void severe(String msg) {
        String message = DYNMAP_INTEGRATION + ChatColor.RED.toString() + msg;
        FactionsPlugin.getInstance().getLogger().severe(message);
    }

    enum Direction {
        XPLUS, ZPLUS, XMINUS, ZMINUS
    }

    // Find all contiguous blocks, set in target and clear in source
    private int floodFillTarget(TileFlags source, TileFlags destination, int x, int y) {
        int cnt = 0;
        ArrayDeque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{x, y});

        while (!stack.isEmpty()) {
            int[] nxt = stack.pop();
            x = nxt[0];
            y = nxt[1];
            if (source.getFlag(x, y)) { // Set in src
                source.setFlag(x, y, false); // Clear source
                destination.setFlag(x, y, true); // Set in destination
                cnt++;
                if (source.getFlag(x + 1, y)) {
                    stack.push(new int[]{x + 1, y});
                }
                if (source.getFlag(x - 1, y)) {
                    stack.push(new int[]{x - 1, y});
                }
                if (source.getFlag(x, y + 1)) {
                    stack.push(new int[]{x, y + 1});
                }
                if (source.getFlag(x, y - 1)) {
                    stack.push(new int[]{x, y - 1});
                }
            }
        }
        return cnt;
    }
}
