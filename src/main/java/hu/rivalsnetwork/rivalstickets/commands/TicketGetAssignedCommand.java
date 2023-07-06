package hu.rivalsnetwork.rivalstickets.commands;

import hu.rivalsnetwork.rivalstickets.storage.Executor;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;

public class TicketGetAssignedCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("getassignee")) return;
        event.deferReply(true).queue();

        if (Executor.isTicket(event.getChannel().asTextChannel())) {
            List<Member> assignees = Executor.getAssignedTo(event.getChannel());
            if (assignees.isEmpty()) {
                event.getHook().sendMessage("Ez a hibajegy nincs senkihez hozzácsatolva!").queue();
                return;
            }
            List<String> mentions = assignees.stream().map(IMentionable::getAsMention).toList();

            event.getHook().sendMessage("Ez a hibajegy a következő csapattagokhoz van hozzácsatolva: " + String.join(", ", mentions)).queue();
            return;
        }
        event.getHook().sendMessage("Ez nem egy hibajegy!").queue();
    }
}
