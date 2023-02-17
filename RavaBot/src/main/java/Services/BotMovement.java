package Services;

import Models.*;
import Enums.*;

import java.util.*;
// import java.util.stream.*;

public class BotMovement {
  // PUBLIC METHOD
  public static int getOptimalHeading(Bot player) {
    // Get all objects
    List<GameObject> allObjects = player.getGameState().getPlayerGameObjects();
    allObjects.addAll(player.getGameState().getGameObjects());

    // Get the nearest consumables
    List<GameObject> nearestConsumables = player.getNearestObjects(player.getGameState().getGameObjects(),
        "CONSUMABLES");
    GameObject nearestConsumable = !nearestConsumables.isEmpty() ? nearestConsumables.get(0) : null;

    // If there are no enemy of concern, consider consumables
    if (nearestConsumable != null) {
      System.out.println("Nom nom");
      return player.getHeadingBetween(nearestConsumable);
    }

    System.out.println("Nothing of concern..");
    return player.getHeadingBetween(player.getCenterPoint());
  }

  /**
   * This function controls the logic of escaping an obstacle
   * 
   * @param player : Player's bot
   * @return True when the bot is in an obstacle
   */
  public static boolean checkObstacle(Bot player) {
    if (player.isInGasCloud() || player.isInAsteroidField()) {
      PlayerAction playerAction = new PlayerAction();
      playerAction.action = PlayerActions.FORWARD;
      playerAction.heading = (player.getBot().getHeading() + 180) % 360;
      System.out.println("OOOOOOOOOOOOO KELUAR DARI BAHAYA OOOOOOOOOOOOO");
      player.setPlayerAction(playerAction);
      return true;
    }

    return false;
  }
}
