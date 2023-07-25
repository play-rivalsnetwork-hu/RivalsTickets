package hu.rivalsnetwork.rivalstickets.listeners;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReviewMessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!(event.getChannel() instanceof TextChannel textChannel)) return;
        if (!event.getChannel().getId().equals("1058025532266123274")) return;

        textChannel.createThreadChannel("Felülbírálás").queue();
        event.getMessage().addReaction(Emoji.fromCustom("yes", 971088003365756968L, false)).queue();
    }
}
