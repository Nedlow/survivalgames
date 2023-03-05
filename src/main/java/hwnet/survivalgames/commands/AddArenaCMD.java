package hwnet.survivalgames.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import hwnet.survivalgames.SettingsManager;
import hwnet.survivalgames.handlers.Map;

public class AddArenaCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender.hasPermission("sg.admin")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /addarena <filename> <name>");
            } else {
                String mapname = "";
                for (int i = 1; i < args.length; i++) {
                    if (i == args.length - 1) {
                        mapname += (args[i]) + "";
                    } else mapname += (args[i]) + " ";
                }
                FileConfiguration config = SettingsManager.getInstance().getData();
                config.set("arenas." + args[0] + ".name", mapname);
                config.set("arenas." + args[0] + ".gracetime", 30);
                config.set("arenas." + args[0] + ".gametime", 60);
                SettingsManager.getInstance().saveData();

                Map map = new Map(mapname.trim(), args[0]);
                sender.sendMessage("Map created: " + map.getMapName());
            }
        } else {
            sender.sendMessage(ChatColor.RED + "No permission");
            return true;
        }
        return false;
    }
}
