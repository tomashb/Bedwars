package com.example.bedwars.util;
import org.bukkit.Bukkit; import org.bukkit.Location; import org.bukkit.World; import org.bukkit.configuration.ConfigurationSection;
import java.util.LinkedHashMap; import java.util.Map;
public class Loc {
    public static Location read(ConfigurationSection s){ World w=Bukkit.getWorld(s.getString("world","world")); return new Location(w, s.getDouble("x"), s.getDouble("y"), s.getDouble("z"), (float)s.getDouble("yaw",0.0), (float)s.getDouble("pitch",0.0)); }
    public static Location read(Map<?,?> s){ World w=Bukkit.getWorld(String.valueOf(s.containsKey("world")? s.get("world"): "world")); return new Location(w, toD(s.get("x")), toD(s.get("y")), toD(s.get("z")), (float)toD(s.containsKey("yaw")? s.get("yaw"): 0.0), (float)toD(s.containsKey("pitch")? s.get("pitch"): 0.0)); }
    private static double toD(Object o){ return o instanceof Number? ((Number)o).doubleValue(): Double.parseDouble(String.valueOf(o)); }
    public static Map<String,Object> write(Location l){ Map<String,Object> m=new LinkedHashMap<>(); m.put("world", l.getWorld().getName()); m.put("x", l.getX()); m.put("y", l.getY()); m.put("z", l.getZ()); m.put("yaw", l.getYaw()); m.put("pitch", l.getPitch()); return m; }
}
