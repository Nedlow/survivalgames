package hwnet.survivalgames.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import hwnet.survivalgames.SG;
import org.bukkit.inventory.meta.ItemMeta;

public class ChestHandler {

    static ConsoleCommandSender clogger = SG.clogger;

    static FileConfiguration data = SG.config;
    static List<String> contents = data.getStringList("chests.contents");
    static List<ItemStack> items = new ArrayList<ItemStack>();

    private static void addItemsToList() {
        for (String con : contents) {
            String[] nCon = con.split(",");
            try {
                ItemStack is = new ItemStack(Material.getMaterial(nCon[0]), Integer.valueOf(nCon[1].trim()));

                if (nCon.length > 2) {
                    ItemMeta meta = is.getItemMeta();
                    Enchantment ench = EnchantmentWrapper.getByKey(NamespacedKey.minecraft(nCon[2].trim().toLowerCase()));
                    if (ench == null) {
                        clogger.sendMessage("Enchantment is null: " + nCon[2]);
                    }
                    if (nCon.length == 3) {
                        is.addUnsafeEnchantment(ench, 1);
                        meta.addEnchant(ench, 1, false);
                    } else if (nCon.length == 4) {
                        is.addUnsafeEnchantment(ench, Integer.valueOf(nCon[3].trim()));
                        meta.addEnchant(ench, Integer.valueOf(nCon[3].trim()), false);
                    } else if (nCon.length == 5) {
                        is.addUnsafeEnchantment(ench, Integer.valueOf(nCon[3].trim()));
                        meta.addEnchant(ench, Integer.valueOf(nCon[3].trim()), false);
                        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', nCon[4].trim()));
                    }
                    is.setItemMeta(meta);
                }

                items.add(is);
            } catch (Exception e) {
                clogger.sendMessage("Enchantment info: " + nCon[2]);
                e.printStackTrace();
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
        int low = 3;
        int high = 9;
        Random rnd = new Random();

        for (int i = 0; i < rnd.nextInt(high - low) + low; i++) {
            int rand = rnd.nextInt(items.size());
            if (inv.contains(items.get(rand))) continue;
            inv.setItem(rnd.nextInt(27), items.get(rand));
        }
    }
}