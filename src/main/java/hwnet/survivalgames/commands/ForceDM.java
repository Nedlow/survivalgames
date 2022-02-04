package hwnet.survivalgames.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import hwnet.survivalgames.SG;

public class ForceDM implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {


        SG.gametime = SG.dmtime - 30;
        sender.sendMessage("Forcing deathmatch.");
        return true;
    }

}
