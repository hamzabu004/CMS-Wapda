package com.electricity.cms.util;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;

/**
 * Loads environment variables from a .env file at the project root.
 * Falls back to real environment variables if the .env file is absent.
 * Throws descriptive RuntimeExceptions for missing required keys.
 */
public class EnvLoader {

    private static final Dotenv dotenv;

    static {
        try {
            dotenv = Dotenv.configure()
                    .directory(".")          // project root
                    .ignoreIfMissing()       // fall back to OS env if .env absent
                    .load();
        } catch (DotenvException e) {
            throw new RuntimeException(
                    "[EnvLoader] Failed to parse .env file: " + e.getMessage(), e);
        }
    }

    private EnvLoader() {}

    /**
     * Returns the value for the given key.
     *
     * @throws RuntimeException if the key is not found in .env or OS environment
     */
    public static String get(String key) {
        String value = dotenv.get(key);
        if (value == null || value.isBlank()) {
            throw new RuntimeException(
                    "[EnvLoader] Required environment variable '" + key + "' is not set. " +
                    "Add it to your .env file or export it as an OS environment variable.");
        }
        return value;
    }

    /**
     * Returns the value for the given key, or {@code defaultValue} if not found.
     */
    public static String getOrDefault(String key, String defaultValue) {
        String value = dotenv.get(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }
}

