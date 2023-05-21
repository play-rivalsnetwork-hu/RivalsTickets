package hu.rivalsnetwork.rivalstickets.listeners;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import hu.rivalsnetwork.rivalstickets.Main;
import hu.rivalsnetwork.rivalstickets.configuration.Config;
import hu.rivalsnetwork.rivalstickets.storage.Executor;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.restaction.WebhookAction;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MessageSendListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.isWebhookMessage()) return;
        if (event.getMember() == null) return;
        if (event.getMember().getUser().isBot()) return;
        if (!(event.getChannel() instanceof TextChannel ch)) return;
        if (!Executor.isTicket(ch)) return;
        if (event.getMessage().getAttachments().isEmpty()) return;
        String fmessage = event.getMessage().getContentDisplay();
        WebhookAction wh = ch.createWebhook(event.getAuthor().getName());
        int[] asd = {0};

        wh.queue(webhook -> {
            WebhookClient client = WebhookClient.withUrl(webhook.getUrl());
            WebhookMessageBuilder builder = new WebhookMessageBuilder();
            builder.setUsername(event.getAuthor().getName()).setAvatarUrl(event.getAuthor().getAvatarUrl());

            List<Message.Attachment> attachmentList = event.getMessage().getAttachments();
            AtomicReference<List<Message.Attachment>> secondAttachmentList = new AtomicReference<>();

            for (Message.Attachment attachment : attachmentList) {
                attachment.getProxy().download().thenAccept(stream -> {
                    int[] amount = {0};
                    Main.getJDA().getGuildById(Config.DUMP_GUILD_ID).getTextChannelById(Config.DUMP_CHANNEL_ID).sendFiles(FileUpload.fromData(stream, attachment.getFileName())).queue(message -> {
                        secondAttachmentList.set(message.getAttachments());
                        for (Message.Attachment messageAttachment : secondAttachmentList.get()) {
                            messageAttachment.getProxy().download().thenAccept(data -> {
                                amount[0]++;
                                builder.addFile(messageAttachment.getFileName(), data);

                                if (amount[0] == secondAttachmentList.get().size()) {
                                    asd[0]++;
                                }
                            }).thenAccept(webhook2 -> {
                                if (attachmentList.size() == amount[0] && asd[0] == secondAttachmentList.get().size()) {
                                    builder.setContent(fmessage);
                                    client.send(builder.build());
                                    client.close();
                                    event.getMessage().delete().queue();
                                    ch.deleteWebhookById(webhook.getId()).queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_WEBHOOK));
                                }
                            });
                        }
                    });
                });
            }
        });
    }
}
