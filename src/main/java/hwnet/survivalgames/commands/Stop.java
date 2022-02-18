package hwnet.survivalgames.commands;

import hwnet.survivalgames.SG;
import hwnet.survivalgames.handlers.Map;
import hwnet.survivalgames.utils.ChatUtil;
import hwnet.survivalgames.utils.LocUtil;
import hwnet.survivalgames.utils.ResetMap;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Stop implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only users can edit arenas");
            return true;
        }

        ChatUtil.broadcast("Server will shutdown in 1 minute.");

        Bukkit.getScheduler().scheduleSyncDelayedTask(SG.pl, new Runnable() {
            @Override
            public void run() {
                Bukkit.shutdown();
            }
        }, 20L * 10);

        return false;
    }
}
