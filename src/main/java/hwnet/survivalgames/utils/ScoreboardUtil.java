package hwnet.survivalgames.utils;

import hwnet.survivalgames.SG;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class ScoreboardUtil {

    private static Scoreboard board;
    private static Objective pregame, ingame;
    private static Score gameState, timeLeft, kills;
    private Team red, blue;

    public ScoreboardUtil() {
        this.board = Bukkit.getScoreboardManager().getNewScoreboard();
        for (int i = 1; i <= hwnet.survivalgames.handlers.Team.getAllTeams().size(); i++) {
            board.registerNewTeam("" + i).setPrefix(ChatColor.translateAlternateColorCodes('&', "&7[&b" + i + "&7] &r"));
        }
        pregame = board.registerNewObjective("Pregame", "dummy", ChatColor.translateAlternateColorCodes('&', ChatUtil.centerText("&r&4=== &6&lSurvival Games: In Lobby &r&4===")));
        pregame.setDisplaySlot(DisplaySlot.SIDEBAR);
        pregame.getScore("").setScore(2);
        gameState = pregame.getScore("Time till start: Not enough players");
        gameState.setScore(1);
    }

    public void updatePregame(Player player, String line, String newLine, int scoreSlot) {
        player.getScoreboard().resetScores(gameState.getEntry());
        gameState = pregame.getScore(ChatColor.translateAlternateColorCodes('&', newLine));
        gameState.setScore(scoreSlot);
    }

    public void updateIngame(Player player, String line, String newLine, int scoreSlot) {
        player.getScoreboard().resetScores(timeLeft.getEntry());
        timeLeft = ingame.getScore(ChatColor.translateAlternateColorCodes('&', newLine));
        timeLeft.setScore(scoreSlot);
    }

    public void swapToIngame(Player player) {
        player.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
        board.clearSlot(DisplaySlot.SIDEBAR);
        ingame = board.registerNewObjective("Ingame", "dummy", ChatColor.translateAlternateColorCodes('&', ChatUtil.centerText("&r&4=== &6&lSurvival Games: In Game &r&4===")));
        ingame.setDisplaySlot(DisplaySlot.SIDEBAR);
        // SPACE
        ingame.getScore("").setScore(10);
        // TIME LEFT
        timeLeft = ingame.getScore("Time left: ");
        timeLeft.setScore(9);
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

    public static void resetScoreboard() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            SG.SBU.removeFromTeam(p, hwnet.survivalgames.handlers.Team.getTeam(p).getName());
        }
    }
}
