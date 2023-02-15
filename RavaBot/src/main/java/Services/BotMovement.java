package Services;

import Models.*;
import Enums.*;

import java.util.*;
import java.util.stream.*;

public class BotMovement {
  // PUBLIC METHOD
  public static int avoidThreatHeading(Bot player, List<GameObject> objectList, GameObject threat, int minDegree,
      int maxDegree) {
    if (!objectList.isEmpty()) {
      List<GameObject> resources = objectList
          .stream().filter(item -> (item.getGameObjectType() == ObjectTypes.FOOD ||
              item.getGameObjectType() == ObjectTypes.SUPERFOOD ||
              item.getGameObjectType() == ObjectTypes.SUPERNOVA_PICKUP ||
              (item.getGameObjectType() == ObjectTypes.PLAYER && item.getSize() < player.getBot().getSize()
                  && Math.abs(item.getSize() - player.getBot().getSize()) > 30)))
          .sorted(Comparator
              .comparing(item -> BotUtil.getDistanceBetween(player.getBot(), item)))
          .collect(Collectors.toList());

      GameObject nearestTarget;

      if (resources.isEmpty()) {
        // Getting away with no resource left
        System.out.println("Running pointlessly");
        return BotUtil.toDegrees(Math.atan2(threat.getPosition().getY() - player.getBot().getPosition().getY(),
            threat.getPosition().getX() - player.getBot().getPosition().getX()));
      } else {
        // Getting away while gathering resources
        // Consider distances
        double distanceToThreat = BotUtil.getActualDistance(player.getBot(), threat);
        nearestTarget = resources.get(0);
        double distanceToNearestTarget = BotUtil.getActualDistance(player.getBot(), nearestTarget);
        double potentialThreatRadius = threat.getSize() + threat.getSpeed();

        if (distanceToThreat > potentialThreatRadius
            && distanceToNearestTarget < BotUtil.getActualDistance(threat, nearestTarget) + potentialThreatRadius) {
          System.out.println("Nom nom");
          return player.getHeadingBetween(nearestTarget);
        } else {
          List<GameObject> resourcesBehind = resources
              .stream()
              .filter(item -> player.getHeadingBetween(item) > (player.getHeadingBetween(threat) + maxDegree) % 360 ||
                  player.getHeadingBetween(item) < (player.getHeadingBetween(threat) - minDegree) % 360)
              .sorted(Comparator
                  .comparing(item -> BotUtil.getDistanceBetween(player.getBot(), item)))
              .collect(Collectors.toList());
          nearestTarget = resourcesBehind.get(0);

          if (nearestTarget != null) {
            System.out.println("Runnn");
            return player.getHeadingBetween(nearestTarget);
          } else {
            System.out.println("Yolo");
            return player.getHeadingBetween(resources.get(0));
          }
        }
      }
    } else {
      return player.getHeadingBetween(player.getCenterPoint());
    }
  }

  public static int getOptimalHeading(Bot player, int toll) {
    // Get all objects
    List<GameObject> allObjects = player.getGameState().getPlayerGameObjects();
    allObjects.addAll(player.getGameState().getGameObjects());

    // Get the nearest consumables
    List<GameObject> nearestConsumables = player.getNearestObjects(player.getGameState().getGameObjects(),
        "CONSUMABLES");
    GameObject nearestConsumable;
    if (!nearestConsumables.isEmpty()) {
      nearestConsumable = nearestConsumables.get(0);
    } else {
      nearestConsumable = null;
    }

    // Get the nearest enemy
    List<GameObject> nearestEnemies = player.getNearestObjects(player.getGameState().getPlayerGameObjects(), "ENEMY");
    GameObject nearestEnemy;
    if (!nearestEnemies.isEmpty()) {
      nearestEnemy = nearestEnemies.get(0);
    } else {
      nearestEnemy = null;
    }

    // Consider nearest enemy bot
    if (nearestEnemy != null) {
      // If enemy is bigger
      if (nearestEnemy.getSize() >= player.getBot().getSize()) {
        return BotMovement.avoidThreatHeading(player, allObjects, nearestEnemy, 45, 45);
      }

      // If enemy is significantly smaller
      if (Math.abs(nearestEnemy.getSize() - player.getBot().getSize()) > 30
          && BotUtil.getActualDistance(player.getBot(), nearestEnemy) < toll) {
        System.out.println("Kejar bot nih");
        return player.getHeadingBetween(nearestEnemy);
      }
    }

    // If there are no enemy of concern, consider consumables
    if (nearestConsumable != null) {
      if (Math.abs(player.getHeadingBetween(nearestConsumable) - player.getBot().currentHeading) > 165) {
        System.out.println("No bolak balik..");
        return player.getBot().currentHeading;
      }

      return player.getHeadingBetween(nearestConsumable);
    }

    System.out.println("Nothing of concern..");
    return player.getHeadingBetween(player.getCenterPoint());
  }
}
