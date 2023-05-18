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
    
    **Játékosnév: $username**
    """;
    @Key("embed.close.color")
    public static int CLOSE_COLOR = 0x0997f7;
    @Key("embed.close.button.name")
    public static String CLOSE_BUTTON_NAME = "\uD83D\uDD12 Lezárás";
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

    public static final Config CONFIG = new Config();

    public static void reload() {
        Path mainDir = Main.getDataFolder().toPath();

        FileUtils.extractFile(Config.class, "config.yml", mainDir, false);

        CONFIG.reload(mainDir.resolve("config.yml"), Config.class);
    }
}
