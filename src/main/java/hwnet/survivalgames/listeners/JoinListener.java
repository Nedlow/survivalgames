package hwnet.survivalgames.listeners;

import hwnet.survivalgames.handlers.*;
import hwnet.survivalgames.utils.GameBoard;
import hwnet.survivalgames.utils.LocUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Bat;
import org.bukkit.entity.GlowSquid;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import hwnet.survivalgames.SG;
import hwnet.survivalgames.utils.ChatUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoinPre(PlayerLoginEvent event) {
        if (event.getResult() == Result.KICK_FULL) event.setResult(Result.ALLOWED);
    }


    public final Block getTargetBlock(Player player, int range) {
        return player.getTargetBlock((Set<Material>) null, range);
    }

    @EventHandler
    public void onVoteSignClick(PlayerInteractEvent e) {
        // Check for proper case
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!(e.getClickedBlock().getState() instanceof Sign)) return;
        if (!ClickSign.signExists(e.getClickedBlock().getState().getLocation())) return;
        //Debug
        //ChatUtil.sendMessage(e.getPlayer(), "Clicked a sign");

        // Get ClickSign instance at given location
        ClickSign sign = ClickSign.getSign(e.getClickedBlock().getState().getLocation());
        // Check for proper case
        if (sign.getType() != ClickSign.SignType.VOTE) return;
        Player p = e.getPlayer();
        p.performCommand("vote " + (sign.getVoteID() + 1));
    }

    @EventHandler
    public void onSignBreak(BlockBreakEvent e) {
        if (!(e.getBlock().getState() instanceof Sign)) return;
        if (!ClickSign.signExists(e.getBlock().getState().getLocation())) return;

        ClickSign sign = ClickSign.getSign(e.getBlock().getState().getLocation());

        if (sign.getType() == ClickSign.SignType.VOTE)
            ChatUtil.sendMessage(e.getPlayer(), "Sign removed: " + sign.getType().toString() + " " + sign.getVoteID());
        else
            ChatUtil.sendMessage(e.getPlayer(), "Sign removed: " + sign.getType().toString());
        sign.remove();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        FileConfiguration c = SG.config;
        LocUtil.teleportToLobby(p);
        SG.clearPlayer(p);
        Gamer g = Gamer.getGamer(p);
        Bukkit.getScheduler().scheduleSyncDelayedTask(SG.pl, new Runnable() {
            @Override
            public void run() {
                if (!PointSystem.load(p)) {
                    PointSystem.initialize(p);
                }
                if (g.hasResourcePackEnabled())
                    p.performCommand("resourcepack enable false");
                // SCOREBOARD
                if (!GameBoard.hasBoard(p)) {
                    GameBoard gb = new GameBoard(p);
                    gb.intializeLobby();
                }
            }
        }, 20);


        p.setGameMode(GameMode.ADVENTURE);
        if (p.hasPermission("sg.admin")) {
            ChatUtil.sendMessage(p, "Joined as admin. Type /join to join the game");
            g.setAlive(false);
            g.setSpectator(true);
            ChatUtil.sendMessage(p, ChatColor.AQUA + "" + Gamer.getRealGamers().size() + "/24" + ChatColor.GREEN + " tributes waiting to play.");
            if (!SG.checkCanStart())
                ChatUtil.sendMessage(p, "" + (SG.minPlayers - Gamer.getRealGamers().size()) + " more tributes needed to start game.");
        } else {
            ChatUtil.sendVoteMenu(p);
            ChatUtil.broadcast(ChatColor.AQUA + "" + Gamer.getRealGamers().size() + "/24" + ChatColor.GREEN + " tributes waiting to play.");
            if (!SG.checkCanStart())
                ChatUtil.broadcast((SG.minPlayers - Gamer.getRealGamers().size()) + " more tributes needed to start game.");
        }
        if (!g.hasResourcePackEnabled())
            ChatUtil.sendMessage(p, "We recommend the use of the SapixCraft resource-pack. To use this pack, use /resourcepack enable");
        else p.performCommand("resourcepack enable false");

        List<String> motd = new ArrayList<String>();
        // SPECIAL CHARS: ⚝ ✰ ✩
        motd.add(ChatUtil.centerText("&d&l✩ &r&6SurvivalGames: &aIn Lobby &d&l✩", 69));
        motd.add(ChatUtil.centerText("&e▶ &r&a" + (24 - Gamer.getRealGamers().size()) + " spots left! &e◀", 59));
        ChatUtil.setMOTD(motd);

        if (SG.config.getBoolean("settings.bungeecord")) {
            ItemStack s = new ItemStack(Material.NETHER_STAR, 1);
            ItemMeta m = s.getItemMeta();
            m.setDisplayName(ChatColor.RED + "Leave");
            s.setItemMeta(m);
            p.getInventory().setItem(8, s);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Gamer g = Gamer.getGamer(event.getPlayer());
        PointSystem.save(event.getPlayer());
        g.remove();
        List<String> motd = new ArrayList<String>();
        motd.add("&6SurvalGames&7: &aIn Lobby");
        motd.add("&a" + (24 - Gamer.getRealGamers().size()) + " spots left!");
        ChatUtil.setMOTD(motd);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        int points = PointSystem.getPoints(e.getPlayer().getUniqueId());
        String playerName = e.getPlayer().getName();
        if (e.getPlayer().hasPermission("sg.admin")) {
            playerName = ChatColor.RED + playerName + ChatColor.RESET;
        }
        e.setFormat(ChatUtil.getFormat().replace("%points", String.valueOf(points)).replace("%name", playerName).replace("%msg", e.getMessage()));
    }


    @EventHandler
    public void onWeatherChange(WeatherChangeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e) {
        if (e instanceof Bat || e instanceof GlowSquid) e.setCancelled(true);
    }

    @EventHandler
    public void onLeaveClick(PlayerInteractEvent e) {
        if (SG.config.getBoolean("settings.bungeecord")) {
            if (e.getItem() == null) return;
            if (e.getItem().getType().equals(Material.NETHER_STAR)) {
                SG.sendToServer(e.getPlayer(), SG.config.getString("settings.lobbyserver"));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMove(InventoryClickEvent e) {
        ItemStack i = e.getWhoClicked().getInventory().getItem(0);
        if (i != null) {
            if (e.getSlot() == 0 && i.getType() == Material.NETHER_STAR) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onMOTD(ServerListPingEvent e) {
        e.setMotd(ChatUtil.getMOTD());
    }

    /*
    @EventHandler
    public void onMOTD(ServerListPingEvent e) {
        e.setMotd(ChatColor.translateAlternateColorCodes('&', SG.config.getString("settings.motd.lobby").replace("%newline%", "\n")));
    }

     */

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) event.setCancelled(true);
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) event.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) event.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) event.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }


    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) event.setCancelled(true);
    }
}