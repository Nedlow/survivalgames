package hwnet.survivalgames.utils;

import hwnet.survivalgames.SG;
import hwnet.survivalgames.handlers.*;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GameBoard {

    private static List<GameBoard> boardList = new ArrayList<>();
    private List<Score> mapScores = new ArrayList<>();
    private Scoreboard board;
    private Objective lobby, ingame;
    private Score lobby_timeleft, ingame_timeleft, kills, timealive, stats_games, stats_wins, stats_kills, stats_points;
    private Player player;

    public static enum ScoreType {
        TIME_LOBBY, TIME_GAME, KILLS, VOTES;
    }

    public GameBoard(Player player) {
        this.board = Bukkit.getScoreboardManager().getNewScoreboard();
        this.player = player;

        for (int i = 1; i <= hwnet.survivalgames.handlers.Team.getAllTeams().size(); i++) {
            board.registerNewTeam("" + i).setPrefix(ChatColor.translateAlternateColorCodes('&', "&7[&b" + i + "&7] &r"));
        }
        player.setScoreboard(board);
        boardList.add(this);
    }


    public static GameBoard getBoard(Player player) {
        for (GameBoard boards : boardList) {
            if (boards.getPlayer() == player) {
                return boards;
            }
        }
        return null;
    }

    public static boolean hasBoard(Player player) {
        if (GameBoard.getBoard(player) == null) return false;
        else return true;
    }

    public void remove() {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        boardList.remove(this);
    }

    public Scoreboard getBoard() {
        return board;
    }

    public Player getPlayer() {
        return player;
    }


    public static void addToTeam(Player p, String teamName) {
        for (GameBoard board : boardList) {
            if (!board.getBoard().getTeam(teamName).hasEntry(p.getName()))
                board.getBoard().getTeam(teamName).addEntry(p.getName());
        }
    }

    public static void removeFromTeam(Player p, String teamName) {
        for (GameBoard board : boardList) {
            if (board.getBoard().getTeam(teamName).hasEntry(p.getName()))
                board.getBoard().getTeam(teamName).removeEntry(p.getName());
        }
    }

    public void intializeLobby() {
        lobby = board.registerNewObjective(shortName("P-" + player.getName()), "dummy", ChatColor.translateAlternateColorCodes('&', ChatUtil.centerText("&r&4=== &6&lSurvival Games: In Lobby &r&4===", 40)));
        lobby.setDisplaySlot(DisplaySlot.SIDEBAR);


        lobby.getScore("").setScore(11);
        lobby_timeleft = lobby.getScore(ChatColor.AQUA + "Time till start: " + ChatColor.YELLOW + ChatUtil.formatTime(SG.pretime));
        lobby_timeleft.setScore(10);
        lobby.getScore(" ").setScore(9);
        lobby.getScore(ChatColor.translateAlternateColorCodes('&', "&b&lStats for &a&l" + player.getName() + ":")).setScore(8);
        stats_wins = lobby.getScore(ChatColor.translateAlternateColorCodes('&', "&b - Wins: &a  ") + PointSystem.getWins(player.getUniqueId()));
        stats_wins.setScore(7);
        stats_games = lobby.getScore(ChatColor.translateAlternateColorCodes('&', "&b - Games: &a") + PointSystem.getGames(player.getUniqueId()));
        stats_games.setScore(6);
        stats_kills = lobby.getScore(ChatColor.translateAlternateColorCodes('&', "&b - Kills:    &a") + PointSystem.getKills(player.getUniqueId()));
        stats_kills.setScore(5);
        stats_points = lobby.getScore(ChatColor.translateAlternateColorCodes('&', "&b - Points: &a") + PointSystem.getPoints(player.getUniqueId()));
        stats_points.setScore(4);
        lobby.getScore("  ").setScore(3);
        lobby.getScore("   ").setScore(2);
        lobby.getScore(ChatUtil.centerText("  &5&l&o&nvigorousgaming.se&r", 45)).setScore(1);
    }

    public void intializeGame() {
        //player.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
        board.clearSlot(DisplaySlot.SIDEBAR);
        clearBoard();
        unregisterObjective(lobby);
        ingame = board.registerNewObjective(shortName("I-" + player.getName()), "dummy", ChatColor.translateAlternateColorCodes('&', ChatUtil.centerText("&r&4=== &6&lSurvival Games: In Game &r&4===", 40)));
        ingame.setDisplaySlot(DisplaySlot.SIDEBAR);
        // SPACE
        ingame.getScore("").setScore(10);
        // TIME LEFT
        ingame_timeleft = ingame.getScore("Time left: ");
        ingame_timeleft.setScore(9);
        // KILLS
        kills = ingame.getScore(ChatColor.translateAlternateColorCodes('&', "&bKills: &a0"));
        kills.setScore(8);
        ingame.getScore("  ").setScore(6);
        ingame.getScore("   ").setScore(5);
        ingame.getScore(ChatUtil.centerText("  &5&l&o&nvigorousgaming.se&r", 45)).setScore(4);
    }

    public void intiliazeDeath() {
        // Time alive
        timealive = ingame.getScore(ChatColor.translateAlternateColorCodes('&', "&bTime alive: &a0" + ChatUtil.formatTime(Gamer.getGamer(player.getUniqueId()).getTimeAlive())));
        timealive.setScore(7);
    }

    public void clearBoard() {
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }
    }

    public void unregisterGame() {
        ingame.unregister();
    }

    public static void resetScoreboard() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (SG.districts_mode) removeFromTeam(p, hwnet.survivalgames.handlers.Team.getTeam(p).getName());
            p.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
            getBoard(p).unregisterGame();
            getBoard(p).intializeLobby();
        }
    }

    public void unregisterObjective(Objective obj) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(SG.pl, new Runnable() {
            @Override
            public void run() {
                obj.unregister();
            }
        }, 40L);
    }


    public void update(ScoreType type, String line, String newLine) {
        int score = 0;
        switch (type) {
            case TIME_LOBBY:
                score = lobby_timeleft.getScore();
                player.getScoreboard().resetScores(lobby_timeleft.getEntry());
                lobby_timeleft = lobby.getScore(ChatColor.translateAlternateColorCodes('&', newLine));
                lobby_timeleft.setScore(score);
                break;
            case TIME_GAME:
                score = ingame_timeleft.getScore();
                player.getScoreboard().resetScores(ingame_timeleft.getEntry());
                ingame_timeleft = ingame.getScore(ChatColor.translateAlternateColorCodes('&', newLine));
                ingame_timeleft.setScore(score);
                break;
            case KILLS:
                score = kills.getScore();
                player.getScoreboard().resetScores(kills.getEntry());
                kills = ingame.getScore(ChatColor.translateAlternateColorCodes('&', newLine));
                kills.setScore(score);
        }
    }

    @Deprecated
    public void updatePregame(String line, String newLine, int scoreSlot) {
        player.getScoreboard().resetScores(lobby_timeleft.getEntry());
        lobby_timeleft = lobby.getScore(ChatColor.translateAlternateColorCodes('&', newLine));
        lobby_timeleft.setScore(scoreSlot);
    }

    @Deprecated
    public void updateIngame(String line, String newLine, int scoreSlot) {
        player.getScoreboard().resetScores(ingame_timeleft.getEntry());
        ingame_timeleft = ingame.getScore(ChatColor.translateAlternateColorCodes('&', newLine));
        ingame_timeleft.setScore(scoreSlot);
    }

    public String shortName(String str) {
        if (str.length() > 16) {
            return str.substring(0, 16);
        } else return str;
    }
}
