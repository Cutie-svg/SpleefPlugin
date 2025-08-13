package org.examplef.spleef.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.examplef.spleef.Spleef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class StatsUI implements Listener {
    private final Spleef spleef;
    private static final int STATS_SLOT = 4;
    private static final int CLOSE_SLOT = 8;

    public StatsUI(Spleef spleef) {
        this.spleef = spleef;
    }

    public void openStats(Player viewer, String targetName) {

        if (targetName == null || targetName.isEmpty()) {
            viewer.sendMessage(ChatColor.RED + "Please specify a player name!");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        UUID targetUUID = target.getUniqueId();

        int wins = spleef.getPlayerManager().getWins(targetUUID);
        int gamesPlayed = spleef.getPlayerManager().getGamesPlayed(targetUUID);
        int losses = spleef.getPlayerManager().getLoses(targetUUID);

        // Calculate WLR
        double wlr = losses == 0 ? wins : (double) wins / losses;

        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.GOLD + targetName + "'s Stats");

        ItemStack playerHead = getPlayerHead(target, targetName, wins, gamesPlayed, losses, wlr);
        gui.setItem(STATS_SLOT, playerHead);

        gui.setItem(2, createStatItem(
                Material.DIAMOND,
                ChatColor.AQUA + "Wins",
                ChatColor.GRAY + "Total: " + ChatColor.GREEN + wins));

        gui.setItem(5, createStatItem(
                Material.REDSTONE,
                ChatColor.RED + "Losses",
                ChatColor.GRAY + "Total: " + ChatColor.GREEN + losses));

        gui.setItem(6, createStatItem(
                Material.BOOK,
                ChatColor.YELLOW + "Games",
                ChatColor.GRAY + "Played: " + ChatColor.GREEN + gamesPlayed
        ));

        gui.setItem(CLOSE_SLOT, createCloseButton());

        viewer.openInventory(gui);
    }

    private ItemStack getPlayerHead(OfflinePlayer target, String name, int wins, int gamesPlayed, int losses, double wlr) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        meta.setDisplayName(ChatColor.GOLD + name + "'s Stats");
        meta.setOwningPlayer(target);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Status: " + getOnlineStatus(target));
        lore.add("");
        lore.add(ChatColor.WHITE + "Main Statistics:");
        lore.add(ChatColor.GRAY + " ▸ Wins: " + ChatColor.GREEN + wins);
        lore.add(ChatColor.GRAY + " ▸ Losses: " + ChatColor.RED + losses);
        lore.add(ChatColor.GRAY + " ▸ WLR: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", wlr));
        lore.add(ChatColor.GRAY + " ▸ Games: " + ChatColor.GREEN + gamesPlayed);
        lore.add("");

        meta.setLore(lore);
        head.setItemMeta(meta);
        return head;
    }

    private ItemStack createStatItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createCloseButton() {
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta meta = close.getItemMeta();

        meta.setDisplayName(ChatColor.RED + "Close");
        close.setItemMeta(meta);

        return close;
    }

    private String getOnlineStatus(OfflinePlayer player) {
        return player.isOnline() ?
                ChatColor.GREEN + "Online" :
                ChatColor.RED + "Offline";
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;

        Player player = (Player) e.getWhoClicked();

        if (e.getView().getTitle().endsWith("'s Stats")) {
            e.setCancelled(true);

            if (e.getSlot() == CLOSE_SLOT) {
                player.closeInventory();
            }
        }
    }
}