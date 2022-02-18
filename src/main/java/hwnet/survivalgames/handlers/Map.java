package hwnet.survivalgames.handlers;

import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import hwnet.survivalgames.SettingsManager;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

public class Map {

    FileConfiguration data = SettingsManager.getInstance().getData();

    private static Map activeMap = null;
    private static List<Map> voteMaps = new ArrayList<Map>();
    private static List<Map> allMaps = new ArrayList<Map>();
    private List<Location> spawns = new ArrayList<>();

    private String fileName, MapName;
    private static HashMap<Map, Integer> tempId = new HashMap<Map, Integer>();

    public Map(String mapName, String fileName) {
        this.fileName = fileName;
        this.MapName = mapName;
        allMaps.add(this);
    }

    public static void addMap(Map map) {
        allMaps.add(map);
    }

    public static Map getMap(String filename) {
        for (Map map : allMaps) {
            if (map.fileName == filename) return map;
        }
        return null;
    }

    public static List<Map> getAllMaps() {
        return allMaps;
    }

    public static void setTempId(Map map, int id) {
        tempId.put(map, id);
    }

    public static void clearInfo() {
        tempId.clear();
        voteMaps.clear();
        setActiveMap(null);
    }

    public static int getTempId(Map map) {
        return tempId.get(map);
    }

    public static void setVoteMap(Map map, int tempid) {
        tempId.put(map, tempid);
        voteMaps.add(map);
    }

    public static boolean hasTempId(Map map) {
        return tempId.get(map) != null;
    }

    public static void setActiveMap(Map map) {
        activeMap = map;
    }

    public static void setVoteMaps() {
        for (Map map : sortedMap().keySet()) {
            voteMaps.add(map);
        }
    }

    private static java.util.Map<Map, Integer> sortedMap() {
        java.util.Map<Map, Integer> result = tempId.entrySet()
                .stream()
                .sorted(java.util.Map.Entry.comparingByValue())
                .collect(Collectors.toMap(
                        java.util.Map.Entry::getKey,
                        java.util.Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        return result;
    }

    public static List<Map> getVoteMaps() {
        return voteMaps;
    }

    public static Map getActiveMap() {
        return activeMap;
    }

    public static Map getMapById(int id) {
        return voteMaps.get(id);
    }

    public String getMapName() {
        return MapName;
    }

    public String getFileName() {
        return fileName;
    }

    public World getWorld() {
        return Bukkit.getWorld(fileName);
    }

    public List<Location> getSpawns() {
        return this.spawns;
    }

    public void initializeSpawns() {
        SettingsManager.getInstance().getData().getList("arenas." + this.getFileName() + ".spawns");
        for (int id = 0; id < 24; id++) {
            World world = Bukkit.getWorld(Map.getActiveMap().getFileName());
            double x = data.getDouble("arenas." + Map.getActiveMap().getFileName() + ".spawns." + id + ".x");
            double y = data.getDouble("arenas." + Map.getActiveMap().getFileName() + ".spawns." + id + ".y");
            double z = data.getDouble("arenas." + Map.getActiveMap().getFileName() + ".spawns." + id + ".z");
            float yaw = (float) data.getDouble("arenas." + Map.getActiveMap().getFileName() + ".spawns." + id + ".yaw");
            float pitch = (float) data.getDouble("arenas." + Map.getActiveMap().getFileName() + ".spawns." + id + ".pitch");
            spawns.add(new Location(world, x, y, z, yaw, pitch));
        }
    }

    public Location getSpawn(int id) {
        World world = Bukkit.getWorld(Map.getActiveMap().getFileName());
        double x = data.getDouble("arenas." + Map.getActiveMap().getFileName() + ".spawns." + id + ".x");
        double y = data.getDouble("arenas." + Map.getActiveMap().getFileName() + ".spawns." + id + ".y");
        double z = data.getDouble("arenas." + Map.getActiveMap().getFileName() + ".spawns." + id + ".z");
        float yaw = (float) data.getDouble("arenas." + Map.getActiveMap().getFileName() + ".spawns." + id + ".yaw");
        float pitch = (float) data.getDouble("arenas." + Map.getActiveMap().getFileName() + ".spawns." + id + ".pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

    public Location getCenterLocation() {
        World world = Bukkit.getWorld(Map.getActiveMap().getFileName());
        double x = data.getDouble("arenas." + Map.getActiveMap().getFileName() + ".center.x");
        double y = data.getDouble("arenas." + Map.getActiveMap().getFileName() + ".center.y");
        double z = data.getDouble("arenas." + Map.getActiveMap().getFileName() + ".center.z");
        return new Location(world, x, y, z);
    }

    public static void chooseMaps() {
        Random rand = new Random();
        if (Map.getAllMaps().size() >= 6) {
            for (int id = 1; id < 7; id++) {
                Map map = Map.getAllMaps().get(rand.nextInt(Map.getAllMaps().size()));
                while (Map.hasTempId(map)) {
                    map = Map.getAllMaps().get(rand.nextInt(Map.getAllMaps().size()));
                }
                Map.setTempId(map, id);
            }
        } else {
            for (int i = 1; i <= Map.getAllMaps().size(); i++) {
                Map map = Map.getAllMaps().get(i);
                Map.setTempId(map, i);
            }
        }
        Map.setVoteMaps();
    }
}
