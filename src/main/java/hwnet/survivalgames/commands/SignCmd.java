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

        if (args.length < 1 && args.length < 3) {
            ChatUtil.sendMessage(sender, "Usage: /setsign <type> [voteid: 1-8]");
            return true;
        }

        Block target = getTargetBlock(p, 10);

        if (!(target.getState() instanceof org.bukkit.block.Sign)) {
            ChatUtil.sendMessage(p, "Please look at a sign when performing this command!");
            return true;
        }

        if (ClickSign.signExists(target.getLocation())) {
            ClickSign.getSign(target.getLocation()).remove();
            ChatUtil.sendMessage(p, "Replacing current clicksign with a new one.");
        }
        UUID randomUUID = UUID.randomUUID();
        ClickSign.SignType type = ClickSign.getType(args[0]);
        ClickSign csign = new ClickSign(randomUUID, type, target.getLocation());

        // Debug
        //ChatUtil.sendMessage(p, "Location: " + target.getLocation().getX() + " " + target.getLocation().getY() + " " + target.getLocation().getZ());


        FileConfiguration data = SettingsManager.getInstance().getData();
        if (type == ClickSign.SignType.VOTE) {
            int id = Integer.parseInt(args[1]);
            if (id < 1 || id > 8) {
                ChatUtil.sendMessage(p, "Set vote number between 1 and 8!");
                return true;
            }
            csign.setVoteID(id - 1);
            data.set("signs." + randomUUID + ".voteid", csign.getVoteID());
        }
        data.set("signs." + randomUUID + ".type", String.valueOf(csign.getType()));
        data.set("signs." + randomUUID + ".location.world", csign.getLocation().getWorld().getName());
        data.set("signs." + randomUUID + ".location.x", csign.getLocation().getX());
        data.set("signs." + randomUUID + ".location.y", csign.getLocation().getY());
        data.set("signs." + randomUUID + ".location.z", csign.getLocation().getZ());
        SettingsManager.getInstance().saveData();
        csign.setSignText();

        ChatUtil.sendMessage(p, "Created sign: " + csign.getType().toString());

        return true;
    }

    public final Block getTargetBlock(Player player, int range) {
        return player.getTargetBlock((Set<Material>) null, range);
    }
}
