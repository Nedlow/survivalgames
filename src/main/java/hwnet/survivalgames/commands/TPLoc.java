package hwnet.survivalgames.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPLoc implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        Player p = (Player) sender;

        World w = Bukkit.getWorld(args[0]);
        double x = (double) Integer.parseInt(args[1]);
        double y = (double) Integer.parseInt(args[2]);
        double z = (double) Integer.parseInt(args[3]);

        p.teleport(new Location(w,x,y,z));

        return false;
    }

}
