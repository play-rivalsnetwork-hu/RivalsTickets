package hu.rivalsnetwork.rivalstickets.storage;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.UpdateOptions;
import me.ryzeon.transcripts.DiscordHtmlTranscripts;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.stream.Collectors;

public class Executor {

    public static boolean isTicketBanned(@NotNull String id) {
        boolean[] ticketBanned = {false};

        Storage.mongo(database -> {
            MongoCollection<Document> collection = database.getCollection("rivals_tickets_users");
            Document find = new Document();
            find.put("discord_id", id);
            FindIterable<Document> cursor = collection.find(find);
            try (final MongoCursor<Document> iterator = cursor.cursor()) {
                if (iterator.hasNext()) {
                    ticketBanned[0] = iterator.next().getBoolean("ticket_banned");
                }
            }
        });

        return ticketBanned[0];
    }

    public static void createUser(@NotNull Member member) {
        int id = getNextID("rivals_tickets_users_counter");
        Storage.mongo(database -> {
            MongoCollection<Document> collection = database.getCollection("rivals_tickets_users");
            Document document = new Document();
            document.put("_id", id);
            document.put("discord_id", member.getId());
            document.put("ticket_banned", false);

            collection.insertOne(document);
        });
    }

    public static void deleteTicket(@NotNull Member closer, @NotNull TextChannel channel, @NotNull String reason)  {
        if (!isTicket(channel)) return;
        DiscordHtmlTranscripts transcript = DiscordHtmlTranscripts.getInstance();

        try (InputStream stream = transcript.generateFromMessages(channel.getIterableHistory().stream().collect(Collectors.toList()))) {
            close(closer, channel, stream, reason);
            channel.delete().queue();
        } catch (Exception exception) {
            System.getLogger("RivalsTickets").log(System.Logger.Level.ERROR, "There was an issue while getting inputstream for transcript!");
        }
    }

    public static String getMemberIdByChannel(@NotNull TextChannel channel) {
        String[] memberID = {""};
        Storage.mongo(database -> {
            MongoCollection<Document> collection = database.getCollection("rivals_tickets_tickets");
            Document search = new Document();
            search.put("channel_id", channel.getId());
            FindIterable<Document> cursor = collection.find(search);
            try (final MongoCursor<Document> iterator = cursor.cursor()) {
                if (iterator.hasNext()) {
                    memberID[0] = iterator.next().getString("owner");
                }
            }
        });

        return memberID[0];
    }

    public static boolean isTicket(@NotNull TextChannel channel) {
        boolean[] ticket = {false};
        Storage.mongo(database -> {
            MongoCollection<Document> collection = database.getCollection("rivals_tickets_tickets");
            Document search = new Document();
            search.put("channel_id", channel.getId());
            FindIterable<Document> cursor = collection.find(search);
            try (final MongoCursor<Document> iterator = cursor.cursor()) {
                if (iterator.hasNext()) {
                    ticket[0] = true;
                }
            }
        });

        return ticket[0];
    }

    public static boolean hasOpenTicket(@NotNull Member member) {
        boolean[] hasTicket = {false};
        Storage.mongo(database -> {
            MongoCollection<Document> collection = database.getCollection("rivals_tickets_tickets");
            Document find = new Document();
            find.put("owner", member.getId());
            FindIterable<Document> cursor = collection.find(find);
            try (final MongoCursor<Document> iterator = cursor.cursor()) {
                while (iterator.hasNext()) {
                    if (iterator.next().getBoolean("closed")) continue;

                    hasTicket[0] = true;
                    break;
                }
            }
        });

        return hasTicket[0];
    }

    public static boolean doesUserExist(@NotNull Member member) {
        boolean[] exists = {false};

        Storage.mongo(database -> {
            MongoCollection<Document> collection = database.getCollection("rivals_tickets_users");
            Document find = new Document();
            find.put("discord_id", member.getId());
            FindIterable<Document> cursor = collection.find(find);
            try (final MongoCursor<Document> iterator = cursor.cursor()) {
                if (iterator.hasNext()) {
                    exists[0] = true;
                }
            }
        });

        return exists[0];
    }

    public static int getNextID(String collectionName) {
        int[] id = {0};
        Storage.mongo(database -> {
            MongoCollection<Document> collection = database.getCollection(collectionName);
            Document find = new Document();
            find.put("_id", "userid");
            Document update = new Document();
            update.put("$inc", new Document("seq", 1));
            Document object = collection.findOneAndUpdate(find, update, new FindOneAndUpdateOptions().upsert(true));
            id[0] = object.getInteger("seq");
        });

        return id[0];
    }

    public static void createTicket(@NotNull Member member, TextChannel channel, int id) {
        Storage.mongo(database -> {
            MongoCollection<Document> collection = database.getCollection("rivals_tickets_tickets");
            Document document = new Document();
            document.put("channel_id", channel.getId());
            document.put("owner", member.getId());
            document.put("_id", id);
            document.put("closed", false);
            document.put("transcript", new Document());
            document.put("closer", "");
            document.put("close-time", "");
            document.put("close-reason", "");

            collection.insertOne(document);
        });
    }

    public static void close(@NotNull Member closer, @NotNull TextChannel channel, @NotNull InputStream stream, @NotNull String reason) {
        Storage.mongo(database -> {
            MongoCollection<Document> collection = database.getCollection("rivals_tickets_tickets");
            Document search = new Document();
            search.put("channel_id", channel.getId());
            Document document = new Document();
            document.put("closed", true);
            document.put("closer", closer.getId());
            document.put("close-time", Date.from(Instant.now(Clock.system(ZoneId.of("Europe/Budapest")))));
            document.put("close-reason", reason);
            Document update = new Document();
            update.put("$set", document);
            collection.updateOne(search, update);

            GridFSBucket bucket = GridFSBuckets.create(database, "rivals_tickets_tickets");

            bucket.uploadFromStream(channel.getId(), stream);
            Bson query = Filters.eq("channel_id", channel.getId());
            collection.updateOne(
                    query,
                    new Document().append("$set", query),
                    new UpdateOptions().upsert(true)
            );
        });
    }

    public static void download(@NotNull String channelId) {
        Storage.mongo(database -> {
            GridFSBucket bucket = GridFSBuckets.create(database, "rivals_tickets_tickets");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bucket.downloadToStream(channelId, stream);

            try (OutputStream outputStream = new FileOutputStream(channelId + ".html")) {
                stream.writeTo(outputStream);
            } catch (Exception ignored) {}
        });
    }
}
