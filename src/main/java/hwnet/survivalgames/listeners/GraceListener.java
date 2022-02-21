package hwnet.survivalgames.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class GraceListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if ((e.getCurrentItem().getType() == Material.COMPASS) || (e.getCurrentItem().getType() == Material.PLAYER_HEAD)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent e) {
        if ((e.getItemDrop().getItemStack().getType() == Material.COMPASS) || (e.getItemDrop().getItemStack().getType() == Material.PLAYER_HEAD)) {
            e.getPlayer().sendMessage("It would not be wise to drop this item.");
            e.setCancelled(true);
        }
    }
}
