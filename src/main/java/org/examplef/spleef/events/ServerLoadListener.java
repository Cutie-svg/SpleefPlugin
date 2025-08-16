package org.examplef.spleef.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.examplef.spleef.Spleef;

public class ServerLoadListener implements Listener {

    private Spleef spleef;

    public ServerLoadListener(Spleef spleef) { this.spleef = spleef; }

    @EventHandler
    public void onServerLoad(ServerLoadEvent e) { spleef.getArenaManager().reload(); }
}
