package hu.rivalsnetwork.rivalstickets.configuration;

import hu.rivalsnetwork.rivalstickets.Main;
import hu.rivalsnetwork.rivalstickets.utils.FileUtils;

import java.nio.file.Path;

public class Config extends AbstractConfig {
    @Key("database.address")
    public static String ADDRESS = "";
    @Key("database.database")
    public static String DATABASE = "";
    @Key("database.username")
    public static String USERNAME = "";
    @Key("database.password")
    public static String PASSWORD = "";
    @Key("embed.create.title")
    public static String CREATE_TITLE = "**Hibajegy**";
    @Key("embed.create.field.title")
    public static String CREATE_FIELD_TITLE = "**Support**";
    @Key("embed.create.field.content")
    public static String CREATE_FIELD_CONTENT = "Segítségre van szükséged? Nem probléma! Használd az alábbi reakciót egy új ticket létrehozásához, ahol munkatársaink segíteni fognak neked.";
    @Key("embed.create.footer")
    public static String CREATE_FOOTER = "RivalsNetwork";
    @Key("embed.create.color")
    public static int CREATE_COLOR = 0x32E6F7;
    @Key("embed.create.button.text")
    public static String CREATE_BUTTON_TEXT = "✅ Hibajegy létrehozása!";
    @Key("bot.guild-id")
    public static String GUILD_ID = "682191814454935558";
    @Key("messages.ticket-banned")
    public static String TICKET_BANNED = "Sikertelen interakció! Ticketbannolva vagy!";
    @Key("messages.has-open-ticket")
    public static String HAS_OPEN_TICKET = "Már van nyitva hibajegyed!";
    @Key("ticket.default-category-id")
    public static String DEFAULT_CATEGORY_ID = "1060964392906276905";
    @Key("ticket.name-pattern")
    public static String NAME_PATTERN = "hibajegy-$id";
    @Key("default-group-id")
    public static long DEFAULT_GROUP_ID = 682191814454935558L;
    @Key("embed.close.field.title")
    public static String CLOSE_TITLE = "**Hibajegy**";
    @Key("embed.close.field.content")
    public static String CLOSE_CONTENT = """
    Üdvözöllek, **<@$id>**
    A személyzet egy tagja hamarosan segít neked.
    **Kérlek, válaszd ki a kategóriát, amiben segíteni tudunk.**
    \n
    **Játékosnév: $username**
    """;
    @Key("embed.close.color")
    public static int CLOSE_COLOR = 0x0997f7;
    @Key("embed.close.button.name")
    public static String CLOSE_BUTTON_NAME = "\uD83D\uDD12 Lezárás";
    @Key("embed.assign.button.name")
    public static String ASSIGN_BUTTON_NAME = "\uD83D\uDD12 Claimelés";
    @Key("embed.select.color")
    public static int SELECT_COLOR = 0x00ff00;
    @Key("embed.select.field.title")
    public static String SELECT_TITLE = "**Kategória**";
    @Key("embed.select.field.content")
    public static String SELECT_CONTENT = "*Kérlek, válaszd ki, hogy a problémád melyik kategóriába tartozik!*";
    @Key("ticket.channel-created")
    public static String CHANNEL_CREATED = "Létrehozva a hibajegyed! <#$channelId>";
    @Key("ticket.cant-close")
    public static String CANT_CLOSE = "Csak staff zárhat le hibajegyet!";
    @Key("ticket.cant-close")
    public static String CANT_ASSIGN = "Csak staffot lehet hozzárendelni egy hibajegyhez!";
    @Key("ticket.dump.guildid")
    public static String DUMP_GUILD_ID = "1064541123021901824";
    @Key("ticket.dump.channelid")
    public static String DUMP_CHANNEL_ID = "1109131200615944223";
    @Key("embed.finish.field.title")
    public static String FINISH_TITLE = "**Hibajegy**";
    @Key("embed.finish.field.content")
    public static String FINISH_CONTENT = """
    Kérlek írd le a problémádat részletesen, hogy tudjunk segíteni!
    """;
    @Key("embed.finish.color")
    public static int FINISH_COLOR = 0x00ff00;
    @Key("settings.debug")
    public static boolean DEBUG = true;
    @Key("embed.close-dm.field.title")
    public static String CLOSE_DM_TITLE = "\uD83C\uDFAB RivalsNetwork - Hibajegy";
    @Key("embed.close-dm.field.content")
    public static String CLOSE_DM_CONTENT = """
    A hibajegyed lezárásra került $staff által!
    \n
    \n
    Lezárás indoka: $reason
    \n
    Transcript: $url
    """;
    @Key("embed.close-dm.color")
    public static int CLOSE_DM_COLOR = 0xffee00;
    @Key("embed.close-server.color")
    public static int CLOSE_SERVER_COLOR = 0x00ff00;
    @Key("embed.close-server.title")
    public static String CLOSE_SERVER_TITLE = "\uD83C\uDFAB Hibajegy bezárás";
    @Key("ticket.close-transcript-channel-id")
    public static String CLOSE_TRANSCRIPT_CHANNEL = "1109131200615944223";
    @Key("ticket.role-to-ping")
    public static String ROLE_TO_PING = "1064551010284097556";
    @Key("embed.toplist.title")
    public static String TOPLIST_TITLE = "**Bezárt hibajegy toplista**";
    @Key("embed.toplist.field.title")
    public static String TOPLIST_FIELD_TITLE = "$name";
    @Key("embed.toplist.field.content")
    public static String TOPLIST_FIELD_CONTENT = "#$position | $amount bezárt hibajegy";
    @Key("embed.toplist.color")
    public static int TOPLIST_COLOR = 0x00ff00;
    @Key("embed.toplist.inline")
    public static boolean TOPLIST_INLINE = true;
    @Key("embed.toplist.footer")
    public static String TOPLIST_FOOTER = "**Ekkortól: $date**";
    @Key("time-format")
    public static String TIME_FORMAT = "<t:$epochSeconds>";
    @Key("review-channel.id")
    public static long REVIEW_CHANNEL_ID = 1058025532266123274L;
    @Key("review-channel.emoji.id")
    public static long REVIEW_CHANNEL_EMOJI_ID = 971088003365756968L;
    @Key("review-channel.emoji.name")
    public static String REVIEW_CHANNEL_EMOJI_NAME = "yes";
    @Key("review-channel.emoji.animated")
    public static boolean REVIEW_CHANNEL_EMOJI_ANIMATED = false;
    @Key("review-channel.thread-name")
    public static String REVIEW_CHANNEL_THREAD_NAME = "Felülbírálás";
    @Key("current-open-tickets.no-category")
    public static String CURRENTLY_OPEN_TICKETS_NO_CATEGORY = "Kategória nélkül";
    @Key("current-open-tickets.embed.color")
    public static int CURRENTLY_OPEN_TICKETS_COLOR = 0x00ff00;
    @Key("current-open-tickets.title")
    public static String CURRENTLY_OPEN_TICKETS_TITLE = "Nyitott hibajegyek";
    @Key("current-open-tickets.field.title")
    public static String CURRENTLY_OPEN_TICKETS_FIELD_TITLE = "$category";
    @Key("current-open-tickets.field.content")
    public static String CURRENTLY_OPEN_TICKETS_FIELD_CONTENT = "$amount";
    @Key("current-open-tickets.footer")
    public static String CURRENTLY_OPEN_TICKETS_FOOTER = "Összesen: $amount";

    public static final Config CONFIG = new Config();

    public static void reload() {
        Path mainDir = Main.getDataFolder().toPath();

        FileUtils.extractFile(Config.class, "config.yml", mainDir, false);

        CONFIG.reload(mainDir.resolve("config.yml"), Config.class);
    }
}
