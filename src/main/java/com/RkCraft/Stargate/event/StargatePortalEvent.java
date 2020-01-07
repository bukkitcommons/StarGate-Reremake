package com.RkCraft.Stargate.event;

import org.bukkit.entity.*;
import com.RkCraft.Stargate.*;
import org.bukkit.*;
import org.bukkit.event.*;

public class StargatePortalEvent extends StargateEvent
{
    private final Player player;
    private final Portal destination;
    private Location exit;
    private static final HandlerList handlers;
    
    public HandlerList getHandlers() {
        return StargatePortalEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return StargatePortalEvent.handlers;
    }
    
    public StargatePortalEvent(final Player player, final Portal portal, final Portal dest, final Location exit) {
        super("StargatePortalEvent", portal);
        this.player = player;
        this.destination = dest;
        this.exit = exit;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public Portal getDestination() {
        return this.destination;
    }
    
    public Location getExit() {
        return this.exit;
    }
    
    public void setExit(final Location loc) {
        this.exit = loc;
    }
    
    static {
        handlers = new HandlerList();
    }
}
