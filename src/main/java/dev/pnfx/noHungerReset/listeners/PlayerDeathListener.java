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
        boolean shouldCheckSelfInflicted = plugin.getConfig().getBoolean("general.playerSuicideDetection");
        Player entity = event.getEntity();
        // Get player hunger level
        int dyingHungerLevel = entity.getFoodLevel();
        // Check if player died from self-inflicted damage
        if(shouldCheckSelfInflicted && !isSelfInflictedDeath(event)){
            plugin.logDebug("Player didn't die from self-inflicted damage. Skipping hunger reset...");
            return;
        }
        // Reset player hunger level
        int referenceHunger = (getDamageCause(event) == EntityDamageEvent.DamageCause.STARVATION) ?
                MAX_HUNGER
                :
                dyingHungerLevel;
        double percentFed = (getDamageCause(event) == EntityDamageEvent.DamageCause.STARVATION) ?
                plugin.getConfig().getDouble("hungerRetention.percentOfMaxFedOnStarvation")
                :
                plugin.getConfig().getDouble("hungerRetention.percentFedOnDeath");

        int newHungerLevel = (int) Math.ceil(referenceHunger * percentFed);
        entity.setFoodLevel(newHungerLevel);


    }
}
