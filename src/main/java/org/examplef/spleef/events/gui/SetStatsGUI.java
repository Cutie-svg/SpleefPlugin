package org.examplef.spleef.events.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.examplef.spleef.Spleef;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SetStatsGUI implements Listener {

    private final Spleef spleef;
    private final Map<UUID, UUID> targetMap = new HashMap<>();
    private final Map<UUID, String> awaitingInput = new HashMap<>();

    public SetStatsGUI(Spleef spleef) {
        this.spleef = spleef;
    }

    public void open(Player admin, UUID targetUUID) {
        targetMap.put(admin.getUniqueId(), targetUUID);

        Inventory inv = Bukkit.createInventory(null, 9, ChatColor.DARK_GREEN + "Set Player Stats");

        inv.setItem(2, createItem(Material.DIAMOND_SWORD, ChatColor.GREEN + "Set Wins"));
        inv.setItem(4, createItem(Material.BONE, ChatColor.RED + "Set Loses"));
        inv.setItem(6, createItem(Material.CLOCK, ChatColor.GOLD + "Set Games Played"));  // New item
        inv.setItem(8, createItem(Material.BARRIER, ChatColor.YELLOW + "Reset Stats"));  // Shifted to slot 8

        admin.openInventory(inv);
    }

    private ItemStack createItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        if (!ChatColor.stripColor(e.getView().getTitle()).equalsIgnoreCase("Set Player Stats")) return;

        int slot = e.getRawSlot();
        if (slot < 0 || slot >= e.getInventory().getSize()) return;

        e.setCancelled(true);

        UUID targetUUID = targetMap.get(player.getUniqueId());
        if (targetUUID == null) return;

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID);

        switch (slot) {
            case 2 -> {
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "Enter new wins count for " + target.getName() + ":");
                awaitingInput.put(player.getUniqueId(), "wins");
            }
            case 4 -> {
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "Enter new loses count for " + target.getName() + ":");
                awaitingInput.put(player.getUniqueId(), "loses");
            }
            case 6 -> {
                player.closeInventory();
                player.sendMessage(ChatColor.GOLD + "Enter new games played count for " + target.getName() + ":");
                awaitingInput.put(player.getUniqueId(), "gamesPlayed");
            }
            case 8 -> {
                spleef.getPlayerManager().setWins(targetUUID, 0);
                spleef.getPlayerManager().setLoses(targetUUID, 0);
                spleef.getPlayerManager().setGamesPlayed(targetUUID, 0);
                player.sendMessage(ChatColor.YELLOW + "Stats reset for " + target.getName() + ".");
            }
        }
    }


    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        UUID adminUUID = player.getUniqueId();

        if (!awaitingInput.containsKey(adminUUID)) return;

        e.setCancelled(true);

        String type = awaitingInput.get(adminUUID);
        UUID targetUUID = targetMap.get(adminUUID);
        if (targetUUID == null) return;

        try {
            int value = Integer.parseInt(e.getMessage());
            if (value < 0) {
                player.sendMessage(ChatColor.RED + "Value must be a positive number.");
                return;
            }

            switch (type) {
                case "wins" -> spleef.getPlayerManager().setWins(targetUUID, value);
                case "loses" -> spleef.getPlayerManager().setLoses(targetUUID, value);
                case "gamesPlayed" -> spleef.getPlayerManager().setGamesPlayed(targetUUID, value);
            }

            player.sendMessage(ChatColor.GREEN + "Set " + type + " to " + value + ".");
            awaitingInput.remove(adminUUID);
        } catch (NumberFormatException ex) {
            player.sendMessage(ChatColor.RED + "Invalid number. Try again.");
        }

    }
}
