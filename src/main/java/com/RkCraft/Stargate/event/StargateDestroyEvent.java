package com.RkCraft.Stargate.event;

import org.bukkit.entity.*;
import org.bukkit.event.*;
import com.RkCraft.Stargate.*;

public class StargateDestroyEvent extends StargateEvent
{
    private final Player player;
    private boolean deny;
    private String denyReason;
    private int cost;
    private static final HandlerList handlers;
    
    public HandlerList getHandlers() {
        return StargateDestroyEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return StargateDestroyEvent.handlers;
    }
    
    public StargateDestroyEvent(final Portal portal, final Player player, final boolean deny, final String denyMsg, final int cost) {
        super("StargateDestroyEvent", portal);
        this.player = player;
        this.deny = deny;
        this.denyReason = denyMsg;
        this.cost = cost;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public boolean getDeny() {
        return this.deny;
    }
    
    public void setDeny(final boolean deny) {
        this.deny = deny;
    }
    
    public String getDenyReason() {
        return this.denyReason;
    }
    
    public void setDenyReason(final String denyReason) {
        this.denyReason = denyReason;
    }
    
    public int getCost() {
        return this.cost;
    }
    
    public void setCost(final int cost) {
        this.cost = cost;
    }
    
    static {
        handlers = new HandlerList();
    }
}
