package hwnet.survivalgames.commands;

import hwnet.survivalgames.SG;
import hwnet.survivalgames.utils.ChatUtil;
import hwnet.survivalgames.utils.LocUtil;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class Spawn implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can teleport to lobby!");
            return true;
        }
        Player p = (Player) sender;

        LocUtil.teleportToLobby(p);

        return false;
    }
}
