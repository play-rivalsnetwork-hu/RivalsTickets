package hu.rivalsnetwork.rivalstickets;

import hu.rivalsnetwork.rivalstickets.commands.TicketEmbedCommand;
import hu.rivalsnetwork.rivalstickets.configuration.Config;
import hu.rivalsnetwork.rivalstickets.listeners.*;
import hu.rivalsnetwork.rivalstickets.storage.Storage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.EnumSet;
import java.util.Scanner;

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
        new Storage();

        jda.upsertCommand("ticketembed", "Ticket embed message").setGuildOnly(true).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)).queue();
        jda.addEventListener(new TicketEmbedCommand(), new CreateButtonListener(), new CreateModalListener(), new CreateStringReasonListener(), new CloseButtonListener(), new CloseModalListener());

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
                    System.out.println("Config újratöltve!");
                } else if (next.equalsIgnoreCase("stop")) {
                    System.out.println("Bot leállítása");
                    jda.shutdown();
                    System.exit(-1);
                }
            }
        }).start();
    }
}