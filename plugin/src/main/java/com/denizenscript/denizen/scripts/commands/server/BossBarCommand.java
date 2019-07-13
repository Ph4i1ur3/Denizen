package com.denizenscript.denizen.scripts.commands.server;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.objects.dPlayer;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BossBarCommand extends AbstractCommand {

    // <--[command]
    // @Name BossBar
    // @Syntax bossbar ({create}/update/remove) [<id>] (players:<player>|...) (title:<title>) (progress:<#.#>) (color:<color>) (style:<style>) (flags:<flag>|...)
    // @Required 1
    // @Short Shows players a boss bar.
    // @Group server
    //
    // @Description
    // Displays a boss bar at the top of the screen of the specified player(s). You can also update the
    // values and remove the bar.
    //
    // Requires an ID. Progress must be between 0 and 1.
    //
    // Valid colors: BLUE, GREEN, PINK, PURPLE, RED, WHITE, YELLOW.
    // Valid styles: SEGMENTED_10, SEGMENTED_12, SEGMENTED_20, SEGMENTED_6, SOLID.
    // Valid flags: CREATE_FOG, DARKEN_SKY, PLAY_BOSS_MUSIC.
    //
    // @Tags
    // <server.current_bossbars>
    //
    // @Usage
    // Shows a message to all online players.
    // - bossbar MyMessageID players:<server.list_online_players> "title:HI GUYS" color:red
    //
    // @Usage
    // Update the boss bar's color and progress.
    // - bossbar update MyMessageID color:blue progress:0.2
    //
    // @Usage
    // Add more players to the boss bar.
    // - bossbar update MyMessageID players:<server.flag[new_players]>
    //
    // @Usage
    // Remove a player from the boss bar.
    // - bossbar remove MyMessageID players:<server.match_player[BlackCoyote]>
    //
    // @Usage
    // Delete the boss bar.
    // - bossbar remove MyMessageID
    // -->

    private enum Action {
        CREATE, UPDATE, REMOVE
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("title")
                    && arg.matchesPrefix("title", "t")) {
                scriptEntry.addObject("title", arg.asElement());
            }
            else if (!scriptEntry.hasObject("progress")
                    && arg.matchesPrefix("progress", "health", "p", "h")
                    && arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Double)) {
                scriptEntry.addObject("progress", arg.asElement());
            }
            else if (!scriptEntry.hasObject("color")
                    && arg.matchesPrefix("color", "c")
                    && arg.matchesEnum(BarColor.values())) {
                scriptEntry.addObject("color", arg.asElement());
            }
            else if (!scriptEntry.hasObject("style")
                    && arg.matchesPrefix("style", "s")
                    && arg.matchesEnum(BarStyle.values())) {
                scriptEntry.addObject("style", arg.asElement());
            }
            else if (!scriptEntry.hasObject("flags")
                    && arg.matchesPrefix("flags", "flag", "f")
                    && arg.matchesEnumList(BarFlag.values())) {
                scriptEntry.addObject("flags", arg.asType(ListTag.class));
            }
            else if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(Action.values())) {
                scriptEntry.addObject("action", arg.asElement());
            }
            else if (!scriptEntry.hasObject("players")
                    && arg.matchesPrefix("players")
                    && arg.matchesArgumentList(dPlayer.class)) {
                scriptEntry.addObject("players", arg.asType(ListTag.class));
            }
            else if (!scriptEntry.hasObject("id")) {
                scriptEntry.addObject("id", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }
        }

        if (!scriptEntry.hasObject("id")) {
            throw new InvalidArgumentsException("Must specify an ID!");
        }

        if ((!scriptEntry.hasObject("action") || scriptEntry.getElement("action").asString().equalsIgnoreCase("CREATE"))
                && !scriptEntry.hasObject("players")) {
            BukkitScriptEntryData data = (BukkitScriptEntryData) scriptEntry.entryData;
            if (data.hasPlayer() && data.getPlayer().isOnline()) {
                scriptEntry.addObject("players", new ListTag(Collections.singleton(data.getPlayer().identify())));
            }
            else {
                throw new InvalidArgumentsException("Must specify valid player(s)!");
            }
        }

        scriptEntry.defaultObject("action", new ElementTag("CREATE"));
    }

    public final static Map<String, BossBar> bossBarMap = new HashMap<>();

    @Override
    public void execute(ScriptEntry scriptEntry) {

        ElementTag id = scriptEntry.getElement("id");
        ElementTag action = scriptEntry.getElement("action");
        ListTag players = scriptEntry.getdObject("players");
        ElementTag title = scriptEntry.getElement("title");
        ElementTag progress = scriptEntry.getElement("progress");
        ElementTag color = scriptEntry.getElement("color");
        ElementTag style = scriptEntry.getElement("style");
        ListTag flags = scriptEntry.getdObject("flags");

        if (scriptEntry.dbCallShouldDebug()) {

            Debug.report(scriptEntry, getName(), id.debug() + action.debug()
                    + (players != null ? players.debug() : "")
                    + (title != null ? title.debug() : "")
                    + (progress != null ? progress.debug() : "")
                    + (color != null ? color.debug() : "")
                    + (style != null ? style.debug() : "")
                    + (flags != null ? flags.debug() : ""));

        }

        String idString = CoreUtilities.toLowerCase(id.asString());

        switch (Action.valueOf(action.asString().toUpperCase())) {
            case CREATE:
                if (bossBarMap.containsKey(idString)) {
                    Debug.echoError("BossBar '" + idString + "' already exists!");
                    return;
                }
                String barTitle = title != null ? title.asString() : "";
                List<dPlayer> barPlayers = players.filter(dPlayer.class, scriptEntry);
                double barProgress = progress != null ? progress.asDouble() : 1D;
                BarColor barColor = color != null ? BarColor.valueOf(color.asString().toUpperCase()) : BarColor.WHITE;
                BarStyle barStyle = style != null ? BarStyle.valueOf(style.asString().toUpperCase()) : BarStyle.SOLID;
                BarFlag[] barFlags = new BarFlag[flags != null ? flags.size() : 0];
                if (flags != null) {
                    for (int i = 0; i < flags.size(); i++) {
                        barFlags[i] = (BarFlag.valueOf(flags.get(i).toUpperCase()));
                    }
                }
                BossBar bossBar = Bukkit.createBossBar(barTitle, barColor, barStyle, barFlags);
                bossBar.setProgress(barProgress);
                for (dPlayer player : barPlayers) {
                    if (!player.isOnline()) {
                        Debug.echoError("Player must be online to show a BossBar to them!");
                        continue;
                    }
                    bossBar.addPlayer(player.getPlayerEntity());
                }
                bossBar.setVisible(true);
                bossBarMap.put(idString, bossBar);
                break;

            case UPDATE:
                if (!bossBarMap.containsKey(idString)) {
                    Debug.echoError("BossBar '" + idString + "' does not exist!");
                    return;
                }
                BossBar bossBar1 = bossBarMap.get(idString);
                if (title != null) {
                    bossBar1.setTitle(title.asString());
                }
                if (progress != null) {
                    bossBar1.setProgress(progress.asDouble());
                }
                if (color != null) {
                    bossBar1.setColor(BarColor.valueOf(color.asString().toUpperCase()));
                }
                if (style != null) {
                    bossBar1.setStyle(BarStyle.valueOf(style.asString().toUpperCase()));
                }
                if (players != null) {
                    for (dPlayer player : players.filter(dPlayer.class, scriptEntry)) {
                        bossBar1.addPlayer(player.getPlayerEntity());
                    }
                }
                break;

            case REMOVE:
                if (!bossBarMap.containsKey(idString)) {
                    Debug.echoError("BossBar '" + idString + "' does not exist!");
                    return;
                }
                if (players != null) {
                    BossBar bar = bossBarMap.get(idString);
                    for (dPlayer player : players.filter(dPlayer.class, scriptEntry)) {
                        bar.removePlayer(player.getPlayerEntity());
                    }
                    break;
                }
                bossBarMap.get(idString).setVisible(false);
                bossBarMap.remove(idString);
                break;
        }
    }
}
