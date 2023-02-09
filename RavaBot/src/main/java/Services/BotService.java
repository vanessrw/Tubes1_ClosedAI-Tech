package Services;

import Enums.*;
import Models.*;

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
        playerAction.action = PlayerActions.FORWARD;
        playerAction.heading = new Random().nextInt(360);

        if (!gameState.getGameObjects().isEmpty()) {
            // var foodList = gameState.getGameObjects()
            //         .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD)
            //         .sorted(Comparator
            //                 .comparing(item -> getDistanceBetween(bot, item)))
            //         .collect(Collectors.toList());

            // playerAction.heading = getHeadingBetween(foodList.get(0));

            playerAction.heading = getHeadingBetween(this.getMaxScoreObject());
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

    // Scoring methods
    private void scoreObject(GameObject object) {

        object.score = 0.0;

        if (object.getGameObjectType() == ObjectTypes.FOOD || object.getGameObjectType() == ObjectTypes.SUPERFOOD) {
            // RESOURCE TYPE
            // V(s) = Size + Effects
            // Food Effects = 0; Superfood Effects = 5; Supernova Pickup = 10. 
            object.score += object.getSize();
            if (object.getGameObjectType() == ObjectTypes.SUPERFOOD) {
                object.score += 5;
            } else if (object.getGameObjectType() == ObjectTypes.SUPERNOVAPICKUP){
                object.score += 10;
            }
            object.score /= getDistanceBetween(this.bot, object);
        } else if (object.getGameObjectType() == ObjectTypes.PLAYER) {
            // ENEMY TYPE
            // V(s) = w1 * Size + w2 * Speed
            if (object.getSize() >= this.bot.getSize()) {
                object.score += -object.getSize() - 0.25 * object.getSpeed();
            } else if (Math.abs(this.bot.getSize() - object.getSize()) <= 10) {
                object.score += 0.35 * object.getSize() + 0.35 * object.getSpeed();
            } else if (Math.abs(this.bot.getSize() - object.getSize()) <= 25) {
                object.score += 0.75 * object.getSize() + 0.65 * object.getSpeed();
            } else {
                object.score += 0.65 * object.getSize() + 0.65 * object.getSpeed();
            }
            object.score /= getDistanceBetween(this.bot, object);
        } else if (object.getGameObjectType() == ObjectTypes.GASCLOUD || object.getGameObjectType() == ObjectTypes.ASTEROIDFIELD) {
            // OBSTACLE TYPE
            // V(s) = Effects
            if (this.bot.getSize() <= 15) {
                object.score -= object.getSize();
            } else if (this.bot.getSize() <= 30) {
                object.score -= 0.85 * object.getSize();
            } else {
                object.score -= 0.75 * object.getSize();
            }
            object.score /= getDistanceBetween(this.bot, object);
        } else if (object.getGameObjectType() == ObjectTypes.TORPEDOSALVO || object.getGameObjectType() == ObjectTypes.SUPERNOVABOMB) {
            // ENEMY WEAPONS TYPE
            object.score -= 10;
            if (object.getGameObjectType() == ObjectTypes.SUPERNOVABOMB) {
                object.score -= 30;
            }
            object.score /= getDistanceBetween(this.bot, object);
        } else if (object.getGameObjectType() == ObjectTypes.WORMHOLE) {
            // TRAVERSAL TYPE
            // V(s) = 0.2 * objectSize for bigger wormhole; 0 for smaller
            if (object.getSize() >= this.bot.getSize()) {
                object.score += 0.2 * object.getSize();
            }
            object.score /= getDistanceBetween(this.bot, object);
        }
    }

    private void scale(ArrayList<GameObject> objects, double weight) {
        // Min max scaler with weight multiplier
        double max = objects.get(0).score;
        double min = objects.get(0).score;

        for (GameObject object: objects) {
            if (object.score > max) {
                max = object.score;
            } else if (object.score < min) {
                min = object.score;
            }
        }
        double range = max - min;
    
        for (GameObject object : objects) {
          object.score = ((object.score - min) / range) * weight;
        }
    }

    private GameObject getMaxScoreObject() {
        // Type 0: Resource
        // Type 1: Enemy bot
        // Type 2: Obstacle
        // Type 3: Weapons
        // Type 4: Traversal

        ArrayList<GameObject> type0 = new ArrayList<>();
        ArrayList<GameObject> type1 = new ArrayList<>();
        ArrayList<GameObject> type2 = new ArrayList<>();
        ArrayList<GameObject> type3 = new ArrayList<>();
        ArrayList<GameObject> type4 = new ArrayList<>();

        for (GameObject object : gameState.gameObjects) {
            this.scoreObject(object);
            if (object.getGameObjectType() == ObjectTypes.FOOD || object.getGameObjectType() == ObjectTypes.SUPERFOOD) {
                // RESOURCE TYPE
                type0.add(object);
            } else if (object.getGameObjectType() == ObjectTypes.PLAYER) {
                // ENEMY TYPE
                type1.add(object);
            } else if (object.getGameObjectType() == ObjectTypes.GASCLOUD || object.getGameObjectType() == ObjectTypes.ASTEROIDFIELD) {
                // OBSTACLE TYPE
                type2.add(object);
            } else if (object.getGameObjectType() == ObjectTypes.TORPEDOSALVO || object.getGameObjectType() == ObjectTypes.SUPERNOVABOMB) {
                // ENEMY WEAPONS TYPE
                type3.add(object);
            } else if (object.getGameObjectType() == ObjectTypes.WORMHOLE) {
                // TRAVERSAL TYPE
                type4.add(object);
            } else {
                continue;
            }
        }

        this.scale(type0, 1.0);
        this.scale(type1, 0.9);
        this.scale(type2, 0.75);
        this.scale(type3, 0.65);
        this.scale(type4, 0.25);
        
        GameObject maxScoreObject = type0.get(0);

        for (GameObject object: type0) {
            if (object.score > maxScoreObject.score) {
                maxScoreObject = object;
            }
        }
        for (GameObject object: type1) {
            if (object.score > maxScoreObject.score) {
                maxScoreObject = object;
            }
        }
        for (GameObject object: type2) {
            if (object.score > maxScoreObject.score) {
                maxScoreObject = object;
            }
        }
        for (GameObject object: type3) {
            if (object.score > maxScoreObject.score) {
                maxScoreObject = object;
            }
        }
        for (GameObject object: type4) {
            if (object.score > maxScoreObject.score) {
                maxScoreObject = object;
            }
        }

        return maxScoreObject;
    }
}