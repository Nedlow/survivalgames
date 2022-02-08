package hwnet.survivalgames.listeners;

import hwnet.survivalgames.handlers.Map;
import hwnet.survivalgames.handlers.Team;
import hwnet.survivalgames.utils.LocUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import hwnet.survivalgames.SG;
import hwnet.survivalgames.events.GamerDeathEvent;
import hwnet.survivalgames.events.GamerKillEvent;
import hwnet.survivalgames.handlers.Gamer;
import hwnet.survivalgames.handlers.PointSystem;
import hwnet.survivalgames.utils.ChatUtil;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.util.ArrayList;
import java.util.List;

public class IngameListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onMOTD(ServerListPingEvent e) {
        e.setMotd(ChatUtil.getMOTD());
    }

    @EventHandler
    public void onJoinPre(PlayerLoginEvent event) {
        if (event.getResult() == PlayerLoginEvent.Result.KICK_FULL)
            event.setResult(PlayerLoginEvent.Result.ALLOWED);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        Gamer g = Gamer.getGamer(p);
        g.setAlive(false);
        p.setGameMode(GameMode.SPECTATOR);
        p.teleport(Map.getActiveMap().getCenterLocation());
    }


    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        e.setCancelled(true);
        Team t = Team.getTeam(e.getPlayer());
        int points = PointSystem.getPoints(e.getPlayer());
        String format = ChatUtil.getFormat().replace("%points", String.valueOf(points)).replace("%name", e.getPlayer().getName()).
                replace("%msg", e.getMessage());
        for (Player p : t.getPlayers()) {
            p.sendMessage(format);
            ChatUtil.sendMessage(SG.cmd, format);
        }
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent e) {
        e.setCancelled(true);
    }


    @EventHandler
    public void onDeath2(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();

            if (e.getDamager() instanceof Player) {
                Player d = (Player) e.getDamager();
                if (Team.getTeam(p).equals(Team.getTeam(d))) e.setCancelled(true);
            }
            if (p.getHealth() - e.getFinalDamage() < 1) {
                handleDeath(p);
                if (e.getDamager() instanceof Player) {
                    Player d = (Player) e.getDamager();
                    if (Team.getTeam(p).equals(Team.getTeam(d))) e.setCancelled(true);

                    // Point System
                    PointSystem.addPoints(p, SG.config.getInt("points.lose"));
                    PointSystem.addPoints(d, SG.config.getInt("points.kill"));

                    Bukkit.getPluginManager().callEvent(new GamerKillEvent(p, d));
                    if (SG.config.getBoolean("mysql.enabled"))
                        Gamer.getGamer(d).addKill();
                }
            }
        }
    }

    @EventHandler
    public void onNaturalDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (p.getHealth() - e.getFinalDamage() < 1) {
                e.setCancelled(true);
                handleDeath(p);
            }
        }
    }

    private void handleDeath(Player p) {
        if (!Gamer.getGamer(p.getUniqueId()).isAlive()) return;

        p.setHealth(20);
        p.setGameMode(GameMode.SPECTATOR);
        Gamer g = Gamer.getGamer(p);
        g.setAlive(false);

        for (Player pl : Bukkit.getOnlinePlayers()) {
            pl.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 20, 1);
            pl.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 20, 1);
        }
        ChatUtil.broadcast("A tribute has fallen. " + Gamer.getAliveGamers().size() + "/"
                + Gamer.getGamers().size() + " tributes remain");

        Team t = Team.getTeam(p);

        if (t.getAlivePlayers().size() < 1) {
            t.setIsAlive(false);
            List<String> motd = new ArrayList<String>();
            motd.add("&6Surval Games&7: &cIn Game");
            motd.add("&b" + Gamer.getAliveGamers().size() + "&7/&b24 tributes left!");
            ChatUtil.setMOTD(motd);
            for (Player po : t.getPlayers()) {
                po.setSpectatorTarget(null);
            }
            if (Team.getAliveTeams().size() == 1) {
                SG.win(Team.getAliveTeams().get(0));
            } else {
                ChatUtil.broadcast("District " + t.getName() + " has been eliminated! Only " + Team.getAliveTeams().size() + " left.");
            }
        }
        if (Team.getTeam(p).isAlive()) {
            p.setSpectatorTarget(Team.getTeam(p).getAlivePlayers().get(0));
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.GLASS)
            event.setCancelled(true);

    }

}