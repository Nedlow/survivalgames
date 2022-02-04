package hwnet.survivalgames.commands;

import hwnet.survivalgames.SG;
import hwnet.survivalgames.utils.ScoreboardUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Spectate implements CommandExecutor {

    @Override
    public boolean onCommand(
            CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            SG.SBU.addToTeam((Player) sender, args[0]);
        }
        return false;
    }
}


/*
Player p = (Player) sender;
            if (Bukkit.getPlayer(args[0]) != null) {
                p.setGameMode(GameMode.SPECTATOR);
                p.setSpectatorTarget(Bukkit.getPlayer(args[0]));
                p.sendMessage(
                        ChatColor.GREEN
                                + "You're now spectating "
                                + ChatColor.BLUE
                                + args[0]
                                + ChatColor.GREEN
                                + ".");
            } else {
                p.sendMessage(
                        ChatColor.DARK_RED
                                + "Player "
                                + ChatColor.BLUE
                                + args[0]
                                + ChatColor.DARK_RED
                                + " isn't online.");
            }
            return true;
 */