package hwnet.survivalgames.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import hwnet.survivalgames.SettingsManager;
import hwnet.survivalgames.handlers.Map;

public class Addmap implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender.hasPermission("sg.admin")) {
            if (args.length != 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /addarena <filename> <name>");
            } else {
                FileConfiguration config = SettingsManager.getInstance().getData();
                config.set("arenas." + args[0] + ".name", args[1]);
                config.set("arenas." + args[0] + ".gracetime", 30);
                config.set("arenas." + args[0] + ".gametime", 60);
                SettingsManager.getInstance().saveData();
                Map map = new Map(args[1], args[0]);
                sender.sendMessage("Map created: " + map.getMapName());
            }
        } else {
            sender.sendMessage(ChatColor.RED + "No permission");
            return true;
        }

        return false;
    }

}
