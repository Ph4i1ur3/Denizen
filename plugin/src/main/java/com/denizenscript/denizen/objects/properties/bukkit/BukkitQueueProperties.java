package com.denizenscript.denizen.objects.properties.bukkit;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.objects.dNPC;
import com.denizenscript.denizen.objects.dPlayer;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.queues.ScriptQueue;
import com.denizenscript.denizencore.tags.Attribute;

public class BukkitQueueProperties implements Property {

    public static boolean describes(ObjectTag script) {
        return script instanceof ScriptQueue;
    }

    public static BukkitQueueProperties getFrom(ObjectTag queue) {
        if (!describes(queue)) {
            return null;
        }
        else {
            return new BukkitQueueProperties((ScriptQueue) queue);
        }
    }

    public static final String[] handledTags = new String[] {
            "player", "npc"
    };

    public static final String[] handledMechs = new String[] {
            "linked_player", "linked_npc"
    };

    private BukkitQueueProperties(ScriptQueue queue) {
        this.queue = queue;
    }

    ScriptQueue queue;

    @Override
    public String getAttribute(Attribute attribute) {

        // <--[tag]
        // @attribute <q@queue.npc>
        // @returns dNPC
        // @mechanism ScriptQueue.linked_npc
        // @description
        // Returns the dNPC linked to a queue.
        // -->
        if (attribute.startsWith("npc")) {
            dNPC npc = null;
            if (queue.getLastEntryExecuted() != null) {
                npc = ((BukkitScriptEntryData) queue.getLastEntryExecuted().entryData).getNPC();
            }
            else if (queue.getEntries().size() > 0) {
                npc = ((BukkitScriptEntryData) queue.getEntries().get(0).entryData).getNPC();
            }
            else {
                Debug.echoError(queue, "Can't determine a linked NPC.");
            }
            if (npc == null) {
                return null;
            }
            else {
                return npc.getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <q@queue.player>
        // @returns dPlayer
        // @mechanism ScriptQueue.linked_player
        // @description
        // Returns the dPlayer linked to a queue.
        // -->
        if (attribute.startsWith("player")) {
            dPlayer player = null;
            if (queue.getLastEntryExecuted() != null) {
                player = ((BukkitScriptEntryData) queue.getLastEntryExecuted().entryData).getPlayer();
            }
            else if (queue.getEntries().size() > 0) {
                player = ((BukkitScriptEntryData) queue.getEntries().get(0).entryData).getPlayer();
            }
            else {
                Debug.echoError(queue, "Can't determine a linked player.");
            }
            if (player == null) {
                return null;
            }
            else {
                return player.getAttribute(attribute.fulfill(1));
            }
        }
        return null;
    }

    @Override
    public String getPropertyString() {
        return null;
    }

    @Override
    public String getPropertyId() {
        return "BukkitQueueProperties";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ScriptQueue
        // @name linked_player
        // @input dPlayer
        // @description
        // Sets the linked player for the remainder of the queue.
        // @tags
        // <q@queue.player>
        // -->
        if (mechanism.matches("linked_player") && mechanism.requireObject(dPlayer.class)) {
            dPlayer player = mechanism.valueAsType(dPlayer.class);
            for (ScriptEntry entry : queue.getEntries()) {
                BukkitScriptEntryData data = (BukkitScriptEntryData) entry.entryData;
                data.setPlayer(player);
            }
        }

        // <--[mechanism]
        // @object ScriptQueue
        // @name linked_npc
        // @input dNPC
        // @description
        // Sets the linked NPC for the remainder of the queue.
        // @tags
        // <q@queue.npc>
        // -->
        if (mechanism.matches("linked_npc") && mechanism.requireObject(dNPC.class)) {
            dNPC npc = mechanism.valueAsType(dNPC.class);
            for (ScriptEntry entry : queue.getEntries()) {
                BukkitScriptEntryData data = (BukkitScriptEntryData) entry.entryData;
                data.setNPC(npc);
            }
        }
    }
}
