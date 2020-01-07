package com.RkCraft.Stargate;

public class RelativeBlockVector
{
    private int right;
    private int depth;
    private int distance;
    
    public RelativeBlockVector(final int right, final int depth, final int distance) {
        this.right = 0;
        this.depth = 0;
        this.distance = 0;
        this.right = right;
        this.depth = depth;
        this.distance = distance;
    }
    
    public int getRight() {
        return this.right;
    }
    
    public int getDepth() {
        return this.depth;
    }
    
    public int getDistance() {
        return this.distance;
    }
}
