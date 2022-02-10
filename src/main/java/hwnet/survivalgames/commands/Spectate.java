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

        if (GameState.getState() == GameState.INGAME || GameState.getState() == GameState.ENDGAME || GameState.getState() == GameState.POSTGAME) {
            Gamer g = Gamer.getGamer((Player) sender);
            if (!g.isAlive() && g.isSpectator()) {
                ChatUtil.sendMessage((Player) sender, "No cheating ;)");
                return true;
            }
            SG.specGUI.open((Player) sender);
            return true;
        }

        ChatUtil.sendMessage((Player) sender, "You can only open the spectate menu while in-game.");


        return false;
    }
}