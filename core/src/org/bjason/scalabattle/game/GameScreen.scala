package org.bjason.scalabattle.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics._
import com.badlogic.gdx.scenes.scene2d.{Actor, Stage}
import com.badlogic.gdx.utils.viewport.ScreenViewport
import org.bjason.scalabattle.{DrawAscii, LoopTrait}

import scala.collection.mutable.ArrayBuffer

object GameScreen {

  var hashCounter = 1
  def getHashCounter= {
    hashCounter=hashCounter+1
    hashCounter
  }
  val torpedos = new scala.collection.mutable.ListBuffer[Torpedo]()
  val asteroids = new scala.collection.mutable.ListBuffer[Asteroid]()
  var cam: Camera = null
  lazy val font = getFont(20)
  var score = 0
  var lives = 0
  val LIVES = 3
  var difficult = 1

  def getMaxSizeForAsteroid = {
    Gdx.graphics.getWidth
  }

  def reset(): Unit = {
    cam = new OrthographicCamera(Gdx.graphics.getWidth, Gdx.graphics.getHeight)
    GameScreen.torpedos.clear()
    GameScreen.asteroids.clear()
    score = 0
    lives = LIVES
    difficult = 1
  }

  def getFont(size: Int) = {

    val fontFile = Gdx.files.internal("data/OpenSans-Italic.ttf")
    val generator = new FreeTypeFontGenerator(fontFile)
    val parameter = new FreeTypeFontParameter()
    parameter.color = Color.WHITE
    parameter.size = size

    val font = generator.generateFont(parameter)
    generator.dispose()
    font
  }


}

class GameScreen extends LoopTrait {
  lazy val ascii = DrawAscii()
  var soft: Texture = null
  var counter = 0
  var stage: Stage = null
  lazy val batch = new SpriteBatch()
  lazy val textBatch = new SpriteBatch()
  lazy val shapeRenderer = new ShapeRenderer()
  var player: Player = null


  override def firstScreenSetup(): Unit = {
    GameScreen.reset
    GameKeyboard.reset
    stage = new Stage(new ScreenViewport(GameScreen.cam))

    super.firstScreenSetup()
    soft = new Texture(ascii.getPixmapForString("GAME " + counter, 1624, 350, Color.YELLOW))
    counter = counter + 1
    player = new Player(Gdx.graphics.getWidth / 2, Gdx.graphics.getHeight / 2)
    stage.clear()
    GameScreen.cam.position.set(GameScreen.cam.viewportWidth / 2f, GameScreen.cam.viewportHeight / 2f, 0)

    GameScreen.cam.update()
    freshBatchOfAsteroids
    player.newScreen
    stage.addActor(player)
    Universe.addStars(stage,player)
  }

  private def playfreshBatchOfAsteroids: Unit = {
    addAsteroid(Gdx.graphics.getWidth / 2, Gdx.graphics.getHeight / 2)
  }

  private def freshBatchOfAsteroids {
    val spaceOut = 150

    val level = GameScreen.difficult + 1
    for (xx <- -spaceOut * level to spaceOut * level by spaceOut) {
      for (yy <- -spaceOut * level to spaceOut * level by spaceOut) {
        if (xx != 0 && yy != 0) {
          addAsteroid(xx + Gdx.graphics.getWidth / 2, yy + Gdx.graphics.getHeight / 2)
        }
      }
    }
  }

  private def addAsteroid(xx: Int, yy: Int, size: Float = 1.5f) = {
    val asteroid = Asteroid(xx, yy, size)
    stage.addActor(asteroid)
    GameScreen.asteroids += asteroid
  }

  def removeAsteroid(a: Asteroid): Unit = {
    GameScreen.asteroids -= a
    a.dispose()
    if (a.size > 0.5f) {
      val size = a.size * 0.6f
      addAsteroid((a.getX() + 20).toInt, a.getY.toInt, size)
      addAsteroid((a.getX() - 20).toInt, a.getY.toInt, size)
    }
  }

  override def create(): Unit = {
    Gdx.input.setInputProcessor(GameKeyboard)

  }


  override def render(): Unit = {
    Gdx.gl.glClearColor(0, 0.0f, 0, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    GameKeyboard.render()
    GameScreen.cam.update()

    textBatch.begin()
    GameScreen.font.draw(textBatch,
      "Score " + GameScreen.score + " lives " + GameScreen.lives + " level=" + GameScreen.difficult + " fps=" + Gdx.graphics.getFramesPerSecond, 0, Gdx.graphics.getHeight - 5)
    textBatch.end()

    batch.setProjectionMatrix(GameScreen.cam.combined)
    batch.begin()
    stage.draw()
    batch.end()

    player.renderDead(shapeRenderer)
    Radar.render(shapeRenderer, player.getX, player.getY, player.getRotation)

    var player_hit = false
    val toRemove = new ArrayBuffer[Actor]()

    if (!player.isDeadAnimate()) {
      for (xy <- player.polygon.getTransformedVertices.grouped(2)) {
        for (asteroid <- GameScreen.asteroids) {
          if (!player_hit && asteroid.isVisible && asteroid.polygon.contains(xy(0), xy(1))) {
            player_hit = true
            toRemove += asteroid
            asteroid.setVisible(false)
            asteroid.dead = true
          }
        }
      }
    }
    for (t <- GameScreen.torpedos) {
      var torpedoDone = false
      for (asteroid <- GameScreen.asteroids) {
        if (!torpedoDone && asteroid.polygon.contains(t.getX, t.getY)) {
          GameScreen.score = GameScreen.score + 1
          toRemove += asteroid
          toRemove += t
          asteroid.setVisible(false)
          torpedoDone = true
          asteroid.dead = true
        }
      }
    }
    if (player_hit) {
      player.blowUp
    }

    for (r <- toRemove) {
      stage.getRoot.removeActor(r)
      r.setVisible(false)
      r match {
        case a: Asteroid => removeAsteroid(a)
        case t: Torpedo => GameScreen.torpedos -= t
        case _ => println("Ignore ")
      }
    }

    if (GameScreen.lives <= 0 || GameKeyboard.escape == true) {
      clearUp()
      this.screenComplete = true
    }
    if (GameScreen.asteroids.length == 0) {
      GameScreen.difficult = GameScreen.difficult + 1
      GameScreen.lives = GameScreen.lives + 1
      freshBatchOfAsteroids
      player.newScreen
    }

  }

  def clearUp() = {
    for (a <- GameScreen.asteroids) {
      a.dispose()
    }
    for (t <- GameScreen.torpedos) {
      t.dispose()
    }
    player.dispose()
    stage.dispose()

  }


  override def dispose(): Unit = {
    stage.dispose()
  }

  override def resize(width: Int, height: Int): Unit = {
    super.resize(width, height)
    stage.getViewport.update(width, height, true)
  }
}


