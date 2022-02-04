package hwnet.survivalgames.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hwnet.survivalgames.handlers.PointSystem;

public class Points implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length != 3) {
            sender.sendMessage(ChatColor.GRAY + "Usage: /points <add|remove|set> <player> <amount>");
            return true;
        }
        Player p = Bukkit.getPlayer(args[1]);
        if (args[0].equalsIgnoreCase("add")) {
            int amount = 0;
            try {
                amount = Integer.parseInt(args[2]);
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "amount is not a number!");
            }

            PointSystem.addPoints(p, amount);
        }
        if (args[0].equalsIgnoreCase("remove")) {
            int amount = 0;
            try {
                amount = Integer.parseInt(args[2]);
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "amount is not a number!");
            }

            PointSystem.removePoints(p, amount);
        }
        if (args[0].equalsIgnoreCase("set")) {
            int amount = 0;
            try {
                amount = Integer.parseInt(args[2]);
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "amount is not a number!");
            }

            PointSystem.setPoints(p, amount);
        }

        return true;
    }

}