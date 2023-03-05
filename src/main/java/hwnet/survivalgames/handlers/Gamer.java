package hwnet.survivalgames.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import hwnet.survivalgames.utils.GameBoard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import hwnet.survivalgames.GameState;
import hwnet.survivalgames.SG;

public class Gamer {

    private String name;
    private UUID uuid;
    private boolean alive = true;
    private boolean spectator = false;
    private int kills = 0;
    private int timeAlive = 0;
    private boolean resourcepack;

    private static List<Gamer> gamers = new ArrayList<Gamer>();

    private Gamer(Player player) {
        this.name = player.getName();
        this.uuid = player.getUniqueId();
        gamers.add(this);
    }

    public static List<Gamer> getRealGamers() {
        List<Gamer> result = new ArrayList<>();
        for (Gamer g : getGamers()) {
            if (!g.isSpectator()) result.add(g);
        }
        return result;
    }

    public static void clearRealGamers() {
        for (Gamer g : getRealGamers()) {
            g.remove();
        }
    }

    public int getKills() {
        return kills;
    }

    public int getTimeAlive() {
        return timeAlive;
    }

    public void addKill() {
        kills += 1;
        GameBoard.getBoard(Bukkit.getPlayer(uuid)).update(GameBoard.ScoreType.KILLS, "Kills", "&bKills: &a" + kills);
    }

    public void setTimeAlive() {
        if (GameState.getState() == GameState.ENDGAME) {
            this.timeAlive = new Integer(SG.gametime + SG.dm);
        } else {
            this.timeAlive = new Integer(SG.gametime);
        }

    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public String getName() {
        return name;
    }

    public UUID getUUID() {
        return uuid;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isSpectator() {
        return spectator;
    }

    public void setSpectator(boolean spectate) {
        this.spectator = spectate;
    }

    public void remove() {
        gamers.remove(this);
    }

    public static Gamer getGamer(Player p) {
        for (Gamer g : gamers)
            if (g.getName().equalsIgnoreCase(p.getName())) return g;
        return new Gamer(p);
    }

    public boolean hasResourcePackEnabled() {
        return resourcepack;
    }

    public void setResourcepack(boolean bool) {
        resourcepack = bool;
    }

    /**
     * @deprecated use getGamer(UUID) instead
     */
    @Deprecated
    public static Gamer getGamer(String name) {
        for (Gamer g : gamers)
            if (g.getName().equalsIgnoreCase(name)) return g;
        return null;
    }

    public static Gamer getGamer(UUID id) {
        for (Gamer g : gamers)
            if (g.getPlayer().getUniqueId().equals(id)) return g;
        return null;
    }

    public static List<Gamer> getGamers() {
        return gamers;
    }

    public static List<Gamer> getAliveGamers() {
        List<Gamer> alive = new ArrayList<Gamer>();
        for (Gamer g : gamers)
            if (g.isAlive() && !g.isSpectator()) alive.add(g);
        return alive;
    }

}
