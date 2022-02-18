package hwnet.survivalgames.listeners;

import hwnet.survivalgames.handlers.GUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class GUIListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) e.getWhoClicked();
        UUID playerUUID = player.getUniqueId();

        UUID inventoryUUID = GUI.openInventories.get(playerUUID);
        if (inventoryUUID != null) {
            e.setCancelled(true);
            GUI gui = GUI.getInventoriesByUUID().get(inventoryUUID);
            GUI.YourGUIAction action = gui.getActions().get(e.getSlot());

            if (action != null) {
                action.click(player);
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        UUID playerUUID = player.getUniqueId();

        GUI.openInventories.remove(playerUUID);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        UUID playerUUID = player.getUniqueId();

        GUI.openInventories.remove(playerUUID);
    }
}
