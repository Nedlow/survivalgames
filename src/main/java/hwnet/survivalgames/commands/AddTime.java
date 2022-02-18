package hwnet.survivalgames.commands;

import hwnet.survivalgames.SG;
import hwnet.survivalgames.SettingsManager;
import hwnet.survivalgames.handlers.Map;
import hwnet.survivalgames.utils.ChatUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class AddTime implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender.hasPermission("sg.admin")) {
            if (args.length != 1) {
                return true;
            }

            int convToInt = Integer.valueOf(args[0]);
            SG.pretime = convToInt * 60;
            ChatUtil.sendMessage(sender, "Set countdown time to " + convToInt + " minutes.");

        } else {
            sender.sendMessage(ChatColor.RED + "No permission");
            return true;
        }
        return false;
    }
}
