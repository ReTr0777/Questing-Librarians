package com.retr0.questinglibrarians.client.config;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class TutorialConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("questing-librarians");
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("questing_librarians.properties");
    public static boolean hasSeenTutorial = false;

    public static void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                Properties props = new Properties();
                try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
                    props.load(in);
                    hasSeenTutorial = Boolean.parseBoolean(props.getProperty("hasSeenTutorial", "false"));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load Questing Librarians tutorial config", e);
        }
    }

    public static void save() {
        try {
            Properties props = new Properties();
            props.setProperty("hasSeenTutorial", String.valueOf(hasSeenTutorial));
            try (OutputStream out = Files.newOutputStream(CONFIG_PATH)) {
                props.store(out, "Questing Librarians Client Config");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save Questing Librarians tutorial config", e);
        }
    }
}
