package Services;

import Models.*;
import Enums.*;

import java.util.*;
import java.util.stream.*;

public class Bot {
  // PROTECTED ATTRIBUTES
  protected int toll = 400;
  protected GameObject bot;
  protected PlayerAction playerAction;
  protected GameState gameState;
  protected GameObject centerPoint = new GameObject(null, null, null, null, new Position(), null, null, null, null,
      null, null);
  protected boolean isFiringTeleporter = false;
  protected int fireTeleporterTick = 0;

  // PRIVATE METHOD
  private void updateSelfState() {
    Optional<GameObject> optionalBot = gameState.getPlayerGameObjects().stream()
        .filter(gameObject -> gameObject.id.equals(bot.id)).findAny();
    optionalBot.ifPresent(bot -> this.bot = bot);
  }

  // PUBLIC METHOD
  // public Bot() {
  // this.playerAction = new PlayerAction();
  // this.gameState = new GameState();
  // }

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
    this.updateSelfState();
  }

  public GameObject getCenterPoint() {
    return this.centerPoint;
  }

  public int getToll() {
    return this.toll;
  }

  public int getHeadingBetween(GameObject otherObject) {
    int direction = BotUtil.toDegrees(Math.atan2(otherObject.getPosition().y - this.bot.getPosition().y,
        otherObject.getPosition().x - this.bot.getPosition().x));
    return (direction + 360) % 360;
  }

  public List<GameObject> getNearestObjects(List<GameObject> objectList, String category) {
    if (objectList.isEmpty() || objectList == null) {
      return new ArrayList<GameObject>(10);
    }

    var filteredList = objectList;
    if (category == "ENEMY") {
      filteredList = objectList
          .stream().filter(item -> item.getGameObjectType() == ObjectTypes.PLAYER && item.getId() != this.bot.getId())
          .sorted(Comparator
              .comparing(item -> BotUtil.getActualDistance(bot, item)))
          .collect(Collectors.toList());

    } else if (category == "CONSUMABLES") {
      filteredList = objectList
          .stream()
          .filter(
              item -> item.getGameObjectType() == ObjectTypes.FOOD || item.getGameObjectType() == ObjectTypes.SUPERFOOD
                  || item.getGameObjectType() == ObjectTypes.SUPERNOVAPICKUP
                      && BotUtil.getDistanceToNearestBorder(item, this.gameState) > this.bot.getSize() * 1.75)
          .sorted(Comparator
              .comparing(item -> BotUtil.getActualDistance(bot, item)))
          .collect(Collectors.toList());

    } else if (category == "OBSTACLES") {
      filteredList = objectList
          .stream()
          .filter(item -> item.getGameObjectType() == ObjectTypes.GAS_CLOUD
              || item.getGameObjectType() == ObjectTypes.ASTEROID_FIELD)
          .sorted(Comparator
              .comparing(item -> BotUtil.getActualDistance(bot, item)))
          .collect(Collectors.toList());

    } else if (category == "WEAPONS") {
      filteredList = objectList
          .stream()
          .filter(item -> item.getGameObjectType() == ObjectTypes.TORPEDOSALVO
              || item.getGameObjectType() == ObjectTypes.SUPERNOVABOMB)
          .sorted(Comparator
              .comparing(item -> BotUtil.getActualDistance(bot, item)))
          .collect(Collectors.toList());

    } else if (category == "TRAVERSAL") {
      filteredList = objectList
          .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER)
          .sorted(Comparator
              .comparing(item -> BotUtil.getActualDistance(bot, item)))
          .collect(Collectors.toList());
    } else {
      filteredList = objectList
          .stream().sorted(Comparator
              .comparing(item -> BotUtil.getActualDistance(bot, item)))
          .collect(Collectors.toList());
    }

    return filteredList;
  }

  public boolean isAfterburnerActive() {
    return (this.bot.getEffects() & 0x00001) == 1;
  }

  public boolean isInAsteroidField() {
    return (this.bot.getEffects() & 0x00010) == 1;
  }

  public boolean isInGasCloud() {
    return (this.bot.getEffects() & 0x00100) == 1;
  }

  public boolean isOnSuperfood() {
    return (this.bot.getEffects() & 0x01000) == 1;
  }

  public boolean isShieldActive() {
    return (this.bot.getEffects() & 0x10000) == 1;
  }
}
