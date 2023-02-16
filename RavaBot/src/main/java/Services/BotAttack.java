package Services;

import Models.*;
import Enums.*;

import java.util.*;
import java.util.stream.*;

public class BotAttack {
  // PUBLIC METHOD
  public static PlayerAction attackEnemy(Bot player, GameObject target) {
    PlayerAction playerAction = new PlayerAction();

    if (BotUtil.getActualDistance(player.getBot(), target) <= player.getToll() / 2) {
      if (player.isAfterburnerActive()) {
        playerAction.action = PlayerActions.STOPAFTERBURNER;
        System.out.println("================WAKTUNYA NEMBAK================");
      } else {
        playerAction.action = PlayerActions.FIRETORPEDOES;
        System.out.println("====================SHOOT===============");
        playerAction.heading = player.getHeadingBetween(target);
      }
    } else if (player.getBot().getSize() >= 50 && !player.isAfterburnerActive()) {
      playerAction.action = PlayerActions.STARTAFTERBURNER;
      System.out.println("====================AFTERBURNER===============");
    } else if (player.isAfterburnerActive() && player.getBot().getSize() <= 50) {
      playerAction.action = PlayerActions.STOPAFTERBURNER;
      System.out.println("===================JAN BUNDIR=================");
    } else {
      playerAction.action = PlayerActions.FORWARD;
      System.out.println("====================KEJAR MASBRO===============");
      playerAction.heading = player.getHeadingBetween(target);
    }

    return playerAction;
  }

  public static PlayerAction checkAfterburner(Bot player, PlayerAction playerAction) {
    if (player.isAfterburnerActive()) {
      playerAction.action = PlayerActions.STOPAFTERBURNER;
      System.out.println("==================PELAN PELAN================");
    }
    return playerAction;
  }

  public static boolean fireRandomTorpedo(Bot player, List<GameObject> objectList, int toll) {
    List<GameObject> nearestEnemies = player.getNearestObjects(player.getGameState().getPlayerGameObjects(), "ENEMY");

    if (nearestEnemies.isEmpty()) {
      return false;
    }

    PlayerAction playerAction = new PlayerAction();

    playerAction.action = PlayerActions.FIRETORPEDOES;
    playerAction.heading = player.getHeadingBetween(nearestEnemies.get(0));

    player.setPlayerAction(playerAction);
    System.out.println("==================NEMBAK RANDOM NGAB==================");
    return true;
  }
}
