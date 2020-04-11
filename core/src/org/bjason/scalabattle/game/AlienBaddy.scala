package org.bjason.scalabattle.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.{Color, Pixmap, Texture}
import com.badlogic.gdx.math.{Polygon, Vector2}
import com.badlogic.gdx.scenes.scene2d.Actor


case class AlienBaddy(startx: Int, starty: Int, player: Player) extends Actor with MyCollision {

  var deadAnimate = 0
  val DEAD_TIME = 501
  val size = 1
  var speed = 1f
  var timeBetweenFire = 0f
  var ttl = 1000f
  val MOVE_OFF_SCREEN_NOW_SPEED = 0.05f
  val SLOW_TURN = 0.5f

  val points = Array[Float](
    -40 * size, 40 * size,
    -40 * size, 0 * size,
    -15 * size, -40 * size,
    15 * size, -40 * size,
    40 * size, 0 * size,
    40 * size, 40 * size,
    20, 40,
    20, 10,
    -20, 10,
    -20, 40
  )

  val texture = makeTexture((81 * size) toInt, (81 * size).toInt, (40 * size).toInt, (40 * size).toInt, points)

  setSize(texture.getWidth, texture.getHeight)
  setPosition(startx, starty)
  rotateBy(90)
  val dir = new Vector2(0, 1)

  def makeTexture(w: Int, h: Int, ax: Int, ay: Int, points: Array[Float]): Texture = {
    val pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888)
    pixmap.setColor(Color.LIME)

    var p = Array(points(0), points(1))
    var dontSkip = false
    for (n <- points.grouped(2)) {
      if (dontSkip) {
        pixmap.drawLine(ax + p(0).toInt, ay + p(1).toInt, ax + n(0).toInt, ay + n(1).toInt)
        pixmap.drawLine(ax + p(0).toInt, ay + p(1).toInt+1, ax + n(0).toInt, ay + n(1).toInt+1)
        pixmap.drawLine(ax + p(0).toInt+1, ay + p(1).toInt, ax + n(0).toInt+1, ay + n(1).toInt)
      }
      dontSkip = true
      p = n
    }
    pixmap.drawLine(ax + p(0).toInt, ay + p(1).toInt, ax + points(0).toInt, ay + points(1).toInt)
    val texture = new Texture(pixmap)
    pixmap.dispose()
    texture
  }

  def doRotate(by: Float): Unit = {
    polygon.rotate(by)
    dir.rotate(by)
    rotateBy(by)

  }

  val differenceToPlayer = new Vector2
  var chasePlayer = true


  override def draw(batch: Batch, parentAlpha: Float) {
    timeBetweenFire = timeBetweenFire + parentAlpha
    val currentPosition = new Vector2(getX, getY)
    var angle = 0
    if (chasePlayer) {
      dir.set(player.getX, player.getY)
      val d = currentPosition.dst(dir)
      val vx = (player.getX - getX) / d
      val vy = (player.getY - getY) / d
      dir.set(vx, vy)
      angle = (dir.angle() - 90).toInt
      if (angle < 0) angle = angle + 360
    } else {
      speed = speed +MOVE_OFF_SCREEN_NOW_SPEED
    }
    if (chasePlayer && angle > getRotation.toInt) {
      doRotate(SLOW_TURN)
    } else if (chasePlayer && angle < getRotation.toInt) {
      doRotate(-SLOW_TURN)
    } else {
      setY(getY + dir.y * speed + Gdx.graphics.getDeltaTime)
      setX(getX + dir.x * speed + Gdx.graphics.getDeltaTime)
      if (chasePlayer && timeBetweenFire.toInt % 100 == 0) {
        val t = Torpedo(hashCode(), getX.toInt, getY.toInt, dir, this.getRotation, speed = 3, timeToLive = 250, whichTexture = TextureCache.alien)
        this.getStage().addActor(t)
        GameScreen.torpedos += t
      }
    }
    polygon.setPosition(getX, getY)

    val w = getWidth.toInt
    val h = getHeight.toInt
    batch.draw(texture, getX() - w / 2, getY() - h / 2, w / 2, h / 2, w, h, 1,
      1, getRotation, 0, 0, w, h, false, false)

    ttl = ttl - parentAlpha
    if (ttl <= 0) {
      GameScreen.toRemove += this
    }
    if ( ttl < 150 ) {
      chasePlayer = false
      dir.set(0,1)
      dir.rotate(getRotation)
    }

  }

  override def dispose(): Unit = {
    texture.dispose()
  }

}


