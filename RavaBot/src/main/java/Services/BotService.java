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

  private GameObject getHighestRatioEnemy() {
    double maxRatio = 0.0;
    GameObject maxRatioPlayer = null;
    if (!gameState.getPlayerGameObjects().isEmpty()) {
      for (GameObject player : gameState.getPlayerGameObjects()) {
        if (player.getId() != this.bot.getId()
            && player.getSize() / BotUtil.getActualDistance(this.bot, player) > maxRatio) {
          maxRatio = player.getSize() / BotUtil.getActualDistance(this.bot, player);
          maxRatioPlayer = player;
        }
      }
    }

    return maxRatioPlayer;
  }

  private double getOrthogonalProjectionMagnitude(GameObject object1, GameObject object2) {
    // Get orthogonal projection of object2 velocity vector to object1
    int angleBetween = Math.abs(object2.currentHeading - BotUtil.getHeadingBetween(object2, object1));
    double projMagnitude = object2.getSpeed() * Math.sin(Math.toRadians(angleBetween));
    return Math.abs(projMagnitude);
  }

  private boolean isTorpedoIncoming() {
    List<GameObject> nearestWeapons = this.getNearestObjects(gameState.getGameObjects(), "WEAPONS");
    if (nearestWeapons.isEmpty() || nearestWeapons == null) {
      return false;
    }

    GameObject nearestWeapon = nearestWeapons.get(0);

    if (Math.abs(BotUtil.getHeadingBetween(nearestWeapon, bot) - nearestWeapon.currentHeading) > 90) {
      return false;
    }

    if (BotUtil.getActualDistance(this.bot, nearestWeapon) - nearestWeapon.getSpeed() < 50
        && this.getOrthogonalProjectionMagnitude(this.bot, nearestWeapon) <= this.bot.getSize()) {
      // if (getActualDistance(this.bot, nearestWeapon) - nearestWeapon.getSpeed() <
      // 100) {
      System.out.println(
          "Orthogonal Projection: " + String.valueOf(this.getOrthogonalProjectionMagnitude(this.bot, nearestWeapon)));
      System.out.println("Bot radius: " + String.valueOf(this.bot.getSize()));
      return true;
    }

    return false;
  }

  // BOT LOGIC METHODS
  // =====================================================================
  // =====================================================================
  public void computeNextPlayerAction(PlayerAction playerAction) {
    // playerAction.action = PlayerActions.FORWARD;
    // playerAction.heading = new Random().nextInt(360);

    if (!gameState.getPlayerGameObjects().isEmpty()) {

      // Teleporting
      if (this.isFiringTeleporter) {
        System.out.println("Is firing teleporter");
      }

      // Check if teleporter still exists
      if (this.isFiringTeleporter && this.getNearestObjects(gameState.getGameObjects(), "TRAVERSAL").isEmpty()
          && gameState.getWorld().getCurrentTick() - this.fireTeleporterTick > 3) {
        this.isFiringTeleporter = false;
      }

      GameObject largestSmallerEnemy = null;
      int largestSmallerEnemySize = 0;
      for (GameObject player : gameState.getPlayerGameObjects()) {
        if (player.getId() != this.bot.getId() && this.bot.getSize() - 15 > player.getSize()
            && player.getSize() > largestSmallerEnemySize) {
          largestSmallerEnemy = player;
        }
      }

      if (this.isFiringTeleporter && !gameState.getGameObjects().isEmpty() && largestSmallerEnemy != null) {
        for (GameObject teleporter : gameState.getGameObjects()) {
          if (teleporter.getGameObjectType() == ObjectTypes.TELEPORTER) {
            if (BotUtil.getActualDistance(largestSmallerEnemy, teleporter) < this.bot.getSize()
                && largestSmallerEnemy.getSize() < this.bot.getSize()) {
              playerAction.action = PlayerActions.TELEPORT;
              playerAction.heading = BotMovement.getOptimalHeading(this, this.toll);
              this.playerAction = playerAction;
              System.out.println("Teleporting..");
              this.isFiringTeleporter = false;
              return;
            }
          }
        }
      }

      if (!this.isFiringTeleporter && largestSmallerEnemy != null && this.bot.getSize() > 80
          && this.bot.getSize() > largestSmallerEnemy.getSize() && this.bot.getTeleportCount() > 0) {
        playerAction.action = PlayerActions.FIRETELEPORT;
        playerAction.heading = getHeadingBetween(largestSmallerEnemy);
        this.playerAction = playerAction;
        this.fireTeleporterTick = gameState.getWorld().getCurrentTick();
        System.out.println("Ejecting teleporter..");
        this.isFiringTeleporter = true;
        return;
      }

      // Nearest consumables
      List<GameObject> nearestConsumables = getNearestObjects(gameState.getPlayerGameObjects(), "CONSUMABLES");
      GameObject nearestConsumable;
      if (!nearestConsumables.isEmpty()) {
        nearestConsumable = nearestConsumables.get(0);
      } else {
        nearestConsumable = null;
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
      if (this.getBot().getTorpedoCount() >= 5 && this.getBot().getSize() >= 50 ) {
        boolean isOk = BotAttack.fireRandomTorpedo(this);

        if (isOk) {
          return;
        }
      }

      if (getHighestRatioEnemy() != null && this.getBot().getSize() - getHighestRatioEnemy().getSize() > 0
          && BotUtil.getActualDistance(this.getBot(), getHighestRatioEnemy()) < this.toll) {
        // Attack enemies
        playerAction = BotAttack.attackEnemy(this, getHighestRatioEnemy());
        System.out.println("Pew pew pew");
        this.playerAction = playerAction;
        return;
      }

      // Check if near or in an obstacle
      List<GameObject> nearestObstacles = this.getNearestObjects(gameState.getGameObjects(), "OBSTACLES");
      GameObject nearestObstacle;
      if (!nearestObstacles.isEmpty()) {
        nearestObstacle = nearestObstacles.get(0);
      } else {
        nearestObstacle = null;
      }

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

      // Consider other targets
      playerAction.heading = BotMovement.getOptimalHeading(this, this.toll);
      playerAction.action = PlayerActions.FORWARD;

      this.playerAction = playerAction;
    }
  }

}