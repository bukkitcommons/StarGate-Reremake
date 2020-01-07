package com.RkCraft.Stargate;

import org.bukkit.block.Sign;
import org.bukkit.block.data.Powerable;
import org.bukkit.event.*;
import org.bukkit.material.*;
import org.bukkit.event.player.*;
import org.bukkit.util.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.*;
import org.bukkit.entity.minecart.*;
import java.util.logging.*;
import org.bukkit.command.*;
import org.bukkit.block.*;
import org.bukkit.*;
import org.bukkit.event.block.*;
import com.RkCraft.Stargate.event.*;
import org.bukkit.util.Vector;

import java.io.*;
import java.util.*;

public final class Portal
{
    private static final HashMap<Blox, Portal> lookupBlocks;
    private static final HashMap<Blox, Portal> lookupEntrances;
    private static final HashMap<Blox, Portal> lookupControls;
    private static final ArrayList<Portal> allPortals;
    private static final HashMap<String, ArrayList<String>> allPortalsNet;
    private static final HashMap<String, HashMap<String, Portal>> lookupNamesNet;
    private static final HashMap<String, Portal> bungeePortals;
    private final Blox topLeft;
    private final int modX;
    private final int modZ;
    private final float rotX;
    private final Blox id;
    private Blox button;
    private Blox[] frame;
    private Blox[] entrances;
    private String name;
    private String destination;
    private String lastDest;
    private String network;
    private final Gate gate;
    private OfflinePlayer owner;
    private final World world;
    private boolean verified;
    private boolean fixed;
    private boolean hidden;
    private boolean alwaysOn;
    private boolean priv;
    private boolean free;
    private boolean backwards;
    private boolean show;
    private boolean noNetwork;
    private boolean random;
    private boolean bungee;
    private Player player;
    private Player activePlayer;
    private ArrayList<String> destinations;
    private boolean isOpen;
    private long openTime;
    
    private Portal(final Blox topLeft, final int modX, final int modZ, final float rotX, final Blox id, final Blox button, final String dest, final String name, final boolean verified, final String network, final Gate gate, final OfflinePlayer owner, final boolean hidden, final boolean alwaysOn, final boolean priv, final boolean free, final boolean backwards, final boolean show, final boolean noNetwork, final boolean random, final boolean bungee) {
        this.lastDest = "";
        this.owner = null;
        this.hidden = false;
        this.alwaysOn = false;
        this.priv = false;
        this.free = false;
        this.backwards = false;
        this.show = false;
        this.noNetwork = false;
        this.random = false;
        this.bungee = false;
        this.destinations = new ArrayList<>();
        this.isOpen = false;
        this.topLeft = topLeft;
        this.modX = modX;
        this.modZ = modZ;
        this.rotX = rotX;
        this.id = id;
        this.destination = dest;
        this.button = button;
        this.verified = verified;
        this.network = network;
        this.name = name;
        this.gate = gate;
        this.owner = owner;
        this.hidden = hidden;
        this.alwaysOn = alwaysOn;
        this.priv = priv;
        this.free = free;
        this.backwards = backwards;
        this.show = show;
        this.noNetwork = noNetwork;
        this.random = random;
        this.bungee = bungee;
        this.world = topLeft.getWorld();
        this.fixed = (dest.length() > 0 || this.random || this.bungee);
        if (this.isAlwaysOn() && !this.isFixed()) {
            this.alwaysOn = false;
            Stargate.debug("Portal", "Can not create a non-fixed always-on gate. Setting AlwaysOn = false");
        }
        if (this.random && !this.isAlwaysOn()) {
            this.alwaysOn = true;
            Stargate.debug("Portal", "Gate marked as random, set to always-on");
        }
        if (verified) {
            this.drawSign();
        }
    }
    
    public boolean isOpen() {
        return this.isOpen || this.isAlwaysOn();
    }
    
    public boolean isAlwaysOn() {
        return this.alwaysOn;
    }
    
    public boolean isHidden() {
        return this.hidden;
    }
    
    public boolean isPrivate() {
        return this.priv;
    }
    
    public boolean isFree() {
        return this.free;
    }
    
    public boolean isBackwards() {
        return this.backwards;
    }
    
    public boolean isShown() {
        return this.show;
    }
    
    public boolean isNoNetwork() {
        return this.noNetwork;
    }
    
    public boolean isRandom() {
        return this.random;
    }
    
    public boolean isBungee() {
        return this.bungee;
    }
    
    public void setAlwaysOn(final boolean alwaysOn) {
        this.alwaysOn = alwaysOn;
    }
    
    public void setHidden(final boolean hidden) {
        this.hidden = hidden;
    }
    
    public void setPrivate(final boolean priv) {
        this.priv = priv;
    }
    
    public void setFree(final boolean free) {
        this.free = free;
    }
    
    public void setBackwards(final boolean backwards) {
        this.backwards = backwards;
    }
    
    public void setShown(final boolean show) {
        this.show = show;
    }
    
    public void setNoNetwork(final boolean noNetwork) {
        this.noNetwork = noNetwork;
    }
    
    public void setRandom(final boolean random) {
        this.random = random;
    }
    
    public float getRotation() {
        return this.rotX;
    }
    
    public Player getActivePlayer() {
        return this.activePlayer;
    }
    
    public String getNetwork() {
        return this.network;
    }
    
    public void setNetwork(final String network) {
        this.network = network;
    }
    
