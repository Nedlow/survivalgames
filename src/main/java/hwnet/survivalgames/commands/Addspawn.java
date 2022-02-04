package hwnet.survivalgames.commands;

import hwnet.survivalgames.handlers.Map;
import hwnet.survivalgames.utils.ChatUtil;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import hwnet.survivalgames.SettingsManager;

public class Addspawn implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player))
            return true;

        Player p = (Player) sender;
        if (p.hasPermission("sg.admin")) {
            if (args.length == 0) {
                p.sendMessage(ChatColor.RED + "Usage: /addspawn <filename> <index>");
            } else {
                if (args[1].equalsIgnoreCase("center")) {
                    FileConfiguration data = SettingsManager.getInstance().getData();
                    data.set("arenas." + args[0] + ".center.x", p.getLocation().getX());
                    data.set("arenas." + args[0] + ".center.y", p.getLocation().getY());
                    data.set("arenas." + args[0] + ".center.z", p.getLocation().getZ());
                    SettingsManager.getInstance().saveData();
                    ChatUtil.sendMessage(p,"Center location set at " + p.getLocation().getX() + " " + p.getLocation().getY()
                            + " " + p.getLocation().getZ());
                    World w = p.getWorld();
                    w.setSpawnLocation(p.getLocation());
                    return true;
                }
                FileConfiguration data = SettingsManager.getInstance().getData();
                data.set("arenas." + args[0] + ".spawns." + args[1] + ".x", p.getLocation().getX());
                data.set("arenas." + args[0] + ".spawns." + args[1] + ".y", p.getLocation().getY());
                data.set("arenas." + args[0] + ".spawns." + args[1] + ".z", p.getLocation().getZ());
                data.set("arenas." + args[0] + ".spawns." + args[1] + ".yaw", p.getLocation().getYaw());
                data.set("arenas." + args[0] + ".spawns." + args[1] + ".pitch", p.getLocation().getPitch());
                SettingsManager.getInstance().saveData();
                ChatUtil.sendMessage(p, "Spawn '" + args[1] + "' set at " + p.getLocation().getX() + " " + p.getLocation().getY()
                        + " " + p.getLocation().getZ());
                return true;
            }

        } else {
            ChatUtil.sendMessage(p, ChatColor.RED + "No permission");
            return true;
        }

        return false;
    }

}