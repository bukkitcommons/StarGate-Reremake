package com.RkCraft.Stargate;

public class BloxPopulator
{
    private Blox blox;
    private int nextMat;
    private byte nextData;
    
    public BloxPopulator(final Blox b, final int m) {
        this.blox = b;
        this.nextMat = m;
        this.nextData = 0;
    }
    
    public BloxPopulator(final Blox b, final int m, final byte d) {
        this.blox = b;
        this.nextMat = m;
        this.nextData = d;
    }
    
    public void setBlox(final Blox b) {
        this.blox = b;
    }
    
    public void setMat(final int m) {
        this.nextMat = m;
    }
    
    public void setData(final byte d) {
        this.nextData = d;
    }
    
    public Blox getBlox() {
        return this.blox;
    }
    
    public int getMat() {
        return this.nextMat;
    }
    
    public byte getData() {
        return this.nextData;
    }
}
