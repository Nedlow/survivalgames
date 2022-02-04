package hwnet.survivalgames.utils;

import static org.bukkit.ChatColor.DARK_GRAY;
import static org.bukkit.ChatColor.RED;
import static org.bukkit.ChatColor.GRAY;
import static org.bukkit.ChatColor.RESET;

import hwnet.survivalgames.SG;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hwnet.survivalgames.handlers.Gamer;


public class ChatUtil {

    private static String prefix = DARK_GRAY + "[" + RED + "SG" + DARK_GRAY + "] ";

    public static void broadcast(String msg) {
        for (Gamer g : Gamer.getGamers()) {
            g.getPlayer().sendMessage(prefix() + ChatColor.translateAlternateColorCodes('&', msg));
        }
    }

    public static void sendMessage(Player p, String msg) {
        p.sendMessage(prefix() + msg);
    }

    public static String prefix() {
        return prefix + GRAY;
    }

    public static void setPrefix(String newPrefix) {
        prefix = ChatColor.translateAlternateColorCodes('&', newPrefix) + RESET + GRAY;
    }


    public static void sendMessage(CommandSender sender, String msg) {
        sender.sendMessage(prefix() + msg);
    }

}
