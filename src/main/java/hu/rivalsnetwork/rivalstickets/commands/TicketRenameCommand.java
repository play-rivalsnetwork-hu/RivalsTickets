package hu.rivalsnetwork.rivalstickets.commands;

import hu.rivalsnetwork.rivalstickets.storage.Executor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class TicketRenameCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("rename")) return;
        if (event.getMember() == null) return;

        if (Executor.isTicket(event.getChannel().asTextChannel())) {
            event.getChannel().asTextChannel().getManager().setName(event.getOption("name") == null ? event.getMember().getEffectiveName() : event.getOption("name").getAsString()).queue();
            event.reply("Átnevezve!").setEphemeral(true).queue();
        }
    }
}
