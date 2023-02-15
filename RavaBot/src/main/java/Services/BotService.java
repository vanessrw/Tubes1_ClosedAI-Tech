package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService {
    // General attributes
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;
    private GameObject centerPoint = new GameObject(null, null, null, null, new Position(), null, null);

    // Hyperparameters
    // toll: Tollerance distance to nearby smaller enemies
    private int toll = 400;

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

    private double getActualDistance(GameObject object1, GameObject object2) {
        return getDistanceBetween(object1, object2) - Double.valueOf(object1.getSize()) - Double.valueOf(object2.getSize());
    }

    private int getHeadingBetween(GameObject otherObject) {
        int direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    private int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }

    //                          BOT LOGIC METHODS
    // =====================================================================
    // =====================================================================

    public void computeNextPlayerAction(PlayerAction playerAction) {
        playerAction.action = PlayerActions.FORWARD;
        playerAction.heading = new Random().nextInt(360);

        if (!gameState.getPlayerGameObjects().isEmpty()) {
            // Nearest enemy
            List<GameObject> nearestEnemies = getNearestObjects(gameState.getPlayerGameObjects(), "ENEMY");
            GameObject nearestEnemy;
            if (!nearestEnemies.isEmpty()) {
                nearestEnemy = nearestEnemies.get(0);
            } else {
                nearestEnemy = null;
            }
            
            // Nearest consumables
            List<GameObject> nearestConsumables = getNearestObjects(gameState.getPlayerGameObjects(), "CONSUMABLES");
            GameObject nearestConsumable;
            if (!nearestConsumables.isEmpty()) {
                nearestConsumable = nearestConsumables.get(0);
            } else {
                nearestConsumable = null;
            }

            // Check if out of bounds
            if (getDistanceBetween(centerPoint, bot) + (1.75 * this.bot.getSize()) + 50 > this.gameState.getWorld().getRadius()) {
                playerAction.action = PlayerActions.FORWARD;
                if (nearestConsumable == null) {
                    playerAction.heading = getHeadingBetween(nearestConsumable);
                    System.out.println("Whoops.. nom nom");
                } else {
                    playerAction.heading = getHeadingBetween(centerPoint);
                    System.out.println("Whoops.. to center");
                }
                this.playerAction = playerAction;
                return;
            }
    
            // Check if near or in an obstacle
            List<GameObject> nearestObstacles = getNearestObjects(gameState.getGameObjects(), "OBSTACLES");
            GameObject nearestObstacle;
            if (!nearestObstacles.isEmpty()) {
                nearestObstacle = nearestObstacles.get(0);
            } else {
                nearestObstacle = null;
            }
    
            if (nearestObstacle != null && getDistanceBetween(nearestObstacle, this.bot) < (1.75 * this.bot.getSize() + nearestObstacle.getSize())) {
                // Get nearest consumable outside of obstacle
                List<GameObject> nearestObjectOutside = getNearestObjects(gameState.getGameObjects(), "CONSUMABLES")
                                                    .stream().filter(item -> getDistanceBetween(item, nearestObstacle) > (1.75 * this.bot.getSize() + nearestObstacle.getSize()))
                                                    .sorted(Comparator.comparing(item -> getDistanceBetween(this.bot, item)))
                                                    .collect(Collectors.toList());
                if (!nearestObjectOutside.isEmpty()) {
                    playerAction.heading = getHeadingBetween(nearestObjectOutside.get(0));
                    playerAction.action = PlayerActions.FORWARD;
                    this.playerAction = playerAction;
                    System.out.println("Avoiding obstacles..");
                    return;
                }
            }
    
            // Consider enemies
            if ((nearestEnemy != null && this.bot.getSize() >= 70 && getActualDistance(this.bot, nearestEnemy) <= this.toll) || (nearestEnemy != null && this.bot.getSize() >= 35 && getActualDistance(this.bot, nearestEnemy) <= 200)) {
            // if (nearestEnemy != null && this.bot.getSize() >= 70 && getActualDistance(this.bot, nearestEnemy) <= this.toll) {
                playerAction.heading = getHeadingBetween(nearestEnemy);
                playerAction.action = PlayerActions.FIRETORPEDOES;
                System.out.println("Pew pew pew");
                this.playerAction = playerAction;
                return;
            } else {
            // Consider other targets
                playerAction.heading = getOptimalHeading(this.toll);
                playerAction.action = PlayerActions.FORWARD;
                this.playerAction = playerAction;
                return;
            }
        }
    }


    private List<GameObject> getNearestObjects(List<GameObject> objectList, String category) {
        if (objectList.isEmpty() || objectList == null) {
            return new ArrayList<GameObject>(10);
        }

        var filteredList = objectList;
        if (category == "ENEMY") {
            filteredList = objectList
                        .stream().filter(item -> item.getGameObjectType() == ObjectTypes.PLAYER && item.getId() != this.bot.getId())
                        .sorted(Comparator
                                .comparing(item -> getActualDistance(bot, item)))
                        .collect(Collectors.toList());

        } else if (category == "CONSUMABLES") {
            filteredList = objectList
            .stream().filter(item -> (item.getGameObjectType() == ObjectTypes.FOOD || item.getGameObjectType() == ObjectTypes.SUPERFOOD || item.getGameObjectType() == ObjectTypes.SUPERNOVA_PICKUP) && getDistanceToNearestBorder(item) > this.bot.getSize() * 2.5 + this.bot.getSpeed() + 150)
                    .sorted(Comparator
                            .comparing(item -> getActualDistance(bot, item)))
                    .collect(Collectors.toList());

        } else if (category == "OBSTACLES") {
            filteredList = objectList
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.GAS_CLOUD || item.getGameObjectType() == ObjectTypes.ASTEROID_FIELD)
                    .sorted(Comparator
                            .comparing(item -> getActualDistance(bot, item)))
                    .collect(Collectors.toList());

        } else if (category == "WEAPONS") {
            filteredList = objectList
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TORPEDO_SALVO || item.getGameObjectType() == ObjectTypes.SUPERNOVA_BOMB)
                    .sorted(Comparator
                            .comparing(item -> getActualDistance(bot, item)))
                    .collect(Collectors.toList());

        } else if (category == "TRAVERSAL") {
            filteredList = objectList
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.WORMHOLE)
                    .sorted(Comparator
                            .comparing(item -> getActualDistance(bot, item)))
                    .collect(Collectors.toList());
        } else {
            filteredList = objectList
                    .stream().sorted(Comparator
                            .comparing(item -> getActualDistance(bot, item)))
                    .collect(Collectors.toList());
        }

        return filteredList;
    }

    private int avoidThreatHeading(List<GameObject> objectList, GameObject threat, int minDegree, int maxDegree) {
        if (!objectList.isEmpty()) {
            List<GameObject> resources = objectList
                        .stream().filter(item -> (
                            item.getGameObjectType() == ObjectTypes.FOOD ||
                            item.getGameObjectType() == ObjectTypes.SUPERFOOD ||
                            item.getGameObjectType() == ObjectTypes.SUPERNOVA_PICKUP ||
                            (item.getGameObjectType() == ObjectTypes.PLAYER && item.getSize() < this.bot.getSize() && Math.abs(item.getSize() - this.bot.getSize()) > 30)
                        ))
                        .sorted(Comparator
                                .comparing(item -> getDistanceBetween(bot, item)))
                        .collect(Collectors.toList());
    
            GameObject nearestTarget;
    
            if (resources.isEmpty()) {
                // Getting away with no resource left
                System.out.println("Running pointlessly");
                return toDegrees(Math.atan2(threat.getPosition().getY() - this.bot.getPosition().getY(), threat.getPosition().getX() - this.bot.getPosition().getX()));
            } else {
                // Getting away while gathering resources 
                // Consider distances
                double distanceToThreat = getActualDistance(this.bot, threat);
                nearestTarget = resources.get(0);
                double distanceToNearestTarget = getActualDistance(this.bot, nearestTarget);
                double potentialThreatRadius = threat.getSize() + threat.getSpeed() + 0.5 * this.toll;
                
                if (distanceToThreat > potentialThreatRadius && distanceToNearestTarget < getActualDistance(threat, nearestTarget) + potentialThreatRadius) {
                    System.out.println("Nom nom");
                    return getHeadingBetween(nearestTarget);
                } else {
                    List<GameObject> resourcesBehind = resources
                                .stream().filter(item ->
                                    getHeadingBetween(item) > (getHeadingBetween(threat) + maxDegree) % 360 ||
                                    getHeadingBetween(item) < (getHeadingBetween(threat) - minDegree) % 360
                                ).sorted(Comparator
                                .comparing(item -> getDistanceBetween(bot, item)))
                                .collect(Collectors.toList());
                    nearestTarget = resourcesBehind.get(0);
                    
                    if (nearestTarget != null) {
                        System.out.println("Runnn");
                        return getHeadingBetween(nearestTarget);
                    } else {
                        System.out.println("Yolo");
                        return getHeadingBetween(resources.get(0));
                    }
                }
            }
        } else {
            return getHeadingBetween(centerPoint);
        }
    }

    private double getDistanceToNearestBorder(GameObject object) {
        double triangleX = Math.abs(object.getPosition().x);
        double triangleY = Math.abs(object.getPosition().y);
        double distanceToCenter = Math.sqrt(triangleX * triangleX + triangleY * triangleY);
        return gameState.getWorld().getRadius() - distanceToCenter - object.getSize();
    }

    private int getOptimalHeading(int toll) {
        // Get all objects
        List<GameObject> allObjects = this.getGameState().getPlayerGameObjects();
        allObjects.addAll(this.getGameState().getGameObjects());

        // Get the nearest consumables
        List<GameObject> nearestConsumables = getNearestObjects(this.gameState.getGameObjects(), "CONSUMABLES");
        GameObject nearestConsumable;
        if (!nearestConsumables.isEmpty()) {
            nearestConsumable = nearestConsumables.get(0);
        } else {
            nearestConsumable = null;
        }

        // Get the nearest enemy
        List<GameObject> nearestEnemies = getNearestObjects(gameState.getPlayerGameObjects(), "ENEMY");
        GameObject nearestEnemy;
        if (!nearestEnemies.isEmpty()) {
            nearestEnemy = nearestEnemies.get(0);
        } else {
            nearestEnemy = null;
        }

        // Consider nearest enemy bot
        if (nearestEnemy != null) {
            // If enemy is bigger
            if (nearestEnemy.getSize() >= this.bot.getSize()) {
                return avoidThreatHeading(allObjects, nearestEnemy, 45, 45);
            }
            
            // If enemy is significantly smaller
            if (Math.abs(nearestEnemy.getSize() - this.bot.getSize()) > 30 && getActualDistance(this.bot, nearestEnemy) < toll) {
                System.out.println("Kejar bot nih");
                return getHeadingBetween(nearestEnemy);
            }
        }

        // If there are no enemy of concern, consider consumables
        if (nearestConsumable != null) {
            if (Math.abs(getHeadingBetween(nearestConsumable) - this.bot.currentHeading) > 165) {
                System.out.println("No bolak balik..");
                return this.bot.currentHeading;
            }
            System.out.println("Nom nom");
            return getHeadingBetween(nearestConsumable);
        }

        System.out.println("Nothing of concern..");
        return getHeadingBetween(centerPoint);

    }
}