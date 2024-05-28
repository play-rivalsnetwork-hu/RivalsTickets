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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;
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

                staffInfoEmbed(sortByValue(userMap), event, secondDate);
            }
        });
    }

    private static void staffInfoEmbed(@NotNull HashMap<String, Integer> map, @NotNull SlashCommandInteractionEvent event, @NotNull Date date) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Config.TOPLIST_COLOR);
        builder.setTitle(Config.TOPLIST_TITLE);
        builder.setFooter(Config.TOPLIST_FOOTER.replace("$date", date.toString()));
        int[] pos = {0};

        map.forEach((key, value) -> {
            if (pos[0] > 24) return;
            pos[0]++;
            builder.addField(new MessageEmbed.Field(Config.TOPLIST_FIELD_TITLE.replace("$name", key).replace("$amount", value.toString()).replace("$position", String.valueOf(pos[0])), Config.TOPLIST_FIELD_CONTENT.replace("$name", key).replace("$amount", value.toString()).replace("$position", String.valueOf(pos[0])), Config.TOPLIST_INLINE));
        });

        event.replyEmbeds(builder.build()).queue();
    }

    // https://www.geeksforgeeks.org/sorting-a-hashmap-according-to-values/ && https://www.javacodegeeks.com/2017/09/java-8-sorting-hashmap-values-ascending-descending-order.html
    public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> hm) {
        HashMap<String, Integer> temp = hm.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e2, LinkedHashMap::new));

        return temp;
    }
}
