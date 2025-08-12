package com.example.bedwars.generator;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a single resource generator in an arena.
 */
public class Generator {

    private final GeneratorType type;
    private final Location location;
    private final int intervalTicks;
    private final int amount;
    private int counter;

    public Generator(GeneratorType type, Location location, int intervalTicks, int amount) {
        this.type = type;
        this.location = location;
        this.intervalTicks = intervalTicks;
        this.amount = amount;
        this.counter = intervalTicks;
    }

    public void tick() {
        counter -= 20; // called once per second
        if (counter <= 0) {
            World world = location.getWorld();
            if (world != null) {
                world.dropItemNaturally(location, new ItemStack(type.getDrop(), amount));
            }
            counter = intervalTicks;
        }
    }

    public void reset() {
        this.counter = intervalTicks;
    }
}
