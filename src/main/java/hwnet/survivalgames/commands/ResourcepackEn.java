package hwnet.survivalgames.commands;

import hwnet.survivalgames.handlers.Gamer;
import hwnet.survivalgames.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public class ResourcepackEn implements CommandExecutor {

    //

    private static String url = "https://www.dropbox.com/s/xilyejb6dg1uj57/Sapixcraft%2064x%20MC1.18.zip?dl=1";

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can enable the resource pack!");
            return true;
        }

        Player p = (Player) sender;


        // TEST CODE


        // DONT DELETE

        if (args.length < 1) {
            ChatUtil.sendMessage(p, "Usage: /resourcepack enable|disable to use our recommended resource pack.");
            return true;
        }

        boolean sendMSG = true;

        if (args.length == 2 && args[1].equalsIgnoreCase("false")) {
            sendMSG = false;
        }

        if (args[0].contains("enable")) {
            if (sendMSG)
                ChatUtil.sendMessage(sender, "Installing SapixCraft 64x...");
            p.setResourcePack(url);
            Gamer.getGamer(p).setResourcepack(true);
        } else if (args[0].contains("disable")) {
            if (sendMSG)
                ChatUtil.sendMessage(sender, "Uninstalling SapixCraft 64x...");
            p.setResourcePack("http://google.com");
            Gamer.getGamer(p).setResourcepack(false);
        }

        return false;

    }
}
