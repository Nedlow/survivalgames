package hwnet.survivalgames.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import hwnet.survivalgames.SG;
import hwnet.survivalgames.utils.ChatUtil;
import hwnet.survivalgames.utils.GameBoard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class Team {

    private static List<Team> teams = new ArrayList<Team>();
    private static HashMap<String, Team> pTeams = new HashMap<String, Team>();
    private static HashMap<Team, Integer> tempId = new HashMap<Team, Integer>();
    private List<String> playerRequests = new ArrayList<>();

    private String name;
    private static int teamSize;
    private ChatColor color;
    private boolean isAlive = true;

    public Team(String name) {
        this.name = name;
        this.isAlive = true;
        teams.add(this);
    }

    public static void clearInfo() {
        pTeams.clear();
    }


    public String getName() {
        return name;
    }

    public ChatColor getColor() {
        return color;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setIsAlive(boolean alive) {
        isAlive = alive;
    }

    public static List<Team> getAliveTeams() {
        List<Team> alives = new ArrayList<Team>();
        for (Team t : getAllTeams()) {
            if (t.isAlive()) {
                alives.add(t);
            }
        }
        return alives;
    }

    public static void addPlayerToAvailableTeam(Player p) {
        List<Team> available = new ArrayList<Team>();
        for (Team t : getAllTeams()) {
            if (t.getPlayers().size() < teamSize) {
                available.add(t);
            }
        }
        Team.getTeam(available.get(0).getName()).add(p);
    }

    public void add(Player p) {
        //SG.SBU.addToTeam(p, this.getName());
        GameBoard.addToTeam(p, this.getName());
        pTeams.put(p.getName(), this);
    }

    public boolean remove(Player p) {
        if (!hasTeam(p))
            return false;
        pTeams.remove(p.getName());
        //SG.SBU.removeFromTeam(p, this.getName());
        GameBoard.removeFromTeam(p, this.getName());
        return true;
    }

    public static boolean hasTeam(Player p) {
        return pTeams.containsKey(p.getName());
    }

    public static Team getTeam(Player p) {
        if (!hasTeam(p))
            return null;
        return pTeams.get(p.getName());
    }

    public static Team getTeam(String teamname) {
        for (Team t : teams) {
            if (t.getName().equalsIgnoreCase(teamname))
                return t;
        }
        return null;
    }

    public static List<Team> getAllTeams() {
        return teams;
    }

    public static int getTeamSize() {
        return teamSize;
    }

    public static void setTeamSize(int size) {
        teamSize = size;
    }

    public Player getClosestPlayer(Player p) {

        Player first = null;
        double lastDistance = 1000;
        for (Player pl : this.getAlivePlayers()) {
            if (pl == p) continue;
            first = pl;
        }

        for (Player plo : this.getAlivePlayers()) {
            if (p.getLocation().distance(plo.getLocation()) < lastDistance) {
                lastDistance = p.getLocation().distance(plo.getLocation());
                first = plo;
            }
        }
        return first;
    }

    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<Player>();
        for (String s : pTeams.keySet()) {
            if (pTeams.get(s) == this) {
                players.add(Bukkit.getPlayer(s));
            }
        }
        return players;
    }

    public List<Player> getAlivePlayers() {
        List<Player> alive = new ArrayList<Player>();
        for (Gamer g : Gamer.getAliveGamers()) {
            if (getTeam(g.getPlayer()).equals(this)) {
                alive.add(g.getPlayer());
            }
        }
        return alive;
    }

    public void setTempId(int id) {
        tempId.put(this, id);
    }

    public int getTempId() {
        return tempId.get(this);
    }

    public boolean hasTempId() {
        return tempId.get(this) != null;
    }

    public static Team getTeamById(int id) {
        return teams.get(id);
    }

    public void sendJoinRequest(Player p) {
        playerRequests.add(p.getName());
    }

    public void removeJoinRequest(Player p) {
        playerRequests.remove(p.getName());
    }

    public List<String> getJoinRequests() {
        return playerRequests;
    }
}
