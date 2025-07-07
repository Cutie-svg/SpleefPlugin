package org.examplef.spleef.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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

        if (sender instanceof Player player) {
            if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
                player.sendMessage(ChatColor.AQUA + "This are the available arenas: ");
                for (Arena arena : spleef.getArenaManager().getArenas()) {
                    player.sendMessage(ChatColor.GREEN + "- " + arena.getId() + "( " + arena.getState().name() + " )");
                }
            } else if (args.length == 1 && args[0].equalsIgnoreCase("leave")) {
                Arena arena = spleef.getArenaManager().getArena(player);
                if (arena != null) {
                    arena.removePlayer(player);
                } else {
                    player.sendMessage(ChatColor.RED + "You are not in an arena.");
                }
            } else if (args.length == 1 && args[0].equalsIgnoreCase("join")) {
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
                if (id >= 0 && id < spleef.getArenaManager().getArenas().size()) {
                    Arena arena = spleef.getArenaManager().getArena(id);
                    if (arena.getState() == GameState.RECRUITING || arena.getState() == GameState.COUNTDOWN) {
                        arena.addPlayer(player);
                        ItemStack shovel = new ItemStack(Material.DIAMOND_SHOVEL, 1);
                        player.getInventory().addItem(shovel);
                    } else {
                        player.sendMessage("Cannot join arena right now");
                    }

                } else {
                    player.sendMessage("Cannot join arena right now");
                }
            } else {
                player.sendMessage(ChatColor.BOLD + "Invalid arena id.");
            }
        }
        return true;
    }
}