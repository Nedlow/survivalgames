package hwnet.survivalgames.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import hwnet.survivalgames.SettingsManager;

public class Map {

    FileConfiguration data = SettingsManager.getInstance().getData();

    private static Map activeMap = null;
    private static List<Map> voteMaps = new ArrayList<Map>();
    private static List<Map> allMaps = new ArrayList<Map>();

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

    public static List<Map> getAllMaps() {
        return allMaps;
    }

    public static void setTempId(Map map, int id) {
        tempId.put(map, id);
    }

    public static int getTempId(Map map) {
        return tempId.get(map);
    }

    public boolean hasTempId(Map map) {
        return tempId.get(map) != null;
    }

    public static void setActiveMap(Map map) {
        activeMap = map;
    }

    public static void setVoteMaps() {
        for (Map map : getAllMaps()) {
            if (map.hasTempId(map)) {
                voteMaps.add(map);
            }
        }
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

    public Location getSpawn(int id) {
        System.out.println(Map.getActiveMap().getFileName());
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


}
