package Services;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

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

  public static void writeToFile(GameState gameState, String fileName) {
    try {
      FileWriter fileWriter = new FileWriter(fileName, true);
      BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
      // if(bufferedWriter)
      bufferedWriter.write(String.valueOf(gameState.getWorld().getCurrentTick()));
      bufferedWriter.newLine();
      bufferedWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static int getHeadingBetween(GameObject object1, GameObject object2) {
    int direction = toDegrees(Math.atan2(object2.getPosition().y - object1.getPosition().y,
        object2.getPosition().x - object1.getPosition().x));
    return (direction + 360) % 360;
  }
}
