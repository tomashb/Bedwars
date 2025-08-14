package com.example.bedwars.shop;

import org.bukkit.potion.PotionType;

/** Metadata describing a custom potion from the shop. */
public record PotionSpec(PotionType type, int amplifier, int seconds, boolean hideParticles) {}