    public long getOpenTime() {
        return this.openTime;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(final String name) {
        this.name = filterName(name);
        this.drawSign();
    }
    
    public Portal getDestination(final Player player) {
        if (!this.isRandom()) {
            return getByName(this.destination, this.getNetwork());
        }
        this.destinations = this.getDestinations(player, this.getNetwork());
        if (this.destinations.isEmpty()) {
            this.destinations.clear();
            return null;
        }
        final String dest = this.destinations.get(new Random().nextInt(this.destinations.size()));
        this.destinations.clear();
        return getByName(dest, this.getNetwork());
    }
    
    public Portal getDestination() {
        return this.getDestination(null);
    }
    
    public void setDestination(final Portal destination) {
        this.setDestination(destination.getName());
    }
    
    public void setDestination(final String destination) {
        this.destination = destination;
    }
    
    public String getDestinationName() {
        return this.destination;
    }
    
    public Gate getGate() {
        return this.gate;
    }
    
    public OfflinePlayer getOwner() {
        return this.owner;
    }
    
    public void setOwner(final OfflinePlayer owner) {
        this.owner = owner;
    }
    
    public Blox[] getEntrances() {
        if (this.entrances == null) {
            final RelativeBlockVector[] space = this.gate.getEntrances();
            this.entrances = new Blox[space.length];
            int i = 0;
            for (final RelativeBlockVector vector : space) {
                this.entrances[i++] = this.getBlockAt(vector);
            }
        }
        return this.entrances;
    }
    
    public Blox[] getFrame() {
        if (this.frame == null) {
            final RelativeBlockVector[] border = this.gate.getBorder();
            this.frame = new Blox[border.length];
            int i = 0;
            for (final RelativeBlockVector vector : border) {
                this.frame[i++] = this.getBlockAt(vector);
            }
        }
        return this.frame;
    }
    
    public Block getSign() {
        return this.id.getBlock();
    }
    
    public World getWorld() {
        return this.world;
    }
    
    public Block getButton() {
        if (this.button == null) {
            return null;
        }
        return this.button.getBlock();
    }
    
    public void setButton(final Blox button) {
        this.button = button;
    }
    
    public static ArrayList<String> getNetwork(final String network) {
        return Portal.allPortalsNet.get(network.toLowerCase());
    }
    
    public boolean open(final boolean force) {
        return this.open(null, force);
    }
    
    public boolean open(final Player openFor, boolean force) {
        final StargateOpenEvent event = new StargateOpenEvent(openFor, this, force);
        Stargate.server.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        force = event.getForce();
        if (this.isOpen() && !force) {
            return false;
        }
        this.getWorld().loadChunk(this.getWorld().getChunkAt(this.topLeft.getBlock()));
        final Material openType = this.gate.getPortalBlockOpen();
        for (final Blox inside : this.getEntrances()) {
            Stargate.blockPopulatorQueue.add(new BloxPopulator(inside, openType));
        }
        this.isOpen = true;
        this.openTime = System.currentTimeMillis() / 1000L;
        Stargate.openList.add(this);
        Stargate.activeList.remove(this);
        if (!this.isAlwaysOn()) {
            this.player = openFor;
            final Portal end = this.getDestination();
            if (!this.random && end != null && (!end.isFixed() || end.getDestinationName().equalsIgnoreCase(this.getName())) && !end.isOpen()) {
                end.open(openFor, false);
                end.setDestination(this);
                if (end.isVerified()) {
                    end.drawSign();
                }
            }
        }
        return true;
    }
    
    public void close(boolean force) {
        if (!this.isOpen) {
            return;
        }
        final StargateCloseEvent event = new StargateCloseEvent(this, force);
        Stargate.server.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        force = event.getForce();
        if (this.isAlwaysOn() && !force) {
            return;
        }
        final Material closedType = this.gate.getPortalBlockClosed();
        for (final Blox inside : this.getEntrances()) {
            Stargate.blockPopulatorQueue.add(new BloxPopulator(inside, closedType));
        }
        this.player = null;
        this.isOpen = false;
        Stargate.openList.remove(this);
        Stargate.activeList.remove(this);
        if (!this.isAlwaysOn()) {
            final Portal end = this.getDestination();
            if (end != null && end.isOpen()) {
                end.deactivate();
                end.close(false);
            }
        }
        this.deactivate();
    }
    
    public boolean isOpenFor(final Player player) {
        return this.isOpen && (this.isAlwaysOn() || this.player == null || (player != null && player.getName().equalsIgnoreCase(this.player.getName())));
    }
    
    public boolean isFixed() {
        return this.fixed;
    }
    
    public boolean isPowered() {
        final RelativeBlockVector[] arr$;
        final RelativeBlockVector[] controls = arr$ = this.gate.getControls();
        for (final RelativeBlockVector vector : arr$) {
            //final MaterialData mat = this.getBlockAt(vector).getBlock().getState().getData();
            BlockState state = this.getBlockAt(vector).getBlock().getState();
            if(state instanceof Powerable){
                return ((Powerable) state).isPowered();
            }
        }
        return false;
    }
    
    public void teleport(final Player player, final Portal origin, final PlayerMoveEvent event) {
        final Location traveller = player.getLocation();
        Location exit = this.getExit(traveller);
        int adjust = 180;
        if (this.isBackwards() || origin.isBackwards()) {
            adjust = 0;
        }
        if (this.isBackwards() && origin.isBackwards()) {
            adjust = 180;
        }
        exit.setYaw(origin.getRotation() - traveller.getYaw() + this.getRotation() + adjust);
        if (!origin.equals(this)) {
            final StargatePortalEvent pEvent = new StargatePortalEvent(player, origin, this, exit);
            Stargate.server.getPluginManager().callEvent(pEvent);
            if (pEvent.isCancelled()) {
                origin.teleport(player, origin, event);
                return;
            }
            exit = pEvent.getExit();
        }
        if (event == null) {
            exit.setYaw(this.getRotation());
            player.teleport(exit);
        }
        else {
            event.setTo(exit);
        }
    }
    
    public void teleport(final Vehicle vehicle) {
        final Location traveller = new Location(this.world, vehicle.getLocation().getX(), vehicle.getLocation().getY(), vehicle.getLocation().getZ());
        final Location exit = this.getExit(traveller);
        final double velocity = vehicle.getVelocity().length();
        vehicle.setVelocity(new Vector());
        final Vector newVelocity = new Vector();
        switch (this.id.getBlock().getData()) {
            case 2: {
                newVelocity.setZ(-1);
                break;
            }
            case 3: {
                newVelocity.setZ(1);
                break;
            }
            case 4: {
                newVelocity.setX(-1);
                break;
            }
            case 5: {
                newVelocity.setX(1);
                break;
            }
        }
        newVelocity.multiply(velocity);
        final List<Entity> passengers = vehicle.getPassengers();
        if (passengers.isEmpty()) {
            final Vehicle v = exit.getWorld().spawn(exit, vehicle.getClass());
            vehicle.eject();
            vehicle.remove();
            passengers.forEach((entity -> entity.teleport(exit)));
            //passengers.teleport(exit);
            Stargate.server.getScheduler().scheduleSyncDelayedTask(Stargate.stargate, new Runnable() {
                @Override
                public void run() {
                    passengers.forEach(v::addPassenger);
                    v.setVelocity(newVelocity);
                }
            }, 1L);
        }
        else {
            final Vehicle mc = (Vehicle)exit.getWorld().spawn(exit, vehicle.getClass());
            if (mc instanceof StorageMinecart) {
                final StorageMinecart smc = (StorageMinecart)mc;
                smc.getInventory().setContents(((StorageMinecart)vehicle).getInventory().getContents());
            }
            mc.setVelocity(newVelocity);
            vehicle.remove();
        }
    }
    
    public Location getExit(final Location traveller) {
        Location loc = null;
        if (this.gate.getExit() != null) {
            final Blox exit = this.getBlockAt(this.gate.getExit());
            final int back = this.isBackwards() ? -1 : 1;
            loc = exit.modRelativeLoc(0.0, 0.0, 1.0, traveller.getYaw(), traveller.getPitch(), this.modX * back, 1, this.modZ * back);
        }
        else {
            Stargate.log.log(Level.WARNING, "[Stargate] Missing destination point in .gate file {0}", this.gate.getFilename());
        }
        if (loc != null) {
            if (Tag.STAIRS.isTagged(this.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).getType())) {
                loc.setY(loc.getY() + 0.5);
            }
            loc.setPitch(traveller.getPitch());
            return loc;
        }
        return traveller;
    }
    
