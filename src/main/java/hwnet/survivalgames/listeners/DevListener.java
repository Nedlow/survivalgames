package hwnet.survivalgames.listeners;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.regions.CylinderRegion;
import com.sk89q.worldedit.world.World;
import hwnet.survivalgames.handlers.PointSystem;
import hwnet.survivalgames.utils.ChatUtil;
import hwnet.survivalgames.utils.LocUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.entity.Squid;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class DevListener implements Listener {

    @EventHandler
    public void onJoin(AsyncPlayerPreLoginEvent e) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(e.getUniqueId());
        if (!p.isOp()) {
            e.setKickMessage(ChatColor.RED + "Server is in developer mode!\n" + ChatColor.DARK_PURPLE + "Please contact admin.");
            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (!PointSystem.load(p)) {
            PointSystem.initialize(p);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        World world = BukkitAdapter.adapt(LocUtil.getLobbyLocation().getWorld());
        BlockVector3 fromLoc = BlockVector3.at(e.getFrom().getX(), e.getFrom().getY(), e.getFrom().getZ());
        BlockVector3 toLoc = BlockVector3.at(e.getTo().getX(), e.getTo().getY(), e.getTo().getZ());
        Vector2 radius = Vector2.at(20, 20);
        BlockVector3 center = BlockVector3.at(LocUtil.getLobbyLocation().getX(), LocUtil.getLobbyLocation().getY(), LocUtil.getLobbyLocation().getZ());
        CylinderRegion region = new CylinderRegion(world, center, radius, 0, 200);
        boolean from = region.contains(fromLoc);
        boolean to = region.contains(toLoc);
        if (from && !to) {
            ChatUtil.sendMessage(e.getPlayer(), "You are now leaving spawn.");
        } else if (!from && to) {
            ChatUtil.sendMessage(e.getPlayer(), "You are now entering spawn.");
        }
    }

    @EventHandler
    public void onMOTD(ServerListPingEvent e) {
        e.setMotd(ChatUtil.getMOTD());
    }

    @EventHandler
    public void onFire(BlockSpreadEvent e) {
        if (e.getSource().getType() == Material.FIRE) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWeatherChange(WeatherChangeEvent event) {

        boolean rain = event.toWeatherState();
        if (rain) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onThunderChange(ThunderChangeEvent event) {

        boolean storm = event.toThunderState();
        if (storm) event.setCancelled(true);
    }

    @EventHandler
    public void onCreeperExplode(EntityExplodeEvent e) {
        if (e.getEntity() instanceof Creeper) {
            for (Block b : e.blockList()) {
                e.blockList().remove(b);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (e.getEntity() instanceof Bat || e.getEntity() instanceof Squid) e.setCancelled(true);
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e) {
        if (e.getEntity() instanceof Bat || e.getEntity() instanceof Squid) e.setCancelled(true);
    }
}
