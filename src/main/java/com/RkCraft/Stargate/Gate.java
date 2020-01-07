package com.RkCraft.Stargate;

import org.bukkit.*;
import java.util.logging.*;
import java.util.*;
import java.io.*;
import org.bukkit.block.*;

public class Gate
{
    public static final int ANYTHING = -1;
    public static final int ENTRANCE = -2;
    public static final int CONTROL = -3;
    public static final int EXIT = -4;
    private static final HashMap<String, Gate> gates;
    private static final HashMap<Integer, ArrayList<Gate>> controlBlocks;
    private static final HashSet<Integer> frameBlocks;
    private final String filename;
    private final Character[][] layout;
    private final HashMap<Character, Integer> types;
    private final HashMap<Character, Integer> metadata;
    private RelativeBlockVector[] entrances;
    private RelativeBlockVector[] border;
    private RelativeBlockVector[] controls;
    private RelativeBlockVector exitBlock;
    private final HashMap<RelativeBlockVector, Integer> exits;
    private int portalBlockOpen;
    private int portalBlockClosed;
    private int useCost;
    private int createCost;
    private int destroyCost;
    private boolean toOwner;
    
    public Gate(final String filename, final Character[][] layout, final HashMap<Character, Integer> types, final HashMap<Character, Integer> metadata) {
        this.entrances = new RelativeBlockVector[0];
        this.border = new RelativeBlockVector[0];
        this.controls = new RelativeBlockVector[0];
        this.exitBlock = null;
        this.exits = new HashMap<RelativeBlockVector, Integer>();
        this.portalBlockOpen = Material.PORTAL.getId();
        this.portalBlockClosed = Material.AIR.getId();
        this.useCost = -1;
        this.createCost = -1;
        this.destroyCost = -1;
        this.toOwner = false;
        this.filename = filename;
        this.layout = layout;
        this.metadata = metadata;
        this.types = types;
        this.populateCoordinates();
    }
    
    private void populateCoordinates() {
        final ArrayList<RelativeBlockVector> entranceList = new ArrayList<RelativeBlockVector>();
        final ArrayList<RelativeBlockVector> borderList = new ArrayList<RelativeBlockVector>();
        final ArrayList<RelativeBlockVector> controlList = new ArrayList<RelativeBlockVector>();
        final RelativeBlockVector[] relativeExits = new RelativeBlockVector[this.layout[0].length];
        final int[] exitDepths = new int[this.layout[0].length];
        RelativeBlockVector lastExit = null;
        for (int y = 0; y < this.layout.length; ++y) {
            for (int x = 0; x < this.layout[y].length; ++x) {
                final Integer id = this.types.get(this.layout[y][x]);
                if (this.layout[y][x] == '-') {
                    controlList.add(new RelativeBlockVector(x, y, 0));
                }
                if (id == -2 || id == -4) {
                    entranceList.add(new RelativeBlockVector(x, y, 0));
                    exitDepths[x] = y;
                    if (id == -4) {
                        this.exitBlock = new RelativeBlockVector(x, y, 0);
                    }
                }
                else if (id != -1) {
                    borderList.add(new RelativeBlockVector(x, y, 0));
                }
            }
        }
        for (int x2 = 0; x2 < exitDepths.length; ++x2) {
            relativeExits[x2] = new RelativeBlockVector(x2, exitDepths[x2], 0);
        }
        for (int x2 = relativeExits.length - 1; x2 >= 0; --x2) {
            if (relativeExits[x2] != null) {
                lastExit = relativeExits[x2];
            }
            else {
                relativeExits[x2] = lastExit;
            }
            if (exitDepths[x2] > 0) {
                this.exits.put(relativeExits[x2], x2);
            }
        }
        this.entrances = entranceList.toArray(this.entrances);
        this.border = borderList.toArray(this.border);
        this.controls = controlList.toArray(this.controls);
    }
    
