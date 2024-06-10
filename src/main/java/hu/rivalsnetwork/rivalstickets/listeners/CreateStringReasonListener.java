package hu.rivalsnetwork.rivalstickets.listeners;

import hu.rivalsnetwork.rivalstickets.Main;
import hu.rivalsnetwork.rivalstickets.configuration.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class CreateStringReasonListener extends ListenerAdapter {

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        if (!event.getComponentId().equals("rivalstickets_reason_select")) return;
        if (event.getMember() == null) return;

        String option = event.getSelectedOptions().get(0).getValue();
        ConfigurationSection categories = Config.CONFIG.getConfig().getConfigurationSection("categories");
        ConfigurationSection section = categories.getConfigurationSection(option);

        if (section.getStringList("child") == null || section.getStringList("child").isEmpty()) {
            MessageCreateAction action = event.getChannel().sendMessageEmbeds(finishEmbed());
            if (!Config.ROLE_TO_PING.isBlank()) {
                action.addContent(Main.getGuild().getRoleById(Config.ROLE_TO_PING).getAsMention());
            }

            action.queue();
            event.getMessage().delete().queue();
            event.getChannel().asTextChannel().getManager().setParent(Main.getGuild().getCategoryById(section.getString("categoryid"))).queue();
            TextChannelManager manager = event.getChannel().asTextChannel().getManager();
            List<String> cantSeeGroups = section.getStringList("cant-see-groups");
            if (cantSeeGroups != null) {
                for (String id : cantSeeGroups) {
                    manager.putRolePermissionOverride(Main.getGuild().getRoleById(id).getIdLong(), null, EnumSet.of(Permission.VIEW_CHANNEL));
                }
            }

            manager.putMemberPermissionOverride(event.getUser().getIdLong(), EnumSet.of(Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL), null).queue();
            return;
        }

        StringSelectMenu.Builder menu = StringSelectMenu.create("rivalstickets_reason_select");
        for (Object child : section.getList("child", Collections.emptyList())) {
            ConfigurationSection section1 = categories.getConfigurationSection(child.toString());
            ConfigurationSection emoji = section1.getConfigurationSection("emoji");
            Emoji emoji2;
            if (emoji.getBoolean("custom")) {
                emoji2 = Emoji.fromCustom(emoji.getString("name"), emoji.getLong("id"), emoji.getBoolean("animated"));
            } else {
                emoji2 = Emoji.fromUnicode(emoji.getString("unicode"));
            }

            menu.addOption(section1.getString("name"), section1.getString("id"), emoji2);
        }

        event.getMessage().delete().queue();
        event.getChannel().sendMessageEmbeds(CreateModalListener.selectEmbed()).setActionRow(menu.build()).queue();
    }

    @NotNull
    private static MessageEmbed finishEmbed() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Config.FINISH_COLOR);
        builder.addField(new MessageEmbed.Field(Config.FINISH_TITLE, Config.FINISH_CONTENT, false));

        return builder.build();
    }
}
