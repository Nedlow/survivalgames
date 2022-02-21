package hwnet.survivalgames.commands;

import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import hwnet.survivalgames.SG;
import hwnet.survivalgames.utils.ChatUtil;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SGCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        sender.sendMessage(ChatUtil.prefix() + SG.pl.getDescription().getName() + " is running version "
                + SG.pl.getDescription().getVersion() + " by "
                + SG.pl.getDescription().getAuthors().toString().replace("[", "").replace("]", ""));

        Player p = (Player) sender;

        ItemStack is = p.getInventory().getItemInMainHand();

        Enchantment ench = EnchantmentWrapper.getByKey(NamespacedKey.minecraft(args[0].trim().toLowerCase()));
        is.addUnsafeEnchantment(ench, Integer.valueOf(args[1]));

        return true;
    }
}