    public boolean isChunkLoaded() {
        return this.getWorld().isChunkLoaded(this.topLeft.getBlock().getChunk());
    }
    
    public void loadChunk() {
        this.getWorld().loadChunk(this.topLeft.getBlock().getChunk());
    }
    
    public boolean isVerified() {
        this.verified = true;
        for (final RelativeBlockVector control : this.gate.getControls()) {
            this.verified = (this.verified && this.getBlockAt(control).getBlock().getType() == this.gate.getControlBlock());
        }
        return this.verified;
    }
    
    public boolean wasVerified() {
        return this.verified;
    }
    
    public boolean checkIntegrity() {
        return this.gate.matches(this.topLeft, this.modX, this.modZ);
    }
    
    public ArrayList<String> getDestinations(final Player player, final String network) {
        final ArrayList<String> destinations = new ArrayList<>();
        for (final String dest : Portal.allPortalsNet.get(network.toLowerCase())) {
            final Portal portal = getByName(dest, network);
            if (portal.isRandom()) {
                continue;
            }
            if (portal.isAlwaysOn() && !portal.isShown()) {
                continue;
            }
            if (dest.equalsIgnoreCase(this.getName())) {
                continue;
            }
            if (portal.isFixed() && !portal.getDestinationName().equalsIgnoreCase(this.getName())) {
                continue;
            }
            if (player == null) {
                destinations.add(portal.getName());
            }
            else {
                if (!Stargate.canAccessWorld(player, portal.getWorld().getName())) {
                    continue;
                }
                if (!Stargate.canSee(player, portal)) {
                    continue;
                }
                destinations.add(portal.getName());
            }
        }
        return destinations;
    }
    
