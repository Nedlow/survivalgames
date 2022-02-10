package hwnet.survivalgames.commands;

import hwnet.survivalgames.GameState;
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

        if (Gamer.getGamer(p.getUniqueId()) != null) {
            ChatUtil.sendMessage(p, "You are already participating!");
            return true;
        }

        if (Gamer.getRealGamers().size() == SG.maxPlayers) {
            ChatUtil.sendMessage(p, "Could not join game. Reason: Game is full!");
            return true;
        }
        Gamer.getGamer(p).setSpectator(false);
        ChatUtil.sendMessage(p, "Joined game.");
        ChatUtil.sendVoteMenu(p);
        return false;
    }

}
