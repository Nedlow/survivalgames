package hwnet.survivalgames.listeners;

import hwnet.survivalgames.handlers.*;
import hwnet.survivalgames.utils.LocUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Bat;
import org.bukkit.entity.GlowSquid;
import org.bukkit.entity.Player;
import org.bukkit.entity.Squid;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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

public class JoinListener implements Listener {

    @EventHandler
    public void onJoinPre(PlayerLoginEvent event) {
        if (event.getResult() == Result.KICK_FULL) event.setResult(Result.ALLOWED);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        FileConfiguration c = SG.config;
        LocUtil.teleportToLobby(p);
        SG.clearPlayer(p);


        // SCOREBOARD
        SG.SBU.setScoreboard(p);

        p.setGameMode(GameMode.ADVENTURE);
        Gamer g = Gamer.getGamer(p);
        if (!PointSystem.load(p)) {
            PointSystem.initialize(p);
        }
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
                ChatUtil.sendMessage(p, "" + (SG.minPlayers - Gamer.getRealGamers().size()) + " more tributes needed to start game.");
        }

        ChatUtil.sendMessage(p, "We recommend the use of the SapixCraft resource-pack. To use this pack, use /resourcepack enable");

        List<String> motd = new ArrayList<String>();
        motd.add("&6SurvalGames&7: &aIn Lobby");
        motd.add("&a" + (24 - Gamer.getRealGamers().size()) + " spots left!");
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
        g.remove();
        PointSystem.save(event.getPlayer());
        List<String> motd = new ArrayList<String>();
        motd.add("&6SurvalGames&7: &aIn Lobby");
        motd.add("&a" + (24 - Gamer.getRealGamers().size()) + " spots left!");
        ChatUtil.setMOTD(motd);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        int points = PointSystem.getPoints(e.getPlayer());
        e.setMessage(ChatUtil.getFormat().replace("%points", String.valueOf(points)).replace("%name", e.getPlayer().getName()).replace("%msg", e.getMessage()));
        e.setFormat("");
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
    public void onTarget(EntityTargetEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) event.setCancelled(true);
    }
}