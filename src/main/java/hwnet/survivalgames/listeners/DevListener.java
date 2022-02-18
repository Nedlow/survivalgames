package hwnet.survivalgames.listeners;

import hwnet.survivalgames.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
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
        if (rain)
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onThunderChange(ThunderChangeEvent event) {

        boolean storm = event.toThunderState();
        if (storm)
            event.setCancelled(true);
    }

    @EventHandler
    public void onCreeperExplode(EntityExplodeEvent e) {
        if (e.getEntity() instanceof Creeper) {
            for (Block b : e.blockList()) {
                e.blockList().remove(this);
            }
        }
    }
}
