package hwnet.survivalgames.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hwnet.survivalgames.SG;
import hwnet.survivalgames.handlers.Gamer;
import hwnet.survivalgames.utils.ChatUtil;

public class Join implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can join SG.");
            return true;
        }
        Player p = (Player) sender;
        SG.spawnFireworks(p.getLocation(), 3);
        for (Player pl : Bukkit.getOnlinePlayers()) {
            pl.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 20, 1);
        }
        Gamer g = Gamer.getGamer(p);
        System.out.println("Added " + g.getName() + " to gamers.");
        ChatUtil.sendMessage(g.getPlayer(), "Joined game.");

        return false;
    }

}
