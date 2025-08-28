package org.examplef.spleef.commands.admin;

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.world.SlimeWorld;
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

import java.io.IOException;

public class ManageArena implements CommandExecutor {

    private final Spleef spleef;
    private final AdvancedSlimePaperAPI asp;

    public ManageArena(Spleef spleef) {
        this.spleef = spleef;
        this.asp = spleef.getApi();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can run this command.");
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(ChatColor.GREEN + "Usage:");
            player.sendMessage(ChatColor.YELLOW + "/spleef register <arenaName>");
            player.sendMessage(ChatColor.YELLOW + "/spleef delete <arenaName>");
            return true;
        }

        String subCommand = args[0].toLowerCase();
        String arenaName = args[1].toLowerCase();

        switch (subCommand) {
            case "register" -> {
                if (spleef.getArenaManager().getArena(arenaName) != null) {
                    player.sendMessage(ChatColor.RED + "Arena '" + arenaName + "' already exists!");
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

                SlimeWorld slimeWorld = asp.getLoadedWorld(arenaName);

                player.sendMessage(ChatColor.GREEN + "Arena '" + arenaName + "' registered!");

                Bukkit.getScheduler().runTaskAsynchronously(spleef, () -> {
                    try {
                        asp.saveWorld(slimeWorld);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

            }
            case "delete" -> {
                Arena arena = spleef.getArenaManager().getArena(arenaName);
                if (arena == null) {
                    player.sendMessage(ChatColor.RED + "Arena '" + arenaName + "' does not exist!");
                    return true;
                }

                spleef.getConfig().set("arenas." + arenaName, null);
                spleef.getArenaManager().getArenas().remove(arena);
                spleef.saveConfig();

                player.sendMessage(ChatColor.GREEN + "Arena '" + arenaName + "' deleted!");
                Bukkit.unloadWorld(arenaName, false);
            }
            default -> player.sendMessage(ChatColor.RED + "Unknown subcommand. Use register or delete.");
        }

        return true;
    }
}
