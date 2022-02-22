package hwnet.survivalgames.handlers;

import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

import hwnet.survivalgames.utils.ChatUtil;
import org.bukkit.entity.Player;

import hwnet.survivalgames.SG;
import hwnet.survivalgames.SettingsManager;

public class PointSystem {

    private static HashMap<UUID, Integer> wins = new HashMap<UUID, Integer>();
    private static HashMap<UUID, Integer> games = new HashMap<UUID, Integer>();
    private static HashMap<UUID, Integer> kills = new HashMap<UUID, Integer>();
    private static HashMap<UUID, Integer> deaths = new HashMap<UUID, Integer>();
    private static HashMap<UUID, Integer> points = new HashMap<UUID, Integer>();

    public static UUID topWin, topGames, topKill, topDeath;

    public static int getPoints(UUID uuid) {
        return points.get(uuid);
    }

    public static int getWins(UUID uuid) {
        return wins.get(uuid);
    }

    public static int getGames(UUID uuid) {
        return games.get(uuid);
    }

    public static int getKills(UUID uuid) {
        return kills.get(uuid);
    }

    public static int getDeaths(UUID uuid) {
        return deaths.get(uuid);
    }


    public static boolean hasPoints(UUID uuid) {
        return points.containsKey(uuid);
    }

    public static void addPoints(Player p, int amount) {
        int old = 0;
        if (points.get(p.getUniqueId()) == null) {
            old = 0;
        } else {
            old = points.get(p.getUniqueId());
        }
        points.put(p.getUniqueId(), old + amount);
    }

    public static void addWin(Player p) {
        int old = 0;
        if (wins.get(p.getUniqueId()) == null) {
            old = 0;
        } else {
            old = wins.get(p.getUniqueId());
        }
        wins.put(p.getUniqueId(), old + 1);
    }

    public static void addKill(Player p) {
        int old = 0;
        if (kills.get(p.getUniqueId()) == null) {
            old = 0;
        } else {
            old = kills.get(p.getUniqueId());
        }
        kills.put(p.getUniqueId(), old + 1);
    }

    public static void addGame(Player p) {
        int old = 0;
        if (games.get(p.getUniqueId()) == null) {
            old = 0;
        } else {
            old = games.get(p.getUniqueId());
        }
        games.put(p.getUniqueId(), old + 1);
    }

    public static void addDeath(Player p) {
        int old = 0;
        if (deaths.get(p.getUniqueId()) == null) {
            old = 0;
        } else {
            old = deaths.get(p.getUniqueId());
        }
        deaths.put(p.getUniqueId(), old + 1);
    }

    public static boolean removePoints(Player p, int amount) {
        int old = points.get(p.getUniqueId());
        if (old - amount >= 0)
            return false;
        points.put(p.getUniqueId(), old - amount);
        return true;
    }

    public static void setPoints(Player p, int amount) {
        points.put(p.getUniqueId(), amount);
    }

    public synchronized static void save(Player p) {
        String sql = "update playerdata set wins=?,games=?,kills=?,deaths=?,points=? where uuid='" + p.getUniqueId().toString() + "'";
        try {
            PreparedStatement pstmt = SG.connection.prepareStatement(sql);
            pstmt.setInt(1, wins.get(p.getUniqueId()));
            pstmt.setInt(2, games.get(p.getUniqueId()));
            pstmt.setInt(3, kills.get(p.getUniqueId()));
            pstmt.setInt(4, deaths.get(p.getUniqueId()));
            pstmt.setInt(5, points.get(p.getUniqueId()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized static boolean load(Player p) {
        boolean status = false;
        String sql = "Select * from playerdata where uuid='" + p.getUniqueId() + "'";
        try {
            Statement stmt = SG.connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                ChatUtil.sendMessage(SG.cmd, "Loaded UUID " + rs.getString("uuid"));
                wins.put(UUID.fromString(rs.getString("uuid")), rs.getInt("wins"));
                games.put(UUID.fromString(rs.getString("uuid")), rs.getInt("games"));
                kills.put(UUID.fromString(rs.getString("uuid")), rs.getInt("kills"));
                deaths.put(UUID.fromString(rs.getString("uuid")), rs.getInt("deaths"));
                points.put(UUID.fromString(rs.getString("uuid")), rs.getInt("points"));
                status = true;
            } else {
                ChatUtil.sendMessage(p, "Could not load UUID");
                status = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            status = false;
        }
        return status;
    }

    public synchronized static void initialize(Player p) {
        String sql = "insert into playerdata(uuid,games,wins,kills,deaths,points) values(?,0,0,0,0,0)";
        try {
            PreparedStatement pstmt = SG.connection.prepareStatement(sql);
            pstmt.setString(1, p.getUniqueId().toString());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        wins.put(p.getUniqueId(), 0);
        games.put(p.getUniqueId(), 0);
        kills.put(p.getUniqueId(), 0);
        deaths.put(p.getUniqueId(), 0);
        points.put(p.getUniqueId(), 0);
        ChatUtil.sendMessage(SG.cmd, "Initialized UUID " + p.getUniqueId());
    }


    public static void sendToDatabase() {
        for (UUID uuid : wins.keySet()) {

        }
    }

    public static void initializeTopStats() {
        FetchTopWinFromDB();
        FetchTopKillFromDB();
        FetchTopDeathsFromDB();
        FetchTopGamesFromDB();
    }

    public synchronized static void FetchTopWinFromDB() {
        String sql = "Select uuid, wins from playerdata order by wins desc limit 1";
        try {
            Statement stmt = SG.connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            topWin = UUID.fromString(rs.getString("uuid"));
            wins.put(topWin, rs.getInt("wins"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized static void FetchTopKillFromDB() {
        String sql = "Select uuid, kills from playerdata order by kills desc limit 1";
        try {
            Statement stmt = SG.connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            kills.put(UUID.fromString(rs.getString("uuid")), rs.getInt("kills"));
            topKill = UUID.fromString(rs.getString("uuid"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized static void FetchTopDeathsFromDB() {
        String sql = "Select uuid, deaths from playerdata order by deaths desc limit 1";
        try {
            Statement stmt = SG.connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            deaths.put(UUID.fromString(rs.getString("uuid")), rs.getInt("deaths"));
            topDeath = UUID.fromString(rs.getString("uuid"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized static void FetchTopGamesFromDB() {
        String sql = "Select uuid, games from playerdata order by games desc limit 1";
        try {
            Statement stmt = SG.connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            games.put(UUID.fromString(rs.getString("uuid")), rs.getInt("games"));
            topGames = UUID.fromString(rs.getString("uuid"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
