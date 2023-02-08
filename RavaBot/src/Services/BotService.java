package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

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
            var foodList = gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());

            playerAction.heading = getHeadingBetween(foodList.get(0));
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

    private double getTypeFactor(GameObject gameObjectType) {
        if (gameObjectType.getGameObjectType() == ObjectTypes.PLAYER) {
            if (gameObjectType.getSize() >= this.bot.getSize()) {
                return -1.0;
            } else if (Math.abs(this.bot.getSize() - gameObjectType.getSize()) <= 10) {
                return 0.25;
            } else if (Math.abs(this.bot.getSize() - gameObjectType.getSize()) <= 25) {
                return 0.75;
            } else {
                return 0.85;
            }
        } else if (gameObjectType.getGameObjectType() == ObjectTypes.SUPERFOOD) {
            return 0.825;
        } else if (gameObjectType.getGameObjectType() == ObjectTypes.FOOD) {
            return 0.8;
        } else if (gameObjectType.getGameObjectType() == ObjectTypes.SUPERNOVAPICKUP) {
            return 0.6;
        } else if (gameObjectType.getGameObjectType() == ObjectTypes.TELEPORTER) {
            return 0.4;
        } else if (gameObjectType.getGameObjectType() == ObjectTypes.WORMHOLE) {
            return 0.25;
        } else if (gameObjectType.getGameObjectType() == ObjectTypes.ASTEROIDFIELD) {
            return -0.5;
        } else if (gameObjectType.getGameObjectType() == ObjectTypes.GASCLOUD) {
            return -0.7;
        } else if (gameObjectType.getGameObjectType() == ObjectTypes.TORPEDOSALVO) {
            return -0.85;
        } else if (gameObjectType.getGameObjectType() == ObjectTypes.SUPERNOVABOMB) {
            return -0.9;
        }
    }

    // w1 = size factor weight
    // w2 = speed factor weight
    private double[] getWeights(GameObject gameObjectType) {
        if (gameObjectType.getGameObjectType() == ObjectTypes.PLAYER) {
            if (gameObjectType.getSize() >= this.bot.getSize()) {
                return new double[]{0.95, 0.9};
            } else if (Math.abs(this.bot.getSize() - gameObjectType.getSize()) <= 10) {
                return new double[]{0.35, 0.35};
            } else if (Math.abs(this.bot.getSize() - gameObjectType.getSize()) <= 20) {
                return new double[]{0.75, 0.65};
            } else {
                return new double[]{0.85, 0.85};
            }
        } else if (gameObjectType.getGameObjectType() == ObjectTypes.SUPERFOOD) {
            return new double[]{0.95, 0.0};
        } else if (gameObjectType.getGameObjectType() == ObjectTypes.FOOD) {
            return new double[]{0.9, 0.0};
        } else if (gameObjectType.getGameObjectType() == ObjectTypes.SUPERNOVAPICKUP) {
            return new double[]{0.85, 0.0};
        } else if (gameObjectType.getGameObjectType() == ObjectTypes.TELEPORTER) {
            return new double[]{0.65, 0.0};
        } else if (gameObjectType.getGameObjectType() == ObjectTypes.WORMHOLE) {
            return new double[]{0.60, 0.0};
        } else if (gameObjectType.getGameObjectType() == ObjectTypes.ASTEROIDFIELD) {
            return new double[]{0.75, 0.0};
        } else if (gameObjectType.getGameObjectType() == ObjectTypes.GASCLOUD) {
            return new double[]{0.85, 0.0};
        } else if (gameObjectType.getGameObjectType() == ObjectTypes.TORPEDOSALVO) {
            return new double[]{0.9, 0.9};
        } else if (gameObjectType.getGameObjectType() == ObjectTypes.SUPERNOVABOMB) {
            return new double[]{0.95, 0.0};
        }
    }

    private double scoreObject(GameObject object) {
        // V(s) = a (w1x1 + w2x2) 
        // x1 = size factor
        // x2 = speed factor
        // a = type factor

        double[] features = new double[] {
            object.getSize(),
            object.getSpeed(),
            this.getTypeFactor(object)
        };

        double[] weights = getWeights(object);

        return (features[2] * (weights[0]*features[0] + weights[1]*features[1]));
    }

    private List<GameObject> getObjectsInRadius(int x, int y, int radius) {
        List<GameObject> result = new ArrayList<>();

        for (GameObject gameObject : gameState.gameObjects) {
            if (Math.min(getDistanceBetween(this.bot, gameObject), this.gameState.getWorld().getRadius()) <= radius) {
                result.add(gameObject);
            }
        }
    }
}