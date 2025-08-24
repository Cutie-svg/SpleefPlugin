package org.examplef.spleef.manager.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
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

    // ---------- Increment Methods ----------

    public void incrementWins(UUID uuid) {
        asyncIncrement(uuid, "wins");
    }

    public void incrementLoses(UUID uuid) {
        asyncIncrement(uuid, "loses");
    }

    public void incrementGamesPlayed(UUID uuid) {
        asyncIncrement(uuid, "gamesPlayed");
    }

    private void asyncIncrement(UUID uuid, String field) {
        Bukkit.getScheduler().runTaskAsynchronously(spleef, () -> {
            Player player = Bukkit.getPlayer(uuid);
            String username = player != null ? player.getName() : null;

            Document updateDoc = new Document("$inc", new Document(field, 1));
            if (username != null) {
                updateDoc.append("$set", new Document("username", username));
            }

            collection.updateOne(
                    Filters.eq("uuid", uuid.toString()),
                    updateDoc,
                    new UpdateOptions().upsert(true)
            );
        });
    }

    // ---------- Setter Methods ----------

    public void setWins(UUID uuid, int value) {
        asyncSet(uuid, "wins", value);
    }

    public void setLoses(UUID uuid, int value) {
        asyncSet(uuid, "loses", value);
    }

    public void setGamesPlayed(UUID uuid, int value) {
        asyncSet(uuid, "gamesPlayed", value);
    }

    private void asyncSet(UUID uuid, String field, int value) {
        Bukkit.getScheduler().runTaskAsynchronously(spleef, () -> {
            Player player = Bukkit.getPlayer(uuid);
            String username = player != null ? player.getName() : null;

            Document updateDoc = new Document(field, value);
            if (username != null) updateDoc.append("username", username);

            collection.updateOne(
                    Filters.eq("uuid", uuid.toString()),
                    new Document("$set", updateDoc),
                    new UpdateOptions().upsert(true)
            );
        });
    }

    // ---------- Reset Methods ----------

    public void resetStats(UUID uuid) {
        asyncSet(uuid, "wins", 0);
        asyncSet(uuid, "loses", 0);
        asyncSet(uuid, "gamesPlayed", 0);
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

    // ---------- Getter Methods ----------

    public int getWins(UUID uuid) {
        return getField(uuid, "wins");
    }

    public int getLoses(UUID uuid) {
        return getField(uuid, "loses");
    }

    public int getGamesPlayed(UUID uuid) {
        return getField(uuid, "gamesPlayed");
    }

    private int getField(UUID uuid, String field) {
        Document doc = collection.find(Filters.eq("uuid", uuid.toString())).first();
        if (doc == null) return 0;
        return doc.getInteger(field, 0);
    }
}