    public boolean activate(final Player player) {
        this.destinations.clear();
        this.destination = "";
        Stargate.activeList.add(this);
        this.activePlayer = player;
        final String network = this.getNetwork();
        this.destinations = this.getDestinations(player, network);
        if (Stargate.sortLists) {
            Collections.sort(this.destinations);
        }
        if (Stargate.destMemory && !this.lastDest.isEmpty() && this.destinations.contains(this.lastDest)) {
            this.destination = this.lastDest;
        }
        final StargateActivateEvent event = new StargateActivateEvent(this, player, this.destinations, this.destination);
        Stargate.server.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            Stargate.activeList.remove(this);
            return false;
        }
        this.destination = event.getDestination();
        this.destinations = event.getDestinations();
        this.drawSign();
        return true;
    }
    
    public void deactivate() {
        final StargateDeactivateEvent event = new StargateDeactivateEvent(this);
        Stargate.server.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        Stargate.activeList.remove(this);
        if (this.isFixed()) {
            return;
        }
        this.destinations.clear();
        this.destination = "";
        this.activePlayer = null;
        this.drawSign();
    }
    
    public boolean isActive() {
        return this.isFixed() || this.destinations.size() > 0;
    }
    
    public void cycleDestination(final Player player) {
        this.cycleDestination(player, 1);
    }
    
    public void cycleDestination(final Player player, final int dir) {
        Boolean activate = false;
        if (!this.isActive() || this.getActivePlayer() != player) {
            if (!this.activate(player)) {
                return;
            }
            Stargate.debug("cycleDestination", "Network Size: " + Portal.allPortalsNet.get(this.network.toLowerCase()).size());
            Stargate.debug("cycleDestination", "Player has access to: " + this.destinations.size());
            activate = true;
        }
        if (this.destinations.isEmpty()) {
            Stargate.sendMessage(player, Stargate.getString("destEmpty"));
            return;
        }
        if (!Stargate.destMemory || !activate || this.lastDest.isEmpty()) {
            int index = this.destinations.indexOf(this.destination);
            index += dir;
            if (index >= this.destinations.size()) {
                index = 0;
            }
            else if (index < 0) {
                index = this.destinations.size() - 1;
            }
            this.destination = this.destinations.get(index);
            this.lastDest = this.destination;
        }
        this.openTime = System.currentTimeMillis() / 1000L;
        this.drawSign();
    }
    
    public final void drawSign() {
        final Material sMat = this.id.getBlock().getType();
        if (!Tag.SIGNS.isTagged(sMat) || !Tag.WALL_SIGNS.isTagged(sMat)) {
            Stargate.log.warning("[Stargate] Sign block is not a Sign object");
            Stargate.debug("Portal::drawSign", "Block: " + this.id.getBlock().getType() + " @ " + this.id.getBlock().getLocation());
            return;
        }
        final Sign sign = (Sign)this.id.getBlock().getState();
        Stargate.setLine(sign, 0, "-" + this.name + "-");
        final int max = this.destinations.size() - 1;
        int done = 0;
        if (!this.isActive()) {
            Stargate.setLine(sign, ++done, Stargate.getString("signRightClick"));
            Stargate.setLine(sign, ++done, Stargate.getString("signToUse"));
            if (!this.noNetwork) {
                Stargate.setLine(sign, ++done, "(" + this.network + ")");
            }
        }
        else if (this.isBungee()) {
            Stargate.setLine(sign, ++done, Stargate.getString("bungeeSign"));
            Stargate.setLine(sign, ++done, ">" + this.destination + "<");
            Stargate.setLine(sign, ++done, "[" + this.network + "]");
        }
        else if (this.isFixed()) {
            if (this.isRandom()) {
                Stargate.setLine(sign, ++done, "> " + Stargate.getString("signRandom") + " <");
            }
            else {
                Stargate.setLine(sign, ++done, ">" + this.destination + "<");
            }
            if (this.noNetwork) {
                Stargate.setLine(sign, ++done, "");
            }
            else {
                Stargate.setLine(sign, ++done, "(" + this.network + ")");
            }
            final Portal dest = getByName(this.destination, this.network);
            if (dest == null && !this.isRandom()) {
                Stargate.setLine(sign, ++done, Stargate.getString("signDisconnected"));
            }
            else {
                Stargate.setLine(sign, ++done, "");
            }
        }
        else {
            final int index = this.destinations.indexOf(this.destination);
            if (index == max && max > 1 && ++done <= 3) {
                if (iConomyHandler.useiConomy() && iConomyHandler.freeGatesGreen) {
                    final Portal dest2 = getByName(this.destinations.get(index - 2), this.network);
                    final boolean green = Stargate.isFree(this.activePlayer, this, dest2);
                    Stargate.setLine(sign, done, (green ? ChatColor.DARK_GREEN : "") + this.destinations.get(index - 2));
                }
                else {
                    Stargate.setLine(sign, done, this.destinations.get(index - 2));
                }
            }
            if (index > 0 && ++done <= 3) {
                if (iConomyHandler.useiConomy() && iConomyHandler.freeGatesGreen) {
                    final Portal dest2 = getByName(this.destinations.get(index - 1), this.network);
                    final boolean green = Stargate.isFree(this.activePlayer, this, dest2);
                    Stargate.setLine(sign, done, (green ? ChatColor.DARK_GREEN : "") + this.destinations.get(index - 1));
                }
                else {
                    Stargate.setLine(sign, done, this.destinations.get(index - 1));
                }
            }
            if (++done <= 3) {
                if (iConomyHandler.useiConomy() && iConomyHandler.freeGatesGreen) {
                    final Portal dest2 = getByName(this.destination, this.network);
                    final boolean green = Stargate.isFree(this.activePlayer, this, dest2);
                    Stargate.setLine(sign, done, (green ? ChatColor.DARK_GREEN : "") + ">" + this.destination + "<");
                }
                else {
                    Stargate.setLine(sign, done, " >" + this.destination + "< ");
                }
            }
            if (max >= index + 1 && ++done <= 3) {
                if (iConomyHandler.useiConomy() && iConomyHandler.freeGatesGreen) {
                    final Portal dest2 = getByName(this.destinations.get(index + 1), this.network);
                    final boolean green = Stargate.isFree(this.activePlayer, this, dest2);
                    Stargate.setLine(sign, done, (green ? ChatColor.DARK_GREEN : "") + this.destinations.get(index + 1));
                }
                else {
                    Stargate.setLine(sign, done, this.destinations.get(index + 1));
                }
            }
            if (max >= index + 2 && ++done <= 3) {
                if (iConomyHandler.useiConomy() && iConomyHandler.freeGatesGreen) {
                    final Portal dest2 = getByName(this.destinations.get(index + 2), this.network);
                    final boolean green = Stargate.isFree(this.activePlayer, this, dest2);
                    Stargate.setLine(sign, done, (green ? ChatColor.DARK_GREEN : "") + this.destinations.get(index + 2));
                }
                else {
                    Stargate.setLine(sign, done, this.destinations.get(index + 2));
                }
            }
        }
        ++done;
        while (done <= 3) {
            sign.setLine(done, "");
            ++done;
        }
        sign.update();
    }
    
    public void unregister(final boolean removeAll) {
        Stargate.debug("Unregister", "Unregistering gate " + this.getName());
        this.close(true);
        for (final Blox block : this.getFrame()) {
            Portal.lookupBlocks.remove(block);
        }
        Portal.lookupBlocks.remove(this.id);
        if (this.button != null) {
            Portal.lookupBlocks.remove(this.button);
        }
        Portal.lookupControls.remove(this.id);
        if (this.button != null) {
            Portal.lookupControls.remove(this.button);
        }
        for (final Blox entrance : this.getEntrances()) {
            Portal.lookupEntrances.remove(entrance);
        }
        if (removeAll) {
            Portal.allPortals.remove(this);
        }
        if (this.bungee) {
            Portal.bungeePortals.remove(this.getName().toLowerCase());
        }
        else {
            Portal.lookupNamesNet.get(this.getNetwork().toLowerCase()).remove(this.getName().toLowerCase());
            Portal.allPortalsNet.get(this.getNetwork().toLowerCase()).remove(this.getName().toLowerCase());
            for (final String originName : Portal.allPortalsNet.get(this.getNetwork().toLowerCase())) {
                final Portal origin = getByName(originName, this.getNetwork());
                if (origin == null) {
                    continue;
                }
                if (!origin.getDestinationName().equalsIgnoreCase(this.getName())) {
                    continue;
                }
                if (!origin.isVerified()) {
                    continue;
                }
                if (origin.isFixed()) {
                    origin.drawSign();
                }
                if (!origin.isAlwaysOn()) {
                    continue;
                }
                origin.close(true);
            }
        }
        if (Tag.WALL_SIGNS.isTagged(this.id.getBlock().getType()) && this.id.getBlock().getState() instanceof Sign) {
            final Sign sign = (Sign)this.id.getBlock().getState();
            sign.setLine(0, this.getName());
            sign.setLine(1, "");
            sign.setLine(2, "");
            sign.setLine(3, "");
            sign.update();
        }
        saveAllGates(this.getWorld());
    }
    
    private Blox getBlockAt(final RelativeBlockVector vector) {
        return this.topLeft.modRelative(vector.getRight(), vector.getDepth(), vector.getDistance(), this.modX, 1, this.modZ);
    }
    
    private void register() {
        this.fixed = (this.destination.length() > 0 || this.random || this.bungee);
        if (this.isBungee()) {
            Portal.bungeePortals.put(this.getName().toLowerCase(), this);
        }
        else {
            if (!Portal.lookupNamesNet.containsKey(this.getNetwork().toLowerCase())) {
                Stargate.debug("register", "Network " + this.getNetwork() + " not in lookupNamesNet, adding");
                Portal.lookupNamesNet.put(this.getNetwork().toLowerCase(), new HashMap<>());
            }
            Portal.lookupNamesNet.get(this.getNetwork().toLowerCase()).put(this.getName().toLowerCase(), this);
            if (!Portal.allPortalsNet.containsKey(this.getNetwork().toLowerCase())) {
                Stargate.debug("register", "Network " + this.getNetwork() + " not in allPortalsNet, adding");
                Portal.allPortalsNet.put(this.getNetwork().toLowerCase(), new ArrayList<>());
            }
            Portal.allPortalsNet.get(this.getNetwork().toLowerCase()).add(this.getName().toLowerCase());
        }
        for (final Blox block : this.getFrame()) {
            Portal.lookupBlocks.put(block, this);
        }
        Portal.lookupBlocks.put(this.id, this);
        if (this.button != null) {
            Portal.lookupBlocks.put(this.button, this);
        }
        Portal.lookupControls.put(this.id, this);
        if (this.button != null) {
            Portal.lookupControls.put(this.button, this);
        }
        for (final Blox entrance : this.getEntrances()) {
            Portal.lookupEntrances.put(entrance, this);
        }
        Portal.allPortals.add(this);
    }
    
    public static Portal createPortal(final SignChangeEvent event, final Player player) {
        final Blox id = new Blox(event.getBlock());
        final Block idParent = id.getParent();
        if (idParent == null) {
            Stargate.debug("createPortal","idParent is null");
            return null;
        }
        if (Gate.getGatesByControlBlock(idParent).length == 0) {
            Stargate.debug("createPortal","getGatesByControlBlock(idParent) size = 0");
            return null;
        }
        if (getByBlock(idParent) != null) {
            Stargate.debug("createPortal", "idParent belongs to existing gate");
            return null;
        }
        final Blox parent = new Blox(player.getWorld(), idParent.getX(), idParent.getY(), idParent.getZ());
        Blox topleft = null;
        final String name = filterName(event.getLine(0));
        final String destName = filterName(event.getLine(1));
        String network = filterName(event.getLine(2));
        final String options = filterName(event.getLine(3)).toLowerCase();
        boolean hidden = options.indexOf(104) != -1;
        boolean alwaysOn = options.indexOf(97) != -1;
        boolean priv = options.indexOf(112) != -1;
        boolean free = options.indexOf(102) != -1;
        boolean backwards = options.indexOf(98) != -1;
        boolean show = options.indexOf(115) != -1;
        boolean noNetwork = options.indexOf(110) != -1;
        boolean random = options.indexOf(114) != -1;
        final boolean bungee = options.indexOf(117) != -1;
        if (hidden && !Stargate.canOption(player, "hidden")) {
            hidden = false;
        }
        if (alwaysOn && !Stargate.canOption(player, "alwayson")) {
            alwaysOn = false;
        }
        if (priv && !Stargate.canOption(player, "private")) {
            priv = false;
        }
        if (free && !Stargate.canOption(player, "free")) {
            free = false;
        }
        if (backwards && !Stargate.canOption(player, "backwards")) {
            backwards = false;
        }
        if (show && !Stargate.canOption(player, "show")) {
            show = false;
        }
        if (noNetwork && !Stargate.canOption(player, "nonetwork")) {
            noNetwork = false;
        }
        if (random && !Stargate.canOption(player, "random")) {
            random = false;
        }
        if (alwaysOn && destName.length() == 0) {
            alwaysOn = false;
        }
        if (show && !alwaysOn) {
            show = false;
        }
        if (random) {
            alwaysOn = true;
            show = false;
        }
        if (bungee) {
            alwaysOn = true;
            random = false;
        }
        int modX = 0;
        int modZ = 0;
        float rotX = 0.0f;
        int facing = 0;
        if (idParent.getX() > id.getBlock().getX()) {
            --modZ;
            rotX = 90.0f;
            facing = 2;
        }
        else if (idParent.getX() < id.getBlock().getX()) {
            ++modZ;
            rotX = 270.0f;
            facing = 1;
        }
        else if (idParent.getZ() > id.getBlock().getZ()) {
            ++modX;
            rotX = 180.0f;
            facing = 4;
        }
        else if (idParent.getZ() < id.getBlock().getZ()) {
            --modX;
            rotX = 0.0f;
            facing = 3;
        }
        final Gate[] possibleGates = Gate.getGatesByControlBlock(idParent);
        Gate gate = null;
        RelativeBlockVector buttonVector = null;
        for (final Gate possibility : possibleGates) {
            if (gate == null && buttonVector == null) {
                final RelativeBlockVector[] vectors = possibility.getControls();
                RelativeBlockVector otherControl = null;
                for (final RelativeBlockVector vector : vectors) {
                    final Blox tl = parent.modRelative(-vector.getRight(), -vector.getDepth(), -vector.getDistance(), modX, 1, modZ);
                    if (gate == null) {
                        if (possibility.matches(tl, modX, modZ, true)) {
                            gate = possibility;
                            topleft = tl;
                            if (otherControl != null) {
                                buttonVector = otherControl;
                            }
                        }
                    }
                    else if (otherControl != null) {
                        buttonVector = vector;
                    }
                    otherControl = vector;
                }
            }
        }
        if (gate == null || buttonVector == null) {
            Stargate.debug("createPortal", "Could not find matching gate layout");
            return null;
        }
        if (bungee) {
            if (!Stargate.enableBungee) {
                Stargate.sendMessage(player, Stargate.getString("bungeeDisabled"));
                return null;
            }
            if (!Stargate.hasPerm(player, "stargate.admin.bungee")) {
                Stargate.sendMessage(player, Stargate.getString("bungeeDeny"));
                return null;
            }
            if (destName.isEmpty() || network.isEmpty()) {
                Stargate.sendMessage(player, Stargate.getString("bungeeEmpty"));
                return null;
            }
        }
        Stargate.debug("createPortal", "h = " + hidden + " a = " + alwaysOn + " p = " + priv + " f = " + free + " b = " + backwards + " s = " + show + " n = " + noNetwork + " r = " + random + " u = " + bungee);
        if (!bungee && (network.length() < 1 || network.length() > 11)) {
            network = Stargate.getDefaultNetwork();
        }
        boolean deny = false;
        String denyMsg = "";
        if (!bungee && !Stargate.canCreate(player, network)) {
            Stargate.debug("createPortal", "Player doesn't have create permissions on network. Trying personal");
            if (Stargate.canCreatePersonal(player)) {
                network = player.getName();
                if (network.length() > 11) {
                    network = network.substring(0, 11);
                }
                Stargate.debug("createPortal", "Creating personal portal");
                Stargate.sendMessage(player, Stargate.getString("createPersonal"));
            }
            else {
                Stargate.debug("createPortal", "Player does not have access to network");
                deny = true;
                denyMsg = Stargate.getString("createNetDeny");
            }
        }
        String gateName = gate.getFilename();
        gateName = gateName.substring(0, gateName.indexOf(46));
        if (!deny && !Stargate.canCreateGate(player, gateName)) {
            Stargate.debug("createPortal", "Player does not have access to gate layout");
            deny = true;
            denyMsg = Stargate.getString("createGateDeny");
        }
        if (!bungee && !deny && destName.length() > 0) {
            final Portal p = getByName(destName, network);
            if (p != null) {
                final String world = p.getWorld().getName();
                if (!Stargate.canAccessWorld(player, world)) {
                    Stargate.debug("canCreate", "Player does not have access to destination world");
                    deny = true;
                    denyMsg = Stargate.getString("createWorldDeny");
                }
            }
        }
        for (final RelativeBlockVector v : gate.getBorder()) {
            final Blox b = topleft.modRelative(v.getRight(), v.getDepth(), v.getDistance(), modX, 1, modZ);
            if (getByBlock(b.getBlock()) != null) {
                Stargate.debug("createPortal", "Gate conflicts with existing gate");
                Stargate.sendMessage(player, Stargate.getString("createConflict"));
                return null;
            }
        }
        Blox button = null;
        Portal portal = null;
        portal = new Portal(topleft, modX, modZ, rotX, id, button, destName, name, false, network, gate, player, hidden, alwaysOn, priv, free, backwards, show, noNetwork, random, bungee);
        int cost = Stargate.getCreateCost(player, gate);
        final StargateCreateEvent cEvent = new StargateCreateEvent(player, portal, event.getLines(), deny, denyMsg, cost);
        Stargate.server.getPluginManager().callEvent(cEvent);
        if (cEvent.isCancelled()) {
            return null;
        }
        if (cEvent.getDeny()) {
            Stargate.sendMessage(player, cEvent.getDenyReason());
            return null;
        }
        cost = cEvent.getCost();
        if (portal.getName().length() < 1 || portal.getName().length() > 11) {
            Stargate.debug("createPortal", "Name length error");
            Stargate.sendMessage(player, Stargate.getString("createNameLength"));
            return null;
        }
        if (portal.isBungee()) {
            if (Portal.bungeePortals.get(portal.getName().toLowerCase()) != null) {
                Stargate.debug("createPortal::Bungee", "Gate Exists");
                Stargate.sendMessage(player, Stargate.getString("createExists"));
                return null;
            }
        }
        else {
            if (getByName(portal.getName(), portal.getNetwork()) != null) {
                Stargate.debug("createPortal", "Name Error");
                Stargate.sendMessage(player, Stargate.getString("createExists"));
                return null;
            }
            final ArrayList<String> netList = Portal.allPortalsNet.get(portal.getNetwork().toLowerCase());
            if (Stargate.maxGates > 0 && netList != null && netList.size() >= Stargate.maxGates) {
                Stargate.sendMessage(player, Stargate.getString("createFull"));
                return null;
            }
        }
        if (cost > 0) {
            if (!Stargate.chargePlayer(player, null, cost)) {
                String inFundMsg = Stargate.getString("ecoInFunds");
                inFundMsg = Stargate.replaceVars(inFundMsg, new String[] { "%cost%", "%portal%" }, new String[] { iConomyHandler.format(cost), name });
                Stargate.sendMessage(player, inFundMsg);
                Stargate.debug("createPortal", "Insufficient Funds");
                return null;
            }
            String deductMsg = Stargate.getString("ecoDeduct");
            deductMsg = Stargate.replaceVars(deductMsg, new String[] { "%cost%", "%portal%" }, new String[] { iConomyHandler.format(cost), name });
            Stargate.sendMessage(player, deductMsg, false);
        }
        if (!alwaysOn) {
            button = topleft.modRelative(buttonVector.getRight(), buttonVector.getDepth(), buttonVector.getDistance() + 1, modX, 1, modZ);
            button.setType(Material.STONE_BUTTON);
            portal.setButton(button);
        }
        portal.register();
        portal.drawSign();
        if (portal.isRandom() || portal.isBungee()) {
            portal.open(true);
        }
        else if (portal.isAlwaysOn()) {
            final Portal dest = getByName(destName, portal.getNetwork());
            if (dest != null) {
                portal.open(true);
                dest.drawSign();
            }
        }
        else {
            for (final Blox inside : portal.getEntrances()) {
                inside.setType(portal.getGate().getPortalBlockClosed());
            }
        }
        if (!portal.isBungee()) {
            for (final String originName : Portal.allPortalsNet.get(portal.getNetwork().toLowerCase())) {
                final Portal origin = getByName(originName, portal.getNetwork());
                if (origin == null) {
                    continue;
                }
                if (!origin.getDestinationName().equalsIgnoreCase(portal.getName())) {
                    continue;
                }
                if (!origin.isVerified()) {
                    continue;
                }
                if (origin.isFixed()) {
                    origin.drawSign();
                }
                if (!origin.isAlwaysOn()) {
                    continue;
                }
                origin.open(true);
            }
        }
        saveAllGates(portal.getWorld());
        return portal;
    }
    
    public static Portal getByName(final String name, final String network) {
        if (!Portal.lookupNamesNet.containsKey(network.toLowerCase())) {
            return null;
        }
        return Portal.lookupNamesNet.get(network.toLowerCase()).get(name.toLowerCase());
    }
    
    public static Portal getByEntrance(final Location location) {
        return Portal.lookupEntrances.get(new Blox(location));
    }
    
    public static Portal getByEntrance(final Block block) {
        return Portal.lookupEntrances.get(new Blox(block));
    }
    
    public static Portal getByControl(final Block block) {
        return Portal.lookupControls.get(new Blox(block));
    }
    
    public static Portal getByBlock(final Block block) {
        return Portal.lookupBlocks.get(new Blox(block));
    }
    
    public static Portal getBungeeGate(final String name) {
        return Portal.bungeePortals.get(name.toLowerCase());
    }
    
    public static void saveAllGates(final World world) {
        final String loc = Stargate.getSaveLocation() + "/" + world.getName() + ".db";
        try (final BufferedWriter bw = new BufferedWriter(new FileWriter(loc, false))) {
            for (final Portal portal : Portal.allPortals) {
                final String wName = portal.world.getName();
                if (!wName.equalsIgnoreCase(world.getName())) {
                    continue;
                }
                final StringBuilder builder = new StringBuilder();
                final Blox sign = new Blox(portal.id.getBlock());
                final Blox button = portal.button;
                builder.append(portal.name);
                builder.append(':');
                builder.append(sign.toString());
                builder.append(':');
                builder.append((button != null) ? button.toString() : "");
                builder.append(':');
                builder.append(portal.modX);
                builder.append(':');
                builder.append(portal.modZ);
                builder.append(':');
                builder.append(portal.rotX);
                builder.append(':');
                builder.append(portal.topLeft.toString());
                builder.append(':');
                builder.append(portal.gate.getFilename());
                builder.append(':');
                builder.append(portal.isFixed() ? portal.getDestinationName() : "");
                builder.append(':');
                builder.append(portal.getNetwork());
                builder.append(':');
                builder.append(portal.getOwner().getUniqueId().toString());
                builder.append(':');
                builder.append(portal.isHidden());
                builder.append(':');
                builder.append(portal.isAlwaysOn());
                builder.append(':');
                builder.append(portal.isPrivate());
                builder.append(':');
                builder.append(portal.world.getName());
                builder.append(':');
                builder.append(portal.isFree());
                builder.append(':');
                builder.append(portal.isBackwards());
                builder.append(':');
                builder.append(portal.isShown());
                builder.append(':');
                builder.append(portal.isNoNetwork());
                builder.append(':');
                builder.append(portal.isRandom());
                builder.append(':');
                builder.append(portal.isBungee());
                bw.append(builder.toString());
                bw.newLine();
            }
        }
        catch (Exception e) {
            Stargate.log.log(Level.SEVERE, "Exception while writing stargates to {0}: {1}", new Object[] { loc, e });
        }
    }
    
    public static void clearGates() {
        Portal.lookupBlocks.clear();
        Portal.lookupNamesNet.clear();
        Portal.lookupEntrances.clear();
        Portal.lookupControls.clear();
        Portal.allPortals.clear();
        Portal.allPortalsNet.clear();
    }
    
    public static void loadAllGates(final World world) {
        final String location = Stargate.getSaveLocation();
        final File db = new File(location, world.getName() + ".db");
        if (db.exists()) {
            int l = 0;
            int portalCount = 0;
            try {
                try (final Scanner scanner = new Scanner(db)) {
                    while (scanner.hasNextLine()) {
                        ++l;
                        final String line = scanner.nextLine().trim();
                        if (!line.startsWith("#")) {
                            if (line.isEmpty()) {
                                continue;
                            }
                            final String[] split = line.split(":");
                            if (split.length < 8) {
                                Stargate.log.log(Level.INFO, "[Stargate] Invalid line - {0}", l);
                            }
                            else {
                                final String name = split[0];
                                final Blox sign = new Blox(world, split[1]);
                                if (!(sign.getBlock().getState() instanceof Sign)) {
                                    Stargate.log.log(Level.INFO, "[Stargate] Sign on line {0} doesn''t exist. BlockType = {1}", new Object[] { l, sign.getBlock().getType() });
                                }
                                else {
                                    final Blox button = (split[2].length() > 0) ? new Blox(world, split[2]) : null;
                                    final int modX = Integer.parseInt(split[3]);
                                    final int modZ = Integer.parseInt(split[4]);
                                    final float rotX = Float.parseFloat(split[5]);
                                    final Blox topLeft = new Blox(world, split[6]);
                                    final Gate gate = split[7].contains(";") ? Gate.getGateByName("nethergate.gate") : Gate.getGateByName(split[7]);
                                    if (gate == null) {
                                        Stargate.log.log(Level.INFO, "[Stargate] Gate layout on line {0} does not exist [{1}]", new Object[] { l, split[7] });
                                    }
                                    else {
                                        final String dest = (split.length > 8) ? split[8] : "";
                                        String network = (split.length > 9) ? split[9] : Stargate.getDefaultNetwork();
                                        if (network.isEmpty()) {
                                            network = Stargate.getDefaultNetwork();
                                        }
                                        final OfflinePlayer owner = Bukkit.getOfflinePlayer(UUID.fromString((split.length > 10) ? split[10] : ""));
                                        final boolean hidden = split.length > 11 && split[11].equalsIgnoreCase("true");
                                        final boolean alwaysOn = split.length > 12 && split[12].equalsIgnoreCase("true");
                                        final boolean priv = split.length > 13 && split[13].equalsIgnoreCase("true");
                                        final boolean free = split.length > 15 && split[15].equalsIgnoreCase("true");
                                        final boolean backwards = split.length > 16 && split[16].equalsIgnoreCase("true");
                                        final boolean show = split.length > 17 && split[17].equalsIgnoreCase("true");
                                        final boolean noNetwork = split.length > 18 && split[18].equalsIgnoreCase("true");
                                        final boolean random = split.length > 19 && split[19].equalsIgnoreCase("true");
                                        final boolean bungee = split.length > 20 && split[20].equalsIgnoreCase("true");
                                        final Portal portal = new Portal(topLeft, modX, modZ, rotX, sign, button, dest, name,false, network, gate, owner, hidden, alwaysOn, priv, free, backwards, show, noNetwork, random, bungee);
                                        portal.register();
                                        portal.close(true);
                                    }
                                }
                            }
                        }
                    }
                }
                int OpenCount = 0;
                final Iterator<Portal> iter = Portal.allPortals.iterator();
                while (iter.hasNext()) {
                    final Portal portal2 = iter.next();
                    if (portal2 == null) {
                        continue;
                    }
                    if (!portal2.wasVerified()) {
                        if (!portal2.isVerified() || !portal2.checkIntegrity()) {
                            for (final RelativeBlockVector control : portal2.getGate().getControls()) {
                                if (portal2.getBlockAt(control).getBlock().getType() != portal2.getGate().getControlBlock()) {
                                    Stargate.debug("loadAllGates", "Control Block Type == " + portal2.getBlockAt(control).getBlock().getType());
                                }
                            }
                            portal2.unregister(false);
                            iter.remove();
                            Stargate.log.log(Level.INFO, "[Stargate] Destroying stargate at {0}", portal2.toString());
                            continue;
                        }
                        portal2.drawSign();
                        ++portalCount;
                    }
                    if (!portal2.isFixed()) {
                        continue;
                    }
                    if (Stargate.enableBungee && portal2.isBungee()) {
                        ++OpenCount;
                        portal2.open(true);
                        portal2.drawSign();
                    }
                    else {
                        final Portal dest2 = portal2.getDestination();
                        if (dest2 == null) {
                            continue;
                        }
                        if (portal2.isAlwaysOn()) {
                            portal2.open(true);
                            ++OpenCount;
                        }
                        portal2.drawSign();
                        dest2.drawSign();
                    }
                }
                Stargate.log.log(Level.INFO, "[Stargate] '{'{0}'}' Loaded {1} stargates with {2} set as always-on", new Object[] { world.getName(), portalCount, OpenCount });
            }
            catch (Exception e) {
                e.printStackTrace();
                Stargate.log.log(Level.SEVERE, "Exception while reading stargates from {0}: {1}", new Object[] { db.getName(), l });
            }
        }
        else {
            Stargate.log.log(Level.INFO, "[Stargate] '{'{0}'}' No stargates for world ", world.getName());
        }
    }
    
    public static void closeAllGates() {
        Stargate.log.info("Closing all stargates.");
        for (final Portal p : Portal.allPortals) {
            if (p == null) {
                continue;
            }
            p.close(true);
        }
    }
    
    public static String filterName(final String input) {
        return input.replaceAll("[\\|:#]", "").trim();
    }
    
    @Override
    public String toString() {
        return String.format("Portal [id=%s, network=%s name=%s, type=%s]", this.id, this.network, this.name, this.gate.getFilename());
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = 31 * result + ((this.name == null) ? 0 : this.name.hashCode());
        result = 31 * result + ((this.network == null) ? 0 : this.network.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final Portal other = (Portal)obj;
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        }
        else if (!this.name.equalsIgnoreCase(other.name)) {
            return false;
        }
        if (this.network == null) {
            return other.network == null;
        }
        else return this.network.equalsIgnoreCase(other.network);
    }
    
    static {
        lookupBlocks = new HashMap<>();
        lookupEntrances = new HashMap<>();
        lookupControls = new HashMap<>();
        allPortals = new ArrayList<>();
        allPortalsNet = new HashMap<>();
        lookupNamesNet = new HashMap<>();
        bungeePortals = new HashMap<>();
    }
}
