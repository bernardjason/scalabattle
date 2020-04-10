package org.bjason.scalabattle.game

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.{Color, Pixmap, Texture}
import com.badlogic.gdx.math.{Polygon, Vector2}
import com.badlogic.gdx.scenes.scene2d.Actor

import scala.collection.mutable.ArrayBuffer

object AsteroidCache {

  case class CachedAsteroidTexture(size: Float, texture: Texture, points: Array[Float])

  val cache = scala.collection.mutable.Map[Float, CachedAsteroidTexture]()

  def getPoints(size: Float) = {
    val points = Array[Float](
      -50 * size, 0 * size,
      -40 * size, 20 * size,
      0 * size, 50 * size,
      40 * size, 30 * size,
      50 * size, 0 * size,
      40 * size, -20 * size,
      35 * size, -30 * size,
      0 * size, -50 * size,
      -30 * size, -30 * size
    )
    points
  }

  def getTexture(size: Float): CachedAsteroidTexture = {

    if (cache.contains(size)) {
      return cache(size)
    }
    val points = getPoints(size)
    val texture = makeTexture((101 * size) toInt, (101 * size).toInt, (50 * size).toInt, (50 * size).toInt, points)

    val r = CachedAsteroidTexture(size, texture, points)
    cache(size)  = r
    r
  }

  def makeTexture(w: Int, h: Int, ax: Int, ay: Int, points: Array[Float]): Texture = {
    val pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888)
    pixmap.setColor(Color.WHITE)

    var p = Array(points(0), points(1))
    var dontSkip = false
    for (n <- points.grouped(2)) {
      if (dontSkip) {
        pixmap.drawLine(ax + p(0).toInt, ay + p(1).toInt, ax + n(0).toInt, ay + n(1).toInt)
      }
      dontSkip = true
      p = n
    }
    pixmap.drawLine(ax + p(0).toInt, ay + p(1).toInt, ax + points(0).toInt, ay + points(1).toInt)
    val texture = new Texture(pixmap)
    pixmap.dispose()
    texture
  }
}

case class Asteroid(startx: Int, starty: Int, size: Float = 2) extends Actor with MyCollision {

  var dead = false
  val dir = new Vector2(Math.random().toFloat / 2, Math.random().toFloat / 2)
  val dirX = (Math.random() * 1000).toInt % 3 - 1
  val dirY = (Math.random() * 1000).toInt % 3 - 1
  dir.scl(dirX * 8, dirY * 8)

  val texturePoints = AsteroidCache.getTexture(size)
  override val points = texturePoints.points
  val texture = texturePoints.texture
  setPosition(startx, starty)
  setSize(texture.getWidth, texture.getHeight)


  override def draw(batch: Batch, parentAlpha: Float): Unit = {
    setY(getY + dir.y)
    setX(getX + dir.x)
    polygon.setPosition(getX, getY)
    if (Math.abs(getX) > GameScreen.getMaxSizeForAsteroid || Math.abs(getY) > GameScreen.getMaxSizeForAsteroid) {
      dir.scl(-1)
    }
    batch.draw(texture, getX - getWidth / 2, getY - getHeight / 2)

  }


  def dispose() = {
    //texture.dispose()
  }
}
