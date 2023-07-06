package hu.rivalsnetwork.rivalstickets;

import hu.rivalsnetwork.rivalstickets.commands.TicketAssignCommand;
import hu.rivalsnetwork.rivalstickets.commands.TicketEmbedCommand;
import hu.rivalsnetwork.rivalstickets.commands.TicketGetAssignedCommand;
import hu.rivalsnetwork.rivalstickets.commands.TicketGetAssignedTicketsCommand;
import hu.rivalsnetwork.rivalstickets.commands.TicketRenameCommand;
import hu.rivalsnetwork.rivalstickets.commands.TicketStaffInfoCommand;
import hu.rivalsnetwork.rivalstickets.commands.TicketUnAssignCommand;
import hu.rivalsnetwork.rivalstickets.configuration.Config;
import hu.rivalsnetwork.rivalstickets.listeners.*;
import hu.rivalsnetwork.rivalstickets.storage.Storage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.EnumSet;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static File dataFolder;
    private static JDA jda;

    public static void main(String[] args) throws Exception {
        jda = JDABuilder.createDefault(System.getProperty("RivalsBot.Token"))
                .enableIntents(EnumSet.allOf(GatewayIntent.class))
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build().awaitReady();

        startListening();
        dataFolder = new File("tickets/");
        if (!dataFolder.exists()) dataFolder.mkdirs();

        Config.reload();
        Storage.reload();

        Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
        jda.upsertCommand("ticketembed", "Ticket embed message").setGuildOnly(true).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)).queue();
        jda.upsertCommand("tickettoplist", "Staff info command").setGuildOnly(true).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)).addOption(OptionType.INTEGER, "time", "Idő").queue();
//        jda.upsertCommand("rename", "Rename ticket command").addOption(OptionType.STRING, "name", "A hibajegy új neve").setGuildOnly(true).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE)).queue();
        jda.upsertCommand("assign", "Assign a staff to a ticket").addOption(OptionType.USER, "assignee", "A hozzácsatolt staff").setGuildOnly(true).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE)).queue();
        jda.upsertCommand("unassign", "Remove an assignee from the ticket").addOption(OptionType.USER, "assignee", "A staff akit elvegyünk").setGuildOnly(true).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE)).queue();
        jda.upsertCommand("getassignee", "Get the currently assigned person").setGuildOnly(true).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE)).queue();
        jda.upsertCommand("tickets", "Get the tickets that are assigned to you").setGuildOnly(true).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE)).queue();
        jda.addEventListener(new TicketEmbedCommand(), new CreateButtonListener(), new CreateModalListener(), new CreateStringReasonListener(), new CloseButtonListener(), new CloseModalListener(), new MessageSendListener(), /*new TicketRenameCommand(),*/ new TicketStaffInfoCommand(), new TicketAssignCommand(), new TicketGetAssignedTicketsCommand(), new TicketGetAssignedCommand(), new TicketUnAssignCommand(), new AssignButtonListener());
    }

    public static File getDataFolder() {
        return dataFolder;
    }

    public static JDA getJDA() {
        return jda;
    }

    @NotNull
    @SuppressWarnings("DataFlowIssue")
    public static Guild getGuild() {
        return jda.getGuildById(Config.GUILD_ID);
    }

    private static void startListening() {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()) {
                String next = scanner.next();

                if (next.equalsIgnoreCase("reload")) {
                    Config.reload();
                    Storage.reload();
                    System.out.println("Config & adatbázis újratöltve!");
                } else if (next.equalsIgnoreCase("stop")) {
                    System.out.println("Bot leállítása");
                    jda.shutdown();
                    System.exit(-1);
                }
            }
        }).start();
    }
}