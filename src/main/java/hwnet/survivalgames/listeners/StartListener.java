package hwnet.survivalgames.listeners;

import hwnet.survivalgames.handlers.Gamer;
import hwnet.survivalgames.utils.ChatUtil;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.server.ServerListPingEvent;

public class StartListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (Gamer.getGamer(e.getPlayer()).isAlive() && !Gamer.getGamer(e.getPlayer()).isSpectator()) {
            if (e.getTo().getX() == e.getFrom().getX() && e.getTo().getZ() == e.getFrom().getZ()) return;
            e.setCancelled(true);
            e.getPlayer().teleport(e.getFrom());
        }
    }

    @EventHandler
    public void onMOTD(ServerListPingEvent e) {
        e.setMotd(ChatUtil.getMOTD());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if ((e.getCurrentItem().getType() == Material.COMPASS) || (e.getCurrentItem().getType() == Material.PLAYER_HEAD)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (Gamer.getGamer(e.getPlayer()).isSpectator() || !Gamer.getGamer(e.getPlayer()).isAlive()) {
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
