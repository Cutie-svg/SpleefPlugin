package org.examplef.spleef.manager.database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoManager {

    private final MongoClient client;
    private final MongoDatabase database;
    private final MongoCollection<Document> players;

    public MongoManager() {
        client = MongoClients.create();
        database = client.getDatabase("player-stats");
        players = database.getCollection("stats");
    }

    public MongoCollection<Document> getPlayersCollection() { return players; }
    public void disconnect() {if (client != null) client.close(); }
}
