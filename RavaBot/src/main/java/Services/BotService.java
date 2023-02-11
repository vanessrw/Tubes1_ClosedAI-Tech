package Services;

import Enums.*;
import Models.*;
import java.io.*;

import java.util.*;
// import java.util.stream.*;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;

    public BotService() {
        this.playerAction = new PlayerAction();
        this.gameState = new GameState();
    }

    public GameObject getBot() {
        return this.bot;
    }

    public void setBot(GameObject bot) {
        this.bot = bot;
    }

    public PlayerAction getPlayerAction() {
        return this.playerAction;
    }

    public void setPlayerAction(PlayerAction playerAction) {
        this.playerAction = playerAction;
    }

    public void computeNextPlayerAction(PlayerAction playerAction) {
        // playerAction.action = PlayerActions.FORWARD;
        // playerAction.heading = new Random().nextInt(360);

        // Weight parameters
        double w0 = 1.0;
        double w1 = 0.0;
        double w2 = 0.0;
        double w3 = 0.0;
        double w4 = 0.0;

        if (!gameState.getGameObjects().isEmpty()) {
            ArrayList<GameObject> nearestObjects = nearestObjectsScored();
            if(gameState.world.getCurrentTick() == 56) {
              writeToFile(bot);
            }
            if(gameState.world.getCurrentTick() == 50) {
              playerAction.action = PlayerActions.FIRETORPEDOES;
              playerAction.heading = 0;
            } else {
              playerAction.action = PlayerActions.FORWARD;
              playerAction.heading = getHeadingBetween(this.getMaxScoreObject(nearestObjects, w0, w1, w2, w3, w4));
            }

        }

        this.playerAction = playerAction;
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        updateSelfState();
    }

    private void updateSelfState() {
        Optional<GameObject> optionalBot = gameState.getPlayerGameObjects().stream().filter(gameObject -> gameObject.id.equals(bot.id)).findAny();
        optionalBot.ifPresent(bot -> this.bot = bot);
    }

    private double getDistanceBetween(GameObject object1, GameObject object2) {
        double triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        double triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    private int getHeadingBetween(GameObject otherObject) {
        int direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    private int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }

    public void writeToFile(GameObject obj) {
      try {
        FileWriter fileWriter = new FileWriter("test.txt", true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(String.valueOf(obj.TorpedoSalvoCount));
        bufferedWriter.close();
      } catch (IOException e) {
        e.printStackTrace();
    }
    }

    public void writeScoresToFile(ArrayList<GameObject> objectList) {
        try {
            FileWriter fileWriter = new FileWriter("scores.txt", true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write("Tick: ");
            bufferedWriter.write(String.valueOf(gameState.world.getCurrentTick()));
            bufferedWriter.newLine();
            bufferedWriter.write("Nearest enemy: ");
            bufferedWriter.write(String.valueOf(objectList.get(0).score));
            bufferedWriter.newLine();
            bufferedWriter.write("Nearest consumables: ");
            bufferedWriter.write(String.valueOf(objectList.get(1).score));
            bufferedWriter.newLine();
            bufferedWriter.write("Nearest obstacle: ");
            bufferedWriter.write(String.valueOf(objectList.get(2).score));
            bufferedWriter.newLine();
            bufferedWriter.write("Nearest weapons: ");
            bufferedWriter.write(String.valueOf(objectList.get(3).score));
            bufferedWriter.newLine();
            bufferedWriter.write("Nearest traversal: ");
            bufferedWriter.write(String.valueOf(objectList.get(4).score));
            bufferedWriter.newLine();
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Scoring methods

    private ArrayList<GameObject> getNearestObjects() {
        GameObject minConsumable = gameState.gameObjects.get(0);
        GameObject minPlayer = gameState.playerGameObjects.get(0);
        GameObject minObstacle = gameState.gameObjects.get(0);
        GameObject minEnemyWeapons = gameState.gameObjects.get(0);
        GameObject minTraversal = gameState.gameObjects.get(0);

        for (GameObject enemy: gameState.playerGameObjects) {
            // ENEMY TYPE
            if (getDistanceBetween(this.bot, enemy) < getDistanceBetween(this.bot, minPlayer)) {
                minPlayer = enemy;
            }
        }

        for (GameObject object: gameState.gameObjects) {
            if (object.getGameObjectType() == ObjectTypes.FOOD || object.getGameObjectType() == ObjectTypes.SUPERFOOD) {
                // CONSUMABLE TYPE
                if (getDistanceBetween(this.bot, object) < getDistanceBetween(this.bot, minConsumable)) {
                    minPlayer = object;
                }
            } else if (object.getGameObjectType() == ObjectTypes.GAS_CLOUD || object.getGameObjectType() == ObjectTypes.ASTEROID_FIELD) {
                // OBSTACLE TYPE
                if (getDistanceBetween(this.bot, object) < getDistanceBetween(this.bot, minObstacle)) {
                    minObstacle = object;
                }
            } else if (object.getGameObjectType() == ObjectTypes.TORPEDO_SALVO || object.getGameObjectType() == ObjectTypes.SUPERNOVA_BOMB) {
                // ENEMY WEAPONS TYPE
                if (getDistanceBetween(this.bot, object) < getDistanceBetween(this.bot, minEnemyWeapons)) {
                    minEnemyWeapons = object;
                }
            } else if (object.getGameObjectType() == ObjectTypes.WORMHOLE) {
                // TRAVERSAL TYPE
                if (getDistanceBetween(this.bot, object) < getDistanceBetween(this.bot, minTraversal)) {
                    minTraversal = object;
                }
            }
        }

        ArrayList<GameObject> result = new ArrayList<GameObject>();

        result.add(minPlayer);
        result.add(minConsumable);
        result.add(minObstacle);
        result.add(minEnemyWeapons);
        result.add(minTraversal);

        return result;
    }

    private void scoreObject(GameObject object) {

        object.score = 0.0;

        if (object.getGameObjectType() == ObjectTypes.FOOD || object.getGameObjectType() == ObjectTypes.SUPERFOOD) {
            // RESOURCE TYPE
            object.score += 5.0;
            if (object.getGameObjectType() == ObjectTypes.SUPERFOOD) {
                object.score += 5.0;
            } else if (object.getGameObjectType() == ObjectTypes.SUPERNOVA_PICKUP){
                object.score += 10.0;
            }
            object.score /= getDistanceBetween(this.bot, object);
        } else if (object.getGameObjectType() == ObjectTypes.PLAYER) {
            // ENEMY TYPE
            if (object.getSize() >= this.bot.getSize()) {
                object.score -= 20.0;
            } else if (Math.abs(this.bot.getSize() - object.getSize()) <= 10) {
                object.score += 2.5;
            } else if (Math.abs(this.bot.getSize() - object.getSize()) <= 25) {
                object.score -= 15.0;
            } else {
                object.score += 25.0;
            }

            if (getDistanceBetween(this.bot, object) < 10) {
                object.score *= 2;
            }
            
            object.score /= getDistanceBetween(this.bot, object);
        } else if (object.getGameObjectType() == ObjectTypes.GAS_CLOUD || object.getGameObjectType() == ObjectTypes.ASTEROID_FIELD) {
            // OBSTACLE TYPE
            if (this.bot.getSize() <= 15) {
                object.score -= 10.0;
            } else if (this.bot.getSize() <= 30) {
                object.score -= 5.0;
            } else {
                object.score -= 2.5;
            }
            object.score /= getDistanceBetween(this.bot, object);
        } else if (object.getGameObjectType() == ObjectTypes.TORPEDO_SALVO || object.getGameObjectType() == ObjectTypes.SUPERNOVA_BOMB) {
            // ENEMY WEAPONS TYPE
            object.score -= 10;
            if (object.getGameObjectType() == ObjectTypes.SUPERNOVA_BOMB) {
                object.score -= 30;
            }
            object.score /= getDistanceBetween(this.bot, object);
        } else if (object.getGameObjectType() == ObjectTypes.WORMHOLE) {
            // TRAVERSAL TYPE
            if (object.getSize() >= this.bot.getSize()) {
                object.score += 2.5;
            }
            object.score /= getDistanceBetween(this.bot, object);
        }
    }

    private ArrayList<GameObject> nearestObjectsScored() {
        ArrayList<GameObject> near = getNearestObjects();

        for (GameObject object: near) {
            scoreObject(object);
        }

        return near;
    }

    private GameObject getMaxScoreObject(ArrayList<GameObject> listObject, double w0, double w1, double w2, double w3, double w4) {
        // Type 0: Enemy Bot
        // Type 1: Resources
        // Type 2: Obstacle
        // Type 3: Weapons
        // Type 4: Traversal

        // Get weighted score

        ArrayList<GameObject> nearestObjects = nearestObjectsScored();
        
        // nearestObjects.get(0).score *= w0;
        // nearestObjects.get(1).score *= w1;
        // nearestObjects.get(2).score *= w2;
        // nearestObjects.get(3).score *= w3;
        // nearestObjects.get(4).score *= w4;
        
        GameObject maxScoreObject = nearestObjects.get(0);
        for (GameObject object: nearestObjects) {
            if (object.score > maxScoreObject.score) {
                maxScoreObject = object;
            }
        }

        return maxScoreObject;
    }
}