package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.dNPC;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.trait.trait.Spawned;

import java.util.Arrays;
import java.util.List;

public class DespawnCommand extends AbstractCommand {

    // <--[command]
    // @Name Despawn
    // @Syntax despawn (<npc>|...)
    // @Plugin Citizens
    // @Required 0
    // @Short Temporarily despawns the linked NPC or a list of NPCs.
    // @Group npc
    //
    // @Description
    // This command will temporarily despawn either the linked NPC or
    // a list of other NPCs. Despawning means they are no longer visible
    // or interactable, but they still exist and can be respawned.
    //
    // @Tags
    // <n@npc.is_spawned>
    //
    // @Usage
    // Use to despawn the linked NPC.
    // - despawn
    //
    // @Usage
    // Use to despawn several NPCs.
    // - despawn <npc>|<player.selected_npc>|n@32
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("npcs")
                    && arg.matchesArgumentList(dNPC.class)) {
                scriptEntry.addObject("npcs", arg.asType(ListTag.class).filter(dNPC.class, scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }
        }

        if (!scriptEntry.hasObject("npcs")) {
            if (Utilities.entryHasNPC(scriptEntry)) {
                scriptEntry.addObject("npcs", Arrays.asList(Utilities.getEntryNPC(scriptEntry)));
            }
            else {
                throw new InvalidArgumentsException("Must specify a valid list of NPCs!");
            }
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) {

        // Get objects
        List<dNPC> npcs = (List<dNPC>) scriptEntry.getObject("npcs");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(),
                    ArgumentHelper.debugObj("NPCs", npcs.toString()));
        }

        for (dNPC npc : npcs) {
            if (npc.isSpawned()) {
                if (npc.getCitizen().hasTrait(Spawned.class)) {
                    npc.getCitizen().getTrait(Spawned.class).setSpawned(false);
                }
                npc.getCitizen().despawn(DespawnReason.PLUGIN);
            }
        }
    }
}
