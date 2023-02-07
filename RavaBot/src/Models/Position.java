package Models;

import Models.GameObject.Effects;

public class Position {

  public int x;
  public int y;

  public Position() {
    x = 0;
    y = 0;
  }

  public int GetHashCode(Effects E){
     res = this.x * (0x00010000) + this.y;
     return res;
  } 

  public Position(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  //radius
  public void radius(){
    int res;
    this.x = getX() + 3;
    this.y = gety() + 3;
  }
}
