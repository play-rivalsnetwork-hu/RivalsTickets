package hu.rivalsnetwork.rivalstickets.listeners;

import hu.rivalsnetwork.rivalstickets.configuration.Config;
import hu.rivalsnetwork.rivalstickets.storage.Executor;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;

public class CreateButtonListener extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (!event.getButton().getId().equals("rivalstickets_open")) return;
        if (event.getMember() == null) return;

        if (Executor.isTicketBanned(event.getUser().getId())) {
            event.reply(Config.TICKET_BANNED).setEphemeral(true).queue();
            return;
        }

        if (!Executor.doesUserExist(event.getMember())) {
            Executor.createUser(event.getMember());
        }

        if (Executor.hasOpenTicket(event.getMember())) {
            event.reply(Config.HAS_OPEN_TICKET).setEphemeral(true).queue();
            return;
        }

        String nickName = event.getMember().getNickname();

        TextInput userName = TextInput.create("username", "Játékosnév", TextInputStyle.SHORT)
                .setRequiredRange(3, 16)
                .setValue(nickName == null ? "Játékosneved" : nickName)
                .setRequired(true)
                .build();

        TextInput description = TextInput.create("ticket_description", Config.DESCRIPTION, TextInputStyle.PARAGRAPH)
                .setRequired(true)
                .build();

        Modal modal = Modal.create("ticket_open_modal", "Hibajegy készítés")
                .addActionRow(userName)
                .addActionRow(description)
                .build();

        event.replyModal(modal).queue();
    }
}
