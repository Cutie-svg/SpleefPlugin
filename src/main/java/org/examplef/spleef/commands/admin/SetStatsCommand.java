package org.examplef.spleef.commands.admin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.examplef.spleef.Spleef;
import org.examplef.spleef.events.gui.SetStatsGUI;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SetStatsCommand implements CommandExecutor {

    private final Spleef spleef;
    private final SetStatsGUI setStatsGUI;

    public SetStatsCommand(Spleef spleef) {
        this.spleef = spleef;
        this.setStatsGUI = new SetStatsGUI(spleef);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can run this.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /set <player>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

        if (target.getName() == null) {
            player.sendMessage(ChatColor.RED + "That player has never joined the server.");
            return true;
        }

        UUID targetUUID = target.getUniqueId();
        setStatsGUI.open(player, targetUUID);
        return true;
    }
}