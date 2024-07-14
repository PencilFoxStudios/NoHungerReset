package dev.pnfx.noHungerReset;

import dev.pnfx.noHungerReset.listeners.PlayerDeathListener;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class NoHungerReset extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        boolean enabled = getConfig().getBoolean("general.enabled");
        if(enabled){
            logDebug("Debug mode enabled");
            logDebug("Registering death listener...");
            getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
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

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
