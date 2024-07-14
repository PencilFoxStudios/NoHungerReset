package dev.pnfx.noHungerReset.listeners;

import dev.pnfx.noHungerReset.NoHungerReset;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnListener implements Listener {
    private final NoHungerReset plugin;

    public PlayerRespawnListener(NoHungerReset plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Integer newHungerLevel = plugin.getPendingHungerLevel(event.getPlayer().getUniqueId());
        plugin.logDebug("Player respawn event triggered... Checking if hunger should be reset...");
        if (newHungerLevel != null) {
            plugin.logDebug("Player hunger level is pending reset. Setting hunger level of " + event.getPlayer().getName() + " to " + newHungerLevel);
            // Wait for the player to fully respawn
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                event.getPlayer().setFoodLevel(newHungerLevel);
            }, 1L);

        }
    }


}