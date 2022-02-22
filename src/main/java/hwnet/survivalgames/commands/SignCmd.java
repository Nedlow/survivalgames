package hwnet.survivalgames.commands;

import hwnet.survivalgames.SG;
import hwnet.survivalgames.SettingsManager;
import hwnet.survivalgames.handlers.ClickSign;
import hwnet.survivalgames.utils.ChatUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import java.util.Set;
import java.util.UUID;

public class SignCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {


        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can set signs!");
            return true;
        }

        Player p = (Player) sender;

        if (args.length != 1) {
            ChatUtil.sendMessage(sender, "Usage: /setsign <type>");
            return true;
        }

        Block target = getTargetBlock(p, 10);

        if (!(target.getState() instanceof org.bukkit.block.Sign)) {
            ChatUtil.sendMessage(p, "Please look at a sign when performing this command!");
            return true;
        }

        if (ClickSign.getSign(target.getLocation()) != null) {
            ClickSign.getSign(target.getLocation()).remove();
            ChatUtil.sendMessage(p, "Replacing current clicksign with a new one.");
        }
        UUID randomUUID = UUID.randomUUID();
        ClickSign csign = new ClickSign(randomUUID, ClickSign.getType(args[0]), target.getLocation());
        ChatUtil.sendMessage(p, "Location: " + target.getLocation().getX() + " " + target.getLocation().getY() + " " + target.getLocation().getZ());
        csign.setSignText();

        FileConfiguration data = SettingsManager.getInstance().getData();

        data.set("signs." + randomUUID + ".type", String.valueOf(csign.getType()));
        data.set("signs." + randomUUID + ".location.world", csign.getLocation().getWorld().getName());
        data.set("signs." + randomUUID + ".location.x", csign.getLocation().getX());
        data.set("signs." + randomUUID + ".location.y", csign.getLocation().getY());
        data.set("signs." + randomUUID + ".location.z", csign.getLocation().getZ());
        SettingsManager.getInstance().saveData();

        return true;
    }

    public final Block getTargetBlock(Player player, int range) {
        return player.getTargetBlock((Set<Material>) null, range);
    }
}