    public void save(final String gateFolder) {
        try (final BufferedWriter bw = new BufferedWriter(new FileWriter(gateFolder + this.filename))) {
            this.writeConfig(bw, "portal-open", this.portalBlockOpen);
            this.writeConfig(bw, "portal-closed", this.portalBlockClosed);
            if (this.useCost != -1) {
                this.writeConfig(bw, "usecost", this.useCost);
            }
            if (this.createCost != -1) {
                this.writeConfig(bw, "createcost", this.createCost);
            }
            if (this.destroyCost != -1) {
                this.writeConfig(bw, "destroycost", this.destroyCost);
            }
            this.writeConfig(bw, "toowner", this.toOwner);
            for (final Character type : this.types.keySet()) {
                final Integer value = this.types.get(type);
                if (value < 0) {
                    continue;
                }
                bw.append((char)type);
                bw.append('=');
                bw.append((CharSequence)value.toString());
                final Integer mData = this.metadata.get(type);
                if (mData != null) {
                    bw.append(':');
                    bw.append((CharSequence)mData.toString());
                }
                bw.newLine();
            }
            bw.newLine();
            for (final Character[] arr$2 : this.layout) {
                final Character[] layout1 = arr$2;
                for (final Character symbol : arr$2) {
                    bw.append((char)symbol);
                }
                bw.newLine();
            }
        }
        catch (IOException ex) {
            Stargate.log.log(Level.SEVERE, "Could not save Gate {0} - {1}", new Object[] { this.filename, ex.getMessage() });
        }
    }
    
    private void writeConfig(final BufferedWriter bw, final String key, final int value) throws IOException {
        bw.append((CharSequence)String.format("%s=%d", key, value));
        bw.newLine();
    }
    
    private void writeConfig(final BufferedWriter bw, final String key, final boolean value) throws IOException {
        bw.append((CharSequence)String.format("%s=%b", key, value));
        bw.newLine();
    }
    
    public Character[][] getLayout() {
        return this.layout;
    }
    
    public HashMap<Character, Integer> getTypes() {
        return this.types;
    }
    
    public HashMap<Character, Integer> getMetaData() {
        return this.metadata;
    }
    
    public RelativeBlockVector[] getEntrances() {
        return this.entrances;
    }
    
    public RelativeBlockVector[] getBorder() {
        return this.border;
    }
    
    public RelativeBlockVector[] getControls() {
        return this.controls;
    }
    
    public HashMap<RelativeBlockVector, Integer> getExits() {
        return this.exits;
    }
    
    public RelativeBlockVector getExit() {
        return this.exitBlock;
    }
    
    public int getControlBlock() {
        return this.types.get('-');
    }
    
    public String getFilename() {
        return this.filename;
    }
    
    public int getPortalBlockOpen() {
        return this.portalBlockOpen;
    }
    
    public void setPortalBlockOpen(final int type) {
        this.portalBlockOpen = type;
    }
    
    public int getPortalBlockClosed() {
        return this.portalBlockClosed;
    }
    
    public void setPortalBlockClosed(final int type) {
        this.portalBlockClosed = type;
    }
    
    public int getUseCost() {
        if (this.useCost < 0) {
            return iConomyHandler.useCost;
        }
        return this.useCost;
    }
    
    public Integer getCreateCost() {
        if (this.createCost < 0) {
            return iConomyHandler.createCost;
        }
        return this.createCost;
    }
    
    public Integer getDestroyCost() {
        if (this.destroyCost < 0) {
            return iConomyHandler.destroyCost;
        }
        return this.destroyCost;
    }
    
    public Boolean getToOwner() {
        return this.toOwner;
    }
    
    public boolean matches(final Blox topleft, final int modX, final int modZ) {
        return this.matches(topleft, modX, modZ, false);
    }
    
