package hwnet.survivalgames.listeners;

import hwnet.survivalgames.utils.LocUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import hwnet.survivalgames.SG;
import hwnet.survivalgames.handlers.Gamer;
import hwnet.survivalgames.handlers.Map;
import hwnet.survivalgames.handlers.PointSystem;
import hwnet.survivalgames.handlers.VoteHandler;
import hwnet.survivalgames.utils.ChatUtil;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoinPre(PlayerLoginEvent event) {
        if (event.getResult() == Result.KICK_FULL)
            event.setResult(Result.ALLOWED);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Gamer g = Gamer.getGamer(event.getPlayer());
        g.remove();
        PointSystem.save(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        FileConfiguration c = SG.config;
        World w = Bukkit.getWorld(c.getString("lobby.world"));
        /*
        double x = c.getDouble("lobby.x");
        double y = c.getDouble("lobby.y");
        double z = c.getDouble("lobby.z");
        float pitch = (float) c.getInt("lobby.pitch");
        float yaw = (float) c.getInt("lobby.yaw");
        Location loc = new Location(w, x, y, z, yaw, pitch);
        p.teleport(loc);
        */
        LocUtil.teleportToLobby(p);
        SG.clearPlayer(p);

        // SCOREBOARD
        SG.SBU.setScoreboard(p);

        p.setGameMode(GameMode.SURVIVAL);
        if (!PointSystem.load(p)) {
            System.out.println("Set points for player");
            PointSystem.setPoints(p, 0);
        }
        if (p.hasPermission("sg.admin")) {
            p.sendMessage("Joined as admin. Type /join to join the game");
        } else {
            ChatUtil.broadcast(ChatColor.translateAlternateColorCodes('&',
                    "&6==== &bSurvivalGames: &eVoting &6===="));
            ChatUtil.sendMessage(p, "Vote: [/vote <id>]");
            for (Map map : Map.getVoteMaps()) {
                ChatUtil.sendMessage(p, Map.getTempId(map) + " > " + map.getMapName() + " ["
                        + VoteHandler.getVotesMap(map) + " votes]");
            }
            ChatUtil.broadcast(ChatColor.translateAlternateColorCodes('&',
                    "&6=================================="));
            Gamer.getGamer(p);
            p.sendMessage(ChatColor.AQUA + "" + Gamer.getGamers().size() + "/24" + ChatColor.GREEN
                    + " tributes waiting to play.");
        }
        if (SG.config.getBoolean("settings.bungeecord")) {
            ItemStack s = new ItemStack(Material.NETHER_STAR, 1);
            ItemMeta m = s.getItemMeta();
            m.setDisplayName(ChatColor.RED + "Leave");
            s.setItemMeta(m);
            p.getInventory().setItem(8, s);
        }
    }

    @EventHandler
    public void onLeaveClick(PlayerInteractEvent e) {
        if (SG.config.getBoolean("settings.bungeecord")) {
            if (e.getItem() == null)
                return;
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
        e.setMotd(ChatColor.translateAlternateColorCodes('&', SG.config.getString("settings.motd.lobby").replace("%newline%", "\n")));
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.SURVIVAL)
            event.setCancelled(true);
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.SURVIVAL)
            event.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.SURVIVAL)
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.SURVIVAL)
            event.setCancelled(true);
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
        if (event.getPlayer().getGameMode() == GameMode.SURVIVAL)
            event.setCancelled(true);
    }
}