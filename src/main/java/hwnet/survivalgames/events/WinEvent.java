package hwnet.survivalgames.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import hwnet.survivalgames.handlers.Gamer;

public class WinEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    private Player player;

    public WinEvent(Gamer gamer) {
        this.player = gamer.getPlayer();
    }

    public Player getPlayer() {
        return player;
    }
}
