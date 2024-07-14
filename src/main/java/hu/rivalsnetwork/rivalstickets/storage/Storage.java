package hu.rivalsnetwork.rivalstickets.storage;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import hu.rivalsnetwork.rivalstickets.configuration.Config;
import org.bson.UuidRepresentation;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class Storage {
    private static final Logger log = LoggerFactory.getLogger(Storage.class);
    private static MongoClient client;

    public static void reload() {
        ServerAddress address = new ServerAddress(Config.ADDRESS, 27017);
        MongoCredential credential = MongoCredential.createCredential(Config.USERNAME, Config.AUTH_DATABASE, Config.PASSWORD.toCharArray());
        MongoClientSettings settings = MongoClientSettings.builder()
                .uuidRepresentation(UuidRepresentation.JAVA_LEGACY)
                .credential(credential)
                .applyToClusterSettings(builder -> builder.hosts(Collections.singletonList(address))).build();

        client = MongoClients.create(settings);
    }

    public static void mongo(@NotNull MongoCallback callback) {
        try {
            callback.accept(client.getDatabase(Config.DATABASE));
        } catch (MongoException exception) {
            log.error("An exception occurred while using MongoDB!", exception);
        }
    }

    public interface MongoCallback {
        void accept(MongoDatabase database) throws MongoException;
    }
}
