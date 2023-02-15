package Services;

import Models.*;

public class BotUtil {
  public static int toDegrees(double v) {
    return (int) (v * (180 / Math.PI));
  }

  public static double getDistanceBetween(GameObject object1, GameObject object2) {
    double triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
    double triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
    return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
  }

  public static double getActualDistance(GameObject object1, GameObject object2) {
    return getDistanceBetween(object1, object2) - Double.valueOf(object1.getSize()) - Double.valueOf(object2.getSize());
  }

  public static double getDistanceToNearestBorder(GameObject object, GameState gameState) {
    double triangleX = Math.abs(object.getPosition().x);
    double triangleY = Math.abs(object.getPosition().y);
    double distanceToCenter = Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    return gameState.getWorld().getRadius() - distanceToCenter - object.getSize();
  }
}
