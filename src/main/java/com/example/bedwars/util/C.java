package com.example.bedwars.util;
import org.bukkit.ChatColor;
public class C {
    public static final String PREFIX = color("&6[BedWars]&r ");
    public static String color(String s){ return ChatColor.translateAlternateColorCodes('&', s); }
    public static String msg(String p){ return Configs.msg(p); }
    public static String msg(String p, String k, Object v){ return Configs.msg(p,k,v); }
    public static String msgRaw(String p, Object... kv){ return Configs.msgRaw(p,kv); }
}
