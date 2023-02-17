package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService extends Bot {
  public BotService() {
    this.playerAction = new PlayerAction();
    this.gameState = new GameState();
  }

  

  // BOT LOGIC METHODS
  // =====================================================================
  // =====================================================================
  public void computeNextPlayerAction(PlayerAction playerAction) {

    // Check incoming weapons and consider using shield
    if (this.bot.getSize() >= 80 && BotDefense.isTorpedoIncoming(this, this.gameState) && !isShieldActive()) {
      this.playerAction = BotDefense.activateShield(playerAction, this, this.gameState);
      return;
    }

    // Get the largest smaller enemy to target for teleporting
    GameObject largestSmallerEnemy = null;
    int largestSmallerEnemySize = 0;
    for (GameObject player : gameState.getPlayerGameObjects()) {
      if (player.getId() != this.bot.getId() && this.bot.getSize() - 15 > player.getSize()
          && player.getSize() > largestSmallerEnemySize) {
        largestSmallerEnemy = player;
        largestSmallerEnemySize = player.getSize();
      }
    }

    // Nearest enemy
    List<GameObject> nearestEnemies = getNearestObjects(gameState.getPlayerGameObjects(), "ENEMY");
    GameObject nearestEnemy;
    if (!nearestEnemies.isEmpty()) {
      nearestEnemy = nearestEnemies.get(0);
    } else {
      nearestEnemy = null;
    }

    // Nearest consumables
    List<GameObject> nearestConsumables = getNearestObjects(gameState.getPlayerGameObjects(), "CONSUMABLES");
    GameObject nearestConsumable;
    if (!nearestConsumables.isEmpty()) {
      nearestConsumable = nearestConsumables.get(0);
    } else {
      nearestConsumable = null;
    }

    // Nearest obstacles
    List<GameObject> nearestObstacles = this.getNearestObjects(gameState.getGameObjects(), "OBSTACLES");
    GameObject nearestObstacle;
    if (!nearestObstacles.isEmpty()) {
      nearestObstacle = nearestObstacles.get(0);
    } else {
      nearestObstacle = null;
    }

    if (!gameState.getPlayerGameObjects().isEmpty()) {
      // Checks if the bot is in a gas cloud or asteroid field
      if (BotMovement.checkObstacle(this)) {
        return;
      }

      // Teleport logger
      if (this.isFiringTeleporter) {
        System.out.println("Is firing teleporter");
      }

      // Check if teleporter still exists
      if (this.isFiringTeleporter && this.getNearestObjects(gameState.getGameObjects(), "TRAVERSAL").isEmpty()
          && gameState.getWorld().getCurrentTick() - this.fireTeleporterTick > 3) {
        this.isFiringTeleporter = false;
      }

      // Teleporting
      if (this.isFiringTeleporter && !gameState.getGameObjects().isEmpty() && largestSmallerEnemy != null) {
        for (GameObject teleporter : gameState.getGameObjects()) {
          if (teleporter.getGameObjectType() == ObjectTypes.TELEPORTER) {
            if (BotUtil.getActualDistance(largestSmallerEnemy, teleporter) < this.bot.getSize()
                && largestSmallerEnemy.getSize() < this.bot.getSize()) {
              playerAction.action = PlayerActions.TELEPORT;
              playerAction.heading = BotMovement.getOptimalHeading(this);
              this.playerAction = playerAction;
              System.out.println("Teleporting..");
              this.isFiringTeleporter = false;
              return;
            }
          }
        }
      }

      // Eject teleporter
      if (!this.isFiringTeleporter && largestSmallerEnemy != null && this.bot.getSize() > 60
          && this.bot.getSize() > largestSmallerEnemy.getSize() && this.bot.getTeleportCount() > 0) {
        playerAction.action = PlayerActions.FIRETELEPORT;
        playerAction.heading = getHeadingBetween(largestSmallerEnemy);
        this.playerAction = playerAction;
        this.fireTeleporterTick = gameState.getWorld().getCurrentTick();
        System.out.println("Ejecting teleporter..");
        this.isFiringTeleporter = true;
        return;
      }

      // Check if out of bounds
      if (BotUtil.getDistanceBetween(centerPoint, bot) + (1.75 * this.bot.getSize()) + 50 > this.gameState.getWorld()
          .getRadius()) {
        playerAction.action = PlayerActions.FORWARD;
        if (nearestConsumable != null) {
          playerAction.heading = this.getHeadingBetween(nearestConsumable);
          System.out.println("Whoops.. nom nom");
        } else {
          playerAction.heading = this.getHeadingBetween(centerPoint);
          System.out.println("Whoops.. to center");
        }

        this.playerAction = playerAction;
        return;
      }

      // Consider enemies
      if (this.getBot().getTorpedoCount() >= 5 && this.getBot().getSize() >= 50) {
        boolean isOk = BotAttack.fireRandomTorpedo(this);

        if (isOk) {
          return;
        }
      }

      // Attack or defend
      if (nearestEnemy != null
          && BotUtil.getActualDistance(this.getBot(), nearestEnemy) < this.getBot().getSize() * 5) {
        if (this.getBot().getSize() - nearestEnemy.getSize() > 0) {
          // Attack enemies
          playerAction = BotAttack.attackEnemy(this, nearestEnemy);
          System.out.println("Pew pew pew");
          this.playerAction = playerAction;
          return;
        } else {
          // Defense mode
          playerAction = BotDefense.avoidThreatHeading(this, nearestEnemy);
          System.out.println("cabut cabut cabut");
          this.playerAction = playerAction;
          return;
        }
      }

      // Check if near or in an obstacle
      if (nearestObstacle != null && BotUtil.getDistanceBetween(nearestObstacle,
          this.bot) < (1.75 * this.bot.getSize() + nearestObstacle.getSize())) {
        // Get nearest consumable outside of obstacle
        List<GameObject> nearestObjectOutside = this.getNearestObjects(gameState.getGameObjects(), "CONSUMABLES")
            .stream()
            .filter(item -> BotUtil.getDistanceBetween(item,
                nearestObstacle) > (1.75 * this.bot.getSize() + nearestObstacle.getSize()))
            .sorted(Comparator.comparing(item -> BotUtil.getDistanceBetween(this.bot, item)))
            .collect(Collectors.toList());
        if (!nearestObjectOutside.isEmpty()) {
          playerAction.heading = getHeadingBetween(nearestObjectOutside.get(0));
          playerAction.action = PlayerActions.FORWARD;

          this.playerAction = playerAction;
          System.out.println("Avoiding obstacles..");
          return;
        }
      }

      // Base case: Farming consumables
      playerAction.heading = BotMovement.getOptimalHeading(this);
      playerAction.action = PlayerActions.FORWARD;

      this.playerAction = playerAction;
    }
  }
}