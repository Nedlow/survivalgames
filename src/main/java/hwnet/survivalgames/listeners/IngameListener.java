package hwnet.survivalgames.listeners;

import hwnet.survivalgames.handlers.Team;
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
import org.bukkit.event.entity.PlayerDeathEvent;

import hwnet.survivalgames.SG;
import hwnet.survivalgames.events.GamerDeathEvent;
import hwnet.survivalgames.events.GamerKillEvent;
import hwnet.survivalgames.handlers.Gamer;
import hwnet.survivalgames.handlers.PointSystem;
import hwnet.survivalgames.utils.ChatUtil;

public class IngameListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onDeath2(EntityDamageByEntityEvent e) {

        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (p.getHealth() - e.getFinalDamage() < 1) {
                p.setHealth(20);
                p.setGameMode(GameMode.SPECTATOR);
                Gamer g = Gamer.getGamer(p);
                g.setAlive(false);

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

                for (Player pl : Bukkit.getOnlinePlayers()) {
                    pl.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 20, 1);
                    pl.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 20, 1);
                }
                ChatUtil.broadcast("A tribute has fallen. " + Gamer.getAliveGamers().size() + "/"
                        + Gamer.getGamers().size() + " tributes remain");

                Team t = Team.getTeam(p);

                if (t.getAlivePlayers().size() < 1) {
                    t.setIsAlive(false);
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
        }

        /*


        if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
            Player p = (Player) e.getEntity();
            Player d = (Player) e.getDamager();

            if (Team.getTeam(p).equals(Team.getTeam(d))) e.setCancelled(true);


            if (p.getHealth() - e.getFinalDamage() < 1) {
                p.setHealth(20);
                p.setGameMode(GameMode.SPECTATOR);
                Gamer g = Gamer.getGamer(p);
                g.setAlive(false);


                //ChatUtil.broadcast(p.getName() + " was killed by " + d.getName());


                for (Player pl : Bukkit.getOnlinePlayers()) {
                    pl.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 20, 1);
                    pl.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 20, 1);
                }
                ChatUtil.broadcast("A tribute has fallen. " + Gamer.getAliveGamers().size() + "/"
                        + Gamer.getGamers().size() + " tributes remain");

                Team t = Team.getTeam(p);

                if (Team.getAliveTeams().size() == 1) {
                    SG.win(Team.getAliveTeams().get(0));
                } else {
                    if (t.getAlivePlayers().size() < 1) {
                        t.setIsAlive(false);
                        for (Player po : t.getPlayers()) {
                            po.setSpectatorTarget(null);
                        }
                        ChatUtil.broadcast("District " + t.getName() + " has been eliminated! Only " + Team.getAliveTeams().size() + " left.");
                    }
                }
                if (Team.getTeam(p).isAlive()) {
                    p.setSpectatorTarget(Team.getTeam(p).getAlivePlayers().get(0));
                }
                PointSystem.addPoints(p, SG.config.getInt("points.lose"));
                PointSystem.addPoints(d, SG.config.getInt("points.kill"));
                Bukkit.getPluginManager().callEvent(new GamerKillEvent(p, d));
                if (SG.config.getBoolean("mysql.enabled"))
                    Gamer.getGamer(d).addKill();
            }
        } */
    }


    /* Death event not working, implementing more checks in EntityDamageEntityEvent
    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        p.setBedSpawnLocation(p.getLocation());
        p.setGameMode(GameMode.SPECTATOR);
        Gamer g = Gamer.getGamer(p);
        g.setAlive(false);
        p.setHealth(20);

        for (Player pl : Bukkit.getOnlinePlayers()) {
            pl.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 20, 1);
            pl.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 20, 1);
        }
        ChatUtil.broadcast("A tribute has fallen. " + Gamer.getAliveGamers().size() + "/" + Gamer.getGamers().size()
                + " tributes remain");
        Team t = Team.getTeam(p);
        if (t.getAlivePlayers().size() < 1) {
            t.setIsAlive(false);
            ChatUtil.broadcast("District " + t.getName() + " has been eliminated! Only " + Team.getAliveTeams().size() + " left.");
        }

        if (Team.getAliveTeams().size() == 1) {
            SG.win(Team.getAliveTeams().get(0));
        }

        if (Team.getTeam(p).isAlive()) {
            p.setSpectatorTarget(Team.getTeam(p).getAlivePlayers().get(0));
        }

        PointSystem.addPoints(p, 50);
        Bukkit.getPluginManager().callEvent(new GamerDeathEvent(p));
        if (SG.config.getBoolean("mysql.enabled")) {
            if (SG.playerDataContains(p.getUniqueId().toString())) {
                g.addDeath();
            }
        }
        PointSystem.save(p);
    }
*/

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if
        (event.getPlayer().getGameMode() != GameMode.SURVIVAL) return;
        if
        (event.getBlock().getType() == Material.LEGACY_LEAVES)
            event.setCancelled(true);
        if (event.getBlock().getType() == Material.GLASS)
            event.setCancelled(true);
        if (event.getBlock().getType() == Material.LEGACY_THIN_GLASS)
            event.setCancelled(true);
    }
}