package com.RkCraft.Stargate.event;

import org.bukkit.event.*;
import com.RkCraft.Stargate.*;

public abstract class StargateEvent extends Event implements Cancellable
{
    protected Portal portal;
    protected boolean cancelled;
    
    public StargateEvent(final String event, final Portal portal) {
        this.portal = portal;
        this.cancelled = false;
    }
    
    public Portal getPortal() {
        return this.portal;
    }
    
    public boolean isCancelled() {
        return this.cancelled;
    }
    
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }
}
