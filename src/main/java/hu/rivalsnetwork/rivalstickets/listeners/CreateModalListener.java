package hu.rivalsnetwork.rivalstickets.listeners;

import hu.rivalsnetwork.rivalstickets.Main;
import hu.rivalsnetwork.rivalstickets.configuration.Config;
import hu.rivalsnetwork.rivalstickets.storage.Executor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class CreateModalListener extends ListenerAdapter {

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (!event.getModalId().equals("ticket_username_select")) return;
        int ticketId = Executor.getNextID("rivals_tickets_counter");
        Category category = Main.getGuild().getCategoryById(Config.DEFAULT_CATEGORY_ID);

        category.createTextChannel(Config.NAME_PATTERN.replace("$id", String.valueOf(ticketId))).queue(channel -> {
            channel.getManager().putRolePermissionOverride(Config.DEFAULT_GROUP_ID, null, EnumSet.of(Permission.VIEW_CHANNEL)).queue();
            channel.getManager().putMemberPermissionOverride(event.getMember().getIdLong(), Permission.VIEW_CHANNEL.getRawValue(), Permission.MESSAGE_SEND.getRawValue()).queue();
            List<Button> list = new ArrayList<>();
            list.add(Button.primary("rivalstickets_close", Config.CLOSE_BUTTON_NAME).withStyle(ButtonStyle.DANGER));
            channel.sendMessageEmbeds(closeEmbed(event.getMember(), event.getValue("username").getAsString())).addContent(event.getMember().getAsMention()).addActionRow(list).queue();

            StringSelectMenu.Builder menu = StringSelectMenu.create("rivalstickets_reason_select");

            ConfigurationSection section = Config.CONFIG.getConfig().getConfigurationSection("categories");
            for (String key : section.getKeys(false)) {
                ConfigurationSection section1 = section.getConfigurationSection(key);
                if (section1.getBoolean("sub")) continue;

                ConfigurationSection emoji = section1.getConfigurationSection("emoji");
                Emoji emoji2;
                if (emoji.getBoolean("custom")) {
                    emoji2 = Emoji.fromCustom(emoji.getString("name"), emoji.getLong("id"), emoji.getBoolean("animated"));
                } else {
                    emoji2 = Emoji.fromUnicode(emoji.getString("unicode"));
                }

                menu.addOption(section1.getString("name"), section1.getString("id"), emoji2);
            }

            Executor.createTicket(event.getMember(), channel, ticketId, event.getValue("username").getAsString());
            channel.sendMessageEmbeds(selectEmbed()).addActionRow(menu.build()).queue();
            event.reply(Config.CHANNEL_CREATED.replace("$channelId", channel.getId())).setEphemeral(true).queue();
        });
    }

    @NotNull
    public static MessageEmbed selectEmbed() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Config.SELECT_COLOR);
        builder.addField(new MessageEmbed.Field(Config.SELECT_TITLE, Config.SELECT_CONTENT, false));
        return builder.build();
    }

    @NotNull
    private MessageEmbed closeEmbed(@NotNull Member member, @NotNull String username) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Config.CLOSE_COLOR);
        builder.addField(new MessageEmbed.Field(Config.CLOSE_TITLE, Config.CLOSE_CONTENT.replace("$id", member.getId()).replace("$username", username), false));
        return builder.build();
    }
}
