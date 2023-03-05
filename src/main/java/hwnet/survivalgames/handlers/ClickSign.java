package hwnet.survivalgames.handlers;

import hwnet.survivalgames.SG;
import hwnet.survivalgames.SettingsManager;
import hwnet.survivalgames.utils.ChatUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClickSign {

    private static List<ClickSign> signs = new ArrayList<>();
    private SignType type;
    private Location location;
    private UUID uuid;
    public int voteID = 0;

    public static enum SignType {
        TOP_KILLS, TOP_WINS, TOP_GAMES, TOP_DEATHS, TOP_POINTS, VOTE, VOTE_INACTIVE;
    }

    public static SignType getType(String type) {
        switch (type.toLowerCase()) {
            case "wins":
            case "top_wins":
                return SignType.TOP_WINS;
            case "deaths":
            case "top_deaths":
                return SignType.TOP_DEATHS;
            case "points":
            case "top_points":
                return SignType.TOP_POINTS;
            case "games":
            case "top_games":
                return SignType.TOP_GAMES;
            case "kills":
            case "top_kills":
                return SignType.TOP_KILLS;
            case "vote":
            case "votes":
                return SignType.VOTE;
        }
        return null;
    }


    public ClickSign(UUID uuid, SignType type, Location location) {
        this.location = location;
        this.type = type;
        signs.add(this);
    }

    public SignType getType() {
        return this.type;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public Location getLocation() {
        return this.location;
    }

    public int getVoteID() {
        return this.voteID;
    }

    public void setVoteID(int id) {
        this.voteID = id;
    }

    public void setSignType(SignType type) {
        this.type = type;
    }

    public void remove() {
        SettingsManager.getInstance().getData().set("signs." + getUUID(), null);
        SettingsManager.getInstance().saveData();
        signs.remove(this);

    }

    public static ClickSign getSign(Location loc) {
        for (ClickSign sign : signs) {
            if (sign.getLocation().equals(loc)) return sign;
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
        OfflinePlayer p = null;
        switch (type) {
            case TOP_WINS:
                p = Bukkit.getOfflinePlayer(PointSystem.topWin);
                sign.setLine(0, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "▉▉▉▉▉▉▉▉▉▉▉");
                sign.setLine(1, ChatColor.YELLOW + "" + ChatColor.BOLD + p.getName());
                sign.setLine(2, ChatColor.GOLD + "" + ChatColor.BOLD + String.valueOf(PointSystem.getWins(PointSystem.topWin)) + " Wins");
                sign.setLine(3, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "▉▉▉▉▉▉▉▉▉▉▉");
                break;
            case TOP_KILLS:
                p = Bukkit.getOfflinePlayer(PointSystem.topKill);
                sign.setLine(0, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "▉▉▉▉▉▉▉▉▉▉▉");
                sign.setLine(1, ChatColor.YELLOW + "" + ChatColor.BOLD + p.getName());
                sign.setLine(2, ChatColor.GOLD + "" + ChatColor.BOLD + String.valueOf(PointSystem.getKills(PointSystem.topKill)) + " Kills");
                sign.setLine(3, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "▉▉▉▉▉▉▉▉▉▉▉");
                break;
            case TOP_GAMES:
                p = Bukkit.getOfflinePlayer(PointSystem.topGames);
                sign.setLine(0, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "▉▉▉▉▉▉▉▉▉▉▉");
                sign.setLine(1, ChatColor.YELLOW + "" + ChatColor.BOLD + p.getName());
                sign.setLine(2, ChatColor.GOLD + "" + ChatColor.BOLD + String.valueOf(PointSystem.getGames(PointSystem.topGames)) + " Games");
                sign.setLine(3, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "▉▉▉▉▉▉▉▉▉▉▉");
                break;
            case TOP_DEATHS:
                p = Bukkit.getOfflinePlayer(PointSystem.topDeath);
                sign.setLine(0, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "▉▉▉▉▉▉▉▉▉▉▉");
                sign.setLine(1, ChatColor.YELLOW + "" + ChatColor.BOLD + p.getName());
                sign.setLine(2, ChatColor.GOLD + "" + ChatColor.BOLD + String.valueOf(PointSystem.getDeaths(PointSystem.topDeath)) + " Deaths");
                sign.setLine(3, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "▉▉▉▉▉▉▉▉▉▉▉");
                break;
            case VOTE:
                Map map = Map.getVoteMaps().get(getVoteID());
                sign.setLine(0, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "▉▉▉▉▉▉▉▉▉▉▉");
                sign.setLine(1, ChatColor.YELLOW + "Vote for");
                sign.setLine(2, ChatColor.GOLD + map.getMapName());
                sign.setLine(3, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "▉▉▉▉▉▉▉▉▉▉▉");
                break;
            case VOTE_INACTIVE:
                sign.setLine(0, ChatColor.DARK_RED + "" + ChatColor.BOLD + "▉▉▉▉▉▉▉▉▉▉▉");
                sign.setLine(1, ChatColor.GRAY + "Not");
                sign.setLine(2, ChatColor.GRAY + "Active");
                sign.setLine(3, ChatColor.DARK_RED + "" + ChatColor.BOLD + "▉▉▉▉▉▉▉▉▉▉▉");
                break;

        }
        sign.update(true);
    }

    public static void updateSigns() {
        int voteint = 0;
        for (ClickSign sign : signs) {
            if (voteint > Map.getVoteMaps().size() - 1) {
                sign.setSignType(SignType.VOTE_INACTIVE);
            } else {
                if (sign.getType() == SignType.VOTE) {
                    voteint++;
                }
            }
            sign.setSignText();
        }
    }

    public static boolean signExists(Location loc) {
        for (ClickSign sign : signs) {
            if (sign.getLocation().equals(loc)) return true;
        }
        return false;
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
                if (!(loc.getBlock().getState() instanceof org.bukkit.block.Sign) || signExists(loc)) {
                    data.set("signs." + uuid, null);
                    SettingsManager.getInstance().saveData();
                    ChatUtil.sendMessage(SG.cmd, "Deleted unused sign");
                } else {
                    //if (data.getInt("signs." + uuid + ".voteid") > Map.getVoteMaps().size() - 1) return;
                    ClickSign s = new ClickSign(UUID.fromString(uuid), type, loc);
                    ChatUtil.sendMessage(SG.cmd, "Added sign with type " + s.getType().toString());
                    if (type == SignType.VOTE) {
                        s.setVoteID(data.getInt("signs." + uuid + ".voteid"));
                    }
                }
            }
        } else {
            ChatUtil.sendMessage(SG.cmd, "No signs created.");
        }
    }
}
