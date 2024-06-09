package hu.rivalsnetwork.rivalstickets.commands;

import hu.rivalsnetwork.rivalstickets.configuration.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TicketEmbedCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("ticketembed")) return;

        List<Button> buttons = new ArrayList<>(1);
        buttons.add(Button.danger("rivalstickets_open", Config.CREATE_BUTTON_TEXT));
        event.getChannel().sendMessageEmbeds(mainEmbed()).setActionRow(buttons).queue();
    }

    @NotNull
    private MessageEmbed mainEmbed() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(Config.CREATE_TITLE);
        builder.setColor(Config.CREATE_COLOR);
        builder.addField(new MessageEmbed.Field(Config.CREATE_FIELD_TITLE, Config.CREATE_FIELD_CONTENT, false));
        builder.setFooter(Config.CREATE_FOOTER);

        return builder.build();
    }
}
