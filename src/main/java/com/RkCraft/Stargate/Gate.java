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
    private static final HashMap<Material, ArrayList<Gate>> controlBlocks;
    private static final HashSet<Material> frameBlocks;
    private final String filename;
    private final Character[][] layout;
    private final HashMap<Character, DataMaterial> types;
    private RelativeBlockVector[] entrances;
    private RelativeBlockVector[] border;
    private RelativeBlockVector[] controls;
    private RelativeBlockVector exitBlock;
    private final HashMap<RelativeBlockVector, Integer> exits;
    private Material portalBlockOpen;
    private Material portalBlockClosed;
    private int useCost;
    private int createCost;
    private int destroyCost;
    private boolean toOwner;
    
    public Gate(final String filename, final Character[][] layout, final HashMap<Character, DataMaterial> types) {
        this.entrances = new RelativeBlockVector[0];
        this.border = new RelativeBlockVector[0];
        this.controls = new RelativeBlockVector[0];
        this.exitBlock = null;
        this.exits = new HashMap<>();
        this.portalBlockOpen = Material.NETHER_PORTAL;
        this.portalBlockClosed = Material.AIR;
        this.useCost = -1;
        this.createCost = -1;
        this.destroyCost = -1;
        this.toOwner = false;
        this.filename = filename;
        this.layout = layout;
        this.types = types;
        this.populateCoordinates();
    }
    
    private void populateCoordinates() {
        final ArrayList<RelativeBlockVector> entranceList = new ArrayList<>();
        final ArrayList<RelativeBlockVector> borderList = new ArrayList<>();
        final ArrayList<RelativeBlockVector> controlList = new ArrayList<>();
        final RelativeBlockVector[] relativeExits = new RelativeBlockVector[this.layout[0].length];
        final int[] exitDepths = new int[this.layout[0].length];
        RelativeBlockVector lastExit = null;
        for (int y = 0; y < this.layout.length; ++y) {
            for (int x = 0; x < this.layout[y].length; ++x) {
                final DataMaterial id = this.types.get(this.layout[y][x]);
                if (this.layout[y][x] == '-') {
                    controlList.add(new RelativeBlockVector(x, y, 0));
                }
                if(id.type == 1){
                    if (id.portalType == ENTRANCE || id.portalType == EXIT) {
                        entranceList.add(new RelativeBlockVector(x, y, 0));
                        exitDepths[x] = y;
                        if (id.portalType == EXIT) {
                            this.exitBlock = new RelativeBlockVector(x, y, 0);
                        }
                    }
                }else if (id.portalType != ANYTHING) {
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
            this.writeConfig(bw, "portal-open", String.valueOf(this.portalBlockOpen));
            this.writeConfig(bw, "portal-closed", String.valueOf(this.portalBlockClosed));
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
                final Material value = this.types.get(type).material;
                if (value == null) {
                    continue;
                }
                bw.append(type);
                bw.append('=');
                bw.append(value.toString());
                bw.newLine();
            }
            bw.newLine();
            for (final Character[] arr$2 : this.layout) {
                final Character[] layout1 = arr$2;
                for (final Character symbol : arr$2) {
                    bw.append(symbol);
                }
                bw.newLine();
            }
        }
        catch (IOException ex) {
            Stargate.log.log(Level.SEVERE, "Could not save Gate {0} - {1}", new Object[] { this.filename, ex.getMessage() });
        }
    }
    
    private void writeConfig(final BufferedWriter bw, final String key, final String  value) throws IOException {
        bw.append(String.format("%s=%s", key, value));
        bw.newLine();
    }
    private void writeConfig(final BufferedWriter bw, final String key, final int value) throws IOException {
        bw.append(String.format("%s=%s", key, value));
        bw.newLine();
    }

    private void writeConfig(final BufferedWriter bw, final String key, final boolean value) throws IOException {
        bw.append(String.format("%s=%b", key, value));
        bw.newLine();
    }
    
    public Character[][] getLayout() {
        return this.layout;
    }
    
    public HashMap<Character,DataMaterial> getTypes() {
        return this.types;
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
    
    public Material getControlBlock() {
        return this.types.get('-').material;
    }
    
    public String getFilename() {
        return this.filename;
    }
    
    public Material getPortalBlockOpen() {
        return this.portalBlockOpen;
    }
    
    public void setPortalBlockOpen(final Material type) {
        this.portalBlockOpen = type;
    }
    
    public Material getPortalBlockClosed() {
        return this.portalBlockClosed;
    }
    
    public void setPortalBlockClosed(final Material type) {
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
        for (int y = 0; y < layout.length; y++) {
            for (int x = 0; x < layout[y].length; x++) {
                DataMaterial id = types.get(layout[y][x]);

                if (id.portalType == ENTRANCE || id.portalType == EXIT) {
                    // TODO: Remove once snowmanTrailEvent is added
                    if (Stargate.ignoreEntrance) continue;

                    Material type = topleft.modRelative(x, y, 0, modX, 1, modZ).getType();

                    // Ignore entrance if it's air and we're creating a new gate
                    if (onCreate && type == Material.AIR) continue;

                    if (type != portalBlockClosed && type != portalBlockOpen) {
                        // Special case for water gates
                        if (portalBlockOpen == Material.WATER ) {
                            if (type == Material.WATER) {
                                continue;
                            }
                        }
                        // Special case for lava gates
                        if (portalBlockOpen == Material.LAVA) {
                            if (type == Material.LAVA) {
                                continue;
                            }
                        }
                        Stargate.debug("Gate::Matches", "Entrance/Exit Material Mismatch: " + type);
                        return false;
                    }
                } else if (id.portalType != ANYTHING) {
                    if (topleft.modRelative(x, y, 0, modX, 1, modZ).getType() != id.material) {
                        Stargate.debug("Gate::Matches", "Block Type Mismatch: " + topleft.modRelative(x, y, 0, modX, 1, modZ).getType() + " != " + id);
                        return false;
                    }
                }
            }
        }

        return true;
    }
    
    public static void registerGate(final Gate gate) {
        Gate.gates.put(gate.getFilename(), gate);
        final Material blockID = gate.getControlBlock();
        if (!Gate.controlBlocks.containsKey(blockID)) {
            Gate.controlBlocks.put(blockID, new ArrayList<>());
        }
        Gate.controlBlocks.get(blockID).add(gate);
    }
    
    public static Gate loadGate(final File file) {
        Scanner scanner = null;
        boolean designing = false;
        final ArrayList<ArrayList<Character>> design = new ArrayList<>();
        final HashMap<Character, DataMaterial> types = new HashMap<>();
        final HashMap<String, String> config = new HashMap<>();
        final HashSet<Material> frameTypes = new HashSet<>();
        int cols = 0;
        types.put('.', new DataMaterial(-2));
        types.put('*', new DataMaterial(-4));
        types.put(' ', new DataMaterial(-1));
        try {
            scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                final String line = scanner.nextLine();
                if (designing) {
                    final ArrayList<Character> row = new ArrayList<>();
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
                        final Material id = Material.matchMaterial(value);
                        types.put(symbol2, new DataMaterial(id));
                        frameTypes.add(id);
                    }
                    else {
                        config.put(key, value);
                    }
                }
            }
        }
        catch (FileNotFoundException ignored) {}
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
        final Gate gate = new Gate(file.getName(), layout, types);
        gate.portalBlockOpen = Material.matchMaterial(readConfig(config, gate, file, "portal-open", gate.portalBlockOpen.name()));
        gate.portalBlockClosed = Material.matchMaterial(readConfig(config, gate, file, "portal-closed", gate.portalBlockClosed.name()));
        gate.useCost = readConfig(config, gate, file, "usecost", -1);
        gate.destroyCost = readConfig(config, gate, file, "destroycost", -1);
        gate.createCost = readConfig(config, gate, file, "createcost", -1);
        gate.toOwner = (config.containsKey("toowner") ? Boolean.parseBoolean(config.get("toowner")) : iConomyHandler.toOwner);
        if (gate.getControls().length != 2) {
            Stargate.log.log(Level.SEVERE, "Could not load Gate {0} - Gates must have exactly 2 control points.", file.getName());
            return null;
        }
        Gate.frameBlocks.addAll(frameTypes);
        gate.save(file.getParent() + "/");
        return gate;
    }
    
    private static String readConfig(final HashMap<String, String> config, final Gate gate, final File file, final String key, final String def) {
        if (config.containsKey(key)) {
            try {
                return config.get(key);
            }
            catch (NumberFormatException ex) {
                Stargate.log.log(Level.WARNING, String.format("%s reading %s: %s is not string", ex.getClass().getName(), file, key));
            }
        }
        return def;
    }
    private static int readConfig(final HashMap<String, String> config, final Gate gate, final File file, final String key, final int def) {
        if (config.containsKey(key)) {
            try {
                return Integer.parseInt(config.get(key));
            }
            catch (NumberFormatException ex) {
                Stargate.log.log(Level.WARNING, String.format("%s reading %s: %s is not int", ex.getClass().getName(), file, key));
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
        final Material Obsidian = Material.OBSIDIAN;
        final Character[][] layout = { { ' ', 'X', 'X', ' ' }, { 'X', '.', '.', 'X' }, { '-', '.', '.', '-' }, { 'X', '*', '.', 'X' }, { ' ', 'X', 'X', ' ' } };
        final HashMap<Character, DataMaterial> types = new HashMap<>();
        types.put('.', new DataMaterial(ENTRANCE));
        types.put('*', new DataMaterial(EXIT));
        types.put(' ', new DataMaterial(ANYTHING));
        types.put('X', new DataMaterial(Obsidian));
        types.put('-', new DataMaterial(Obsidian));
        final Gate gate = new Gate("nethergate.gate", layout, types);
        gate.save(gateFolder);
        registerGate(gate);
    }
    
    public static Gate[] getGatesByControlBlock(final Block block) {
        return getGatesByControlBlock(block);
    }
    
    public static Gate[] getGatesByControlBlock(final Material block) {
        Gate[] result = new Gate[0];
        final ArrayList<Gate> lookup = Gate.controlBlocks.get(block);
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
    
    public static boolean isGateBlock(final Material type) {
        return Gate.frameBlocks.contains(type);
    }
    
    public static void clearGates() {
        Gate.gates.clear();
        Gate.controlBlocks.clear();
        Gate.frameBlocks.clear();
    }
    
    static {
        gates = new HashMap<>();
        controlBlocks = new HashMap<>();
        frameBlocks = new HashSet<>();
    }
    
    static class StargateFilenameFilter implements FilenameFilter
    {
        @Override
        public boolean accept(final File dir, final String name) {
            return name.endsWith(".gate");
        }
    }
}