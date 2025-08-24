package org.examplef.spleef.commands.admin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.examplef.spleef.Spleef;
import org.examplef.spleef.instance.Arena;
import org.jetbrains.annotations.NotNull;

public class ManageArena implements CommandExecutor {

    private final Spleef spleef;

    public ManageArena(Spleef spleef) {
        this.spleef = spleef;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can run this command.");
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(ChatColor.GREEN + "Usage:");
            player.sendMessage(ChatColor.YELLOW + "/spleef register <arenaName> - Register a new arena");
            player.sendMessage(ChatColor.YELLOW + "/spleef delete <arenaName> - Delete an existing arena");
            return true;
        }

        String subCommand = args[0].toLowerCase();
        String arenaName = args[1].toLowerCase();

        switch (subCommand) {
            case "register" -> {
                if (spleef.getArenaManager().getArena(arenaName) != null) {
                    player.sendMessage(ChatColor.RED + "The arena '" + arenaName + "' already exists!");
                    return true;
                }

                Location loc = player.getLocation();
                spleef.getConfig().set("arenas." + arenaName + ".world", loc.getWorld().getName());
                spleef.getConfig().set("arenas." + arenaName + ".x", loc.getX());
                spleef.getConfig().set("arenas." + arenaName + ".y", loc.getY());
                spleef.getConfig().set("arenas." + arenaName + ".z", loc.getZ());
                spleef.getConfig().set("arenas." + arenaName + ".yaw", loc.getYaw());
                spleef.getConfig().set("arenas." + arenaName + ".pitch", loc.getPitch());

                Arena arena = new Arena(spleef, arenaName, loc);
                spleef.getArenaManager().getArenas().add(arena);
                spleef.saveConfig();

                player.sendMessage(ChatColor.GREEN + "Arena '" + arenaName + "' has been registered successfully!");
            }

            case "delete" -> {
                Arena arena = spleef.getArenaManager().getArena(arenaName);
                if (arena == null) {
                    player.sendMessage(ChatColor.RED + "Arena '" + arenaName + "' does not exist!");
                    return true;
                }

                // Kick all players in the arena
                for (Player p : arena.getAlivePlayers()) {
                    p.teleport(spleef.getConfig().getLocation("lobby", new Location(Bukkit.getWorlds().get(0), 0, 64, 0)));
                    p.getInventory().clear();
                    p.sendMessage(ChatColor.RED + "Arena '" + arenaName + "' has been deleted. You have been moved to the lobby.");
                }

                spleef.getConfig().set("arenas." + arenaName, null);
                spleef.getArenaManager().getArenas().remove(arena);
                spleef.saveConfig();


                player.sendMessage(ChatColor.GREEN + "Arena '" + arenaName + "' has been deleted successfully!");
            }

            default -> player.sendMessage(ChatColor.RED + "Unknown subcommand. Use register or delete.");
        }

        return true;
    }
}