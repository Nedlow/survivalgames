package hwnet.survivalgames.utils;

import static org.bukkit.ChatColor.DARK_GRAY;
import static org.bukkit.ChatColor.RED;
import static org.bukkit.ChatColor.GRAY;
import static org.bukkit.ChatColor.RESET;

import hwnet.survivalgames.SG;
import hwnet.survivalgames.handlers.Map;
import hwnet.survivalgames.handlers.VoteHandler;
import org.bukkit.Bukkit;
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
        List<String> centered = new ArrayList<>();
        for (String lines : newMOTD) {
            centered.add(ChatUtil.centerText(lines, 59));
        }
        String build = String.join("\n", centered);
        MOTD = ChatColor.translateAlternateColorCodes('&', build);
    }


    public static void setChatFormat(String format) {
        chatFormat = ChatColor.translateAlternateColorCodes('&', format);
    }

    public static String getFormat() {
        return chatFormat;
    }

    public static String formatTime(int timeInSeconds) {
        int minutes = Math.round(timeInSeconds / 60);
        int seconds = Math.round(timeInSeconds % 60);
        String min = String.format("%02d", minutes);
        String secs = String.format("%02d", seconds);
        return min + ":" + secs;
    }

    public static void sendVoteMenu(Player p) {
        sendMessage(p, ChatColor.translateAlternateColorCodes('&', "&6============= &bSurvivalGames: &eVoting &6============="));
        sendMessage(p, "Vote: [/vote <id>]");
        for (Map map : Map.getVoteMaps()) {
            ChatUtil.sendMessage(p, Map.getTempId(map) + " > " + map.getMapName() + " [" + VoteHandler.getVotesMap(map) + " votes]");
        }
        sendMessage(p, ChatColor.translateAlternateColorCodes('&', "&6================================================="));

    }

    public static String centerText(String str, int maxLength) {
        str = ChatColor.translateAlternateColorCodes('&', str);
        int length = str.length();
        int spaces = maxLength - length;
        String result = "";
        String space = "";
        for (int i = 0; i < spaces / 2; i++) {
            space += " ";
        }
        result = space + str + space;
        if (result.length() > maxLength) {
            result = result.substring(0, result.length());
        }
        return result;
    }

    public static String centerText(String str, int length, int maxLength) {
        str = ChatColor.translateAlternateColorCodes('&', str);
        int spaces = maxLength - length;
        String result = "";
        String space = "";
        for (int i = 0; i < spaces / 2; i++) {
            space += " ";
        }
        result = space + str + space;
        if (result.length() > maxLength) {
            result = result.substring(0, result.length());
        }
        return result;
    }


    public static void broadcast(String msg) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(prefix() + ChatColor.translateAlternateColorCodes('&', msg));
        }
    }

    public static void sendMessage(Player p, String msg) {
        p.sendMessage(prefix() + ChatColor.translateAlternateColorCodes('&', msg));
    }
    public static void sendMessage(CommandSender sender, String msg) {
        sender.sendMessage(prefix() + msg);
    }

    public static String prefix() {
        return prefix + GRAY;
    }

    public static void setPrefix(String newPrefix) {
        prefix = ChatColor.translateAlternateColorCodes('&', newPrefix) + RESET + GRAY + " ";
    }


}
