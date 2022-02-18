package hwnet.survivalgames.commands;

import hwnet.survivalgames.handlers.Gamer;
import hwnet.survivalgames.utils.ChatUtil;
import hwnet.survivalgames.utils.LocUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ResourcepackEn implements CommandExecutor {

    //

    private static String url = "https://www.dropbox.com/s/fliya0wbr6ll8ju/PureBDcraft%20%2064x%20MC118.zip?dl=1";

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can leave!");
            return true;
        }

        Player p = (Player) sender;

        if (args.length != 1) {
            ChatUtil.sendMessage(p, "Usage: /resourcepack enable|disable to use our recommended resource pack.");
            return true;
        }

        if (args[0].contains("enable")) {
            ChatUtil.sendMessage(sender, "Installing PureBDCraft 64x...");
            p.setResourcePack(url);
        } else if (args[0].contains("disable")) {
            ChatUtil.sendMessage(sender, "Uninstalling PureBDCraft 64x...");
            p.setResourcePack("http://google.com");
        }

        return false;

    }

}
