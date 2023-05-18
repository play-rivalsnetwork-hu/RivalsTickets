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

import java.util.Collections;

public class Storage {
    private static MongoClient client;

    public Storage() {
        ServerAddress address = new ServerAddress(Config.ADDRESS, 27017);
        MongoCredential credential = MongoCredential.createCredential(Config.USERNAME, Config.DATABASE, Config.PASSWORD.toCharArray());
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
            exception.printStackTrace();
        }
    }

    public interface MongoCallback {
        void accept(MongoDatabase database) throws MongoException;
    }
}
