package hu.rivalsnetwork.rivalstickets.commands;

import hu.rivalsnetwork.rivalstickets.storage.Executor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;

public class TicketGetAssignedTicketsCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("tickets")) return;
        event.deferReply(true).queue();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Hozzád csatolt hibajegyek");
        builder.setColor(0x08ff41);
        List<Channel> channels = Executor.getAssignedChannels(event.getUser());
        for (Channel channel : channels) {
            builder.addField(new MessageEmbed.Field(channel.getAsMention(), "", true));
        }

        event.getHook().sendMessageEmbeds((builder.build())).queue();
    }
}
