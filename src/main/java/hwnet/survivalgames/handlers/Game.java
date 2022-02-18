package hwnet.survivalgames.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import hwnet.survivalgames.utils.ResetMap;
import hwnet.survivalgames.utils.ScoreboardUtil;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;

import hwnet.survivalgames.GameState;
import hwnet.survivalgames.SG;
import hwnet.survivalgames.events.GameStartEvent;
import hwnet.survivalgames.utils.ChatUtil;
import hwnet.survivalgames.utils.LocUtil;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

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
        if (Gamer.getAliveGamers().size() >= SG.minPlayers) {
            Bukkit.getScheduler().cancelTask(SG.PreGamePID);
            SG.unRegisterPreEvents();
            SG.registerStartEvents();
            if (VoteHandler.getWithMostVotes() == null) {
                // FIX ASAP
                Random randomInt = new Random();
                Map randomMapNum = Map.getVoteMaps().get(randomInt.nextInt(Map.getVoteMaps().size()));
                Map.setActiveMap(randomMapNum);
            } else {
                Map.setActiveMap(VoteHandler.getWithMostVotes());
            }
            Map.getActiveMap().initializeSpawns();
            SG.config.getConfigurationSection("settings").set("lastmap", Map.getActiveMap().getFileName());
            SG.pl.saveConfig();
            World w = Map.getActiveMap().getWorld();
            WorldBorder border = w.getWorldBorder();
            w.setClearWeatherDuration(3600 * 20);
            w.setTime(1000);
            border.setSize(1024);
            border.setCenter(Map.getActiveMap().getCenterLocation());
            hasStarted = true;
            GameState.setState(GameState.INGAME);

            ChestHandler.fillAllChests(Map.getActiveMap().getFileName());
            ChatUtil.sendMessage(SG.clogger, "Filled chests with loot");

            Bukkit.getWorld(Map.getActiveMap().getFileName()).setTime(0);
            if (SG.districts_mode) {
                for (Gamer gamer : Gamer.getGamers()) {
                    if (Team.hasTeam(gamer.getPlayer())) continue;
                    Team.addPlayerToAvailableTeam(gamer.getPlayer());
                }
                for (Team teams : Team.getAllTeams()) {
                    if (teams.getPlayers().size() < 1) teams.setIsAlive(false);
                }
                ChatUtil.sendMessage(SG.clogger, Team.getAliveTeams().size() + " districts participating in this game.");
            } else {
                ChatUtil.sendMessage(SG.clogger, Gamer.getAliveGamers().size() + " tributes participating in this game.");
            }


            int i = 0;
            Random rand = new Random();
            ArrayList<Integer> usedSpawns = new ArrayList<>();
            for (Gamer pla : Gamer.getGamers()) {

                Player p = pla.getPlayer();

                SG.clearPlayer(p);
                if (pla.isSpectator()) {
                    p.teleport(Map.getActiveMap().getCenterLocation());
                    p.setGameMode(GameMode.SPECTATOR);
                } else {
                    i = rand.nextInt(24);
                    while (usedSpawns.contains(i)) {
                        i = rand.nextInt(24);
                    }
                    ChatUtil.sendMessage(SG.clogger, "Spawn assigned to " + pla.getName() + ": " + i);
                    usedSpawns.add(i);
                    ItemStack compass = new ItemStack(Material.COMPASS);
                    ItemMeta meta = compass.getItemMeta();
                    List<String> lore = new ArrayList<String>();
                    lore.add("Shows the way.");
                    meta.setLore(lore);
                    compass.setItemMeta(meta);
                    p.getInventory().setItem(8, compass);
                    participants.add(p);
                    LocUtil.teleportToGame(p, i);
                    p.setGameMode(GameMode.SURVIVAL);
                    i++;
                }
            }
            SG.specGUI.getYourInventory().clear();
            for (int iD = 0; iD < Gamer.getAliveGamers().size(); iD++) {
                ItemStack playerhead = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta playerheadmeta = (SkullMeta) playerhead.getItemMeta();
                playerheadmeta.setOwner(Gamer.getAliveGamers().get(iD).getName());
                playerheadmeta.setDisplayName(Gamer.getAliveGamers().get(iD).getName());
                playerhead.setItemMeta(playerheadmeta);
                int finalI = iD;
                SG.specGUI.setItem(iD, playerhead, player -> {
                    player.teleport(Gamer.getAliveGamers().get(finalI).getPlayer());
                });
            }

            SG.startGameTimer();

            List<String> motd = new ArrayList<String>();
            motd.add("&6Surval Games&7: &cIn Game");
            motd.add("&b" + Gamer.getAliveGamers().size() + "&7/&b24 tributes left!");
            ChatUtil.setMOTD(motd);

            Bukkit.getPluginManager().callEvent(new GameStartEvent(participants));
        } else {
            Bukkit.getScheduler().cancelTask(SG.PreGamePID);
            ChatUtil.broadcast("Not enough tributes to start game!");
            SG.startPreGameCountdown();
        }
    }

    public static void win(Team team, Player player) {
        GameState.setState(GameState.POSTGAME);
        if (player == null && SG.districts_mode) {
            if (team.getAlivePlayers().size() > 1) {
                ChatUtil.broadcast("&6&l District " + team.getName() + "&r won the Survival Games with multiple players alive!");
            } else if (team.getAlivePlayers().size() == 1) {
                ChatUtil.broadcast("&6&l" + team.getAlivePlayers().get(0).getName() + "&r from District " + team.getName() + " won the Survival Games!");
            }
            for (Player p : team.getAlivePlayers()) {
                p.sendTitle(ChatColor.translateAlternateColorCodes('&', "&6Victory!"), ChatColor.translateAlternateColorCodes('&', "&eThanks for playing."), 20, 20 * 5, 20);
            }
        } else if (team == null && !SG.districts_mode) {
            ChatUtil.broadcast("&6&l" + player.getName() + "&r won the Survival Games!");
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getUniqueId() == player.getUniqueId())
                    p.sendTitle(ChatColor.translateAlternateColorCodes('&', "&6Victory!"), ChatColor.translateAlternateColorCodes('&', "&eThanks for playing."), 20, 20 * 5, 20);
                else
                    p.sendTitle(ChatColor.translateAlternateColorCodes('&', "&7" + player.getName() + " won!"), ChatColor.translateAlternateColorCodes('&', "&eThanks for playing."), 20, 20 * 5, 20);
            }
        }

        if (GameState.getState() == GameState.ENDGAME) {
            spawnFireworks(true);
            for (Player pl : Bukkit.getOnlinePlayers()) {
                //pl.playSound(Map.getActiveMap().getCenterLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 20, 1);
                pl.playSound(Map.getActiveMap().getCenterLocation(), Sound.MUSIC_DISC_CAT, 20, 1);
            }
        } else {
            spawnFireworks(false);
            for (Player pl : Bukkit.getOnlinePlayers()) {
                //pl.playSound(Map.getActiveMap().getCenterLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 20, 1);
                if (SG.districts_mode) {
                    pl.playSound(Team.getAliveTeams().get(0).getAlivePlayers().get(0).getLocation(), Sound.MUSIC_DISC_CAT, 20, 1);
                } else {
                    pl.playSound(Gamer.getAliveGamers().get(0).getPlayer().getLocation(), Sound.MUSIC_DISC_CAT, 20, 1);
                }
            }
        }

        ChatUtil.broadcast("Teleporting to lobby in 10 seconds...");

        Bukkit.getScheduler().scheduleSyncDelayedTask(SG.pl, new Runnable() {

            @Override
            public void run() {
                if (SG.districts_mode) {
                    for (Player p : Team.getAliveTeams().get(0).getAlivePlayers()) {
                        PointSystem.addPoints(p, 200);
                        Gamer g = Gamer.getGamer(p);
                        if (SG.config.getBoolean("mysql.enabled")) g.addWin();
                    }
                } else {
                    Gamer g = Gamer.getAliveGamers().get(0);
                    PointSystem.addPoints(g.getPlayer(), 200);

                    if (SG.config.getBoolean("mysql.enabled")) g.addWin();
                }
                Game.cancelGame();
            }
        }, 20L * 10);
    }

    public static void spawnFireworks(boolean dm) {
        if (dm) {
            SG.fireworksPID = Bukkit.getScheduler().scheduleSyncRepeatingTask(SG.pl, new Runnable() {


                int amount = Map.getActiveMap().getSpawns().size();
                int current = 0;
                int i = 1;

                @Override
                public void run() {

                    Location loc = Map.getActiveMap().getSpawn(current).add(0, 10, 0);
                    current++;

                    Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
                    FireworkMeta fwm = fw.getFireworkMeta();

                    Color color = Color.RED;
                    if (i == 1) {
                        color = Color.LIME;
                    } else if (i == 2) {
                        color = Color.MAROON;
                    } else if (i == 3) {
                        color = Color.YELLOW;
                    } else if (i == 4) {
                        color = Color.AQUA;
                    }

                    fwm.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(color).flicker(true).build());
                    fwm.setPower(3);
                    fw.setFireworkMeta(fwm);
                    fw.detonate();
                    if (i == 4) i = 1;
                    else i++;

                    if (current == amount) {
                        Bukkit.getScheduler().cancelTask(SG.fireworksPID);
                    }

                }
            }, 0, 5);
        } else {
            Location loc = Gamer.getAliveGamers().get(0).getPlayer().getLocation();
            for (int i = 1; i <= 4; i++) {
                if (SG.districts_mode) {
                    if (i == 1) {
                        loc = Team.getAliveTeams().get(0).getAlivePlayers().get(0).getLocation().add(10, 15, 0);
                    } else if (i == 2) {
                        loc = Team.getAliveTeams().get(0).getAlivePlayers().get(0).getLocation().add(-10, 15, 0);
                    } else if (i == 3) {
                        loc = Team.getAliveTeams().get(0).getAlivePlayers().get(0).getLocation().add(0, 15, 10);
                    } else if (i == 4) {
                        loc = Team.getAliveTeams().get(0).getAlivePlayers().get(0).getLocation().add(0, 15, -10);
                    }
                } else {
                    if (i == 1) {
                        loc = Gamer.getAliveGamers().get(0).getPlayer().getLocation().add(10, 15, 0);
                    } else if (i == 2) {
                        loc = Gamer.getAliveGamers().get(0).getPlayer().getLocation().add(-10, 15, 0);
                    } else if (i == 3) {
                        loc = Gamer.getAliveGamers().get(0).getPlayer().getLocation().add(0, 15, 10);
                    } else if (i == 4) {
                        loc = Gamer.getAliveGamers().get(0).getPlayer().getLocation().add(0, 15, -10);
                    }
                }
                Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
                FireworkMeta fwm = fw.getFireworkMeta();

                Color color = Color.RED;
                if (i == 1) {
                    color = Color.LIME;
                } else if (i == 2) {
                    color = Color.MAROON;
                } else if (i == 3) {
                    color = Color.YELLOW;
                } else if (i == 4) {
                    color = Color.AQUA;
                }

                fwm.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(color).flicker(true).build());
                fwm.setPower(3);
                fw.setFireworkMeta(fwm);
                fw.detonate();
            }
        }
    }

    public static void cancelGame() {
        GameState.setState(GameState.RESTARTING);
        for (Player pl : Bukkit.getOnlinePlayers()) {
            if (SG.config.getBoolean("bungeecord")) SG.sendToServer(pl, SG.config.getString("lobbyserver"));
            else {
                pl.setGameMode(GameMode.ADVENTURE);
                SG.clearPlayer(pl);
                pl.showPlayer(SG.pl, pl);
                LocUtil.teleportToLobby(pl);
            }
        }

        // CLEAR ALL DATA (VOTES, RANDOM MAP IDS, GAMERS '
        // (need to find the logic here, so we know who is a gamer and who is spectator without it being removed with the clearing)
        // Currently clears gamers that are NOT spectators from the start. Dead players will be cleared.
        Bukkit.getScheduler().cancelTask(SG.DMPID);
        Bukkit.getScheduler().cancelTask(SG.gamePID);
        Bukkit.getScheduler().cancelTask(SG.compassPID);
        Gamer.clearRealGamers();
        if (SG.districts_mode)
            ScoreboardUtil.resetScoreboard();
        Team.clearInfo();
        VoteHandler.clearVotes();

        // Reset config information
        SG.minPlayers = SG.pl.getConfig().getConfigurationSection("settings").getInt("minPlayers");
        Team.setTeamSize(SG.config.getConfigurationSection("settings").getInt("teamSize"));
        ChatUtil.setChatFormat(SG.config.getConfigurationSection("settings.chat").getString("format"));


        // UNREGISTER AND REGISTER CORRECT LISTENERS (registerPreEvents)
        SG.unregisterGameEvents();
        SG.registerPreEvents();

        // UNLOAD WORLD AND ROLLBACK
        ChatUtil.sendMessage(SG.cmd, "Rolling back map " + Map.getActiveMap().getMapName());
        ResetMap.handleReset(SG.pl);
        // Clear Map info
        Map.clearInfo();
        // Choose 6 random maps for vote cycle
        Map.chooseMaps();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.isOp()) {
                Gamer.getGamer(p);
                ChatUtil.sendMessage(SG.cmd, "Added " + p.getName() + " to gamers");
            }
            if (Gamer.getGamer(p.getUniqueId()) != null)
                if (Gamer.getGamer(p).isSpectator())
                    ChatUtil.sendMessage(p, "You spectated last game. To participate, you need to type /join while there are available slots.");
        }

        SG.startPreGameCountdown();
        // SET MOTD
        List<String> motd = new ArrayList<String>();
        motd.add("&6SurvalGames&7: &aIn Lobby");
        motd.add("&a" + (24 - Gamer.getAliveGamers().size()) + " spots left!");
        ChatUtil.setMOTD(motd);
        GameState.setState(GameState.WAITING);
    }
}
