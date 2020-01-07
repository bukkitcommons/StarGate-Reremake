package com.RkCraft.Stargate;

import org.bukkit.block.*;
import org.bukkit.*;

public class Blox
{
    private final int x;
    private final int y;
    private final int z;
    private final World world;
    private Blox parent;
    
    public Blox(final World world, final int x, final int y, final int z) {
        this.parent = null;
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }
    
    public Blox(final Block block) {
        this.parent = null;
        this.x = block.getX();
        this.y = block.getY();
        this.z = block.getZ();
        this.world = block.getWorld();
    }
    
    public Blox(final Location location) {
        this.parent = null;
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.world = location.getWorld();
    }
    
    public Blox(final World world, final String string) {
        this.parent = null;
        final String[] split = string.split(",");
        this.x = Integer.parseInt(split[0]);
        this.y = Integer.parseInt(split[1]);
        this.z = Integer.parseInt(split[2]);
        this.world = world;
    }
    
    public Blox makeRelative(final int x, final int y, final int z) {
        return new Blox(this.world, this.x + x, this.y + y, this.z + z);
    }
    
    public Location makeRelativeLoc(final double x, final double y, final double z, final float rotX, final float rotY) {
        return new Location(this.world, this.x + x, this.y + y, this.z + z, rotX, rotY);
    }
    
    public Blox modRelative(final int right, final int depth, final int distance, final int modX, final int modY, final int modZ) {
        return this.makeRelative(-right * modX + distance * modZ, -depth * modY, -right * modZ + -distance * modX);
    }
    
    public Location modRelativeLoc(final double right, final double depth, final double distance, final float rotX, final float rotY, final int modX, final int modY, final int modZ) {
        return this.makeRelativeLoc(0.5 + -right * modX + distance * modZ, depth, 0.5 + -right * modZ + -distance * modX, rotX, 0.0f);
    }
    
    public void setType(final Material type) {
        this.world.getBlockAt(this.x, this.y, this.z).setType(type);
    }
    
    public Material getType() {
        return this.world.getBlockAt(this.x, this.y, this.z).getType();
    }
    
    public int getData() {
        return this.world.getBlockAt(this.x, this.y, this.z).getData();
    }
    
    public Block getBlock() {
        return this.world.getBlockAt(this.x, this.y, this.z);
    }
    
    public int getX() {
        return this.x;
    }
    
    public int getY() {
        return this.y;
    }
    
    public int getZ() {
        return this.z;
    }
    
    public World getWorld() {
        return this.world;
    }
    
    public Block getParent() {
        if (this.parent == null) {
            this.findParent();
        }
        if (this.parent == null) {
            return null;
        }
        return this.parent.getBlock();
    }
    
    private void findParent() {
        int offsetX = 0;
        int offsetY = 0;
        int offsetZ = 0;
        if (Tag.WALL_SIGNS.isTagged(this.getBlock().getType())) {
            if (this.getData() == 2) {
                offsetZ = 1;
            }
            else if (this.getData() == 3) {
                offsetZ = -1;
            }
            else if (this.getData() == 4) {
                offsetX = 1;
            }
            else if (this.getData() == 5) {
                offsetX = -1;
            }
        }
        else {
            if (Tag.SIGNS.isTagged(this.getBlock().getType())) {
                return;
            }
            offsetY = -1;
        }
        this.parent = new Blox(this.world, this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ);
    }
    
    @Override
    public String toString() {
        return String.valueOf(this.x) +
                ',' +
                this.y +
                ',' +
                this.z;
    }
    
    @Override
    public int hashCode() {
        int result = 18;
        result = result * 27 + this.x;
        result = result * 27 + this.y;
        result = result * 27 + this.z;
        result = result * 27 + this.world.getName().hashCode();
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
        final Blox blox = (Blox)obj;
        return this.x == blox.x && this.y == blox.y && this.z == blox.z && this.world.getName().equals(blox.world.getName());
    }
}
