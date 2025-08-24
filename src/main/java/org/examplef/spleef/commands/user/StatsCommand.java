package org.examplef.spleef.commands.user;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.examplef.spleef.Spleef;
import org.examplef.spleef.gui.StatsUI;
import org.jetbrains.annotations.NotNull;

public class StatsCommand implements CommandExecutor {

    private final Spleef spleef;
    private final StatsUI statsUI;

    public StatsCommand(Spleef spleef) {
        this.spleef = spleef;
        statsUI = new StatsUI(spleef);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can run this command.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /stats <player>");
            return true;
        }
        String target = args[0];
        statsUI.openStats(player, target);


        return true;
    }
}