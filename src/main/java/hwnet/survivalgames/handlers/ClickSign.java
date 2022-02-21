package hwnet.survivalgames.handlers;

import hwnet.survivalgames.SettingsManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ClickSign {

    private static List<ClickSign> signs = new ArrayList<>();
    private SignType type;
    private Location location;

    private static enum SignType {
        TOP_KILLS, TOP_WINS, TOP_GAMES, TOP_DEATHS, TOP_POINTS;
    }

    public static SignType getType(String type) {
        switch (type.toLowerCase()) {
            case "wins":
            case "TOP_WINS":
                return SignType.TOP_WINS;
            case "deaths":
            case "TOP_DEATHS":
                return SignType.TOP_DEATHS;
            case "points":
            case "TOP_POINTS":
                return SignType.TOP_POINTS;
            case "games":
            case "TOP_GAMES":
                return SignType.TOP_GAMES;
            case "TOP_KILLS":
            default:
                return SignType.TOP_KILLS;
        }
    }


    public ClickSign(SignType type, Location location) {
        this.location = location;
        this.type = type;
        signs.add(this);
    }

    public SignType getType() {
        return this.type;
    }

    public Location getLocation() {
        return this.location;
    }

    public void remove() {
        signs.remove(this);
    }

    public static ClickSign getSign(Location loc) {
        for (ClickSign sign : signs) {
            if (sign.getLocation() == loc) return sign;
        }
        return null;
    }

    public static List<ClickSign> getAllSigns() {
        return signs;
    }

    public static List<ClickSign> getAllSigns(SignType type) {
        List<ClickSign> result = new ArrayList<>();
        for (ClickSign sign : signs) {
            if (sign.getType() == type) result.add(sign);
        }
        return result;
    }

    public void setSignText() {
        Block b = location.getBlock();
        Sign sign = (Sign) b.getState();
        if (getType() == SignType.TOP_WINS) {
            Player p = (Player) Bukkit.getOfflinePlayer(PointSystem.topWin);
            sign.setLine(0, ChatColor.GOLD + "" + ChatColor.BOLD + "TOP WINS");
            sign.setLine(1, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + p.getName());
            sign.setLine(2, ChatColor.GREEN + "" + ChatColor.BOLD + String.valueOf(PointSystem.getWins(PointSystem.topWin)));
        } else if (getType() == SignType.TOP_KILLS) {
            Player p = (Player) Bukkit.getOfflinePlayer(PointSystem.topKill);
            sign.setLine(0, ChatColor.GOLD + "TOP 1 KILLS");
            sign.setLine(1, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + p.getName());
            sign.setLine(2, ChatColor.GREEN + "" + ChatColor.BOLD + String.valueOf(PointSystem.getWins(PointSystem.topKill)));
        } else if (getType() == SignType.TOP_GAMES) {
            Player p = (Player) Bukkit.getOfflinePlayer(PointSystem.topGames);
            sign.setLine(0, ChatColor.GOLD + "TOP 1 KILLS");
            sign.setLine(1, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + p.getName());
            sign.setLine(2, ChatColor.GREEN + "" + ChatColor.BOLD + String.valueOf(PointSystem.getWins(PointSystem.topGames)));
        }
        sign.update(true);
    }

    public static void updateSigns() {
        for (ClickSign sign : signs) {
            sign.setSignText();
        }
    }

    public static void importSigns() {
        FileConfiguration data = SettingsManager.getInstance().getData();
        if (data.getConfigurationSection("signs") != null) {
            for (String uuid : data.getConfigurationSection("signs").getKeys(false)) {
                SignType type = getType(data.getString("signs." + uuid + ".type"));
                World w = Bukkit.getWorld(data.getString("signs." + uuid + ".location.world"));
                double x = data.getDouble("signs." + uuid + ".location.x");
                double y = data.getDouble("signs." + uuid + ".location.y");
                double z = data.getDouble("signs." + uuid + ".location.z");
                Location loc = new Location(w, x, y, z);
                new ClickSign(type, loc);
            }
        }
    }
}
