package org.bjason.scalabattle.game

import com.badlogic.gdx.math.Polygon

trait MyCollision {
  val points:Array[Float]

  lazy val polygon = new Polygon(points)

  // WHY? Because stage wasnt always removing actors
  val hash = GameScreen.getHashCounter
  override def hashCode(): Int = {
    hash
  }
  override def equals(obj: Any): Boolean = {
    hash == obj.hashCode()
  }
}
