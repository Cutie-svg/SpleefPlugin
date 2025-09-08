package org.examplef.spleef.manager.database;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.examplef.spleef.Spleef;

import java.io.File;

public class MongoManager {

    private final Spleef spleef;

    private MongoClient client;
    private MongoDatabase database;
    private MongoCollection<Document> players;

    private final FileConfiguration mongoConfig;

    public MongoManager(Spleef spleef) {
        this.spleef = spleef;

        File mongoFile = new File(spleef.getDataFolder(), "mongo.yml");
        if (!mongoFile.exists()) {
            spleef.saveResource("mongo.yml", false);
            spleef.getLogger().info("mongo.yml was missing, copied default from JAR.");
        }

        this.mongoConfig = YamlConfiguration.loadConfiguration(mongoFile);
        connect();
    }

    private void connect() {
        try {
            String uri = mongoConfig.getString("uri");
            String dbName = mongoConfig.getString("database");
            String collectionName = mongoConfig.getString("collection");

            if (uri == null || dbName == null || collectionName == null) {
                spleef.getLogger().severe("Mongo config is missing required values!");
                return;
            }

            client = MongoClients.create(uri);
            database = client.getDatabase(dbName);
            players = database.getCollection(collectionName);

            spleef.getLogger().info("Connected to MongoDB! Database: " + dbName + " | Collection: " + collectionName);
        } catch (MongoException e) {
            spleef.getLogger().severe("Failed to connect to MongoDB: " + e.getMessage());
        }
    }

    public MongoCollection<Document> getPlayersCollection() {
        return players;
    }

    public void disconnect() {
        if (client != null) {
            client.close();
            spleef.getLogger().info("Disconnected from MongoDB!");
        }
    }
}
