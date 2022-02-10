package hwnet.survivalgames.listeners;

import hwnet.survivalgames.handlers.Gamer;
import hwnet.survivalgames.utils.ChatUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.server.ServerListPingEvent;

public class StartListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (Gamer.getGamer(e.getPlayer()).isAlive() && !Gamer.getGamer(e.getPlayer()).isSpectator()) {
            if (e.getTo().getX() == e.getFrom().getX() && e.getTo().getZ() == e.getFrom().getZ()) return;
            e.setCancelled(true);
            e.getPlayer().teleport(e.getFrom());
        }
    }

    @EventHandler
    public void onMOTD(ServerListPingEvent e) {
        e.setMotd(ChatUtil.getMOTD());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        event.setCancelled(true);
    }
}
