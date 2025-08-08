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

    private final Spleef spleef;

    public ArenaCommand(Spleef spleef) {
        this.spleef = spleef;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can run this command.");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            player.sendMessage(ChatColor.AQUA + "Available arenas:");
            for (Arena arena : spleef.getArenaManager().getArenas()) {
                player.sendMessage(ChatColor.GREEN + "- " + arena.getId() + " (State: " + arena.getState().name() + ")");
            }
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("leave")) {
            Arena arena = spleef.getArenaManager().getArena(player);
            if (arena != null) {
                arena.removePlayer(player);
                player.sendMessage(ChatColor.GREEN + "You have left the arena.");
            } else {
                player.sendMessage(ChatColor.RED + "You are not currently in an arena.");
            }
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
            if (spleef.getArenaManager().getArena(player) != null) {
                player.sendMessage(ChatColor.RED + "You are already in an arena!");
                return true;  // Return true since handled
            }

            int id;
            try {
                id = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid arena ID. Please enter a valid number.");
                return true;
            }

            if (id < 0 || id >= spleef.getArenaManager().getArenas().size()) {
                player.sendMessage(ChatColor.RED + "Arena with ID " + id + " does not exist.");
                return true;
            }

            Arena arena = spleef.getArenaManager().getArena(id);
            if (arena == null) {
                player.sendMessage(ChatColor.RED + "Arena not found.");
                return true;
            }

            if (arena.getState() == GameState.RECRUITING || arena.getState() == GameState.COUNTDOWN) {
                arena.addPlayer(player);
                player.sendMessage(ChatColor.GREEN + "You joined arena " + id + ".");
            } else {
                player.sendMessage(ChatColor.RED + "Cannot join arena right now.");
            }
            return true;
        }

        // Default fallback message
        player.sendMessage(ChatColor.RED + "Usage:");
        player.sendMessage(ChatColor.YELLOW + "/arena list - List all arenas");
        player.sendMessage(ChatColor.YELLOW + "/arena join <id> - Join an arena");
        player.sendMessage(ChatColor.YELLOW + "/arena leave - Leave current arena");
        return true;
    }
}
