package hwnet.survivalgames.commands;

import hwnet.survivalgames.handlers.Gamer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hwnet.survivalgames.handlers.Map;
import hwnet.survivalgames.handlers.VoteHandler;
import hwnet.survivalgames.utils.ChatUtil;

public class Vote implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player))
            return true;

        Player p = (Player) sender;

        if (Gamer.getGamer(p.getUniqueId()) == null) {
            ChatUtil.sendMessage(p, ChatColor.RED + "You are not currently participating! Do /join first.");
            return true;
        }

        if (args.length == 0) {
            ChatUtil.sendVoteMenu(p);
        }
        if (args.length == 1) {

            if (VoteHandler.hasVoted(p.getName())) {
                ChatUtil.sendMessage(p, ChatColor.RED + "You have already voted!");
                return true;
            }
            int id = Integer.parseInt(args[0]) - 1;
            Map map = Map.getMapById(id);
            if (map == null) {
                ChatUtil.sendMessage(p, ChatColor.RED + "That map doesn't exist!");
                return true;
            }
            VoteHandler.vote(p.getName(), map);
            ChatUtil.sendMessage(p, "Voted for '&b" + map.getMapName() + "&7'");
        }
        return false;
    }

}
