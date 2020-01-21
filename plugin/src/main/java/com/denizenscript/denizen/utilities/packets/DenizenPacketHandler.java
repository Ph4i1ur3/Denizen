package com.denizenscript.denizen.utilities.packets;

import com.denizenscript.denizen.events.player.PlayerHoldsShieldEvent;
import com.denizenscript.denizen.utilities.implementation.DenizenCoreImplementation;
import com.denizenscript.denizen.nms.interfaces.packets.*;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.events.player.PlayerReceivesMessageScriptEvent;
import com.denizenscript.denizen.events.player.PlayerSteersEntityScriptEvent;
import com.denizenscript.denizen.events.player.ResourcePackStatusScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.scripts.commands.player.GlowCommand;
import com.denizenscript.denizen.scripts.commands.server.ExecuteCommand;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class DenizenPacketHandler {

    public static HashSet<UUID> forceNoclip = new HashSet<>();

    public void receivePacket(final Player player, final PacketInResourcePackStatus resourcePackStatus) {
        Bukkit.getScheduler().runTask(DenizenAPI.getCurrentInstance(), new Runnable() {
            @Override
            public void run() {
                ResourcePackStatusScriptEvent event = ResourcePackStatusScriptEvent.instance;
                event.status = new ElementTag(resourcePackStatus.getStatus());
                event.player = PlayerTag.mirrorBukkitPlayer(player);
                event.fire();
            }
        });
    }

    public boolean receivePacket(final Player player, final PacketInSteerVehicle steerVehicle) {
        if (PlayerSteersEntityScriptEvent.instance.enabled) {
            Future<Boolean> future = Bukkit.getScheduler().callSyncMethod(DenizenAPI.getCurrentInstance(),
                    new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            PlayerSteersEntityScriptEvent event = PlayerSteersEntityScriptEvent.instance;
                            event.player = PlayerTag.mirrorBukkitPlayer(player);
                            event.entity = player.isInsideVehicle() ? new EntityTag(player.getVehicle()) : null;
                            event.sideways = new ElementTag(steerVehicle.getLeftwardInput());
                            event.forward = new ElementTag(steerVehicle.getForwardInput());
                            event.jump = new ElementTag(steerVehicle.getJumpInput());
                            event.dismount = new ElementTag(steerVehicle.getDismountInput());
                            event.cancelled = false;
                            event.fire();
                            return event.cancelled;
                        }
                    }
            );
            try {
                return future.get();
            }
            catch (Exception e) {
                Debug.echoError(e);
            }
        }
        return false;
    }

    public static boolean isHoldingShield(Player player) {
        return player.getEquipment().getItemInMainHand().getType() == Material.SHIELD
            || player.getEquipment().getItemInOffHand().getType() == Material.SHIELD;
    }

    public void receivePlacePacket(final Player player) {
        if (isHoldingShield(player)) {
            Bukkit.getScheduler().runTask(DenizenAPI.getCurrentInstance(), () -> {
                PlayerHoldsShieldEvent.signalDidRaise(player);
            });
        }
    }

    public void receiveDigPacket(final Player player) {
        if (isHoldingShield(player)) {
            Bukkit.getScheduler().runTask(DenizenAPI.getCurrentInstance(), () -> {
                PlayerHoldsShieldEvent.signalDidLower(player);
            });
        }
    }

    public boolean sendPacket(final Player player, final PacketOutChat chat) {
        if (ExecuteCommand.silencedPlayers.contains(player.getUniqueId())) {
            return true;
        }
        final PlayerReceivesMessageScriptEvent event = PlayerReceivesMessageScriptEvent.instance;
        if (event.loaded) {
            Callable<Boolean> eventCall = new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    int pos = chat.getPosition();
                    if (pos != 2) {
                        event.message = new ElementTag(chat.getMessage());
                        event.rawJson = new ElementTag(chat.getRawJson());
                        event.system = new ElementTag(pos == 1);
                        event.messageModified = false;
                        event.rawJsonModified = false;
                        event.player = PlayerTag.mirrorBukkitPlayer(player);
                        event.cancelled = false;
                        event.fire();
                        if (event.messageModified) {
                            chat.setMessage(event.message.asString());
                        }
                        else if (event.rawJsonModified) {
                            chat.setRawJson(event.rawJson.asString());
                        }
                        return event.cancelled;
                    }
                    return false;
                }
            };
            try {
                if (DenizenCoreImplementation.isSafeThread()) {
                    return eventCall.call();
                }
                else {
                    FutureTask<Boolean> futureTask = new FutureTask<>(eventCall);
                    Bukkit.getScheduler().runTask(DenizenAPI.getCurrentInstance(), futureTask);
                    return futureTask.get();
                }
            }
            catch (Exception e) {
                Debug.echoError(e);
                return false;
            }
        }
        return false;
    }

    public boolean sendPacket(Player player, PacketOutEntityMetadata entityMetadata) {
        HashSet<UUID> players = GlowCommand.glowViewers.get(entityMetadata.getEntityId());
        return players != null && entityMetadata.checkForGlow() && !players.contains(player.getUniqueId());
    }
}
