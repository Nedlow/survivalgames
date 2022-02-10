package hwnet.survivalgames.utils;

import hwnet.survivalgames.handlers.GUI;
import hwnet.survivalgames.handlers.Gamer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.HashMap;

public class SpectatorGUI extends GUI {

    private HashMap<Integer, ItemStack> items = new HashMap<>();

    public SpectatorGUI() {
        super(9 * 3, "Spectate Menu");

        /*
        setItem(4, new ItemStack(Material.DIAMOND), player -> {
            player.sendMessage("Well this is pretty cool, I'm definitely going to give nice feedback!!!");
            player.setHealth(0);
        });

         */
    }


}
