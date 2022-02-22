package hwnet.survivalgames;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import com.sk89q.worldedit.WorldEdit;
import hwnet.survivalgames.commands.*;
import hwnet.survivalgames.handlers.*;
import hwnet.survivalgames.handlers.Team;
import hwnet.survivalgames.listeners.*;
import hwnet.survivalgames.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

public class SG extends JavaPlugin {

    public static Connection connection;

    public static FileConfiguration config;
    public static FileConfiguration data = SettingsManager.getInstance().getData();
    public static ConsoleCommandSender cmd = Bukkit.getConsoleSender();

    public static int gamePID, PreGamePID, DMPID, compassPID, fireworksPID;
    public static int pretime, gametime, dmtime, dm, minPlayers, maxPlayers;

    public static boolean districts_mode, devMode;

    public static SG pl;

    public static Logger logger;
    public static ConsoleCommandSender clogger;

    public static SpectatorGUI specGUI;

    private static boolean mysql;
    private static String sqlite_url;

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

        devMode = false;

        minPlayers = getConfig().getConfigurationSection("settings").getInt("minPlayers");
        Team.setTeamSize(SG.config.getConfigurationSection("settings").getInt("teamSize"));
        ChatUtil.setChatFormat(SG.config.getConfigurationSection("settings.chat").getString("format"));
        maxPlayers = 24;

