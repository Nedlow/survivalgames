package hwnet.survivalgames.commands;

import hwnet.survivalgames.SG;
import hwnet.survivalgames.handlers.Map;
import hwnet.survivalgames.utils.ChatUtil;
import hwnet.survivalgames.utils.ResetMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hwnet.survivalgames.utils.LocUtil;

public class EditArena implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only users can edit arenas");
            return true;
        }

        Player p = (Player) sender;

        if (label.equalsIgnoreCase("editarena")) {
            if (p.hasPermission("sg.admin")) {
                if (args.length == 0) {
                    p.sendMessage("Usage: /editarena <filename>");
                    return true;
                }
                WorldCreator wc = new WorldCreator(args[0]);
                World world = wc.createWorld();
                double x = Bukkit.getWorld(args[0]).getSpawnLocation().getX();
                double y = Bukkit.getWorld(args[0]).getSpawnLocation().getY() + 1;
                double z = Bukkit.getWorld(args[0]).getSpawnLocation().getZ();
                p.teleport(new Location(world, x, y, z));
                p.setGameMode(GameMode.CREATIVE);
                ChatUtil.sendMessage(p, "Editing arena '" + args[0] + "'.");
            } else {
                p.sendMessage(ChatColor.RED + "No permission.");
            }
        } else if (label.equalsIgnoreCase("savearena")) {
            p.getWorld().save();
            Map map = Map.getMap(p.getLocation().getWorld().getName());
            ResetMap.createBackup(map, SG.pl, true);

            LocUtil.teleportToLobby(p);
            String msg = "&aSaved arena &f'&a" + map.getMapName() + "&f'";
            ChatUtil.sendMessage(p, msg);
        }
        return false;
    }
}
