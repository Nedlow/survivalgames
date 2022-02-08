package hwnet.survivalgames.utils;

import static org.bukkit.ChatColor.DARK_GRAY;
import static org.bukkit.ChatColor.RED;
import static org.bukkit.ChatColor.GRAY;
import static org.bukkit.ChatColor.RESET;

import hwnet.survivalgames.SG;
import hwnet.survivalgames.handlers.Map;
import hwnet.survivalgames.handlers.VoteHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hwnet.survivalgames.handlers.Gamer;

import java.util.ArrayList;
import java.util.List;


public class ChatUtil {

    private static String prefix = DARK_GRAY + "[" + RED + "SG" + DARK_GRAY + "] ";

    private static String chatFormat, MOTD;


    public static String getMOTD() {
        return MOTD;
    }

    public static void setMOTD(List<String> newMOTD) {
        String build = String.join("\n", newMOTD);
        MOTD = ChatColor.translateAlternateColorCodes('&', build);
    }


    public static void setChatFormat(String format) {
        chatFormat = ChatColor.translateAlternateColorCodes('&', format);
    }

    public static String getFormat() {
        return chatFormat;
    }

    public static void sendVoteMenu(Player p) {
        sendMessage(p, ChatColor.translateAlternateColorCodes('&', "&6============= &bSurvivalGames: &eVoting &6============="));
        sendMessage(p, "Vote: [/vote <id>]");
        for (Map map : Map.getVoteMaps()) {
            ChatUtil.sendMessage(p, Map.getTempId(map) + " > " + map.getMapName() + " [" + VoteHandler.getVotesMap(map) + " votes]");
        }
        sendMessage(p, ChatColor.translateAlternateColorCodes('&', "&6=============================================="));

    }


    public static void broadcast(String msg) {
        for (Gamer g : Gamer.getGamers()) {
            g.getPlayer().sendMessage(prefix() + ChatColor.translateAlternateColorCodes('&', msg));
        }
    }

    public static void sendMessage(Player p, String msg) {
        p.sendMessage(prefix() + ChatColor.translateAlternateColorCodes('&', msg));
    }

    public static String prefix() {
        return prefix + GRAY;
    }

    public static void setPrefix(String newPrefix) {
        prefix = ChatColor.translateAlternateColorCodes('&', newPrefix) + RESET + GRAY + " ";
    }


    public static void sendMessage(CommandSender sender, String msg) {
        sender.sendMessage(prefix() + msg);
    }
}
