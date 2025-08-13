package org.examplef.spleef.commands.admin;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.examplef.spleef.Spleef;

import java.util.UUID;

public class ResetStats implements CommandExecutor {

    private final Spleef spleef;

    public ResetStats(Spleef spleef) {
        this.spleef = spleef;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            spleef.getPlayerManager().resetAllStats();
            sender.sendMessage("§aAll player stats have been reset.");
            return true;
        }

        if (args.length == 1) {
            String targetName = args[0];
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

            if (target == null || target.getUniqueId() == null) {
                sender.sendMessage("§cPlayer not found: " + targetName);
                return true;
            }

            UUID targetUUID = target.getUniqueId();
            spleef.getPlayerManager().resetStats(targetUUID);
            sender.sendMessage("§aStats reset for player: " + targetName);
            return true;
        }

        sender.sendMessage("§cUsage:");
        sender.sendMessage("§c/spleefreset           - reset all player stats");
        sender.sendMessage("§c/spleefreset <player> - reset stats for specific player");
        return true;
    }
}
