package com.example.bedwars.game;

/** Settings for arena boundaries separating horizontal limits from void kill height. */
public record ArenaBoundaryPolicy(int minY, int voidKillY) {}
