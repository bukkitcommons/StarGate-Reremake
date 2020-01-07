package com.RkCraft.Stargate.event;

import org.bukkit.entity.*;
import org.bukkit.event.*;
import com.RkCraft.Stargate.*;

public class StargateOpenEvent extends StargateEvent
{
    private final Player player;
    private boolean force;
    private static final HandlerList handlers;
    
    public HandlerList getHandlers() {
        return StargateOpenEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return StargateOpenEvent.handlers;
    }
    
    public StargateOpenEvent(final Player player, final Portal portal, final boolean force) {
        super("StargateOpenEvent", portal);
        this.player = player;
        this.force = force;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public boolean getForce() {
        return this.force;
    }
    
    public void setForce(final boolean force) {
        this.force = force;
    }
    
    static {
        handlers = new HandlerList();
    }
}
