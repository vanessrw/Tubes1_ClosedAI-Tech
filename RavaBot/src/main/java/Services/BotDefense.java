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
}