    public boolean matches(final Blox topleft, final int modX, final int modZ, final boolean onCreate) {
        for (int y = 0; y < this.layout.length; ++y) {
            for (int x = 0; x < this.layout[y].length; ++x) {
                final int id = this.types.get(this.layout[y][x]);
                if (id == -2 || id == -4) {
                    if (!Stargate.ignoreEntrance) {
                        final int type = topleft.modRelative(x, y, 0, modX, 1, modZ).getType();
                        if (!onCreate || type != Material.AIR.getId()) {
                            if (type != this.portalBlockClosed && type != this.portalBlockOpen) {
                                if (this.portalBlockOpen == Material.WATER.getId() || this.portalBlockOpen == Material.STATIONARY_WATER.getId()) {
                                    if (type == Material.WATER.getId()) {
                                        continue;
                                    }
                                    if (type == Material.STATIONARY_WATER.getId()) {
                                        continue;
                                    }
                                }
                                if (this.portalBlockOpen == Material.LAVA.getId() || this.portalBlockOpen == Material.STATIONARY_LAVA.getId()) {
                                    if (type == Material.LAVA.getId()) {
                                        continue;
                                    }
                                    if (type == Material.STATIONARY_LAVA.getId()) {
                                        continue;
                                    }
                                }
                                Stargate.debug("Gate::Matches", "Entrance/Exit Material Mismatch: " + type);
                                return false;
                            }
                        }
                    }
                }
                else if (id != -1) {
                    if (topleft.modRelative(x, y, 0, modX, 1, modZ).getType() != id) {
                        Stargate.debug("Gate::Matches", "Block Type Mismatch: " + topleft.modRelative(x, y, 0, modX, 1, modZ).getType() + " != " + id);
                        return false;
                    }
                    final Integer mData = this.metadata.get(this.layout[y][x]);
                    if (mData != null && topleft.modRelative(x, y, 0, modX, 1, modZ).getData() != mData) {
                        Stargate.debug("Gate::Matches", "Block Data Mismatch: " + topleft.modRelative(x, y, 0, modX, 1, modZ).getData() + " != " + mData);
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    public static void registerGate(final Gate gate) {
        Gate.gates.put(gate.getFilename(), gate);
        final int blockID = gate.getControlBlock();
        if (!Gate.controlBlocks.containsKey(blockID)) {
            Gate.controlBlocks.put(blockID, new ArrayList<Gate>());
        }
        Gate.controlBlocks.get(blockID).add(gate);
    }
    
    public static Gate loadGate(final File file) {
        Scanner scanner = null;
        boolean designing = false;
        final ArrayList<ArrayList<Character>> design = new ArrayList<ArrayList<Character>>();
        final HashMap<Character, Integer> types = new HashMap<Character, Integer>();
        final HashMap<Character, Integer> metadata = new HashMap<Character, Integer>();
        final HashMap<String, String> config = new HashMap<String, String>();
        final HashSet<Integer> frameTypes = new HashSet<Integer>();
        int cols = 0;
        types.put('.', -2);
        types.put('*', -4);
        types.put(' ', -1);
        try {
            scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                final String line = scanner.nextLine();
                if (designing) {
                    final ArrayList<Character> row = new ArrayList<Character>();
                    if (line.length() > cols) {
                        cols = line.length();
                    }
                    for (final Character symbol : line.toCharArray()) {
                        if (symbol.equals('?') || !types.containsKey(symbol)) {
                            Stargate.log.log(Level.SEVERE, "Could not load Gate {0} - Unknown symbol ''{1}'' in diagram", new Object[] { file.getName(), symbol });
                            return null;
                        }
                        row.add(symbol);
                    }
                    design.add(row);
                }
                else if (line.isEmpty() || !line.contains("=")) {
                    designing = true;
                }
                else {
                    String[] split = line.split("=");
                    final String key = split[0].trim();
                    String value = split[1].trim();
                    if (key.length() == 1) {
                        final Character symbol2 = key.charAt(0);
                        if (value.contains(":")) {
                            split = value.split(":");
                            value = split[0].trim();
                            final String mData = split[1].trim();
                            metadata.put(symbol2, Integer.parseInt(mData));
                        }
                        final Integer id = Integer.parseInt(value);
                        types.put(symbol2, id);
                        frameTypes.add(id);
                    }
                    else {
                        config.put(key, value);
                    }
                }
            }
        }
        catch (FileNotFoundException ex2) {}
        catch (NumberFormatException ex) {
            Stargate.log.log(Level.SEVERE, "Could not load Gate {0} - Invalid block ID given", file.getName());
            return null;
        }
        finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        final Character[][] layout = new Character[design.size()][cols];
        for (int y = 0; y < design.size(); ++y) {
            final ArrayList<Character> row2 = design.get(y);
            final Character[] result = new Character[cols];
            for (int x = 0; x < cols; ++x) {
                if (x < row2.size()) {
                    result[x] = row2.get(x);
                }
                else {
                    result[x] = ' ';
                }
            }
            layout[y] = result;
        }
        final Gate gate = new Gate(file.getName(), layout, types, metadata);
        gate.portalBlockOpen = readConfig(config, gate, file, "portal-open", gate.portalBlockOpen);
        gate.portalBlockClosed = readConfig(config, gate, file, "portal-closed", gate.portalBlockClosed);
        gate.useCost = readConfig(config, gate, file, "usecost", -1);
        gate.destroyCost = readConfig(config, gate, file, "destroycost", -1);
        gate.createCost = readConfig(config, gate, file, "createcost", -1);
        gate.toOwner = (config.containsKey("toowner") ? Boolean.valueOf(config.get("toowner")) : iConomyHandler.toOwner);
        if (gate.getControls().length != 2) {
            Stargate.log.log(Level.SEVERE, "Could not load Gate {0} - Gates must have exactly 2 control points.", file.getName());
            return null;
        }
        Gate.frameBlocks.addAll((Collection<?>)frameTypes);
        gate.save(file.getParent() + "/");
        return gate;
    }
    
    private static int readConfig(final HashMap<String, String> config, final Gate gate, final File file, final String key, final int def) {
        if (config.containsKey(key)) {
            try {
                return Integer.parseInt(config.get(key));
            }
            catch (NumberFormatException ex) {
                Stargate.log.log(Level.WARNING, String.format("%s reading %s: %s is not numeric", ex.getClass().getName(), file, key));
            }
        }
        return def;
    }
    
    public static void loadGates(final String gateFolder) {
        final File dir = new File(gateFolder);
        File[] files;
        if (dir.exists()) {
            files = dir.listFiles(new StargateFilenameFilter());
        }
        else {
            files = new File[0];
        }
        if (files.length == 0) {
            dir.mkdir();
            populateDefaults(gateFolder);
        }
        else {
            for (final File file : files) {
                final Gate gate = loadGate(file);
                if (gate != null) {
                    registerGate(gate);
                }
            }
        }
    }
    
    public static void populateDefaults(final String gateFolder) {
        final int Obsidian = Material.OBSIDIAN.getId();
        final Character[][] layout = { { ' ', 'X', 'X', ' ' }, { 'X', '.', '.', 'X' }, { '-', '.', '.', '-' }, { 'X', '*', '.', 'X' }, { ' ', 'X', 'X', ' ' } };
        final HashMap<Character, Integer> types = new HashMap<Character, Integer>();
        types.put('.', -2);
        types.put('*', -4);
        types.put(' ', -1);
        types.put('X', Obsidian);
        types.put('-', Obsidian);
        final HashMap<Character, Integer> metadata = new HashMap<Character, Integer>();
        final Gate gate = new Gate("nethergate.gate", layout, types, metadata);
        gate.save(gateFolder);
        registerGate(gate);
    }
    
    public static Gate[] getGatesByControlBlock(final Block block) {
        return getGatesByControlBlock(block.getTypeId());
    }
    
    public static Gate[] getGatesByControlBlock(final int type) {
        Gate[] result = new Gate[0];
        final ArrayList<Gate> lookup = Gate.controlBlocks.get(type);
        if (lookup != null) {
            result = lookup.toArray(result);
        }
        return result;
    }
    
    public static Gate getGateByName(final String name) {
        return Gate.gates.get(name);
    }
    
    public static int getGateCount() {
        return Gate.gates.size();
    }
    
    public static boolean isGateBlock(final int type) {
        return Gate.frameBlocks.contains(type);
    }
    
    public static void clearGates() {
        Gate.gates.clear();
        Gate.controlBlocks.clear();
        Gate.frameBlocks.clear();
    }
    
    static {
        gates = new HashMap<String, Gate>();
        controlBlocks = new HashMap<Integer, ArrayList<Gate>>();
        frameBlocks = new HashSet<Integer>();
    }
    
    static class StargateFilenameFilter implements FilenameFilter
    {
        @Override
        public boolean accept(final File dir, final String name) {
            return name.endsWith(".gate");
        }
    }
}
