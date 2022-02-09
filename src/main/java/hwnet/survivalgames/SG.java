package hwnet.survivalgames;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import hwnet.survivalgames.commands.*;
import hwnet.survivalgames.handlers.*;
import hwnet.survivalgames.handlers.Team;
import hwnet.survivalgames.utils.ScoreboardUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

import hwnet.survivalgames.listeners.GraceListener;
import hwnet.survivalgames.listeners.IngameListener;
import hwnet.survivalgames.listeners.JoinListener;
import hwnet.survivalgames.listeners.StartListener;
import hwnet.survivalgames.utils.ChatUtil;
import hwnet.survivalgames.utils.LocUtil;
import hwnet.survivalgames.utils.ResetMap;

public class SG extends JavaPlugin {

    public static int cdId;

    public static Connection connection;

    public static FileConfiguration config;
    public static FileConfiguration data = SettingsManager.getInstance().getData();
    public static ConsoleCommandSender cmd = Bukkit.getConsoleSender();

    public static int gamePID, PreGamePID, DMPID, compassPID, fireworksPID;
    public static int pretime, gametime, dmtime, dm, minPlayers, maxPlayers;

    public static boolean districts_mode;

    public static SG pl;

    public static Logger logger;
    public static ConsoleCommandSender clogger;

    public static ScoreboardUtil SBU;

    @Override
    public void onLoad() {
        configs();
        SettingsManager.getInstance().setup(this);

        String lastmap = config.getConfigurationSection("settings").getString("lastmap");
        if (lastmap == null || lastmap.equalsIgnoreCase("null")) {
            cmd.sendMessage("No maps played yet.");
        } else {
            ChatUtil.sendMessage(cmd, "Rolling back map " + lastmap);
            ResetMap.rollback(lastmap, this);
        }
    }

    @Override
    public void onEnable() {
        logger = getLogger();
        clogger = getServer().getConsoleSender();
        pl = this;

        registerCommands();
        registerPreEvents();

        SBU = new ScoreboardUtil();

        minPlayers = getConfig().getConfigurationSection("settings").getInt("settings.");
        Team.setTeamSize(SG.config.getConfigurationSection("settings").getInt("teamSize"));
        ChatUtil.setChatFormat(SG.config.getConfigurationSection("settings.chat").getString("format"));
        maxPlayers = 24;

        districts_mode = config.getBoolean("settings.district_mode");
        if (districts_mode) registerDistricts();
        if (config.getBoolean("lobby.enabled")) {
            Bukkit.getWorld(config.getString("lobby.world")).setSpawnLocation(LocUtil.getLobbyLocation());
            Bukkit.getWorld(config.getString("lobby.world")).setClearWeatherDuration(3600 * 20);
        }


        if (getConfig().getConfigurationSection("settings.chat").getBoolean("customprefix")) {
            ChatUtil.setPrefix(SG.config.getConfigurationSection("settings.chat").getString("prefix"));
        }

        dmtime = getConfig().getInt("settings.deathmatch") * 60;
        data = SettingsManager.getInstance().getData();

        ChatUtil.sendMessage(clogger, ChatColor.GOLD + "==================================");
        ChatUtil.sendMessage(clogger, ChatColor.AQUA + "Enabling SurvivalGames by " + getDescription().getAuthors().toString().replace("[", "").replace("]", ""));
        ChatUtil.sendMessage(clogger, ChatColor.GOLD + "==================================");

        if (data.getConfigurationSection("arenas") == null) {
            ChatUtil.sendMessage(clogger, ChatColor.translateAlternateColorCodes('&', "&4No arenas created!"));
        } else {
            ChatUtil.sendMessage(clogger, "Registering maps");
            for (String maps : data.getConfigurationSection("arenas").getKeys(false)) {
                Map map = new Map(data.getString("arenas." + maps + ".name"), maps);
                ChatUtil.sendMessage(cmd, "Loaded " + map.getMapName());
                ResetMap.createBackup(map, this);
                WorldCreator world = new WorldCreator(map.getFileName());
                world.createWorld();
            }
            ChatUtil.sendMessage(cmd, "A total of " + Map.getAllMaps().size() + " maps.");
            Map.chooseMaps();
        }

        List<String> motd = new ArrayList<String>();
        motd.add("&6SurvalGames&7: &aIn Lobby");
        motd.add("&a" + (24 - Gamer.getAliveGamers().size()) + " spots left!");
        ChatUtil.setMOTD(motd);


        if (config.getBoolean("mysql.enabled")) {
            openConnection();
        }
        startPreGameCountdown();
    }