        districts_mode = config.getBoolean("settings.districts_mode");
        devMode = config.getBoolean("settings.devmode");
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
                ResetMap.createBackup(map, this, false);
                WorldCreator world = new WorldCreator(map.getFileName());
                world.createWorld();
            }
            ChatUtil.sendMessage(cmd, "A total of " + Map.getAllMaps().size() + " maps.");
            Map.chooseMaps();
        }

        List<String> motd = new ArrayList<String>();
        // SPECIAL CHARS: ⚝ ✰ ✩
        motd.add(ChatUtil.centerText("&d&l✩ &r&6SurvivalGames: &aIn Lobby &d&l✩", 69));
        motd.add(ChatUtil.centerText("&e▶ &r&a" + (24 - Gamer.getRealGamers().size()) + " spots left! &e◀", 59));
        ChatUtil.setMOTD(motd);

        mysql = config.getConfigurationSection("mysql").getBoolean("enabled");
        // Open connection to either mysql or sqlite.
        sqlite_url = SG.pl.getDataFolder() + File.separator + "sqlite.db";
        openConnection();
        createTables();

        PointSystem.FetchTopWinFromDB();
        PointSystem.FetchTopKillFromDB();
        PointSystem.FetchTopDeathsFromDB();


        // Temporary mob list
        IngameListener.addMobsToList();

        specGUI = new SpectatorGUI();
        registerMenus();
        PointSystem.initializeTopStats();
        ClickSign.importSigns();
        ClickSign.updateSigns();
        World w = Bukkit.getWorld("lobby");
        for (Entity e : w.getEntities()) {
            if (e instanceof Player || e instanceof ArmorStand) continue;
            e.remove();
        }
        if (devMode) {
            devMode(cmd, true);
        }
    }

    @Override
    public void onDisable() {
        try {
            if (connection != null && connection.isClosed()) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (config.getBoolean("settings.bungeecord")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                sendToServer(p, config.getString("lobbyserver"));
            }
        }
    }

    public static boolean checkCanStart() {
        if (Gamer.getRealGamers().size() >= minPlayers) {
            startPreGameCountdown();
            return true;
        }
        return false;
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
            if (mysql) {
                connection = DriverManager.getConnection("jdbc:mysql://" + config.getString("mysql.host") + ":" + config.getString("mysql.port") + "/" + config.getString("mysql.database"), config.getString("mysql.username"), config.getString("mysql.password"));
            } else {
                connection = DriverManager.getConnection("jdbc:sqlite:" + sqlite_url);
                DatabaseMetaData meta = connection.getMetaData();
                ChatUtil.sendMessage(cmd, "The driver name is " + meta.getDriverName());
                ChatUtil.sendMessage(cmd, "Connection established.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static void createTables() {
        String sql = "CREATE TABLE IF NOT EXISTS playerdata (uuid text PRIMARY KEY, games integer, wins integer, kills integer, deaths integer, points integer);";

        try {
            Statement stmt = connection.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
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
        getCommand("spawn").setExecutor(new Spawn());
        getCommand("addtime").setExecutor(new AddTime());
        getCommand("softstop").setExecutor(new Stop());
        getCommand("devmode").setExecutor(new Devmode());
        getCommand("resourcepack").setExecutor(new ResourcepackEn());
        getCommand("setsign").setExecutor(new SignCmd());
        getCommand("cancel").setExecutor(new CancelCMD());

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new ChatHandler(), this);
        if (config.getBoolean("settings.bungeecord"))
            getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }


    private static Listener preListener, startListener, graceListener, ingameListener, menuListener, devListener;

    private static void registerMenus() {
        menuListener = new GUIListener();
        Bukkit.getPluginManager().registerEvents(menuListener, SG.pl);
    }

    private static void registerDevMode() {
        devListener = new DevListener();
        Bukkit.getPluginManager().registerEvents(devListener, SG.pl);
    }

    private static void unregisterDevMode() {
        HandlerList.unregisterAll(devListener);
    }

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
                    ChatUtil.broadcast(ChatColor.AQUA + "" + Gamer.getAliveGamers().size() + "/24" + ChatColor.GREEN + " tributes waiting to play.");
                }


                if (pretime == 45 || pretime == 30 || pretime == 15) {
                    ChatUtil.broadcast("Game starting in " + pretime + " seconds.");
                }
                if ((pretime >= 0 && pretime <= 10)) {
                    for (Gamer gl : Gamer.getGamers()) {
                        gl.getPlayer().sendTitle(ChatColor.translateAlternateColorCodes('&', "&eGame Starting!"), ChatColor.translateAlternateColorCodes('&', ("&a" + String.valueOf(pretime))), 10, 15, 10);
                    }
                }
                if (pretime == 0) {
                    Game.start();
                }
                for (Player p : Bukkit.getOnlinePlayers()) {
                    GameBoard.getBoard(p).update(GameBoard.ScoreType.TIME_LOBBY, "Time till start", "Time till start: " + ChatUtil.formatTime(pretime));
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
                            else p.setCompassTarget(t.getClosestPlayer(p).getLocation());
                        }
                    }
                } else {
                    for (Gamer g : Gamer.getAliveGamers()) {
                        g.getPlayer().setCompassTarget(Map.getActiveMap().getCenterLocation());
                    }
                }

            }
        }, 0, 20 * 10);
        gametime = -15;
        gamePID = Bukkit.getScheduler().scheduleSyncRepeatingTask(SG.pl, new Runnable() {
            int countdown = 10;
            int dmcountdown = 10;

            World w = Map.getActiveMap().getWorld();

            @Override
            public void run() {
                if (gametime <= 0 && gametime > -10) {
                    //ChatUtil.broadcast("&cStarting in &4&l" + countdown + " &r&cseconds!");
                    for (Gamer gl : Gamer.getGamers()) {
                        gl.getPlayer().playSound(Map.getActiveMap().getCenterLocation(), Sound.BLOCK_NOTE_BLOCK_COW_BELL, 20, 1);
                        gl.getPlayer().sendTitle(ChatColor.translateAlternateColorCodes('&', "&4") + String.valueOf(countdown), "", 5, 10, 5);
                    }
                    countdown--;
                }
                if (gametime == 1) {
                    unregisterStartEvents();
                    registerGraceEvents();
                    registerGameEvents();

                    for (Gamer gl : Gamer.getGamers()) {
                        gl.getPlayer().playSound(Map.getActiveMap().getCenterLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 20, 1);
                        gl.getPlayer().sendTitle(ChatColor.translateAlternateColorCodes('&', "&bRun!"), ChatColor.translateAlternateColorCodes('&', "&eGrace period lasts for 10 seconds."), 5, 10 * 3, 10);
                    }

                    /*
                    ChatUtil.broadcast("&b&lThe game has started!");
                    ChatUtil.broadcast("&eThere is a grace period for 5 seconds.");
                     */
                }
                if (gametime == 11) {
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
                if (gametime == dmtime) {
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
                    /*
                    for (Gamer gl : Gamer.getGamers()) {
                        gl.getPlayer().playSound(Map.getActiveMap().getCenterLocation(), Sound.BLOCK_NOTE_BLOCK_COW_BELL, 20, 1);
                        gl.getPlayer().sendTitle(ChatColor.translateAlternateColorCodes('&', "&4") + String.valueOf(dmcountdown), "", 5, 10, 5);
                    }
                    */
                    ChatUtil.broadcast("&cDeathmatch in &4&l" + dmcountdown + " &cseconds.");
                    dmcountdown--;
                }
                if (gametime == dmtime + 10) {
                    Deathmatch();
                }

                if (gametime >= 0) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        GameBoard.getBoard(p).update(GameBoard.ScoreType.TIME_GAME, "Time left", "&bTime left: &a" + ChatUtil.formatTime(dmtime - gametime));
                    }
                } else {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        GameBoard.getBoard(p).update(GameBoard.ScoreType.TIME_GAME, "Time left", "&bTime left: &a" + ChatUtil.formatTime(dmtime));
                    }
                }
                gametime++;
            }

        }, 0, 20);
    }

    private static void Deathmatch() {
        unregisterStartEvents();
        unregisterGraceEvents();
        registerGameEvents();

        GameState.setState(GameState.ENDGAME);

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
                    ChatUtil.broadcast("&cEnding game. No win due to multiple tributes left.");
                }
                if (dm == (5 * 60) + 5) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (config.getBoolean("bungeecord")) sendToServer(p, config.getString("lobbyserver"));
                    }
                    Game.cancelGame();
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


    public static void devMode(CommandSender sender, boolean override) {
        if (devMode && !override) {
            ChatUtil.sendMessage(sender, "Developer mode disabled. Starting game engine.");
            config.set("settings.devmode", false);
            pl.saveConfig();
            unregisterDevMode();
            registerPreEvents();

            for (Player p : Bukkit.getOnlinePlayers()) {
                // SCOREBOARD
                if (!GameBoard.hasBoard(p)) {
                    GameBoard gb = new GameBoard(p);
                    gb.intializeLobby();
                }
            }

            devMode = false;
        } else {
            ChatUtil.sendMessage(sender, "Developer mode enabled. Stopping game engine.");
            config.set("settings.devmode", true);
            pl.saveConfig();
            unRegisterPreEvents();
            unregisterStartEvents();
            unregisterGraceEvents();
            unregisterGameEvents();
            registerDevMode();
            Bukkit.getWorld(SG.config.getString("lobby.world")).setClearWeatherDuration(3600 * 20);
            Bukkit.getScheduler().cancelTask(PreGamePID);
            List<String> motd = new ArrayList<>();
            motd.add("&6Survival Games&7: &cDeveloper Mode");
            motd.add("&5-= Staff-Only =-");
            ChatUtil.setMOTD(motd);
            for (Player p : Bukkit.getOnlinePlayers()) {
                // SCOREBOARD
                p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }
            devMode = true;
        }
    }
}