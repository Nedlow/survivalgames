package hwnet.survivalgames.handlers;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.*;
import org.bukkit.entity.Player;

import hwnet.survivalgames.GameState;
import hwnet.survivalgames.SG;
import hwnet.survivalgames.events.GameStartEvent;
import hwnet.survivalgames.utils.ChatUtil;
import hwnet.survivalgames.utils.LocUtil;

public class Game {

    private static boolean canstart = false;
    private static boolean hasStarted = false;

    public static boolean canStart() {
        return canstart;
    }

    public static void setCanStart(boolean b) {
        canstart = b;
    }

    public static boolean hasStarted() {
        return hasStarted;
    }

    public static void start() {
        ArrayList<Player> participants = new ArrayList<Player>();
        if (Gamer.getGamers().size() >= SG.minPlayers) {
            Bukkit.getScheduler().cancelTask(SG.PreGamePID);
            SG.unRegisterPreEvents();
            SG.registerStartEvents();
            if (VoteHandler.getWithMostVotes() == null) {
                Random randomInt = new Random();
                int randomMapNum = randomInt.nextInt(Map.getVoteMaps().size());
                Map.setActiveMap(Map.getMapById(randomMapNum));
            } else {
                Map.setActiveMap(VoteHandler.getWithMostVotes());
                SG.config.getConfigurationSection("settings").set("lastmap", Map.getActiveMap().getFileName());
                System.out.println("Active Map: " + Map.getActiveMap().getMapName());
                World w = Map.getActiveMap().getWorld();
                WorldBorder border = w.getWorldBorder();
                w.setClearWeatherDuration(3600 * 20);
                w.setTime(1000);
                border.setSize(1024);
                border.setCenter(Map.getActiveMap().getCenterLocation());
                hasStarted = true;
                GameState.setState(GameState.INGAME);

                ChestHandler.fillAllChests(Map.getActiveMap().getFileName());
                System.out.println("Filled chests with fun loot!");

                Bukkit.getWorld(Map.getActiveMap().getFileName()).setTime(0);

                for (Gamer gamer : Gamer.getGamers()) {
                    if (Team.hasTeam(gamer.getPlayer())) continue;
                    Team.addPlayerToAvailableTeam(gamer.getPlayer());
                }

                for (Team teams : Team.getAllTeams()) {
                    if (teams.getPlayers().size() < 1) teams.setIsAlive(false);
                }
                ChatUtil.sendMessage(SG.clogger, Team.getAliveTeams().size() + " districts participating in this game.");
                int i = 0;
                Random rand = new Random();
                ArrayList<Integer> usedSpawns = new ArrayList<>();
                for (Gamer pla : Gamer.getGamers()) {
                    pla.setAlive(true);
                    i = rand.nextInt(24);
                    while (usedSpawns.contains(i)) {
                        i = rand.nextInt(24);
                    }
                    ChatUtil.sendMessage(SG.clogger, "Spawn assigned to " + pla.getName() + ": " + i);
                    usedSpawns.add(i);
                    Player p = pla.getPlayer();
                    SG.clearPlayer(p);
                    participants.add(p);
                    LocUtil.teleportToGame(p, i);
                    p.setGameMode(GameMode.SURVIVAL);
                    i++;
                }
                SG.startGameTimer();
                /*
                ChatUtil.sendMessage(SG.clogger, "All districts:");
                for (Team a : Team.getAllTeams()) {
                    ChatUtil.sendMessage(SG.clogger, a.getName() + ": " + a.getPlayers().toString());
                }
                 */
                Bukkit.getPluginManager().callEvent(new GameStartEvent(participants));
            }
        } else {
            Bukkit.getScheduler().cancelTask(SG.PreGamePID);
            ChatUtil.broadcast("Not enough players to start game!");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SG.startPreGameCountdown();
        }
    }

    public static void stop() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.kickPlayer(ChatColor.RED + "Server restarting");
        }
    }
}