package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService extends Bot {
  // BOT LOGIC METHODS
  // =====================================================================
  // =====================================================================
  public void computeNextPlayerAction(PlayerAction playerAction) {
    playerAction.action = PlayerActions.FORWARD;
    playerAction.heading = new Random().nextInt(360);

    if (!gameState.getPlayerGameObjects().isEmpty()) {
      // Nearest enemy
      List<GameObject> nearestEnemies = getNearestObjects(gameState.getPlayerGameObjects(), "ENEMY");
      GameObject nearestEnemy;
      if (!nearestEnemies.isEmpty()) {
        nearestEnemy = nearestEnemies.get(0);
      } else {
        nearestEnemy = null;
      }

      // Check if out of bounds
      if (BotUtil.getDistanceBetween(centerPoint, bot) + (1.75 * this.bot.getSize()) + 50 > this.gameState.getWorld()
          .getRadius()) {
        playerAction.heading = getHeadingBetween(centerPoint);
        System.out.println("Whoops.. to center");
      }

      // Check if near or in an obstacle
      List<GameObject> nearestObstacles = getNearestObjects(gameState.getGameObjects(), "OBSTACLES");
      GameObject nearestObstacle;
      if (!nearestObstacles.isEmpty()) {
        nearestObstacle = nearestObstacles.get(0);
      } else {
        nearestObstacle = null;
      }

      if (nearestObstacle != null
          && BotUtil.getDistanceBetween(nearestObstacle,
              this.bot) < (1.75 * this.bot.getSize() + nearestObstacle.getSize())) {
        // Get nearest consumable outside of obstacle
        GameObject nearestObjectOutside = getNearestObjects(gameState.getGameObjects(), "CONSUMABLES")
            .stream()
            .filter(item -> BotUtil.getDistanceBetween(item,
                nearestObstacle) > (1.75 * this.bot.getSize() + nearestObstacle.getSize()))
            .sorted(Comparator.comparing(item -> BotUtil.getDistanceBetween(this.bot, item)))
            .collect(Collectors.toList()).get(0);
        playerAction.heading = getHeadingBetween(nearestObjectOutside);
        System.out.println("Avoiding obstacles..");
      }

      // Consider enemies
      if (nearestEnemy != null && this.bot.getSize() >= 70
          && BotUtil.getActualDistance(this.bot, nearestEnemy) <= this.toll) {
        playerAction.heading = getHeadingBetween(nearestEnemy);
        playerAction.action = PlayerActions.FIRETORPEDOES;
        // BotAttack.attackEnemy(this, nearestEnemy);
        System.out.println("Pew pew pew");
      } else {
        // Consider other targets
        playerAction.heading = BotMovement.getOptimalHeading(this, this.toll);
        playerAction.action = PlayerActions.FORWARD;
      }

      this.playerAction = playerAction;
    }
  }

}