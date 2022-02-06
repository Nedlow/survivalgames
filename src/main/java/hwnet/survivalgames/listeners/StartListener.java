package hwnet.survivalgames.listeners;

import hwnet.survivalgames.handlers.Gamer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.server.ServerListPingEvent;

public class StartListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (Gamer.getAliveGamers().contains(e.getPlayer())) {
            if (e.getTo().getX() == e.getFrom().getX() && e.getTo().getZ() == e.getFrom().getZ()) return;
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onMOTD(ServerListPingEvent e) {
        e.setMotd("SurvivalGames\nStarting");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        event.setCancelled(true);
    }
}
