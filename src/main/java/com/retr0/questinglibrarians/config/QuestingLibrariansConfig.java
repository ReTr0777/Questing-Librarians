package com.retr0.questinglibrarians.config;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class QuestingLibrariansConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("questing-librarians-config");
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("questing_librarians_server.properties");

    // Settings with their default values
    public static boolean disableCuringDiscounts = true;
    public static boolean allowGrindstoneUnlearning = true;
    public static boolean allowTradeUpgrading = true;
    public static boolean guaranteeZombification = true;
    public static boolean curingRewardEnabled = true;
    public static int maxBooksNormal = 2;
    public static int maxBooksMaster = 3;
    public static int maxBooksCured = 4;
    public static int baseEmeraldCost = 15;
    public static int discountPerLevel = 2;
    public static int curingRewardMinLevel = 5;
    public static int curingRewardEmeraldCost = 7;

    public static void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                Properties props = new Properties();
                try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
                    props.load(in);
                    disableCuringDiscounts = Boolean.parseBoolean(props.getProperty("disableCuringDiscounts", "true"));
                    allowGrindstoneUnlearning = Boolean.parseBoolean(props.getProperty("allowGrindstoneUnlearning", "true"));
                    allowTradeUpgrading = Boolean.parseBoolean(props.getProperty("allowTradeUpgrading", "true"));
                    guaranteeZombification = Boolean.parseBoolean(props.getProperty("guaranteeZombification", "true"));
                    curingRewardEnabled = Boolean.parseBoolean(props.getProperty("curingRewardEnabled", "true"));
                    
                    maxBooksNormal = getIntOrDefault(props, "maxBooksNormal", 2);
                    maxBooksMaster = getIntOrDefault(props, "maxBooksMaster", 3);
                    maxBooksCured = getIntOrDefault(props, "maxBooksCured", 4);
                    baseEmeraldCost = getIntOrDefault(props, "baseEmeraldCost", 15);
                    discountPerLevel = getIntOrDefault(props, "discountPerLevel", 2);
                    curingRewardMinLevel = getIntOrDefault(props, "curingRewardMinLevel", 5);
                    curingRewardEmeraldCost = getIntOrDefault(props, "curingRewardEmeraldCost", 7);
                }
            } else {
                save(); // Write default file
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load Questing Librarians server config", e);
        }
    }

    private static int getIntOrDefault(Properties props, String key, int defaultValue) {
        try {
            return Integer.parseInt(props.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static void save() {
        try {
            Properties props = new Properties();
            props.setProperty("disableCuringDiscounts", String.valueOf(disableCuringDiscounts));
            props.setProperty("allowGrindstoneUnlearning", String.valueOf(allowGrindstoneUnlearning));
            props.setProperty("allowTradeUpgrading", String.valueOf(allowTradeUpgrading));
            props.setProperty("guaranteeZombification", String.valueOf(guaranteeZombification));
            props.setProperty("curingRewardEnabled", String.valueOf(curingRewardEnabled));
            props.setProperty("maxBooksNormal", String.valueOf(maxBooksNormal));
            props.setProperty("maxBooksMaster", String.valueOf(maxBooksMaster));
            props.setProperty("maxBooksCured", String.valueOf(maxBooksCured));
            props.setProperty("baseEmeraldCost", String.valueOf(baseEmeraldCost));
            props.setProperty("discountPerLevel", String.valueOf(discountPerLevel));
            props.setProperty("curingRewardMinLevel", String.valueOf(curingRewardMinLevel));
            props.setProperty("curingRewardEmeraldCost", String.valueOf(curingRewardEmeraldCost));

            Files.createDirectories(CONFIG_PATH.getParent());
            try (OutputStream out = Files.newOutputStream(CONFIG_PATH)) {
                props.store(out, "Questing Librarians Server Config");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save Questing Librarians server config", e);
        }
    }
}
