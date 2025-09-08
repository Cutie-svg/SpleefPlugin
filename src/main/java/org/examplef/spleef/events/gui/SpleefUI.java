package org.examplef.spleef.events.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.examplef.spleef.instance.Arena;
import org.examplef.spleef.manager.ArenaManager;

import java.util.List;
import java.util.Random;

public class SpleefUI implements Listener {

    private ArenaManager arenaManager;

    private final Random random;

    public SpleefUI(ArenaManager arenaManager) {
        this.arenaManager = arenaManager;

        random = new Random();
    }

    public static ItemStack createCompass(Player player) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        CompassMeta compassMeta = (CompassMeta) compass.getItemMeta();
        compassMeta.setDisplayName(ChatColor.GRAY + "Join a game!");
        compass.setItemMeta(compassMeta);
        return compass;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        player.getInventory().clear();

        player.getInventory().addItem(createCompass(player));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) return;

        Player player = e.getPlayer();
        ItemStack item = e.getItem();

        if (item != null && item.getType() == Material.COMPASS && item.hasItemMeta()) {
            String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());

            if (name.equals("Join a game!")) {
                openGUI(player);
                e.setCancelled(true);
            }
        }
    }

    private void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.GOLD + "Join a game!");

        ItemStack snow = new ItemStack(Material.SNOW_BLOCK);
        ItemMeta snowMeta = snow.getItemMeta();

        snowMeta.setDisplayName(ChatColor.GREEN + "Join spleef!");
        snow.setItemMeta(snowMeta);

        gui.setItem(0, snow);
        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(ChatColor.GOLD + "Join a game!")) return;

        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();
        ItemStack clicked = e.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (clicked.getType() == Material.SNOW_BLOCK) {
            player.closeInventory();

            List<Arena> arenas = arenaManager.getArenas();

            if (arenas.isEmpty()) {
                player.sendMessage(ChatColor.RED + "No arenas available right now.");
                return;
            }
            Arena randomArena = arenas.get(random.nextInt(arenas.size()));
            randomArena.addPlayer(player);
        }
    }
}