package com.course.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Hands out JDBC connections. Settings come from db.properties on the classpath;
 * -D system properties (e.g. -Ddb.url=...) override them.
 */
public final class DBUtil {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = DBUtil.class.getResourceAsStream("/db.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBUtil() {
    }

    private static String get(String key) {
        return System.getProperty(key, PROPS.getProperty(key));
    }

    public static Connection getConnection() throws SQLException {
        String driver = get("db.driver");
        if (driver != null && !driver.isEmpty()) {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e) {
                throw new SQLException("JDBC driver not found: " + driver, e);
            }
        }
        return DriverManager.getConnection(get("db.url"), get("db.user"), get("db.password"));
    }
}
