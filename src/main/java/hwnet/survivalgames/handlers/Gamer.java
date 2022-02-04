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
    private boolean alive = false;

    private Gamer(Player player) {
        this.name = player.getName();
        this.uuid = player.getUniqueId();
        gamers.add(this);
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

    public void remove() {
        gamers.remove(this);
    }

    private static final List<Gamer> gamers = new ArrayList<Gamer>();

    public static Gamer getGamer(Player p) {
        for (Gamer g : gamers)
            if (g.getName().equalsIgnoreCase(p.getName()))
                return g;
        return new Gamer(p);
    }

    /**
     * @deprecated use getGamer(UUID) instead
     */
    @Deprecated
    public static Gamer getGamer(String name) {
        for (Gamer g : gamers)
            if (g.getName().equalsIgnoreCase(name))
                return g;
        return null;
    }

    public static Gamer getGamer(UUID id) {
        for (Gamer g : gamers)
            if (g.getPlayer().getUniqueId().equals(id))
                return g;
        return null;
    }

    public static List<Gamer> getGamers() {
        return gamers;
    }

    public static List<Gamer> getAliveGamers() {
        List<Gamer> alive = new ArrayList<Gamer>();
        boolean started = GameState.isState(GameState.INGAME);
        for (Gamer g : gamers)
            if (started ? g.isAlive() : g.getPlayer().getGameMode() == GameMode.SURVIVAL)
                alive.add(g);
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
