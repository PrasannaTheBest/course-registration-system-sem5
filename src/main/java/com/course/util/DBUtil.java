package com.course.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Hands out pooled JDBC connections (HikariCP). Settings come from db.properties on the classpath;
 * -D system properties (for example -Ddb.url=...) override them.
 *
 * <p>Calling {@code close()} on a connection returned by {@link #getConnection()} gives it back to the
 * pool, so every DAO can keep using try-with-resources unchanged.
 */
public final class DBUtil {
    private static final Properties PROPS = new Properties();
    private static volatile HikariDataSource pool;

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

    private static HikariDataSource pool() {
        HikariDataSource p = pool;
        if (p == null) {
            synchronized (DBUtil.class) {
                p = pool;
                if (p == null) {
                    HikariConfig cfg = new HikariConfig();
                    String driver = get("db.driver");
                    if (driver != null && !driver.isEmpty()) {
                        cfg.setDriverClassName(driver);
                    }
                    cfg.setJdbcUrl(get("db.url"));
                    cfg.setUsername(get("db.user"));
                    cfg.setPassword(get("db.password"));
                    String size = get("db.pool.size");
                    cfg.setMaximumPoolSize(size == null || size.isEmpty() ? 10 : Integer.parseInt(size));
                    cfg.setPoolName("course-registration-pool");
                    pool = p = new HikariDataSource(cfg);
                }
            }
        }
        return p;
    }

    public static Connection getConnection() throws SQLException {
        return pool().getConnection();
    }

    /** Closes the pool (called when the web application stops). */
    public static synchronized void shutdown() {
        if (pool != null) {
            pool.close();
            pool = null;
        }
    }
}
