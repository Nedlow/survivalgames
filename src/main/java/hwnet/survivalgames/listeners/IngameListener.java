package hwnet.survivalgames.listeners;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.regions.CylinderRegion;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import hwnet.survivalgames.handlers.*;
import hwnet.survivalgames.utils.GameBoard;
import hwnet.survivalgames.utils.LocUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;

import hwnet.survivalgames.SG;
import hwnet.survivalgames.events.GamerDeathEvent;
import hwnet.survivalgames.events.GamerKillEvent;
import hwnet.survivalgames.utils.ChatUtil;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IngameListener implements Listener {


    private static List<EntityType> moblist = new ArrayList<>();

    public static void addMobsToList() {
        moblist.add(EntityType.CREEPER);
        moblist.add(EntityType.ZOMBIE);
        moblist.add(EntityType.CAVE_SPIDER);
        moblist.add(EntityType.SPIDER);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        //if (!(event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.EGG)) event.setCancelled(true);
    }


    @EventHandler
    public void onMobEgg(PlayerEggThrowEvent e) {
        ItemStack is = e.getEgg().getItem();
        if (is.getItemMeta().getDisplayName().contains("Mob")) {
            Random rand = new Random();
            int random = rand.nextInt(moblist.size());
            e.setHatching(true);
            e.setHatchingType(moblist.get(random));
            e.getEgg().getLocation().getWorld().spawnEntity(e.getEgg().getLocation(), moblist.get(random));
        }
    }

    @EventHandler
    public void onMOTD(ServerListPingEvent e) {
        e.setMotd(ChatUtil.getMOTD());
    }

    @EventHandler
    public void onJoinPre(PlayerLoginEvent event) {
        if (event.getResult() == PlayerLoginEvent.Result.KICK_FULL) event.setResult(PlayerLoginEvent.Result.ALLOWED);
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

        // Player Menu item
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = head.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Spectate Menu");
        List<String> lore = new ArrayList<String>();
        lore.add("Click on a head to teleport");
        meta.setLore(lore);
        head.setItemMeta(meta);
        p.getInventory().setItem(0, head);

        ChatUtil.sendMessage(p, "Joined as spectator.");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (Gamer.getGamer(e.getPlayer().getUniqueId()) != null) {
            if (Gamer.getGamer(e.getPlayer()).isAlive()) {
                e.setQuitMessage(null);
                ChatUtil.broadcast("A tribute has fallen. " + Gamer.getAliveGamers().size() + "/" + Gamer.getGamers().size() + " tributes remain");
                PointSystem.addDeath(e.getPlayer());
                PointSystem.save(e.getPlayer());
                Gamer.getGamer(e.getPlayer()).remove();
            }
            SG.specGUI.getYourInventory().clear();
        }

        if (Bukkit.getOnlinePlayers().size() < 1) {
            Game.cancelGame();
        }
    }


    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        e.setCancelled(true);
        if (SG.districts_mode) {
            Team t = Team.getTeam(e.getPlayer());
            int points = PointSystem.getPoints(e.getPlayer().getUniqueId());
            String format = ChatUtil.getFormat().replace("%points", String.valueOf(points)).replace("%name", e.getPlayer().getName()).replace("%msg", e.getMessage());
            for (Player p : t.getPlayers()) {
                p.sendMessage(format);
                ChatUtil.sendMessage(SG.cmd, format);
            }
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
                if (SG.districts_mode) if (Team.getTeam(p).equals(Team.getTeam(d))) e.setCancelled(true);
            }
            if (p.getHealth() - e.getFinalDamage() < 1) {
                handleDeath(p);
                if (e.getDamager() instanceof Player) {
                    Player d = (Player) e.getDamager();
                    PointSystem.addKill(d);
                    Gamer.getGamer(d.getUniqueId()).addKill();

                    // Point System
                    PointSystem.addPoints(p, SG.config.getInt("points.lose"));
                    PointSystem.addPoints(d, SG.config.getInt("points.kill"));

                    Bukkit.getPluginManager().callEvent(new GamerKillEvent(p, d));
                }
            }
        }
    }

    @EventHandler
    public void onNaturalDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (!Gamer.getGamer(p.getUniqueId()).isAlive() || Gamer.getGamer(p.getUniqueId()).isSpectator()) {
                e.setCancelled(true);
                return;
            }
            if (p.getHealth() - e.getFinalDamage() < 1) {
                e.setCancelled(true);
                handleDeath(p);
            }
        }
    }

    @EventHandler
    public void onMobTarget(EntityTargetLivingEntityEvent e) {
        Entity target = e.getTarget();
        Entity entity = e.getEntity();

        if (target instanceof Player) {
            Player p = (Player) target;
            if (!Gamer.getGamer(p.getUniqueId()).isAlive() || Gamer.getGamer(p.getUniqueId()).isSpectator()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onTarget(EntityTargetEvent e) {
        Entity target = e.getTarget();
        Entity entity = e.getEntity();

        if (target instanceof Player) {
            Player p = (Player) target;
            if (!Gamer.getGamer(p.getUniqueId()).isAlive() || Gamer.getGamer(p.getUniqueId()).isSpectator()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onLeaveClick(PlayerInteractEvent e) {
        if (e.getItem() == null) return;
        if (!Gamer.getGamer(e.getPlayer().getUniqueId()).isAlive() || Gamer.getGamer(e.getPlayer().getUniqueId()).isSpectator())
            e.setCancelled(true);
        if (e.getItem().getType() == Material.PLAYER_HEAD) {
            SG.specGUI.open(e.getPlayer());
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (Gamer.getGamer(e.getPlayer()).isSpectator() || !Gamer.getGamer(e.getPlayer()).isAlive()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getCurrentItem() == null) return;
        if ((e.getCurrentItem().getType() == Material.COMPASS) || (e.getCurrentItem().getType() == Material.PLAYER_HEAD)) {
            e.setCancelled(true);
        }
        if (e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
            SG.specGUI.open((Player) e.getWhoClicked());
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent e) {
        if ((e.getItemDrop().getItemStack().getType() == Material.COMPASS) || (e.getItemDrop().getItemStack().getType() == Material.PLAYER_HEAD)) {
            e.getPlayer().sendMessage("It would not be wise to drop this item.");
            e.setCancelled(true);
        }
    }


    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.GLASS) event.setCancelled(true);
    }

    private void handleDeath(Player p) {
        if (!Gamer.getGamer(p.getUniqueId()).isAlive()) return;

        p.setHealth(20);
        for (ItemStack is : p.getInventory().getContents()) {
            if (is == null) continue;
            if (is.getType() == Material.COMPASS) continue;
            p.getWorld().dropItemNaturally(p.getLocation(), is);
        }

        for (ItemStack is : p.getInventory().getArmorContents()) {
            if (is == null) continue;
            p.getWorld().dropItemNaturally(p.getLocation(), is);
        }
        SG.clearPlayer(p);
        PointSystem.addDeath(p);
        p.setGameMode(GameMode.ADVENTURE);
        for (Gamer gl : Gamer.getAliveGamers()) {
            gl.getPlayer().hidePlayer(SG.pl, p);
        }
        p.setAllowFlight(true);
        p.setFlying(true);
        // Player Menu item
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = head.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Spectate Menu");
        List<String> lore = new ArrayList<String>();
        lore.add("Click on a head to teleport");
        meta.setLore(lore);
        head.setItemMeta(meta);
        p.getInventory().setItem(0, head);

        Gamer g = Gamer.getGamer(p);
        g.setAlive(false);
        g.setTimeAlive();
        GameBoard.getBoard(p).intiliazeDeath();

        for (Player pl : Bukkit.getOnlinePlayers()) {
            pl.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 20, 1);
            pl.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 20, 1);
        }
        ChatUtil.broadcast("A tribute has fallen. " + Gamer.getAliveGamers().size() + "/" + Gamer.getGamers().size() + " tributes remain");

        if (SG.districts_mode) {
            Team t = Team.getTeam(p);
            if (t.getAlivePlayers().size() < 1) {
                t.setIsAlive(false);
                List<String> motd = new ArrayList<String>();
                motd.add("&6Surval Games&7: &cIn Game");
                motd.add("&b" + Gamer.getAliveGamers().size() + "&7/&b24 tributes left!");
                ChatUtil.setMOTD(motd);
                for (Player po : t.getPlayers()) {
                    po.setGameMode(GameMode.ADVENTURE);
                }
                if (Team.getAliveTeams().size() == 1) {
                    Game.win(Team.getAliveTeams().get(0), null);
                } else {
                    ChatUtil.broadcast("District " + t.getName() + " has been eliminated! Only " + Team.getAliveTeams().size() + " left.");
                }
            }
            if (Team.getTeam(p).isAlive()) {
                p.setGameMode(GameMode.SPECTATOR);
                p.setSpectatorTarget(Team.getTeam(p).getAlivePlayers().get(0));
            }
        } else {
            if (Gamer.getAliveGamers().size() < 2) {
                Game.win(null, Gamer.getAliveGamers().get(0).getPlayer());
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
}