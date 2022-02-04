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
import java.util.Random;
import java.util.logging.Level;
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
import org.bukkit.scoreboard.*;

public class SG extends JavaPlugin {

    public static int cdId;

    public static Connection connection;

    public static FileConfiguration config;
    public static FileConfiguration data = SettingsManager.getInstance().getData();
    public static ConsoleCommandSender cmd = Bukkit.getConsoleSender();

    public static int gamePID, PreGamePID, DMPID;
    public static int pretime, gametime, dmtime, dm, minPlayers;

    public static SG pl;

    public static Logger logger;
    public static ConsoleCommandSender clogger;

    public static ScoreboardUtil SBU;

    @Override
    public void onLoad() {
        configs();
        SettingsManager.getInstance().setup(this);

        String lastmap = config.getString("lastmap");
        if (lastmap == null || lastmap.equalsIgnoreCase("null")) {
            cmd.sendMessage("No maps played yet.");
        } else {
            cmd.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eRolling back last map: " + lastmap));
            ResetMap.rollback(lastmap, this);
        }
    }

    @Override
    public void onEnable() {
        logger = getLogger();
        clogger = getServer().getConsoleSender();
        pl = this;
        registerDistricts();
        registerCommands();
        registerPreEvents();

        SBU = new ScoreboardUtil();

        minPlayers = getConfig().getConfigurationSection("settings").getInt("settings.");
        Team.setTeamSize(SG.config.getConfigurationSection("settings").getInt("teamSize"));

        if (getConfig().getConfigurationSection("settings.chat").getBoolean("customprefix")) {
            ChatUtil.setPrefix(SG.config.getConfigurationSection("settings.chat").getString("prefix"));
        }

        dmtime = getConfig().getInt("settings.deathmatch") * 60;
        data = SettingsManager.getInstance().getData();

        ChatUtil.sendMessage(clogger, ChatColor.RED + "---------------------------------------");
        ChatUtil.sendMessage(clogger, ChatColor.GREEN + "Enabling SurvivalGames by "
                + getDescription().getAuthors().toString().replace("[", "").replace("]", ""));
        ChatUtil.sendMessage(clogger, ChatColor.RED + "---------------------------------------");

        if (data.getConfigurationSection("arenas") == null) {
            ChatUtil.sendMessage(clogger, ChatColor.translateAlternateColorCodes('&', "&4No arenas created!"));
        } else {
            ChatUtil.sendMessage(clogger, "Registering maps");
            for (String maps : data.getConfigurationSection("arenas").getKeys(false)) {
                Map map = new Map(data.getString("arenas." + maps + ".name"), maps);
                ResetMap.createBackup(map, this);
                WorldCreator worldc = new WorldCreator(map.getFileName());
                World world = worldc.createWorld();
                //ChatUtil.sendMessage(clogger, "World '" + world.getName() + "' imported"); //debug
            }
            Random rand = new Random();

            if (Map.getAllMaps().size() >= 6) {
                //ChatUtil.sendMessage(clogger, "Size is bigger than 6"); //debug
                for (int i = 0; i < 6; i++) {
                    Map map = Map.getAllMaps().get(rand.nextInt(Map.getAllMaps().size()));
                    Map.setTempId(map, i + 1);
                    Map.setVoteMaps();
                }
            } else {
                //clogger.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eSize: " + Map.getAllMaps().size())); //debug
                for (int i = 0; i < Map.getAllMaps().size(); i++) {
                    Map map = Map.getAllMaps().get(i);
                    Map.setTempId(map, i + 1);
                    //ChatUtil.sendMessage(clogger, map.getMapName() + " : " + Map.getTempId(map)); //Debug
                }
                Map.setVoteMaps();
            }
        }
        if (config.getBoolean("mysql.enabled")) {
            openConnection();
        }
        startPreGameCountdown();
    }

    @Override
    public void onDisable() {
        if (config.getBoolean("mysql.enabled")) {
            try {
                if (connection != null && connection.isClosed())
                    connection.close();
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
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + config.getString("mysql.host") + ":" + config.getString("mysql.port") + "/"
                            + config.getString("mysql.database"),
                    config.getString("mysql.username"), config.getString("mysql.password"));
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

    public static void registerGameEvents() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new IngameListener(), SG.pl);
    }

    private static Listener preListener;
    private static Listener startListener;
    private static Listener graceListener;

    private void registerPreEvents() {
        preListener = new JoinListener();
        // MinecraftServer.getServer().setMotd(" " +
        // SG.config.getString("settings.motd.lobby"));
        Bukkit.getPluginManager().registerEvents(preListener, this);
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
                    ChatUtil.broadcast(ChatColor.translateAlternateColorCodes('&',
                            "&6==== &bSurvivalGames: &eVoting &6===="));
                    ChatUtil.broadcast("Vote: [/vote <id>]");
                    for (Map map : Map.getVoteMaps()) {
                        ChatUtil.broadcast(Map.getTempId(map) + " > " + map.getMapName() + " ["
                                + VoteHandler.getVotesMap(map) + " votes]");
                    }
                    ChatUtil.broadcast(ChatColor.translateAlternateColorCodes('&',
                            "&6=================================="));

                    if (pretime == 60) {
                        ChatUtil.broadcast("Game starting in " + pretime / 60 + " minute.");
                    } else if (pretime > 60) {
                        ChatUtil.broadcast("Game starting in " + pretime / 60 + " minutes.");
                    }
                    ChatUtil.broadcast(ChatColor.AQUA + "" + Gamer.getGamers().size() + "/24" + ChatColor.GREEN
                            + " tributes waiting to play.");
                }


