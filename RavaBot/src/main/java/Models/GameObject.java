package Models;

import Enums.*;
import java.util.*;

public class GameObject {
  public UUID id;
  public Integer size;
  public Integer speed;
  public Integer currentHeading;
  public Position position;
  public ObjectTypes gameObjectType;
  public Integer effects;
  public Integer TorpedoSalvoCount;
  public Integer SupernovaAvailable;
  public Integer TeleportCount;
  public Integer ShieldCount;
  public Double score;

  public GameObject(UUID id, Integer size, Integer speed, Integer currentHeading, 
  Position position, ObjectTypes gameObjectType) {
    this.id = id;
    this.size = size;
    this.speed = speed;
    this.currentHeading = currentHeading;
    this.position = position;
    this.gameObjectType = gameObjectType;
    // this.effects = effect;
    // this.TorpedoSalvoCount = torpedo_count;
    // this.SupernovaAvailable = supernova_avail;
    // this.TeleportCount = teleport_count;
    // this.ShieldCount = shield_count;
    this.score = 0.0;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public int getSpeed() {
    return speed;
  }

  public void setSpeed(int speed) {
    this.speed = speed;
  }

  public Position getPosition() {
    return position;
  }

  public void setPosition(Position position) {
    this.position = position;
  }

  public ObjectTypes getGameObjectType() {
    return gameObjectType;
  }
  
  public void setGameObjectType(ObjectTypes gameObjectType) {
    this.gameObjectType = gameObjectType;
  }
  
  public static GameObject FromStateList(UUID id, List<Integer> stateList)
  {
    Position position = new Position(stateList.get(4), stateList.get(5));
    return new GameObject(id, stateList.get(0), stateList.get(1), stateList.get(2), position, ObjectTypes.valueOf(stateList.get(3)));
  }
  
  // public int getTorpedoCount(){
  //   return TorpedoSalvoCount;
  // }

  // public int getSupernovaCount(){
  //   return SupernovaAvailable;
  // }

  // public int getTeleportCount(){
  //   return TeleportCount;
  // }

  // public int getShieldCount(){
  //   return ShieldCount;
  // }


  // public void setEffects(Integer effects) {
  //   this.effects = effects;
  // }

  // public Integer getEffects() {
  //   return this.effects;
  // }

  // 5 statelist trakhir blm lengkap
  // public static GameObject FromStateList(UUID id, List<Integer> stateList)
  // {
  //   Position position = new Position(stateList.get(4), stateList.get(5));

  //   if (stateList.size() == 7){
  //     return new GameObject(
  //       id,
  //       stateList.get(0),
  //       stateList.get(1),
  //       stateList.get(2),
  //       position,
  //       ObjectTypes.valueOf(stateList.get(3)),
  //       0,
  //       stateList.get(6),
  //       0,
  //       0,
  //       0
  //     ); 
  //   }

  //   return new GameObject(id, stateList.get(0), stateList.get(1), stateList.get(2), position, ObjectTypes.valueOf(stateList.get(3)),
  //   stateList.get(7), stateList(6), stateList(8), stateList(10), stateList(9));
  // }
}
