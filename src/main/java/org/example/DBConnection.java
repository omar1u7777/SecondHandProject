package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Hanterar databasanslutningar utan att återanvända stängda anslutningar.
 */
public class DBConnection {
    private static final Logger logger = Logger.getLogger(DBConnection.class.getName());
    private static final String url;
    private static final String username;
    private static final String password;

    static {
        Properties props = new Properties();
        try (InputStream input = DBConnection.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (input == null) {
                logger.severe("db.properties saknas i resources. Kontrollera classpath.");
                throw new RuntimeException("Kan inte hitta db.properties. Se till att filen finns i src/main/resources.");
            }
            props.load(input);
        } catch (IOException e) {
            logger.severe("Fel vid inläsning av db.properties: " + e.getMessage());
            throw new ExceptionInInitializerError(e);
        }

        url = props.getProperty("db.url");
        username = props.getProperty("db.username");
        password = props.getProperty("db.password");

        if (url == null || username == null || password == null) {
            String msg = String.format(
                    "Saknade property-värden i db.properties: url=%s, username=%s, password=%s", url, username, password);
            logger.severe(msg);
            throw new ExceptionInInitializerError(msg);
        }
    }

    /**
     * Skapar och returnerar en ny databasanslutning vid varje anrop.
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(url, username, password);
        logger.info("Ny databasanslutning etablerad: " + url);
        return conn;
    }
}
