package onl.jami.averUtils.storage.database;

import java.sql.*;

public class Driver {

    private static Connection connection = null;

    public static void initDb() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:plugins/AverUtils/database.db");
    }

    public static void close() throws SQLException {
        if (connection == null) { return; }
        connection.close();
        connection = null;
    }

    public static Connection getConnection() {
        return connection;
    }

    private static final int SCHEMA_VERSION = 3;

    public static void initTables() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys= ON;");
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS players (
                    uuid                TEXT PRIMARY KEY NOT NULL,
                    is_verified         BOOLEAN NOT NULL DEFAULT FALSE,
                    discord_id          INTEGER,
                    discord_globalname  TEXT,
                    discord_username    TEXT,
                    discord_nickname    TEXT,
                    nickname            TEXT,
                    pronouns            TEXT,
                    name_choice         TEXT NOT NULL DEFAULT 'PLAYER_NAME'
                                        CHECK(name_choice IN ('PLAYER_NAME', 'DISCORD_GLOBAL', 'DISCORD_NICK', 'NICKNAME')),
                    name_style          TEXT,
                    pvp_enabled         BOOLEAN NOT NULL DEFAULT TRUE,
                    tp_enabled          BOOLEAN NOT NULL DEFAULT TRUE,
                    ping_enabled        BOOLEAN NOT NULL DEFAULT TRUE
                )
            """);
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS homes (
                    player_uuid TEXT NOT NULL,
                    name        TEXT NOT NULL DEFAULT 'home',
                    world       TEXT NOT NULL,
                    x           INTEGER NOT NULL,
                    y           INTEGER NOT NULL,
                    z           INTEGER NOT NULL,
                    yaw         INTEGER NOT NULL,
                    pitch       INTEGER NOT NULL,
                                PRIMARY KEY (player_uuid, name))
            """);
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS warps (
                    name        TEXT PRIMARY KEY NOT NULL,
                    world       TEXT NOT NULL,
                    x           INTEGER NOT NULL,
                    y           INTEGER NOT NULL,
                    z           INTEGER NOT NULL,
                    yaw         INTEGER NOT NULL,
                    pitch       INTEGER NOT NULL
                )
            """);
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS channel_links (
                    guild_id    INTEGER NOT NULL,
                    channel_id  INTEGER NOT NULL,
                    categories  INTEGER NOT NULL DEFAULT 0,
                    webhook_url TEXT NOT NULL,
                                PRIMARY KEY (guild_id, channel_id)
                )
            """);
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS role_links (
                    role_id     INTEGER PRIMARY KEY NOT NULL,
                    lp_group    TEXT NOT NULL
                )
            """);
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS motds (
                    name        TEXT PRIMARY KEY NOT NULL,
                    message     TEXT NOT NULL,
                    is_active   BOOLEAN NOT NULL DEFAULT FALSE
                )
            """);
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS player_list_decorations (
                    name        TEXT NOT NULL,
                    location    TEXT NOT NULL
                                CHECK(location IN ('HEADER', 'FOOTER')),
                    message     TEXT NOT NULL,
                    is_active   BOOLEAN NOT NULL DEFAULT FALSE,
                                PRIMARY KEY (name, location)
                )
            """);
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS blocks (
                    player_uuid     TEXT NOT NULL REFERENCES players(uuid) ON DELETE CASCADE,
                    blocked_uuid    TEXT NOT NULL REFERENCES players(uuid) ON DELETE CASCADE,
                    categories      INTEGER NOT NULL DEFAULT 0,
                                    PRIMARY KEY (player_uuid, blocked_uuid)
                )
            """);
        }
    }

    public static void migrate() throws SQLException {
        int current = getUserVersion();
        if (current < 1) { migrateToV1(); }
        if (current < 2) { migrateToV2(); }
        if (current < 3) { migrateToV3(); }
        setSchemaVersion(SCHEMA_VERSION);
    }

    private static void migrateToV1() throws SQLException {
        boolean oldAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("""
            CREATE TABLE players_new (
                uuid                TEXT PRIMARY KEY NOT NULL,
                is_verified         BOOLEAN NOT NULL DEFAULT FALSE,
                discord_id          INTEGER,
                discord_globalname  TEXT,
                discord_username    TEXT,
                discord_nickname    TEXT,
                nickname            TEXT,
                pronouns            TEXT,
                name_choice         TEXT NOT NULL DEFAULT 'PLAYER_NAME'
                                    CHECK(name_choice IN ('PLAYER_NAME', 'DISCORD_GLOBAL', 'DISCORD_NICK', 'NICKNAME'))
            )
            """);
            st.executeUpdate("""
                INSERT INTO players_new (uuid, is_verified, discord_id, discord_globalname, discord_username, discord_nickname, nickname, name_choice)
                SELECT uuid, is_verified, discord_id, discord_globalname, discord_username, discord_nickname, nickname, CASE name_choice when 'NICK' then 'NICKNAME' ELSE name_choice END
                FROM players
            """);
            st.executeUpdate("DROP TABLE players");
            st.executeUpdate("ALTER TABLE players_new RENAME TO players");
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(oldAutoCommit);
        }
    }

    private static void migrateToV2() throws SQLException {
        boolean oldAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("ALTER TABLE players ADD COLUMN name_style TEXT");
            st.executeUpdate("ALTER TABLE players ADD COLUMN pvp_enabled BOOLEAN NOT NULL DEFAULT TRUE");
            st.executeUpdate("ALTER TABLE players ADD COLUMN tp_enabled BOOLEAN NOT NULL DEFAULT TRUE");
            connection.commit();
        } catch (SQLException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(oldAutoCommit);
        }
    }

    private static void migrateToV3() throws SQLException {
        boolean oldAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("ALTER TABLE players ADD COLUMN ping_enabled BOOLEAN NOT NULL DEFAULT TRUE");
            connection.commit();
        } catch (SQLException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(oldAutoCommit);
        }
    }

    private static int getUserVersion() throws SQLException {
        try (Statement st = connection.createStatement()) {
            ResultSet rs = st.executeQuery("PRAGMA user_version");
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static void setSchemaVersion(int version) throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("PRAGMA user_version=" + version);
        }
    }
}
