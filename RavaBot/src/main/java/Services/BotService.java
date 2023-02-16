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
    private GameObject centerPoint = new GameObject(null, null, null, null, new Position(), null, null, null, null, null, null);
    private boolean isFiringTeleporter = false;
    private int fireTeleporterTick = 0;

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

    private int getHeadingBetween(GameObject object1, GameObject object2) {
        int direction = toDegrees(Math.atan2(object2.getPosition().y - object1.getPosition().y,
                object2.getPosition().x - object1.getPosition().x));
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
            
            // Teleporting
            if (isFiringTeleporter) {
                System.out.println("Is firing teleporter");
            }

            if (this.isFiringTeleporter && getNearestObjects(gameState.getGameObjects(), "TRAVERSAL").isEmpty() && gameState.getWorld().getCurrentTick() - this.fireTeleporterTick > 40) {
                this.isFiringTeleporter = false;
            }

            GameObject largestSmallerEnemy = null;
            int largestSmallerEnemySize = 0;
            for (GameObject player: gameState.getPlayerGameObjects()) {
                if (player.getId() != this.bot.getId() && this.bot.getSize() - 15 > player.getSize() && player.getSize() > largestSmallerEnemySize) {
                    largestSmallerEnemy = player;
                }
            }

            if (this.isFiringTeleporter && !gameState.getGameObjects().isEmpty() && largestSmallerEnemy != null) {
                for (GameObject teleporter: gameState.getGameObjects()) {
                    if (teleporter.getGameObjectType() == ObjectTypes.TELEPORTER) {
                        if (getActualDistance(largestSmallerEnemy, teleporter) < this.bot.getSize() && largestSmallerEnemy.getSize() < this.bot.getSize()) {
                            playerAction.action = PlayerActions.TELEPORT;
                            playerAction.heading = getOptimalHeading(toll);
                            this.playerAction = playerAction;
                            System.out.println("Teleporting..");
                            this.isFiringTeleporter = false;
                            return;
                        }
                    }
                }
            }

            if (!this.isFiringTeleporter && largestSmallerEnemy != null && this.bot.getSize() > 80 && this.bot.getSize() > largestSmallerEnemy.getSize() && this.bot.getTeleportCount() > 0) {
                playerAction.action = PlayerActions.FIRETELEPORT;
                playerAction.heading = getHeadingBetween(largestSmallerEnemy);
                this.playerAction = playerAction;
                this.fireTeleporterTick = gameState.getWorld().getCurrentTick();
                System.out.println("Ejecting teleporter..");
                this.isFiringTeleporter = true;
                return;
            }
            
            // Check if an enemy weapon is incoming
            // if (isTorpedoIncoming() && this.bot.getSize() > 100 && this.playerAction.action != PlayerActions.ACTIVATESHIELD) {
            //     playerAction.action = PlayerActions.ACTIVATESHIELD;
            //     System.out.println("Shieldddd");
            //     playerAction.heading = getOptimalHeading(toll);
            //     this.playerAction = playerAction;
            //     // return;
            // }

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
                if (nearestConsumable != null) {
                    playerAction.heading = getHeadingBetween(nearestConsumable);
                    System.out.println("Whoops.. nom nom");
                } else {
                    playerAction.heading = getHeadingBetween(centerPoint);
                    System.out.println("Whoops.. to center");
                }
                this.playerAction = playerAction;
                return;
            }
    
            // Consider enemies
            if ((getHighestRatioEnemy() != null && this.bot.getSize() >= 70 && getActualDistance(this.bot, getHighestRatioEnemy()) <= this.toll) || (getHighestRatioEnemy() != null && this.bot.getSize() >= 35 && getActualDistance(this.bot, getHighestRatioEnemy()) <= 200)) {
                playerAction.heading = getHeadingBetween(getHighestRatioEnemy());
                playerAction.action = PlayerActions.FIRETORPEDOES;
                System.out.println("Pew pew pew");
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
            
            // Consider other targets
            playerAction.heading = getOptimalHeading(this.toll);
            playerAction.action = PlayerActions.FORWARD;
            this.playerAction = playerAction;
            return;
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
            .stream().filter(item -> (item.getGameObjectType() == ObjectTypes.FOOD || item.getGameObjectType() == ObjectTypes.SUPERFOOD || item.getGameObjectType() == ObjectTypes.SUPERNOVAPICKUP) && getDistanceToNearestBorder(item) > this.bot.getSize() * 2.5 + this.bot.getSpeed() + 150)
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
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TORPEDOSALVO || item.getGameObjectType() == ObjectTypes.SUPERNOVABOMB)
                    .sorted(Comparator
                            .comparing(item -> getActualDistance(bot, item)))
                    .collect(Collectors.toList());

        } else if (category == "TRAVERSAL") {
            filteredList = objectList
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER)
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

    private GameObject getHighestRatioEnemy() {
        double maxRatio = 0.0;
        GameObject maxRatioPlayer = null;
        if (!gameState.getPlayerGameObjects().isEmpty()) {
            for (GameObject player: gameState.getPlayerGameObjects()) {
                if (player.getId() != this.bot.getId() && player.getSize()/getActualDistance(this.bot, player) > maxRatio) {
                    maxRatio = player.getSize()/getActualDistance(this.bot, player);
                    maxRatioPlayer = player;
                }
            }
        }

        return maxRatioPlayer;
    }

    private int avoidThreatHeading(List<GameObject> objectList, GameObject threat, int minDegree, int maxDegree) {
        if (!objectList.isEmpty()) {
            List<GameObject> resources = objectList
                        .stream().filter(item -> (
                            item.getGameObjectType() == ObjectTypes.FOOD ||
                            item.getGameObjectType() == ObjectTypes.SUPERFOOD ||
                            item.getGameObjectType() == ObjectTypes.SUPERNOVAPICKUP ||
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
                        System.out.println(String.valueOf(getActualDistance(threat, this.bot)));
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
                return avoidThreatHeading(allObjects, nearestEnemy, 60, 60);
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

    private double getOrthogonalProjectionMagnitude(GameObject object1, GameObject object2) {
        // Get orthogonal projection of object2 velocity vector to object1
        int angleBetween = Math.abs(object2.currentHeading - getHeadingBetween(object2, object1));
        double projMagnitude = object2.getSpeed() * Math.sin(Math.toRadians(angleBetween)); 
        return Math.abs(projMagnitude);
    }

    private boolean isTorpedoIncoming() {
        List <GameObject> nearestWeapons = getNearestObjects(gameState.getGameObjects(), "WEAPONS");
        if (nearestWeapons.isEmpty() || nearestWeapons == null) {
            return false;
        }

        GameObject nearestWeapon = nearestWeapons.get(0);

        if (Math.abs(getHeadingBetween(nearestWeapon, bot) - nearestWeapon.currentHeading) > 90) {
            return false;
        }
        
        if (getActualDistance(this.bot, nearestWeapon) - nearestWeapon.getSpeed() < 50 && getOrthogonalProjectionMagnitude(this.bot, nearestWeapon) <= this.bot.getSize()) {
        // if (getActualDistance(this.bot, nearestWeapon) - nearestWeapon.getSpeed() < 100) {
            System.out.println("Orthogonal Projection: " + String.valueOf(getOrthogonalProjectionMagnitude(this.bot, nearestWeapon)));
            System.out.println("Bot radius: " + String.valueOf(this.bot.getSize()));
            return true;
        }

        return false;
    }
}