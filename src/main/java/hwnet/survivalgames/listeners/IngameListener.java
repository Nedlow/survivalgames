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
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

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
        e.setJoinMessage(null);
        g.setAlive(false);
        g.setSpectator(true);
        p.setGameMode(GameMode.SPECTATOR);
        p.teleport(Map.getActiveMap().getCenterLocation());
        ChatUtil.sendMessage(p, "Joined as spectator.");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (Gamer.getGamer(e.getPlayer().getUniqueId()) != null) {
            if (Gamer.getGamer(e.getPlayer()).isAlive()) {
                e.setQuitMessage(null);
                ChatUtil.broadcast("A tribute has fallen. " + Gamer.getAliveGamers().size() + "/"
                        + Gamer.getGamers().size() + " tributes remain");
            }
            Gamer.getGamer(e.getPlayer()).remove();

            SG.specGUI.getYourInventory().clear();
            for (int iD = 0; iD < Gamer.getAliveGamers().size(); iD++) {
                ItemStack playerhead = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta playerheadmeta = (SkullMeta) playerhead.getItemMeta();
                playerheadmeta.setOwner(Gamer.getAliveGamers().get(iD).getName());
                playerheadmeta.setDisplayName(Gamer.getAliveGamers().get(iD).getName());
                playerhead.setItemMeta(playerheadmeta);
                int finalI = iD;
                SG.specGUI.setItem(iD, playerhead, player -> {
                    player.teleport(Gamer.getAliveGamers().get(finalI).getPlayer());
                });
            }
        }
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
                if (SG.districts_mode)
                    if (Team.getTeam(p).equals(Team.getTeam(d))) e.setCancelled(true);
            }
            if (p.getHealth() - e.getFinalDamage() < 1) {
                handleDeath(p);
                if (e.getDamager() instanceof Player) {
                    Player d = (Player) e.getDamager();

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

        if (SG.districts_mode) {
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
                    SG.win(Team.getAliveTeams().get(0), null);
                } else {
                    ChatUtil.broadcast("District " + t.getName() + " has been eliminated! Only " + Team.getAliveTeams().size() + " left.");
                }
            }
            if (Team.getTeam(p).isAlive()) {
                p.setSpectatorTarget(Team.getTeam(p).getAlivePlayers().get(0));
            }
        } else {
            if (Gamer.getAliveGamers().size() < 2) {
                SG.win(null, Gamer.getAliveGamers().get(0).getPlayer());
            }
        }
        SG.specGUI.getYourInventory().clear();
        for (int iD = 0; iD < Gamer.getAliveGamers().size(); iD++) {
            ItemStack playerhead = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta playerheadmeta = (SkullMeta) playerhead.getItemMeta();
            playerheadmeta.setOwner(Gamer.getAliveGamers().get(iD).getName());
            playerheadmeta.setDisplayName(Gamer.getAliveGamers().get(iD).getName());
            playerhead.setItemMeta(playerheadmeta);
            int finalI = iD;
            SG.specGUI.setItem(iD, playerhead, player -> {
                player.teleport(Gamer.getAliveGamers().get(finalI).getPlayer());
            });
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.GLASS)
            event.setCancelled(true);

    }
}