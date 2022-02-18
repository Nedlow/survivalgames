package hwnet.survivalgames.commands;

import hwnet.survivalgames.GameState;
import hwnet.survivalgames.SG;
import hwnet.survivalgames.handlers.Gamer;
import hwnet.survivalgames.utils.ChatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Spectate implements CommandExecutor {

    @Override
    public boolean onCommand(
            CommandSender sender, Command cmd, String label, String[] args) {

        Gamer g = Gamer.getGamer((Player) sender);
        if (GameState.getState() == GameState.INGAME || GameState.getState() == GameState.ENDGAME || GameState.getState() == GameState.POSTGAME) {
            if (!g.isAlive() && g.isSpectator()) {
                ChatUtil.sendMessage((Player) sender, "No cheating ;)");
                return true;
            }
            SG.specGUI.open((Player) sender);
            return true;
        }

        if (!g.isSpectator()) {
            g.setSpectator(true);
            g.setAlive(false);
            ChatUtil.sendMessage((Player) sender, "You are now a spectator. Make sure to follow our game rules!");
        } else {
            ChatUtil.sendMessage((Player) sender, "You are already a spectator! Use /join to play.");
        }
        return false;
    }
}