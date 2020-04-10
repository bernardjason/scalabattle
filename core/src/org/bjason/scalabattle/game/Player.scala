package org.bjason.scalabattle.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.{Color, Pixmap, Texture}
import com.badlogic.gdx.math.{Polygon, Vector2}
import com.badlogic.gdx.scenes.scene2d.Actor


class Player(startx: Int, starty: Int) extends Actor with MyCollision with CareAboutKeyboard {

  val DEAD_TIME = 500
  val size = 64
  val speed = 10
  var timeBetweenFire = 0f
  val pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888)
  pixmap.setColor(Color.CYAN)
  pixmap.drawLine(0, size, size / 2, 0)
  pixmap.drawLine(size / 2, 0, size, size)
  pixmap.setColor(Color.YELLOW)
  pixmap.drawLine(0, size - 1, size - 1, size - 1)
  var deadAnimate = 0

  val texture = new Texture(pixmap)
  pixmap.dispose()
  setSize(texture.getWidth, texture.getHeight)
  setPosition(startx, starty)
  GameKeyboard.listeners += this
  val dir = new Vector2(0, 1)

  override val points: Array[Float] = Array[Float](
    -texture.getWidth / 2, -texture.getHeight / 2,
    0, texture.getHeight / 2,
    texture.getWidth / 2, -texture.getDepth / 2
  )

  override def forward(): Unit = {
    if (deadAnimate <= DEAD_TIME / 2) {
      setY(getY + dir.y * speed + Gdx.graphics.getDeltaTime)
      setX(getX + dir.x * speed + Gdx.graphics.getDeltaTime)
      val xx = getX
      val yy = getY
      if ( Math.abs(xx) > Universe.universeSize ) {
        setX(xx * -1)
      }
      if ( Math.abs(yy) > Universe.universeSize ) {
        setY(yy * -1)
      }
      GameScreen.cam.position.x = getX
      GameScreen.cam.position.y = getY
    }
  }

  override def left(): Unit = {
    doRotate(100 * Gdx.graphics.getDeltaTime)
  }

  override def right(): Unit = {
    doRotate(-100 * Gdx.graphics.getDeltaTime)
  }

  private def doRotate(by: Float) = {
    this.rotateBy(by)
    dir.rotate(by)
    polygon.rotate(by)
    GameScreen.cam.rotate(by, 0, 0, 1)
  }


  override def fire(): Unit = {
    if (deadAnimate <= DEAD_TIME / 2) {
      if (timeBetweenFire <= 0) {
        val t = Torpedo(getX.toInt, getY.toInt, dir, this.getRotation)
        this.getStage().addActor(t)
        GameScreen.torpedos += t
        timeBetweenFire = 10
      }
    }
  }


  override def draw(batch: Batch, parentAlpha: Float) {
    timeBetweenFire = timeBetweenFire - parentAlpha
    polygon.setPosition(getX, getY)

    val w = getWidth.toInt
    val h = getHeight.toInt
    val rotation = getRotation()
    if (deadAnimate <= DEAD_TIME / 2) {
      if ( deadAnimate <= 0 || deadAnimate % 20 < 10 ) {
        batch.draw(texture, getX() - w / 2, getY() - h / 2, w / 2, h / 2, w, h, 1,
          1, rotation, 0, 0, w, h, false, false)
      }
    }
  }

  def newScreen = {
    deadAnimate = DEAD_TIME/2 -1
  }

  def blowUp = {
    if (!isDeadAnimate()) {
      GameScreen.lives = GameScreen.lives - 1
      deadAnimate = DEAD_TIME
      val deadPolygonAnimate = new Polygon(deadPolygon)
      deadPolygonAnimate.setPosition(Gdx.graphics.getWidth / 2, Gdx.graphics.getHeight / 2)
      val rotate = (Math.random() * 1000 % 360).toFloat
      deadPolygonAnimate.rotate(rotate)
      deadLines = deadPolygonAnimate.getTransformedVertices

    }
  }

  def isDeadAnimate() = {
    deadAnimate > 0
  }

  var deadLines: Array[Float] = null
  val deadPolygon = Array[Float](
    -size / 2, -size / 2,
    size / 2, -size / 2,
    -size / 2, -size / 2,
    0, size / 2,
    0, size / 2,
    size / 2, -size / 2
  )

  def renderDead(shapeRenderer: ShapeRenderer): Unit = {
    deadAnimate = deadAnimate - 1
    if (isDeadAnimate() && deadAnimate > DEAD_TIME/2) {
      shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
      shapeRenderer.setColor(Color.CYAN)
      shapeRenderer.line(deadLines(0), deadLines(1), deadLines(2), deadLines(3))
      shapeRenderer.line(deadLines(4), deadLines(5), deadLines(6), deadLines(7))
      shapeRenderer.setColor(Color.YELLOW)
      shapeRenderer.line(deadLines(8), deadLines(9), deadLines(10), deadLines(11))

      def addTo(x: Float, y: Float, i: Int) = {
        deadLines(i) = deadLines(i) + x
        deadLines(i + 1) = deadLines(i + 1) + y
        deadLines(i + 2) = deadLines(i + 2) + x
        deadLines(i + 3) = deadLines(i + 3) + y
      }

      addTo(-1, 1, 0)
      addTo(1, 0, 4)
      addTo(-1, -1, 8)

      shapeRenderer.end()
    }
  }

  def dispose(): Unit = {
    texture.dispose()
  }

}

