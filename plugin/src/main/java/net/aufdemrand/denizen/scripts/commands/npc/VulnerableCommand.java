package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.api.npc.NPC;

public class VulnerableCommand extends AbstractCommand {

    // <--[command]
    // @Name Vulnerable
    // @Syntax vulnerable (state:{true}/false/toggle)
    // @Required 0
    // @Plugin Citizens
    // @Short Sets whether an NPC is vulnerable.
    // @Group npc
    //
    // @Description
    // Toggles whether an NPC can be hurt or not.
    //
    // @Tags
    // <n@npc.invulnerable>
    //
    // @Usage
    // Makes an NPC vulnerable.
    // - vulnerable state:true
    //
    // @Usage
    // Makes an NPC vulnerable if it is not, and invulnerable if it is.
    // - vulnerable
    //
    // -->

    enum Toggle {TRUE, FALSE, TOGGLE}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize fields
        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {
            if (!scriptEntry.hasObject("action") && arg.matchesEnum(Toggle.values())) {
                scriptEntry.addObject("action", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }
        }

        scriptEntry.defaultObject("action", new Element("toggle"));
        if (!((BukkitScriptEntryData) scriptEntry.entryData).hasNPC()) {
            throw new InvalidArgumentsException("This command requires a linked NPC!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        // Fetch objects
        Element action = scriptEntry.getElement("action");

        BukkitScriptEntryData entryData = (BukkitScriptEntryData) scriptEntry.entryData;

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(), entryData.getNPC().debug() + action.debug());
        }

        NPC npc = entryData.getNPC().getCitizen();
        Toggle toggle = Toggle.valueOf(action.asString().toUpperCase());

        npc.setProtected(!(toggle == Toggle.TOGGLE ? npc.isProtected() : action.asBoolean()));
    }
}
