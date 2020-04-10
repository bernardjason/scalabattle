package org.bjason.scalabattle.game

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.{Color, Pixmap, Texture}
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor

object TextureCache {
  val size = 2
  val points = Array[Float](
    -size, -size,
    -size, size,
    size, size,
    size, -size
  )
  lazy val texture = makeTexture((size + 1) * 2, (size + 1) * 2, size, size)

  def makeTexture(w: Int, h: Int, ax: Int, ay: Int): Texture = {

    val pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888)
    pixmap.setColor(Color.RED)

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
case class Torpedo(startx: Int, starty: Int, startDir: Vector2, degrees: Float) extends Actor with MyCollision {

  val dir = startDir.cpy
  var timeToLive = 40f
  val speed = 12
  val texture = TextureCache.texture
  val points = TextureCache.points

  setPosition(startx, starty)
  setSize(texture.getWidth, texture.getHeight)
  setRotation(degrees)

  override def draw(batch: Batch, parentAlpha: Float): Unit = {
    val apply = dir.cpy().scl(speed * parentAlpha)
    setPosition(getX + apply.x, getY + apply.y)
    polygon.setPosition(getX, getY)
    batch.draw(texture, getX - getWidth / 2, getY - getHeight / 2)
    timeToLive = timeToLive - parentAlpha
    if (timeToLive <= 0) {
      GameScreen.torpedos -= this
      this.getParent.removeActor(this)
      dispose()
    }
  }



  def dispose(): Unit = {
    //texture.dispose()
  }
}
