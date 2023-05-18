package hu.rivalsnetwork.rivalstickets.listeners;

import hu.rivalsnetwork.rivalstickets.storage.Executor;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class CloseModalListener extends ListenerAdapter {

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (!event.getModalId().equals("ticket_close_reason")) return;

        try {
            Executor.deleteTicket(event.getMember(), event.getChannel().asTextChannel());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
