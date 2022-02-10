package hwnet.survivalgames.handlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import hwnet.survivalgames.GameState;
import hwnet.survivalgames.SG;

public class Gamer {

    private String name;
    private UUID uuid;
    private boolean alive = true;
    private boolean spectator = false;

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
        boolean started = GameState.isState(GameState.INGAME) || GameState.isState(GameState.ENDGAME);
        for (Gamer g : gamers)
            if (started ? g.isAlive() : !g.isSpectator()) alive.add(g);
        return alive;
    }

    private static Connection connection = SG.connection;

    public void addWin() {
        try {
            PreparedStatement sql = connection.prepareStatement("update ? set wins=wins+1 where uuid=?");
            sql.setString(1, SG.config.getString("mysql.table"));
            sql.setString(2, uuid.toString());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addKill() {
        try {
            PreparedStatement sql = connection.prepareStatement("update ? set kills=kills+1 where uuid=?");
            sql.setString(1, SG.config.getString("mysql.table"));
            sql.setString(2, uuid.toString());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addDeath() {
        try {
            PreparedStatement sql = connection.prepareStatement("update ? set deaths=deaths+1 where uuid=?");
            sql.setString(1, SG.config.getString("mysql.table"));
            sql.setString(2, uuid.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
