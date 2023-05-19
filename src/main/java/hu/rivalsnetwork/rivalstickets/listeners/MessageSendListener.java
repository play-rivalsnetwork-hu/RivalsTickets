package hu.rivalsnetwork.rivalstickets.listeners;

import hu.rivalsnetwork.rivalstickets.Main;
import hu.rivalsnetwork.rivalstickets.configuration.Config;
import hu.rivalsnetwork.rivalstickets.storage.Executor;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

public class MessageSendListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.isWebhookMessage()) return;
        if (event.getMember() == null) return;
        if (event.getMember().getUser().isBot()) return;
        if (!Executor.isTicket(event.getChannel().asTextChannel())) return;
        if (event.getMessage().getAttachments().isEmpty()) return;
        String fmessage = event.getMessage().getContentDisplay();
        event.getMessage().delete().queue();

        for (Message.Attachment attachment : event.getMessage().getAttachments()) {
            attachment.forceClose();
            attachment.getProxy().download().thenAccept(stream -> Main.getJDA().getGuildById(Config.DUMP_GUILD_ID).getTextChannelById(Config.DUMP_CHANNEL_ID).sendFiles(FileUpload.fromData(stream, attachment.getFileName())).queue(message -> {
                for (Message.Attachment messageAttachment : message.getAttachments()) {
                    String url = messageAttachment.getProxy().getUrl();
                    event.getChannel().sendMessage(fmessage + url).queue();
                }
            }));
        }
    }
}
