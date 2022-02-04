package hwnet.survivalgames.commands;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import hwnet.survivalgames.SG;
import hwnet.survivalgames.utils.ChatUtil;

public class Lobby implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can set lobby!");
            return true;
        }
        Player p = (Player) sender;

        if (p.hasPermission("sg.admin")) {
            World w = p.getWorld();
            double x = p.getLocation().getX();
            double y = p.getLocation().getY();
            double z = p.getLocation().getZ();
            float pitch = p.getLocation().getPitch();
            float yaw = p.getLocation().getYaw();
            FileConfiguration config = SG.config;
            ConfigurationSection lobbySec = config.getConfigurationSection("lobby");
            lobbySec.set("world", w.getName());
            lobbySec.set("x", x);
            lobbySec.set("y", y);
            lobbySec.set("z", z);
            lobbySec.set("pitch", pitch);
            lobbySec.set("yaw", yaw);
            lobbySec.set("enabled", true);
            SG.pl.saveConfig();
            ChatUtil.sendMessage(p, "Lobby set.");


        } else {
            ChatUtil.sendMessage(p, ChatColor.RED + "No permission.");
        }

        return false;
    }

}
