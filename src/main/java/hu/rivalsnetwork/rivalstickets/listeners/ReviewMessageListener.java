package hu.rivalsnetwork.rivalstickets.listeners;

import hu.rivalsnetwork.rivalstickets.configuration.Config;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReviewMessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!(event.getChannel() instanceof TextChannel textChannel)) return;
        if (event.getMessage().getAuthor().isBot()) return;
        if (event.getChannel().getIdLong() != Config.REVIEW_CHANNEL_ID) return;

        textChannel.createThreadChannel(Config.REVIEW_CHANNEL_THREAD_NAME).queue();
        event.getMessage().addReaction(Emoji.fromCustom(Config.REVIEW_CHANNEL_EMOJI_NAME, Config.REVIEW_CHANNEL_EMOJI_ID, Config.REVIEW_CHANNEL_EMOJI_ANIMATED)).queue();
    }
}
