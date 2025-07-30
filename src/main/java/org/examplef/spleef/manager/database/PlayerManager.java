package org.examplef.spleef.manager.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.examplef.spleef.Spleef;

import java.util.UUID;

public class PlayerManager {

    private final Spleef spleef;
    private final MongoCollection<Document> collection;

    public PlayerManager(Spleef spleef, MongoManager mongoManager) {
        this.spleef = spleef;
        this.collection = mongoManager.getPlayersCollection();
    }

    public void incrementWins(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(spleef, () -> {
            collection.updateOne(
                    Filters.eq("uuid", uuid.toString()),
                    Updates.combine(
                            Updates.inc("wins", 1),
                            Updates.set("username", player.getName())
                    ),
                    new UpdateOptions().upsert(true)
            );
        });
    }

    public int getWins(UUID playerUUID) {
        Document doc = collection.find(Filters.eq("uuid", playerUUID.toString())).first();
        if (doc == null) return 0;
        return doc.getInteger("wins", 0);
    }
}
