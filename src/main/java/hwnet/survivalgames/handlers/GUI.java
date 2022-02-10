package hwnet.survivalgames.handlers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class GUI {

    public static Map<UUID, GUI> inventoriesByUUID = new HashMap<>();
    public static Map<UUID, UUID> openInventories = new HashMap<>();

    private Inventory yourInventory;
    private UUID uuid;
    private Map<Integer, YourGUIAction> actions;

    public GUI(int invSize, String invName) {
        uuid = UUID.randomUUID();
        yourInventory = Bukkit.createInventory(null, invSize, invName);
        actions = new HashMap<>();
        inventoriesByUUID.put(getUuid(), this);
    }


    public interface YourGUIAction {
        void click(Player player);
    }

    public UUID getUuid() {
        return uuid;
    }

    public Inventory getYourInventory() {
        return yourInventory;
    }

    public static Map<UUID, GUI> getInventoriesByUUID() {
        return inventoriesByUUID;
    }

    public static Map<UUID, UUID> getOpenInventories() {
        return openInventories;
    }

    public Map<Integer, YourGUIAction> getActions() {
        return actions;
    }


    public void setItem(int slot, ItemStack stack, YourGUIAction action) {
        yourInventory.setItem(slot, stack);
        if (action != null) {
            actions.put(slot, action);
        }
    }

    public void setItem(int slot, ItemStack stack) {
        setItem(slot, stack, null);
    }

    public void open(Player p) {
        p.openInventory(yourInventory);
        openInventories.put(p.getUniqueId(), getUuid());
    }

    public void delete() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            UUID u = openInventories.get(p.getUniqueId());
            if (u.equals(getUuid())) {
                p.closeInventory();
            }
        }
        inventoriesByUUID.remove(getUuid());
    }

}
