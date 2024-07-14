package dev.pnfx.noHungerReset.listeners;

import dev.pnfx.noHungerReset.NoHungerReset;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.meta.components.FoodComponent;

import java.util.Objects;

public class PlayerDeathListener implements Listener {
//    String deathMessage = getConfig().getString("messages.deathMessage");
    private final NoHungerReset plugin;

    private final int MAX_HUNGER = 20;

    public PlayerDeathListener(NoHungerReset plugin) {
        this.plugin = plugin;
    }

    private EntityDamageEvent.DamageCause getDamageCause(PlayerDeathEvent event) {
        try {
            return Objects.requireNonNull(event.getEntity().getLastDamageCause()).getCause();
        } catch (NullPointerException e) {
            return null;
        }
    }

    private boolean isSelfInflictedDeath(PlayerDeathEvent event) {
        EntityDamageEvent.DamageCause cause = getDamageCause(event);
        return switch (cause) {
            // Cause was fall damage or drowning, aka likely self-inflicted.
            case FALL, DROWNING -> true;
            // Cause was entity attack, explosion, or sweep attack, and the killer was the player. Also likely self-inflicted.
            case ENTITY_ATTACK, ENTITY_EXPLOSION, ENTITY_SWEEP_ATTACK -> event.getEntity().getKiller() == event.getEntity();
            // Cause was likely not self-inflicted.
            case null, default -> false;
        };

    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        plugin.logDebug("Player death event triggered... Checking if hunger should be reset...");
        boolean shouldCheckSelfInflicted = plugin.getConfig().getBoolean("general.playerSuicideDetection");
        boolean shouldCheckWhitelist = plugin.getConfig().getBoolean("specificPlayerLists.useExemptPlayers");
        boolean shouldCheckBlacklist = plugin.getConfig().getBoolean("specificPlayerLists.useAffectedPlayers");
        Player entity = event.getEntity();
        // Get player hunger level
        int dyingHungerLevel = entity.getFoodLevel();
        // Check if player died from self-inflicted damage
        if(shouldCheckSelfInflicted && !isSelfInflictedDeath(event)){
            plugin.logDebug("Player didn't die from self-inflicted damage. Skipping hunger reset...");
            return;
        }

        if(shouldCheckWhitelist && plugin.getConfig().getStringList("specificPlayerLists.exemptPlayers").contains(entity.getName())){
            plugin.logDebug("Player is on exempt list. Skipping hunger reset...");
            return;
        }

        if(shouldCheckBlacklist && !plugin.getConfig().getStringList("specificPlayerLists.affectedPlayers").contains(entity.getName())){
            plugin.logDebug("Player is not on affected list. Skipping hunger reset...");
            return;
        }

        plugin.logDebug("Resetting player hunger level...");


        boolean playerStarved = (getDamageCause(event) == EntityDamageEvent.DamageCause.STARVATION);
        double percentFedOnDeath = plugin.getConfig().getDouble("hungerRetention.percentFedOnDeath");
        double percentOfMaxFedOnStarvation = plugin.getConfig().getDouble("hungerRetention.percentOfMaxFedOnStarvation");

        if(playerStarved) plugin.logDebug("Player starved to death. I will use percentOfMaxFedOnStarvation (" + percentOfMaxFedOnStarvation + ") instead of percentFedOnDeath (" + percentFedOnDeath + ").");

        // Reset player hunger level
        double referenceHunger = playerStarved ?
                MAX_HUNGER
                :
                dyingHungerLevel;
        double percentFed = playerStarved ?
                percentOfMaxFedOnStarvation
                :
                percentFedOnDeath;

        plugin.logDebug("Hunger will be replenished by " + (percentFed*100) + "% of " + referenceHunger + " hunger points.");

        int newHungerLevel = (int) Math.ceil((playerStarved?0:referenceHunger) + (referenceHunger * percentFed));


        plugin.logDebug("Setting hunger level to " + newHungerLevel + "...");
        // set food level to newHungerLevel WHEN ENTITY RESPAWNS

        plugin.savePendingHungerLevel(entity.getUniqueId(), newHungerLevel);


    }
}
