package Services;

import Models.*;
import Enums.*;

import java.util.*;
import java.util.stream.*;

public class BotDefense {
  // PUBLIC METHOD

  /**
   * This function controls the logic of avoiding larger enemy bot
   * 
   * @param player : Player's bot
   * @param threat : Enemy bot
   * @return A new player action object to avoid enemy
   */
  public static PlayerAction avoidThreatHeading(Bot player, GameObject threat) {
    // Minimum and maximum degree for a resource to be considered safe
    int maxDegree = 100;
    int minDegree = 100;

    // Minimum tollerable distance from enemy before the bot starts shooting
    int minDistance = 350;

    List<GameObject> resources = player.getGameState().getPlayerGameObjects()
        .stream().filter(item -> (item.getGameObjectType() == ObjectTypes.FOOD ||
            item.getGameObjectType() == ObjectTypes.SUPERFOOD ||
            item.getGameObjectType() == ObjectTypes.SUPERNOVAPICKUP ||
            (item.getGameObjectType() == ObjectTypes.PLAYER && item.getSize() < player.getBot().getSize()
                && Math.abs(item.getSize() - player.getBot().getSize()) > 30)))
        .sorted(Comparator
            .comparing(item -> BotUtil.getActualDistance(player.getBot(), item)))
        .collect(Collectors.toList());

    List<GameObject> safeResources = resources.stream()
        .filter(item -> player.getHeadingBetween(item) > (player.getHeadingBetween(threat) + maxDegree) % 360 ||
            player.getHeadingBetween(item) < (player.getHeadingBetween(threat) - minDegree) % 360)
        .sorted(Comparator.comparing(item -> BotUtil.getActualDistance(player.getBot(), item)))
        .collect(Collectors.toList());

    PlayerAction playerAction = new PlayerAction();

    if (BotUtil.getActualDistance(player.getBot(), threat) <= minDistance && player.getBot().getTorpedoCount() > 0) {
      playerAction.action = PlayerActions.FIRETORPEDOES;
      playerAction.heading = player.getHeadingBetween(threat);
      System.out.println("OOOOOOOOOOOOOOO TEMBAK DESPERATELY OOOOOOOOOOOOOOOOO");
    } else if (player.getBot().getTorpedoCount() >= 5) {
      playerAction.action = PlayerActions.FIRETORPEDOES;
      playerAction.heading = player.getHeadingBetween(threat);
      System.out.println("OOOOOOOOOOOOOO TEMBAK YG GEDE OOOOOOOOOOOOOOO");
    } else if (safeResources.isEmpty()) {
      playerAction.action = PlayerActions.FORWARD;
      playerAction.heading = (player.getHeadingBetween(threat) + 180) % 360;
      System.out.println("OOOOOOOOOOOOOOO YOLO OOOOOOOOOOOOOOOO");
    } else {
      playerAction.action = PlayerActions.FORWARD;
      playerAction.heading = player.getHeadingBetween(safeResources.get(0));
      System.out.println("OOOOOOOOOOOOOOOO KABUR OOOOOOOOOOOOOOOOO");
    }

    return playerAction;
  }

  /**
   * This function handles the usage of a shield
   * 
   * @param player : Player's bot
   * @param gameState : State of the current game
   * @return A new player action object to activate shield
   */
  public static PlayerAction activateShield(PlayerAction playerAction, Bot player, GameState gameState) {
    if (BotDefense.isTorpedoIncoming(player, gameState)) {
      System.out.println("Shield activated");
      playerAction.action = PlayerActions.ACTIVATESHIELD;
      playerAction.heading = player.getBot().currentHeading;
    }
    
    return playerAction;
  }

  public static double getOrthogonalProjectionMagnitude(GameObject object1, GameObject object2) {
    // Get orthogonal projection of object2 velocity vector to object1
    int angleBetween = Math.abs(object2.currentHeading - BotUtil.getHeadingBetween(object2, object1));
    double projMagnitude = object2.getSpeed() * Math.sin(Math.toRadians(angleBetween));
    return Math.abs(projMagnitude);
  }

  public static boolean isTorpedoIncoming(Bot player, GameState gameState) {
    List<GameObject> nearestWeapons = player.getNearestObjects(gameState.getGameObjects(), "WEAPONS");
    if (nearestWeapons.isEmpty() || nearestWeapons == null) {
      return false;
    }

    int countIncoming = 0;

    for (GameObject weapon: nearestWeapons) {
      if ((Math.abs(BotUtil.getHeadingBetween(weapon, player.getBot()) - weapon.currentHeading) < 90)
      && BotUtil.getActualDistance(player.getBot(), weapon) - weapon.getSpeed() < 50
      && BotDefense.getOrthogonalProjectionMagnitude(player.getBot(), weapon) <= player.getBot().getSize()) {
        countIncoming += 1;
      }

      if (countIncoming > 2) {
        System.out.println(
          "Orthogonal Projection: " + String.valueOf(BotDefense.getOrthogonalProjectionMagnitude(player.getBot(), weapon)));
        System.out.println("Bot radius: " + String.valueOf(player.getBot().getSize()));
        return true;
      }
    }

    return false;
  }
}
