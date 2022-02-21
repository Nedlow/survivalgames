package hwnet.survivalgames.commands;

import hwnet.survivalgames.utils.ChatUtil;
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

        Player p = (Player) sender;

        boolean status = PointSystem.load(p);

        if (status) {
            ChatUtil.sendMessage(p, "Loaded your stats.");
        } else {
            ChatUtil.sendMessage(p, "Could not load your stats.");
        }
        return false;
    }

}