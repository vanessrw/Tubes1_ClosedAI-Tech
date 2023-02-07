package Enums;

public enum PlayerActions {
  FORWARD(1),
  STOP(2),
  STARTAFTERBURNER(3),
  STOPAFTERBURNER(4),
  FIRETORPEDOS(5),
  FIRESUPERNOVA(6),
  DETONATESUPERNOVA(7),
  FIRETELEPORT(8),
  TELEPORT(9),
  ACTIVATE_SHIELD(10);

  public final Integer value;

  private PlayerActions(Integer value) {
    this.value = value;
  }
}
