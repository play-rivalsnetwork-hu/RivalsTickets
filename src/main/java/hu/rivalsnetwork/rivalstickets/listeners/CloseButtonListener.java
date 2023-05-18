package hu.rivalsnetwork.rivalstickets.listeners;

import hu.rivalsnetwork.rivalstickets.configuration.Config;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;

public class CloseButtonListener extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (!event.getButton().getId().equals("rivalstickets_close")) return;
        if (event.getMember() == null) return;
        if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            event.reply(Config.CANT_CLOSE).setEphemeral(true).queue();
            return;
        }

        TextInput userName = TextInput.create("reason", "Megoldva", TextInputStyle.SHORT)
                .setValue("Megoldva")
                .setRequiredRange(3, 16)
                .setRequired(true)
                .build();

        Modal modal = Modal.create("ticket_close_reason", "Hibajegy lezárása")
                .addActionRow(userName)
                .build();

        event.replyModal(modal).queue();
    }
}
