package com.RkCraft.Stargate;

import org.bukkit.Material;

public class BloxPopulator
{
    private Blox blox;
    private Material nextMat;
    
    public BloxPopulator(final Blox b, final Material m) {
        this.blox = b;
        this.nextMat = m;
    }

    public void setBlox(final Blox b) {
        this.blox = b;
    }
    
    public void setMat(final Material m) {
        this.nextMat = m;
    }
    
    public Blox getBlox() {
        return this.blox;
    }
    
    public Material getMat() {
        return this.nextMat;
    }
}
