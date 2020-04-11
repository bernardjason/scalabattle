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

import scala.collection.mutable.{ArrayBuffer, ListBuffer}

object GameScreen {

  var hashCounter = 1

  def getHashCounter = {
    hashCounter = hashCounter + 1
    hashCounter
  }

  val torpedos = new scala.collection.mutable.ListBuffer[Torpedo]()
  val asteroids = new scala.collection.mutable.ListBuffer[Actor with MyCollision]()
  val alienBaddy = new scala.collection.mutable.ListBuffer[Actor with MyCollision]()
  val toRemove = new ArrayBuffer[Actor]()
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
    GameScreen.alienBaddy.clear()
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
  var countDownToBaddy = 1f


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
    Universe.addStars(stage, player)
  }

  private def addBaddyIfWeWant(deltaTime: Float): Unit = {
    countDownToBaddy = countDownToBaddy - deltaTime
    if (countDownToBaddy <= 0 && GameScreen.alienBaddy.length < 2 ) {
      def plusMinus = {
        val decide = (Math.random() * 256).toInt % 2
        if ( decide == 0 ) -1 else 1
      }

      val alienBaddy = AlienBaddy(player.getX.toInt, player.getY.toInt + (400 * plusMinus), player)
      GameScreen.alienBaddy += alienBaddy
      stage.addActor(alienBaddy)
      countDownToBaddy = 10f+(Math.random()*1000 % 30).toFloat
    }
  }

  private def playfreshBatchOfAsteroids: Unit = {
    addAsteroid(Gdx.graphics.getWidth / 2, Gdx.graphics.getHeight / 2)
  }

  private def justOne_freshBatchOfAsteroids: Unit = {
    val xx = 200
    val yy = 200
    addAsteroid(xx + Gdx.graphics.getWidth / 2, yy + Gdx.graphics.getHeight / 2)
  }

  private def freshBatchOfAsteroids {
    val spaceOut = 250

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

    GameScreen.toRemove.clear()

    GameKeyboard.render()
    GameScreen.cam.update()

    addBaddyIfWeWant(Gdx.graphics.getDeltaTime)

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

    var player_hit = checkPlayerAgainst(GameScreen.asteroids)
    if (!player_hit) {
      player_hit = checkPlayerAgainst(GameScreen.alienBaddy, (x, y) => {
        AlienExplode.makeExplosion(x, y, stage)
      })
    }
    checkTorpedoCollision(GameScreen.asteroids)
    checkTorpedoCollision(GameScreen.alienBaddy, (x, y) => {
      AlienExplode.makeExplosion(x, y, stage)
    })

    if (player_hit) {
      player.blowUp
      Sound.playExplode
      for(a <- GameScreen.alienBaddy) {
        a.asInstanceOf[AlienBaddy].chasePlayer  = false
      }
    }

    for (r <- GameScreen.toRemove) {
      stage.getRoot.removeActor(r)
      r.setVisible(false)
      r match {
        case a: Asteroid => removeAsteroid(a)
        case a: AlienBaddy => GameScreen.alienBaddy -= a
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
    if ( GameScreen.alienBaddy.length > 0 ) {
      Sound.playWarning
    }

  }

  def checkPlayerAgainst(against: ListBuffer[Actor with MyCollision], extra: (Int, Int) => Unit = (_, _) => {}) = {
    var player_hit = false
    if (!player.isDeadAnimate()) {
      for (xy <- player.polygon.getTransformedVertices.grouped(2)) {
        for (a <- against) {
          if (!player_hit && a.isVisible && a.polygon.contains(xy(0), xy(1))) {
            player_hit = true
            GameScreen.toRemove += a
            a.setVisible(false)
            extra(a.getX.toInt, a.getY.toInt)
          }
        }
      }
      GameScreen.torpedos.filter( _.me != player.hashCode() ).map { t =>
        if ( player.polygon.contains(t.getX , t.getY )) {
          player_hit = true
        }
      }
    }
    player_hit
  }

  def checkTorpedoCollision(against: ListBuffer[Actor with MyCollision], extra: (Int, Int) => Unit = (_, _) => {}): Unit = {
    for (t <- GameScreen.torpedos) {
      var torpedoDone = false
      for (o <- against) {
        if (o.hashCode() != t.me && !torpedoDone && o.isVisible && o.polygon.contains(t.getX, t.getY)) {
          GameScreen.score = GameScreen.score + 1
          GameScreen.toRemove += o
          GameScreen.toRemove += t
          o.setVisible(false)
          torpedoDone = true
          extra(t.getX.toInt, t.getY.toInt)
        }
      }
    }
  }

  def clearUp() = {
    for (a <- GameScreen.asteroids) {
      a.dispose()
    }
    for (t <- GameScreen.torpedos) {
      t.dispose()
    }
    for (a <- GameScreen.alienBaddy) {
      a.dispose()
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


