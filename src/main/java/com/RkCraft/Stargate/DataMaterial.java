package com.RkCraft.Stargate;

import jdk.nashorn.internal.objects.annotations.Getter;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
public class DataMaterial {
    int type;
    @Nullable Material material;
    int portalType;

    public DataMaterial(@NotNull Material mat){
        this.material = mat;
        type = 0;
    }

    public DataMaterial(int inteager){
        this.portalType = inteager;
        type = 1;
    }

    public Integer getPortalType() {
        return portalType;
    }

    @Nullable
    public int getType() {
        return type;
    }
    @Nullable
    public Material getMaterial() {
        return material;
    }
}
