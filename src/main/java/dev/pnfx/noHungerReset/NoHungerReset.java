package dev.pnfx.noHungerReset;

import dev.pnfx.noHungerReset.analytics.Metrics;
import dev.pnfx.noHungerReset.listeners.PlayerDeathListener;
import dev.pnfx.noHungerReset.listeners.PlayerRespawnListener;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import dev.pnfx.noHungerReset.analytics.BStats;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class NoHungerReset extends JavaPlugin {
    private File pendingHungerFile;
    private YamlConfiguration pendingHungerConfig;
    private final Map<UUID, Integer> pendingHungerLevels = new HashMap<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        boolean enabled = getConfig().getBoolean("general.enabled");
        if(enabled){
            logDebug("Debug mode enabled");
            logDebug("Connecting to bStats...");
            Metrics metrics = new Metrics(this, BStats.getPluginId());

            logDebug("Registering death listener...");
            getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
            getServer().getPluginManager().registerEvents(new PlayerRespawnListener(this), this);
            loadPendingHungerLevels();
        }else{
            getLogger().info("Plugin is disabled in config.yml! Disabling plugin...");
        }


    }

    public @NotNull FileConfiguration getConfig() {
        return super.getConfig();
    }

    public void logDebug(String message){
        boolean debug = getConfig().getBoolean("general.debug");
        if(debug){
            getLogger().info(message);
        }
    }

    private void loadPendingHungerLevels() {
        pendingHungerFile = new File(getDataFolder(), "pendingHungerLevels.yml");
        if (!pendingHungerFile.exists()) {
            try {
                pendingHungerFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        pendingHungerConfig = YamlConfiguration.loadConfiguration(pendingHungerFile);
        pendingHungerConfig.getKeys(false).forEach(key -> {
            UUID uuid = UUID.fromString(key);
            int hungerLevel = pendingHungerConfig.getInt(key);
            pendingHungerLevels.put(uuid, hungerLevel);
        });
    }

    public void savePendingHungerLevel(UUID playerUUID, int hungerLevel) {
        pendingHungerLevels.put(playerUUID, hungerLevel);
        pendingHungerConfig.set(playerUUID.toString(), hungerLevel);
        try {
            pendingHungerConfig.save(pendingHungerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Integer getPendingHungerLevel(UUID playerUUID) {
        Integer hungerLevel = pendingHungerLevels.remove(playerUUID);
        pendingHungerConfig.set(playerUUID.toString(), null); // Remove from file
        try {
            pendingHungerConfig.save(pendingHungerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hungerLevel;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
