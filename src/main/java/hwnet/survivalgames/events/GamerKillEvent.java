package hwnet.survivalgames.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GamerKillEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private Player p, d;

    public GamerKillEvent(Player death, Player attacker) {
        this.p = death;
        this.d = attacker;
    }

    public Player getPlayer() {
        return p;
    }

    public Player getKiller() {
        return d;
    }

}
