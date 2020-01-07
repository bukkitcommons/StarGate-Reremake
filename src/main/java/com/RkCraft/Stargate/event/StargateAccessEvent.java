package com.RkCraft.Stargate.event;

import org.bukkit.entity.*;
import org.bukkit.event.*;
import com.RkCraft.Stargate.*;

public class StargateAccessEvent extends StargateEvent
{
    private final Player player;
    private boolean deny;
    private static final HandlerList handlers;
    
    public HandlerList getHandlers() {
        return StargateAccessEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return StargateAccessEvent.handlers;
    }
    
    public StargateAccessEvent(final Player player, final Portal portal, final boolean deny) {
        super("StargateAccessEvent", portal);
        this.player = player;
        this.deny = deny;
    }
    
    public boolean getDeny() {
        return this.deny;
    }
    
    public void setDeny(final boolean deny) {
        this.deny = deny;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    static {
        handlers = new HandlerList();
    }
}
