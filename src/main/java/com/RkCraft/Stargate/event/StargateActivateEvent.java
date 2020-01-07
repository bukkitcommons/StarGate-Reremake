package com.RkCraft.Stargate.event;

import org.bukkit.entity.*;
import java.util.*;
import org.bukkit.event.*;
import com.RkCraft.Stargate.*;

public class StargateActivateEvent extends StargateEvent
{
    private final Player player;
    private ArrayList<String> destinations;
    private String destination;
    private static final HandlerList handlers;
    
    public HandlerList getHandlers() {
        return StargateActivateEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return StargateActivateEvent.handlers;
    }
    
    public StargateActivateEvent(final Portal portal, final Player player, final ArrayList<String> destinations, final String destination) {
        super("StargatActivateEvent", portal);
        this.player = player;
        this.destinations = destinations;
        this.destination = destination;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public ArrayList<String> getDestinations() {
        return this.destinations;
    }
    
    public void setDestinations(final ArrayList<String> destinations) {
        this.destinations = destinations;
    }
    
    public String getDestination() {
        return this.destination;
    }
    
    public void setDestination(final String destination) {
        this.destination = destination;
    }
    
    static {
        handlers = new HandlerList();
    }
}
