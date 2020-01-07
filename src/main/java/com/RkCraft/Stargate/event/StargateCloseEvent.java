package com.RkCraft.Stargate.event;

import org.bukkit.event.*;
import com.RkCraft.Stargate.*;

public class StargateCloseEvent extends StargateEvent
{
    private boolean force;
    private static final HandlerList handlers;
    
    public HandlerList getHandlers() {
        return StargateCloseEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return StargateCloseEvent.handlers;
    }
    
    public StargateCloseEvent(final Portal portal, final boolean force) {
        super("StargateCloseEvent", portal);
        this.force = force;
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
