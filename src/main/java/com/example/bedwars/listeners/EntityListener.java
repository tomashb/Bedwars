package com.example.bedwars.listeners;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class EntityListener implements Listener {
    @EventHandler public void onNpcDamage(EntityDamageByEntityEvent e){ if (e.getEntityType()==EntityType.VILLAGER) e.setCancelled(true); }
}
