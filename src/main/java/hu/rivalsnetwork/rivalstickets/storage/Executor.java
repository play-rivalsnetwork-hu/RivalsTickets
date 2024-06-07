package hu.rivalsnetwork.rivalstickets.storage;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.UpdateOptions;
import hu.rivalsnetwork.rivalstickets.Main;
import hu.rivalsnetwork.rivalstickets.configuration.Config;
import me.ryzeon.transcripts.DiscordHtmlTranscripts;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.simpleyaml.configuration.ConfigurationSection;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Executor {

    public static boolean isTicketBanned(@NotNull String id) {
        AtomicBoolean ticketBanned = new AtomicBoolean();

        Storage.mongo(database -> {
            MongoCollection<Document> collection = database.getCollection("rivals_tickets_users");
            Document find = new Document();
            find.put("discord_id", id);
            FindIterable<Document> cursor = collection.find(find);
            try (final MongoCursor<Document> iterator = cursor.cursor()) {
                if (iterator.hasNext()) {
                    ticketBanned.set(iterator.next().getBoolean("ticket_banned"));
                }
            }
        });

        return ticketBanned.get();
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
            Main.getGuild().getTextChannelById(Config.CLOSE_TRANSCRIPT_CHANNEL).sendMessageEmbeds(getTicketByChannelID(closer, channel, reason)).queue();
            String userId = getMemberIdByChannel(channel);
            channel.delete().queue();

            if (userId == null) return;
            User user = Main.getJDA().getUserById(userId);
            if (user != null) {
                user.openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessageEmbeds(closeEmbed(closer, reason, channel))).queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            System.getLogger("RivalsTickets").log(System.Logger.Level.ERROR, "There was an issue while getting inputstream for transcript!");
        }
    }

    @NotNull
    public static MessageEmbed getTicketByChannelID(Member closer, TextChannel channel, String reason) {
        EmbedBuilder builder = new EmbedBuilder();
        Storage.mongo(database -> {
            MongoCollection<Document> collection = database.getCollection("rivals_tickets_tickets");
            Document search = new Document();
            search.put("channel_id", channel.getId());
            FindIterable<Document> cursor = collection.find(search);
            try (final MongoCursor<Document> iterator = cursor.cursor()) {
                if (iterator.hasNext()) {
                    Document next = iterator.next();

                    builder.setColor(Config.CLOSE_SERVER_COLOR);
                    builder.setTitle(Config.CLOSE_SERVER_TITLE);
                    ConfigurationSection section = Config.CONFIG.getConfig().getConfigurationSection("embed.close-server.fields");

                    for (String key : section.getKeys(false)) {
                        ConfigurationSection subSection = section.getConfigurationSection(key);
                        builder.addField(new MessageEmbed.Field(subSection.getString("title"), subSection.getString("content")
                                .replace("$reason", reason)
                                .replace("$closer", closer.getUser().getName())
                                .replace("$name", channel.getName())
                                .replace("$category", channel.getParentCategory() == null ? "---" : channel.getParentCategory().getName())
                                .replace("$channelId", channel.getId())
                                .replace("$url", Config.TRANSCRIPT_DNS + next.getString("uuid"))
                                .replace("$playerName", next.getString("username"))
                                .replace("$opener", next.getString("owner-formatted-discord-name"))
                                .replace("$open", Config.TIME_FORMAT.replace ("$epochSeconds", String.valueOf(((Date) next.get("open-time")).toInstant().getEpochSecond())))
                                .replace("$close", Config.TIME_FORMAT.replace ("$epochSeconds", String.valueOf(((Date) next.get("close-time")).toInstant().getEpochSecond())))
                                .replace("$id", next.getInteger("_id").toString()), subSection.getBoolean("inline")));
                    }
                }
            }
        });

        return builder.build();
    }

    public static String getMemberIdByChannel(@NotNull TextChannel channel) {
        AtomicReference<String> memberID = new AtomicReference<>();
        Storage.mongo(database -> {
            MongoCollection<Document> collection = database.getCollection("rivals_tickets_tickets");
            Document search = new Document();
            search.put("channel_id", channel.getId());
            FindIterable<Document> cursor = collection.find(search);
            try (final MongoCursor<Document> iterator = cursor.cursor()) {
                if (iterator.hasNext()) {
                    memberID.set(iterator.next().getString("owner"));
                }
            }
        });

        return memberID.get();
    }

    public static boolean isTicket(@NotNull TextChannel channel) {
        AtomicBoolean ticket = new AtomicBoolean();
        Storage.mongo(database -> {
            MongoCollection<Document> collection = database.getCollection("rivals_tickets_tickets");
            Document search = new Document();
            search.put("channel_id", channel.getId());
            FindIterable<Document> cursor = collection.find(search);
            try (final MongoCursor<Document> iterator = cursor.cursor()) {
                if (iterator.hasNext()) {
                    ticket.set(true);
                }
            }
        });

        return ticket.get();
    }

    public static boolean hasOpenTicket(@NotNull Member member) {
        AtomicBoolean hasTicket = new AtomicBoolean();
        Storage.mongo(database -> {
            MongoCollection<Document> collection = database.getCollection("rivals_tickets_tickets");
            Document find = new Document();
            find.put("owner", member.getId());
            FindIterable<Document> cursor = collection.find(find);
            try (final MongoCursor<Document> iterator = cursor.cursor()) {
                while (iterator.hasNext()) {
                    Document next = iterator.next();
                    if (next.getBoolean("closed")) continue;
                    TextChannel channel = Main.getGuild().getTextChannelById(next.getString("channel_id"));
                    if (channel == null) continue;

                    hasTicket.set(true);
                    break;
                }
            }
        });

        return hasTicket.get();
    }

    public static boolean doesUserExist(@NotNull Member member) {
        AtomicBoolean exists = new AtomicBoolean();

        Storage.mongo(database -> {
            MongoCollection<Document> collection = database.getCollection("rivals_tickets_users");
            Document find = new Document();
            find.put("discord_id", member.getId());
            FindIterable<Document> cursor = collection.find(find);
            try (final MongoCursor<Document> iterator = cursor.cursor()) {
                if (iterator.hasNext()) {
                    exists.set(true);
                }
            }
        });

        return exists.get();
    }

    public static int getNextID(String collectionName) {
        AtomicInteger id = new AtomicInteger();
        Storage.mongo(database -> {
            MongoCollection<Document> collection = database.getCollection(collectionName);
            Document find = new Document();
            find.put("_id", "userid");
            Document update = new Document();
            update.put("$inc", new Document("seq", 1));
            Document object = collection.findOneAndUpdate(find, update, new FindOneAndUpdateOptions().upsert(true));
            id.set(object.getInteger("seq"));
        });

        return id.get();
    }

    public static void createTicket(@NotNull Member member, TextChannel channel, int id, @NotNull String userName) {
        Storage.mongo(database -> {
            MongoCollection<Document> collection = database.getCollection("rivals_tickets_tickets");
            Document document = new Document();
            document.put("channel_id", channel.getId());
            document.put("owner", member.getId());
            document.put("owner-formatted-discord-name", member.getUser().getName());
            document.put("username", userName);
            document.put("open-time", Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of("Europe/Budapest"))).getTime());
            document.put("_id", id);
            document.put("closed", false);
            document.put("closer", "");
            document.put("close-time", "");
            document.put("close-reason", "");
            document.put("uuid", UUID.randomUUID().toString());

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
            document.put("close-time", Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of("Europe/Budapest"))).getTime());
            document.put("close-reason", reason);
            document.put("closer-formatted-discord-name", closer.getUser().getName());
            Document update = new Document();
            update.put("$set", document);
            collection.updateOne(search, update);

            GridFSBucket bucket = GridFSBuckets.create(database, "rivals_tickets_transcripts");

            bucket.uploadFromStream(channel.getId(), stream);
            Bson query = Filters.eq("channel_id", channel.getId());
            collection.updateOne(
                    query,
                    new Document().append("$set", query),
                    new UpdateOptions().upsert(true)
            );

            if (Config.DEBUG) {
                download(channel.getId());
            }
        });
    }

    public static void download(@NotNull String channelId) {
        Storage.mongo(database -> {
            GridFSBucket bucket = GridFSBuckets.create(database, "rivals_tickets_transcripts");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bucket.downloadToStream(channelId, stream);

            try (OutputStream outputStream = new FileOutputStream(channelId + ".html")) {
                stream.writeTo(outputStream);
            } catch (Exception ignored) {}
        });
    }

    public static String getURL(TextChannel channel) {
        AtomicReference<String> url = new AtomicReference<>();
        Storage.mongo(database -> {
            MongoCollection<Document> collection = database.getCollection("rivals_tickets_tickets");
            Document search = new Document();
            search.put("channel_id", channel.getId());
            FindIterable<Document> cursor = collection.find(search);
            try (final MongoCursor<Document> iterator = cursor.cursor()) {
                if (iterator.hasNext()) {
                    url.set("[A transcript megtekintéséhez kattints ide!](" + Config.TRANSCRIPT_DNS + iterator.next().getString("uuid") + ")");
                }
            }
        });

        return url.get();
    }

    public static void assignTo(@NotNull Channel channel, @Nullable Member member) {
        if (member == null) return;
        List<Member> assignees = getAssignedTo(channel);
        assignees.add(member);
        List<String> assigneeIds = assignees.stream().map(ISnowflake::getId).toList();

        Storage.mongo(database -> {
            MongoCollection<Document> collection = database.getCollection("rivals_tickets_tickets");
            Document filter = new Document();
            filter.put("channel_id", channel.getId());
            Document assign = new Document();
            assign.put("assignees", assigneeIds);
            Document update = new Document();
            update.put("$set", assign);
            collection.updateOne(filter, update, new UpdateOptions().upsert(true));
        });
    }

    public static void removeAssignee(@NotNull Channel channel, @Nullable Member member) {
        if (member == null) return;
        List<Member> assignees = getAssignedTo(channel);
        assignees.remove(member);
        List<String> assigneeIds = assignees.stream().map(ISnowflake::getId).toList();

        Storage.mongo(database -> {
            MongoCollection<Document> collection = database.getCollection("rivals_tickets_tickets");
            Document filter = new Document();
            filter.put("channel_id", channel.getId());
            Document assign = new Document();
            assign.put("assignees", assigneeIds);
            Document update = new Document();
            update.put("$set", assign);
            collection.updateOne(filter, update, new UpdateOptions().upsert(true));
        });
    }

    public static List<Member> getAssignedTo(@NotNull Channel channel) {
        List<Member> assignees = new ArrayList<>();
        Storage.mongo(database -> {
            MongoCollection<Document> collection = database.getCollection("rivals_tickets_tickets");
            Document filter = new Document();
            filter.put("channel_id", channel.getId());
            FindIterable<Document> cursor = collection.find(filter);
            try (final MongoCursor<Document> iterator = cursor.cursor()) {
                if (iterator.hasNext()) {
                    List<String> assigneeIds = iterator.next().getList("assignees", String.class);
                    if (assigneeIds == null) return;
                    for (String assignee : assigneeIds) {
                        assignees.add(Main.getGuild().getMemberById(assignee));
                    }
                }
            }
        });

        return assignees;
    }

    public static List<Channel> getAssignedChannels(@NotNull User user) {
        List<Channel> channels = new ArrayList<>();
        Storage.mongo(database -> {
            MongoCollection<Document> collection = database.getCollection("rivals_tickets_tickets");
            FindIterable<Document> cursor = collection.find();
            try (final MongoCursor<Document> iterator = cursor.cursor()) {
                while (iterator.hasNext()) {
                    Document next = iterator.next();
                    if (next.getBoolean("closed")) continue;
                    Channel channel = Main.getGuild().getTextChannelById(next.getString("channel_id"));
                    if (channel == null) continue;
                    List<String> assignees = next.getList("assignees", String.class);
                    if (assignees == null) continue;
                    if (!assignees.contains(user.getId())) continue;

                    channels.add(channel);
                }
            }
        });

        return channels;
    }

    @NotNull
    private static MessageEmbed closeEmbed(@NotNull Member closer, @NotNull String reason, @NotNull TextChannel channel) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Config.CLOSE_DM_COLOR);
        builder.addField(new MessageEmbed.Field(Config.CLOSE_DM_TITLE, Config.CLOSE_DM_CONTENT.replace("$url", getURL(channel)).replace("$reason", reason).replace("$staff", closer.getUser().getName()), false));

        return builder.build();
    }
}
