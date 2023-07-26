package hu.rivalsnetwork.rivalstickets.commands;

import hu.rivalsnetwork.rivalstickets.Main;
import hu.rivalsnetwork.rivalstickets.configuration.Config;
import hu.rivalsnetwork.rivalstickets.storage.Executor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;

public class TicketListCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("ticketlist")) return;
        event.deferReply().queue();

        HashMap<String, Integer> openTickets = new HashMap<>();
        int i = 0;
        for (TextChannel textChannel : Main.getGuild().getTextChannels()) {
            if (Executor.isTicket(textChannel)) {
                i++;
                if (textChannel.getParentCategory() == null) {
                    openTickets.put(Config.CURRENTLY_OPEN_TICKETS_NO_CATEGORY, openTickets.getOrDefault(Config.CURRENTLY_OPEN_TICKETS_NO_CATEGORY, 0) + 1);
                    continue;
                }

                openTickets.put(textChannel.getParentCategory().getName(), openTickets.getOrDefault(textChannel.getParentCategory().getName(), 0) + 1);
            }
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Config.CURRENTLY_OPEN_TICKETS_COLOR);
        builder.setTitle(Config.CURRENTLY_OPEN_TICKETS_TITLE);
        builder.setFooter(Config.CURRENTLY_OPEN_TICKETS_FOOTER.replace("$amount", String.valueOf(i)));
        HashMap<String, Integer> sorted = TicketStaffInfoCommand.sortByValue(openTickets);

        sorted.forEach((key, value) -> {
            builder.addField(new MessageEmbed.Field(Config.CURRENTLY_OPEN_TICKETS_FIELD_TITLE.replace("$category", key).replace("$amount", String.valueOf(value)), Config.CURRENTLY_OPEN_TICKETS_FIELD_CONTENT.replace("$category", key).replace("$amount", String.valueOf(value)), true));
        });

        event.getHook().sendMessageEmbeds(builder.build()).queue();
    }
}
