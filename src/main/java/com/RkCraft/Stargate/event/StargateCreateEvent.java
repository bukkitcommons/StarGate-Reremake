package com.RkCraft.Stargate.event;

import org.bukkit.entity.*;
import org.bukkit.event.*;
import com.RkCraft.Stargate.*;

public class StargateCreateEvent extends StargateEvent
{
    private final Player player;
    private boolean deny;
    private String denyReason;
    private final String[] lines;
    private int cost;
    private static final HandlerList handlers;
    
    public HandlerList getHandlers() {
        return StargateCreateEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return StargateCreateEvent.handlers;
    }
    
    public StargateCreateEvent(final Player player, final Portal portal, final String[] lines, final boolean deny, final String denyReason, final int cost) {
        super("StargateCreateEvent", portal);
        this.player = player;
        this.lines = lines;
        this.deny = deny;
        this.denyReason = denyReason;
        this.cost = cost;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public String getLine(final int index) throws IndexOutOfBoundsException {
        return this.lines[index];
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
