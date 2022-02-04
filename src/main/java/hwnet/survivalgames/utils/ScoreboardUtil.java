package hwnet.survivalgames.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class ScoreboardUtil {

    private Scoreboard board;
    private Team red, blue;

    public ScoreboardUtil() {
        this.board = Bukkit.getScoreboardManager().getNewScoreboard();
        for (int i = 1; i <= hwnet.survivalgames.handlers.Team.getAllTeams().size(); i++) {
            board.registerNewTeam("" + i).setPrefix(ChatColor.translateAlternateColorCodes('&', "&7[&b" + i + "&7] &r"));
        }
    }

    public void addToTeam(Player player, String teamName) {
        board.getTeam(teamName).addEntry(player.getName());
    }

    public void removeFromTeam(Player player, String teamName) {
        board.getTeam(teamName).removeEntry(player.getName());
    }

    public void updateScoreboard() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setScoreboard(board);
        }
    }

    public void setScoreboard(Player p) {
        p.setScoreboard(board);
    }

}
