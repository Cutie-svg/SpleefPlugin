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
    public int getGamesPlayed(UUID playerUUID) {
        Document doc = collection.find(Filters.eq("uuid", playerUUID.toString())).first();
        if (doc == null) return 0;
        return doc.getInteger("gamesPlayed", 0);
    }
    public void incrementGamesPlayed(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(spleef, () -> {
            collection.updateOne(
                    Filters.eq("uuid", uuid.toString()),
                    Updates.combine(
                            Updates.inc("gamesPlayed", 1),
                            Updates.set("username", player.getName())
                    ),
                    new UpdateOptions().upsert(true)
            );
        });
    }
    public int getLoses(UUID playerUUID) {
        Document doc = collection.find(Filters.eq("uuid", playerUUID.toString())).first();
        if (doc == null) return 0;
        return doc.getInteger("loses", 0);
    }

    public void incrementLoses(UUID uuid) {
        Player loser = Bukkit.getPlayer(uuid);
        if (loser == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(spleef, () -> {
            collection.updateOne(
                    Filters.eq("uuid", uuid.toString()),
                    Updates.combine(
                            Updates.inc("loses", 1),
                            Updates.set("username", loser.getName())
                    ),
                    new UpdateOptions().upsert(true)
            );
        });
    }
    public void resetStats(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        String username = player != null ? player.getName() : null;

        Bukkit.getScheduler().runTaskAsynchronously(spleef, () -> {
            Document updateFields = new Document("wins", 0)
                    .append("loses", 0)
                    .append("gamesPlayed", 0);
            if (username != null) {
                updateFields.append("username", username);
            }

            collection.updateOne(
                    Filters.eq("uuid", uuid.toString()),
                    new Document("$set", updateFields),
                    new UpdateOptions().upsert(true)
            );
        });
    }
    public void resetAllStats() {
        Bukkit.getScheduler().runTaskAsynchronously(spleef, () -> {
            Document resetFields = new Document("wins", 0)
                    .append("loses", 0)
                    .append("gamesPlayed", 0);

            collection.updateMany(
                    new Document(),
                    new Document("$set", resetFields)
            );
        });
    }
    public void setWins(UUID uuid, int wins) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(spleef, () -> {
            collection.updateOne(
                    Filters.eq("uuid", uuid.toString()),
                    Updates.combine(
                            Updates.set("wins", wins),
                            Updates.set("username", player.getName())
                    ),
                    new UpdateOptions().upsert(true)
            );
        });
    }

    public void setLoses(UUID uuid, int loses) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(spleef, () -> {
            collection.updateOne(
                    Filters.eq("uuid", uuid.toString()),
                    Updates.combine(
                            Updates.set("loses", loses),
                            Updates.set("username", player.getName())
                    ),
                    new UpdateOptions().upsert(true)
            );
        });
    }

    public void setGamesPlayed(UUID uuid, int number) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(spleef, () -> {
            collection.updateOne(
                    Filters.eq("uuid", uuid.toString()),
                    Updates.combine(
                            Updates.set("gamesPlayed", number),
                            Updates.set("username", player.getName())
                    ),
                    new UpdateOptions().upsert(true)
            );
        });
    }

}
