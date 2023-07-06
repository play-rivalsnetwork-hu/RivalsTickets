package hu.rivalsnetwork.rivalstickets.listeners;

import hu.rivalsnetwork.rivalstickets.configuration.Config;
import hu.rivalsnetwork.rivalstickets.storage.Executor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class AssignButtonListener extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (!event.getButton().getId().equals("rivalstickets_assign")) return;
        if (event.getMember() == null) return;
        if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            event.reply(Config.CANT_ASSIGN).setEphemeral(true).queue();
            return;
        }

        Executor.assignTo(event.getChannel(), event.getMember());
        event.reply("Sikeresen magadhozrendelted a hibajegyet!").setEphemeral(true).queue();
    }
}
