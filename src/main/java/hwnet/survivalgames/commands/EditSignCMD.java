package hwnet.survivalgames.commands;

import hwnet.survivalgames.utils.ChatUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public class EditSignCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can set signs!");
            return true;
        }

        Player p = (Player) sender;

        Block target = getTargetBlock(p, 10);


        if (!(target.getState() instanceof Sign)) {
            ChatUtil.sendMessage(p, "Please look at a sign when performing this command!");
            return true;
        }

        Sign sign = (Sign) target.getState();

        if (args.length < 1) {
            p.openSign((Sign) target.getState());
            return true;
        }

        if (args.length < 2) {
            ChatUtil.sendMessage(p, "Usage: /setsign <lineNum> <new text>");
            return true;
        }
        int line = 0;
        try {
            line = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            ChatUtil.sendMessage(p, "Error: '" + args[0] + "' is not a number!");
            return true;
        }

        String linestr = "";
        for (int i = 1; i < args.length; i++) {
            if (i == args.length - 1) linestr += (args[i]) + "";
            else linestr += (args[i]) + " ";
        }
        linestr = ChatColor.translateAlternateColorCodes('&', linestr);
        sign.setLine((line - 1), linestr);
        sign.update();
        ChatUtil.sendMessage(p, "Set sign line " + line + " to: '" + linestr + "&r&7'");
        return true;
    }

    public final Block getTargetBlock(Player player, int range) {
        return player.getTargetBlock((Set<Material>) null, range);
    }
}
