package hwnet.survivalgames.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import hwnet.survivalgames.SG;

public class ChestHandler {

    static ConsoleCommandSender clogger = SG.clogger;

    static FileConfiguration data = SG.config;
    static List<String> contents = data.getStringList("chests.contents");
    static List<ItemStack> items = new ArrayList<ItemStack>();

    private static void addItemsToList() {
        for (String con : contents) {
            String[] nCon = con.split(",");
            try {
                ItemStack is = new ItemStack(Material.valueOf(nCon[0]), Integer.valueOf(nCon[1]));
                items.add(is);
            } catch (Exception e) {
                clogger.sendMessage(
                        ChatColor.translateAlternateColorCodes('&', "&4String '" + nCon[0] + "' is not a material!"));
            }
        }
    }

    public static void fillAllChests(String world) {
        addItemsToList();
        for (Chunk chunk : SG.pl.getServer().getWorld(world).getLoadedChunks()) {
            for (BlockState entities : chunk.getTileEntities()) {
                if (entities instanceof Chest) {
                    Inventory inv = ((Chest) entities).getInventory();
                    fillChests(inv);
                }
            }
        }
    }

    private static void fillChests(Inventory inv) {
        inv.clear();
        int low = 2;
        int high = 7;
        Random rnd = new Random();

        for (int i = 0; i < rnd.nextInt(high - low) + low; i++) {
            int rand = rnd.nextInt(items.size());
            if (inv.contains(items.get(rand)))
                continue;
            if (items.get(rand).getType() == Material.DIAMOND) {
                Random rnd2 = new Random();
                if (rnd2.nextInt(4) == 4) {
                    inv.setItem(rnd.nextInt(27), items.get(rand));
                }
            } else {
                inv.setItem(rnd.nextInt(27), items.get(rand));
            }
        }
    }
}