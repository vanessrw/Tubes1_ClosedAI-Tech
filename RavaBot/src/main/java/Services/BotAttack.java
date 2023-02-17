package Services;

import Models.*;
import Enums.*;

import java.util.*;

public class BotAttack {
  // PRIVATE METHOD

  /**
   * This function checks if the alignment between the bot and its target is
   * within a certain threshold
   * 
   * @param player     : Player's bot
   * @param target     : Enemy bot
   * @param tollerance : Degree threshold
   * @return True when the alignment between the bot and its target is within a
   *         certain threshold
   */
  private static boolean isAligned(Bot player, GameObject target, int tollerance) {
    return Math.abs(target.getHeading() - player.getBot().getHeading()) <= tollerance;
  }

  // PUBLIC METHOD

  /**
   * This function controls the logic of attacking enemy bot
   * 
   * @param player : Player's bot
   * @param target : Enemy bot
   * @return A new player action object to attack enemy
   */
  public static PlayerAction attackEnemy(Bot player, GameObject target) {
    PlayerAction playerAction = new PlayerAction();

    if (BotUtil.getActualDistance(player.getBot(), target) <= player.getToll() / 2) {
      
      playerAction.action = PlayerActions.FIRETORPEDOES;
      System.out.println("====================SHOOT===============");
      playerAction.heading = player.getHeadingBetween(target);
      
    } else {
      playerAction.action = PlayerActions.FORWARD;
      System.out.println("====================KEJAR MASBRO===============");
      playerAction.heading = player.getHeadingBetween(target);
    }

    return playerAction;
  }

  /**
   * This function will stops the bot's afterburner when the bot is not attacking
   * an enemy by modifying previous player action
   * 
   * @param player       : Player's bot
   * @param playerAction : Player's previous action
   * @return A new player action object to stop afterburner
   */
  public static PlayerAction checkAfterburner(Bot player, PlayerAction playerAction) {
    if (player.isAfterburnerActive()) {
      playerAction.action = PlayerActions.STOPAFTERBURNER;
      System.out.println("==================PELAN PELAN================");
    }
    return playerAction;
  }

  /**
   * This function controls the logic of firing a random torpedo when the bot has
   * reached its maximum salvo limit
   * 
   * @param player : Player's bot
   * @return True when the bot has succeded firing a random torpedo
   */
  public static boolean fireRandomTorpedo(Bot player) {
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
