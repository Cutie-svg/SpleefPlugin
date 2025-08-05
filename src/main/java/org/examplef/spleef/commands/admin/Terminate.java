package org.examplef.spleef.commands.admin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.examplef.spleef.Spleef;
import org.examplef.spleef.instance.Arena;
import org.jetbrains.annotations.NotNull;

public class Terminate implements CommandExecutor {

    private final Spleef spleef;

    public Terminate(Spleef spleef) {
        this.spleef = spleef;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /terminate <player>");
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Player not found or not online.");
            return true;
        }

        boolean terminated = false;
        for (Arena arena : spleef.getArenaManager().getArenas()) {
            if (arena.getPlayers().contains(player.getUniqueId())) {
                arena.sendMessage(ChatColor.YELLOW + "Match terminated by an admin.");
                arena.end();
                sender.sendMessage(ChatColor.GREEN + "Terminated match for player " + player.getName());
                terminated = true;
                break;
            }
        }

        if (!terminated) {
            sender.sendMessage(ChatColor.RED + "Player is not in any arena.");
        }

        return true;
    }
}