    @Override
    public void onDisable() {
        if (config.getBoolean("mysql.enabled")) {
            try {
                if (connection != null && connection.isClosed()) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (config.getBoolean("settings.bungeecord")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                sendToServer(p, config.getString("lobbyserver"));
            }
        }
    }


    public static void setMinPlayers(int amount) {
        minPlayers = amount;
    }

    // Bungeecord
    public static void sendToServer(Player p, String server) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);

            out.writeUTF("Connect");
            out.writeUTF(server);
            p.sendPluginMessage(pl, "BungeeCord", b.toByteArray());

            out.close();
        } catch (Exception er) {
            er.printStackTrace();
        }
    }

    // MYSQL
    public synchronized static void openConnection() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + config.getString("mysql.host") + ":" + config.getString("mysql.port") + "/" + config.getString("mysql.database"), config.getString("mysql.username"), config.getString("mysql.password"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static void closeConnection() {
        try {
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static boolean playerDataContains(String uuid) {
        try {
            PreparedStatement sql = connection.prepareStatement("select * from users where uuid=?");
            sql.setString(1, uuid);
            ResultSet result = sql.executeQuery();
            boolean contains = result.next();

            sql.close();
            return contains;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void registerDistricts() {
        for (int i = 1; i <= 12; i++) {
            new Team(String.valueOf(i));
        }
    }

    private void registerCommands() {
        getCommand("addarena").setExecutor(new Addmap());
        getCommand("addspawn").setExecutor(new Addspawn());
        getCommand("vote").setExecutor(new Vote());
        getCommand("editarena").setExecutor(new EditArena());
        getCommand("savearena").setExecutor(new EditArena());
        getCommand("fstart").setExecutor(new ForceStart());
        getCommand("forcedm").setExecutor(new ForceDM());
        getCommand("join").setExecutor(new Join());
        getCommand("volunteer").setExecutor(new Volunteer());
        getCommand("leave").setExecutor(new Leave());
        getCommand("setlobby").setExecutor(new Lobby());
        getCommand("tploc").setExecutor(new TPLoc());
        getCommand("sg").setExecutor(new SGCommand());
        getCommand("points").setExecutor(new Points());
        getCommand("spectate").setExecutor(new Spectate());

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new ChatHandler(), this);
        if (config.getBoolean("settings.bungeecord"))
            getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }


    private static Listener preListener, startListener, graceListener, ingameListener;


    public static void registerPreEvents() {
        preListener = new JoinListener();
        Bukkit.getPluginManager().registerEvents(preListener, SG.pl);
    }

    public static void unRegisterPreEvents() {
        HandlerList.unregisterAll(preListener);
    }

    public static void registerStartEvents() {
        startListener = new StartListener();
        Bukkit.getPluginManager().registerEvents(startListener, SG.pl);
    }

    public static void unregisterStartEvents() {
        HandlerList.unregisterAll(startListener);
    }

    public static void registerGraceEvents() {
        graceListener = new GraceListener();
        Bukkit.getPluginManager().registerEvents(graceListener, SG.pl);
    }

    public static void unregisterGraceEvents() {
        HandlerList.unregisterAll(graceListener);
    }

    public static void registerGameEvents() {
        ingameListener = new IngameListener();
        Bukkit.getPluginManager().registerEvents(ingameListener, SG.pl);
    }

    public static void unregisterGameEvents() {
        HandlerList.unregisterAll(ingameListener);
    }

    private void configs() {
        config = getConfig();
        saveDefaultConfig();
        if (config.getBoolean("rewriteconfig")) {
            File file = new File(getDataFolder() + File.separator + "config.yml");
            file.delete();
            saveDefaultConfig();
        }
        saveConfig();
    }

    public static void startPreGameCountdown() {
        pretime = SG.pl.getConfig().getInt("settings.pretime") * 60;
        PreGamePID = Bukkit.getScheduler().scheduleSyncRepeatingTask(SG.pl, new Runnable() {

            @Override
            public void run() {
                if (pretime % 60 == 0) {
                    for (Gamer g : Gamer.getGamers()) {
                        ChatUtil.sendVoteMenu(g.getPlayer());
                    }
                    if (pretime == 60) {
                        ChatUtil.broadcast("Game starting in " + pretime / 60 + " minute.");
                    } else if (pretime > 60) {
                        ChatUtil.broadcast("Game starting in " + pretime / 60 + " minutes.");
                    }
                    ChatUtil.broadcast(ChatColor.AQUA + "" + Gamer.getGamers().size() + "/24" + ChatColor.GREEN + " tributes waiting to play.");
                }


                if (pretime == 45 || pretime == 30 || pretime == 15 || (pretime >= 0 && pretime <= 10)) {
                    ChatUtil.broadcast("Game starting in " + pretime + " seconds.");
                }
                if (pretime == 0) {
                    Game.start();
                }
                pretime--;
            }
        }, 0, 20);
    }

    public static void startGameTimer() {

        compassPID = Bukkit.getScheduler().scheduleSyncRepeatingTask(SG.pl, new Runnable() {
            World w = Map.getActiveMap().getWorld();

            @Override
            public void run() {
                if (SG.districts_mode) {
                    for (Team t : Team.getAliveTeams()) {
                        for (Player p : t.getAlivePlayers()) {
                            if (t.getAlivePlayers().size() <= t.getPlayers().size())
                                p.setCompassTarget(Map.getActiveMap().getCenterLocation());
                            else
                                p.setCompassTarget(t.getClosestPlayer(p).getLocation());
                        }
                    }
                } else {
                    for (Gamer g : Gamer.getAliveGamers()) {
                        g.getPlayer().setCompassTarget(Map.getActiveMap().getCenterLocation());
                    }
                }

            }
        }, 0, 20 * 10);
        gametime = 0;
        gamePID = Bukkit.getScheduler().scheduleSyncRepeatingTask(SG.pl, new Runnable() {
            int countdown = 10;
            int dmcountdown = 10;

            World w = Map.getActiveMap().getWorld();

            @Override
            public void run() {
                if (gametime <= 15 && gametime > 5) {
                    //ChatUtil.broadcast("&cStarting in &4&l" + countdown + " &r&cseconds!");
                    for (Gamer gl : Gamer.getGamers()) {
                        gl.getPlayer().playSound(Map.getActiveMap().getCenterLocation(), Sound.BLOCK_NOTE_BLOCK_COW_BELL, 20, 1);
                        gl.getPlayer().sendTitle(ChatColor.translateAlternateColorCodes('&', "&4") + String.valueOf(countdown), "", 5, 10, 5);
                    }
                    countdown--;
                }
                if (gametime == 16) {
                    unregisterStartEvents();
                    registerGraceEvents();
                    registerGameEvents();

                    for (Gamer gl : Gamer.getGamers()) {
                        gl.getPlayer().playSound(Map.getActiveMap().getCenterLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 20, 1);
                        gl.getPlayer().sendTitle(ChatColor.translateAlternateColorCodes('&', "&bRun!"), ChatColor.translateAlternateColorCodes('&', "&eGrace period lasts for 5 seconds."), 5, 10 * 3, 10);
                    }

                    /*
                    ChatUtil.broadcast("&b&lThe game has started!");
                    ChatUtil.broadcast("&eThere is a grace period for 5 seconds.");
                     */
                }
                if (gametime == 22) {
                    unregisterGraceEvents();
                    for (Gamer gl : Gamer.getGamers()) {
                        gl.getPlayer().playSound(Map.getActiveMap().getCenterLocation(), Sound.AMBIENT_CAVE, 20, 1);
                    }
                    ChatUtil.broadcast("&6Grace period is over! &lFight&r&6!");
                }
                if (gametime == (dmtime / 60 - 10) * 60) {
                    ChatUtil.broadcast("&cDeathmatch in &4&l10 &cminutes.");
                    w.setStorm(true);
                }
                if (gametime == (dmtime / 60 - 5) * 60) {
                    ChatUtil.broadcast("&cDeathmatch in &4&l5 &cminutes.");

                }
                if (gametime == (dmtime / 60 - 1) * 60) {
                    ChatUtil.broadcast("&cDeathmatch in &4&l1 &cminute. Players will be teleported soon.");
                }
                if (gametime == dmtime - 20) {

                    w.setStorm(false);
                    w.setClearWeatherDuration(3600);
                    w.setTime(11000);
                    ChatUtil.broadcast("&cTeleporting players to deathmatch. Waiting for players to load world.");
                    unregisterGameEvents();
                    registerStartEvents();
                    ChestHandler.fillAllChests(Map.getActiveMap().getFileName());
                    WorldBorder border = w.getWorldBorder();
                    border.setSize(68);
                    border.setCenter(Map.getActiveMap().getCenterLocation());
                    int i = 0;
                    ArrayList<Integer> usedSpawns = new ArrayList<>();
                    Random rand = new Random();
                    for (Gamer pla : Gamer.getAliveGamers()) {
                        i = rand.nextInt(24);
                        while (usedSpawns.contains(i)) {
                            i = rand.nextInt(24);
                        }
                        ChatUtil.sendMessage(clogger, "Spawn assigned to " + pla.getName() + ": " + i);
                        usedSpawns.add(i);
                        Player p = pla.getPlayer();
                        LocUtil.teleportToGame(p, i);
                        p.setGameMode(GameMode.ADVENTURE);
                    }
                    usedSpawns.clear();
                }

                if (gametime >= (dmtime - 10) && gametime < dmtime) {
                    for (Gamer gl : Gamer.getGamers()) {
                        gl.getPlayer().playSound(Map.getActiveMap().getCenterLocation(), Sound.BLOCK_NOTE_BLOCK_COW_BELL, 20, 1);
                        gl.getPlayer().sendTitle(ChatColor.translateAlternateColorCodes('&', "&4") + String.valueOf(dmcountdown), "", 5, 10, 5);
                    }
                    //ChatUtil.broadcast("&cDeathmatch in &4&l" + dmcountdown + " &cseconds.");
                    dmcountdown--;
                }
                if (gametime == dmtime) {
                    Deathmatch();
                }

                gametime++;
            }

        }, 0, 20);
    }

    private static void Deathmatch() {
        unregisterStartEvents();
        registerGraceEvents();
        registerGameEvents();

        for (Gamer gl : Gamer.getGamers()) {
            gl.getPlayer().playSound(Map.getActiveMap().getCenterLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 20, 1);
            gl.getPlayer().sendTitle(ChatColor.translateAlternateColorCodes('&', "&cFight!"), ChatColor.translateAlternateColorCodes('&', ""), 5, 10 * 3, 10);
        }
        startDeathmatchTimer();
    }

    private static void startDeathmatchTimer() {
        DMPID = Bukkit.getScheduler().scheduleSyncRepeatingTask(SG.pl, new Runnable() {

            @Override
            public void run() {
                if (dm == 0) {
                    ChatUtil.broadcast("&cGame will end in &4&l5 &r&cminutes!");
                    System.out.println("5 minutes till stop.");
                }
                if (dm == 1 * 60) {
                    System.out.println("4 minutes till stop.");
                }
                if (dm == 2 * 60) {
                    ChatUtil.broadcast("&cGame will end in &4&l3 &r&cminutes!");
                    System.out.println("3 minutes till stop.");
                    Bukkit.getWorld(Map.getActiveMap().getFileName()).setTime(18000);
                }
                if (dm == 4 * 60) {
                    ChatUtil.broadcast("&cGame will end in &4&l1 &r&cminute!");
                    System.out.println("1 minute till stop.");
                }
                if (dm == 5 * 60) {
                    ChatUtil.broadcast("&cEnding game. No win due to multiple players left.");
                }
                if (dm == (5 * 60) + 5) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (config.getBoolean("bungeecord")) sendToServer(p, config.getString("lobbyserver"));
                    }
                    cancelGame();
                }
                dm++;
            }
        }, 0, 20);
    }

    public static void clearPlayer(Player p) {
        p.getInventory().clear();
        p.getInventory().setHelmet(null);
        p.getInventory().setChestplate(null);
        p.getInventory().setLeggings(null);
        p.getInventory().setBoots(null);
        p.setHealth(20);
        p.setFoodLevel(25);
        p.setFireTicks(0);
        p.setFallDistance(0.0F);
        p.setLevel(0);
        p.setExp(0);
        for (PotionEffect pe : p.getActivePotionEffects())
            p.removePotionEffect(pe.getType());
    }

    public static void win(Team team, Player player) {
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
                if (p.getUniqueId() == Gamer.getAliveGamers().get(0).getUUID())
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
                if (districts_mode) {
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
                if (districts_mode) {
                    for (Player p : Team.getAliveTeams().get(0).getAlivePlayers()) {
                        PointSystem.addPoints(p, 200);
                        Gamer g = Gamer.getGamer(p);
                        if (config.getBoolean("mysql.enabled")) g.addWin();
                    }
                } else {
                    Gamer g = Gamer.getAliveGamers().get(0);
                    PointSystem.addPoints(g.getPlayer(), 200);

                    if (config.getBoolean("mysql.enabled")) g.addWin();
                }
                cancelGame();
            }
        }, 20L * 10);
    }


    private static void cancelGame() {
        for (Player pl : Bukkit.getOnlinePlayers()) {
            if (config.getBoolean("bungeecord")) sendToServer(pl, config.getString("lobbyserver"));
            else {
                pl.setGameMode(GameMode.ADVENTURE);
                clearPlayer(pl);
                pl.showPlayer(SG.pl, pl);
                LocUtil.teleportToLobby(pl);
            }
            //pl.kickPlayer(ChatColor.translateAlternateColorCodes('&', "&r&6District &6&l" + Team.getAliveTeams().get(0).getName() + " &r&6won!\n&cServer restarting."));
            // NEW IMPLEMENTATION

        }

        // CLEAR ALL DATA (VOTES, RANDOM MAP IDS, GAMERS '
        // (need to find the logic here, so we know who is a gamer and who is spectator without it being removed with the clearing)
        // Currently clears gamers that are NOT spectators from the start. Dead players will be cleared.
        Bukkit.getScheduler().cancelTask(DMPID);
        Bukkit.getScheduler().cancelTask(gamePID);
        Bukkit.getScheduler().cancelTask(compassPID);
        Gamer.clearRealGamers();
        for (Gamer g : Gamer.getRealGamers()) {
            ChatUtil.sendMessage(cmd, g.getName());
        }
        if (SG.districts_mode)
            ScoreboardUtil.resetScoreboard();
        Team.clearInfo();
        VoteHandler.clearVotes();

        // Reset config information
        minPlayers = SG.pl.getConfig().getConfigurationSection("settings").getInt("settings.");
        Team.setTeamSize(SG.config.getConfigurationSection("settings").getInt("teamSize"));
        ChatUtil.setChatFormat(SG.config.getConfigurationSection("settings.chat").getString("format"));


        // UNREGISTER AND REGISTER CORRECT LISTENERS (registerPreEvents)
        unregisterGameEvents();
        registerPreEvents();

        // UNLOAD WORLD AND ROLLBACK
        ChatUtil.sendMessage(cmd, "Rolling back map " + Map.getActiveMap().getMapName());
        ResetMap.handleReset(SG.pl);
        // Clear Map info
        Map.clearInfo();
        // Choose 6 random maps for vote cycle
        Map.chooseMaps();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.isOp()) {
                Gamer.getGamer(p);
                ChatUtil.sendMessage(cmd, "Added " + p.getName() + " to gamers");
            }
        }

        SG.startPreGameCountdown();
        // SET MOTD
        List<String> motd = new ArrayList<String>();
        motd.add("&6SurvalGames&7: &aIn Lobby");
        motd.add("&a" + (24 - Gamer.getAliveGamers().size()) + " spots left!");
        ChatUtil.setMOTD(motd);

        // Stop server (OLD IMPLEMENTATION)
        // Bukkit.shutdown();
    }

    public static void spawnFireworks(boolean dm) {
        if (dm) {
            fireworksPID = Bukkit.getScheduler().scheduleSyncRepeatingTask(SG.pl, new Runnable() {


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
                        Bukkit.getScheduler().cancelTask(fireworksPID);
                    }

                }
            }, 0, 5);
        } else {
            Location loc = Gamer.getAliveGamers().get(0).getPlayer().getLocation();
            for (int i = 1; i <= 4; i++) {
                if (districts_mode) {
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
}