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
  lazy val textures = List(
    makeTexture(Color.RED,(size + 1) * 2, (size + 1) * 2, size, size),
    makeTexture(Color.GREEN,(size + 1) * 2, (size + 1) * 2, size, size)
  )

  val player=0
  val alien=1

  def makeTexture(colour:Color,w: Int, h: Int, ax: Int, ay: Int): Texture = {
    val pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888)
    pixmap.setColor(colour)
    pixmap.drawRectangle(0,0,w-1,h-1)

    val texture = new Texture(pixmap)
    pixmap.dispose()
    texture
  }
}
case class Torpedo(me:Int,startx: Int, starty: Int, startDir: Vector2, degrees: Float,speed:Float=12,var timeToLive:Float=40,whichTexture:Int = TextureCache.player) extends Actor with MyCollision {

  val dir = startDir.cpy
  val texture = TextureCache.textures(whichTexture)
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
}
