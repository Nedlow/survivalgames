package hwnet.survivalgames.commands;

import hwnet.survivalgames.GameState;
import hwnet.survivalgames.SG;
import hwnet.survivalgames.handlers.Game;
import hwnet.survivalgames.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CancelCMD implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        int cancelTime = 5;

        if ((GameState.getState() != GameState.INGAME) && (GameState.getState() != GameState.ENDGAME)) {
            ChatUtil.sendMessage(sender, "No game currently running!");
            return true;
        }

        if (args.length == 1) {
            cancelTime = Integer.valueOf(args[0]);
        }
        ChatUtil.broadcast("Game is cancelled. Players will be teleported to lobby in " + cancelTime + " seconds.");
        Bukkit.getScheduler().scheduleSyncDelayedTask(SG.pl, new Runnable() {
            @Override
            public void run() {
                Game.cancelGame();
            }
        }, 20L * cancelTime);
        return true;
    }
}
