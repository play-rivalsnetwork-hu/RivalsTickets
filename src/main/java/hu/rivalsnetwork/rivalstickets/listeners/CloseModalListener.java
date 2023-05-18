package hu.rivalsnetwork.rivalstickets.listeners;

import hu.rivalsnetwork.rivalstickets.storage.Executor;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class CloseModalListener extends ListenerAdapter {

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (!event.getModalId().equals("ticket_close_reason")) return;
        if (event.getMember() == null) return;

        Executor.deleteTicket(event.getMember(), event.getChannel().asTextChannel(), event.getValue("reason").getAsString());
    }
}
