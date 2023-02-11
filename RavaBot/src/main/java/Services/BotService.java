package Services;

import Enums.*;
import Models.*;
import java.io.*;

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
        ArrayList<Double> weights = new ArrayList<>(Arrays.asList(1.25, 1.5, 1.0, 1.0));

        playerAction.action = PlayerActions.FORWARD;
        playerAction.heading = getOptimalHeading(weights, 700, 8, 350);

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
        return getDistanceBetween(object1, object2) - 0.5 * object1.getSize() - 0.5 * object2.getSize();
    }

    private int getHeadingBetween(GameObject otherObject) {
        int direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    private int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }

    public void writeToFile(ArrayList<GameObject> objectList, String fileName) {
        try {
            FileWriter fileWriter = new FileWriter(fileName, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write("Tick: ");
            bufferedWriter.write(String.valueOf(gameState.world.getCurrentTick()));
            bufferedWriter.newLine();
            bufferedWriter.write("Nearest enemy: ");
            bufferedWriter.write(String.valueOf(objectList.get(0).getSize()));
            bufferedWriter.newLine();
            bufferedWriter.write("Nearest consumables: ");
            bufferedWriter.write(String.valueOf(objectList.get(1).getSize()));
            bufferedWriter.newLine();
            bufferedWriter.write("Nearest obstacle: ");
            bufferedWriter.write(String.valueOf(objectList.get(2).getSize()));
            bufferedWriter.newLine();
            bufferedWriter.write("Nearest weapons: ");
            bufferedWriter.write(String.valueOf(objectList.get(3).getSize()));
            bufferedWriter.newLine();
            bufferedWriter.write("Nearest traversal: ");
            bufferedWriter.write(String.valueOf(objectList.get(4).getSize()));
            bufferedWriter.newLine();
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //                          Bot logic methods
    // =====================================================================
    // =====================================================================
    private List<GameObject> getNearestObjects(List<GameObject> objectList, String category) {
        var filteredList = objectList;
        if (category == "ENEMY") {
            filteredList = objectList
                        .stream().filter(item -> item.getGameObjectType() == ObjectTypes.PLAYER && item.getId() != this.bot.getId())
                        .sorted(Comparator
                                .comparing(item -> getActualDistance(bot, item)))
                        .collect(Collectors.toList());

        } else if (category == "CONSUMABLES") {
            filteredList = objectList
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD || item.getGameObjectType() == ObjectTypes.SUPERFOOD || item.getGameObjectType() == ObjectTypes.SUPERNOVA_PICKUP)
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
        var filteredList = objectList
                    .stream().filter(item -> (
                        item.getGameObjectType() == ObjectTypes.FOOD ||
                        item.getGameObjectType() == ObjectTypes.SUPERFOOD ||
                        item.getGameObjectType() == ObjectTypes.SUPERNOVA_PICKUP
                    ) && (
                        getHeadingBetween(item) > (getHeadingBetween(threat) + maxDegree) % 360 ||
                        getHeadingBetween(item) < (getHeadingBetween(threat) - minDegree) % 360
                    ))
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());

        if (!filteredList.isEmpty()) {
            System.out.println("Runnnn");
            return getHeadingBetween(filteredList.get(0));
        } else {
            filteredList = this.gameState.getGameObjects()
                    .stream().filter(item -> (
                        item.getGameObjectType() == ObjectTypes.FOOD ||
                        item.getGameObjectType() == ObjectTypes.SUPERFOOD ||
                        item.getGameObjectType() == ObjectTypes.SUPERNOVA_PICKUP
                    ) && (
                        getHeadingBetween(item) > (getHeadingBetween(threat) + maxDegree) % 360 ||
                        getHeadingBetween(item) < (getHeadingBetween(threat) - minDegree) % 360
                    ))
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());
            if (!filteredList.isEmpty()) {
                System.out.println("Cari section lain..");
                return getHeadingBetween(filteredList.get(0));
            } else {
                System.out.println("Dahlah...");
                return new Random().nextInt(360);
            }
        }
    }

    private double getDistanceToNearestBorder(GameObject object) {
        double triangleX = Math.abs(object.getPosition().x);
        double triangleY = Math.abs(object.getPosition().y);
        double distanceToCenter = Math.sqrt(triangleX * triangleX + triangleY * triangleY);
        return gameState.getWorld().getRadius() - distanceToCenter - 0.5 * object.getSize();
    }

    private List<GameObject> filterObjectListByHeading(List<GameObject> objects, double minDegree, double maxDegree, int radius){
        return objects.stream().filter(item -> getActualDistance(this.bot, item) <= radius && (getHeadingBetween(item) >= maxDegree || getHeadingBetween(item) <= minDegree)).collect(Collectors.toList());
    }

    private double getTotalObjectScore(List<GameObject> objects, double w0, double w1, double w2, double w3) {
        double enemyScore = 0;
        double consumableScore = 0;
        double obstacleScore = 0;
        double weaponScore = 0;
        
        for (GameObject item: objects) {
            if (item.getGameObjectType() == ObjectTypes.FOOD && item.getId() != this.bot.getId()) {
                // ENEMY TYPE
                if (item.getSize() >= this.bot.getSize()) {
                    enemyScore += item.getSize();
                } else {
                    enemyScore -= item.getSize();
                }
            } else if (item.getGameObjectType() == ObjectTypes.FOOD || item.getGameObjectType() == ObjectTypes.SUPERFOOD || item.getGameObjectType() == ObjectTypes.SUPERNOVA_PICKUP) {
                // CONSUMABLE TYPE
                consumableScore += item.getSize();
            } else if (item.getGameObjectType() == ObjectTypes.GAS_CLOUD || item.getGameObjectType() == ObjectTypes.ASTEROID_FIELD) {
                // OBSTACLE TYPE
                obstacleScore -= item.getSize();
            } else if (item.getGameObjectType() == ObjectTypes.TORPEDO_SALVO || item.getGameObjectType() == ObjectTypes.SUPERNOVA_BOMB) {
                // WEAPONS TYPE
                weaponScore -= item.getSize();
            } else {
                continue;
            }
        }

        return (w0*enemyScore + w1*consumableScore + w2*obstacleScore + w3*weaponScore);
    }

    private List<GameObject> getHighestScoreSection(List<GameObject> objects, List<Double> weights, int n_sections, int radius) {
        int minDegree = 0;
        int maxDegree = (360/n_sections) % 360;
        int increment = (360/n_sections) % 360;

        double w0 = weights.get(0);
        double w1 = weights.get(1);
        double w2 = weights.get(2);
        double w3 = weights.get(3);

        List<GameObject> maxScoreSection = filterObjectListByHeading(objects, minDegree, maxDegree, radius);
        double maxScore = getTotalObjectScore(maxScoreSection, w0, w1, w2, w3);
        minDegree += increment;
        maxDegree += increment;

        for (int i = 1; i < n_sections; i++) {
            List<GameObject> section = filterObjectListByHeading(objects, minDegree, maxDegree, radius);
            double sectionScore = getTotalObjectScore(section, w0, w1, w2, w3);
            
            if (sectionScore > maxScore) {
                maxScore = sectionScore;
                maxScoreSection = section;
            }
            
            minDegree += increment;
            maxDegree += increment;
        }

        return maxScoreSection;
    }

    private int getOptimalHeading(List<Double> weights, int radius, int n_section, int toll) {
        // Hyperparameters:
        // w0, w1, w2, w3: Weight factor for enemy, consumables, obstacles, and weapons score respectively
        // radius: Radius of concern
        // n_section: Map scoring partitions
        // toll: Tollerance distance to nearby enemies

        // Initialize heading
        int bestHeading = new Random().nextInt(360);

        // Get all in-game objects
        List<GameObject> allObjects = this.getGameState().getPlayerGameObjects();
        allObjects.addAll(this.getGameState().getGameObjects());

        // Segregate object list and score by heading sections
        List<GameObject> bestSection = getHighestScoreSection(allObjects, weights, n_section, radius);
        // Calculate best heading for the selected section
        if (!allObjects.isEmpty()) {
            // Consumables
            List <GameObject> nearestConsumablesList = getNearestObjects(bestSection, "CONSUMABLES").stream().filter(item -> getDistanceToNearestBorder(item) > (0.9 * this.bot.getSize())).collect(Collectors.toList());
            GameObject nearestConsumable = nearestConsumablesList.get(0);

            // Enemy bots
            List <GameObject> nearestEnemiesList = getNearestObjects(this.gameState.getPlayerGameObjects(), "ENEMY");
            GameObject nearestEnemy = nearestEnemiesList.get(0);
            double distanceToEnemy = getActualDistance(this.bot, nearestEnemy) - 0.5 * this.bot.getSize() - 0.5 * nearestEnemy.getSize();
            
            // Consider nearest enemies
            if (distanceToEnemy < toll) {
                if (nearestEnemy.getSize() >= this.bot.getSize()) {
                    bestHeading = avoidThreatHeading(bestSection, nearestEnemy, 45, 45);
                    System.out.println(String.valueOf(distanceToEnemy));
                } else if (Math.abs(nearestEnemy.getSize() - this.bot.getSize()) > 10.0) {
                    bestHeading = getHeadingBetween(nearestEnemy);
                    System.out.println("Kejar bot nih");
                } else {
                    bestHeading = getHeadingBetween(nearestConsumable);
                    System.out.println("Farming dulu deh.." + String.valueOf(distanceToEnemy));
                }
            // Consider nearest consumables
            } else if (!nearestConsumablesList.isEmpty()) {
                bestHeading = getHeadingBetween(nearestConsumable);
                System.out.println("Nom nom");
            // No object of concern
            } else {
                // To the center
                bestHeading = toDegrees(Math.atan2(0 - bot.getPosition().y,
                                0 - bot.getPosition().x));
                System.out.println("void..");
            }
        }

        return bestHeading;
    }
}