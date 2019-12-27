package com.denizenscript.denizen.scripts.commands.server;

import com.denizenscript.denizen.utilities.command.scripted.DenizenCommandSender;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShellCommand  extends AbstractCommand {

    // <--[command]
    // @Name Shell
    // @Syntax shell <ShellScriptFileName> <Server>
    // @Required 2
    // @Short Executes a shell script on the host machine
    // @Group server
    //
    // @Description
    // Allows the execution of a shell script on the host machine
    // This command is ONLY available on the relay server.
    //
    // @Usage
    // Use to execute the save-all command as the server.
    // - shell git_update Telix
    //
    // -->


    enum Type {AS_SERVER, AS_NPC, AS_PLAYER, AS_OP}

    public DenizenCommandSender dcs = new DenizenCommandSender();
    public static final List<UUID> silencedPlayers = new ArrayList<>();

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Parse arguments
        for (Argument arg : scriptEntry.getProcessedArgs()) {

            if (!scriptEntry.hasObject("shellscript")) {
                scriptEntry.addObject("shellscript", new ElementTag(arg.raw_value));
            }
            else if (!scriptEntry.hasObject("server")) {
                scriptEntry.addObject("server", new ElementTag(arg.raw_value));
            }
            else {
                arg.reportUnhandled();
            }
        }

        if (!scriptEntry.hasObject("shellscript")) {
            throw new InvalidArgumentsException("Missing shell script file name!");
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        ElementTag file = scriptEntry.getElement("shellscript");
        ElementTag server = scriptEntry.getElement("server");

        String command = "/home/minecraft/scripts/" + file.asString() + ".sh";
        String servername = server.asString();

            try {
                Process p = Runtime.getRuntime().exec(command + " " + servername);
            } catch (IOException e) {
                Debug.echoError(scriptEntry.getResidingQueue(), "Exception while executing shell script.");
                Debug.echoError(scriptEntry.getResidingQueue(), e);
            }
        }
    }