                if (pretime == 45 || pretime == 30 || pretime == 15 || (pretime >= 0 && pretime <= 10)) {
                    ChatUtil.broadcast("Game starting in " + pretime + " seconds.");
                    for (Gamer gl : Gamer.getGamers()) {
                        gl.getPlayer().playSound(Map.getActiveMap().getCenterLocation(), Sound.BLOCK_NOTE_BLOCK_COW_BELL, 20, 1);
                    }

                }
                if (pretime == 0) {
                    Game.start();
                }
                pretime--;
            }
        }, 0, 20);
    }

    public static void startGameTimer() {
        gamePID = Bukkit.getScheduler().scheduleSyncRepeatingTask(SG.pl, new Runnable() {
            int countdown = 10;
            int dmcountdown = 10;

            @Override
            public void run() {
                if (gametime <= 15 && gametime > 5) {
                    ChatUtil.broadcast("&cStarting in &4&l" + countdown + " &r&cseconds!");
                    for (Gamer gl : Gamer.getGamers()) {
                        gl.getPlayer().playSound(Map.getActiveMap().getCenterLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 20, 1);
                    }
                    countdown--;
                }
                if (gametime == 16) {
                    unregisterStartEvents();
                    registerGameEvents();
                    registerGraceEvents();
                    for (Gamer gl : Gamer.getGamers()) {
                        gl.getPlayer().playSound(Map.getActiveMap().getCenterLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 20, 1);
                    }
                    ChatUtil.broadcast("&b&lThe game has started!");
                    ChatUtil.broadcast("&eThere is a grace period for 5 seconds.");
                }
                if (gametime == 22) {
                    unregisterGraceEvents();
                    for (Gamer gl : Gamer.getGamers()) {
                        gl.getPlayer().playSound(Map.getActiveMap().getCenterLocation(), Sound.ENTITY_GHAST_WARN, 20, 1);
                    }
                    ChatUtil.broadcast("&6Grace period is over! &lFight&r&6!");
                }
                if (gametime == (dmtime / 60 - 10) * 60) {
                    ChatUtil.broadcast("&cDeathmatch in &4&l10 &cminutes.");
                }
                if (gametime == (dmtime / 60 - 5) * 60) {
                    ChatUtil.broadcast("&cDeathmatch in &4&l5 &cminutes.");
                    Bukkit.getWorld(Map.getActiveMap().getFileName()).setTime(8000);
                }
                if (gametime == (dmtime / 60 - 1) * 60) {
                    ChatUtil.broadcast("&cDeathmatch in &4&l1 &cminute. Players will be teleported soon.");
                }
                if (gametime == dmtime - 20) {
                    World w = Map.getActiveMap().getWorld();
                    w.setClearWeatherDuration(3600);
                    w.setTime(11000);
                    ChatUtil.broadcast("&cTeleporting players to deathmatch. Waiting for players to load world.");
                    registerStartEvents();
                    ChestHandler.fillAllChests(Map.getActiveMap().getFileName());
                    WorldBorder border = w.getWorldBorder();
                    border.setSize(72);
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
                    ChatUtil.broadcast("&cDeathmatch in &4&l" + dmcountdown + " &cseconds.");
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
        registerGameEvents();


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
                        if (config.getBoolean("bungeecord"))
                            sendToServer(p, config.getString("lobbyserver"));
                        else
                            p.kickPlayer(
                                    ChatColor.translateAlternateColorCodes('&', "&cNoone won!\n&cServer restarting"));
                    }
                    Bukkit.getServer().shutdown();
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

    public static void win(Team team) {

        if (team.getAlivePlayers().size() > 1) {
            ChatUtil.broadcast("&6&l District " + team.getName() + "&r won the SurvivalGames with multiple players alive!");
        } else if (team.getAlivePlayers().size() == 1) {
            ChatUtil.broadcast("&6&l" + team.getAlivePlayers().get(0).getName() + "&r from District " + team.getName() + " won the SurvivalGames!");
        }
        spawnFireworks(Map.getActiveMap().getCenterLocation(), 5);
        for (Player pl : Bukkit.getOnlinePlayers()) {
            //pl.playSound(Map.getActiveMap().getCenterLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 20, 1);
            pl.playSound(Map.getActiveMap().getCenterLocation(), Sound.MUSIC_DISC_CAT, 20, 1);
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(SG.pl, new Runnable() {

            @Override
            public void run() {
                for (Player p : Team.getAliveTeams().get(0).getAlivePlayers()) {
                    PointSystem.addPoints(p, 200);
                    Gamer g = Gamer.getGamer(p);
                    if (config.getBoolean("mysql.enabled"))
                        g.addWin();
                }
                for (Player pl : Bukkit.getOnlinePlayers()) {
                    if (config.getBoolean("bungeecord"))
                        sendToServer(pl, config.getString("lobbyserver"));
                    else
                        pl.kickPlayer(ChatColor.translateAlternateColorCodes('&',
                                "&r&6District &6&l" + Team.getAliveTeams().get(0).getName() + " &r&6won!\n&cServer restarting."));
                }
                Bukkit.getServer().shutdown();
            }
        }, 20L * 20);
    }

    public static void spawnFireworks(Location location, int amount) {
        Location loc = location;
        Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();

        fwm.setPower(2);
        fwm.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(Color.LIME).flicker(true)
                .build());

        fw.setFireworkMeta(fwm);
        fw.detonate();

        for (int i = 0; i < amount; i++) {
            Firework fw2 = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
            fw2.setFireworkMeta(fwm);
        }
    }


}