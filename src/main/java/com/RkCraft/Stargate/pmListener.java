package com.RkCraft.Stargate;

import org.bukkit.plugin.messaging.*;
import org.bukkit.entity.*;
import java.io.*;
import java.util.logging.*;
import org.bukkit.event.player.*;

public class pmListener implements PluginMessageListener
{
    public void onPluginMessageReceived(final String channel, final Player unused, final byte[] message) {
        if (!Stargate.enableBungee || !channel.equals("BungeeCord")) {
            return;
        }
        String inChannel;
        byte[] data;
        try {
            final DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
            inChannel = in.readUTF();
            final short len = in.readShort();
            data = new byte[len];
            in.readFully(data);
        }
        catch (IOException ex) {
            Stargate.log.severe("[Stargate] Error receiving BungeeCord message");
            return;
        }
        if (!inChannel.equals("SGBungee")) {
            return;
        }
        final String msg = new String(data);
        final String[] parts = msg.split("#@#");
        final String playerName = parts[0];
        final String destination = parts[1];
        final Player player = Stargate.server.getPlayer(playerName);
        if (player == null) {
            Stargate.bungeeQueue.put(playerName.toLowerCase(), destination);
        }
        else {
            final Portal dest = Portal.getBungeeGate(destination);
            if (dest == null) {
                Stargate.log.log(Level.INFO, "[Stargate] Bungee gate {0} does not exist", destination);
                return;
            }
            dest.teleport(player, dest, null);
        }
    }
}
