package hwnet.survivalgames.commands;

import hwnet.survivalgames.SG;
import hwnet.survivalgames.handlers.Gamer;
import hwnet.survivalgames.utils.ChatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import hwnet.survivalgames.handlers.Game;
import hwnet.survivalgames.handlers.VoteHandler;

public class ForceStart implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        SG.setMinPlayers(1);
        SG.startPreGameCountdown();
        SG.pretime = 10;
        ChatUtil.sendMessage(sender, "Force starting game...");
        return false;
    }

}
