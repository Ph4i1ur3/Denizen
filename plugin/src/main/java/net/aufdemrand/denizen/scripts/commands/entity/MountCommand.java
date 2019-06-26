package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.Conversion;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.entity.Position;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;

import java.util.List;

public class MountCommand extends AbstractCommand {

    // <--[command]
    // @Name Mount
    // @Syntax mount (cancel) [<entity>|...] (<location>)
    // @Required 0
    // @Short Mounts one entity onto another.
    // @Group entity
    //
    // @Description
    // Mounts an entity onto another as though in a vehicle. Can be used to force a player into a vehicle or to
    // mount an entity onto another entity. e.g. a player onto an npc. If the entity(s) don't exist they will be
    // spawned. Accepts a location, which the entities will be teleported to on mounting.
    //
    // @Tags
    // <e@entity.vehicle>
    // <e@entity.inside_vehicle>
    // <entry[saveName].mounted_entities> returns a list of entities that were mounted.
    //
    // @Usage
    // Use to mount an NPC on top of a player.
    // - mount <npc>|<player>
    //
    // @Usage
    // Use to spawn a mutant pile of mobs.
    // - mount cow|pig|sheep|chicken
    //
    // @Usage
    // Use to place a diamond block above a player's head.
    // - mount falling_block,diamond_block|<player>
    //
    // @Usage
    // Use to force an entity in a vehicle.
    // - mount <player>|boat
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize necessary fields

        List<dEntity> entities = null;

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("cancel")
                    && arg.matches("cancel")) {

                scriptEntry.addObject("cancel", "");
            }
            else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class)) {
                // Location arg
                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dEntity.class)) {
                // Entity arg
                entities = arg.asType(dList.class).filter(dEntity.class, scriptEntry);
                scriptEntry.addObject("entities", entities);
            }
            else {
                arg.reportUnhandled();
            }
        }

        if (!scriptEntry.hasObject("entities")) {
            throw new InvalidArgumentsException("Must specify entity/entities!");
        }

        if (!scriptEntry.hasObject("location")) {
            if (entities != null) {
                for (int i = entities.size() - 1; i >= 0; i--) {
                    if (entities.get(i).isSpawned()) {
                        scriptEntry.defaultObject("location", entities.get(i).getLocation());
                        break;
                    }
                }
            }
            scriptEntry.defaultObject("location",
                    ((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer() ? ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getLocation() : null,
                    ((BukkitScriptEntryData) scriptEntry.entryData).hasNPC() ? ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getLocation() : null);
        }

        if (!scriptEntry.hasObject("location")) {
            throw new InvalidArgumentsException("Must specify a location!");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) {
        // Get objects

        dLocation location = (dLocation) scriptEntry.getObject("location");
        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        boolean cancel = scriptEntry.hasObject("cancel");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(), (cancel ? aH.debugObj("cancel", cancel) : "") +
                    aH.debugObj("location", location) +
                    aH.debugObj("entities", entities.toString()));
        }

        // Mount or dismount all of the entities
        if (!cancel) {

            // Go through all the entities, spawning/teleporting them
            for (dEntity entity : entities) {
                entity.spawnAt(location);
            }

            Position.mount(Conversion.convertEntities(entities));
        }
        else {
            Position.dismount(Conversion.convertEntities(entities));
        }

        dList entityList = new dList();
        entityList.addObjects((List) entities);
        scriptEntry.addObject("mounted_entities", entityList);
    }
}
