package org.examplef.spleef.commands.user;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.examplef.spleef.GameState;
import org.examplef.spleef.Spleef;
import org.examplef.spleef.instance.Arena;

public class ArenaCommand implements CommandExecutor {

    private Spleef spleef;

    public ArenaCommand(Spleef spleef) {
        this.spleef = spleef;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (!(sender instanceof Player player)) return true;

        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            player.sendMessage(ChatColor.AQUA + "This are the available arenas: ");
            for (Arena arena : spleef.getArenaManager().getArenas()) {
                player.sendMessage(ChatColor.GREEN + "- " + arena.getId() + "( " + arena.getState().name() + " )");
            }
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("leave")) {
            Arena arena = spleef.getArenaManager().getArena(player);
            if (arena != null) {
                arena.removePlayer(player);
            } else {
                player.sendMessage(ChatColor.RED + "You are not in an arena.");
            }
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
            if (spleef.getArenaManager().getArena(player) != null) {
                player.sendMessage(ChatColor.RED + "You are already playing in an arena!");
                return false;
            }


            int id;
            try {
                id = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid arena id.");
                return false;
            }

            if (id < 0 || id >= spleef.getArenaManager().getArenas().size()) {
                player.sendMessage("Cannot join arena right now");
                return true;
            }

            Arena arena = spleef.getArenaManager().getArena(id);
            if (arena.getState() == GameState.RECRUITING || arena.getState() == GameState.COUNTDOWN) {
                arena.addPlayer(player);
            } else {
                player.sendMessage("Cannot join arena right now");
            }
            return true;
        }

        player.sendMessage(ChatColor.BOLD + "Invalid arena id.");
        return true;
    }
}
