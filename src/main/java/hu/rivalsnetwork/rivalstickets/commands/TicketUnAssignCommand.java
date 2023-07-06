package hu.rivalsnetwork.rivalstickets.commands;

import hu.rivalsnetwork.rivalstickets.storage.Executor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class TicketUnAssignCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("unassign")) return;
        OptionMapping mapping = event.getOption("assignee");
        if (mapping == null) return;
        event.deferReply(true).queue();

        if (Executor.isTicket(event.getChannel().asTextChannel())) {
            Executor.removeAssignee(event.getChannel(), mapping.getAsMember());
            event.getHook().sendMessage("A hibajegyből sikeresen eltávolítva " + mapping.getAsMember().getAsMention()).queue();
            return;
        }

        event.getHook().sendMessage("Ez nem egy hibajegy!").queue();
    }
}
