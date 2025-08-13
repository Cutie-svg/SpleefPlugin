package org.examplef.spleef.commands.admin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.examplef.spleef.Spleef;

public class ReloadCommand implements CommandExecutor {

    private Spleef spleef;

    public ReloadCommand(Spleef spleef) { this.spleef = spleef;}

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (sender instanceof Player || sender instanceof ConsoleCommandSender) {
            spleef.getArenaManager().reload();
            sender.sendMessage(ChatColor.GREEN + "Arena reloaded successfully.");
        }


        return true;
    }


}
