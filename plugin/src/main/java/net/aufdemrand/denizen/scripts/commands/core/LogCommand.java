package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.DebugLog;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.tags.TagManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;

public class LogCommand extends AbstractCommand {

    // <--[command]
    // @Name Log
    // @Syntax log [<text>] (type:{info}/severe/warning/fine/finer/finest/none/clear) [file:<name>]
    // @Required 2
    // @Short Logs some debugging info to a file.
    // @Group core
    //
    // @Description
    // This is a quick and simple way to store debugging information for admins to read.
    // You just input a file name and some text, and it will store that information in the file
    // with a date/time stamp and the chosen type ('INFO' by default). If you don't want the
    // date/time stamp and type, you can set the type to 'none' and it will only add the
    // message text.
    // Regardless of type, each usage of the log command will add a new line to the file, you can't
    // just keep adding to one line.
    // You might choose to use this to record some important things, for example, every time a player
    // uses a dangerous command you might log the player's name and their location, so you'll know
    // who to blame if you find something damaged.
    // Remember that the file location is inside the server's primary folder. You most likely want to prefix
    // file names with a folder name, For example: 'file:logs/security.log'
    //
    // Warning: Remember that file operations are dangerous! A typo in the filename could ruin your server.
    // It's recommended you use this command minimally.
    //
    // @Tags
    // None
    //
    // @Usage
    // Use to log some information to a file.
    // - log "Security breach on level 3!" type:severe file:securitylog.txt
    //
    // @Usage
    // Use to log a player's name and location when they did something dangerous.
    // - log "<player.name> used the '/EXPLODE' command at <player.location.simple>!" type:warning file:security.log
    //
    // @Usage
    // Use to write information directly to a file.
    // - log "This won't have a date or type" type:none file:example.log
    //
    // @Usage
    // Use to clear a log file and write some text at the start.
    // - log "// Log File Generated by my Denizen script, do not edit!" type:clear file:myfile.log
    //
    // @Usage
    // Use to clear a log file entirely.
    // - log "" type:clear file:myfile.log
    // -->

    public enum Type {SEVERE, INFO, WARNING, FINE, FINER, FINEST, NONE, CLEAR}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {
            if (!scriptEntry.hasObject("type")
                    && arg.matchesPrefix("type")
                    && arg.matchesEnum(Type.values())) {
                scriptEntry.addObject("type", arg.asElement());
            }
            else if (!scriptEntry.hasObject("file")
                    && arg.matchesPrefix("file")) {
                scriptEntry.addObject("file", arg.asElement());
            }
            else if (!scriptEntry.hasObject("message")) {
                scriptEntry.addObject("message", new Element(arg.raw_value));
            }
            else {
                arg.reportUnhandled();
            }
        }

        if (!scriptEntry.hasObject("message")) {
            throw new InvalidArgumentsException("Must specify a message.");
        }

        if (!scriptEntry.hasObject("file")) {
            throw new InvalidArgumentsException("Must specify a file.");
        }

        if (!scriptEntry.hasObject("type")) {
            scriptEntry.addObject("type", new Element("INFO"));
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        if (!Settings.allowLogging()) {
            dB.echoError("Logging disabled by administrator.");
            return;
        }
        Element message = scriptEntry.getElement("message");
        Element fileName = scriptEntry.getElement("file");
        Element typeElement = scriptEntry.getElement("type");

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(),
                    message.debug() + fileName.debug() + typeElement.debug());

        }

        Type type = Type.valueOf(typeElement.asString().toUpperCase());

        String directory = URLDecoder.decode(System.getProperty("user.dir"));
        File file = new File(directory, fileName.asString());

        file.getParentFile().mkdirs();
        if (!Utilities.canWriteToFile(file)) {
            dB.echoError(scriptEntry.getResidingQueue(), "Cannot log into that file!");
            return;
        }

        String output = TagManager.cleanOutputFully(message.asString());

        file.getParentFile().mkdirs();
        if (type == Type.NONE) {
            try {
                FileWriter fw = new FileWriter(file, true);
                fw.write(output + "\n");
                fw.close();
            }
            catch (IOException e) {
                dB.echoError(scriptEntry.getResidingQueue(), "Error logging to file...");
                dB.echoError(scriptEntry.getResidingQueue(), e);
            }
            return;
        }
        else if (type == Type.CLEAR) {
            try {
                FileWriter fw = new FileWriter(file);
                if (output.length() > 0) {
                    fw.write(output + "\n");
                }
                fw.close();
            }
            catch (IOException e) {
                dB.echoError(scriptEntry.getResidingQueue(), "Error logging to file...");
                dB.echoError(scriptEntry.getResidingQueue(), e);
            }
            return;
        }

        DebugLog log = new DebugLog("Denizen-ScriptLog-" + fileName, file.getAbsolutePath());

        switch (type) {
            case SEVERE:
                log.severe(output);
                break;

            case INFO:
                log.info(output);
                break;

            case WARNING:
                log.warning(output);
                break;

            case FINE:
                log.fine(output);
                break;

            case FINER:
                log.finer(output);
                break;

            case FINEST:
                log.finest(output);
        }

        log.close();
    }
}
