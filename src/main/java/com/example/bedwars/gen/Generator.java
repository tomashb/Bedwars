package com.example.bedwars.gen;
import org.bukkit.Location;
public class Generator { private final GeneratorType type; private final Location location; private int tier;
    public Generator(GeneratorType t, Location l, int tier){ this.type=t; this.location=l; this.tier=tier; }
    public GeneratorType getType(){ return type; } public Location getLocation(){ return location; } public int getTier(){ return tier; } public void setTier(int t){ this.tier=t; }
}
