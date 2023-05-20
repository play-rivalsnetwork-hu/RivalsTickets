package hu.rivalsnetwork.rivalstickets.commands;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import hu.rivalsnetwork.rivalstickets.configuration.Config;
import hu.rivalsnetwork.rivalstickets.storage.Storage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class TicketStaffInfoCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("tickettoplist")) return;
        int time = event.getOption("time") == null ? 7 : event.getOption("time").getAsInt();
        Date date = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of("Europe/Budapest"))).getTime();
        Date secondDate = Date.from(Instant.now().minus(Duration.ofDays(time)));
        HashMap<String, Integer> userMap = new HashMap<>(50);

        Storage.mongo(database -> {
            MongoCollection<Document> collection = database.getCollection("rivals_tickets_tickets");
            Document search = new Document();
            Document doc2 = new Document();
            doc2.put("$gte", secondDate);
            doc2.put("$lte", date);
            search.put("close-time", doc2);
            FindIterable<Document> cursor = collection.find(search);
            try (final MongoCursor<Document> iterator = cursor.cursor()) {
                while (iterator.hasNext()) {
                    String key = iterator.next().getString("closer-formatted-discord-name");
                    userMap.put(key, userMap.getOrDefault(key, 0) + 1);
                }
            }

            TreeMap<String, Integer> result = userMap.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue, TreeMap::new));

            staffInfoEmbed(result, event, secondDate);
        });
    }

    private static void staffInfoEmbed(@NotNull TreeMap<String, Integer> map, SlashCommandInteractionEvent event, @NotNull Date date) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Config.TOPLIST_COLOR);
        builder.setTitle(Config.TOPLIST_TITLE);
        builder.setFooter(Config.TOPLIST_FOOTER.replace("$date", date.toString()));
        int pos = 0;

        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            pos++;
            builder.addField(new MessageEmbed.Field(Config.TOPLIST_FIELD_TITLE.replace("$name", entry.getKey()).replace("$amount", entry.getValue().toString()).replace("$position", String.valueOf(pos)), Config.TOPLIST_FIELD_CONTENT.replace("$name", entry.getKey()).replace("$amount", entry.getValue().toString()).replace("$position", String.valueOf(pos)), Config.TOPLIST_INLINE));
        }

        event.replyEmbeds(builder.build()).queue();
    }
}
