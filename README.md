### Are your players too lazy to get food and instead choose to die when they're hungry?
Look no further!

# <img src="https://cdn.modrinth.com/data/h6P1dqMR/847ff7e5bb5635b91f9f7274241dffb23d78a73d.png" alt="drawing" width="50"/> &nbsp; NoHungerReset

It's a fully customizable Minecraft Paper plugin that prevents player hunger from resetting to full upon death. It makes the player respawn with the hunger they tried to avoid like a coward... and that's it! Check out the example config below for all the things you can customize!

```yml
general:
  # The plugin will only run if this is set to true.
  enabled: true
  # Debug mode
  debug: false
  # The plugin will TRY to determine if player deaths were intentional or not,
  # and will only run if it believes the death could have been intentional.
  # (Example: drowning, falling, etc.)
  #
  # If false, the plugin will affect all player deaths.
  playerSuicideDetection: false
hungerRetention:
  # If a player died due to starvation, their hunger will be replenished to this percentage of their MAX hunger. (ex: 0.5 = 50%)
  # Use this if you want to avoid a death loop and be a little more forgiving. (ignored if playerSuicideDetection is true)
  percentOfMaxFedOnStarvation: 0.2
  # Otherwise, when a player dies and respawns, their hunger is replenished to this percentage of their DYING hunger. (ex: 0.5 = 50%)
  percentFedOnDeath: 0
specificPlayerLists:
  # If true, the plugin will use the exemptPlayers and affectedPlayers lists.
  useExemptPlayers: false
  useAffectedPlayers: false
  # List of players who are exempt from this plugin's effects.
  exemptPlayers:
    - "Player1"
    - "Player2"
    - "Player3"
  # List of players who are affected by this plugin's effects.
  affectedPlayers:
    - "Player4"
    - "Player5"
    - "Player6"
```