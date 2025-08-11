package com.example.bedwars.util;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList; import java.util.Arrays; import java.util.List;
public class ItemBuilder {
    private final ItemStack item; private final List<String> lore = new ArrayList<>();
    public ItemBuilder(Material m){ this.item=new ItemStack(m); }
    public ItemBuilder name(String n){ ItemMeta meta=item.getItemMeta(); meta.setDisplayName(n); item.setItemMeta(meta); return this; }
    public ItemBuilder lore(String... lines){ lore.addAll(Arrays.asList(lines)); return this; }
    public ItemBuilder lore(List<String> lines){ lore.addAll(lines); return this; }
    public ItemStack build(){ if (!lore.isEmpty()){ ItemMeta meta=item.getItemMeta(); meta.setLore(lore); item.setItemMeta(meta);} return item; }
}
