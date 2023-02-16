package Enums;

public enum PlayerActions {
  FORWARD(1),
  STOP(2),
  STARTAFTERBURNER(3),
  STOPAFTERBURNER(4),
  FIRETORPEDOES(5),
  FIRESUPERNOVA(6),
<<<<<<< HEAD
  DETONATE_SUPERNOVA(7),
=======
  DETONATESUPERNOVA(7),
>>>>>>> 3c419f3fa95a78e778e4ace4fa4740d7a6ae5f90
  FIRETELEPORT(8),
  TELEPORT(9),
  ACTIVATESHIELD(10);

  public final Integer value;

  private PlayerActions(Integer value) {
    this.value = value;
  }
}
