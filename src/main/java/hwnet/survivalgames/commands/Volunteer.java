package hwnet.survivalgames.commands;

import hwnet.survivalgames.handlers.Team;
import hwnet.survivalgames.utils.ChatUtil;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Volunteer implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can volunteer for a district.");
            return true;
        }

        Player p = (Player) sender;

        if (args.length != 1 && args.length != 2) {
            ChatUtil.sendMessage(p, "Usage: /volunteer <district number|regret>");
            return true;
        }

        if (args.length == 1) {

            if (args[0].equalsIgnoreCase("regret")) {
                Team t = Team.getTeam(p);
                if (t == null) {
                    ChatUtil.sendMessage(p, "You have not volunteered for a district.");
                    return true;
                }
                t.remove(p);
                ChatUtil.sendMessage(p, "You have stepped down from district " + t.getName() + ".");
                return true;
            }

            if (Team.getTeam(args[0]) == null) {
                ChatUtil.sendMessage(p, "That is not a valid district.");
                return true;
            }

            if (Team.getTeam(p) != null) {
                ChatUtil.sendMessage(p, "You are already in a district! Please use /volunteer regret to swap districts.");
                return true;
            }

            Team t = Team.getTeam(args[0]);

            if (t.getPlayers().size() > 1) {
                ChatUtil.sendMessage(p, "That district already has enough volunteers.");
                return true;
            }

            if (t.getPlayers().size() == 0) {
                t.add(p);
                ChatUtil.sendMessage(p, "District " + t.getName() + " has let you volunteer. You are now a part of that district.");
                return true;
            }

            t.sendJoinRequest(p);
            ChatUtil.sendMessage(p, "Volunteered for district " + t.getName() + ".");

            Player leader = t.getPlayers().get(0);


            TextComponent message = new TextComponent(p.getName() + " has volunteered for your team.");
            TextComponent accept = new TextComponent(" [ACCEPT] ");
            accept.setColor(net.md_5.bungee.api.ChatColor.GREEN);
            accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/volunteer accept " + p.getName()));
            accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to accept").create()));
            TextComponent decline = new TextComponent(" [DECLINE] ");
            decline.setColor(net.md_5.bungee.api.ChatColor.RED);
            decline.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/volunteer decline " + p.getName()));
            accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to decline").create()));

            BaseComponent[] components = new ComponentBuilder(ChatUtil.prefix() + p.getName() + " has volunteered for your team.").create();
            BaseComponent[] clickables = new ComponentBuilder(ChatUtil.prefix()).append(accept).append(decline).create();
            leader.spigot().sendMessage(components);
            leader.spigot().sendMessage(clickables);
            return true;
        }

        if (args.length == 2) {
            if (!args[0].equalsIgnoreCase("accept") && !args[0].equalsIgnoreCase("decline")) {
                ChatUtil.sendMessage(p, "Usage: /volunteer accept|decline <username>");
                return true;
            }

            if (args[0].equalsIgnoreCase("accept")) {
                Team t = Team.getTeam(p);
                if (t == null) {
                    ChatUtil.sendMessage(p, "You need to be in a district to accept volunteers.");
                    return true;
                }

                if(t.getPlayers().size() == t.getTeamSize()){

                }

                Player volunt = Bukkit.getPlayer(args[1]);

                t.add(volunt);
                t.removeJoinRequest(volunt);

                ChatUtil.sendMessage(volunt, "District " + t.getName() + " has let you volunteer. You are now a part of that district.");

                return true;
            }

            if (args[0].equalsIgnoreCase("decline")) {
                Team t = Team.getTeam(p.getName());
                if (t == null) {
                    ChatUtil.sendMessage(p, "You need to be in a district to decline volunteers.");
                    return true;
                }
                Player volunt = Bukkit.getPlayer(args[1]);
                t.removeJoinRequest(volunt);
                return true;
            }

        }
        return false;
    }
}
