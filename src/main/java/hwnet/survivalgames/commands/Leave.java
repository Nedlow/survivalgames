package hwnet.survivalgames.commands;

import hwnet.survivalgames.handlers.Gamer;
import hwnet.survivalgames.utils.LocUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import hwnet.survivalgames.SG;

public class Leave implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can leave!");
            return true;
        }

        Player p = (Player) sender;

        LocUtil.teleportToLobby(p);
        if (Gamer.getGamer(p.getUniqueId()) != null) Gamer.getGamer(p).remove();

        return false;

    }

}