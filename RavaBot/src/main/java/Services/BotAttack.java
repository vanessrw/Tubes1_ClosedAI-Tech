package Services;

import Models.*;
import Enums.*;

public class BotAttack {
  // PUBLIC METHOD
  public static void attackEnemy(Bot player, GameObject target) {
    PlayerAction playerAction = new PlayerAction();

    if (BotUtil.getActualDistance(player.getBot(), target) <= player.getToll() / 2) {
      playerAction.action = PlayerActions.FIRETORPEDOES;
      playerAction.heading = player.getHeadingBetween(target);
    } else if (player.getBot().getSize() >= 30) {
      playerAction.action = PlayerActions.STARTAFTERBURNER;
      playerAction.heading = player.getHeadingBetween(target);
    } else {
      playerAction.action = PlayerActions.FORWARD;
      playerAction.heading = player.getHeadingBetween(target);
    }

    player.setPlayerAction(playerAction);
  }
}
