package hwnet.survivalgames.commands;

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

        if (args.length == 0) {
            ChatUtil.sendMessage(p,ChatColor.translateAlternateColorCodes('&', "&6=============== &bSurvivalGames: &eVoting &6==============="));
            ChatUtil.sendMessage(p,"Vote: [/vote <id>]");
            for(Map map : Map.getVoteMaps()){
                ChatUtil.sendMessage(p,Map.getTempId(map) + " > "+map.getMapName() + " ["+VoteHandler.getVotesMap(map)+" votes]");
            }
            ChatUtil.sendMessage(p,ChatColor.translateAlternateColorCodes('&', "&6================================================="));

        }
        if(args.length == 1){

            if(VoteHandler.hasVoted(p.getName())){
                ChatUtil.sendMessage(p,ChatColor.RED+"You have already voted!");
                return true;
            }


            String uname = p.getName();
            int id = Integer.parseInt(args[0])-1;
            Map mapp = Map.getMapById(id);
            VoteHandler.vote(uname, mapp);
            ChatUtil.sendMessage(p, "Voted for map '"+mapp.getMapName()+"'");
        }
        return false;
    }

}
